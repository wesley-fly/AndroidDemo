package com.internal.voipdemo;

public class MsgEntity
{
    private String msgId;

    private String msgFromId;

    private String msgDstId;

    private int msgStatus = -1;

    public void setMsgId(String msgId)
    {
        this.msgId = msgId;
    }

    public void setMsgFromId(String msgFromId)
    {
        this.msgFromId = msgFromId;
    }

    public void setMsgStatus(int msgStatus)
    {
        this.msgStatus = msgStatus;
    }

    public String getMsgId()
    {
        return msgId;
    }

    public String getMsgFromId()
    {
        return msgFromId;
    }

    public int getMsgStatus()
    {
        return msgStatus;
    }
}
