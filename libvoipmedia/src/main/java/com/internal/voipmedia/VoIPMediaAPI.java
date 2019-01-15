package com.internal.voipmedia;

public abstract class VoIPMediaAPI
{
    public static VoIPMediaAPI getInstance()
    {
        return VoIPMediaImpl.getInstance();
    }

}