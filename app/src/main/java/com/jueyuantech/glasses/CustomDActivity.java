package com.jueyuantech.glasses;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.Gravity;
import android.graphics.drawable.GradientDrawable;

import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.bean.LanguageHint;
import com.jueyuantech.venussdk.bean.VNSttInfo;

public class CustomDActivity extends VenusAppBaseActivity implements View.OnClickListener {

    private ImageView mBackIv;
    private Button mStartBtn;
    private LinearLayout mChatContainer;
    private TextView mStatusTextView;
    private Handler mHandler = new Handler();
    private boolean isSimulating = false; // 标记是否正在模拟对话
    private TextView mCurrentStreamingTextView; // 当前正在流式更新的TextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_d_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);
        
        mStartBtn = findViewById(R.id.btn_start);
        mStartBtn.setOnClickListener(this);
        
        mChatContainer = findViewById(R.id.ll_chat_container);
        mStatusTextView = findViewById(R.id.tv_status);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initConfig();
            }
        }, 500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateSttInfo("系统已就绪，点击开始测试按钮开始模拟对话");
                mStatusTextView.setText("系统已就绪，点击开始测试按钮开始模拟对话");
            }
        }, 1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.btn_start:
                if (!isSimulating) {
                    startConversationSimulation();
                }
                break;
            default:
        }
    }

    @Override
    protected void notifyVenusEnter() {
        VNCommon.setView(VNConstant.View.CUSTOM_D, null);
    }

    @Override
    protected void notifyVenusExit() {
        VNCommon.setView(VNConstant.View.HOME, null);
    }

    @Override
    protected void notifyExitFromVenus() {

    }

    private int mVenusApp;

    private void initConfig() {
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
        mVenusApp = VNConstant.View.CUSTOM_D;

        // STEP 2:

        // STEP 3:

        // STEP 4:
        int venusTextMode = VNConstant.SttConfig.TextMode.CURRENT_AND_HISTORICAL;
        VNCommon.setTextMode(mVenusApp, venusTextMode, null);

        // STEP 5:
        //int transShowMode = VNConstant.SttConfig.TransMode.ORIGINAL_AND_TRANSLATION;
        //VNCommon.setTransMode(mVenusApp, transShowMode, null);

        // STEP 6:
        //VNCommon.setMaxLine(mVenusApp, 6, null);

        // STEP 7: 设置音频输入源
        int venusAudioInput = VNConstant.SttConfig.AudioSource.PHONE;
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

    private void updateSttInfo(String msg) {
        VNSttInfo sttInfo = new VNSttInfo();
        sttInfo.setTranscribe(msg);
        sttInfo.setActionType(VNConstant.SttInfo.ActionType.NEW);
        sttInfo.setMsgType(VNConstant.SttInfo.MsgType.STT);
        sttInfo.setCreatedAt(System.currentTimeMillis() / 1000);
        VNCommon.updateSttInfo(mVenusApp, sttInfo, null);
    }
    
    private void updateSttInfo(String msg, int area, boolean isNewSentence) {
        // 打印当前角色和文本内容
        String role = (area == 1) ? "用户" : "语音助手";
        String actionType = isNewSentence ? "NEW" : "UPDATE";
        Log.d("CustomDActivity", "角色: " + role + ", 动作: " + actionType + ", 文本内容: " + msg);
        
        // 更新UI显示
        updateUIDisplay(msg, area, isNewSentence);
        
        VNSttInfo sttInfo = new VNSttInfo();
        sttInfo.setTranscribe(msg);
        sttInfo.setArea(area);
        sttInfo.setActionType(isNewSentence ? VNConstant.SttInfo.ActionType.NEW : VNConstant.SttInfo.ActionType.UPDATE);
        sttInfo.setMsgType(VNConstant.SttInfo.MsgType.STT);
        sttInfo.setCreatedAt(System.currentTimeMillis() / 1000);
        VNCommon.updateSttInfo(mVenusApp, sttInfo, null);
    }
    
    private void updateSttInfo(String msg, int area) {
        updateSttInfo(msg, area, true); // 默认为新句子
    }
    
    /**
     * 更新UI显示
     * @param msg 消息内容
     * @param area 区域 (1=用户, 0=语音助手)
     * @param isNewSentence 是否为新句子
     */
    private void updateUIDisplay(String msg, int area, boolean isNewSentence) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isNewSentence) {
                    // 创建新的聊天气泡
                    mCurrentStreamingTextView = createChatBubble(msg, area);
                    mChatContainer.addView(mCurrentStreamingTextView);
                } else {
                    // 更新当前流式文本
                    if (mCurrentStreamingTextView != null) {
                        mCurrentStreamingTextView.setText(msg);
                    }
                }
                
                // 更新状态
                if (area == 1) {
                    mStatusTextView.setText("用户正在说话...");
                } else {
                    mStatusTextView.setText("语音助手正在回答...");
                }
                
                // 滚动到底部
                scrollToBottom();
            }
        });
    }
    
    /**
     * 创建聊天气泡
     * @param msg 消息内容
     * @param area 区域 (1=用户, 0=语音助手)
     * @return TextView
     */
    private TextView createChatBubble(String msg, int area) {
        TextView textView = new TextView(this);
        textView.setText(msg);
        textView.setTextSize(14);
        textView.setTextColor(getResources().getColor(android.R.color.white));
        textView.setPadding(24, 16, 24, 16);
        textView.setMinHeight(80);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        
        // 创建圆角背景
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(24);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 8, 16, 8);
        
        if (area == 1) {
            // 用户消息 - 右对齐，绿色背景
            drawable.setColor(getResources().getColor(android.R.color.holo_green_dark));
            params.gravity = Gravity.END;
            params.setMarginStart(100); // 左边距大，右对齐效果
        } else {
            // 助手消息 - 左对齐，蓝色背景
            drawable.setColor(getResources().getColor(android.R.color.holo_blue_dark));
            params.gravity = Gravity.START;
            params.setMarginEnd(100); // 右边距大，左对齐效果
        }
        
        textView.setBackground(drawable);
        textView.setLayoutParams(params);
        
        return textView;
    }
    
    /**
     * 滚动到底部
     */
    private void scrollToBottom() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ScrollView scrollView = (ScrollView) mChatContainer.getParent();
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
    
    // 对话数据
    private String[] userQuestions = {
        "今天天气怎么样？",
        "附近有什么好吃的餐厅？",
        "帮我设置一个明天上午9点的提醒",
        "播放一些轻音乐"
    };
    
    private String[] assistantAnswers = {
        "今天天气晴朗，温度适宜，是个出门的好日子。建议您穿轻薄的衣服，记得带上太阳镜。",
        "为您推荐几家附近的餐厅：海底捞火锅距离您500米，评分4.8分；星巴克咖啡距离您200米，适合休闲聊天。",
        "好的，我已经为您设置了明天上午9点的提醒。届时我会准时提醒您。",
        "正在为您播放轻音乐，希望您喜欢这些舒缓的旋律。"
    };
    
    private int currentConversationIndex = 0;
    
    /**
     * 开始模拟对话过程
     */
    private void startConversationSimulation() {
        Log.d("CustomDActivity", "开始模拟对话");
        isSimulating = true;
        mStartBtn.setEnabled(false);
        mStartBtn.setText("模拟中...");
        
        // 清空UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChatContainer.removeAllViews();
                mCurrentStreamingTextView = null;
                mStatusTextView.setText("开始模拟对话...");
            }
        });
        
        currentConversationIndex = 0;
        simulateNextConversation();
    }
    
    /**
     * 模拟下一轮对话
     */
    private void simulateNextConversation() {
        if (currentConversationIndex >= userQuestions.length) {
            // 所有对话完成，显示结束消息
            mHandler.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     updateSttInfo("对话模拟完成，感谢您的使用！", 0, true);
                     // 恢复按钮状态和UI显示
                     runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             mStatusTextView.setText("对话模拟完成，感谢您的使用！");
                             isSimulating = false;
                             mStartBtn.setEnabled(true);
                             mStartBtn.setText("开始测试");
                         }
                     });
                 }
             }, 2000);
            return;
        }
        
        String userQuestion = userQuestions[currentConversationIndex];
        String assistantAnswer = assistantAnswers[currentConversationIndex];
        
        // 先显示用户问题（流式）
        simulateStreamingText(userQuestion, 1, 0, new Runnable() {
            @Override
            public void run() {
                // 用户问题显示完成后，延迟1秒开始显示助手回答
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        simulateStreamingText(assistantAnswer, 0, 0, new Runnable() {
                            @Override
                            public void run() {
                                // 助手回答完成后，延迟2秒开始下一轮对话
                                currentConversationIndex++;
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        simulateNextConversation();
                                    }
                                }, 2000);
                            }
                        });
                    }
                }, 1000);
            }
        });
    }
    
    /**
     * 模拟流式文本显示
     * @param fullText 完整文本
     * @param area 区域 (1=用户, 0=语音助手)
     * @param currentIndex 当前字符索引
     * @param onComplete 完成回调
     */
    private void simulateStreamingText(String fullText, int area, int currentIndex, Runnable onComplete) {
        if (currentIndex >= fullText.length()) {
            // 文本显示完成
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        
        // 获取当前要显示的部分文本
        String currentText = fullText.substring(0, currentIndex + 1);
        
        // 判断是否为新句子（第一个字符）
        boolean isNewSentence = (currentIndex == 0);
        
        // 更新显示
        updateSttInfo(currentText, area, isNewSentence);
        
        // 延迟显示下一个字符（模拟流式效果）
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                simulateStreamingText(fullText, area, currentIndex + 1, onComplete);
            }
        }, 100); // 每100ms显示一个字符
    }
}