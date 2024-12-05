package com.jueyuantech.glasses;

import static com.jueyuantech.glasses.common.Constants.HEARTBEAT_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.HEARTBEAT_ENABLED;
import static com.jueyuantech.glasses.common.Constants.MMKV_HEARTBEAT_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_PROTOCOL_VER_KEY;

import android.app.Application;
import android.content.Context;

import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechUtility;
import com.jueyuantech.venussdk.VenusConstant;
import com.jueyuantech.venussdk.VenusOptions;
import com.jueyuantech.venussdk.VenusSDK;
import com.tencent.mmkv.MMKV;

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
        LogUtil.i("MMKV root: " + rootDir);

        NotificationPushManager.getInstance().init(this);

        String protocolVer = MmkvUtil.getInstance().decodeString(MMKV_PROTOCOL_VER_KEY, VenusConstant.Protocol.VER_2);
        int heartbeat = (int) MmkvUtil.decode(MMKV_HEARTBEAT_KEY, HEARTBEAT_DEFAULT);
        VenusOptions options = new VenusOptions();
        options.setAppId("");
        options.setProtocolVer(protocolVer);
        options.setHeartbeatEnable(heartbeat == HEARTBEAT_ENABLED);
        VenusSDK.init(this, options);

        DeviceManager.init(mContext);
    }

    public static Context getInstance() {
        return mContext;
    }

    private static boolean mscInitialize = false;
    public static void initializeMsc(Context context, String appId){
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
}
