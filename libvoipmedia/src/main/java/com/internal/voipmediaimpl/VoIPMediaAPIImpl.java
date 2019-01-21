package com.internal.voipmediaimpl;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.internal.jni.VoIPMediaJni;
import com.internal.jni.StatusJniListener;
import com.internal.voipmedia.ErrorCode;
import com.internal.voipmedia.MediaType;
import com.internal.voipmedia.MessageObjectType;
import com.internal.voipmedia.MessageOfRead;
import com.internal.voipmedia.MessageOfRec;
import com.internal.voipmedia.MessageOneToOne;
import com.internal.voipmedia.MessageStatus;
import com.internal.voipmedia.MimeType;
import com.internal.voipmedia.EventListener;
import com.internal.voipmedia.VoIPMediaAPI;
import com.internal.voipmedia.util.Utils;
import com.internal.webrtc.adapter.DeviceAdaptation;
import com.internal.webrtc.adapter.LocalAudioCapabilityFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

//Docs export 17 API
public class VoIPMediaAPIImpl extends VoIPMediaAPI implements StatusJniListener
{
    private String TAG = getClass().getSimpleName();

    private static VoIPMediaAPIImpl singleInstance = null;

    private static VoIPMediaJni m_voip_jni = null;

    private static EventListener m_voipEventListener = null;

    private Context m_context;

    private VoIPMediaAPIImpl()
    {
        m_voip_jni = new VoIPMediaJni();
    }
    public static VoIPMediaAPIImpl getInstance()
    {
        if(singleInstance == null)
        {
            singleInstance = new VoIPMediaAPIImpl();
        }

        return singleInstance;
    }

    //API Impl, total 17
    @Override
    public synchronized boolean initialization(Context context, EventListener listener)
    {
        m_voipEventListener = listener;
        m_context = context;
        DeviceAdaptation device = LocalAudioCapabilityFactory.create().get();
        int useJavaAudio = device.getAudioSelect();
        boolean isInitedOk = m_voip_jni.initialize(this,context,useJavaAudio);
        if(isInitedOk)
        {
            m_voip_jni.setDeviceInfo("android", Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE, Build.VERSION.SDK_INT);
        }

        return isInitedOk;
    }
    @Override
    public int setSystemParams(String name,String value)
    {
        return m_voip_jni.setParameter(name, value);
    }
    @Override
    public String getSystemParams(String name)
    {
        String value = m_voip_jni.getParameter(name);
        if(value != null && value.length() > 0)
        {
            return value;
        }
        else
        {
            return "";
        }
    }
    @Override
    public int checkAccount(String phoneNumber)
    {
        return m_voip_jni.checkAccount(phoneNumber);
    }
    @Override
    public int registerAccount(String phoneNumber,String countryCode,String passWord)
    {
        return m_voip_jni.registerAccount(phoneNumber,countryCode,passWord,Utils.generatePassword(8));
    }
    @Override
    public String loginAccount(String phoneNumber,String passWord)
    {
        return m_voip_jni.loginAccount(phoneNumber, passWord);
    }
    @Override
    public int changeAccountPassword(String oldPassWord,String newPassWord)
    {
        return m_voip_jni.changeAccountPassWD(oldPassWord, newPassWord);
    }
    @Override
    public int logoutAccount()
    {
        return m_voip_jni.logoutAppAccount();
    }
    @Override
    public String sendMessage(String dstId,String mimeType,String textContent,String filePath,String msgId)
    {
        String retMsgId;
        MimeType mt = MimeType.buildMimeType(mimeType);
        if(mt.isFile())
        {
            File file = new File(filePath);
            if(!file.exists() || !file.isFile() || !file.canRead())
            {
                Log.e(TAG,"检查文件不可被操作!");
                return "";
            }
        }
        if(filePath == null)
        {
            filePath = "";
        }
        if(msgId == null)
        {
            msgId = "";
        }
        if(textContent == null)
        {
            textContent = "";
        }

        retMsgId = m_voip_jni.sendMessage(dstId,mimeType,textContent,filePath,msgId);

        if(retMsgId != null && retMsgId.length() > 0)
        {
            return retMsgId;
        }
        else
        {
            return "";
        }
    }
    @Override
    public void checkNewMessage()
    {
        m_voip_jni.checkNewMessage();
    }
    @Override
    public int downloadMessageAttachment(String msgId,int isThumbnail,String filePath)
    {
        File file = new File(filePath);
        String filePathStr = file.getParent();
        File fileStr = new File(filePathStr);

        if(!fileStr.exists())
        {
            fileStr.mkdirs();
        }

        return m_voip_jni.downloadMessageAttachment(msgId,isThumbnail,filePath);
    }
    @Override
    public int reportMessageStatus(String dstId,String msgId,int status)
    {
        return m_voip_jni.reportMessageStatus(dstId,msgId,status);
    }
    @Override
    public String makeCall(String dstId,int mediaType)
    {
        if(mediaType != MediaType.MEDIA_AUDIO)
        {
            Log.e(TAG,"不支持的通话类型!");
            return "";
        }
        String callId = m_voip_jni.makeCall(dstId, mediaType);
        if(callId != null && callId.length() > 0)
        {
            return callId;
        }
        else
        {
            return "";
        }
    }
    @Override
    public int hangupCall(String callId)
    {
        return m_voip_jni.hangupCall(callId);
    }
    @Override
    public int answerCall(String callId)
    {
        return m_voip_jni.answerCall(callId);
    }
    @Override
    public int holdCall(String callId,int holdStatus)
    {
        return m_voip_jni.holdCall(callId, holdStatus);
    }
    @Override
    public int muteCall(String callId, int muteStatus)
    {
        return m_voip_jni.muteCall(callId, muteStatus);
    }
    @Override
    public int setAudioOutput(int device) {
        if (0 != device && 1 != device) {
            return ErrorCode.PARAM_ERROR;
        }

        AudioManager manager = (AudioManager) m_context.getSystemService(Context.AUDIO_SERVICE);
        if (1 == device) {
            manager.setSpeakerphoneOn(true);
        } else if (0 == device) {
            manager.setSpeakerphoneOn(false);
        }

        return 0;
    }

