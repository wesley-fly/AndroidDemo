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
        final EditText editTextPhone = (EditText)findViewById(R.id.et_phone);
        final EditText editTextPassword = (EditText)findViewById(R.id.et_password);

        final Handler handler = new Handler()
        {
            @Override
            public void handleMessage(Message msg){
                hideProgressDialog();
                if (msg.what == LOGIN_ACCOUNT_SUCCESS)
                {
                    enterApplication();
                }
                else if(msg.what == LOGIN_ACCOUNT_PASS_LEN_ERROR)
                {
                    showToastMessage("密码长度为6-20位，请重新输入！");
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
                String hostAddr = editTextHost.getText().toString().trim();
                String passWord = editTextPassword.getText().toString().trim();
                String phoneNumber = !TextUtils.isEmpty(editTextPhone.getText().toString().trim()) ? editTextPhone.getText().toString().trim() : "";

                if (!TextUtils.isEmpty(passWord)) {
                    showProgressDialog("登录中...");
                    AppLoginAppAccount(hostAddr, phoneNumber, passWord,  handler);
                }

            }
        });
    }
}
