package com.neusoft.qiangzi.baiduyuyintest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
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

    VoiceServiceManager serviceManager;
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

        serviceManager = new VoiceServiceManager(this);
        serviceManager.setVoiceListener(voiceListener);
        serviceManager.setOnBindedListener(new VoiceServiceManager.OnBindedListener() {
            @Override
            public void onBinded() {
                swWakeUp.setChecked(serviceManager.wakeupIsStart());
            }
        });
        serviceManager.start();

        mChatRobot = new ChatRobot(this);//语音聊天机器人
        mChatRobot.setOnResponseListener(robotListener);

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceManager.asrStart();
            }
        });
        swWakeUp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    serviceManager.wakeupStart();
                }else {
                    serviceManager.wakeupStop();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        serviceManager.bind();
    }

    @Override
    protected void onStop() {
        super.onStop();
        serviceManager.unbind();
    }

    VoiceListener voiceListener = new VoiceListener.Stub() {
        @Override
        public void onEvent(int e, String value) throws RemoteException {
            switch (e) {
                case EVENT_ASR_READY:
                    break;
                case EVENT_ASR_PARTIAL_RESULT:
                    tvRealtimeResult.setText(value);
                    break;
                case EVENT_ASR_FINAL_RESULT:
                    tvRecgResult.append("\n");
                    tvRecgResult.append(value);
                    tvRealtimeResult.setText(value);
                    mChatRobot.speakToTuring(value);
                    break;
                case EVENT_ASR_FINISH:
                    tvRealtimeResult.setText("");
                    break;
                case EVENT_TTS_FINISH:
                    break;
                case EVENT_WAKEUP_SUCCESS:
                    tvRecgResult.append("\n"+value);
                    break;
                default:
                    break;
            }
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
                serviceManager.ttsSpeak(response);
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
