package com.internal.voipdemo;

import android.os.Bundle;
import android.view.WindowManager;

public class StartupActivity extends BaseActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_startup);

        final String accountId = SharedPerfUtils.getAccountId(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);

                    if(accountId != null)
                    {
                        enterApplication();
                    }
                    else
                    {
                        enterLoginActivity();
                    }
                }
                catch (Exception e)
                {
                }
            }
        }).start();
    }
}
