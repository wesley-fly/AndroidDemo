package com.internal.jni;

import android.content.Context;

public class VoIPMediaJni
{
    static
    {
        System.loadLibrary("VideoEngine");
        System.loadLibrary("MediaEngineJni");
    }

    public native boolean initialize(StatusJniListener listener, Context context, int isUseJavaAuto);

    public native int checkAccount(String phoneNumber);

    public native int registerAccount(String phoneNumber, String countryCode, String userPW, String appRandomPW);

    public native int changeAccountPassWD(String userOldPW, String userNewPW);

    public native String loginAccount(String phoneNumber, String userPW);

    public native int logoutAppAccount();

    public native String sendMessage(String dstId, String mimeType,String textContent, String filePath, String messageId);

    public native int downloadMessageAttachment(String messageID, int isThumbnail, String filePath);

    public native int reportMessageStatus(String dstId, String messageID,int status);

    public native int setParameter(String typeParam, String value);

    public native void setDeviceInfo(String os, String mf, String md, String ov, int api);

    public native String getParameter(String key);

    public native String makeCall(String dstID,int media);

    public native int answerCall(String callNo);

    public native int hangupCall(String callID);

    public native int holdCall(String callID,int hold);

    public native int muteCall(String callNo, int mute);

    public native void checkNewMessage();//For Third party push

    public native void setNickName(String nickname);

    public native byte[] getUserProfiles(String jsonIds);

    public native int getCallQualityLevel(String callId);

    public native boolean initializeCamera(Context context);
}
