package com.jueyuantech.glasses;

import static com.jueyuantech.glasses.RecorderCallback.MSG_TYPE_NEW;
import static com.jueyuantech.glasses.RecorderCallback.MSG_TYPE_UPDATE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;
import static com.jueyuantech.glasses.common.Constants.MMKV_TRANS_SHOW_MODE_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_TRANS_TTS_KEY;
import static com.jueyuantech.glasses.common.Constants.TRANS_SHOW_MODE_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.TRANS_TTS_ENABLED;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.jueyuantech.glasses.adapter.ChatAdapter;
import com.jueyuantech.glasses.bean.ChatMessageBean;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNCommon;

import java.util.ArrayList;
import java.util.List;

public class RecorderActivity extends VenusAppBaseActivity implements View.OnClickListener, View.OnTouchListener {

    private ActivityManager mActivityManager;

    private String func = STT_FUNC_TRANSCRIBE;
    private int venusApp = VNConstant.View.TRANSCRIBE;
    private boolean mRecording = false;

    private ImageView mBackIv;
    private TextView mTitleTv;
    private Button mStartRecordBtn;
    private LottieAnimationView mAudioTrackLav;
    private RecyclerView mChatRcv;

    private ChatMessageBean mCurrentMessage;
    private ChatAdapter mChatAdapter;
    private List<ChatMessageBean> messageBeen = new ArrayList<>();
    private int scrollState = 0;

    private Intent mRecordServiceIntent;

    private TTSHelper ttsHelper;
    private boolean transTtsEnable = false;

