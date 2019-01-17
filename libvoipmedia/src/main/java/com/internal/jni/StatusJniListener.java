package com.internal.jni;

public interface StatusJniListener
{
    public void onSystemEvent(String jsonString);

    public void onSendMessageEvent(String jsonString);

    public void onReceiveMessageEvent(String jsonString);

    public void onDownloadMessageAttachmentEvent(String jsonString);

    public void onUploadMessageAttachmentProgressEvent(String jsonString);

    public void onDownloadMessageAttachmentProgressEvent(String jsonString);

    public void onReceiveCallEvent(String jsonString);

    public void onCallStateEvent(String jsonString);
}