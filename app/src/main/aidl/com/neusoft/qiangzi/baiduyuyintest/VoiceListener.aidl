// VoiceListener.aidl
package com.neusoft.qiangzi.baiduyuyintest;

// Declare any non-default types here with import statements

interface VoiceListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    const int EVENT_ASR_READY = 1;
    const int EVENT_ASR_PARTIAL_RESULT = 2;
    const int EVENT_ASR_FINAL_RESULT = 3;
    const int EVENT_ASR_FINISH = 4;
    const int EVENT_TTS_FINISH = 5;
    const int EVENT_WAKEUP_SUCCESS = 6;

    void onEvent(int e, String value);
//    //asr
//    void onAsrReady();
//    void onAsrPartialResult(String result);
//    void onAsrFinalResult(String result);
//    void onAsrFinish();
//    //tts
//    void onTtsFinish(String utteranceId);
//    //wakeup
//    void onWakeupSuccess(String word);
}
