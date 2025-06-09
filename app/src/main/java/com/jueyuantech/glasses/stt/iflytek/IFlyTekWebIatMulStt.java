package com.jueyuantech.glasses.stt.iflytek;

import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.jueyuantech.glasses.bean.LanguageTag;
import com.jueyuantech.glasses.stt.SttConfigManager;
import com.jueyuantech.glasses.stt.SttEngine;
import com.jueyuantech.glasses.util.LogUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class IFlyTekWebIatMulStt extends SttEngine {
    private static final String BASE_URL = "https://iat.cn-huabei-1.xf-yun.com/v1"; // 注意多语种识别，也支持中文音频

    private Gson gson = new Gson();

    private OkHttpClient mSocketClient;
    private Request mSocketRequest;
    private WebSocket mSocket;

    private boolean isHeaderSent = false;

    private boolean isTrans = false;
    private String transcribeSourceKey = "";
    private String translateSourceKey = "";
    private String translateTargetKey = "";

    private String mFirstFrameStr = "";

    private long mSegmentId = 0;
    private boolean mSegmentEnd = true;

    int seq = 0; //数据序号
    String appId = "";

    public IFlyTekWebIatMulStt(String func) {
        isTrans = STT_FUNC_TRANSLATE.equals(func);

        if (isTrans) {

        } else {
            LanguageTag transcribeSource = SttConfigManager.getInstance().getSourceLanTag(
                    SttConfigManager.getInstance().getEngine(),
                    STT_FUNC_TRANSCRIBE
            );
            if (null != transcribeSource) {
                transcribeSourceKey = transcribeSource.getTag();
            }
        }
    }

    @Override
    public String getName() {
        return "IFlyTekWebIatMul";
    }

    @Override
    public String getServiceId() {
        return "9B4D2E8F1A3C5D7E9F0A1B2C4D6E8F0A";
    }

    @Override
    public String getLocalParam() {
        return null;
    }

    @Override
    public void initParam(String params) {
        Param param = gson.fromJson(params, Param.class);
        appId = param.getAPPID();
        mFirstFrameStr = getFirstFrame(param.getAPPID());

        String authUrl = null;
        try {
            authUrl = getAuthUrl(BASE_URL, param.getAPIKey(), param.getAPISecret());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        mSocketClient = new OkHttpClient.Builder().build();
        String url = authUrl.toString().replace("http://", "ws://").replace("https://", "wss://");
        LogUtil.i(url);
        mSocketRequest = new Request.Builder().url(url).build();
    }

    @Override
    public void connect() {
        LogUtil.mark();
        if (null != mOnSttListener) {
            mOnSttListener.onConnect("");
        }
        openSocket();
    }

    @Override
    public void disconnect() {
        mSocket.send(getLastFrame());
        closeSocket();
    }

    @Override
    public boolean shouldRetry(int errCode) {
        return false;
    }

    @Override
    public void send(byte[] data) {
        if (!isHeaderSent) return;
        seq++;
        writeSocket(getContinueFrame(data, data.length));
    }

    /* WebSocket */
    private void openSocket() {
        mSocket = mSocketClient.newWebSocket(mSocketRequest, mWebSocketListener);
    }

    private void closeSocket() {
        mSocket.close(/* NORMAL_CLOSURE_STATUS */ 1000, "Close");
    }

    private void reopenSocket() {
        mSocket.send(getLastFrame());
        mSocket.close(/* NORMAL_CLOSURE_STATUS */ 1000, "Close");
        mSocket = mSocketClient.newWebSocket(mSocketRequest, mWebSocketListener);
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

            seq = 0; //数据序号
            writeSocket(mFirstFrameStr);
            isHeaderSent = true;
        }

        @Override
        public void onMessage(WebSocket webSocket, String msg) {
            LogUtil.i("webSocket onMessage = " + msg);
            JsonParse jsonParse = gson.fromJson(msg, JsonParse.class);

            if (null == jsonParse) {
                return;
            }

            int respCode = jsonParse.header.code;
            if (0 != respCode) {
                mOnSttListener.onError(respCode, jsonParse.header.message);
                LogUtil.i("错误码查询链接：https://www.xfyun.cn/document/error-code");
                return;
            }

            if (null == jsonParse.payload) {
                return;
            }

            int status = 0;
            String te = "";
            if (jsonParse.payload.result.text != null) { // 中间结果
                byte[] decodedBytes = Base64.getDecoder().decode(jsonParse.payload.result.text);
                String decodeRes = new String(decodedBytes, StandardCharsets.UTF_8);
                JsonParseText jsonParseText = gson.fromJson(decodeRes, JsonParseText.class);
                List<Ws> wsList = jsonParseText.ws;
                for (Ws ws : wsList) {
                    List<Cw> cwList = ws.cw;
                    for (Cw cw : cwList) {
                        te += cw.w;
                        LogUtil.i(cw.w);
                    }
                }
            }

            status = jsonParse.payload.result.status;
            LogUtil.i("[sid]-> " + jsonParse.header.sid + " [status]-> " + status + " [te]-> " + te);

            if (0 == status || 1 == status) {
                if (mSegmentEnd) {
                    mSegmentEnd = false;
                    mSegmentId = System.currentTimeMillis();
                    LogUtil.i("recognizing : NEW SegmentId " + mSegmentId);
                }

                if (null != mOnSttListener) {
                    mOnSttListener.onMessage(mSegmentId, "", STT_MSG_TYPE_VAR, te, "");
                }
            } else if (2 == status) {
                mSegmentEnd = true;

                if (!TextUtils.isEmpty(te)) {
                    if (null != mOnSttListener) {
                        mOnSttListener.onMessage(mSegmentId, "", STT_MSG_TYPE_REC, te, "");
                    }
                }

                isHeaderSent = false;

                reopenSocket();
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            LogUtil.i("webSocket onClosing");
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            LogUtil.i("webSocket onFailure");
            try {
                if (null != response) {
                    int code = response.code();
                    LogUtil.i("onFailure code:" + code);
                    LogUtil.i("onFailure body:" + response.body().string());

                    mOnSttListener.onError(code, response.body().string());
                    LogUtil.i("错误码查询链接：https://www.xfyun.cn/document/error-code");
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
            LogUtil.i("webSocket ByteString onMessage");
            super.onMessage(webSocket, bytes);
        }
    };

    class Param {
        String APPID;
        String APIKey;
        String APISecret;

        public String getAPPID() {
            return APPID;
        }

        public void setAPPID(String APPID) {
            this.APPID = APPID;
        }

        public String getAPIKey() {
            return APIKey;
        }

        public void setAPIKey(String APIKey) {
            this.APIKey = APIKey;
        }

        public String getAPISecret() {
            return APISecret;
        }

        public void setAPISecret(String APISecret) {
            this.APISecret = APISecret;
        }
    }

    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").//
                append("date: ").append(date).append("\n").//
                append("GET ").append(url.getPath()).append(" HTTP/1.1");
        // System.out.println(builder);
        Charset charset = Charset.forName("UTF-8");
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
        String sha = Base64.getEncoder().encodeToString(hexDigits);

        //System.out.println(sha);
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        //System.out.println(authorization);
        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath()).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(charset))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();
        return httpUrl.toString();
    }

    private String getFirstFrame(String appId) {
        byte[] buffer = new byte[0];
        String json = "{\n" +
                "  \"header\": {\n" +
                "    \"app_id\": \"" + appId + "\",\n" +
                "    \"status\": " + 0 + "\n" +
                "  },\n" +
                "  \"parameter\": {\n" +
                "    \"iat\": {\n" +
                "      \"domain\": \"slm\",\n" +
                "      \"language\": \"mul_cn\",\n" +
                "      \"accent\": \"mandarin\",\n" +
                "      \"eos\": 6000,\n" +
                "      \"vinfo\": 1,\n" +
                "      \"result\": {\n" +
                "        \"encoding\": \"utf8\",\n" +
                "        \"compress\": \"raw\",\n" +
                "        \"format\": \"json\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"payload\": {\n" +
                "    \"audio\": {\n" +
                "      \"encoding\": \"raw\",\n" +
                "      \"sample_rate\": 16000,\n" +
                "      \"channels\": 1,\n" +
                "      \"bit_depth\": 16,\n" +
                "      \"seq\": " + 0 + ",\n" +
                "      \"status\": 0,\n" +
                "      \"audio\": \"" + Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, 0)) + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        return json;
    }

    private String getContinueFrame(byte[] buffer, int len) {
        String json = "{\n" +
                "  \"header\": {\n" +
                "    \"app_id\": \"" + appId + "\",\n" +
                "    \"status\": 1\n" +
                "  },\n" +
                "  \"payload\": {\n" +
                "    \"audio\": {\n" +
                "      \"encoding\": \"raw\",\n" +
                "      \"sample_rate\": 16000,\n" +
                "      \"channels\": 1,\n" +
                "      \"bit_depth\": 16,\n" +
                "      \"seq\": " + seq + ",\n" +
                "      \"status\": 1,\n" +
                "      \"audio\": \"" + Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, len)) + "\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        return json.toString();
    }

    private String getLastFrame() {
        String json = "{\n" +
                "  \"header\": {\n" +
                "    \"app_id\": \"" + appId + "\",\n" +
                "    \"status\": 2\n" +
                "  },\n" +
                "  \"payload\": {\n" +
                "    \"audio\": {\n" +
                "      \"encoding\": \"raw\",\n" +
                "      \"sample_rate\": 16000,\n" +
                "      \"channels\": 1,\n" +
                "      \"bit_depth\": 16,\n" +
                "      \"seq\": " + seq + ",\n" +
                "      \"status\": 2,\n" +
                "      \"audio\": \"\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        return json;
    }

    // 返回结果拆分与展示，仅供参考
    class JsonParse {
        Header header;
        Payload payload;
    }

    class Header {
        int code;
        String message;
        String sid;
        int status;
    }

    class Payload {
        Result result;
    }

    class Result {
        String text;
        int status;
    }

    class JsonParseText {
        List<Ws> ws;
        String pgs;
        List<Integer> rg;
    }

    class Ws {
        List<Cw> cw;
    }

    class Cw {
        String w;
    }
}