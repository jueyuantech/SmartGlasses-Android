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
import com.jueyuantech.glasses.stt.iflytek.bean.ITransReq;
import com.jueyuantech.glasses.stt.iflytek.bean.ITransResult;
import com.jueyuantech.glasses.stt.iflytek.bean.ITransRsp;
import com.jueyuantech.glasses.util.LogUtil;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

public class IFlyTekWebAsrStt extends SttEngine {
    private static final String BASE_URL_TRANSCRIBE = "https://iat-api.xfyun.cn/v2/iat";
    private static final String BASE_URL_TRANSLATE = "https://itrans.xf-yun.com/v1/its";

    private Gson gson = new Gson();

    private String transUrl = "";
    private OkHttpClient okHttpClient;
    private MediaType mediaType;

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

    private String mFirstFrameStr = "";

    private long mSegmentId = 0;
    private boolean mSegmentEnd = true;
    private String mTranscribeResult = "";
    private String mTranslateResult = "";

    private Decoder decoder = new Decoder();

    private Param mParam;

    public IFlyTekWebAsrStt(String func) {
        isTrans = STT_FUNC_TRANSLATE.equals(func);

        okHttpClient = new OkHttpClient();
        mediaType = MediaType.parse("application/json");

        if (isTrans) {
            LanguageTag translateSource = SttConfigManager.getInstance().getSourceLanTag(
                    SttConfigManager.getInstance().getEngine(),
                    STT_FUNC_TRANSLATE
            );
            if (null != translateSource) {
                translateSourceKey = translateSource.getTag();

                if ("cn".equals(translateSourceKey)) {
                    transcribeSourceKey = "zh-cn";
                } else if ("en".equals(translateSourceKey)) {
                    transcribeSourceKey = "en-us";
                }

                LanguageTag translateTarget = SttConfigManager.getInstance().getTargetLanTag(
                        SttConfigManager.getInstance().getEngine(),
                        STT_FUNC_TRANSLATE,
                        translateSource.getTag()
                );
                if (null != translateTarget) {
                    translateTargetKey = translateTarget.getTag();
                }
            }
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
        return "IFlyTekWebAsr";
    }

    @Override
    public String getServiceId() {
        return "4F6D8A2C1E3B5D7F9A1B2C4D6E8F0A12";
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

        if (isTrans) {
            transUrl = getITransAuthUrl(param);
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
                        if (isTrans) translateText(mSegmentId, mTranscribeResult, STT_MSG_TYPE_VAR);
                        postResult(mSegmentId, STT_MSG_TYPE_VAR);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (2 == status) { // 数据全部返回完毕，可以关闭连接，释放资源
                LogUtil.i(resp.getSid() + " REC-> " + decoder.toString());

                mSegmentEnd = true;
                mTranscribeResult =  decoder.toString();
                LogUtil.i("recognized: " + mTranscribeResult);
                if (!TextUtils.isEmpty(mTranscribeResult)) {
                    mTranscribeMap.put(mSegmentId, mTranscribeResult);
                    if (isTrans) translateText(mSegmentId, mTranscribeResult, STT_MSG_TYPE_REC);
                    postResult(mSegmentId, STT_MSG_TYPE_REC);
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
        //business.addProperty("rlang", "zh-hk"); // zh-cn :简体中文（默认值）zh-hk :繁体香港(若未授权不生效，在控制台可免费开通)
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
        Request request = new Request.Builder()
                .url(transUrl)
                .post(RequestBody.create(mediaType, getITransReqParam(mParam, text, translateSourceKey, translateTargetKey)))
                //.addHeader("Ocp-Apim-Subscription-Key", BuildConfig.PROP_AZURE_TRANS_KEY)
                // region required if you're using a multi-service or regional (not global) resource.
                //.addHeader("Ocp-Apim-Subscription-Region", REGION)
                .addHeader("Content-type", "application/json")
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    String result = response.body().string();
                    ITransRsp iTransRsp = gson.fromJson(result, ITransRsp.class);
                    String textBase64Decode = new String(Base64.getDecoder().decode(iTransRsp.getPayload().getResult().getText()), "UTF-8");
                    ITransResult iTransResult = gson.fromJson(textBase64Decode, ITransResult.class);

                    mTranslateResult = iTransResult.getTrans_result().getDst();
                    mTranslateMap.put(segmentId, mTranslateResult);
                    postResult(segmentId, type);
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

    private String getITransAuthUrl(Param param) {
        URL url = null;
        // 替换调schema前缀 ，原因是URL库不支持解析包含ws,wss schema的url
        String httpRequestUrl = BASE_URL_TRANSLATE.replace("ws://", "http://").replace("wss://", "https://");
        try {
            url = new URL(httpRequestUrl);
            //获取当前日期并格式化
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = format.format(new Date());
            //String date="Thu, 18 Nov 2021 03:05:18 GMT";
            String host = url.getHost();
            /*
            if (url.getPort()!=80 && url.getPort() !=443){
                host = host +":"+String.valueOf(url.getPort());
            }
            */
            StringBuilder builder = new StringBuilder("host: ").append(host).append("\n").//
                    append("date: ").append(date).append("\n").//
                    append("POST ").append(url.getPath()).append(" HTTP/1.1");
            Charset charset = Charset.forName("UTF-8");
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(param.getAPISecret().getBytes(charset), "hmacsha256");
            mac.init(spec);
            byte[] hexDigits = mac.doFinal(builder.toString().getBytes(charset));
            String sha = Base64.getEncoder().encodeToString(hexDigits);
            //System.out.println(sha);
            String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", param.getAPIKey(), "hmac-sha256", "host date request-line", sha);
            String authBase = Base64.getEncoder().encodeToString(authorization.getBytes(charset));
            return String.format("%s?authorization=%s&host=%s&date=%s", BASE_URL_TRANSLATE, URLEncoder.encode(authBase), URLEncoder.encode(host), URLEncoder.encode(date));
        } catch (Exception e) {
            throw new RuntimeException("assemble requestUrl error:" + e.getMessage());
        }
    }

    private static final String RES_ID = "its_en_cn_word";
    private String getITransReqParam(Param param, String text, String from, String to) {
        ITransReq iTransReq = new ITransReq();

        ITransReq.HeaderBean headerBean = new ITransReq.HeaderBean();
        headerBean.setApp_id(param.getAPPID());
        headerBean.setStatus(3);
        headerBean.setRes_id(RES_ID);

        ITransReq.ParameterBean parameterBean = new ITransReq.ParameterBean();
        ITransReq.ParameterBean.ItsBean itsBean = new ITransReq.ParameterBean.ItsBean();
        itsBean.setFrom(from);
        itsBean.setTo(to);
        ITransReq.ParameterBean.ItsBean.ResultBean resultBean = new ITransReq.ParameterBean.ItsBean.ResultBean();
        itsBean.setResult(resultBean);
        parameterBean.setIts(itsBean);

        ITransReq.PayloadBean payloadBean = new ITransReq.PayloadBean();
        ITransReq.PayloadBean.InputDataBean inputDataBean = new ITransReq.PayloadBean.InputDataBean();
        inputDataBean.setEncoding("utf8");
        inputDataBean.setStatus(3);
        inputDataBean.setText(Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8)));
        payloadBean.setInput_data(inputDataBean);

        iTransReq.setHeader(headerBean);
        iTransReq.setParameter(parameterBean);
        iTransReq.setPayload(payloadBean);

        return gson.toJson(iTransReq);
    }
}
