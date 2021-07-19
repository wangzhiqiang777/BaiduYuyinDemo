// VoiceBinder.aidl
package com.neusoft.qiangzi.baiduyuyintest;
import com.neusoft.qiangzi.baiduyuyintest.VoiceListener;

// Declare any non-default types here with import statements

interface VoiceBinder {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);

    const String SERVICE_NAME = "com.neusoft.qiangzi.voiceassistant.VoiceService";

    void asrStart();
    void asrStop();
    void asrCancel();
    void ttsSpeak(String text);
    void ttsStop();
    void wakeupStart();
    void wakeupStop();
    boolean wakeupIsStart();

    void registerListener(VoiceListener listener);
    void unregisterListener(VoiceListener listener);
}
