package com.jueyuantech.glasses;

import static com.jueyuantech.glasses.common.Constants.MMKV_TRANS_TTS_KEY;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;
import static com.jueyuantech.glasses.common.Constants.TRANS_TTS_ENABLED;
import static com.jueyuantech.glasses.stt.SttWorkerCallback.MSG_TYPE_NEW;
import static com.jueyuantech.glasses.stt.SttWorkerCallback.MSG_TYPE_UPDATE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.jueyuantech.glasses.adapter.ChatAdapter;
import com.jueyuantech.glasses.bean.ChatMessageBean;
import com.jueyuantech.glasses.stt.SttWorker;
import com.jueyuantech.glasses.stt.SttWorkerCallback;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNCommon;

import java.util.ArrayList;
import java.util.List;

public class SttActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackIv;
    private TextView mTitleTv;
    private LottieAnimationView mAudioTrackLav;
    private RecyclerView mChatRcv;

    private ChatMessageBean mCurrentMessage;
    private ChatAdapter mChatAdapter;
    private List<ChatMessageBean> messageBeen = new ArrayList<>();
    private int scrollState = 0;

    private String func = STT_FUNC_TRANSCRIBE;
    private int venusApp = VNConstant.View.TRANSCRIBE;

    private TTSHelper ttsHelper;
    private boolean transTtsEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stt);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        initFunc(getIntent());

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mTitleTv = findViewById(R.id.tv_title);
        if (STT_FUNC_TRANSCRIBE.equals(func)) {
            mTitleTv.setText(R.string.func_stt);
        } else if (STT_FUNC_TRANSLATE.equals(func)) {
            mTitleTv.setText(R.string.func_trans);
        }

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

        mAudioTrackLav = findViewById(R.id.lav_audio_track);

        // FIXME TTSHelper to SttWorker
        ttsHelper = new TTSHelper(this);
        int transTts = MmkvUtil.decodeInt(MMKV_TRANS_TTS_KEY);
        transTtsEnable = TRANS_TTS_ENABLED == transTts;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SttWorker.getInstance().addSttWorkerCallback(sttWorkerCallback);
    }

    @Override
    protected void onPause() {
        SttWorker.getInstance().removeSttWorkerCallback(sttWorkerCallback);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unInitUI();

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            default:
        }
    }

    private void initFunc(Intent intent) {
        func = STT_FUNC_TRANSCRIBE;
        venusApp = VNConstant.View.TRANSCRIBE;
        try {
            func = intent.getStringExtra("func");
            if (STT_FUNC_TRANSCRIBE.equals(func)) {
                venusApp = VNConstant.View.TRANSCRIBE;
            } else if (STT_FUNC_TRANSLATE.equals(func)) {
                venusApp = VNConstant.View.TRANSLATE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unInitUI() {
        mAudioTrackLav.cancelAnimation();

        if (null != mCurrentMessage && mCurrentMessage.isHint) {
            if (mChatAdapter.getItemCount() > 0) {
                mChatAdapter.deleteData(mChatAdapter.getItemCount() - 1);
            }
        }
    }

    private void onMessageRcv(int type, String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MSG_TYPE_NEW == type) {
                    mCurrentMessage = new ChatMessageBean(ChatMessageBean.TYPE_RECEIVED, getString(R.string.app_name), "", msg);
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

    private void showSysMessage(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChatAdapter.addData(new ChatMessageBean(ChatMessageBean.TYPE_SYSTEM, "System", "", msg));
                mChatRcv.scrollToPosition(messageBeen.size() - 1);
            }
        });
    }

    private SttWorkerCallback sttWorkerCallback = new SttWorkerCallback() {
        @Override
        public void onWorkerInitComplete(String funcType, String engineType, String audioSource) {

        }

        @Override
        public void onWorkerStarting() {

        }

        @Override
        public void onWorkerStart() {
            showSysMessage("START");
        }

        @Override
        public void onWorkerStopping() {

        }

        @Override
        public void onWorkerStop() {
            showSysMessage("STOP");
        }

        @Override
        public void onWorkerErr(int code, String msg, String cause) {
            showSysMessage("WORKER ERR [" + code + "] " + cause);
        }

        @Override
        public void onEngineStart() {
            showSysMessage("EngineStart");
        }

        @Override
        public void onEngineStop() {
            showSysMessage("EngineStop");
        }

        @Override
        public void onEngineErr(int code, String msg, String cause) {
            showSysMessage("ENGINE ERR [" + code + "] " + cause);
        }

        @Override
        public void onEngineTick(long time) {

        }

        @Override
        public void onRecorderStart(String audioFilePath, String audioFileName) {
            showSysMessage("RecorderStart");
        }

        @Override
        public void onRecorderStop() {
            showSysMessage("RecorderStop");
        }

        @Override
        public void onRecorderErr(int code, String msg, String cause) {
            showSysMessage("REC ERR [" + code + "] " + cause);
        }

        @Override
        public void onSttMessage(int type, String transcribeStr, String translateStr, boolean isEnd) {
            LogUtil.i("type=" + type + " transcribeStr=" + transcribeStr);
            onMessageRcv(type, transcribeStr + (TextUtils.isEmpty(translateStr) ? "" : ("\n" + translateStr)));

            if (isEnd && transTtsEnable) {
                ttsHelper.start(translateStr);
            }
        }

        @Override
        public void onSysMessage(int level, String msg) {

        }

        @Override
        public void onHintMessage(String transcribeHintStr, String translateHintStr) {
            onMessageRcv(MSG_TYPE_NEW, transcribeHintStr + (TextUtils.isEmpty(translateHintStr) ? "" : ("\n" + translateHintStr)));
        }

        @Override
        public void onAudioTrackStateChanged(boolean silence) {
            if (silence) {
                mAudioTrackLav.setScaleY(0.2f);
            } else {
                mAudioTrackLav.setScaleY(1.0f);
            }
        }
    };
}