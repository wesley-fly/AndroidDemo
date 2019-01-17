package com.internal.voipdemo;

import android.os.Bundle;

public class StartupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                }
                catch (Exception e)
                {
                }
            }
        }).start();

        String accountId = SharedPerfUtils.getAccountId(this);
        if(accountId != null)
        {
            enterApplication();
        }
        else
        {
            enterLoginActivity();
        }
    }
}
