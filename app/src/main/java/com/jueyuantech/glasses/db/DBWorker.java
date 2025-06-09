package com.jueyuantech.glasses.db;

import android.content.Context;
import android.text.TextUtils;

import com.jueyuantech.glasses.bean.SpeechMessage;
import com.jueyuantech.glasses.bean.SpeechSession;
import com.jueyuantech.glasses.stt.SttWorker;
import com.jueyuantech.glasses.stt.SttWorkerCallback;
import com.jueyuantech.glasses.util.LogUtil;

import java.util.UUID;

public class DBWorker {

    private static final boolean DEBUG = false;
    private static volatile DBWorker singleton;
    private Context mContext;

    private DBHelper dbHelper;

    private long curSessionId = -1;
    private SpeechMessage curSpeechMessage;


    private DBWorker(Context context) {
        mContext = context.getApplicationContext();
        dbHelper = new DBHelper(mContext);
        initWorker();
    }

    public static DBWorker getInstance() {
        if (singleton == null) {
            throw new IllegalStateException("DBWorker is not initialized. Call init() before getInstance().");
        }
        return singleton;
    }

    /**
     * 初始化方法，应该在Application中调用
     *
     * @param context
     */
    public static void init(Context context) {
        if (singleton == null) {
            synchronized (DBWorker.class) {
                if (singleton == null) {
                    singleton = new DBWorker(context);
                }
            }
        }
    }

    private void initWorker() {
        SttWorker.getInstance().addSttWorkerCallback(sttWorkerCallback);
    }

    private void saveSysMessage(String message) {
        if (DEBUG) LogUtil.i("save SYS ==> curSessionId[" + curSessionId + "] " + message);
        SpeechMessage msg = new SpeechMessage();
        msg.setSessionId(curSessionId);
        msg.setOriginalText(message);
        msg.setType("sys");
        dbHelper.addMessage(msg);
    }

    private void saveSttMessage() {
        if (null == curSpeechMessage) {
            return;
        }

        String ori = curSpeechMessage.getOriginalText();
        String trans = curSpeechMessage.getTranslatedText();
        if (TextUtils.isEmpty(ori) && TextUtils.isEmpty(trans)) {
            return;
        }

        if (DEBUG) LogUtil.i("save STT ori ==> curSessionId[" + curSessionId + "] " + ori);
        if (DEBUG) LogUtil.i("save STT trans ==> curSessionId[" + curSessionId + "] " + trans);

        SpeechMessage msg = new SpeechMessage();
        msg.setSessionId(curSessionId);
        msg.setOriginalText(TextUtils.isEmpty(ori) ? "" : ori);
        msg.setTranslatedText(TextUtils.isEmpty(trans) ? "" : trans);
        msg.setType("stt");
        dbHelper.addMessage(msg);

        curSpeechMessage.setOriginalText("");
        curSpeechMessage.setTranslatedText("");
    }

    private SttWorkerCallback sttWorkerCallback = new SttWorkerCallback() {
        @Override
        public void onWorkerInitComplete(String funcType, String engineType, String audioSource) {

            SpeechSession newSpeechSession = new SpeechSession();
            newSpeechSession.setUuid(UUID.randomUUID().toString());
            newSpeechSession.setTitle("Session");
            newSpeechSession.setFuncType(funcType);
            newSpeechSession.setAudioSource(audioSource);

            curSessionId = dbHelper.createSession(newSpeechSession);
            curSpeechMessage = new SpeechMessage();
            if (DEBUG) LogUtil.i(" ==> curSessionId " + curSessionId);
        }

        @Override
        public void onWorkerStarting() {

        }

        @Override
        public void onWorkerStart() {

        }

        @Override
        public void onWorkerStopping() {

        }

        @Override
        public void onWorkerStop() {
            saveSttMessage();
        }

        @Override
        public void onWorkerErr(int code, String msg, String cause) {
            saveSysMessage("WORKER ERR [" + code + "] " + cause);
        }

        @Override
        public void onEngineStart() {

        }

        @Override
        public void onEngineStop() {

        }

        @Override
        public void onEngineErr(int code, String msg, String cause) {
            saveSysMessage("ENGINE ERR [" + code + "] " + cause);
        }

        @Override
        public void onEngineTick(long time) {

        }

        @Override
        public void onRecorderStart(String audioFilePath, String audioFileName) {
            dbHelper.updateSessionAudioFileName(curSessionId, audioFileName);
        }

        @Override
        public void onRecorderStop() {

        }

        @Override
        public void onRecorderErr(int code, String msg, String cause) {
            saveSysMessage("REC ERR [" + code + "] " + cause);
        }

        @Override
        public void onSttMessage(int type, String transcribeStr, String translateStr, boolean isEnd) {
            if (type == MSG_TYPE_NEW) {
                // 保存上一句
                saveSttMessage();
            }
            curSpeechMessage.setOriginalText(transcribeStr);
            curSpeechMessage.setTranslatedText(translateStr);
        }

        @Override
        public void onSysMessage(int level, String msg) {

        }

        @Override
        public void onHintMessage(String transcribeHintStr, String translateHintStr) {

        }

        @Override
        public void onAudioTrackStateChanged(boolean silence) {

        }
    };
}
