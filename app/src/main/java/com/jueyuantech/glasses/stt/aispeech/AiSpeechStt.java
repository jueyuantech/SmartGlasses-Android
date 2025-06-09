package com.jueyuantech.glasses.stt.aispeech;

import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.jueyuantech.glasses.BuildConfig;
import com.jueyuantech.glasses.bean.LanguageTag;
import com.jueyuantech.glasses.stt.SttConfigManager;
import com.jueyuantech.glasses.stt.SttEngine;
import com.jueyuantech.glasses.stt.aispeech.bean.AudioBean;
import com.jueyuantech.glasses.stt.aispeech.bean.LasrBean;
import com.jueyuantech.glasses.stt.aispeech.bean.StartTransCmd;
import com.jueyuantech.glasses.util.LogUtil;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class AiSpeechStt extends SttEngine {

    private boolean DEBUG_TRANS = false;
    private String iTransVar = "";

    private static final String HOST = "";
    private static final String HOST_PLUS = "";
    private static final String PROTOCOL = "";

    private String targetUrl = "";
    private String startCmd = "";

    private OkHttpClient mSocketClient;
    private Request mSocketRequest;
    private WebSocket mSocket;

    private boolean mSpeakerRecognition = false;

    private byte[] mEndBytes;

    private Gson gson = new Gson();

    private boolean isTrans = false;
    private String transcribeSourceKey = "";
    private String translateSourceKey = "";
    private String translateTargetKey = "";

    private long segmentId = 0;

    public AiSpeechStt(String func) {
        isTrans = STT_FUNC_TRANSLATE.equals(func);

        if (isTrans) {
            LanguageTag translateSource = SttConfigManager.getInstance().getSourceLanTag(
                    SttConfigManager.getInstance().getEngine(),
                    STT_FUNC_TRANSLATE
            );
            if (null != translateSource) {
                translateSourceKey = translateSource.getTag();

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

        mEndBytes = new byte[]{};
    }

    @Override
    public String getName() {
        return "AiSpeech";
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

        if (isTrans) {
            targetUrl = PROTOCOL + HOST_PLUS + getTransHandShakeParams(param.getProductId(), param.getSecretKey());
            startCmd = getTransCmd(translateSourceKey, translateTargetKey);
        } else {
            targetUrl = PROTOCOL + HOST + getHandShakeParams(param.getProductId(), param.getSecretKey());
            startCmd = getLasrCmd(transcribeSourceKey);
        }
        mSocketClient = new OkHttpClient();
        mSocketRequest = new Request.Builder().url(targetUrl).build();
    }

    @Override
    public void connect() {
        openSocket();
    }

    @Override
    public void disconnect() {
        mSocket.send(ByteString.of(mEndBytes));
        closeSocket();
    }

    @Override
    public boolean shouldRetry(int errCode) {
        return true;
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

    private long dataBg = -1l;
    private WebSocketListener mWebSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            LogUtil.i("webSocket onOpen");

            iTransVar = "";
            webSocket.send(startCmd);

            if (null != mOnSttListener) {
                mOnSttListener.onConnect("");
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String msg) {
            LogUtil.i("webSocket onMessage = " + msg);

            if (isTrans) {
                processTrans(webSocket, msg);
            } else {
                processLasr(webSocket, msg);
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            LogUtil.i("webSocket onClosing");
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            LogUtil.i("webSocket onFailure");
            if (null != mOnSttListener) {
                mOnSttListener.onError(-1, "onFailure");
            }
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
            LogUtil.i("webSocket ByteString onMessage");
            super.onMessage(webSocket, bytes);
        }
    };

    private String getHandShakeParams(String productId, String apiKey) {
        return "?productId=" + productId + "&apikey=" + apiKey + "&res=comm" + "&language=zh-CN";
    }

    private String getTransHandShakeParams(String productId, String apiKey) {
        //return "?productId=" + productId + "&apiKey=" + apiKey + "&res=comm" + "&language=zh-CN" + "&enableItrans=true";
        return "?productId=" + productId + "&apiKey=" + apiKey + "&res=aitranson" + "&lang=cn" + "&requestId=1" + "&enableItrans=true";
    }

    /**
     * @param productId
     * @param apiKey
     * @return
     */
    private String getVprHandShakeParams(String productId, String apiKey) {
        return "?productId=" + productId + "&apiKey=" + apiKey + "&res=aitranson" + "&lang=cn" + "&requestId=1" + "&enableSsc=true";
    }

    private void processLasr(WebSocket webSocket, String msg) {
        int errno = JSONObject.parseObject(msg).getInteger("errno");
        JSONObject data = JSONObject.parseObject(msg).getJSONObject("data");
        if (0 == errno) {
            // processRec 最终结果
            String msgStr = data.getString("onebest");
            if (!TextUtils.isEmpty(msgStr)) {
                if (null != mOnSttListener) {
                    mOnSttListener.onMessage(segmentId, "", STT_MSG_TYPE_REC, msgStr, "");
                }

                segmentId = System.currentTimeMillis();
            }
        } else if (7 == errno) {
            //extracted(webSocket);
        } else if (8 == errno) {
            // processVar 中间过程
            String msgStr = data.getString("var");
            if (null != mOnSttListener) {
                mOnSttListener.onMessage(segmentId, "", STT_MSG_TYPE_VAR, msgStr, "");
            }
        } else if (9 == errno) {
            webSocket.close(1000, "success");
            //future.complete(JSONObject.parseObject(msg).getJSONObject("data").getString("onebest"));
        }
    }

    private String asrRecStr = "";
    private String asrRecTempStr = "";
    private String transResultStr = "";
    private boolean transEnd = false;

    private void processTrans(WebSocket webSocket, String msg) {
        int errno = JSONObject.parseObject(msg).getInteger("errno");
        if (0 == errno) {
            transEnd = false;
            JSONObject transResponse = JSONObject.parseObject(msg).getJSONObject("itransResponse");
            JSONObject lasrResponse = JSONObject.parseObject(msg).getJSONObject("lasrResponse");
            if (null != lasrResponse) {
                JSONObject data = lasrResponse.getJSONObject("data");
                String rec = data.getString("rec");
                if (null == rec) rec = "";
                String var = data.getString("var");
                if (null == var) var = "";
                asrRecTempStr += rec;
                asrRecStr = asrRecTempStr + var;
            } else if (null != transResponse) {
                JSONObject data = transResponse.getJSONObject("data");
                JSONObject result = data.getJSONObject("result");
                if (null == result) return;
                if (null == result.getInteger("end")) {
                    String transVar = result.getString("transVar");
                    if (DEBUG_TRANS) LogUtil.i("TRANSLATE VAR #### " + transVar);
                    transResultStr += transVar;

                } else {
                    String rec = result.getString("rec");
                    String transRec = result.getString("transRec");
                    asrRecStr = rec;
                    transResultStr = transRec;
                    if (DEBUG_TRANS) LogUtil.i("TRANSLATE REC =============" + transRec);

                    transEnd = true;
                }
            }

            if (transEnd) {
                if (null != mOnSttListener) {
                    mOnSttListener.onMessage(segmentId, "", STT_MSG_TYPE_REC, asrRecStr, transResultStr);
                }

                asrRecStr = "";
                asrRecTempStr = "";
                transResultStr = "";

                segmentId = System.currentTimeMillis();
            } else {
                if (TextUtils.isEmpty(asrRecStr) && TextUtils.isEmpty(transResultStr)) {

                } else {
                    if (null != mOnSttListener) {
                        mOnSttListener.onMessage(segmentId, "", STT_MSG_TYPE_VAR, asrRecStr, transResultStr);
                    }
                }
            }
        } else if (7 == errno) {
            //extracted(webSocket);
        } else if (8 == errno) {
            //processVar(msg);
        } else if (9 == errno) {
            webSocket.close(1000, "success");
            //future.complete(JSONObject.parseObject(msg).getJSONObject("data").getString("onebest"));
        }
    }

    private String getLasrCmd(String sourceLan) {
        LasrBean mLasrBean = new LasrBean();
        mLasrBean.setCommand("start");
        LasrBean.ParamsBean paramsBean = new LasrBean.ParamsBean();
        LasrBean.ParamsBean.EnvBean envBean = new LasrBean.ParamsBean.EnvBean();
        envBean.setUse_txt_smooth(1);
        envBean.setUse_tprocess(1);
        envBean.setUse_sensitive_wds_norm(0);
        envBean.setUse_alignment(1);
        envBean.setUse_aux(1);
        paramsBean.setEnv(envBean);
        AudioBean audioBean = new AudioBean();
        audioBean.setAudioType("wav");
        audioBean.setSampleRate(16000);
        audioBean.setSampleBytes(2);
        audioBean.setChannel(1);
        paramsBean.setAudio(audioBean);
        mLasrBean.setParams(paramsBean);
        mLasrBean.setLmId("default");
        mLasrBean.setPhraseFileId("");
        mLasrBean.setSensitiveFileId("");

        return gson.toJson(mLasrBean);
    }

    private String getTransCmd(String sourceLan, String targetLan) {
        StartTransCmd.VprBean vprBean = new StartTransCmd.VprBean();

        StartTransCmd.VprBean.AsrPlusBean asrPlusBean = new StartTransCmd.VprBean.AsrPlusBean();
        asrPlusBean.setDomain("comm");

        AudioBean audioBean = new AudioBean();
        audioBean.setAudioType("wav");
        audioBean.setSampleRate(16000);
        audioBean.setSampleBytes(2);
        audioBean.setChannel(1);

        StartTransCmd.VprBean.EnvBean vprEnvBean = new StartTransCmd.VprBean.EnvBean();
        vprEnvBean.setDebug(true);
        vprEnvBean.setEnableVad(false);
        vprEnvBean.setIsDependRecWord(true);
        vprEnvBean.setAsrErrorRate(50.0);
        vprEnvBean.setMinSpeechlength(0.4);

        vprBean.setAsrPlus(asrPlusBean);
        vprBean.setAudio(audioBean);
        vprBean.setEnv(vprEnvBean);
        vprBean.setRequestId("h5iujav06kn0cpuagxuvb1ha2000oa3d0");
        vprBean.setAction("mock_vpr_user");

        LasrBean lasrBean = new LasrBean();
        LasrBean.ParamsBean paramsBean = new LasrBean.ParamsBean();
        LasrBean.ParamsBean.EnvBean lasrEnvBean = new LasrBean.ParamsBean.EnvBean();
        lasrEnvBean.setUse_alignment(1);
        lasrEnvBean.setUse_stream(1);
        lasrEnvBean.setUse_stream_rec_words(1);
        lasrEnvBean.setUse_stream_sp(0);
        lasrEnvBean.setUse_txtpost(1);
        lasrEnvBean.setUse_wp_in_rec(1);
        lasrEnvBean.setUse_txt_smooth(1);
        paramsBean.setAudio(audioBean);
        paramsBean.setEnv(lasrEnvBean);
        lasrBean.setCommand("start");
        lasrBean.setParams(paramsBean);

        StartTransCmd.ItransBean itransBean = new StartTransCmd.ItransBean();
        StartTransCmd.ItransBean.ItsBean itsBean = new StartTransCmd.ItransBean.ItsBean();
        itsBean.setUseNer(0);
        itsBean.setDomain("comm");
        itsBean.setFrom(sourceLan); // "Chinese"
        itsBean.setTo(targetLan); // "English"
        itransBean.setIts(itsBean);
        itransBean.setTopic("itrans.start");

        StartTransCmd startTransCmd = new StartTransCmd();
        startTransCmd.setVpr(vprBean);
        startTransCmd.setLasr(lasrBean);
        startTransCmd.setItrans(itransBean);
        return gson.toJson(startTransCmd);
    }

    class Param {
        String productId;
        String secretKey;

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
    }
}
