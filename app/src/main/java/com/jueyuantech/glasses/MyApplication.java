package com.jueyuantech.glasses;

import static com.jueyuantech.glasses.common.Constants.AGREEMENT_ACCEPTED;
import static com.jueyuantech.glasses.common.Constants.AGREEMENT_ACCEPT_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.AGREEMENT_UNACCEPTED;
import static com.jueyuantech.glasses.common.Constants.APP_VERSION_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.HEARTBEAT_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.HEARTBEAT_ENABLED;
import static com.jueyuantech.glasses.common.Constants.MMKV_AGREEMENT_ACCEPT_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_APP_VERSION_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_HEARTBEAT_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_PROTOCOL_VER_KEY;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.jueyuantech.glasses.common.Constants;
import com.jueyuantech.glasses.db.DBWorker;
import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.stt.SttWorker;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechUtility;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNOptions;
import com.jueyuantech.venussdk.VNCommon;
import com.tencent.mmkv.MMKV;

import java.io.File;

public class MyApplication extends Application {

    private static Context mContext;

    public MyApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        String rootDir = MMKV.initialize(this);
        MmkvUtil.getInstance();
        //LogUtil.i("MMKV root: " + rootDir);
    }

    public static Context getInstance() {
        return mContext;
    }

    public static void initApp() {
        initExternalFilesDir();
        checkAppVersion();

        int heartbeat = (int) MmkvUtil.decode(MMKV_HEARTBEAT_KEY, HEARTBEAT_DEFAULT);
        VNOptions options = new VNOptions();
        //options.setAppId("");
        options.setHeartbeatEnabled(heartbeat == HEARTBEAT_ENABLED);
        VNCommon.init(mContext, options);

        DeviceManager.init(mContext);
        UserManager.init(mContext);
        SttWorker.init(mContext);
        DBWorker.init(mContext);
        NotificationPushManager.getInstance().init(mContext);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(new Intent(mContext, HomeService.class));
        } else {
            mContext.startService(new Intent(mContext, HomeService.class));
        }
    }

    private static void initExternalFilesDir() {
        File imageFilesDir = mContext.getExternalFilesDir("VenusImage");
        if (!imageFilesDir.exists()) {
            imageFilesDir.mkdir();
        }
        File otaFilesDir = mContext.getExternalFilesDir("VenusOTA");
        if (!otaFilesDir.exists()) {
            otaFilesDir.mkdir();
        }
        File ftpFilesDir = mContext.getExternalFilesDir("VenusFTP");
        if (!ftpFilesDir.exists()) {
            ftpFilesDir.mkdir();
        }
        File mockFilesDir = mContext.getExternalFilesDir("VenusMock");
        if (!mockFilesDir.exists()) {
            mockFilesDir.mkdir();
        }
    }

    private static boolean mscInitialize = false;

    public static void initializeMsc(Context context, String appId) {
        if (mscInitialize) return;
        // 应用程序入口处调用，避免手机内存过小，杀死后台进程后通过历史intent进入Activity造成SpeechUtility对象为null
        // 如在Application中调用初始化，需要在Mainifest中注册该Applicaiton
        // 注意：此接口在非主进程调用会返回null对象，如需在非主进程使用语音功能，请增加参数：SpeechConstant.FORCE_LOGIN+"=true"
        // 参数间使用半角“,”分隔。
        // 设置你申请的应用appid,请勿在'='与appid之间添加空格及空转义符

        // 注意： appid 必须和下载的SDK保持一致，否则会出现10407错误

        SpeechUtility.createUtility(context, "appid=" + appId);

        // 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
        Setting.setShowLog(true);
        mscInitialize = true;
    }

    public static boolean isAgreementAccepted() {
        return AGREEMENT_ACCEPTED == (int) MmkvUtil.decode(MMKV_AGREEMENT_ACCEPT_KEY, AGREEMENT_ACCEPT_DEFAULT);
    }

    public static void acceptAgreement() {
        MmkvUtil.encode(MMKV_AGREEMENT_ACCEPT_KEY, AGREEMENT_ACCEPTED);
    }

    public static void withdrawAgreement() {
        MmkvUtil.encode(MMKV_AGREEMENT_ACCEPT_KEY, AGREEMENT_UNACCEPTED);
    }

    private static void checkAppVersion() {
        int oldVersion = (int) MmkvUtil.decode(MMKV_APP_VERSION_KEY, APP_VERSION_DEFAULT);
        int currentVersion = BuildConfig.VERSION_CODE;

        // 只处理从低版本升级的情况
        if (oldVersion > 0 && oldVersion < currentVersion) {
            // 检查升级过程中是否经过了特定的目标版本
            if (oldVersion < 3 && currentVersion >= 3) {
                performUpgradeToVersion3();
            }
            if (oldVersion < 4 && currentVersion >= 4) {
                performUpgradeToVersion3();
            }
        }

        // 保存当前版本号
        MmkvUtil.encode(MMKV_APP_VERSION_KEY, currentVersion);
    }

    private static void performUpgradeToVersion3() {
        MmkvUtil.getInstance().removeKey(Constants.MMKV_STT_LANGUAGE_CONFIG_KEY);
    }
}
