package com.jueyuantech.glasses;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.graphics.Color;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.graphics.drawable.GradientDrawable;

import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.bean.LanguageHint;
import com.jueyuantech.venussdk.bean.VNSttInfo;

public class CustomEActivity extends VenusAppBaseActivity implements View.OnClickListener {

    private ImageView mBackIv;
    private Button mStartBtn;
    private Button mToggleAudioSourceBtn;
    private Button mRandomLanguageBtn;
    
    // 音频源状态跟踪
    private boolean mIsUsingGlassesAudio = true;
    
    // UI相关变量
    private LinearLayout mMessagesContainer;
    private ScrollView mScrollView;
    private TextView mCurrentMessageView;

    // 测试相关变量
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsTesting = false;
    private int mCurrentSentenceIndex = 0;
    private int mCurrentAppendCount = 0;
    
    // 测试句子数据
    private String[] mTestTranscribes = {
        "今天天气真不错",
        "我正在测试语音转写功能", 
        "这是第三个测试句子",
        "流式语音识别效果很好",
        "测试即将结束了"
    };
    
    private String[] mTestTranslates = {
        "The weather is really nice today",
        "I am testing the speech transcription function",
        "This is the third test sentence", 
        "Streaming speech recognition works very well",
        "The test is about to end"
    };
    
    // 中文追加内容
    private String[] mAppendTextsChinese = {"，很", "棒的", "功能", "真的", "不错！"};
    
