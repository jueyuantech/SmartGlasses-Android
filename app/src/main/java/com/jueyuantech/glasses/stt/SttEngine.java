package com.jueyuantech.glasses.stt;

import okhttp3.Response;
import okhttp3.WebSocket;

public abstract class SttEngine {

    public static final int STT_MSG_TYPE_VAR = 0;
    public static final int STT_MSG_TYPE_REC = 1;

    private String mSourceLanguageHint = "";
    private String mTargetLanguageHint = "";

    public String getSourceLanguageHint() {
        return mSourceLanguageHint;
    }

    public void setSourceLanguageHint(String sourceLanguageHint) {
        this.mSourceLanguageHint = sourceLanguageHint;
    }

    public String getTargetLanguageHint() {
        return mTargetLanguageHint;
    }

    public void setTargetLanguageHint(String targetLanguageHint) {
        this.mTargetLanguageHint = targetLanguageHint;
    }

    public abstract String getName();

    public abstract String getServiceId();

    public abstract String getLocalParam();

    public abstract void initParam(String params);

    public abstract void connect();

    public abstract void disconnect();

    public abstract boolean shouldRetry(int errCode);

    public abstract void send(byte[] data);

    public interface OnSocketListener {
        void onOpen(WebSocket webSocket, Response response);

        void onMessage(WebSocket webSocket, String msg);

        void onClosing(WebSocket webSocket, int code, String reason);

        void onFailure(WebSocket webSocket, Throwable t, Response response);
    }


    protected OnSttListener mOnSttListener;

    public void setOnSttListener(OnSttListener listener) {
        this.mOnSttListener = listener;
    }

    public interface OnSttListener {
        void onConnect(String sid);

        void onError(int code, String msg);

        /**
         * @param speakerId
         * @param type          {@link #STT_MSG_TYPE_VAR}, {@link #STT_MSG_TYPE_REC}
         * @param transcribeStr
         * @param translateStr
         */
        void onMessage(long segmentId, String speakerId, int type, String transcribeStr, String translateStr);
    }
}
