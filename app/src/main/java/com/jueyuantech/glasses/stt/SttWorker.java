package com.jueyuantech.glasses.stt;

import static com.jueyuantech.glasses.common.Constants.AUDIO_INPUT_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.AUDIO_INPUT_PHONE;
import static com.jueyuantech.glasses.common.Constants.AUDIO_INPUT_SCO;
import static com.jueyuantech.glasses.common.Constants.AUDIO_RECORD_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.AUDIO_RECORD_ENABLED;
import static com.jueyuantech.glasses.common.Constants.MIC_DIRECTIONAL_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.MIC_DIRECTIONAL_FRONT;
import static com.jueyuantech.glasses.common.Constants.MIC_DIRECTIONAL_OMNI;
import static com.jueyuantech.glasses.common.Constants.MMKV_AUDIO_INPUT_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_AUDIO_RECORD_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_MIC_DIRECTIONAL_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_SIMPLIFIED_MODE_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_TEXT_MODE_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_TRANS_SHOW_MODE_KEY;
import static com.jueyuantech.glasses.common.Constants.SIMPLIFIED_MODE_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.SIMPLIFIED_MODE_ENABLED;
import static com.jueyuantech.glasses.common.Constants.STT_ENGINE_AISPEECH;
import static com.jueyuantech.glasses.common.Constants.STT_ENGINE_AZURE;
import static com.jueyuantech.glasses.common.Constants.STT_ENGINE_AZURE_SWEDENCENTRAL;
import static com.jueyuantech.glasses.common.Constants.STT_ENGINE_AZURE_WESTUS;
import static com.jueyuantech.glasses.common.Constants.STT_ENGINE_IFLYTEK;
import static com.jueyuantech.glasses.common.Constants.STT_ENGINE_IFLYTEK_WEB_ASR;
import static com.jueyuantech.glasses.common.Constants.STT_ENGINE_IFLYTEK_WEB_IAT_MUL;
import static com.jueyuantech.glasses.common.Constants.STT_ENGINE_MOCK;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;
import static com.jueyuantech.glasses.common.Constants.TEXT_MODE_CURRENT_AND_HISTORICAL;
import static com.jueyuantech.glasses.common.Constants.TEXT_MODE_CURRENT_ONLY;
import static com.jueyuantech.glasses.common.Constants.TEXT_MODE_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.TRANS_SHOW_MODE_DEFAULT;
import static com.jueyuantech.glasses.stt.SttEngine.STT_MSG_TYPE_REC;
import static com.jueyuantech.glasses.stt.SttWorkerCallback.MSG_TYPE_NEW;
import static com.jueyuantech.glasses.stt.SttWorkerCallback.MSG_TYPE_UPDATE;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;
import com.jueyuantech.glasses.BuildConfig;
import com.jueyuantech.glasses.R;
import com.jueyuantech.glasses.bean.LanguageTag;
import com.jueyuantech.glasses.bean.ServiceKeyPkg;
import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.stt.aispeech.AiSpeechStt;
import com.jueyuantech.glasses.stt.azure.AzureStt;
import com.jueyuantech.glasses.stt.azure.AzureSwedenCentralStt;
import com.jueyuantech.glasses.stt.azure.AzureWestUSStt;
import com.jueyuantech.glasses.stt.iflytek.IFlyTekStt;
import com.jueyuantech.glasses.stt.iflytek.IFlyTekWebAsrStt;
import com.jueyuantech.glasses.stt.iflytek.IFlyTekWebIatMulStt;
import com.jueyuantech.glasses.stt.mock.MockStt;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.bean.VNSttInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class SttWorker {

    private static volatile SttWorker singleton;
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private ConnectivityManager mConnectivityManager;

    private Gson gson = new Gson();

    /* Audio Start */
    private AudioManager mAudioManager;
    private AudioRecord mAudioRecord;
    private Thread mRecordingThread;
    // 采样率，现在能够保证在所有设备上使用的采样率是44100Hz, 但是其他的采样率（22050, 16000, 11025）在一些设备上也可以使用。
    private static final int SAMPLE_RATE_HZ = 16000;
    // 声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_DEFAULT;
    // 返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private boolean isRecording = false;

    private int isAudioRecordEnabled = AUDIO_RECORD_DEFAULT;
    /* Audio End */

    /* Config Start */
    private int mVenusApp;
    private String mSttFunc;
    private SttEngine mSttEngine;
    private int mAudioInput = AUDIO_INPUT_DEFAULT;
    private String STR_LISTENING_TRANSCRIBE = "";
    private String STR_LISTENING_TRANSLATE = "";
    /* Config End */

    public static final int STATE_STOPPED = 0;
    public static final int STATE_STOPPING = 1;
    public static final int STATE_STARTING = 2;
    public static final int STATE_STARTED = 3;
    private int mCurState = STATE_STOPPED;

    /* Retry Start */
    private static final int MAX_RETRY_COUNT = 5;
    private static final int RETRY_DELAY_MS = 3000;
    private int mRetryCount = 0;
    /* Retry End */

    private long lastSegmentId = 0;

    private static final int SILENCE_TIMEOUT = 1000 * 2;
    private static final int MSG_SILENCE = 1;
    private static final int MSG_START_WORK_NOW = 101;
    private static final int MSG_START_RECORDER = 201;
    private static final int MSG_START_RETRY = 301;
    private static final int MSG_RESET_RETRY_COUNT = 1001;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_SILENCE:
                    switchSilenceState(true);
                    break;
                case MSG_START_WORK_NOW:
                    startWorkNow();
                    break;
                case MSG_START_RECORDER:
                    startRecordAudio();
                    break;
                case MSG_START_RETRY:
                    startWorkInner();
                    break;
                case MSG_RESET_RETRY_COUNT:
                    resetRetryCount();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private SttWorker(Context context) {
        mContext = context.getApplicationContext();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        initWorker();
    }

    public static SttWorker getInstance() {
        if (singleton == null) {
            throw new IllegalStateException("SttWorker is not initialized. Call init() before getInstance().");
        }
        return singleton;
    }

    /**
     * 初始化方法，应该在Application中调用
     *
     * @param context
     */
    public static void init(Context context) {
        if (singleton == null) {
            synchronized (SttWorker.class) {
                if (singleton == null) {
                    singleton = new SttWorker(context);
                }
            }
        }
    }

    private void initWorker() {
        DeviceManager.getInstance().addDeviceServiceListener(serviceListener);

        if (mAudioManager.isBluetoothScoAvailableOffCall()) {
            IntentFilter intentFilter = new IntentFilter();
            //intentFilter.addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED);
            //intentFilter.addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED);
            intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
            mContext.registerReceiver(mBroadcastReceiver, intentFilter);
        }
    }

    private void initConfig(String sttFunc) {
        /*
         * 1. VenusApp Type : TRANSCRIBE or TRANSLATE
         * 2. SttEngine : Engine Provider
         * 3. SttEngine : CallbackListener
         * 4. TextMode :
         * 5. TransShowMode :
         * 6. MaxLine :
         * 7. Audio Source :
         * 8. Mic Directional :
         * 9. Listening String :
         * 10. Reset to Silence and Listening... :
         */

        // STEP 1:
        mSttFunc = sttFunc;
        mVenusApp = VNConstant.View.TRANSCRIBE;
        if (STT_FUNC_TRANSCRIBE.equals(sttFunc)) {
            mVenusApp = VNConstant.View.TRANSCRIBE;
        } else if (STT_FUNC_TRANSLATE.equals(sttFunc)) {
            mVenusApp = VNConstant.View.TRANSLATE;
        }

        // STEP 2:
        String sttEngine = SttConfigManager.getInstance().getEngine();
        if (STT_ENGINE_AZURE.equals(sttEngine)) {
            mSttEngine = new AzureStt(mContext, sttFunc);
        } else if (STT_ENGINE_AZURE_WESTUS.equals(sttEngine)) {
            mSttEngine = new AzureWestUSStt(mContext, sttFunc);
        } else if (STT_ENGINE_AZURE_SWEDENCENTRAL.equals(sttEngine)) {
            mSttEngine = new AzureSwedenCentralStt(mContext, sttFunc);
        } else if (STT_ENGINE_AISPEECH.equals(sttEngine)) {
            mSttEngine = new AiSpeechStt(sttFunc);
        } else if (STT_ENGINE_IFLYTEK.equals(sttEngine)) {
            mSttEngine = new IFlyTekStt(sttFunc);
        } else if (STT_ENGINE_IFLYTEK_WEB_ASR.equals(sttEngine)) {
            mSttEngine = new IFlyTekWebAsrStt(sttFunc);
        } else if (STT_ENGINE_IFLYTEK_WEB_IAT_MUL.equals(sttEngine)) {
            mSttEngine = new IFlyTekWebIatMulStt(sttFunc);
        } else if (STT_ENGINE_MOCK.equals(sttEngine)) {
            mSttEngine = new MockStt(mContext, sttFunc);
        }

        // STEP 3:
        mSttEngine.setOnSttListener(onSttListener);

        // STEP 4:
        int textMode = (int) MmkvUtil.decode(MMKV_TEXT_MODE_KEY, TEXT_MODE_DEFAULT);
        int venusTextMode = VNConstant.SttConfig.TextMode.CURRENT_ONLY;
        if (textMode == TEXT_MODE_CURRENT_ONLY) {
            venusTextMode = VNConstant.SttConfig.TextMode.CURRENT_ONLY;
        } else if (textMode == TEXT_MODE_CURRENT_AND_HISTORICAL) {
            venusTextMode = VNConstant.SttConfig.TextMode.CURRENT_AND_HISTORICAL;
        }
        VNCommon.setTextMode(mVenusApp, venusTextMode, null);

        // STEP 5:
        int transShowMode = (int) MmkvUtil.decode(MMKV_TRANS_SHOW_MODE_KEY, TRANS_SHOW_MODE_DEFAULT);
        if (STT_FUNC_TRANSLATE.equals(sttFunc)) {
            VNCommon.setTransMode(mVenusApp, transShowMode, null);
        }

        // STEP 6:
        int simplified = (int) MmkvUtil.decode(MMKV_SIMPLIFIED_MODE_KEY, SIMPLIFIED_MODE_DEFAULT);
        if (simplified == SIMPLIFIED_MODE_ENABLED) {
            VNCommon.setMaxLine(mVenusApp, 3, null);
        } else {
            VNCommon.setMaxLine(mVenusApp, 6, null);
        }

        // STEP 7: 设置音频输入源
        mAudioInput = (int) MmkvUtil.decode(MMKV_AUDIO_INPUT_KEY, AUDIO_INPUT_DEFAULT);
        int venusAudioInput = VNConstant.SttConfig.AudioSource.GLASSES;
        if (mAudioInput == AUDIO_INPUT_SCO) {
            venusAudioInput = VNConstant.SttConfig.AudioSource.GLASSES;
        } else if (mAudioInput == AUDIO_INPUT_PHONE) {
            venusAudioInput = VNConstant.SttConfig.AudioSource.PHONE;
        }
        VNCommon.setAudioSource(mVenusApp, venusAudioInput, null);

        // STEP 8: 设置麦克风方向性
        int micDirectional = (int) MmkvUtil.decode(MMKV_MIC_DIRECTIONAL_KEY, MIC_DIRECTIONAL_DEFAULT);
        int venusMicDirectional = VNConstant.SttConfig.MicDirectional.OMNI;
        if (micDirectional == MIC_DIRECTIONAL_OMNI) {
            venusMicDirectional = VNConstant.SttConfig.MicDirectional.OMNI;
        } else if (micDirectional == MIC_DIRECTIONAL_FRONT) {
            venusMicDirectional = VNConstant.SttConfig.MicDirectional.FRONT;
        }
        VNCommon.setMicDirectional(mVenusApp, venusMicDirectional, null);

        // STEP 9:
        initListeningStr(sttFunc);

        // STEP 10:
        switchSilenceState(true);

        isAudioRecordEnabled = (int) MmkvUtil.decode(MMKV_AUDIO_RECORD_KEY, AUDIO_RECORD_DEFAULT);

        notifyOnWorkerInitComplete(sttFunc, mSttEngine.getName(), mAudioInput == AUDIO_INPUT_PHONE ? "phone" : "sco");

        LogUtil.i("mVenusApp[" + mVenusApp + "] "
                + "sttEngine[" + sttEngine + "] "
                + "textMode[" + textMode + "] "
                + "transShowMode[" + transShowMode + "] "
                + "simplified[" + simplified + "] "
                + "mAudioInput[" + mAudioInput + "]"
                + "micDirectional[" + micDirectional + "]"
                + "isAudioRecordEnabled[" + isAudioRecordEnabled + "]"
        );
    }

    public void startWork(String func) {
        LogUtil.i("==> startWork mCurState " + mCurState);
        if (mCurState != STATE_STOPPED) {
            LogUtil.i("==> startWork Ignore");
            return;
        }

        // Only reset retry count for non-retry calls
        mHandler.sendEmptyMessage(MSG_RESET_RETRY_COUNT);

        // Init first, then Sys message can be sent to device, and errMsg can be save to DB
        initConfig(func);

        startWorkInner();
    }

    /**
     *
     */
    private void startWorkInner() {
        LogUtil.i("==> startWorkInner mCurState " + mCurState);
        if (mCurState != STATE_STOPPED) {
            LogUtil.i("==> startWorkInner Ignore");
            return;
        }

        setWorkingState(STATE_STARTING);

        // STEP 1: TODO 根据会话类型判断是否需要联网
        if (!isNetworkConnected()) {
            notifyOnWorkerErr(STT_ERR_NETWORK_UNAVAILABLE, "");
            setWorkingState(STATE_STOPPED);
            // [异常终止][不需要重试] 提示用户网络不可用
            return;
        }

        // STEP 2:
        // 这里只检查权限并给出提示，不申请权限。原因：
        // -若由Device端发起，则有可能后台运行不能弹出权限申请框；
        // -若由App端发起，则由App端确保授予权限。
        if (!isSttPermissionGranted()) {
            notifyOnWorkerErr(STT_ERR_PERMISSION_NOT_GRANTED, "");
            setWorkingState(STATE_STOPPED);
            // [异常终止][不需要重试] 提示用户先授予权限
            return;
        }

        // STEP 3+ : in Thread
        new Thread(startWorkPreCheckRunnable).start();
    }

    private Runnable startWorkPreCheckRunnable = new Runnable() {
        @Override
        public void run() {
            LogUtil.i("Pre check");
            // STEP 3.1:
            if (AUDIO_INPUT_SCO == mAudioInput) {
                LogUtil.i("Pre check HFP");
                int headsetConnectState = mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
                if (headsetConnectState != BluetoothProfile.STATE_CONNECTED) {
                    notifyOnRecorderErr(STT_ERR_HFP_LOST, "");
                    setWorkingState(STATE_STOPPED);
                    // [异常终止][需要重试] HFP可能由于设备刚开机连接未就绪，这里需要重试
                    handleRetry();
                    return;
                }

                boolean myHfpConnected = isHeadsetProfileConnected(DeviceManager.getInstance().getBoundDevice());
                if (!myHfpConnected) {
                    notifyOnRecorderErr(STT_ERR_HFP_MISMATCH, "");
                    setWorkingState(STATE_STOPPED);
                    // [异常终止][不需要重试] HFP连接在别的蓝牙设备上，需提示用户介入处理
                    return;
                }
            }

            // STEP 3.2:
            LogUtil.i("Pre check Service Key");

            // TODO
            mSttEngine.initParam("");

            // STEP 4+ : in startWorkNow()
            mHandler.sendEmptyMessage(MSG_START_WORK_NOW);
        }
    };

    private void startWorkNow() {
        // STEP 4: FIXME here or after Record Started?
        //setWorkingState(STATE_STARTED);

        // STEP 5:
        mSttEngine.connect();

        // STEP 6: startRecordAudio after SttEngine connected
    }

    public void stopWork() {
        LogUtil.i("==> stopWork mCurState " + mCurState);
        // 清空重试队列
        mHandler.removeMessages(MSG_START_RETRY);
        // 重置重试次数
        mHandler.sendEmptyMessage(MSG_RESET_RETRY_COUNT);

        stopWorkInner();
    }

    private void stopWorkInner() {
        // 由于启动录音指令是延迟执行，这里需要移除队列中的启动命令
        mHandler.removeMessages(MSG_START_RECORDER);

        LogUtil.i("==> stopWorkInner mCurState " + mCurState);
        if (mCurState == STATE_STOPPED) {
            LogUtil.i("==> stopWorkInner already stopped");
            return;
        }

        setWorkingState(STATE_STOPPING);
        stopRecordAudio();

        if (null != mSttEngine) {
            // FIXME New Thread to disconnect engine.
            mSttEngine.disconnect();
        }

        setWorkingState(STATE_STOPPED);
    }

    private void handleRetry() {
        mHandler.removeMessages(MSG_RESET_RETRY_COUNT);

        if (mRetryCount < MAX_RETRY_COUNT) {
            mRetryCount++;
            LogUtil.i("Retry attempt " + mRetryCount + " of " + MAX_RETRY_COUNT);
            mHandler.sendEmptyMessageDelayed(MSG_START_RETRY, RETRY_DELAY_MS);

            String loadingStr = mContext.getString(R.string.tips_stt_err_loading, mRetryCount, MAX_RETRY_COUNT);
            notifyOnSysMessage(0, loadingStr);
            sendSysMessageToDevice(loadingStr);
        } else {
            mHandler.sendEmptyMessage(MSG_RESET_RETRY_COUNT);
            LogUtil.i("Max retry attempts reached");
        }
    }

    private void resetRetryCount() {
        LogUtil.mark();
        mRetryCount = 0;
    }

    public void setWorkingState(int state) {
        LogUtil.i("==> " + state);
        mCurState = state;
        switch (state) {
            case STATE_STOPPED:
                notifyOnWorkerStop();
                break;
            case STATE_STOPPING:
                notifyOnWorkerStopping();
                break;
            case STATE_STARTING:
                notifyOnWorkerStarting();
                break;
            case STATE_STARTED:
                notifyOnWorkerStart();
                break;
            default:
        }
    }

    public boolean isBusy() {
        boolean isBusy = mCurState != STATE_STOPPED;
        LogUtil.i("isBusy " + isBusy);
        return isBusy;
    }

    public int getCurState() {
        return mCurState;
    }

    public String getWorkingFunc() {
        return mSttFunc;
    }

    private boolean isSilence = false;

    private void switchSilenceState(boolean silence) {
        if (isSilence != silence) {
            isSilence = silence;
            notifyOnAudioTrackStateChanged(silence);
        }
    }

    private SttEngine.OnSttListener onSttListener = new SttEngine.OnSttListener() {
        @Override
        public void onConnect(String sid) {
            LogUtil.mark();

            notifyOnEngineStart();
            initAudioInput();
            // delay wait for sco switch, receive SCO_AUDIO_STATE_CONNECTED
            mHandler.sendEmptyMessageDelayed(MSG_START_RECORDER, 800);
        }

        @Override
        public void onError(int code, String msg) {
            notifyOnEngineErr(code, msg);
            stopWorkInner();

            // 需要根据错误原因：
            if (mSttEngine.shouldRetry(code)) {
                //  [异常终止][需要重试] 如网络波动等问题
                handleRetry();
            } else {
                //  [异常终止][不需要重试] 提示用户服务不可用，切换到可用引擎
            }
        }

        @Override
        public void onMessage(long segmentId, String speakerId, int type, String transcribeStr, String translateStr) {
            LogUtil.i("==> segmentId:" + segmentId + " transcribeStr:" + transcribeStr + " translateStr:" + translateStr);

            int updateUiType = MSG_TYPE_NEW;
            if (segmentId > lastSegmentId) {
                updateUiType = MSG_TYPE_NEW;
                lastSegmentId = segmentId;
            } else if (segmentId == lastSegmentId) {
                updateUiType = MSG_TYPE_UPDATE;
            } else {
                //FIXME 处理前一条segment的更新
                return;
            }
            LogUtil.i("==> updateUiType:" + (updateUiType == MSG_TYPE_NEW ? "NEW" : "UPDATE"));

            switchSilenceState(false);
            mHandler.removeMessages(MSG_SILENCE);
            mHandler.sendEmptyMessageDelayed(MSG_SILENCE, SILENCE_TIMEOUT);

            notifyOnSttMessage(updateUiType, transcribeStr, translateStr, STT_MSG_TYPE_REC == type);
        }
    };

    public boolean isRecording() {
        return isRecording;
    }

    private boolean isAudioRecordEnabled() {
        return isAudioRecordEnabled == AUDIO_RECORD_ENABLED;
    }

    public void startRecordAudio() {
        LogUtil.mark();

        File pcmFile;
        if (isAudioRecordEnabled()) {
            pcmFile = new File(mContext.getExternalFilesDir("VenusPCM"), UUID.randomUUID() + ".pcm");
        } else {
            pcmFile = new File(mContext.getExternalFilesDir("VenusTemp"), "temp.pcm");
        }

        try {
            // 获取最小录音缓存大小，
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_HZ, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);

            // 开始录音
            isRecording = true;
            mAudioRecord.startRecording();

            // 创建数据流，将缓存导入数据流
            mRecordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        pcmFile.deleteOnExit();
                        pcmFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(pcmFile);
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
                                    mSttEngine.send(data);
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

            mRecordingThread.start();
            if (isAudioRecordEnabled()) {
                notifyOnRecorderStart(pcmFile.getPath(), pcmFile.getName());
            }

            setWorkingState(STATE_STARTED);
            notifyOnHintMessage();

            /*
             * 如Azure引擎，mSttEngine.connect()时会先收到onConnect()进而启动recorder，而后才收到onError()，所以导致每次失败后都执行了resetRetryCount。
             * 这里修改为当稳定开启（如15s后），再重置重试次数，下次发生错误时进入新的计次周期。
             * 当触发重试时代表当前重试周期未结束，应该移除此消息。
             */
            mHandler.sendEmptyMessageDelayed(MSG_RESET_RETRY_COUNT, 1000 * 15);

        } catch (IllegalArgumentException e) {
            LogUtil.w("构造参数异常");
            e.printStackTrace();
            notifyOnRecorderErr(STT_ERR_RECORDER_ILLEGAL_ARGUMENT, e.getMessage());
            stopWorkInner();
            // [异常终止][不需要重试] 提示用户服务不可用，构造参数异常
        } catch (IllegalStateException e) {
            LogUtil.w("录音状态机异常");
            e.printStackTrace();
            notifyOnRecorderErr(STT_ERR_RECORDER_ILLEGAL_STATE, e.getMessage());
            stopWorkInner();
            // [异常终止][不需要重试] 提示用户服务不可用，音频状态异常
        } catch (SecurityException e) {
            LogUtil.w("录音权限异常");
            e.printStackTrace();
            notifyOnRecorderErr(STT_ERR_RECORDER_PERMISSION_NOT_GRANTED, e.getMessage());
            stopWorkInner();
            // [异常终止][不需要重试] 提示用户服务不可用，权限未申请或用户动态拒绝了权限
        }
    }

    private void stopRecordAudio() {
        LogUtil.mark();
        try {
            isRecording = false;
            if (mAudioRecord != null) {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }

            if (null != mRecordingThread) {
                mRecordingThread.interrupt();
                mRecordingThread = null;
            }
        } catch (Exception e) {
            LogUtil.mark();
            LogUtil.w(e.getLocalizedMessage());
        }

        disableSco();
    }

    private void initAudioInput() {
        LogUtil.i("initAudioInput SCO = " + (AUDIO_INPUT_SCO == mAudioInput));
        if (AUDIO_INPUT_SCO == mAudioInput) {
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

    private boolean isSttPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void sendSttMessageToDevice(int type, String transcribeStr, String translateStr) {
        VNSttInfo sttInfo = new VNSttInfo();
        sttInfo.setTranscribe(transcribeStr);
        sttInfo.setTranslate(translateStr);
        sttInfo.setActionType(type);
        sttInfo.setMsgType(VNConstant.SttInfo.MsgType.STT);
        sttInfo.setCreatedAt(System.currentTimeMillis() / 1000);

        if (VNConstant.View.TRANSCRIBE == mVenusApp) {
            VNCommon.updateTranscribe(sttInfo, null);
        } else if (VNConstant.View.TRANSLATE == mVenusApp) {
            VNCommon.updateTranslate(sttInfo, null);
        }
    }

    private void sendSysMessageToDevice(String msg) {
        VNSttInfo sttInfo = new VNSttInfo();
        sttInfo.setActionType(VNConstant.SttInfo.ActionType.NEW);
        sttInfo.setMsgType(VNConstant.SttInfo.MsgType.SYS);
        sttInfo.setTranscribe(msg);
        sttInfo.setCreatedAt(System.currentTimeMillis() / 1000);

        if (VNConstant.View.TRANSCRIBE == mVenusApp) {
            VNCommon.updateTranscribe(sttInfo, null);
        } else if (VNConstant.View.TRANSLATE == mVenusApp) {
            VNCommon.updateTranslate(sttInfo, null);
        }
    }

    private void sendHintMessageToDevice(String transcribeHintStr, String translateHintStr) {
        VNSttInfo sttInfo = new VNSttInfo();
        sttInfo.setActionType(VNConstant.SttInfo.ActionType.NEW);
        sttInfo.setMsgType(VNConstant.SttInfo.MsgType.HINT);
        sttInfo.setTranscribe(transcribeHintStr);
        sttInfo.setTranslate(translateHintStr);
        sttInfo.setCreatedAt(System.currentTimeMillis() / 1000);

        if (VNConstant.View.TRANSCRIBE == mVenusApp) {
            VNCommon.updateTranscribe(sttInfo, null);
        } else if (VNConstant.View.TRANSLATE == mVenusApp) {
            VNCommon.updateTranslate(sttInfo, null);
        }
    }

    private void initListeningStr(String func) {
        if (STT_FUNC_TRANSCRIBE.equals(func)) {
            LanguageTag transcribeSource = SttConfigManager.getInstance().getSourceLanTag(
                    SttConfigManager.getInstance().getEngine(),
                    STT_FUNC_TRANSCRIBE
            );
            if (null != transcribeSource) {
                STR_LISTENING_TRANSCRIBE = transcribeSource.getListeningStr();
            }
            STR_LISTENING_TRANSLATE = "";

        } else if (STT_FUNC_TRANSLATE.equals(func)) {
            LanguageTag translateSource = SttConfigManager.getInstance().getSourceLanTag(
                    SttConfigManager.getInstance().getEngine(),
                    STT_FUNC_TRANSLATE
            );
            if (null != translateSource) {
                STR_LISTENING_TRANSCRIBE = translateSource.getListeningStr();

                LanguageTag translateTarget = SttConfigManager.getInstance().getTargetLanTag(
                        SttConfigManager.getInstance().getEngine(),
                        STT_FUNC_TRANSLATE,
                        translateSource.getTag()
                );
                if (null != translateTarget) {
                    STR_LISTENING_TRANSLATE = translateTarget.getListeningStr();
                }
            }
        }
    }

    List<SttWorkerCallback> sttWorkerCallbacks = new ArrayList<>();

    public void addSttWorkerCallback(SttWorkerCallback callback) {
        synchronized (sttWorkerCallbacks) {
            if (!sttWorkerCallbacks.contains(callback)) {
                sttWorkerCallbacks.add(callback);
            }
        }
    }

    public void removeSttWorkerCallback(SttWorkerCallback callback) {
        synchronized (sttWorkerCallbacks) {
            if (sttWorkerCallbacks.contains(callback)) {
                sttWorkerCallbacks.remove(callback);
            }
        }
    }

    private void notifyOnWorkerInitComplete(String funcType, String engineType, String audioSource) {
        LogUtil.mark();
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onWorkerInitComplete(funcType, engineType, audioSource);
            }
        }
    }

    private void notifyOnWorkerStarting() {
        LogUtil.mark();
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onWorkerStarting();
            }
        }
    }

    private void notifyOnWorkerStart() {
        LogUtil.mark();
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onWorkerStart();
            }
        }
    }

    private void notifyOnWorkerStopping() {
        LogUtil.mark();
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onWorkerStopping();
            }
        }
    }

    private void notifyOnWorkerStop() {
        LogUtil.mark();
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onWorkerStop();
            }
        }
    }

    private void notifyOnWorkerErr(int code, String cause) {
        LogUtil.i("[" + code + "]" + cause);
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onWorkerErr(code, getErrPrompt(code), cause);
            }
        }
        sendSysMessageToDevice("[" + code + "]" + getErrPrompt(code));
    }

    private void notifyOnEngineStart() {
        LogUtil.mark();
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onEngineStart();
            }
        }
    }

    private void notifyOnEngineStop() {
        LogUtil.mark();
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onEngineStop();
            }
        }
    }

    private void notifyOnEngineErr(int code, String cause) {
        LogUtil.i("[" + code + "]" + cause);
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onEngineErr(code, mContext.getString(R.string.tips_stt_engine_err, code), cause);
            }
        }
        sendSysMessageToDevice(mContext.getString(R.string.tips_stt_engine_err, code));
    }

    private void notifyOnEngineTick(long time) {
        LogUtil.mark();
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onEngineTick(time);
            }
        }
    }

    private void notifyOnRecorderStart(String audioFilePath, String audioFileName) {
        LogUtil.mark();
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onRecorderStart(audioFilePath, audioFileName);
            }
        }
    }

    private void notifyOnRecorderStop() {
        LogUtil.mark();
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onRecorderStop();
            }
        }
    }

    private void notifyOnRecorderErr(int code, String cause) {
        LogUtil.i("[" + code + "]" + cause);
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onRecorderErr(code, getErrPrompt(code), cause);
            }
        }
        sendSysMessageToDevice("[" + code + "]" + getErrPrompt(code));
    }

    private void notifyOnSttMessage(int type, String transcribeStr, String translateStr, boolean isEnd) {
        LogUtil.mark();

        // UI and DB
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onSttMessage(type, transcribeStr, translateStr, isEnd);
            }
        }

        // Bt Device
        sendSttMessageToDevice(type, transcribeStr, translateStr);
    }

    private void notifyOnSysMessage(int level, String msg) {
        LogUtil.mark();
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onSysMessage(level, msg);
            }
        }
    }

    private void notifyOnHintMessage() {
        LogUtil.mark();
        if (STATE_STARTED != mCurState) {
            return;
        }

        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                if (VNConstant.View.TRANSCRIBE == mVenusApp) {
                    listener.onHintMessage(STR_LISTENING_TRANSCRIBE, "");
                } else if (VNConstant.View.TRANSLATE == mVenusApp) {
                    listener.onHintMessage(STR_LISTENING_TRANSCRIBE, STR_LISTENING_TRANSLATE);
                }
            }
        }

        sendHintMessageToDevice(STR_LISTENING_TRANSCRIBE, STR_LISTENING_TRANSLATE);
    }

    private void notifyOnAudioTrackStateChanged(boolean silence) {
        LogUtil.mark();
        for (SttWorkerCallback listener : sttWorkerCallbacks) {
            if (null != listener) {
                listener.onAudioTrackStateChanged(silence);
            }
        }

        if (silence) {
            VNCommon.setAudioTrackState(mVenusApp, VNConstant.AudioTracker.SILENCE, null);
            notifyOnHintMessage();
        } else {
            VNCommon.setAudioTrackState(mVenusApp, VNConstant.AudioTracker.SPEAKING, null);
        }
    }

    private boolean isNetworkConnected() {
        boolean connected = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            NetworkCapabilities networkCapabilities =
                    mConnectivityManager.getNetworkCapabilities(mConnectivityManager.getActiveNetwork());
            if (networkCapabilities != null) {
                connected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            }
        } else {
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    connected = true;
                } else {
                    connected = false;
                }
            }
        }
        return connected;
    }

    private DeviceManager.DeviceServiceListener serviceListener = new DeviceManager.DeviceServiceListener() {
        @Override
        public void onStateChanged(int sysState) {
            if (!VNCommon.isConnected()) {
                // FIXME and STATE_STARTING also?
                if (STATE_STARTED == mCurState) {
                    notifyOnWorkerErr(STT_ERR_DEVICE_CONNECTION_LOST, "");
                    stopWork();
                }
            }
        }
    };

    /**
     * Handle headset and Sco audio connection states.
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @SuppressWarnings({"deprecation", "synthetic-access"})
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED)) {
                LogUtil.d("ACTION_ACL_CONNECTED");

                BluetoothDevice mConnectedHeadset = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                LogUtil.d(mConnectedHeadset.getName() + " connected");

                BluetoothClass bluetoothClass = mConnectedHeadset.getBluetoothClass();
                if (bluetoothClass != null) {
                    LogUtil.d("ACTION_ACL_CONNECTED bluetoothClass=" + bluetoothClass);
                    // Check if device is a headset. Besides the 2 below, are there other
                    // device classes also qualified as headset?
                    int deviceClass = bluetoothClass.getDeviceClass();
                    if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
                            || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET) {

                        LogUtil.d("ACTION_ACL_CONNECTED deviceClass=" + deviceClass);

                        // override this if you want to do other thing when the device is connected.
                        //onHeadsetConnected();
                    }
                }
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                LogUtil.d("ACTION_ACL_DISCONNECTED");

                // override this if you want to do other thing when the device is disconnected.
                //onHeadsetDisconnected();
            } else if (action.equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR);

                switch (state) {
                    case AudioManager.SCO_AUDIO_STATE_ERROR:
                        LogUtil.i("SCO_AUDIO_STATE_ERROR");
                        break;
                    case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                        LogUtil.i("SCO_AUDIO_STATE_DISCONNECTED");

                        // Need to call stopBluetoothSco(), otherwise startBluetoothSco()
                        // will not be successful.
                        //mAudioManager.stopBluetoothSco();
                        //mAudioManager.setBluetoothScoOn(false);

                        // override this if you want to do other thing when Sco audio is disconnected.
                        //onScoAudioDisconnected();
                        break;
                    case AudioManager.SCO_AUDIO_STATE_CONNECTED:
                        LogUtil.i("SCO_AUDIO_STATE_CONNECTED");

                        // When the device is connected before the application starts,
                        // ACTION_ACL_CONNECTED will not be received, so call onHeadsetConnected here
                        //onHeadsetConnected();

                        // override this if you want to do other thing when Sco audio is connected.
                        //onScoAudioConnected();
                        break;
                    case AudioManager.SCO_AUDIO_STATE_CONNECTING:
                        LogUtil.i("SCO_AUDIO_STATE_CONNECTING");
                        break;
                }
            }
        }
    };

    public boolean isHeadsetProfileConnected(BluetoothDevice device) {
        final boolean[] connected = {false};

        CountDownLatch latch = new CountDownLatch(1);
        BluetoothProfile.ServiceListener serviceListener = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HEADSET) {
                    List<BluetoothDevice> connectedDevices = proxy.getConnectedDevices();
                    for (BluetoothDevice dev : connectedDevices) {
                        LogUtil.i("Connected HFP device: " + dev.getName() + " - " + dev.getAddress());
                        if (dev.getAddress().equals(device.getAddress())) {
                            connected[0] = true;
                            break;
                        }
                    }
                }
                mBluetoothAdapter.closeProfileProxy(profile, proxy);
                latch.countDown();
            }

            @Override
            public void onServiceDisconnected(int profile) {
                // TODO Result of call closeProfileProxy
            }
        };

        // 注册服务监听器，开始异步获取代理
        LogUtil.mark();
        boolean success = mBluetoothAdapter.getProfileProxy(mContext, serviceListener, BluetoothProfile.HEADSET);

        if (!success) {
            // 如果注册监听器失败，直接返回null
            return connected[0];
        }

        try {
            boolean awaitResult = latch.await(3, TimeUnit.SECONDS);
            if (awaitResult) { // countdown to 0
                LogUtil.mark();
            } else { // timeout
                LogUtil.mark();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 如果等待过程中线程被中断，也返回null
            Thread.currentThread().interrupt(); // 保留中断状态
        } finally {
            LogUtil.mark();
        }
        return connected[0];
    }

    private final int STT_ERR_DEVICE_CONNECTION_LOST = 1001001;
    private final int STT_ERR_NETWORK_UNAVAILABLE = 1001002;
    private final int STT_ERR_SERVER_REQ_TIMEOUT = 1001003;
    private final int STT_ERR_SERVER_AUTH_FAILED = 1001401;
    private final int STT_ERR_SERVER_REQ_FAILED = 1001005;

    private final int STT_ERR_PERMISSION_NOT_GRANTED = 1002001;

    private final int STT_ERR_HFP_LOST = 1003001;
    private final int STT_ERR_HFP_MISMATCH = 1003002;

    private final int STT_ERR_KEY_NOT_FOUND = 1004002;
    private final int STT_ERR_KEY_EMPTY = 1004003;
    private final int STT_ERR_KEY_PARSER = 1004004;

    private final int STT_ERR_RECORDER_ILLEGAL_ARGUMENT = 1005001;
    private final int STT_ERR_RECORDER_ILLEGAL_STATE = 1005002;
    private final int STT_ERR_RECORDER_PERMISSION_NOT_GRANTED = 1005003;

    private String getErrPrompt(int code) {
        String msg;
        switch (code) {
            case STT_ERR_DEVICE_CONNECTION_LOST:
                msg = mContext.getString(R.string.tips_stt_err_device_connection_lost);
                break;
            case STT_ERR_NETWORK_UNAVAILABLE:
                msg = mContext.getString(R.string.tips_stt_err_no_network_connection);
                break;
            case STT_ERR_SERVER_REQ_TIMEOUT:
                msg = mContext.getString(R.string.tips_stt_err_service_req_timeout);
                break;
            case STT_ERR_SERVER_AUTH_FAILED:
                msg = mContext.getString(R.string.tips_stt_err_service_auth_failed);
                break;
            case STT_ERR_SERVER_REQ_FAILED:
                msg = mContext.getString(R.string.tips_stt_err_service_req_failed);
                break;
            case STT_ERR_PERMISSION_NOT_GRANTED:
                msg = mContext.getString(R.string.tips_stt_err_permission_not_granted);
                break;
            case STT_ERR_HFP_LOST:
                msg = mContext.getString(R.string.tips_stt_err_hfp_lost);
                break;
            case STT_ERR_HFP_MISMATCH:
                msg = mContext.getString(R.string.tips_stt_err_hfp_mismatch);
                break;
            case STT_ERR_KEY_NOT_FOUND:
                msg = mContext.getString(R.string.tips_stt_err_key_not_found);
                break;
            case STT_ERR_KEY_EMPTY:
                msg = mContext.getString(R.string.tips_stt_err_key_empty);
                break;
            case STT_ERR_KEY_PARSER:
                msg = mContext.getString(R.string.tips_stt_err_key_parser);
                break;
            case STT_ERR_RECORDER_ILLEGAL_ARGUMENT:
                msg = mContext.getString(R.string.tips_stt_err_recorder_illegal_arg);
                break;
            case STT_ERR_RECORDER_ILLEGAL_STATE:
                msg = mContext.getString(R.string.tips_stt_err_recorder_illegal_state);
                break;
            case STT_ERR_RECORDER_PERMISSION_NOT_GRANTED:
                msg = mContext.getString(R.string.tips_stt_err_permission_not_granted);
                break;
            default:
                msg = mContext.getString(R.string.tips_stt_err_unknown);
        }
        return msg;
    }
}
