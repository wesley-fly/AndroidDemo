package com.internal.voipdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.internal.voipmedia.AccountStatus;
import com.internal.voipmedia.ParametersName;
import com.internal.voipmedia.SysEventType;
import com.internal.voipmedia.VoIPMediaAPI;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();
    protected final int LOGIN_ACCOUNT_SUCCESS = 0;
    protected final int LOGIN_ACCOUNT_FAILED = 1;
    protected final int LOGIN_ACCOUNT_PASS_LEN_ERROR = 2;
    protected final int LOGIN_ACCOUNT_LEN_ERROR = 3;
    protected final int LOGIN_SERVER_LEN_ERROR = 4;
    protected final int QUERY_APP_ACCOUNT_SUCCESS = 5;
    protected final int QUERY_APP_ACCOUNT_FAILED = 6;
    protected ProgressDialog progressDialog;

    protected void AppLoginAppAccount(final String hostServer, final String appAccount, final Handler handler)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String AccountId = "";
                int passLen = appAccount.length();
                if(passLen < 1 || passLen > 40)
                {
                    handler.sendEmptyMessage(LOGIN_ACCOUNT_LEN_ERROR);
                    return;
                }

                VoIPMediaAPI.getInstance().setSystemParams(ParametersName.VOIP_CS_SERVER,hostServer);
                AccountId = VoIPMediaAPI.getInstance().bindAccount(appAccount);
//                Log.e(TAG, "首先检查此手机号码的注册状态,E-mail:" + email );
//                int status = VoIPMediaAPI.getInstance().checkAccountByMail(email);
//
//                switch (status)
//                {
//                    case AccountStatus.ACCOUNT_USER_NEW:
//                    {
//                        Log.e(TAG, "新用户,进行注册操作,注册E-mail:" + email + ",密码:" + passWord);
//
//                        if(VoIPMediaAPI.getInstance().registerAccountByMail(email, passWord) == 0)
//                        {
//                            Log.e(TAG, "新用户注册成功,接着登陆...");
//                            AccountId = VoIPMediaAPI.getInstance().loginAccountByMail(email, passWord);
//                        }
//                    }
//                    break;
//                    case AccountStatus.ACCOUNT_USER_NORMAL:
//                    {
//                        Log.e(TAG, "已注册用户,进行登陆,登陆E-mail:" + email + ",密码:" + passWord);
//                        AccountId = VoIPMediaAPI.getInstance().loginAccountByMail(email, passWord);
//                    }
//                    break;
//                    case AccountStatus.ACCOUNT_USER_NO_PASS:
//                    {
//                        Log.e(TAG, "已注册用户,未设置密码用户,需要APP确保不会发生此情况");
//                    }
//                    break;
//                    default:
//                    {
//                        Log.e(TAG, "检测此手机号码格式不合法,或状态返回未知,或服务器不可达,状态返回:" + status );
//                    }
//                    break;
//                }

                if(AccountId.length() == 8)
                {
                    Log.e(TAG, "用户登陆成功,存储至本地APP数据");
                    SharedPerfUtils.setServerHost(BaseActivity.this, hostServer);
                    SharedPerfUtils.setAccountId(BaseActivity.this, AccountId);
                    SharedPerfUtils.setAppAccount(BaseActivity.this, appAccount);
//                    SharedPerfUtils.setPassword(BaseActivity.this, passWord);
//                    SharedPerfUtils.setEmail(BaseActivity.this, email);
                    handler.sendEmptyMessage(LOGIN_ACCOUNT_SUCCESS);
                }
                else
                {
                    Log.e(TAG, "用户登陆失败...");
                    handler.sendEmptyMessage(LOGIN_ACCOUNT_FAILED);
                }
            }
        }).start();
    }
    protected void queryIdByAppAccount(final String appAccountId, final Handler handler) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String id = VoIPMediaAPI.getInstance().queryIDByAccount(appAccountId);

                if (id != null && id.length() > 0)
                {
                    Message msg = handler.obtainMessage();
                    msg.what = QUERY_APP_ACCOUNT_SUCCESS;
                    msg.obj = id;
                    handler.sendMessage(msg);
                }
                else
                {
                    handler.sendEmptyMessage(QUERY_APP_ACCOUNT_FAILED);
                }
            }
        }).start();
    }
    protected void showToastMessage(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
    public void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        if (message != null) {
            progressDialog.setMessage(message);
        }else{
            progressDialog.setMessage("正在处理，请稍后...");
        }
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if(progressDialog != null){
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
    protected void enterApplication() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    protected void enterLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
    private AppSimpleListener appSimpleListener = new AppSimpleListener()
    {
        @Override
        public void onSystemEvent(int eventType) {
            switch (eventType)
            {
                case SysEventType.SYS_EVENT_KICKOUT:
                {
                    finish();
                    Intent intent  = new Intent(getApplicationContext(),LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                    Message msg = new Message();
                    msg.what = SysEventType.SYS_EVENT_KICKOUT;
                    mHandler.sendMessage(msg);
                    SharedPerfUtils.clearAllPref(getApplicationContext());
                }
                break;
                case SysEventType.SYS_EVENT_DISCONNECT:
                {
                    Message msg = new Message();
                    msg.what = SysEventType.SYS_EVENT_DISCONNECT;
                    mHandler.sendMessage(msg);
                }
                break;
                case SysEventType.SYS_EVENT_RECONNECT:
                {
                    Message msg = new Message();
                    msg.what = SysEventType.SYS_EVENT_RECONNECT;
                    mHandler.sendMessage(msg);
                }
                break;
                default:
                    break;
            }
        }
    };
    private Handler mHandler = new Handler(){

        public void handleMessage(Message msg) {
            switch(msg.what)
            {
                case SysEventType.SYS_EVENT_KICKOUT:
                    showProgressDialog("此用户已在其他设备登陆,自动退出");
                    break;
                case SysEventType.SYS_EVENT_DISCONNECT:
                    showProgressDialog("网络不可用,请检查网络链接");
                    break;
                case SysEventType.SYS_EVENT_RECONNECT:
                    showProgressDialog("网络链接已重新链接");
                    break;
                default:
                    break;
            }
        };
    };

    protected boolean isNetworkConnected() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication) getApplication()).registerEventListener(appSimpleListener);
    }
}