    private boolean mBound = false;
    private RecorderService mRecordService;
    private RecorderCallback mRecordCallback = new RecorderCallback() {
        @Override
        public void onMessage(int type, String transcribeStr, String translateStr, boolean isEnd) {
            LogUtil.i("type=" + type + " transcribeStr=" + transcribeStr);
            onMessageRcv(type, transcribeStr + (TextUtils.isEmpty(translateStr) ? "" : ("\n" + translateStr)));

            if (isEnd && transTtsEnable) {
                ttsHelper.start(translateStr);
            }
        }

        @Override
        public void onAudioTrackStateChanged(boolean silence) {
            if (silence) {
                mAudioTrackLav.setScaleY(0.2f);
            } else {
                mAudioTrackLav.setScaleY(1.0f);
            }
        }

        @Override
        public void onConnectSucceed() {
            LogUtil.mark();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChatAdapter.addData(new ChatMessageBean(ChatMessageBean.TYPE_SYSTEM, "System", "", "Connected"));
                    mChatRcv.scrollToPosition(messageBeen.size() - 1);
                }
            });
        }

        @Override
        public void onConnectFailed(String message) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChatAdapter.addData(new ChatMessageBean(ChatMessageBean.TYPE_SYSTEM, "System", "", TextUtils.isEmpty(message) ? "Connection failed, please exit and try again." : message));
                    mChatRcv.scrollToPosition(messageBeen.size() - 1);
                }
            });
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecorderService.LocalBinder binder = (RecorderService.LocalBinder) service;
            mRecordService = binder.getService();
            mRecordService.setCallback(mRecordCallback);
            mRecordService.connectEngine();
            mBound = true;

            updateRecordState();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.mark();
        setContentView(R.layout.activity_recorder);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        initFunc(getIntent());

        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        mRecordServiceIntent = new Intent(this, RecorderService.class);
        mRecordServiceIntent.putExtra("func", func);

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);
        mTitleTv = findViewById(R.id.tv_title);

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

        mStartRecordBtn = findViewById(R.id.btn_start_record);
        mStartRecordBtn.setOnClickListener(this);
        mStartRecordBtn.setOnTouchListener(this);

        mAudioTrackLav = findViewById(R.id.lav_audio_track);

        ttsHelper = new TTSHelper(this);
        int transTts = MmkvUtil.decodeInt(MMKV_TRANS_TTS_KEY);
        transTtsEnable = TRANS_TTS_ENABLED == transTts;
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.mark();

        initFunc(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.mark();

        initFunc(getIntent());
        if (STT_FUNC_TRANSCRIBE.equals(func)) {
            mTitleTv.setText(R.string.func_stt);
        } else if (STT_FUNC_TRANSLATE.equals(func)) {
            mTitleTv.setText(R.string.func_trans);
        }

        startRecordServiceIfNotRunning();
        bindRecordServiceIfRunning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.mark();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.mark();
    }

    @Override
    protected void onDestroy() {
        unbindService();
        ttsHelper.destroy();
        LogUtil.mark();
        super.onDestroy();
    }

    @Override
    protected void notifyVenusEnter() {
        VNCommon.setView(venusApp, null);
    }

    @Override
    protected void notifyVenusExit() {
        VNCommon.setView(VNConstant.View.HOME, null);
    }

    @Override
    protected void notifyExitFromVenus() {
        stopWork();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_record:
                if (mRecording) {
                    stopWork();
                } else {
                    startWork();
                }
                updateRecordState();
                break;
            case R.id.iv_back:
                showExistDialog();
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExistDialog();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    private void startWork() {
        // Note: start recorder in service.onStartCommand()
        //mRecordService.startRecorder();
        if (venusApp == VNConstant.View.TRANSLATE) {
            int transShowMode = (int) MmkvUtil.decode(MMKV_TRANS_SHOW_MODE_KEY, TRANS_SHOW_MODE_DEFAULT);
            VNCommon.setTransMode(venusApp, transShowMode, null);
        }
        VNCommon.setAudioTrackState(venusApp, VNConstant.AudioTracker.SPEAKING, null);

        startService();
        bindService();
    }

    private void stopWork() {
        //VNCommon.getInstance().setAudioTrackState(venusApp, VNConstant.AudioTracker.INVISIBLE, null);
        if (null == mRecordService) {
            return;
        }

        mRecordService.stopRecorder();
        unbindService();
        stopService();

        if (null != mCurrentMessage && mCurrentMessage.isHint) {
            if (mChatAdapter.getItemCount() > 0) {
                mChatAdapter.deleteData(mChatAdapter.getItemCount() - 1);
            }
        }
    }

    private void showExistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.tips_stt_exit));
        builder.setNegativeButton(R.string.btn_stt_run_background, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
            }
        });

        builder.setPositiveButton(R.string.btn_stt_exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopWork();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask();
                } else {
                    finish();
                }
            }
        });

        Dialog mAlertDialog = builder.create();
        mAlertDialog.show();
        if (mAlertDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = mAlertDialog.getWindow().getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 8; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            mAlertDialog.getWindow().setAttributes(lp);
        }
    }

    private void startRecordServiceIfNotRunning() {
        boolean isServiceRunning = isRecordServiceRunning();

        if (!isServiceRunning) {
            startWork();
        }
    }

    private void bindRecordServiceIfRunning() {
        boolean isBound = false;
        boolean isServiceRunning = isRecordServiceRunning();

        if (isServiceRunning) {
            isBound = bindService();
        }
    }

    private void updateRecordState() {
        if (null == mRecordService) {
            return;
        }

        mRecording = mRecordService.isRecording();

        if (mRecording) {
            mStartRecordBtn.setText(getText(R.string.listen_stop));
        } else {
            mStartRecordBtn.setText(getText(R.string.listen_start));
        }
    }

    private void startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(mRecordServiceIntent);
        } else {
            this.startService(mRecordServiceIntent);
        }
    }

    private void stopService() {
        Intent serviceIntent = new Intent(this, RecorderService.class);
        stopService(serviceIntent);
    }

    private boolean bindService() {
        mBound = bindService(mRecordServiceIntent, mConnection, Context.BIND_AUTO_CREATE);
        return mBound;
    }

    private void unbindService() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private boolean isRecordServiceRunning() {
        List<ActivityManager.RunningServiceInfo> services = mActivityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : services) {
            if ("com.jueyuantech.glasses.RecorderService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
}