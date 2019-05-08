package com.internal.voipdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class ChatActivity extends BaseActivity
{
    private static final String TAG = ChatActivity.class.getSimpleName();

    private String[] items = { "一对一聊天"};

    private final int CHAT_SINGLE = 0;

    private int chatType = CHAT_SINGLE;

    private EditText AccountIdET;

    String dstAccountId;
    String dstAppAccountId = "";

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            dstAccountId = (String) msg.obj;
            if(dstAccountId.equals(SharedPerfUtils.getAccountId(ChatActivity.this)))
            {
                showToastMessage("不能与自己聊天！");
                return;
            }
            switch (chatType)
            {
                case CHAT_SINGLE:
                    if (!TextUtils.isEmpty(dstAccountId)) {
                        Intent intent = new Intent(ChatActivity.this,SingleChatActivity.class);
                        intent.putExtra("dst_id", dstAccountId);
                        startActivity(intent);
                    } else {
                        showToastMessage("请输入对方帐号");
                    }
                    break;
                default:
                    break;
            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_common);

        AccountIdET = (EditText) findViewById(R.id.et_account);

        ListView listView = (ListView) findViewById(R.id.lv);
        listView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, items));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (!isNetworkConnected()) {
                    showToastMessage("当前无网络");
                    return;
                }

                switch (position)
                {
                    case CHAT_SINGLE:
                    {
//                        String AccountId = AccountIdET.getText().toString().trim();
//                        if (!TextUtils.isEmpty(AccountId)) {
//                            Intent intent = new Intent(ChatActivity.this,SingleChatActivity.class);
//                            intent.putExtra("dst_id", AccountId);
//                            startActivity(intent);
//                        }
                        dstAppAccountId = AccountIdET.getText().toString().trim();
                        if (!TextUtils.isEmpty(dstAppAccountId))
                        {
                            queryIdByAppAccount(dstAppAccountId, handler);
                        } else {
                            showToastMessage("请输入对方帐号");
                        }
                    }
                    break;
                    default:
                        break;
                }
            }

        });
    }
}
