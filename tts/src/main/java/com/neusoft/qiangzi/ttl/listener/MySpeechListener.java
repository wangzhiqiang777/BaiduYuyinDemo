package com.neusoft.qiangzi.ttl.listener;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizerListener;

public class MySpeechListener implements SpeechSynthesizerListener {
    ISpeechListener myListener;

    public MySpeechListener(ISpeechListener myListener) {
        this.myListener = myListener;
    }

    @Override
    public void onSynthesizeStart(String s) {
        myListener.onSynthesizeStart(s);
    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i, int i1) {
        myListener.onSynthesizeDataArrived(s, bytes, i, i1);
    }

    @Override
    public void onSynthesizeFinish(String s) {

        myListener.onSynthesizeFinish(s);
    }

    @Override
    public void onSpeechStart(String s) {

        myListener.onSynthesizeStart(s);
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

        myListener.onSpeechProgressChanged(s,i);
    }

    @Override
    public void onSpeechFinish(String s) {

        myListener.onSpeechFinish(s);
    }

    @Override
    public void onError(String s, SpeechError speechError) {

        myListener.onError(s,speechError);
    }
}
