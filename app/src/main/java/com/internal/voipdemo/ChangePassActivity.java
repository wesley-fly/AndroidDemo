package com.internal.voipdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.internal.voipmedia.VoIPMediaAPI;

public class ChangePassActivity extends BaseActivity {
    private static final String TAG = ChangePassActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        final EditText etOldPass = (EditText)findViewById(R.id.et_old_password);
        final EditText etNewPass1 = (EditText)findViewById(R.id.et_new_password1);
        final EditText etNewPass2 = (EditText)findViewById(R.id.et_new_password2);

        findViewById(R.id.btn_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPass = etOldPass.getText().toString().trim();
                String NewPass1 = etNewPass1.getText().toString().trim();
                String NewPass2 = etNewPass2.getText().toString().trim();
                int passLen = oldPass.length();
                if(passLen < 6 || passLen > 20)
                {
                    showToastMessage("旧密码长度为6-20位，请重新输入！");
                    return;
                }
                passLen = NewPass1.length();
                if(passLen < 6 || passLen > 20)
                {
                    showToastMessage("新密码1长度为6-20位，请重新输入！");
                    return;
                }
                passLen = NewPass2.length();
                if(passLen < 6 || passLen > 20)
                {
                    showToastMessage("新密码2长度为6-20位，请重新输入！");
                    return;
                }

                if(!NewPass1.equals(NewPass2))
                {
                    showToastMessage("两次输入新密码不一致,请重新输入!");
                    return;
                }
                Log.e(TAG, "旧密码:" +oldPass+ ",新密码:" + NewPass1);
//                int result = VoIPMediaAPI.getInstance().changeAccountPasswordByMail(oldPass, NewPass1);
//                if(result == 0)
//                {
//                    showToastMessage("修改密码成功,请使用新密码重新登陆");
//                    VoIPMediaAPI.getInstance().logoutAccount();
//                    enterLoginActivity();
//                }
//                else
//                {
//                    showToastMessage("修改密码失败!");
//                }
            }
        });
    }
}
