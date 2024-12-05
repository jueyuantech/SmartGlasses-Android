package com.jueyuantech.glasses.stt;

import okhttp3.Response;
import okhttp3.WebSocket;

public abstract class SttManager {

    public static final int STT_MSG_TYPE_VAR = 0;
    public static final int STT_MSG_TYPE_REC = 1;

    public abstract void connect();
    public abstract void disconnect();

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
        void onError(String msg);

        /**
         *
         * @param speakerId
         * @param type {@link #STT_MSG_TYPE_VAR}, {@link #STT_MSG_TYPE_REC}
         * @param asr
         * @param trans
         */
        void onMessage(long segmentId, String speakerId, int type, String asr, String trans);
    }
}
