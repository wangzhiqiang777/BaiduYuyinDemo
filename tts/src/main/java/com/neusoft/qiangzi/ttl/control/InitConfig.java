package com.neusoft.qiangzi.ttl.control;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.neusoft.qiangzi.ttl.listener.ISpeechListener;
import com.neusoft.qiangzi.ttl.util.IOfflineResourceConst;

import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

/**
 * 合成引擎的初始化参数
 * <p>
 * Created by fujiayi on 2017/9/13.
 */

public class InitConfig {
    private static final String TAG = "InitConfig";
    /**
     * appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */
    private String appId;

    private String appKey;

    private String secretKey;

    private String sn;

    /**
     * 纯在线或者离在线融合
     */
    private TtsMode ttsMode;


    /**
     * 初始化的其它参数，用于setParam
     */
    private Map<String, String> params = new HashMap<>();

    /**
     * 合成引擎的回调
     */
    private ISpeechListener listener;

    private InitConfig() {

    }

    // 离在线SDK用
    public InitConfig(Context context, ISpeechListener listener) {
        this.appId = getMetaData(context, "com.baidu.speech.APP_ID");
        this.appKey = getMetaData(context, "com.baidu.speech.API_KEY");
        this.secretKey = getMetaData(context, "com.baidu.speech.SECRET_KEY");
        Log.d(TAG, "InitConfig: appId="+appId);
        Log.d(TAG, "InitConfig: appKey="+appKey);
        Log.d(TAG, "InitConfig: secretKey="+secretKey);
        this.ttsMode = IOfflineResourceConst.DEFAULT_SDK_TTS_MODE;
        this.listener = listener;
    }

    // 离在线SDK用
    public InitConfig(Context context, Map<String, String> params, ISpeechListener listener) {
        this.appId = getMetaData(context, "com.baidu.speech.APP_ID");
        this.appKey = getMetaData(context, "com.baidu.speech.API_KEY");
        this.secretKey = getMetaData(context, "com.baidu.speech.SECRET_KEY");
        Log.d(TAG, "InitConfig: appId="+appId);
        Log.d(TAG, "InitConfig: appKey="+appKey);
        Log.d(TAG, "InitConfig: secretKey="+secretKey);
        this.ttsMode = IOfflineResourceConst.DEFAULT_SDK_TTS_MODE;
        this.params = params;
        this.listener = listener;
    }

    // 离在线SDK用
    public InitConfig(String appId, String appKey, String secretKey, TtsMode ttsMode,
                      Map<String, String> params, ISpeechListener listener) {
        this.appId = appId;
        this.appKey = appKey;
        this.secretKey = secretKey;
        this.ttsMode = ttsMode;
        this.params = params;
        this.listener = listener;
    }


    // 纯离线SDK用
    public InitConfig(String appId, String appKey, String secretKey, String sn, TtsMode ttsMode,
                      Map<String, String> params, ISpeechListener listener) {
        this(appId, appKey, secretKey, ttsMode, params, listener);
        this.sn = sn;
        if (sn != null) {
            // 纯离线sdk 才有的参数；离在线版本没有
            params.put(IOfflineResourceConst.PARAM_SN_NAME, sn);
        }
    }

    public static final String PARAM_SPEAKER_WOMAN = "0";//普通女声
    public static final String PARAM_SPEAKER_MAN = "1";//普通男声
    public static final String PARAM_SPEAKER_SPECIAL_MAN = "2";//特别男声
    public static final String PARAM_SPEAKER_EMOTION_MAN = "3";//情感男声<度逍遥>
    public static final String PARAM_SPEAKER_EMOTION_CHILD = "4";//情感儿童声<度丫丫>
    public static final String PARAM_SPEAKER_EMOTION_WOMAN = "5";//度小娇（情感女声）
    public static final String PARAM_SPEAKER_DUBOWEN = "106";//度博文（情感男声）
    public static final String PARAM_SPEAKER_DUXIAOTONG = "110";//度小童（情感儿童声）
    public static final String PARAM_SPEAKER_DUXIAOMENG = "111";//度小萌（情感女声）
    public static final String PARAM_SPEAKER_DUMIDUO = "103";//度米朵（情感儿童声）

    // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>, 其它发音人见文档
    public void setParamSpeaker(String speaker){
        params.put(SpeechSynthesizer.PARAM_SPEAKER, speaker);
    }
    // 设置合成的音量，0-15 ，默认 5
    public void setParamVolume(int volume){
        if(volume < 0)volume = 0;
        else if(volume > 15)volume = 15;
        params.put(SpeechSynthesizer.PARAM_VOLUME, String.valueOf(volume));
    }
    // 设置合成的语速，0-15 ，默认 5
    public void setParamSpeed(int speed){
        if(speed < 0)speed = 0;
        else if(speed > 15)speed = 15;
        params.put(SpeechSynthesizer.PARAM_SPEED, String.valueOf(speed));
    }
    // 设置合成的语调，0-15 ，默认 5
    public void setParamPitch(int pitch){
        if(pitch < 0)pitch = 0;
        else if(pitch > 15)pitch = 15;
        params.put(SpeechSynthesizer.PARAM_PITCH, String.valueOf(pitch));
    }

    private String getMetaData(Context context, String name) {
        try {
            final ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);

            if (ai.metaData != null) {
                return ai.metaData.get(name).toString();
            }
        }catch (Exception e) {
            Log.e(TAG, "Couldn't find meta-data: " + name);
        }
        return null;
    }

    public ISpeechListener getListener() {
        return listener;
    }

    public Map<String, String> getParams() {
        return params;
    }


    public String getAppId() {
        return appId;
    }

    public String getAppKey() {
        return appKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public TtsMode getTtsMode() {
        return ttsMode;
    }

    public String getSn() {
        return sn;
    }
}
