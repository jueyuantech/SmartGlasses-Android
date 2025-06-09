package com.jueyuantech.glasses.stt.azure;

import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jueyuantech.glasses.bean.LanguageTag;
import com.jueyuantech.glasses.stt.SttConfigManager;
import com.jueyuantech.glasses.stt.SttEngine;
import com.jueyuantech.glasses.util.LogUtil;
import com.microsoft.cognitiveservices.speech.CancellationReason;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.translation.SpeechTranslationConfig;
import com.microsoft.cognitiveservices.speech.translation.TranslationRecognizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AzureStt extends SttEngine {
    // location, also known as region.
    // required if you're using a multi-service or regional (not global) resource. It can be found in the Azure portal on the Keys and Endpoint page.
    private static final boolean DEBUG = false;

    private Gson gson = new Gson();

    private AudioConfig audioConfig;

    private SpeechConfig speechConfig;
    private SpeechRecognizer recognizer;

    private SpeechTranslationConfig speechTranslationConfig;
    private TranslationRecognizer translationRecognizer;

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

    public AzureStt(Context context, String func) {
        mContext = context;

        isTrans = STT_FUNC_TRANSLATE.equals(func);

        if (isTrans) {
            LanguageTag translateSource = SttConfigManager.getInstance().getSourceLanTag(
                    SttConfigManager.getInstance().getEngine(),
                    STT_FUNC_TRANSLATE
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
        return "Azure";
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
            speechTranslationConfig = SpeechTranslationConfig.fromSubscription(param.getKey(), param.getRegion());
            speechTranslationConfig.setSpeechRecognitionLanguage(translateSourceKey);
            speechTranslationConfig.addTargetLanguage(translateTargetKey);
            audioConfig = AudioConfig.fromDefaultMicrophoneInput();
            translationRecognizer = new TranslationRecognizer(speechTranslationConfig, audioConfig);
        } else {
            speechConfig = SpeechConfig.fromSubscription(param.getKey(), param.getRegion());
            speechConfig.setSpeechRecognitionLanguage(transcribeSourceKey); // 设置识别语言 "en-US" "zh-CN"
            audioConfig = AudioConfig.fromDefaultMicrophoneInput();
            recognizer = new SpeechRecognizer(speechConfig, audioConfig);
        }
    }

    @Override
    public void connect() {
        //openFile();
        if (isTrans) {
            startTranslate();
        } else {
            startTranscribe();
        }
    }

    @Override
    public void disconnect() {
        if (isTrans) {
            stopTranslate();
        } else {
            stopTranscribe();
        }
        //closeFile();
    }

    @Override
    public boolean shouldRetry(int errCode) {
        return true;
    }

    @Override
    public void send(byte[] data) {

    }

    private void startTranscribe() {
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
                LogUtil.i("recognizing : NEW SegmentId " + mSegmentId);
            }

            mTranscribeResult = e.getResult().getText();
            LogUtil.i("recognizing: " + mTranscribeResult);
            if (!TextUtils.isEmpty(mTranscribeResult)) {
                mTranscribeMap.put(mSegmentId, mTranscribeResult);
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
                if (DEBUG)
                    LogUtil.i("CANCELED: Did you set the speech resource key and region values?");

                if (null != mOnSttListener) {
                    mOnSttListener.onError(e.getErrorCode().getValue(), e.getErrorDetails());
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

    private void stopTranscribe() {
        try {
            if (null != recognizer) {
                recognizer.stopContinuousRecognitionAsync().get();
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void startTranslate() {
        mTranscribeMap.clear();
        mTranslateMap.clear();

        translationRecognizer.sessionStarted.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("sessionStarted : s " + s.toString());
            if (DEBUG) LogUtil.i("sessionStarted : e " + e.toString());

            if (null != mOnSttListener) {
                mOnSttListener.onConnect("");
            }
        });

        translationRecognizer.sessionStopped.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("sessionStopped : s " + s.toString());
            if (DEBUG) LogUtil.i("sessionStopped : e " + e.toString());
        });

        translationRecognizer.speechStartDetected.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("speechStartDetected : s " + s.toString());
            if (DEBUG) LogUtil.i("speechStartDetected : e " + e.toString());
        });

        translationRecognizer.speechEndDetected.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("speechEndDetected : s " + s.toString());
            if (DEBUG) LogUtil.i("speechEndDetected : e " + e.toString());
        });

        translationRecognizer.recognizing.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("recognizing : s " + s.toString());
            if (DEBUG) LogUtil.i("recognizing : e " + e.toString());

            if (mSegmentEnd) {
                mSegmentEnd = false;
                mSegmentId = System.currentTimeMillis();
                mTranscribeMap.put(mSegmentId, "");
                mTranslateMap.put(mSegmentId, "");
                LogUtil.i("recognizing : NEW SegmentId " + mSegmentId);
            }

            mTranscribeResult = e.getResult().getText();
            mTranslateResult = e.getResult().getTranslations().get(translateTargetKey);
            if (DEBUG) LogUtil.i("recognizing: transcribe=> " + mTranscribeResult);
            if (DEBUG) LogUtil.i("recognizing: translate=> " + mTranslateResult);
            if (!TextUtils.isEmpty(mTranscribeResult)) {
                mTranscribeMap.put(mSegmentId, mTranscribeResult);
                mTranslateMap.put(mSegmentId, mTranslateResult);
                postResult(mSegmentId, STT_MSG_TYPE_VAR);
            }
        });

        translationRecognizer.recognized.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("recognized : s " + s.toString());
            if (DEBUG) LogUtil.i("recognized : e " + e.toString());
            if (DEBUG) LogUtil.i("recognized : e Reason=" + e.getResult().getReason());

            // FIXME 放在if里面？
            mSegmentEnd = true;

            if (e.getResult().getReason() == ResultReason.TranslatedSpeech) {
                mTranscribeResult = e.getResult().getText();
                mTranslateResult = e.getResult().getTranslations().get(translateTargetKey);
                if (DEBUG) LogUtil.i("recognized: transcribe=> " + mTranscribeResult);
                if (DEBUG) LogUtil.i("recognized: translate=> " + mTranslateResult);
                if (!TextUtils.isEmpty(mTranscribeResult)) {
                    mTranscribeMap.put(mSegmentId, mTranscribeResult);
                    mTranslateMap.put(mSegmentId, mTranslateResult);
                    postResult(mSegmentId, STT_MSG_TYPE_REC);
                }
            } else if (e.getResult().getReason() == ResultReason.NoMatch) {
                if (DEBUG) LogUtil.i("recognized : NoMatch ");
            }
        });

        translationRecognizer.canceled.addEventListener((s, e) -> {
            if (DEBUG) LogUtil.i("canceled : s " + s.toString());
            if (DEBUG) LogUtil.i("canceled : e " + e.toString());
            if (DEBUG) LogUtil.i("canceled : e Reason=" + e.getReason());

            if (e.getReason() == CancellationReason.Error) {
                if (DEBUG) LogUtil.i("CANCELED: ErrorCode=" + e.getErrorCode());
                if (DEBUG) LogUtil.i("CANCELED: ErrorDetails=" + e.getErrorDetails());
                if (DEBUG)
                    LogUtil.i("CANCELED: Did you set the speech resource key and region values?");

                if (null != mOnSttListener) {
                    mOnSttListener.onError(e.getErrorCode().getValue(), e.getErrorDetails());
                }
            } else if (e.getReason() == CancellationReason.EndOfStream) {
                if (DEBUG) LogUtil.i("CANCELED: EndOfStream");
            } else if (e.getReason() == CancellationReason.CancelledByUser) {
                if (DEBUG) LogUtil.i("CANCELED: CancelledByUser");
            }
        });

        try {
            translationRecognizer.startContinuousRecognitionAsync().get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopTranslate() {
        try {
            if (null != translationRecognizer) {
                translationRecognizer.stopContinuousRecognitionAsync().get();
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String prettify(String json_text) {
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(json_text);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
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

    class Param {
        String key;
        String region;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }
    }
}
