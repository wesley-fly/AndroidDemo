package com.internal.voipmedia;

import android.content.Context;

import com.internal.voipmediaimpl.VoIPMediaAPIImpl;

public abstract class VoIPMediaAPI
{
    public static VoIPMediaAPI getInstance()
    {
        return VoIPMediaAPIImpl.getInstance();
    }

    public abstract boolean initialization(Context context,EventListener listener);

    public abstract int setSystemParams(String name,String value);

    public abstract String getSystemParams(String name);

    public abstract String bindAccount(String appUserAccount);

    public abstract String queryIDByAccount(String appUserAccount);

//    public abstract int checkAccount(String phoneNumber);
//
//    public abstract int checkAccountByMail(String Email);
//
//    public abstract int registerAccount(String phoneNumber,String countryCode,String passWord);
//
//    public abstract int registerAccountByMail(String Email, String passWord);
//
//    public abstract String loginAccount(String phoneNumber,String passWord);
//
//    public abstract String loginAccountByMail(String Email,String passWord);
//
//    public abstract int changeAccountPassword(String oldPassWord,String newPassWord);
//
//    public abstract int changeAccountPasswordByMail(String oldPassWord,String newPassWord);

    public abstract int logoutAccount();

    public abstract String sendMessage(String dstId,String mimeType,String textContent,String filePath,String msgId);

    public abstract void checkNewMessage();

    public abstract int downloadMessageAttachment(String msgId,int isThumbnail,String filePath);

    public abstract int reportMessageStatus(String dstId,String msgId,int status);

    public abstract String makeCall(String dstId,int mediaType);

    public abstract int hangupCall(String callId);

    public abstract int answerCall(String callId);

    public abstract int holdCall(String callId,int holdStatus);

    public abstract int muteCall(String callId, int muteStatus);

    public abstract int setAudioOutput(int device);
}