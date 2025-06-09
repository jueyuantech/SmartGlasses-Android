package com.jueyuantech.glasses.stt;

public interface SttWorkerCallback {
    int MSG_TYPE_UPDATE = 0;
    int MSG_TYPE_NEW = 1;
    int MSG_TYPE_APPEND = 2;

    void onWorkerInitComplete(String funcType, String engineType, String audioSource);

    void onWorkerStarting();
    void onWorkerStart();
    void onWorkerStopping();
    void onWorkerStop();

    void onWorkerErr(int code, String msg, String cause);

    void onEngineStart();

    void onEngineStop();

    void onEngineErr(int code, String msg, String cause);

    void onEngineTick(long time);

    void onRecorderStart(String audioFilePath, String audioFileName);

    void onRecorderStop();

    void onRecorderErr(int code, String msg, String cause);

    void onSttMessage(int type, String transcribeStr, String translateStr, boolean isEnd);

    void onSysMessage(int level, String msg);

    void onHintMessage(String transcribeHintStr, String translateHintStr);

    void onAudioTrackStateChanged(boolean silence);
}
