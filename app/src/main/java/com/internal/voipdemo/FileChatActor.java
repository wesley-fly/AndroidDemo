package com.internal.voipdemo;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.internal.voipmedia.MessageBase;
import com.internal.voipmedia.MessageOneToOne;
import com.internal.voipmedia.MessageStatus;
import com.internal.voipmedia.VoIPMediaAPI;

import java.util.LinkedHashMap;
import java.util.Map;

public class FileChatActor extends ChatActor
{
    public static final String TAG = FileChatActor.class.getSimpleName();
    public final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/voipmedia";

    static final int FILE_BASE = CHAT_BASE << 3;
    static final int FILE_SEND_MSG = FILE_BASE + 1;
    static final int FILE_RECV_MSG = FILE_BASE + 2;
    static final int FILE_UPLOAD_MSG = FILE_BASE + 3;
    static final int FILE_DOWNLOAD_MSG = FILE_BASE + 4;

    public static Map<String,String> addTextMap = null;
    public int sendFileMessage(String destId, String fileSendPath, String addText) {

        String extName = FileUtils.getExtName(fileSendPath, '.');
        mMimeTypeStr = FileUtils.getMimeType(extName).toString();

        String fileMsgId = VoIPMediaAPI.getInstance().sendMessage(destId, mMimeTypeStr,addText, fileSendPath, null);
        Log.e(TAG, "onSendMessageEvent return msgid:" + fileMsgId);
        if (fileMsgId == null)
        {
            return -1;
        }
        mSendMsgMap.put(fileMsgId, fileSendPath);
        addTextMap.put(fileMsgId, addText);

        return 0;
    }
    public FileChatActor(Handler handler) {
        mHandler = handler;
        if(addTextMap == null){
            addTextMap = new LinkedHashMap<String, String>();
        }
        mFileChatEventListener = new FileChatEventListener();
    }
    FileChatEventListener mFileChatEventListener;

    class FileChatEventListener extends AppSimpleListener
    {
        @Override
        public void onReceiveMessageEvent(int type, MessageBase message) {
            Log.e(TAG, "FileChatEventListener->onReceiveMessageEvent Start");
            super.onReceiveMessageEvent(type,message);
            if (message.getClass() == MessageOneToOne.class)
            {
                MessageOneToOne o2oMsg = (MessageOneToOne)message;
                if(o2oMsg.getMimeType().isFile())
                {
                    String dstSender = o2oMsg.getSenderId();
                    String msgId = o2oMsg.getMessageId();
                    VoIPMediaAPI.getInstance().reportMessageStatus(dstSender, msgId, MessageStatus.MSG_STATUS_RECEIVED); // 已送达
                    VoIPMediaAPI.getInstance().reportMessageStatus(dstSender, msgId, MessageStatus.MSG_STATUS_READED); // 已读
                    Message msg = new Message();
                    msg.what = FILE_RECV_MSG;
                    String fileName = SDCARD_PATH+ "/" + o2oMsg.getFilename();
                    String textContent = o2oMsg.getTextContent();
                    Bundle bundle = new Bundle();
                    bundle.putString("addContent", textContent);
                    msg.setData(bundle);
                    msg.obj = fileName;
                    mHandler.sendMessage(msg);
                    VoIPMediaAPI.getInstance().downloadMessageAttachment(o2oMsg.getMessageId(), 0, fileName);
                }
            }
        }

        @Override
        public void onUploadMessageAttachmentProgressEvent(String messageId, int uploadProgress) {
            if (mSendMsgMap.containsKey(messageId))
            {
                Message msg = new Message();
                msg.what = FILE_UPLOAD_MSG;
                msg.arg1 = uploadProgress;
                Bundle bundle = new Bundle();
                bundle.putString("msgId", messageId);

                if (uploadProgress == 100)
                {
                    msg.obj = mSendMsgMap.get(messageId);
                    bundle.putString("addContent", addTextMap.get(messageId));
                    mSendMsgMap.remove(messageId);
                    addTextMap.remove(messageId);
                }
                msg.setData(bundle);
                mHandler.sendMessage(msg);
            }
        }

        @Override
        public void onDownloadMessageAttachmentProgressEvent(String messageId, int downloadProgress)
        {
            Message msg = new Message();
            msg.what = FILE_DOWNLOAD_MSG;
            msg.arg1 = downloadProgress;
            mHandler.sendMessage(msg);
        }

        @Override
        public void onDownloadMessageAttachmentEvent(String messageId, int downloadResult)
        {
            if (downloadResult != 0)
            {
                Log.e(TAG, "下载附件文件失败,错误码:" + downloadResult);
            }
        }
    }
}
