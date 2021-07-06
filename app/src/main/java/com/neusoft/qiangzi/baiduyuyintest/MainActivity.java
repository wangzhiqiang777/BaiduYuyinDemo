package com.neusoft.qiangzi.baiduyuyintest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.RecogResult;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.StatusRecogListener;
import com.baidu.aip.asrwakeup3.core.wakeup.MyWakeup;
import com.baidu.aip.asrwakeup3.core.wakeup.WakeUpResult;
import com.baidu.aip.asrwakeup3.core.wakeup.listener.IWakeupListener;
import com.baidu.aip.asrwakeup3.core.wakeup.listener.SimpleWakeupListener;
import com.baidu.speech.asr.SpeechConstant;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.neusoft.qiangzi.baiduyuyintest.ChatRobot.ChatRobot;
import com.neusoft.qiangzi.ttl.control.InitConfig;
import com.neusoft.qiangzi.ttl.control.MySyntherizer;
import com.neusoft.qiangzi.ttl.control.NonBlockSyntherizer;
import com.neusoft.qiangzi.ttl.listener.ISpeechListener;
import com.neusoft.qiangzi.ttl.listener.MessageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView tvRecgResult;
    TextView tvRealtimeResult;
    FloatingActionButton actionButton;
    Switch swWakeUp;

    protected MyRecognizer mRecognizer;//语音识别对象
    protected MySyntherizer mSynthesizer;//语音合成对象
    protected MyWakeup mWakeup;//语音唤醒
    private ChatRobot mChatRobot;
    private boolean isContinueRecognize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvRecgResult = findViewById(R.id.tvVoiceRecgResult);
        tvRealtimeResult = findViewById(R.id.tvRealtimeResult);
        actionButton = findViewById(R.id.floatingActionButton);
        swWakeUp = findViewById(R.id.switchWakeUp);

        initPermission();//动态权限

        mRecognizer = new MyRecognizer(this, recogListener);//初始化asr
        mSynthesizer = new NonBlockSyntherizer(this,getSpeechConfig(),null);// 初始化TTS引擎
        mWakeup = new MyWakeup(this, wakeupListener);//初始化唤醒
        mChatRobot = new ChatRobot(this);//语音聊天机器人
        mChatRobot.setOnResponseListener(robotListener);

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSynthesizer.stop();
                recogStart();
            }
        });
        swWakeUp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if(b){
                    wakeupStart();
                }else {
                    mWakeup.stop();
                }
            }
        });
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
     * 开始录音，点击“开始”按钮后调用。
     * 基于DEMO集成2.1, 2.2 设置识别参数并发送开始事件
     */
    protected void recogStart() {
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        final Map<String, Object> params = new LinkedHashMap<String, Object>();
        // 基于SDK集成2.1 设置识别参数
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // params 也可以根据文档此处手动修改，参数会以json的格式在界面和logcat日志中打印
        Log.i(TAG, "设置的start输入参数：" + params);
        // DEMO集成步骤2.2 开始识别
        mRecognizer.start(params);
    }

    /**
     * 批量播放
     */
    private void batchSpeak(String[] strlist) {
        List<Pair<String, String>> texts = new ArrayList<>();
        for (String str:strlist
             ) {
            Pair<String,String> pair = new Pair<>(str, null);
            texts.add(pair);
        }

//        texts.add(new Pair<>("开始批量播放，", "a0"));
//        texts.add(new Pair<>("123456，", "a1"));
//        texts.add(new Pair<>("欢迎使用百度语音，，，", "a2"));
//        texts.add(new Pair<>("重(chong2)量这个是多音字示例", "a3"));
        int result = mSynthesizer.batchSpeak(texts);
    }
    private void wakeupStart() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        // "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下
        mWakeup.start(params);
    }

    /**
     * 销毁时需要释放识别资源。
     */
    @Override
    protected void onDestroy() {
        mSynthesizer.release();
        mRecognizer.release();
        mWakeup.release();
        Log.i(TAG, "onDestory");
        super.onDestroy();
    }

    /**
     * 语音识别的监听器。
     * 这个很重要，需要使用这里接口实现人机交互。
     * 有三种listener可选。详细参考asr模块下的recog.listener
     */
    IRecogListener recogListener = new StatusRecogListener() {
        @Override
        public void onAsrReady() {
            tvRealtimeResult.setText("");
        }
        @Override
        public void onAsrPartialResult(String[] results, RecogResult recogResult) {
            StringBuilder sb = new StringBuilder();
            for (String word : results
            ) {
                sb.append(word);
            }
            tvRealtimeResult.setText(sb.toString());
        }
        @Override
        public void onAsrFinalResult(String[] results, RecogResult recogResult) {

            StringBuilder sb = new StringBuilder();
            tvRecgResult.append("\n");
            for (String word : results
            ) {
                sb.append(word);
                tvRecgResult.append(word);
            }
            tvRealtimeResult.setText(sb.toString());
//            synthesizer.speak("您说的是："+sb.toString()+"吗？");
//            chatRobot.speakToQingyun(sb.toString());
            mChatRobot.speakToTuring(sb.toString());
        }
        @Override
        public void onAsrFinish(RecogResult recogResult) {
            tvRealtimeResult.setText("");
        }

    };
    /**
     * 语音合成的监听器定义
     */
    ISpeechListener synthesizerListener = new MessageListener(){
        @Override
        public void onSpeechFinish(String utteranceId) {
            super.onSpeechFinish(utteranceId);
            if(isContinueRecognize){
                isContinueRecognize = false;
                recogStart();
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
            mSynthesizer.stop();
            mRecognizer.cancel();
            String response;
            if(word.equals("小乖小乖")){
                Random r = new Random();
                response = r.nextBoolean()?"在的！":"我在！";
            }else if(word.equals("小乖你好")){
                response = "您好，有什么可以帮您？";
            }else {
                response = "我在，您请讲！";
            }
            mSynthesizer.speak(response);
            tvRecgResult.append("\n"+word);
            tvRecgResult.append("\n"+response);
            isContinueRecognize = true;
        }
    };
    ChatRobot.OnResponseListener robotListener = new ChatRobot.OnResponseListener() {
        @Override
        public void OnResponse(String response) {
            if(response==null || response.isEmpty())return;
//            String[] strlist = response.split("\\{br\\}");
//            if(strlist.length > 1){
//                batchSpeak(strlist);
//                for (String str:strlist
//                     ) {
//                    tvRecgResult.append("\n"+str);
//                }
//            }else {
                tvRecgResult.append("\n"+response);
                mSynthesizer.speak(response);
                if(response.endsWith("?") || response.endsWith("？")){
                    isContinueRecognize = true;
                }
//            }
        }
    };

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
        };

        ArrayList<String> toApplyList = new ArrayList<>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }
}
