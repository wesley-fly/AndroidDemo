package com.internal.voipdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.internal.voipmedia.MessageBase;
import com.internal.voipmedia.MessageOneToOne;
import com.internal.voipmedia.MessageStatus;
import com.internal.voipmedia.MimeType;
import com.internal.voipmedia.VoIPMediaAPI;

public class TextChatActor extends ChatActor
{
    public static final String TAG = TextChatActor.class.getSimpleName();

    static final int TEXT_BASE = CHAT_BASE << 0;
    static final int TEXT_SEND_MSG = TEXT_BASE + 1;
    static final int TEXT_RECV_MSG = TEXT_BASE + 2;

    public int sendTextMessage(String destId, String textMsg) {

        String textMsgId = VoIPMediaAPI.getInstance().sendMessage(destId, mMimeTypeStr, textMsg, null, null);

        if (textMsgId == null || textMsgId.length() == 0) {
            return -1;
        }
        mSendTextMsg = textMsg;
        mSendMsgMap.put(textMsgId, textMsg);
        return 0;
    }
    public TextChatActor(Handler handler) {
        mHandler = handler;
        mMimeTypeStr = MimeType.TEXT_PLAIN.toString();
        mTextChatEventListener = new TextChatEventListener();
    }
    class TextChatEventListener extends AppSimpleListener
    {
        @Override
        public void onSendMessageEvent(String messageId, int sendResult,long timestamp) {
            if (mSendMsgMap.containsKey(messageId)) {
                Message msg = new Message();
                msg.what = TEXT_SEND_MSG;
                msg.obj = mSendMsgMap.get(messageId);
                Bundle bundle = new Bundle();
                bundle.putString("msgId", messageId);
                bundle.putInt("resultCode", sendResult);
                bundle.putLong("timestamp", timestamp);
                msg.setData(bundle);
                mHandler.sendMessage(msg);

                mSendMsgMap.remove(messageId);
            }
        }

        @Override
        public void onReceiveMessageEvent(int type, MessageBase message) {
            Log.e(TAG, "TextChatEventListener->onReceiveMessageEvent Start");
            super.onReceiveMessageEvent(type, message);
            if (message.getClass() == MessageOneToOne.class)
            {
                MessageOneToOne o2oMsg = (MessageOneToOne) message;
                if (!o2oMsg.getMimeType().isFile())
                {
                    String dstSender = o2oMsg.getSenderId();
                    String msgId = o2oMsg.getMessageId();
                    VoIPMediaAPI.getInstance().reportMessageStatus(dstSender, msgId, MessageStatus.MSG_STATUS_RECEIVED); // 已送达
                    VoIPMediaAPI.getInstance().reportMessageStatus(dstSender, msgId, MessageStatus.MSG_STATUS_READED); // 已读

                    StringBuilder sb = new StringBuilder(o2oMsg.getSenderId());
                    sb = sb.append(':');
                    mRecvTextMsg = sb.append(o2oMsg.getTextContent()).toString();
                    Message msg = new Message();
                    msg.what = TEXT_RECV_MSG;
                    msg.obj = mRecvTextMsg;
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    public TextChatEventListener mTextChatEventListener;
}
