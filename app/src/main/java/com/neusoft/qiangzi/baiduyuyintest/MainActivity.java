package com.neusoft.qiangzi.baiduyuyintest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.RecogResult;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.StatusRecogListener;
import com.baidu.speech.asr.SpeechConstant;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.neusoft.qiangzi.ttl.control.InitConfig;
import com.neusoft.qiangzi.ttl.control.MySyntherizer;
import com.neusoft.qiangzi.ttl.control.NonBlockSyntherizer;
import com.neusoft.qiangzi.ttl.listener.MessageListener;
import com.neusoft.qiangzi.ttl.util.IOfflineResourceConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    TextView tvRecgResult;
    TextView tvRealtimeResult;
    FloatingActionButton actionButton;

    protected MyRecognizer myRecognizer;//语音识别对象
    protected MySyntherizer synthesizer;//语音合成对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvRecgResult = findViewById(R.id.tvVoiceRecgResult);
        tvRealtimeResult = findViewById(R.id.tvRealtimeResult);
        actionButton = findViewById(R.id.floatingActionButton);

        initPermission();//动态权限

        //初始化asr
//        IRecogListener listener = new MessageStatusRecogListener(handler);
        myRecognizer = new MyRecognizer(this, listener);
        initialTts(); // 初始化TTS引擎

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });
    }

    protected void initialTts() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        Map<String, String> params = getParams();
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new MessageListener();
        // 添加你自己的参数
        //appId appKey secretKey在下面构造函数中，从Manifest中获取。
        InitConfig initConfig = new InitConfig(this, IOfflineResourceConst.DEFAULT_SDK_TTS_MODE, params, listener);
        synthesizer = new NonBlockSyntherizer(this, initConfig, null); // 此处可以改为MySyntherizer 了解调用过程
    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     * @return 合成参数Map
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>, 其它发音人见文档
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "15");
        // 设置合成的语速，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "7");
        // 设置合成的语调，0-15 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        return params;
    }

    /**
     * 开始录音，点击“开始”按钮后调用。
     * 基于DEMO集成2.1, 2.2 设置识别参数并发送开始事件
     */
    protected void start() {
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        final Map<String, Object> params = new LinkedHashMap<String, Object>();
        // 基于SDK集成2.1 设置识别参数
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        // params 也可以根据文档此处手动修改，参数会以json的格式在界面和logcat日志中打印
        Log.i(TAG, "设置的start输入参数：" + params);
        // DEMO集成步骤2.2 开始识别
        myRecognizer.start(params);
    }

    /**
     * 开始录音后，手动点击“停止”按钮。
     * SDK会识别不会再识别停止后的录音。
     * 基于DEMO集成4.1 发送停止事件 停止录音
     */
    protected void stop() {
        myRecognizer.stop();
    }

    /**
     * 开始录音后，手动点击“取消”按钮。
     * SDK会取消本次识别，回到原始状态。
     * 基于DEMO集成4.2 发送取消事件 取消本次识别
     */
    protected void cancel() {
        myRecognizer.cancel();
    }


    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    private void speak(String text) {
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // params.put(SpeechSynthesizer.PARAM_SPEAKER, "3"); // 设置为度逍遥
        // synthesizer.setParams(params);
        int result = synthesizer.speak(text);
    }


    /**
     * 合成但是不播放，
     * 音频流保存为文件的方法可以参见SaveFileActivity及FileSaveListener
     */
    private void synthesize(String text) {
        int result = synthesizer.synthesize(text);
    }

    /**
     * 批量播放
     */
    private void batchSpeak() {
        List<Pair<String, String>> texts = new ArrayList<>();
        texts.add(new Pair<>("开始批量播放，", "a0"));
        texts.add(new Pair<>("123456，", "a1"));
        texts.add(new Pair<>("欢迎使用百度语音，，，", "a2"));
        texts.add(new Pair<>("重(chong2)量这个是多音字示例", "a3"));
        int result = synthesizer.batchSpeak(texts);
    }

    /**
     * 销毁时需要释放识别资源。
     */
    @Override
    protected void onDestroy() {
        synthesizer.release();
        myRecognizer.release();
        Log.i(TAG, "onDestory");
        super.onDestroy();
    }

    /**
     * 语音识别的监听器。这个很重要，需要使用这里接口实现人机交互。
     * 有三种listener可选。详细参考asr模块下的recog.listener
     */
    IRecogListener listener = new StatusRecogListener() {
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
            synthesizer.speak("您说的是："+sb.toString()+"吗？");
        }
        @Override
        public void onAsrFinish(RecogResult recogResult) {
            tvRealtimeResult.setText("");
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
