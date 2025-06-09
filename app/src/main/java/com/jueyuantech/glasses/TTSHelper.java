package com.jueyuantech.glasses;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;

public class TTSHelper implements TextToSpeech.OnInitListener {
    HashMap ttsOptions = new HashMap<String, String>();
    private TextToSpeech mSpeech;
    private Locale mTargetLocale = Locale.ENGLISH;

    public TTSHelper(Context context) {
        long utteranceId = System.currentTimeMillis();
        //ttsOptions.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(utteranceId));//utterance，这个参数随便写，用于监听播报完成的回调中
        //ttsOptions.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, String.valueOf(1));//音量
        //ttsOptions.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));//播放类型

        mSpeech = new TextToSpeech(context, this);
        mSpeech.setOnUtteranceProgressListener(utteranceProgressListener);
    }

    @Override
    public void onInit(int status) {
        if (mSpeech != null) {
            int isSupport = mSpeech.isLanguageAvailable(mTargetLocale);//是否支持中文
            mSpeech.getMaxSpeechInputLength();//最大播报文本长度

            if (isSupport == TextToSpeech.LANG_AVAILABLE) {
                int setLanRet = mSpeech.setLanguage(mTargetLocale);//设置语言
                int setSpeechRateRet = mSpeech.setSpeechRate(1.0f);//设置语
                int setPitchRet = mSpeech.setPitch(1.0f);//设置音量
                String defaultEngine = mSpeech.getDefaultEngine();//默认引擎
                if (status == TextToSpeech.SUCCESS) {
                    //初始化TextToSpeech引擎成功，初始化成功后才可以play等
                }
            }
        } else {
            //初始化TextToSpeech引擎失败
        }
    }

    public void start(String tts) {
        int ret = mSpeech.speak(tts, TextToSpeech.QUEUE_FLUSH, ttsOptions);
        if (ret == TextToSpeech.SUCCESS) {
            //播报成功
        }
    }

    public void append(String tts) {
        if (null != mSpeech) {
            mSpeech.speak(tts, TextToSpeech.QUEUE_ADD, ttsOptions);
        }
    }

    /*
    private void save() {
        long utteranceId = System.currentTimeMillis();
        File file = new File("/sdcard/audio_" + utteranceId + ".wav");
        int ret = synthesizeToFile("xxxxx", null, file, String.valueOf(utteranceId));
        if (ret == TextToSpeech.SUCCESS) {
            //合成文件成功
        }
    }
    */

    public void stop() {
        mSpeech.stop();
    }

    public void destroy() {
        mSpeech.shutdown();
    }

    private UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {

        }

        @Override
        public void onDone(String utteranceId) {

        }

        @Override
        public void onError(String utteranceId) {

        }
    };
}