package com.jueyuantech.glasses.stt.iflytek;

import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jueyuantech.glasses.bean.LanguageTag;
import com.jueyuantech.glasses.stt.SttConfigManager;
import com.jueyuantech.glasses.stt.SttEngine;
import com.jueyuantech.glasses.util.LogUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class IFlyTekWebAsrNiuStt extends SttEngine {
    //private static final String BASE_URL_TRANSCRIBE = "";
    private static final String BASE_URL_TRANSCRIBE = "";
    private static final String BASE_URL_TRANSLATE_NIU = "";

    private Gson gson = new Gson();

    // niuTrans 相关成员变量
    private String niuTransHost = "";
    private OkHttpClient mNiuTransClient;
    private JsonObject niuTransCommonBody = null;
    private JsonObject niuTransBusinessBody = null;

    private OkHttpClient mSocketClient;
    private Request mSocketRequest;
    private WebSocket mSocket;

    private boolean isHeaderSent = false;

    private Map<Long, String> mTranscribeMap = new HashMap<>();
    private Map<Long, String> mTranslateMap = new HashMap<>();

    private boolean isTrans = false;
    private String transcribeSourceKey = "";
    private String translateSourceKey = "";
    private String translateTargetKey = "";
    private boolean rlang = false;

    private String mFirstFrameStr = "";

    private long mSegmentId = 0;
    private boolean mSegmentEnd = true;
    private String mTranscribeResult = "";
    private String mTranslateResult = "";

    private Decoder decoder = new Decoder();

    private Param mParam;

    /**
     * 将翻译TAG转换为转写TAG
     * 根据语言映射表进行转换
     * @param translateTag 翻译TAG
     * @return 转写TAG，如果没有对应映射则返回原值
     */
    private static String convertTranslateTagToTranscribeTag(String translateTag) {
        if (translateTag == null) {
            return null;
        }
        
        switch (translateTag) {
            case "cn":
                return "zh_cn";
            case "cht":
                return "zh_cn"; // 繁体中文翻译源，但引擎不支持zh_tw，使用zh_cn
            case "en":
                return "en_us";
            case "ko":
                return "ko_kr";
            case "ja":
                return "ja_jp";
            case "it":
                return "it_IT";
            case "de":
                return "de_DE";
            case "ru":
                return "ru-ru";
            case "fr":
                return "fr_fr";
            case "fil":
                return "fil_PH";
            case "ha":
                return "ha_NG";
            case "nl":
                return "nl_NL";
            case "cs":
                return "cs_CZ";
            case "ro":
                return "ro_ro";
            case "ms":
                return "ms_MY";
            case "bn":
                return "bn_BD";
            case "pt":
                return "pt_PT";
            case "sv":
                return "sv_SE";
            case "sw":
                return "sw_KE";
            case "ta":
                return "ta_in";
            case "th":
                return "th_TH";
            case "tr":
                return "tr_TR";
            case "ur":
                return "ur_IN";
            case "uk":
                return "uk_UA";
            case "uz":
                return "uz_UZ";
            case "es":
                return "es_es";
            case "el":
                return "el_GR";
            case "hi":
                return "hi_in";
            case "id":
                return "id_ID";
            case "vi":
                return "vi_VN";
            case "ar":
                return "ar_il";
            case "bg":
                return "bg_bg";
            case "pl":
                return "pl_pl";
            case "fa":
                return "fa_IR";
            default:
                return translateTag; // 如果没有对应映射，返回原值
        }
    }

    public IFlyTekWebAsrNiuStt(String func) {
        isTrans = STT_FUNC_TRANSLATE.equals(func);

        if (isTrans) {
            LanguageTag translateSource = SttConfigManager.getInstance().getSourceLanTag(
                    SttConfigManager.getInstance().getEngine(),
                    STT_FUNC_TRANSLATE
            );
            if (null != translateSource) {
                translateSourceKey = translateSource.getTag();
                setSourceLanguageHint(translateSource.getTitle());

                transcribeSourceKey = convertTranslateTagToTranscribeTag(translateSourceKey);
                
                // 特殊处理繁体中文
                if ("cht".equals(translateSourceKey)) {
                    rlang = true;
                }

                LanguageTag translateTarget = SttConfigManager.getInstance().getTargetLanTag(
                        SttConfigManager.getInstance().getEngine(),
                        STT_FUNC_TRANSLATE,
                        translateSource.getTag()
                );
                if (null != translateTarget) {
                    translateTargetKey = translateTarget.getTag();
                    setTargetLanguageHint(translateTarget.getTitle());
                }
            }
        } else {
            LanguageTag transcribeSource = SttConfigManager.getInstance().getSourceLanTag(
                    SttConfigManager.getInstance().getEngine(),
                    STT_FUNC_TRANSCRIBE
            );
            if (null != transcribeSource) {
                transcribeSourceKey = transcribeSource.getTag();
                setSourceLanguageHint(transcribeSource.getTitle());
                
                // 如果转写源语言是zh_tw，引擎不支持，使用zh_cn
                if ("zh_tw".equals(transcribeSourceKey)) {
                    transcribeSourceKey = "zh_cn";
                    rlang = true;
                }
            }
        }
    }

    @Override
    public String getName() {
        return "IFlyTekWebAsr";
    }

    @Override
    public String getServiceId() {
        return "";
    }

    @Override
    public String getLocalParam() {
        return "";
    }

    @Override
    public void initParam(String params) {
        Param param = gson.fromJson(params, Param.class);
        mParam = param;
        mFirstFrameStr = getFirstFrame(param.getAPPID(), transcribeSourceKey);

        String authUrl = null;
        try {
            authUrl = getAuthUrl(BASE_URL_TRANSCRIBE, param.getAPIKey(), param.getAPISecret());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        mSocketClient = new OkHttpClient.Builder().build();
        String url = authUrl.toString().replace("http://", "ws://").replace("https://", "wss://");
        LogUtil.i(url);
        mSocketRequest = new Request.Builder().url(url).build();

        // 初始化niuTrans相关配置
        if (isTrans) {
            mNiuTransClient = new OkHttpClient.Builder().build();
            initNiuTransParam(param);
        }
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
            mTranscribeMap.clear();
            mTranslateMap.clear();

            writeSocket(mFirstFrameStr);
            isHeaderSent = true;
        }

        @Override
        public void onMessage(WebSocket webSocket, String msg) {
            LogUtil.i("webSocket onMessage = " + msg);
            ResponseData resp = gson.fromJson(msg, ResponseData.class);

            if (null == resp) {
                return;
            }

            int respCode = resp.getCode();
            if (0 != respCode) {
                mOnSttListener.onError(respCode, resp.getMessage());
                LogUtil.i("错误码查询链接：https://www.xfyun.cn/document/error-code");
                return;
            }

            if (null == resp.getData()) {
                return;
            }

            Result result = resp.getData().getResult();
            int status = resp.getData().getStatus();

            if (null != result) {
                Text te = result.getText();
                //LogUtil.i(te.toString());
                try {
                    decoder.decode(te);
                    LogUtil.i(resp.getSid() + " VAR-> " + decoder.toString());

                    if (mSegmentEnd) {
                        mSegmentEnd = false;
                        mSegmentId = System.currentTimeMillis();
                        mTranscribeMap.put(mSegmentId, "");
                        mTranslateMap.put(mSegmentId, "");
                        LogUtil.i("recognizing : NEW SegmentId " + mSegmentId);
                    }

                    mTranscribeResult = decoder.toString();
                    LogUtil.i("recognizing: " + mTranscribeResult);
                    if (!TextUtils.isEmpty(mTranscribeResult)) {
                        mTranscribeMap.put(mSegmentId, mTranscribeResult);
                        postResult(mSegmentId, STT_MSG_TYPE_VAR);
                        if (isTrans) translateText(mSegmentId, mTranscribeResult, STT_MSG_TYPE_VAR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (2 == status) { // 数据全部返回完毕，可以关闭连接，释放资源
                LogUtil.i(resp.getSid() + " REC-> " + decoder.toString());

                mSegmentEnd = true;
                mTranscribeResult = decoder.toString();
                LogUtil.i("recognized: " + mTranscribeResult);
                if (!TextUtils.isEmpty(mTranscribeResult)) {
                    mTranscribeMap.put(mSegmentId, mTranscribeResult);
                    postResult(mSegmentId, STT_MSG_TYPE_REC);
                    if (isTrans) translateText(mSegmentId, mTranscribeResult, STT_MSG_TYPE_REC);
                }

                decoder.discard();
                isHeaderSent = false;

                reopenSocket();
            } else {
                // TODO 根据返回的数据处理
                LogUtil.i("status else");
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

    private String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        StringBuilder builder = new StringBuilder("host: ").append(url.getHost()).append("\n").//
                append("date: ").append(date).append("\n").//
                append("GET ").append(url.getPath()).append(" HTTP/1.1");
        //LogUtil.i(builder);
        Charset charset = Charset.forName("UTF-8");
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(charset), "hmacsha256");
        mac.init(spec);
        byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
        String sha = Base64.getEncoder().encodeToString(hexDigits);

        //LogUtil.i(sha);
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        //LogUtil.i(authorization);
        HttpUrl httpUrl = HttpUrl.parse("https://" + url.getHost() + url.getPath()).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(charset))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();
        return httpUrl.toString();
    }

    private String getFirstFrame(String appId, String transcribeSourceKey) {
        byte[] buffer = new byte[0];

        JsonObject frame = new JsonObject();
        JsonObject business = new JsonObject();
        JsonObject common = new JsonObject();
        JsonObject data = new JsonObject();

        common.addProperty("app_id", appId);

        business.addProperty("language", transcribeSourceKey);
        business.addProperty("accent", "mandarin"); //中文方言请在控制台添加试用，添加后即展示相应参数值
        business.addProperty("domain", "iat");
        //business.addProperty("nunum", 0);
        //business.addProperty("ptt", 0);//标点符号
        if (rlang) {
            business.addProperty("rlang", "zh-hk"); // zh-cn :简体中文（默认值）zh-hk :繁体香港(若未授权不生效，在控制台可免费开通)
        }
        //business.addProperty("vinfo", 1);
        business.addProperty("dwa", "wpgs");//动态修正(若未授权不生效，在控制台可免费开通)
        //business.addProperty("nbest", 5);// 句子多候选(若未授权不生效，在控制台可免费开通)
        //business.addProperty("wbest", 3);// 词级多候选(若未授权不生效，在控制台可免费开通)

        data.addProperty("status", 0);
        data.addProperty("format", "audio/L16;rate=16000");
        data.addProperty("encoding", "raw");
        data.addProperty("audio", Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, 0)));

        frame.add("common", common);
        frame.add("business", business);
        frame.add("data", data);

        return frame.toString();
    }

    private String getContinueFrame(byte[] buffer, int len) {
        JsonObject frame = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty("status", 1);
        data.addProperty("format", "audio/L16;rate=16000");
        data.addProperty("encoding", "raw");
        data.addProperty("audio", Base64.getEncoder().encodeToString(Arrays.copyOf(buffer, len)));
        frame.add("data", data);
        return frame.toString();
    }

    private String getLastFrame() {
        JsonObject frame = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty("status", 2);
        data.addProperty("audio", "");
        data.addProperty("format", "audio/L16;rate=16000");
        data.addProperty("encoding", "raw");
        frame.add("data", data);
        return frame.toString();
    }

    public static class ResponseData {
        private int code;
        private String message;
        private String sid;
        private Data data;

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return this.message;
        }

        public String getSid() {
            return sid;
        }

        public Data getData() {
            return data;
        }
    }

    public static class Data {
        private int status;
        private Result result;

        public int getStatus() {
            return status;
        }

        public Result getResult() {
            return result;
        }
    }

    public static class Result {
        int bg;
        int ed;
        String pgs;
        int[] rg;
        int sn;
        Ws[] ws;
        boolean ls;
        JsonObject vad;

        public Text getText() {
            Text text = new Text();
            StringBuilder sb = new StringBuilder();
            for (Ws ws : this.ws) {
                sb.append(ws.cw[0].w);
            }
            text.sn = this.sn;
            text.text = sb.toString();
            text.sn = this.sn;
            text.rg = this.rg;
            text.pgs = this.pgs;
            text.bg = this.bg;
            text.ed = this.ed;
            text.ls = this.ls;
            text.vad = this.vad == null ? null : this.vad;
            return text;
        }
    }

    public static class Ws {
        Cw[] cw;
        int bg;
        int ed;
    }

    public static class Cw {
        int sc;
        String w;
    }

    public static class Text {
        int sn;
        int bg;
        int ed;
        String text;
        String pgs;
        int[] rg;
        boolean deleted;
        boolean ls;
        JsonObject vad;

        @Override
        public String toString() {
            return "Text{" +
                    "bg=" + bg +
                    ", ed=" + ed +
                    ", ls=" + ls +
                    ", sn=" + sn +
                    ", text='" + text + '\'' +
                    ", pgs=" + pgs +
                    ", rg=" + Arrays.toString(rg) +
                    ", deleted=" + deleted +
                    ", vad=" + (vad == null ? "null" : vad.getAsJsonArray("ws").toString()) +
                    '}';
        }
    }

    //解析返回数据，仅供参考
    public static class Decoder {
        private Text[] texts;
        private int defc = 10;

        public Decoder() {
            this.texts = new Text[this.defc];
        }

        public synchronized void decode(Text text) {
            if (text.sn >= this.defc) {
                this.resize();
            }
            if ("rpl".equals(text.pgs)) {
                for (int i = text.rg[0]; i <= text.rg[1]; i++) {
                    this.texts[i].deleted = true;
                }
            }
            this.texts[text.sn] = text;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Text t : this.texts) {
                if (t != null && !t.deleted) {
                    sb.append(t.text);
                }
            }
            return sb.toString();
        }

        public void resize() {
            int oc = this.defc;
            this.defc <<= 1;
            Text[] old = this.texts;
            this.texts = new Text[this.defc];
            for (int i = 0; i < oc; i++) {
                this.texts[i] = old[i];
            }
        }

        public void discard() {
            for (int i = 0; i < this.texts.length; i++) {
                this.texts[i] = null;
            }
        }
    }

    private void translateText(long segmentId, String text, int type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String body = buildNiuTransHttpBody(text);
                    Map<String, String> headers = buildNiuTransHttpHeader(mParam, body);
                    String result = doHttpPost(BASE_URL_TRANSLATE_NIU, headers, body);

                    if (result != null) {
                        JsonObject response = gson.fromJson(result, JsonObject.class);
                        if (response.has("data") && response.getAsJsonObject("data").has("result")
                                && response.getAsJsonObject("data").getAsJsonObject("result").has("trans_result")) {
                            JsonObject transResult = response.getAsJsonObject("data")
                                    .getAsJsonObject("result")
                                    .getAsJsonObject("trans_result");
                            if (transResult.has("dst")) {
                                mTranslateResult = transResult.get("dst").getAsString();
                                mTranslateMap.put(segmentId, mTranslateResult);
                                postResult(segmentId, type);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void postResult(long segmentId, int type) {
        LogUtil.i("segmentId " + segmentId + " transcribeStr " + mTranscribeMap.get(segmentId) + " translateStr " + mTranslateMap.get(segmentId));
        if (null != mOnSttListener) {
            //writeFileLine(segmentId + "-" + type + "-" + mTranscribeMap.get(segmentId) + "-" + mTranslateMap.get(segmentId));
            mOnSttListener.onMessage(
                    segmentId,
                    "",
                    type,
                    mTranscribeMap.get(segmentId),
                    mTranslateMap.get(segmentId)
            );
        }
    }

    private void initNiuTransParam(Param param) {
        try {
            // 解析URL获取host
            URL url = new URL(BASE_URL_TRANSLATE_NIU);
            niuTransHost = url.getHost();

            // 预构建固定的common部分
            niuTransCommonBody = new JsonObject();
            niuTransCommonBody.addProperty("app_id", param.getAPPID());

            // 预构建固定的business部分
            niuTransBusinessBody = new JsonObject();
            niuTransBusinessBody.addProperty("from", translateSourceKey);
            niuTransBusinessBody.addProperty("to", translateTargetKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建HTTP请求头
     */
    private Map<String, String> buildNiuTransHttpHeader(Param param, String body) {
        Map<String, String> header = new HashMap<>();

        // 使用RFC1123格式的时间字符串，而不是Unix时间戳
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());

        String bodySign = signBody(body);
        String digestBase64 = "SHA-256=" + bodySign;

        // 构建签名字符串，必须包含digest字段
        String signatureString = "host: " + niuTransHost + "\n" +
                "date: " + date + "\n" +
                "POST /v2/ots HTTP/1.1" + "\n" +
                "digest: " + digestBase64;

        String authorization = "api_key=\"" + param.getAPIKey() + "\", algorithm=\"hmac-sha256\", headers=\"host date request-line digest\", signature=\"" + hmacsign(param.getAPISecret(), signatureString) + "\"";

        header.put("Content-Type", "application/json");
        header.put("Accept", "application/json,version=1.0");
        header.put("Host", niuTransHost);
        header.put("Date", date);
        header.put("Authorization", authorization);
        header.put("Digest", digestBase64);

        return header;
    }

    /**
     * 构建HTTP请求体
     */
    private String buildNiuTransHttpBody(String text) {
        JsonObject body = new JsonObject();
        body.add("common", niuTransCommonBody);
        body.add("business", niuTransBusinessBody);

        JsonObject data = new JsonObject();
        try {
            byte[] textByte = text.getBytes("UTF-8");
            String textBase64 = Base64.getEncoder().encodeToString(textByte);
            data.addProperty("text", textBase64);
        } catch (Exception e) {
            data.addProperty("text", text); // 降级处理
        }
        body.add("data", data);

        return body.toString();
    }

    /**
     * 使用OkHttpClient发送HTTP POST请求
     */
    private String doHttpPost(String url, Map<String, String> headers, String body) {
        try {
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(mediaType, body);

            Request.Builder requestBuilder = new Request.Builder()
                    .url(url)
                    .post(requestBody);

            // 添加请求头
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            Request request = requestBuilder.build();

            try (Response response = mNiuTransClient.newCall(request).execute()) {
                if (response.body() != null) {
                    String responseBody = response.body().string();

                    if (response.isSuccessful()) {
                        return responseBody;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 对body进行SHA-256加密
     */
    private String signBody(String body) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(body.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * hmacsha256加密
     */
    private String hmacsign(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] digest = mac.doFinal(data.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
