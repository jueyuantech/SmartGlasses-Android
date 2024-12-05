package com.jueyuantech.glasses;

import static com.jueyuantech.glasses.RecorderCallback.MSG_TYPE_NEW;
import static com.jueyuantech.glasses.RecorderCallback.MSG_TYPE_UPDATE;
import static com.jueyuantech.glasses.common.Constants.ASR_ENGINE_AISPEECH;
import static com.jueyuantech.glasses.common.Constants.ASR_ENGINE_AZURE;
import static com.jueyuantech.glasses.common.Constants.ASR_ENGINE_IFLYTEK;
import static com.jueyuantech.glasses.common.Constants.ASR_ENGINE_MOCK;
import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_TRANSLATE;
import static com.jueyuantech.glasses.common.Constants.AUDIO_INPUT_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.AUDIO_INPUT_SCO;
import static com.jueyuantech.glasses.common.Constants.MMKV_AUDIO_INPUT_KEY;
import static com.jueyuantech.glasses.stt.SttManager.STT_MSG_TYPE_REC;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import androidx.annotation.RequiresApi;

import com.jueyuantech.glasses.stt.AsrConfigManager;
import com.jueyuantech.glasses.stt.SttManager;
import com.jueyuantech.glasses.stt.aispeech.AiSpeechStt;
import com.jueyuantech.glasses.stt.azure.AzureAsr;
import com.jueyuantech.glasses.stt.iflytek.IFlyTekStt;
import com.jueyuantech.glasses.stt.mock.MockStt;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.venussdk.VenusConstant;
import com.jueyuantech.venussdk.VenusSDK;
import com.jueyuantech.venussdk.bean.AsrInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class RecorderService extends Service implements SttManager.OnSttListener {

    private Random random = new Random();

    private AudioManager mAudioManager;
    private NotificationManager mNotificationManager;
    private NotificationChannel mNotificationChannel;
    private int NOTIFICATION = R.string.app_name;
    private String STR_HEARING = "";
    private boolean MSG_TYPE_NEW_FLAG = true;

    private boolean isRecording;
    private AudioRecord mAudioRecord;
    private Thread mRecordingThread;

    private SttManager mSttManager;

    public RecorderCallback mCallback;

    // 采样率，现在能够保证在所有设备上使用的采样率是44100Hz, 但是其他的采样率（22050, 16000, 11025）在一些设备上也可以使用。
    public static final int SAMPLE_RATE_INHZ = 16000;
    // 声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_DEFAULT;
    // 返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private final IBinder mBinder = new LocalBinder();

    public RecorderService() {
        LogUtil.mark();
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.mark();
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        LogUtil.mark();

        initFunc(intent);

        STR_HEARING = getString(R.string.str_hearing);

        String CHANNEL_ID = "HEARING_CHANNEL_ID";
        String CHANNEL_NAME = "HEARING_CHANNEL_ID";
        mAudioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
        mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        //进行8.0的判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            mNotificationChannel.enableLights(true);
            mNotificationChannel.setLightColor(Color.RED);
            mNotificationChannel.setShowBadge(true);
            mNotificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }

        Intent actIntent = new Intent(this, RecorderActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, actIntent, PendingIntent.FLAG_MUTABLE);

        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(STR_HEARING)
                    .setTicker(getString(R.string.app_name))
                    .setContentIntent(contentIntent)
                    .build();
        }
        notification.flags |= Notification.FLAG_NO_CLEAR;

        mNotificationManager.notify(NOTIFICATION, notification);
        //在service里再调用startForeground方法，不然就会出现ANR
        startForeground(NOTIFICATION, notification);

        if (!isRecording) {
            startRecorder();
        }

        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDestroy() {
        super.onDestroy();
        mNotificationManager.cancel(NOTIFICATION);
    }

    private String func = ASR_FUNC_TRANSCRIBE;
    private int venusApp = VenusConstant.View.ASR;
    private void initFunc(Intent intent) {
        func = ASR_FUNC_TRANSCRIBE;
        venusApp = VenusConstant.View.ASR;
        try {
            func = intent.getStringExtra("func");
            if (ASR_FUNC_TRANSCRIBE.equals(func)) {
                venusApp = VenusConstant.View.ASR;
            } else if (ASR_FUNC_TRANSLATE.equals(func)) {
                venusApp = VenusConstant.View.TRANS;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCallback(RecorderCallback callback) {
        mCallback = callback;
    }

    public void connectEngine() {
        mSttManager.connect();
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void startRecorder() {
        String asrEngine = AsrConfigManager.getInstance().getEngine();
        if (ASR_ENGINE_AZURE.equals(asrEngine)) {
            mSttManager = new AzureAsr(this, func);
        } else if (ASR_ENGINE_AISPEECH.equals(asrEngine)) {
            mSttManager = new AiSpeechStt(func);
        } else if (ASR_ENGINE_IFLYTEK.equals(asrEngine)) {
            mSttManager = new IFlyTekStt(func);
        } else if (ASR_ENGINE_MOCK.equals(asrEngine)) {
            mSttManager = new MockStt(this, func);
        }
        mSttManager.setOnSttListener(this);
        startRecordAudio();
    }

    public void stopRecorder() {
        stopRecordAudio();
        mSttManager.disconnect();
    }

    @Override
    public void onConnect(String sid) {
        LogUtil.mark();
        // TODO init
        if (null != mCallback) {
            mCallback.onConnectSucceed();
            mCallback.onMessage(MSG_TYPE_NEW, STR_HEARING, "Listening...", true);
        }
        sendText(MSG_TYPE_NEW, STR_HEARING, "Listening...");

        MSG_TYPE_NEW_FLAG = false;
    }

    @Override
    public void onError(String msg) {
        LogUtil.mark();
        if (null != mCallback) {
            mCallback.onConnectFailed(msg);
        }
    }

    private long lastSegmentId = 0;
    @Override
    public void onMessage(long segmentId, String speakerId, int type, String asr, String trans) {
        LogUtil.i("==> segmentId:" + segmentId + " asr:" + asr + " trans:" + trans);

        int updateUiType = MSG_TYPE_NEW;
        if (segmentId > lastSegmentId) {
            updateUiType = MSG_TYPE_NEW;
            lastSegmentId = segmentId;
        } else if (segmentId == lastSegmentId){
            updateUiType = MSG_TYPE_UPDATE;
        } else {
            //FIXME 处理前一条segment的更新
            return;
        }
        LogUtil.i("==> updateUiType:" + (updateUiType == MSG_TYPE_NEW ? "NEW" : "UPDATE"));

        // Bt Device
        sendText(updateUiType, asr, trans);
        // APP UI
        if (null != mCallback) {
            mCallback.onMessage(updateUiType, asr, trans, STT_MSG_TYPE_REC == type);
        }
    }

    public class LocalBinder extends Binder {
        RecorderService getService() {
            LogUtil.mark();
            return RecorderService.this;
        }
    }

    private String startRecordAudio() {
        initAudioInput();

        String audioCacheFilePath = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/" + "hearing_test.pcm";
        try {
            // 获取最小录音缓存大小，
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);

            // 开始录音
            this.isRecording = true;
            mAudioRecord.startRecording();

            // 创建数据流，将缓存导入数据流
            this.mRecordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    File file = new File(audioCacheFilePath);
                    LogUtil.i("audio cache pcm file path:" + audioCacheFilePath);

                    if (file.exists()) {
                        file.delete();
                    }

                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        LogUtil.e("临时缓存文件未找到");
                        e.printStackTrace();
                    }
                    if (fos == null) {
                        return;
                    }

                    byte[] data = new byte[minBufferSize];
                    int read;
                    if (fos != null) {
                        while (isRecording && !mRecordingThread.isInterrupted()) {
                            read = mAudioRecord.read(data, 0, minBufferSize);
                            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                                try {
                                    fos.write(data);
                                    //LogUtil.i("写录音数据->" + read);
                                    mSttManager.send(data);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            this.mRecordingThread.start();
        } catch (IllegalStateException e) {
            LogUtil.w("需要获取录音权限！");
            //this.checkIfNeedRequestRunningPermission();
        } catch (SecurityException e) {
            LogUtil.w("需要获取录音权限！");
            //this.checkIfNeedRequestRunningPermission();
        }

        return audioCacheFilePath;
    }

    private void stopRecordAudio() {
        try {
            this.isRecording = false;
            if (this.mAudioRecord != null) {
                this.mAudioRecord.stop();
                this.mAudioRecord.release();
                this.mAudioRecord = null;
                this.mRecordingThread.interrupt();
                this.mRecordingThread = null;
            }
        } catch (Exception e) {
            LogUtil.w(e.getLocalizedMessage());
        }

        disableSco();
    }

    private void sendText(int type, String asr, String trans) {
        AsrInfo result = new AsrInfo();
        result.setId(random.nextInt());
        result.setArea(0);
        result.setText(asr);
        result.setTrans(trans);
        result.setUser("Hearing");
        result.setType(type);
        result.setCreatedAt(System.currentTimeMillis() / 1000);

        if (ASR_FUNC_TRANSCRIBE.equals(func)) {
            VenusSDK.updateAsr(result, null);
        } else if (ASR_FUNC_TRANSLATE.equals(func)) {
            VenusSDK.updateTrans(result, null);
        }
    }

    private void initAudioInput() {
        int audioInput = (int) MmkvUtil.decode(MMKV_AUDIO_INPUT_KEY, AUDIO_INPUT_DEFAULT);
        if (AUDIO_INPUT_SCO == audioInput) {
            enableSco();
        }
    }

    private void enableSco() {
        if (mAudioManager.isWiredHeadsetOn() || mAudioManager.isBluetoothA2dpOn() || mAudioManager.isBluetoothScoOn()) {
            //mAudioManager.setMicrophoneMute(false);
            //mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioManager.startBluetoothSco();
            mAudioManager.setBluetoothScoOn(true);
        }
    }

    private void disableSco() {
        //mAudioManager.setMicrophoneMute(false);
        //mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        mAudioManager.stopBluetoothSco();
        mAudioManager.setBluetoothScoOn(false);
    }

    private void registerAudioInputChanged() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        filter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
                    // 处理耳机插入/拔出的逻辑
                } else if (intent.getAction().equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                    // 处理蓝牙SCO音频状态更新的逻辑
                }
            }
        }, filter);
    }
}