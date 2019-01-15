package com.internal.jni;

public interface StatusListener
{
    public void onSystemEvent(int type);

    public void onSendMessageEvent(String jsonString);

    public void onReceiveMessageEvent(String jsonString);

    public void onDownloadMessageAttachmentEvent(String jsonString);

    public void onUploadMessageAttachmentProgressEvent(String jsonString);

    public void onDownloadMessageAttachmentProgressEvent(String jsonString);

    public void onReceiveCallEvent(String jsonString);

    public void onCallStateEvent(String jsonString);
}
