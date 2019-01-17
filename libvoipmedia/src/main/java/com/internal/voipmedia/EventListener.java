package com.internal.voipmedia;

public interface EventListener
{
    void onSystemEvent(int eventType);

    void onSendMessageEvent(String messageId, int sendResult, long timestamp);

    void onReceiveMessageEvent(int type, MessageBase message);

    void onDownloadMessageAttachmentEvent(String messageId, int downloadResult);

    void onUploadMessageAttachmentProgressEvent(String messageId, int uploadProgress);

    void onDownloadMessageAttachmentProgressEvent(String messageId, int downloadProgress);

    void onReceiveCallEvent(String callId, long timestamp,String callerId, String callerName, int media, int callType);

    void onCallStateEvent(String callId, long timestamp, int state, int reason);
}