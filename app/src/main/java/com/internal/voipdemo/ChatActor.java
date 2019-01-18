package com.internal.voipdemo;

import android.os.Handler;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ChatActor
{
    static final int CHAT_BASE = 1;
    public static Map<String,String> mSendMsgMap = new LinkedHashMap<String, String>();
    String mSendTextMsg;
    String mDestId;
    String mMimeTypeStr;
    String mRecvTextMsg;
    Handler mHandler;
}
