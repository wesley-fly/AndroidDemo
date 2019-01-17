package com.internal.voipdemo;

import com.internal.voipmedia.MessageBase;
import com.internal.voipmedia.EventListener;

public class AppSimpleListener implements EventListener
{
    @Override
    public void onSystemEvent(int eventType) {}
    @Override
    public void onSendMessageEvent(String messageId, int sendResult, long timestamp) {}
    @Override
    public void onReceiveMessageEvent(int type, MessageBase message){}
    @Override
    public void onDownloadMessageAttachmentEvent(String messageId, int downloadResult){}
    @Override
    public void onUploadMessageAttachmentProgressEvent(String messageId, int uploadProgress){}
    @Override
    public void onDownloadMessageAttachmentProgressEvent(String messageId, int downloadProgress){}
    @Override
    public void onReceiveCallEvent(String callId, long timestamp,String callerId, String callerName, int media, int callType){}
    @Override
    public void onCallStateEvent(String callId, long timestamp, int state, int reason){}
}
