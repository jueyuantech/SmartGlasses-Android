package com.jueyuantech.glasses;

import static android.media.AudioTrack.PLAYSTATE_PLAYING;

import static com.jueyuantech.glasses.common.Constants.AUDIO_RECORD_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.AUDIO_RECORD_ENABLED;
import static com.jueyuantech.glasses.common.Constants.MMKV_AUDIO_RECORD_KEY;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jueyuantech.glasses.adapter.ChatAdapter;
import com.jueyuantech.glasses.bean.ChatMessageBean;
import com.jueyuantech.glasses.bean.SpeechMessage;
import com.jueyuantech.glasses.bean.SpeechSession;
import com.jueyuantech.glasses.db.DBHelper;
import com.jueyuantech.glasses.util.MmkvUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class SpeechMessageListActivity extends AppCompatActivity implements View.OnClickListener {


    private DBHelper dbHelper;

    private ImageView mBackIv;
    private ImageView mHelpIv;
    private TextView mTitleTv;


    private SpeechSession mSpeechSession;
    private File mAudioFile;
    private List<SpeechMessage> mSpeechMessages = new ArrayList<>();

    private RecyclerView mChatRcv;
    private ChatAdapter mChatAdapter;
    private List<ChatMessageBean> messageBeen = new ArrayList<>();
    private int scrollState = 0;

    private AudioTrack mAudioTrack;
    private Handler mMainHandler;
    private int sampleRateInHz = 16000; // 根据你的PCM文件设置采样率
    private int channelConfig = AudioFormat.CHANNEL_IN_DEFAULT; // 根据你的PCM文件设置声道配置
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT; // 根据你的PCM文件设置音频格式
    private int bufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
    private int pcmTimeCount;

    private RelativeLayout mFooterContainerRl;
    private ImageView mPlayBtn;
    private SeekBar mDurationSkb;
    private TextView mProgressTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_message_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        dbHelper = new DBHelper(this);

        mSpeechSession = (SpeechSession) getIntent().getSerializableExtra("speech_session");
        if (null != mSpeechSession.getAudioFileName()) {
            mAudioFile = new File(getExternalFilesDir("VenusPCM"), mSpeechSession.getAudioFileName());
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);
        mHelpIv = findViewById(R.id.iv_help);
        mHelpIv.setOnClickListener(this);

        mTitleTv = findViewById(R.id.tv_title);
        String func = mSpeechSession.getFuncType();
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
        mSpeechMessages.clear();
        mSpeechMessages.addAll(dbHelper.getMessagesBySessionId(mSpeechSession.getId()));
        for (SpeechMessage speechMessage : mSpeechMessages) {
            String type = speechMessage.getType();
            if ("sys".equals(type)) {
                mChatAdapter.addData(
                        new ChatMessageBean(
                                ChatMessageBean.TYPE_SYSTEM,
                                "System",
                                "",
                                speechMessage.getOriginalText()
                        )
                );
            } else {
                mChatAdapter.addData(
                        new ChatMessageBean(
                                ChatMessageBean.TYPE_RECEIVED,
                                "SmartGlasses",
                                "",
                                speechMessage.getOriginalText() + "\n" + speechMessage.getTranslatedText()
                        )
                );
            }
        }
        mChatAdapter.notifyDataSetChanged();
        mChatRcv.scrollToPosition(messageBeen.size() - 1);

        mFooterContainerRl = findViewById(R.id.rl_container_footer);
        mDurationSkb = findViewById(R.id.skb_duration);
        mPlayBtn = findViewById(R.id.iv_play);
        mPlayBtn.setOnClickListener(this);
        mProgressTv = findViewById(R.id.tv_progress);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isAudioRecordEnabled() && null != mAudioFile && mAudioFile.exists()) {
            mFooterContainerRl.setVisibility(View.VISIBLE);
            initPlayer();
        } else {
            mFooterContainerRl.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_help:
                exportSessionToTxt();
                break;
            case R.id.iv_play:
                playAudio();
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
        }

        super.onDestroy();
    }

    private boolean isAudioRecordEnabled() {
        return AUDIO_RECORD_ENABLED == (int) MmkvUtil.decode(MMKV_AUDIO_RECORD_KEY, AUDIO_RECORD_DEFAULT);
    }

    private void initPlayer() {
        mMainHandler = new Handler(Looper.myLooper());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAudioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                                    .build()
                    )
                    .setAudioFormat(
                            new AudioFormat.Builder()
                                    .setSampleRate(sampleRateInHz)
                                    .setChannelMask(AudioFormat.CHANNEL_IN_DEFAULT)
                                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                    .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .setBufferSizeInBytes(bufferSize)
                    .build();
        } else {
            mAudioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRateInHz,
                    AudioFormat.CHANNEL_IN_DEFAULT,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STREAM
            );
        }

        mAudioTrack.setPlaybackPositionUpdateListener(onPlaybackPositionUpdateListener, mMainHandler);
        mAudioTrack.setPositionNotificationPeriod(1000);
    }

    private AudioTrack.OnPlaybackPositionUpdateListener onPlaybackPositionUpdateListener = new AudioTrack.OnPlaybackPositionUpdateListener() {
        @Override
        public void onMarkerReached(AudioTrack track) {

        }

        @Override
        public void onPeriodicNotification(AudioTrack track) {
            int playFrame = mAudioTrack.getPlaybackHeadPosition();
            int rate = mAudioTrack.getPlaybackRate();
            float currentPlayTime = playFrame * 1.0f / rate * 1.0f;
            mDurationSkb.setProgress((int) MathUtils.clamp(Math.ceil(((currentPlayTime / pcmTimeCount) * 100)), 0, 100));
        }
    };

    private void playAudio() {
        if (mAudioTrack.getState() != AudioTrack.STATE_UNINITIALIZED && mAudioTrack.getPlayState() != PLAYSTATE_PLAYING) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //InputStream dis = getAssets().open("test.pcm");
                        InputStream dis = new FileInputStream(mAudioFile);
                        mAudioTrack.play();
                        int length = 0;
                        byte a[] = new byte[bufferSize];
                        if (dis.available() > 0) {
                            int fileSize = dis.available();
                            //根据计算公式：数据量Byte=44100Hz × (16/8) × 2 × 10s = 1764KByte然后转化为相应的单位
                            mMainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (fileSize > 0) {
                                        pcmTimeCount = (int) ((fileSize * 1.0f) / (16.0f / 8.0f) / (2.0f) / 8000);
                                    }
                                }
                            });
                        }
                        while ((length = dis.read(a)) != -1) {
                            mAudioTrack.write(a, 0, length);
                        }
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SpeechMessageListActivity.this, "播放结束", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        mAudioTrack.stop();
                        mAudioTrack.release();
                    }
                }
            }).start();
        }
    }

    private void exportSessionToTxt() {
        if (mSpeechMessages.isEmpty()) {
            Toast.makeText(this, "没有可导出的内容", Toast.LENGTH_SHORT).show();
            return;
        }

        File sessionDir = getExternalFilesDir("VenusSession");
        if (!sessionDir.exists()) {
            sessionDir.mkdirs();
        }

        File exportFile = new File(sessionDir, mSpeechSession.getId() + ".txt");
        try {
            if (!exportFile.exists()) {
                exportFile.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(exportFile);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");

            for (SpeechMessage message : mSpeechMessages) {
                writer.write("类型: " + message.getType() + "\n");
                writer.write("原文: " + message.getOriginalText() + "\n");
                if (message.getTranslatedText() != null && !message.getTranslatedText().isEmpty()) {
                    writer.write("译文: " + message.getTranslatedText() + "\n");
                }
                writer.write("------------------------\n");
            }

            writer.close();
            fos.close();

            Toast.makeText(this, "导出成功：" + exportFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "导出失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}