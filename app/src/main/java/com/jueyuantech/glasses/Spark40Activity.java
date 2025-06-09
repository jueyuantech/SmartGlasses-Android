package com.jueyuantech.glasses;

import static com.jueyuantech.glasses.RecorderCallback.MSG_TYPE_APPEND;
import static com.jueyuantech.glasses.RecorderCallback.MSG_TYPE_NEW;
import static com.jueyuantech.glasses.RecorderCallback.MSG_TYPE_UPDATE;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jueyuantech.glasses.adapter.ChatAdapter;
import com.jueyuantech.glasses.bean.ChatMessageBean;
import com.jueyuantech.glasses.util.JsonParser;
import com.jueyuantech.glasses.util.LogUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.sparkchain.core.LLM;
import com.iflytek.sparkchain.core.LLMCallbacks;
import com.iflytek.sparkchain.core.LLMConfig;
import com.iflytek.sparkchain.core.LLMError;
import com.iflytek.sparkchain.core.LLMEvent;
import com.iflytek.sparkchain.core.LLMResult;
import com.iflytek.sparkchain.core.Memory;
import com.iflytek.sparkchain.core.SparkChain;
import com.iflytek.sparkchain.core.SparkChainConfig;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.bean.VNSttInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

public class Spark40Activity extends VenusAppBaseActivity implements View.OnClickListener, View.OnTouchListener {

    private Random random = new Random();
    private ImageView mBackIv;
    private TextView mTitleTv;
    private ImageButton mStartRecordBtn;
    private RecyclerView mChatRcv;

    private ChatMessageBean mCurrentMessage;
    private ChatAdapter mChatAdapter;
    private List<ChatMessageBean> messageBeen = new ArrayList<>();
    private int scrollState = 0;

    private TTSHelper ttsHelper;

    private boolean sessionFinished = true;
    private LLM llm;
    private String llmTokenLimitPrompt = "（将回复限制在80字以内，并用对应语言回复）";

    // 听写
    private boolean recognizeFinished = true;
    private SpeechRecognizer mIat;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private String language = "zh_cn";
    private String resultType = "json";
    private StringBuffer buffer = new StringBuffer();
    int ret = 0; // 函数调用返回值

