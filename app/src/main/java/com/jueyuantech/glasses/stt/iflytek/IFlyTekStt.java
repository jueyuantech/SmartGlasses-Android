package com.jueyuantech.glasses.stt.iflytek;

import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_TRANSLATE;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jueyuantech.glasses.BuildConfig;
import com.jueyuantech.glasses.bean.LanguageTag;
import com.jueyuantech.glasses.stt.AsrConfigManager;
import com.jueyuantech.glasses.stt.SttManager;
import com.jueyuantech.glasses.util.EncryptUtil;
import com.jueyuantech.glasses.util.LogUtil;

import java.net.URLEncoder;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class IFlyTekStt extends SttManager {

    private static final String HOST = "rtasr.xfyun.cn/v1/ws";
    private static final String BASE_URL = "wss://" + HOST;

    private OkHttpClient mSocketClient;
    private Request mSocketRequest;
    private WebSocket mSocket;

    private boolean isTrans = false;
    private String transcribeSourceKey = "";
    private String translateSourceKey = "";
    private String translateTargetKey = "";

    public IFlyTekStt(String func) {
        isTrans = ASR_FUNC_TRANSLATE.equals(func);

        if (isTrans) {

        } else {
            LanguageTag transcribeSource = AsrConfigManager.getInstance().getSourceLanTag(
                    AsrConfigManager.getInstance().getEngine(),
                    ASR_FUNC_TRANSCRIBE
            );
            if (null != transcribeSource) {
                transcribeSourceKey = transcribeSource.getTag();
            }
        }

        mSocketClient = new OkHttpClient();
        mSocketRequest = new Request.Builder().url(BASE_URL + getHandShakeParams(BuildConfig.PROP_IFLYTEK_APP_ID, BuildConfig.PROP_IFLYTEK_SECRET_KEY, transcribeSourceKey)).build();
    }

    @Override
    public void connect() {
        openSocket();
    }

    @Override
    public void disconnect() {
        mSocket.send("{\"end\": true}");
        closeSocket();
    }

    @Override
    public void send(byte[] data) {
        mSocket.send(ByteString.of(data));
    }


    /* WebSocket */
    private void openSocket() {
        mSocket = mSocketClient.newWebSocket(mSocketRequest, mWebSocketListener);
    }

    private void closeSocket() {
        mSocket.close(/* NORMAL_CLOSURE_STATUS */ 1000, "Close");
    }

    private void writeSocket(String s) {
        if (null != mSocket) {
            mSocket.send(s);
        }
    }

    private void writeSocket(ByteString byteString) {
        if (null != mSocket) {
            mSocket.send(byteString);
        }
    }

    private WebSocketListener mWebSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            LogUtil.i("webSocket onOpen");
        }

        @Override
        public void onMessage(WebSocket webSocket, String msg) {
            LogUtil.i("webSocket onMessage = " + msg);

            JSONObject msgObj = JSON.parseObject(msg);
            String action = msgObj.getString("action");
            if (Objects.equals("started", action)) {
                // 握手成功
                LogUtil.i("\t握手成功！sid: " + msgObj.getString("sid"));
                //handshakeSuccess.countDown();

                if (null != mOnSttListener) {
                    mOnSttListener.onConnect(msgObj.getString("sid"));
                }
            } else if (Objects.equals("result", action)) {
                // 转写结果
                String result = getContent(msgObj.getString("data"));
                LogUtil.i("\tresult: " + result);
            } else if (Objects.equals("error", action)) {
                // 连接发生错误
                LogUtil.i("Error: " + msg);
                //System.exit(0);
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            LogUtil.i("webSocket onClosing");
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            LogUtil.i("webSocket onFailure");
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
            LogUtil.i("webSocket ByteString onMessage");
            super.onMessage(webSocket, bytes);
        }
    };

    private String getHandShakeParams(String appId, String secretKey, String sourceLanguage) {
        String ts = System.currentTimeMillis() / 1000 + "";
        String signa = "";
        try {
            signa = EncryptUtil.HmacSHA1Encrypt(EncryptUtil.MD5(appId + ts), secretKey);
            if ("zh".equals(sourceLanguage)) {
                return "?appid=" + appId + "&ts=" + ts + "&signa=" + URLEncoder.encode(signa, "UTF-8");
            } else {
                return "?appid=" + appId + "&ts=" + ts + "&signa=" + URLEncoder.encode(signa, "UTF-8") + "&lang=" + sourceLanguage;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public String getContent(String message) {
        LogUtil.i("getContent = ");
        StringBuffer resultBuilder = new StringBuffer();
        try {
            JSONObject messageObj = JSON.parseObject(message);
            JSONObject cn = messageObj.getJSONObject("cn");
            JSONObject st = cn.getJSONObject("st");
            JSONArray rtArr = st.getJSONArray("rt");

            String type = st.getString("type");
            LogUtil.i("type = " + type);

            for (int i = 0; i < rtArr.size(); i++) {
                JSONObject rtArrObj = rtArr.getJSONObject(i);
                JSONArray wsArr = rtArrObj.getJSONArray("ws");
                for (int j = 0; j < wsArr.size(); j++) {
                    JSONObject wsArrObj = wsArr.getJSONObject(j);
                    JSONArray cwArr = wsArrObj.getJSONArray("cw");
                    for (int k = 0; k < cwArr.size(); k++) {
                        JSONObject cwArrObj = cwArr.getJSONObject(k);
                        String wStr = cwArrObj.getString("w");
                        resultBuilder.append(wStr);
                    }
                }
            }

            if ("1".equals(type)) {
                if (null != mOnSttListener) {
                    mOnSttListener.onMessage(0, "", STT_MSG_TYPE_VAR, resultBuilder.toString(), "");
                }
            } else if ("0".equals(type)) { // DONE
                if (null != mOnSttListener) {
                    mOnSttListener.onMessage(0, "", STT_MSG_TYPE_REC, resultBuilder.toString(), "");
                }
            }
        } catch (Exception e) {
            return message;
        }

        return resultBuilder.toString();
    }
}
