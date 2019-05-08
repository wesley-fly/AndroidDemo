package com.internal.voipdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText editTextHost = (EditText)findViewById(R.id.et_server);
        final EditText editTextAppAccount = (EditText)findViewById(R.id.et_appaccount);
//        final EditText editTextPassword = (EditText)findViewById(R.id.et_password);
//        final EditText editTextMail = (EditText)findViewById(R.id.et_mail);

        final Handler handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg){
                hideProgressDialog();
                if (msg.what == LOGIN_ACCOUNT_SUCCESS)
                {
                    enterApplication();
                }
                else if(msg.what == LOGIN_ACCOUNT_LEN_ERROR)
                {
                    showToastMessage("APP ACCOUNT Must < 40 Length");
                }
                else
                {
                    showToastMessage("登录失败（该账号对应的身份ID为空）");
                }
            }
        };

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hostServer = editTextHost.getText().toString().trim();
                String appAccount = editTextAppAccount.getText().toString().trim();
//                String passWord = editTextPassword.getText().toString().trim();
//                String email = editTextMail.getText().toString().trim();

                if (!TextUtils.isEmpty(appAccount)) {
                    showProgressDialog("登录中...");
                    AppLoginAppAccount(hostServer, appAccount, handler);
                }

            }
        });
    }
}