    private Dialog mRecordingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spark40);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mRecordingDialog = new Dialog(this);
        mRecordingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mRecordingDialog.setContentView(R.layout.dialog_recording);
        mRecordingDialog.setCancelable(false);
        mRecordingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mRecordingDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);
        mTitleTv = findViewById(R.id.tv_title);
        mTitleTv.setText(R.string.func_ai);

        mChatAdapter = new ChatAdapter(this, messageBeen);
        mChatAdapter.setHasStableIds(true);

        mChatRcv = findViewById(R.id.rv_chat);
        mChatRcv.getItemAnimator().setChangeDuration(0);
        mChatRcv.setLayoutManager(new LinearLayoutManager(this));
        mChatRcv.setAdapter(mChatAdapter);
        mChatRcv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                scrollState = newState;
            }
        });

        mStartRecordBtn = findViewById(R.id.ib_record);
        mStartRecordBtn.setOnClickListener(this);
        mStartRecordBtn.setOnTouchListener(this);

        ttsHelper = new TTSHelper(this);

        initSDK();

        MyApplication.initializeMsc(this, BuildConfig.PROP_SPARK40_APP_ID);
        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_record:
                if (recognizeFinished && sessionFinished) {
                    startRecognize();
                }
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                break;
            case MotionEvent.ACTION_UP:
                //view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY_RELEASE);
                break;
        }
        return false;
    }

    @Override
    protected void onPause() {
        stopRecognize();
        cancelRecognize();
        dismissRecordingDialog();
        recognizeFinished = true;

        if (null != ttsHelper) {
            ttsHelper.stop();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unInitSDK();
    }

    @Override
    protected void notifyVenusEnter() {
        VNCommon.setView(VNConstant.View.TRANSCRIBE, null);
    }

    @Override
    protected void notifyVenusExit() {
        VNCommon.setView(VNConstant.View.HOME, null);
    }

    @Override
    protected void notifyExitFromVenus() {
        finish();
    }

    private void showRecordingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != mRecordingDialog && (!mRecordingDialog.isShowing())) {
                    mRecordingDialog.show();
                }
            }
        });
    }

    private void dismissRecordingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (null != mRecordingDialog && mRecordingDialog.isShowing()) {
                    mRecordingDialog.dismiss();
                }
            }
        });
    }

    private void sendText(int type, String transcribeStr, String translateStr) {
        VNSttInfo sttInfo = new VNSttInfo();
        sttInfo.setTranscribe(transcribeStr);
        sttInfo.setTranslate(translateStr);
        sttInfo.setActionType(type);
        sttInfo.setMsgType(VNConstant.SttInfo.MsgType.STT);
        VNCommon.updateTranscribe(sttInfo, null);
    }

    private void initSDK() {
        // 初始化SDK，Appid等信息在清单中配置
        SparkChainConfig sparkChainConfig = SparkChainConfig.builder();
        sparkChainConfig.appID(BuildConfig.PROP_SPARK40_APP_ID)
                .apiSecret(BuildConfig.PROP_SPARK40_API_SECRET)
                .apiKey(BuildConfig.PROP_SPARK40_API_KEY)//应用申请的appid三元组
                .logLevel(0);

        int ret = SparkChain.getInst().init(getApplicationContext(), sparkChainConfig);
        if (ret == 0) {
            LogUtil.d("SDK初始化成功：" + ret);
            showSystemMessage("服务已开启");
            setLLMConfig();
        } else {
            LogUtil.d("SDK初始化失败：其他错误:" + ret);
            showSystemMessage("服务开启失败：" + ret);
        }
    }

    private void setLLMConfig() {
        LogUtil.d("setLLMConfig");
        LLMConfig llmConfig = LLMConfig.builder();
        llmConfig.domain("4.0Ultra");
        llmConfig.url("ws(s)://spark-api.xf-yun.com/v4.0/chat");//必填
        llmConfig.maxToken(150);
        //memory有两种，windows_memory和tokens_memory，二选一即可
        Memory window_memory = Memory.windowMemory(2);
        llm = new LLM(llmConfig, window_memory);

        //Memory tokens_memory = Memory.tokenMemory(8192);
        //llm = new LLM(llmConfig,tokens_memory);

        llm.registerLLMCallbacks(llmCallbacks);
    }

    private void unInitSDK() {
        SparkChain.getInst().unInit();
    }

    private void startChat(String usrInputText) {
        if (llm == null) {
            LogUtil.e("startChat failed,please setLLMConfig before!");
            return;
        }
        LogUtil.d("用户输入：" + usrInputText);
        if (TextUtils.isEmpty(usrInputText)) {
            return;
        }

        //usrInputText += llmTokenLimitPrompt;

        String myContext = "myContext";

        int ret = llm.arun(usrInputText, myContext);
        if (ret != 0) {
            LogUtil.e("SparkChain failed:\n" + ret);
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onMessageRcv(MSG_TYPE_NEW, "");
            }
        });

        sessionFinished = false;
        return;
    }

    private void onMessageRcv(int type, String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MSG_TYPE_NEW == type) {
                    mCurrentMessage = new ChatMessageBean(ChatMessageBean.TYPE_RECEIVED, getString(R.string.app_name), "", msg);
                    mChatAdapter.addData(mCurrentMessage);

                    sendText(MSG_TYPE_NEW, msg, "");
                } else if (MSG_TYPE_UPDATE == type) {
                    if (null != mCurrentMessage) {
                        mCurrentMessage.content = msg;
                        mCurrentMessage.isHint = false;
                        mChatAdapter.updateData();

                        sendText(MSG_TYPE_UPDATE, mCurrentMessage.content, "");
                    }
                } else if (MSG_TYPE_APPEND == type) {
                    if (null != mCurrentMessage) {
                        mCurrentMessage.content = mCurrentMessage.content + msg;
                        mCurrentMessage.isHint = false;
                        mChatAdapter.updateData();

                        sendText(MSG_TYPE_UPDATE, mCurrentMessage.content, "");
                    }
                }

                mChatRcv.scrollToPosition(messageBeen.size() - 1);
            }
        });
    }

    private void showSystemMessage(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCurrentMessage = new ChatMessageBean(ChatMessageBean.TYPE_SYSTEM, getString(R.string.app_name), "", msg);
                mChatAdapter.addData(mCurrentMessage);
                mChatRcv.scrollToPosition(messageBeen.size() - 1);
            }
        });
    }

    private void showRecognizeResult(int type, String msg) {
        sendText(type, msg, "");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MSG_TYPE_NEW == type) {
                    mCurrentMessage = new ChatMessageBean(ChatMessageBean.TYPE_SEND, getString(R.string.app_name), "", msg);
                    mChatAdapter.addData(mCurrentMessage);
                } else if (MSG_TYPE_UPDATE == type) {
                    if (null != mCurrentMessage) {
                        mCurrentMessage.content = msg;
                        mCurrentMessage.isHint = false;
                        mChatAdapter.updateData();
                    }
                }

                mChatRcv.scrollToPosition(messageBeen.size() - 1);
            }
        });
    }

    LLMCallbacks llmCallbacks = new LLMCallbacks() {
        @Override
        public void onLLMResult(LLMResult llmResult, Object usrContext) {
            LogUtil.d("onLLMResult\n");
            String content = llmResult.getContent();
            LogUtil.e("onLLMResult:" + content);
            int status = llmResult.getStatus();
            if (content != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onMessageRcv(MSG_TYPE_APPEND, content);
                    }
                });
            }
            if (usrContext != null) {
                String context = (String) usrContext;
                LogUtil.d("context:" + context);
            }
            if (status == 2) {
                int completionTokens = llmResult.getCompletionTokens();
                int promptTokens = llmResult.getPromptTokens();//
                int totalTokens = llmResult.getTotalTokens();
                LogUtil.e("completionTokens:" + completionTokens + "promptTokens:" + promptTokens + "totalTokens:" + totalTokens);
                sessionFinished = true;

                if (null != mCurrentMessage) {
                    ttsHelper.start(mCurrentMessage.content);
                }
            }
        }

        @Override
        public void onLLMEvent(LLMEvent event, Object usrContext) {
            LogUtil.d("onLLMEvent\n");
            LogUtil.w("onLLMEvent:" + " " + event.getEventID() + " " + event.getEventMsg());
        }

        @Override
        public void onLLMError(LLMError error, Object usrContext) {
            LogUtil.d("onLLMError\n");
            LogUtil.e("errCode:" + error.getErrCode() + "errDesc:" + error.getErrMsg());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onMessageRcv(MSG_TYPE_NEW, "错误:" + " err:" + error.getErrCode() + " errDesc:" + error.getErrMsg());
                }
            });
            if (usrContext != null) {
                String context = (String) usrContext;
                LogUtil.d("context:" + context);
            }
            sessionFinished = true;
        }
    };

    // 开始听写
    // 如何判断一次听写结束：OnResult isLast=true 或者 onError
    private void startRecognize() {
        buffer.setLength(0);
        mIatResults.clear();
        // 设置参数
        setParam();

        ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("听写失败,错误码：" + ret + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");

            showSystemMessage("听写服务失败");
        } else {
            showTip("请开始说话…");

            showRecordingDialog();
            recognizeFinished = false;
            ttsHelper.stop();
            showRecognizeResult(MSG_TYPE_NEW, "");
        }
    }

    private void stopRecognize() {
        mIat.stopListening();
        showTip("停止听写");
    }

    private void cancelRecognize() {
        mIat.cancel();
        showTip("取消听写");
    }

    /**
     * 参数设置
     *
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        if (language.equals("zh_cn")) {
            String lag = "mandarin";
            // 设置语言
            LogUtil.e("language = " + language);
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, language);
        }
        LogUtil.e("last language:" + mIat.getParameter(SpeechConstant.LANGUAGE));

        //此处用于设置dialog中不显示错误码信息
        //mIat.setParameter("view_tips_plain","false");

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "2000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav.
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
                getExternalFilesDir("msc").getAbsolutePath() + "/iat.wav");
    }

    /**
     * 显示结果
     */
    private void printResult(RecognizerResult results, boolean isLast) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        String result = resultBuffer.toString();
        showRecognizeResult(MSG_TYPE_UPDATE, result);

        if (isLast) {
            if (TextUtils.isEmpty(result)) {
                showSystemMessage("没听到，请重试");
            } else {
                if (sessionFinished) {
                    startChat(result);
                } else {
                    //ToastUtil.toast(Spark40Activity.this, "Busying! Please Wait");
                    showSystemMessage("语音助手忙线中，请稍后再试");
                }
            }
        }
    }

    private void showTip(String tip) {
        LogUtil.i(tip);
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            LogUtil.d("SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            LogUtil.d("onError " + error.getPlainDescription(true));
            showTip(error.getPlainDescription(true));

            showSystemMessage("听写服务失败：" + error.getErrorCode());
            recognizeFinished = true;
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            LogUtil.d(results.getResultString());
            if (isLast) {
                LogUtil.d("onResult 结束");
                dismissRecordingDialog();
                recognizeFinished = true;
            }
            if (resultType.equals("json")) {
                printResult(results, isLast);
                return;
            }
            if (resultType.equals("plain")) {
                buffer.append(results.getResultString());
                LogUtil.d(buffer.toString());
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            //showTip("当前正在说话，音量大小 = " + volume + " 返回音频数据 = " + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };
}