    //Event Impl, total 8
    @Override
    public void onSystemEvent(String jsonString)
    {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);
            int type = jsonObject.optInt("eventType");
            m_voipEventListener.onSystemEvent(type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onSendMessageEvent(String jsonString)
    {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);

            String msgId = jsonObject.optString("messageID");
            int result = jsonObject.optInt("result");
            long timestamp = jsonObject.optInt("timestamp");

            m_voipEventListener.onSendMessageEvent(msgId, result,timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onReceiveMessageEvent(String jsonString)
    {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);
            int type = jsonObject.optInt("type");

            switch (type) {
                case MessageObjectType.MSG_OBJECT_TYPE_MMS:
                {
                    MessageOneToOne singleChatMsg;
                    String mimeType = jsonObject.optString("mimeType");
                    String srcId = jsonObject.optString("srcId");
//                    String dstId = jsonObject.optString("dstId");
                    String textContent = jsonObject.optString("textContent");
                    String messageID = jsonObject.optString("messageID");
                    String filePath = jsonObject.optString("filePath");
                    String mediaInfo = jsonObject.optString("mediaInfo");
                    int createTime = jsonObject.optInt("createTime");
                    int sessionType = jsonObject.optInt("sessionType");

                    if (sessionType == 0) {
                        singleChatMsg = new MessageOneToOne(MimeType.buildMimeType(mimeType), srcId, messageID, textContent, createTime, filePath, mediaInfo);
                        m_voipEventListener.onReceiveMessageEvent(MessageObjectType.MSG_OBJECT_TYPE_MMS, singleChatMsg);
                    }
                }
                break;
                case MessageObjectType.MSG_OBJECT_TYPE_STATUS:
                {
                    int messageStatus = jsonObject.optInt("messageStatus");
                    String messageID = jsonObject.optString("messageID");
                    int createTime = jsonObject.optInt("createTime");

                    if (MessageStatus.MSG_STATUS_RECEIVED == messageStatus) {
                        MessageOfRec msgStateRec = new MessageOfRec((long) createTime);
                        msgStateRec.setMessageId(messageID);
//                        Log.e(TAG, "Impl->STATUS->singleChatMsg->onReceiveMessageEvent Peer Received");
                        m_voipEventListener.onReceiveMessageEvent(MessageObjectType.MSG_OBJECT_TYPE_STATUS, msgStateRec);
                    } else if (MessageStatus.MSG_STATUS_READED == messageStatus) {
                        MessageOfRead msgStateRead = new MessageOfRead((long) createTime);
                        msgStateRead.setMessageId(messageID);
//                        Log.e(TAG, "Impl->STATUS->singleChatMsg->onReceiveMessageEvent Peer Readed");
                        m_voipEventListener.onReceiveMessageEvent(MessageObjectType.MSG_OBJECT_TYPE_STATUS, msgStateRead);
                    }
                }
                break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDownloadMessageAttachmentEvent(String jsonString)
    {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);

            String msgId = jsonObject.optString("messageID");
            int result = jsonObject.optInt("result");
            m_voipEventListener.onDownloadMessageAttachmentEvent(msgId, result);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public void onUploadMessageAttachmentProgressEvent(String jsonString)
    {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);

            String msgId = jsonObject.optString("messageID");
            int process = jsonObject.optInt("progress");
            m_voipEventListener.onUploadMessageAttachmentProgressEvent(msgId, process);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public void onDownloadMessageAttachmentProgressEvent(String jsonString)
    {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);

            String msgId = jsonObject.optString("messageID");
            int process = jsonObject.optInt("progress");

            m_voipEventListener.onDownloadMessageAttachmentProgressEvent(msgId, process);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public void onReceiveCallEvent(String jsonString)
    {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);

            String callId = jsonObject.optString("callId");
            String callerId = jsonObject.optString("callerId");
            String callerName = jsonObject.optString("callerName");
            int media = jsonObject.optInt("media");
            int callType = jsonObject.optInt("callType");
            long timestamp = jsonObject.optInt("timestamp");

            m_voipEventListener.onReceiveCallEvent(callId, timestamp,callerId, callerName, media, callType);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public void onCallStateEvent(String jsonString)
    {
        Log.e(TAG, "Impl->onCallStateEvent, " + jsonString);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);

            String callId = jsonObject.optString("callID");
            long timestamp = jsonObject.optInt("timestamp");
            int state = jsonObject.optInt("state");
            int reason = jsonObject.optInt("reason");

            m_voipEventListener.onCallStateEvent(callId, timestamp, state, reason);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
