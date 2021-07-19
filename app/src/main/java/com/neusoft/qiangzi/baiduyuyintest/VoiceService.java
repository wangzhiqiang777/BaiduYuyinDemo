package com.neusoft.qiangzi.baiduyuyintest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.RecogResult;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.StatusRecogListener;
import com.baidu.aip.asrwakeup3.core.wakeup.MyWakeup;
import com.baidu.aip.asrwakeup3.core.wakeup.WakeUpResult;
import com.baidu.aip.asrwakeup3.core.wakeup.listener.IWakeupListener;
import com.baidu.aip.asrwakeup3.core.wakeup.listener.SimpleWakeupListener;
import com.baidu.speech.asr.SpeechConstant;
import com.neusoft.qiangzi.ttl.control.InitConfig;
import com.neusoft.qiangzi.ttl.control.MySyntherizer;
import com.neusoft.qiangzi.ttl.control.NonBlockSyntherizer;
import com.neusoft.qiangzi.ttl.listener.ISpeechListener;
import com.neusoft.qiangzi.ttl.listener.MessageListener;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import androidx.core.app.NotificationCompat;

public class VoiceService extends Service {
    private static final String TAG = "VoiceService";
    protected MyRecognizer mAsr;//语音识别对象
    protected MySyntherizer mTts;//语音合成对象
    protected MyWakeup mWakeup;//语音唤醒
//    private VoiceListener voiceListener;
    private RemoteCallbackList<VoiceListener> mListenerList = new RemoteCallbackList<>();
    private boolean isContinueRecognize = false;

    public VoiceService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAsr = new MyRecognizer(this, recogListener);//初始化asr
        mTts = new NonBlockSyntherizer(this,getSpeechConfig(),null);// 初始化TTS引擎
        mWakeup = new MyWakeup(this, wakeupListener);//初始化唤醒

        //回复保存的参数
        SharedPreferences shp = getSharedPreferences("voice_config", MODE_PRIVATE);
        wakeupIsStarted = shp.getBoolean("wakeup_is_enabled", false);

        //根据参数做设置
        if(wakeupIsStarted) wakeupStart();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: is called");
        NotificationChannel notificationChannel = null;
        String CHANNEL_ID = getClass().getPackage().toString();
        String CHANNEL_NAME = "Voice Servier";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Notification notification = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("小乖语音")
                .setContentText("小乖语音助手运行中。。。")
                .setWhen(System.currentTimeMillis())
                .build();
        startForeground(1,notification);

        return START_STICKY;
    }

    boolean wakeupIsStarted = false;
    @Override
    public IBinder onBind(Intent intent) {
        return new VoiceBinder.Stub() {

            @Override
            public void asrStart() throws RemoteException {
                VoiceService.this.asrStart();
            }

            @Override
            public void asrStop() throws RemoteException {
                mAsr.stop();
            }

            @Override
            public void asrCancel() throws RemoteException {
                mAsr.cancel();
            }

            @Override
            public void ttsSpeak(String text) throws RemoteException {
                mTts.speak(text);
            }

            @Override
            public void ttsStop() throws RemoteException {
                mTts.stop();
            }

            @Override
            public void wakeupStart() throws RemoteException {
                VoiceService.this.wakeupStart();
                wakeupIsStarted = true;
                //保存语音唤醒
                SharedPreferences shp = getSharedPreferences("voice_config", MODE_PRIVATE);
                SharedPreferences.Editor editor = shp.edit();
                editor.putBoolean("wakeup_is_enabled", wakeupIsStarted);
                editor.commit();
            }

            @Override
            public void wakeupStop() throws RemoteException {
                mWakeup.stop();
                wakeupIsStarted = false;
            }

            @Override
            public boolean wakeupIsStart() throws RemoteException {
                return wakeupIsStarted;
            }

            @Override
            public void registerListener(VoiceListener listener) throws RemoteException {
                mListenerList.register(listener);
                Log.d(TAG, "registerListener: current size:" + mListenerList.getRegisteredCallbackCount());
            }

            @Override
            public void unregisterListener(VoiceListener listener) throws RemoteException {
                mListenerList.unregister(listener);
                Log.d(TAG, "unregisterListener: current size:" + mListenerList.getRegisteredCallbackCount());
            }
        };
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: is called.");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAsr.release();
        mTts.release();
        mWakeup.release();
        Log.i(TAG, "onDestory is called.");
    }

