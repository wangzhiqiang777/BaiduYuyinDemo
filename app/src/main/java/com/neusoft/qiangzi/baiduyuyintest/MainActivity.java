package com.neusoft.qiangzi.baiduyuyintest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.baidu.aip.asrwakeup3.core.mini.ActivityMiniRecog;
import com.baidu.aip.asrwakeup3.core.mini.AutoCheck;
import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.RecogResult;
import com.baidu.aip.asrwakeup3.core.recog.listener.IRecogListener;
import com.baidu.aip.asrwakeup3.core.recog.listener.MessageStatusRecogListener;
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
import com.neusoft.qiangzi.ttl.listener.UiMessageListener;
import com.neusoft.qiangzi.ttl.util.Auth;
import com.neusoft.qiangzi.ttl.util.IOfflineResourceConst;
import com.neusoft.qiangzi.ttl.util.OfflineResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    TextView tvRecgResult;
    TextView tvRealtimeResult;
    FloatingActionButton actionButton;
    protected MyRecognizer myRecognizer;
    protected Handler handler;
    // 主控制类，所有合成控制方法从这个类开始
    protected MySyntherizer synthesizer;
    protected String appId;
    protected String appKey;
    protected String secretKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Auth.getInstance(this);
        } catch (Auth.AuthCheckException e) {
            Log.e(TAG, "onCreate: AuthCheckException");
            return;
        }
        appId = Auth.getInstance(this).getAppId();
        appKey = Auth.getInstance(this).getAppKey();
        secretKey = Auth.getInstance(this).getSecretKey();

        tvRecgResult = findViewById(R.id.tvVoiceRecgResult);
        tvRealtimeResult = findViewById(R.id.tvRealtimeResult);
        actionButton = findViewById(R.id.floatingActionButton);

        initPermission();

        //初始化asr
//        IRecogListener listener = new MessageStatusRecogListener(handler);
        myRecognizer = new MyRecognizer(this, listener);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });

        initialTts(); // 初始化TTS引擎
    }

    protected void initialTts() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new MessageListener();
        InitConfig config = getInitConfig(listener);
        synthesizer = new NonBlockSyntherizer(this, config, null); // 此处可以改为MySyntherizer 了解调用过程
    }

    protected InitConfig getInitConfig(SpeechSynthesizerListener listener) {
        Map<String, String> params = getParams();
        // 添加你自己的参数
        InitConfig initConfig;
        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        initConfig = new InitConfig(appId, appKey, secretKey, IOfflineResourceConst.DEFAULT_SDK_TTS_MODE, params, listener);
        return initConfig;
    }
    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
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
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
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
        // 复制此段可以自动检测常规错误
        (new AutoCheck(getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
//                        tvRecgResult.append(message + "\n");
                        // 可以用下面一行替代，在logcat中查看代码
                        Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, false)).checkAsr(params);

        // 这里打印出params， 填写至您自己的app中，直接调用下面这行代码即可。
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
        // 如果之前调用过myRecognizer.loadOfflineEngine()， release()里会自动调用释放离线资源
        // 基于DEMO5.1 卸载离线资源(离线时使用) release()方法中封装了卸载离线资源的过程
        // 基于DEMO的5.2 退出事件管理器
        myRecognizer.release();

        Log.i(TAG, "onDestory");

        // BluetoothUtil.destory(this); // 蓝牙关闭

        super.onDestroy();
    }

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
                /* 下面是蓝牙用的，可以不申请
                Manifest.permission.BROADCAST_STICKY,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
                */
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
