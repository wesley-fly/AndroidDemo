package com.internal.voipdemo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.internal.voipmedia.MessageBase;
import com.internal.voipmedia.ParametersName;
import com.internal.voipmedia.EventListener;
import com.internal.voipmedia.VoIPMediaAPI;

import java.util.concurrent.CopyOnWriteArrayList;

public class MyApplication extends Application implements EventListener {
    private String TAG = getClass().getSimpleName();

    private CopyOnWriteArrayList<AppSimpleListener> listeners = new CopyOnWriteArrayList<AppSimpleListener>();

    private int AUD_USE_AGC=(1<<0);
    private int AUD_USE_RX_AGC=(1<<1);
    private int AUD_USE_AECM=(1<<2);
    private int AUD_USE_NS=(1<<3);
    private int AUD_CODEC_ADAPTIVE=(1<<4);
    private int audioProcessCap=AUD_USE_AGC|AUD_USE_RX_AGC|AUD_USE_AECM|AUD_USE_NS|AUD_CODEC_ADAPTIVE;

    public static Context context = null;

    public volatile static  boolean  isTalking = false;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        String ServerHost = SharedPerfUtils.getServerHost(this);
        String LocalDBPath = context.getFilesDir().toString();

        Log.e(TAG, "VoIP 初始化,初始化需要先设定服务器地址: " + ServerHost + ",本地数据库路径:" + LocalDBPath);
        VoIPMediaAPI.getInstance().setSystemParams(ParametersName.VOIP_CS_SERVER, ServerHost);
        VoIPMediaAPI.getInstance().setSystemParams(ParametersName.VOIP_LOCAL_DB_PATH, LocalDBPath);
        VoIPMediaAPI.getInstance().setSystemParams(ParametersName.VOIP_ENABLE_DEBUG, "1");
        boolean initOk = VoIPMediaAPI.getInstance().initialization(getApplicationContext(),this);
        if(initOk)
        {
            VoIPMediaAPI.getInstance().setSystemParams(ParametersName.VOIP_AUDIO_CAP, String.valueOf(audioProcessCap));
            tryAutoLoginAccount();
        }
        else
        {
            Log.e(TAG, "VoIP初始化失败!");
        }
    }
    private void tryAutoLoginAccount() {
        final String appUserPw = SharedPerfUtils.getPassword(this);
        final String appRootCS = SharedPerfUtils.getServerHost(this);
        final String appEmail = SharedPerfUtils.getEmail(this);

        if (appEmail != null && appUserPw != null && appRootCS != null)
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    Log.e(TAG, "自动登陆帐号: " + appEmail + ", 自动登陆密码: " + appUserPw);
                    String AccountId = VoIPMediaAPI.getInstance().loginAccountByMail(appEmail, appUserPw);
                    if (AccountId.length() == 8)
                    {
                        Log.e(TAG, "自动登陆帐号成功,返回AccountId = " + AccountId);
                    }
                    else
                    {
                        Log.e(TAG, "自动登陆帐号失败!");
                    }
                }
            }).start();
        }
    }
    public void registerEventListener(AppSimpleListener listener)
    {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    public void unRegisterEventListener(AppSimpleListener listener)
    {
        listeners.remove(listener);
    }

    @Override
    public void onSystemEvent(int eventType)
    {
        Log.e(TAG, "onSystemEvent : type = " + eventType);
        for (EventListener listener : listeners) {
            listener.onSystemEvent(eventType);
        }
    }
    @Override
    public void onSendMessageEvent(String messageId, int sendResult, long timestamp)
    {
        Log.e(TAG, "onSendMessageEvent messageId = " + messageId + ", sendResult = " + sendResult + ", timestamp = " + timestamp);
        for (EventListener listener : listeners) {
            listener.onSendMessageEvent(messageId, sendResult,timestamp);
        }
    }
    @Override
    public void onReceiveMessageEvent(int type, MessageBase message)
    {
        for (EventListener listener : listeners) {
            listener.onReceiveMessageEvent(type, message);
        }
    }
    @Override
    public void onDownloadMessageAttachmentEvent(String messageId, int downloadResult)
    {
        Log.e(TAG, "onDownloadMessageAttachmentEvent messageId = " + messageId + ", downloadResult = " + downloadResult);
        for (EventListener listener : listeners) {
            listener.onDownloadMessageAttachmentEvent(messageId, downloadResult);
        }
    }
    @Override
    public void onUploadMessageAttachmentProgressEvent(String messageId, int uploadProgress)
    {
//        Log.e(TAG, "onUploadMessageAttachmentProgressEvent messageId = " + messageId + ", uploadProgress = " + uploadProgress);
        for (EventListener listener : listeners) {
            listener.onUploadMessageAttachmentProgressEvent(messageId, uploadProgress);
        }
    }
    @Override
    public void onDownloadMessageAttachmentProgressEvent(String messageId, int downloadProgress)
    {
//        Log.e(TAG, "onDownloadMessageAttachmentProgressEvent messageId = " + messageId + ", downloadProgress = " + downloadProgress);
        for (EventListener listener : listeners) {
            listener.onDownloadMessageAttachmentProgressEvent(messageId,downloadProgress);
        }
    }
    @Override
    public void onReceiveCallEvent(String callId, long timestamp,String callerId, String callerName, int media, int callType)
    {
        Log.e(TAG, "onReceiveCallEvent callId = " + callId + ", callerId = "
                + callId + ", timestamp = " + timestamp + ", callerName = " + callerName + ", media = "
                + media + ", callType = " + callType);
        for (EventListener listener : listeners) {
            listener.onReceiveCallEvent(callId, timestamp, callerId, callerName, media, callType);
        }
    }
    @Override
    public void onCallStateEvent(String callId, long timestamp, int state, int reason)
    {
        Log.e(TAG, "onCallStateEvent callId = " + callId + ", timestamp = " +timestamp + ", state = " + state + ", reason= " + reason);
        for (EventListener listener : listeners) {
            listener.onCallStateEvent(callId, timestamp,state, reason);
        }
    }
}
