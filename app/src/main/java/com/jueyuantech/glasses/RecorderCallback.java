package com.jueyuantech.glasses;

public interface RecorderCallback {
    int MSG_TYPE_UPDATE = 0;
    int MSG_TYPE_NEW = 1;
    int MSG_TYPE_APPEND = 2;

    void onMessage(int type, String transcribeStr, String translateStr,boolean isEnd);
    void onAudioTrackStateChanged(boolean silence);
    void onConnectSucceed();
    void onConnectFailed(String message);
}