    // 英文追加内容
    private String[] mAppendTextsEnglish = {", very", " good", " feature", " really", " nice!"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_e_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mStartBtn = findViewById(R.id.btn_start);
        mStartBtn.setOnClickListener(this);
        
        mToggleAudioSourceBtn = findViewById(R.id.btn_toggle_audio_source);
        mToggleAudioSourceBtn.setOnClickListener(this);
        
        mRandomLanguageBtn = findViewById(R.id.btn_random_language);
        mRandomLanguageBtn.setOnClickListener(this);
        
        // 初始化UI组件
        mMessagesContainer = findViewById(R.id.ll_messages_container);
        mScrollView = findViewById(R.id.sv_messages);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理Handler，避免内存泄漏
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mIsTesting = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.btn_start:
                if (mIsTesting) {
                    stopTest();
                } else {
                    showConfigDialog();
                }
                break;
            case R.id.btn_toggle_audio_source:
                toggleAudioSource();
                break;
            case R.id.btn_random_language:
                setRandomLanguageHint();
                break;
            default:
        }
    }

    @Override
    protected void notifyVenusEnter() {
        VNCommon.setView(VNConstant.View.CUSTOM_E, null);
    }

    @Override
    protected void notifyVenusExit() {
        VNCommon.setView(VNConstant.View.HOME, null);
    }

    @Override
    protected void notifyExitFromVenus() {

    }

    private int mVenusApp;

    private void initConfig(boolean showHistoryText, boolean showOriginalAndTranslation, boolean useGlassesAudio) {
        /*
         * 1. VenusApp Type : TRANSCRIBE or TRANSLATE
         * 2. SttEngine : Engine Provider
         * 3. SttEngine : CallbackListener
         * 4. TextMode :
         * 5. TransShowMode :
         * 6. MaxLine :
         * 7. Audio Source :
         * 8. Mic Directional :
         * 9. Language Hint:
         * 10. Listening String :
         * 11. Reset to Silence and Listening... :
         */

        // STEP 1:
        mVenusApp = VNConstant.View.CUSTOM_E;

        // STEP 2:

        // STEP 3:

        // STEP 4: 根据用户选择设置文本显示模式
        int venusTextMode = showHistoryText ? 
            VNConstant.SttConfig.TextMode.CURRENT_AND_HISTORICAL :
            VNConstant.SttConfig.TextMode.CURRENT_ONLY;
        VNCommon.setTextMode(mVenusApp, venusTextMode, null);

        // STEP 5: 根据用户选择设置译文显示模式
        int transShowMode = showOriginalAndTranslation ? 
            VNConstant.SttConfig.TransMode.ORIGINAL_AND_TRANSLATION : 
            VNConstant.SttConfig.TransMode.TRANSLATION_ONLY;
        VNCommon.setTransMode(mVenusApp, transShowMode, null);

        // STEP 6:
        //VNCommon.setMaxLine(mVenusApp, 6, null);

        // STEP 7: 根据用户选择设置音频输入源
        int venusAudioInput = useGlassesAudio ? 
            VNConstant.SttConfig.AudioSource.GLASSES : 
            VNConstant.SttConfig.AudioSource.PHONE;
        VNCommon.setAudioSourceIndicator(mVenusApp, venusAudioInput, null);

        // STEP 8: 设置麦克风方向性
        int venusMicDirectional = VNConstant.SttConfig.MicDirectional.OMNI;
        VNCommon.setMicDirectional(mVenusApp, venusMicDirectional, null);

        // STEP 9:
        LanguageHint languageHint = new LanguageHint();
        languageHint.setMode(1);
        languageHint.setSource("中文");
        languageHint.setTarget("English");
        VNCommon.setLanguageHint(mVenusApp, languageHint, null);

        // STEP 10:

        // STEP 11:
    }

    private void updateSttInfo(int actionType, String transcribe, String translate) {
        // 发送到设备端
        VNSttInfo sttInfo = new VNSttInfo();
        sttInfo.setTranscribe(transcribe);
        sttInfo.setTranslate(translate);
        sttInfo.setActionType(actionType);
        sttInfo.setMsgType(VNConstant.SttInfo.MsgType.STT);
        sttInfo.setCreatedAt(System.currentTimeMillis() / 1000);
        VNCommon.updateSttInfo(mVenusApp, sttInfo, null);
        
        // 同时在UI上显示
        updateUIWithSttInfo(actionType, transcribe, translate);
    }

    /**
     * 在UI上更新语音转写信息
     */
    private void updateUIWithSttInfo(int actionType, String transcribe, String translate) {
        runOnUiThread(() -> {
            if (actionType == VNConstant.SttInfo.ActionType.NEW) {
                // 创建新的消息视图
                mCurrentMessageView = createMessageView();
                mMessagesContainer.addView(mCurrentMessageView);
            }
            
            // 更新当前消息内容
            if (mCurrentMessageView != null) {
                StringBuilder content = new StringBuilder();
                content.append("原文: ").append(transcribe).append("\n");
                content.append("译文: ").append(translate);
                
                mCurrentMessageView.setText(content.toString());
            }
            
            // 滚动到底部
            mScrollView.post(() -> mScrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    /**
     * 创建消息视图
     */
    private TextView createMessageView() {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setPadding(20, 15, 20, 15);
        textView.setTextSize(16);
        textView.setTextColor(Color.WHITE);
        
        // 使用固定的背景颜色
        int backgroundColor = Color.parseColor("#4CAF50"); // 绿色
        
        // 创建圆角背景
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(backgroundColor);
        shape.setCornerRadius(15);
        textView.setBackground(shape);
        
        // 设置边距
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 10);
        textView.setLayoutParams(params);
        
        return textView;
    }

    private void showConfigDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_config, null);

        Switch switchTextMode = dialogView.findViewById(R.id.switch_text_mode);
        Switch switchTransMode = dialogView.findViewById(R.id.switch_trans_mode);
        Switch switchAudioSource = dialogView.findViewById(R.id.switch_audio_source);
        
        // 获取说明文字的TextView引用
        TextView textModeDesc = dialogView.findViewById(R.id.tv_text_mode_desc);
        TextView transModeDesc = dialogView.findViewById(R.id.tv_trans_mode_desc);
        TextView audioSourceDesc = dialogView.findViewById(R.id.tv_audio_source_desc);

        // 设置默认状态
        switchTextMode.setChecked(false);  // 默认只显示当前
        switchTransMode.setChecked(true);  // 默认显示译文和原文
        switchAudioSource.setChecked(false);  // 默认手机收声
        
        // 初始化文字内容和颜色
        updateTextContent(textModeDesc, switchTextMode.isChecked(), "只显示当前文本", "显示当前和历史文本");
        updateTextContent(transModeDesc, switchTransMode.isChecked(), "只显示译文", "显示译文和原文");
        updateTextContent(audioSourceDesc, switchAudioSource.isChecked(), "手机收声", "眼镜收声");
        
        // 设置开关状态监听器
        switchTextMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateTextContent(textModeDesc, isChecked, "只显示当前文本", "显示当前和历史文本");
            }
        });
        
        switchTransMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateTextContent(transModeDesc, isChecked, "只显示译文", "显示译文和原文");
            }
        });
        
        switchAudioSource.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateTextContent(audioSourceDesc, isChecked, "手机收声", "眼镜收声");
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setPositiveButton("确定", (dialog, which) -> {
                    // 获取用户选择并直接传递给initConfig
                    boolean showHistoryText = switchTextMode.isChecked();
                    boolean showOriginalAndTranslation = switchTransMode.isChecked();
                    boolean useGlassesAudio = switchAudioSource.isChecked();
                    
                    // 应用配置
                    initConfig(showHistoryText, showOriginalAndTranslation, useGlassesAudio);
                    
                    // 延时500ms开始测试
                    mHandler.postDelayed(() -> startTest(), 500);
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    /**
     * 根据开关状态更新文字内容
     * @param textView 要更新的TextView
     * @param isChecked 开关是否选中
     * @param offText 关闭状态的文字
     * @param onText 开启状态的文字
     */
    private void updateTextContent(TextView textView, boolean isChecked, String offText, String onText) {
        // 根据开关状态设置文字内容
        textView.setText(isChecked ? onText : offText);
        // 固定使用venus_green颜色
        textView.setTextColor(Color.parseColor("#4CAF50"));
    }
    
    /**
     * 开始测试
     */
    private void startTest() {
        mIsTesting = true;
        mCurrentSentenceIndex = 0;
        mCurrentAppendCount = 0;
        mStartBtn.setText("停止测试");
        
        // 开始第一个句子
        sendNewSentence();
    }
    
    /**
     * 停止测试
     */
    private void stopTest() {
        mIsTesting = false;
        mHandler.removeCallbacksAndMessages(null);
        mStartBtn.setText("开始");

        // 清理UI
        runOnUiThread(() -> {
            mMessagesContainer.removeAllViews();
            mCurrentMessageView = null;
        });
    }
    
    /**
     * 发送新句子
     */
    private void sendNewSentence() {
        if (!mIsTesting) {
            // 测试已停止
            return;
        }

        // 如果到达数组末尾，重置索引实现循环测试
        if (mCurrentSentenceIndex >= mTestTranscribes.length) {
            mCurrentSentenceIndex = 0;
        }

        // 重置追加计数
        mCurrentAppendCount = 0;
        
        // 获取当前句子内容并发送新句子
        String currentTranscribe = mTestTranscribes[mCurrentSentenceIndex];
        String currentTranslate = mTestTranslates[mCurrentSentenceIndex];
        updateSttInfo(VNConstant.SttInfo.ActionType.NEW, currentTranscribe, currentTranslate);
        
        // 延时后开始追加
        mHandler.postDelayed(() -> appendToSentence(currentTranscribe, currentTranslate), 300);
    }
    
    /**
     * 追加到当前句子
     */
    private void appendToSentence(String baseTranscribe, String baseTranslate) {
        if (!mIsTesting) {
            return;
        }
        
        if (mCurrentAppendCount < 5) {
            // 追加对应的中文和英文内容
            String appendChinese = mAppendTextsChinese[mCurrentAppendCount % mAppendTextsChinese.length];
            String appendEnglish = mAppendTextsEnglish[mCurrentAppendCount % mAppendTextsEnglish.length];
            
            String updatedTranscribe = baseTranscribe + appendChinese;
            String updatedTranslate = baseTranslate + appendEnglish;
            
            // 发送更新
            updateSttInfo(VNConstant.SttInfo.ActionType.UPDATE, updatedTranscribe, updatedTranslate);
            
            mCurrentAppendCount++;
            
            // 继续追加
            mHandler.postDelayed(() -> appendToSentence(updatedTranscribe, updatedTranslate), 200);
        } else {
            // 追加完成，准备下一个句子
            mCurrentSentenceIndex++;
            mHandler.postDelayed(() -> sendNewSentence(), 500);
        }
    }
    
    /**
     * 切换音频源
     */
    private void toggleAudioSource() {
        mIsUsingGlassesAudio = !mIsUsingGlassesAudio;
        
        int venusAudioInput = mIsUsingGlassesAudio ? 
            VNConstant.SttConfig.AudioSource.GLASSES : 
            VNConstant.SttConfig.AudioSource.PHONE;
        
        VNCommon.setAudioSourceIndicator(mVenusApp, venusAudioInput, null);
        
        // 更新按钮文字显示当前状态
        String buttonText = mIsUsingGlassesAudio ? "音频源:眼镜" : "音频源:手机";
        mToggleAudioSourceBtn.setText(buttonText);
        
        // 显示提示信息
        String message = mIsUsingGlassesAudio ? "已切换到眼镜收声" : "已切换到手机收声";
        runOnUiThread(() -> {
            // 可以在这里添加Toast提示或其他UI反馈
        });
    }
    
    /**
     * 随机设置语言提示
     */
    private void setRandomLanguageHint() {
        // 定义语言对数组
        String[][] languagePairs = {
            {"中文", "English"},
            {"English", "中文"},
            {"中文", "日本語"},
            {"日本語", "中文"},
            {"English", "日本語"},
            {"日本語", "English"}
        };
        
        // 随机选择一个语言对
        int randomIndex = (int) (Math.random() * languagePairs.length);
        String[] selectedPair = languagePairs[randomIndex];
        
        LanguageHint languageHint = new LanguageHint();
        languageHint.setMode(1);
        languageHint.setSource(selectedPair[0]);
        languageHint.setTarget(selectedPair[1]);
        
        VNCommon.setLanguageHint(mVenusApp, languageHint, null);
        
        // 更新按钮文字显示当前语言对
        String buttonText = selectedPair[0] + "→" + selectedPair[1];
        mRandomLanguageBtn.setText(buttonText);
        
        // 显示提示信息
        String message = "语言设置: " + selectedPair[0] + " → " + selectedPair[1];
        runOnUiThread(() -> {
            // 可以在这里添加Toast提示或其他UI反馈
        });
    }
}