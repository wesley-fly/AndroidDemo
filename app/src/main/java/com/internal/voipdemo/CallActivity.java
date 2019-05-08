package com.internal.voipdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.internal.voipmedia.MediaType;
import com.internal.voipmedia.RecCallType;
import com.internal.voipmedia.VoIPMediaAPI;

public class CallActivity extends BaseActivity
{
    private static final String TAG = CallActivity.class.getSimpleName();

    private final int CALL_AUDIO = 0;

    private int callType = CALL_AUDIO;

    private String[] items = { "语音呼叫" };

    Intent intent = null;

    String dstAccountId;

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == QUERY_APP_ACCOUNT_SUCCESS)
            {
                dstAccountId = (String) msg.obj;
                if(dstAccountId.equals(SharedPerfUtils.getAccountId(CallActivity.this)))
                {
                    showToastMessage("不能拨打自己！");
                    return;
                }
                switch (callType)
                {
                    case CALL_AUDIO:
                    {
                        MyApplication.isTalking = true;

                        String callId = VoIPMediaAPI.getInstance().makeCall(dstAccountId, MediaType.MEDIA_AUDIO);
                        if(callId==null || callId.equals(""))
                        {
                            MyApplication.isTalking = false;
                            showToastMessage("呼叫失败");
                            return;
                        }
                        intent = new Intent(CallActivity.this, TalkingActivity.class);
                        intent.putExtra("call_id", callId);
                        intent.putExtra("dst_id", dstAccountId);
                        intent.putExtra("is_callee", 0);
                        startActivity(intent);
                    }
                    break;

                    default:
                        break;
                }
            }
            else
            {
                showToastMessage("该用户不存在！");
            }
        }
    };
    private AppSimpleListener appSimpleListener = new AppSimpleListener()
    {
        @Override
        public void onReceiveCallEvent(String callId, long timestamp,String callerId, String callerName, int media, int callType)
        {
            Log.e(TAG, "onReceiveCallEvent callId : " + callId + ", timestamp : " + timestamp +", callerId : " + callerId + " ,callerName : " + callerName + " callType : " + callType);

            if (callType == RecCallType.RECEIVE_CALL_TYPE_MISSED_CALL)
            {
                Log.e(TAG, "未接来电");
                return;
            }

            if(MyApplication.isTalking == true)
            {
                Log.e(TAG, "正在通话中，直接挂断新来电");
                VoIPMediaAPI.getInstance().hangupCall(callId);
                return;
            }

            intent = new Intent(CallActivity.this, IncomingCallActivity.class);
            intent.putExtra("call_id", callId);
            intent.putExtra("caller_name", callerName);
            intent.putExtra("call_type", media);
            intent.putExtra("caller_id", callerId);
            CallActivity.this.startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_common);

        ((MyApplication) getApplication()).registerEventListener(appSimpleListener);

        final EditText accountEditText = (EditText) findViewById(R.id.et_account);

        ListView listView = (ListView) findViewById(R.id.lv);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!isNetworkConnected())
                {
                    showToastMessage("当前无网络");
                    return;
                }
                String dstAccountId = "";
                switch (position)
                {
                    case CALL_AUDIO:
                        dstAccountId = accountEditText.getText().toString().trim();
                        if (!TextUtils.isEmpty(dstAccountId))
                        {
                            queryIdByAppAccount(dstAccountId, handler);
                        }
                        else
                        {
                            showToastMessage("请输入对方账户");
                        }
                        break;
                    default:
                        break;
                }
            }
        });
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        ((MyApplication) getApplication()).unRegisterEventListener(appSimpleListener);
    }
}
