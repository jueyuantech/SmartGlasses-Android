package com.jueyuantech.glasses.stt.azure;

import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_TRANSLATE;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jueyuantech.glasses.BuildConfig;
import com.jueyuantech.glasses.bean.AzureTransReq;
import com.jueyuantech.glasses.bean.AzureTransResult;
import com.jueyuantech.glasses.bean.LanguageTag;
import com.jueyuantech.glasses.stt.AsrConfigManager;
import com.jueyuantech.glasses.stt.SttManager;
import com.jueyuantech.glasses.util.LogUtil;
import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AzureAsr extends SttManager {
    // location, also known as region.
    // required if you're using a multi-service or regional (not global) resource. It can be found in the Azure portal on the Keys and Endpoint page.
    private static final boolean DEBUG = false;
    private static String REGION = "eastasia"; // 例如 "westus"
    private String TRANS_ENDPOINT = "https://api.cognitive.microsofttranslator.com";
    private String TRANS_ROUTE = "/translate?api-version=3.0&from=%s&to=%s";
    private String TRANS_URL = TRANS_ENDPOINT.concat(TRANS_ROUTE);

    private Gson gson = new Gson();
    private OkHttpClient okHttpClient;
    private MediaType mediaType;

    private SpeechConfig speechConfig;
    private AudioConfig audioConfig;
    private SpeechRecognizer recognizer;

    private AzureTransReq transReq = new AzureTransReq();
    private List<AzureTransReq> transReqList = new ArrayList<>();

    private long mSegmentId = 0;
    private boolean mSegmentEnd = true;
    private String mTranscribeResult = "";
    private String mTranslateResult = "";

    private Map<Long, String> mTranscribeMap = new HashMap<>();
    private Map<Long, String> mTranslateMap = new HashMap<>();

    private boolean isTrans = false;
    private String transcribeSourceKey = "";
    private String translateSourceKey = "";
    private String translateTargetKey = "";

    private Context mContext;
    public AzureAsr(Context context, String func) {
        mContext = context;

        isTrans = ASR_FUNC_TRANSLATE.equals(func);

        okHttpClient = new OkHttpClient();
        mediaType = MediaType.parse("application/json");

        if (isTrans) {
            LanguageTag translateSource = AsrConfigManager.getInstance().getSourceLanTag(
                    AsrConfigManager.getInstance().getEngine(),
                    ASR_FUNC_TRANSLATE
            );
            if (null != translateSource) {
                translateSourceKey = translateSource.getTag();

                if ("zh-Hans".equals(translateSourceKey)) {
                    transcribeSourceKey = "zh-CN";
                } else if ("en".equals(translateSourceKey)) {
                    transcribeSourceKey = "en-US";
                } else if ("ja".equals(translateSourceKey)) {
                    transcribeSourceKey = "ja-JP";
                }

                LanguageTag translateTarget = AsrConfigManager.getInstance().getTargetLanTag(
                        AsrConfigManager.getInstance().getEngine(),
                        ASR_FUNC_TRANSLATE,
                        translateSource.getTag()
                );
                if (null != translateTarget) {
                    translateTargetKey = translateTarget.getTag();
                }
            }
            TRANS_URL = String.format(TRANS_ENDPOINT.concat(TRANS_ROUTE), translateSourceKey, translateTargetKey);
        } else {
            LanguageTag transcribeSource = AsrConfigManager.getInstance().getSourceLanTag(
                    AsrConfigManager.getInstance().getEngine(),
                    ASR_FUNC_TRANSCRIBE
            );
            if (null != transcribeSource) {
                transcribeSourceKey = transcribeSource.getTag();
            }
        }

        speechConfig = SpeechConfig.fromSubscription(BuildConfig.PROP_AZURE_ASR_KEY, REGION);
        speechConfig.setSpeechRecognitionLanguage(transcribeSourceKey); // 设置识别语言 "en-US" "zh-CN"
        audioConfig = AudioConfig.fromDefaultMicrophoneInput();
        recognizer = new SpeechRecognizer(speechConfig, audioConfig);
    }

    @Override
    public void connect() {
        //openFile();
        startAsr();
    }

    @Override
    public void disconnect() {
        stopAsr();
        //closeFile();
    }

    @Override
    public void send(byte[] data) {

    }

    private void startAsr() {
        mTranscribeMap.clear();
        mTranslateMap.clear();

        recognizer.sessionStarted.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("sessionStarted : s " + s.toString());
            if (DEBUG) LogUtil.i("sessionStarted : e " + e.toString());

            if (null != mOnSttListener) {
                mOnSttListener.onConnect("");
            }
        });

        recognizer.sessionStopped.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("sessionStopped : s " + s.toString());
            if (DEBUG) LogUtil.i("sessionStopped : e " + e.toString());
        });

        recognizer.speechStartDetected.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("speechStartDetected : s " + s.toString());
            if (DEBUG) LogUtil.i("speechStartDetected : e " + e.toString());
        });

        recognizer.speechEndDetected.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("speechEndDetected : s " + s.toString());
            if (DEBUG) LogUtil.i("speechEndDetected : e " + e.toString());
        });

        recognizer.recognizing.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("recognizing : s " + s.toString());
            if (DEBUG) LogUtil.i("recognizing : e " + e.toString());

            if (mSegmentEnd) {
                mSegmentEnd = false;
                mSegmentId = System.currentTimeMillis();
                mTranscribeMap.put(mSegmentId, "");
                mTranslateMap.put(mSegmentId, "");
                LogUtil.i("recognizing : NEW SegmentId " + mSegmentEnd);
            }

            mTranscribeResult = e.getResult().getText();
            LogUtil.i("recognizing: " + mTranscribeResult);
            if (!TextUtils.isEmpty(mTranscribeResult)) {
                mTranscribeMap.put(mSegmentId, mTranscribeResult);
                if (isTrans) translate(mSegmentId, mTranscribeResult, STT_MSG_TYPE_VAR);
                postResult(mSegmentId, STT_MSG_TYPE_VAR);
            }
        });

        recognizer.recognized.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("recognized : s " + s.toString());
            if (DEBUG) LogUtil.i("recognized : e " + e.toString());

            // FIXME 放在if里面？
            mSegmentEnd = true;

            if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                mTranscribeResult = e.getResult().getText();
                LogUtil.i("recognized: " + mTranscribeResult);
                if (!TextUtils.isEmpty(mTranscribeResult)) {
                    mTranscribeMap.put(mSegmentId, mTranscribeResult);
                    if (isTrans) translate(mSegmentId, mTranscribeResult, STT_MSG_TYPE_REC);
                    postResult(mSegmentId, STT_MSG_TYPE_REC);
                }
            }
        });

        recognizer.canceled.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("canceled : s " + s.toString());
            if (DEBUG) LogUtil.i("canceled : e " + e.toString());
            if (DEBUG) LogUtil.i("canceled : e Reason=" + e.getReason());

            if (e.getReason() == CancellationReason.Error) {
                if (DEBUG) LogUtil.i("CANCELED: ErrorCode=" + e.getErrorCode());
                if (DEBUG) LogUtil.i("CANCELED: ErrorDetails=" + e.getErrorDetails());
                if (DEBUG) LogUtil.i("CANCELED: Did you set the speech resource key and region values?");

                if (null != mOnSttListener) {
                    mOnSttListener.onError(e.getErrorDetails());
                }
            } else if (e.getReason() == CancellationReason.EndOfStream) {
                if (DEBUG) LogUtil.i("CANCELED: EndOfStream");
            } else if (e.getReason() == CancellationReason.CancelledByUser) {
                if (DEBUG) LogUtil.i("CANCELED: CancelledByUser");
            }
        });

        try {
            recognizer.startContinuousRecognitionAsync().get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopAsr() {
        try {
            recognizer.stopContinuousRecognitionAsync().get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void translate(long segmentId, String text, int type) {
        transReq = new AzureTransReq();
        transReq.setText(text);
        transReqList.clear();
        transReqList.add(transReq);

        Request request = new Request.Builder()
                .url(TRANS_URL)
                .post(RequestBody.create(mediaType, gson.toJson(transReqList)))
                .addHeader("Ocp-Apim-Subscription-Key", BuildConfig.PROP_AZURE_TRANS_KEY)
                // region required if you're using a multi-service or regional (not global) resource.
                .addHeader("Ocp-Apim-Subscription-Region", REGION)
                .addHeader("Content-type", "application/json")
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    String result = response.body().string();
                    AzureTransResult[] azureTransResults = gson.fromJson(result, AzureTransResult[].class);
                    if (null != azureTransResults && azureTransResults.length > 0) {
                        List<AzureTransResult.TranslationsBean> translations = azureTransResults[0].getTranslations();
                        if (null != translations && translations.size() > 0) {
                            mTranslateResult = translations.get(0).getText();
                            mTranslateMap.put(segmentId, mTranslateResult);
                            postResult(segmentId, type);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static String prettify(String json_text) {
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(json_text);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

    private void postResult(long segmentId, int type) {
        LogUtil.i("segmentId " + segmentId + " scribe " + mTranscribeMap.get(segmentId) + " trans " + mTranslateMap.get(segmentId));
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

    private BufferedWriter mockFileWriter;
    public void openFile() {
        File ftpFilesDir = mContext.getExternalFilesDir("VenusMock");
        if (!ftpFilesDir.exists()) {
            ftpFilesDir.mkdir();
        }

        File transMockFile = new File(ftpFilesDir, "TransMock");
        transMockFile.deleteOnExit();
        try {
            transMockFile.createNewFile();
            mockFileWriter = new BufferedWriter(new FileWriter(transMockFile, true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeFileLine(String lineContent) {
        try {
            mockFileWriter.write(lineContent);
            mockFileWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeFile() {
        try {
            if (mockFileWriter != null) {
                mockFileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
