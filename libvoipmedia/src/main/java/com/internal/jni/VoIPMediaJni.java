package com.internal.jni;

import android.content.Context;

public class VoIPMediaJni
{
    static
    {
        System.loadLibrary("VideoEngine");
        System.loadLibrary("MediaEngineJni");
    }

    public native boolean initialize(StatusListener listener, Context context, int isUseJavaAuto);

    public native boolean initializeCamera(Context context);
}