//    private static final int EVENT_ASR_READY = 1;
//    private static final int EVENT_ASR_PARTIAL_RESULT = 2;
//    private static final int EVENT_ASR_FINAL_RESULT = 3;
//    private static final int EVENT_ASR_FINISH = 4;
//    private static final int EVENT_TTS_FINISH = 5;
//    private static final int EVENT_WAKEUP_SUCCESS = 6;
    private void broadcastEvent(int event, String value) {
        synchronized (mListenerList) {
            int n = mListenerList.beginBroadcast();
//            Log.d(TAG, "broadcastEvent: begin n="+n);
            try {
                for (int i = 0; i < n; i++) {
                    VoiceListener listener = mListenerList.getBroadcastItem(i);
                    if (listener != null) {
                        listener.onEvent(event,value);
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "broadcastEvent: error!");
                e.printStackTrace();
            }
            mListenerList.finishBroadcast();
//            Log.d(TAG, "broadcastEvent: end");
        }
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     */
    protected InitConfig getSpeechConfig() {
        InitConfig initConfig = new InitConfig(this, synthesizerListener);
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>, 其它发音人见文档
        initConfig.setParamSpeaker(InitConfig.PARAM_SPEAKER_EMOTION_CHILD);
        // 设置合成的音量，0-15 ，默认 5
        initConfig.setParamVolume(15);
        // 设置合成的语速，0-15 ，默认 5
        initConfig.setParamSpeed(5);
        // 设置合成的语调，0-15 ，默认 5
        initConfig.setParamPitch(5);

        return initConfig;
    }
    /**
     * 语音识别的监听器。
     * 这个很重要，需要使用这里接口实现人机交互。
     * 有三种listener可选。详细参考asr模块下的recog.listener
     */
    IRecogListener recogListener = new StatusRecogListener() {
        @Override
        public void onAsrReady() {
            broadcastEvent(VoiceListener.EVENT_ASR_READY,null);
        }
        @Override
        public void onAsrPartialResult(String[] results, RecogResult recogResult) {
            StringBuilder sb = new StringBuilder();
            for (String word : results
            ) {
                sb.append(word);
            }
            broadcastEvent(VoiceListener.EVENT_ASR_PARTIAL_RESULT,sb.toString());
        }
        @Override
        public void onAsrFinalResult(String[] results, RecogResult recogResult) {

            StringBuilder sb = new StringBuilder();
            for (String word : results
            ) {
                sb.append(word);
            }
            //可以在这里进行指令判断，并转换成广播
//            if (sb.toString().contains("导航到")) {
//                int index = sb.toString().indexOf("导航到") + 3;
//                String des = sb.toString().substring(index);
//                Intent intent = new Intent("navigation");
//                intent.putExtra("destination", des);
//                sendBroadcast(intent);
//                startActivity(intent);
//            }
            //
            broadcastEvent(VoiceListener.EVENT_ASR_FINAL_RESULT,sb.toString());
        }
        @Override
        public void onAsrFinish(RecogResult recogResult) {
            broadcastEvent(VoiceListener.EVENT_ASR_FINISH,null);
        }

    };
    /**
     * 语音合成的监听器定义
     */
    ISpeechListener synthesizerListener = new MessageListener(){
        @Override
        public void onSpeechFinish(String utteranceId) {
            super.onSpeechFinish(utteranceId);
            if (isContinueRecognize) {
                isContinueRecognize = false;
                asrStart();
            } else {
                broadcastEvent(VoiceListener.EVENT_TTS_FINISH,null);
            }
        }
    };
    /**
     * 唤醒的监听器定义
     */
    IWakeupListener wakeupListener = new SimpleWakeupListener(){
        @Override
        public void onSuccess(String word, WakeUpResult result) {
            super.onSuccess(word, result);
            Log.d(TAG, "wakeupListener: onSuccess:"+word);
            mTts.stop();
            mAsr.cancel();
            String response;
            Random r = new Random();
            if(word.equals("小乖小乖")){
                response = r.nextBoolean()?"在的":"我在";
                response += r.nextBoolean()?"！":"，您请讲！";
            }else if(word.equals("小乖你好")){
                response = r.nextBoolean()?"主人您好！":"您好，有什么可以帮您？";
            }else {
                response = "我在，您请讲！";
            }
            mTts.speak(response);
            isContinueRecognize = true;
            broadcastEvent(VoiceListener.EVENT_WAKEUP_SUCCESS,word);
        }
    };

    protected void asrStart() {
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        final Map<String, Object> params = new LinkedHashMap<String, Object>();
        // 基于SDK集成2.1 设置识别参数
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // params 也可以根据文档此处手动修改，参数会以json的格式在界面和logcat日志中打印
        Log.i(TAG, "设置的start输入参数：" + params);
        // DEMO集成步骤2.2 开始识别
        mAsr.start(params);
    }
    private void wakeupStart() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        // "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下
        mWakeup.start(params);
    }
}
