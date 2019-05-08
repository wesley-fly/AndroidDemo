package com.internal.voipdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.internal.voipmedia.VoIPMediaAPI;

public class MainActivity extends BaseActivity {
    private final int CALL = 0;
    private final int CHAT = 1;
    private final int PASS_CHANGE = 2;
    private String[] items = { "呼叫", "聊天"/*, "修改用户登录密码"*/};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("正在注销，请稍后...");
                SharedPerfUtils.clearAllPref(MainActivity.this);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        VoIPMediaAPI.getInstance().logoutAccount();
                        enterLoginActivity();
                        hideProgressDialog();
                    }
                }).start();
            }
        });

        TextView accountTextView = (TextView) findViewById(R.id.tv_account);
        accountTextView.setText("账户ID：" + SharedPerfUtils.getAccountId(this));

        ListView listView = (ListView) findViewById(R.id.lv);
        listView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, items));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                switch (position) {
                    case CALL:
                        intent.setClass(MainActivity.this, CallActivity.class);
                        startActivity(intent);
                        break;
                    case CHAT:
                        startActivity(new Intent(view.getContext(),ChatActivity.class));
                        break;
                    case PASS_CHANGE:
                        startActivity(new Intent(view.getContext(),ChangePassActivity.class));
                        break;

                    default:
                        break;
                }

            }
        });
    }
}
