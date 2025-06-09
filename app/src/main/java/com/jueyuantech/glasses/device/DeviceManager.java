package com.jueyuantech.glasses.device;

import static com.jueyuantech.glasses.common.Constants.MMKV_STT_FUNC_KEY;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;
import static com.jueyuantech.venussdk.VNConstant.ServiceState.CONNECT_ERROR;
import static com.jueyuantech.venussdk.VNConstant.ServiceState.CONNECT_SUCCESS;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.amap.api.maps.MapsInitializer;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.jueyuantech.glasses.PrompterActivity;
import com.jueyuantech.glasses.RecorderActivity;
import com.jueyuantech.glasses.amap.NaviActivity;
import com.jueyuantech.glasses.stt.SttWorker;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.bean.VNDeviceInfo;
import com.jueyuantech.venussdk.bean.VNSystemStatus;
import com.jueyuantech.venussdk.cb.VNDeviceInfoCallBack;
import com.jueyuantech.venussdk.cb.VNSystemStatusCallBack;
import com.jueyuantech.venussdk.listener.VNDeviceServiceListener;
import com.jueyuantech.venussdk.listener.VNDeviceStatusListener;
import com.jueyuantech.venussdk.listener.VNDeviceUnsolicitedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceManager {
    private static volatile DeviceManager singleton;
    private Context mContext;

    private static final String MMKV_KEY_BOUNDED_DEVICE = "boundedDevice";
    private static final String MMKV_KEY_BOUNDED_DEVICE_INFO = "boundedDeviceInfo";
    private UUID deviceUUID = null;
    private BluetoothDevice boundedDevice = null;

    private int mCurVenusDeviceConnectState = CONNECT_ERROR;
    private int mCurSysState = -1;
    private int mCurChargeState = 0;
    private int mCurBattery = -1;

    private boolean autoConnect = true;

    private static final int MSG_NOTIFY_SYS_STATE_CHANGED = 1;
    private static final int MSG_NOTIFY_CHARGE_STATE_CHANGED = 2;
    private static final int MSG_NOTIFY_BATTERY_STATE_CHANGED = 3;
    private static final int MSG_NOTIFY_SERVICE_STATE_CHANGED = 4;
    private static final int MSG_CONNECT = 100;
    private static final int MSG_FETCH_SYSTEM_STATUS = 101;
    private static final int MSG_FETCH_DEVICE_INFO = 102;

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_NOTIFY_SYS_STATE_CHANGED:
                    for (DeviceStatusListener listener : deviceStatusListeners) {
                        listener.onSysStateChanged(mCurSysState);
                    }
                    break;
                case MSG_NOTIFY_CHARGE_STATE_CHANGED:
                    for (DeviceStatusListener listener : deviceStatusListeners) {
                        listener.onChargeStateChanged(mCurChargeState);
                    }
                    break;
                case MSG_NOTIFY_BATTERY_STATE_CHANGED:
                    for (DeviceStatusListener listener : deviceStatusListeners) {
                        listener.onBatteryChanged(mCurBattery);
                    }
                    break;
                case MSG_NOTIFY_SERVICE_STATE_CHANGED:
                    for (DeviceServiceListener listener : deviceServiceListeners) {
                        listener.onStateChanged(mCurVenusDeviceConnectState);
                    }
                    break;
                case MSG_CONNECT:
                    connect();
                    break;
                case MSG_FETCH_SYSTEM_STATUS:
                    fetchSystemStatus();
                    break;
                case MSG_FETCH_DEVICE_INFO:
                    fetchDeviceInfoIfNull();
                    break;
                default:
            }
        }
    };

    private DeviceManager(Context context) {
        mContext = context.getApplicationContext();
        initDevice();

        VNCommon.setDeviceServiceListener(venusDeviceServiceListener);
        VNCommon.setDeviceStatusListener(venusDeviceStatusListener);
        VNCommon.setDeviceUnsolicitedListener(venusDeviceUnsolicitedListener);
    }

    public static DeviceManager getInstance() {
        if (singleton == null) {
            throw new IllegalStateException("DeviceManager is not initialized. Call init() before getInstance().");
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
            synchronized (DeviceManager.class) {
                if (singleton == null) {
                    singleton = new DeviceManager(context);
                }
            }
        }
    }

    boolean isInitAlready = false;

    public void initDevice() {
        if (!isBound()) {
            return;
        }

        if (!isPermissionGranted()) {
            return;
        }

        if (isInitAlready) {
            return;
        }

        boundedDevice = getBoundDevice();
        VNCommon.initDevice(boundedDevice);
        if (!handler.hasMessages(MSG_CONNECT)) {
            handler.sendEmptyMessage(MSG_CONNECT);
        }
        isInitAlready = true;
    }

    private boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public boolean isBound() {
        return MmkvUtil.getInstance().have(MMKV_KEY_BOUNDED_DEVICE);
    }

    public void setBoundDevice(BluetoothDevice device) {
        MmkvUtil.getInstance().encodeParcelable(MMKV_KEY_BOUNDED_DEVICE, device);
        initDevice();
        handler.sendEmptyMessage(MSG_FETCH_DEVICE_INFO);
    }

    public BluetoothDevice getBoundDevice() {
        return (BluetoothDevice) MmkvUtil.getInstance().decodeParcelable(MMKV_KEY_BOUNDED_DEVICE, BluetoothDevice.class);
    }

    public void removeBoundDevice() {
        MmkvUtil.getInstance().removeKey(MMKV_KEY_BOUNDED_DEVICE);
        removeDeviceInfo();
        isInitAlready = false;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
        if (autoConnect) {
            if (!handler.hasMessages(MSG_CONNECT)) {
                handler.sendEmptyMessageDelayed(MSG_CONNECT, 1000 * 5);
            }
        } else {
            handler.removeMessages(MSG_CONNECT);
        }
    }

    private void connect() {
        LogUtil.mark();
        if (isBound()) {
            if (VNCommon.isConnected()) {

            } else {
                boundedDevice = getBoundDevice();
                VNCommon.connect(boundedDevice);
            }
        }
    }

    private void resetStatus() {
        mCurVenusDeviceConnectState = CONNECT_ERROR;
        mCurSysState = 0;
        mCurChargeState = 0;
        mCurBattery = -1;
    }

    public int getSysState() {
        return mCurSysState;
    }

    public int getChargeState() {
        return mCurChargeState;
    }

    public int getBattery() {
        return mCurBattery;
    }

    /**
     * 每当设备连接成功，都重新获取一次系统状态，直至获取成功。
     * 后续的系统状态通过眼镜上报获取。
     */
    private void fetchSystemStatus() {
        VNCommon.getSystemStatus(new VNSystemStatusCallBack() {
            @Override
            public void onSuccess(VNSystemStatus systemStatus) {
                LogUtil.i("fetchSystemStatus succeed");
                mCurSysState = systemStatus.getSysState();
                handler.sendEmptyMessage(MSG_NOTIFY_SYS_STATE_CHANGED);
                if (null != systemStatus.getChargeState()) {
                    mCurChargeState = systemStatus.getChargeState();
                    handler.sendEmptyMessage(MSG_NOTIFY_CHARGE_STATE_CHANGED);
                }
                mCurBattery = systemStatus.getBattery();
                handler.sendEmptyMessage(MSG_NOTIFY_BATTERY_STATE_CHANGED);
            }

            @Override
            public void onTimeOut() {
                LogUtil.i("fetchSystemStatus timeout");
                handler.sendEmptyMessageDelayed(MSG_FETCH_SYSTEM_STATUS, 2000);
            }

            @Override
            public void onFailed() {
                LogUtil.i("fetchSystemStatus failed");
                handler.sendEmptyMessageDelayed(MSG_FETCH_SYSTEM_STATUS, 2000);
            }
        });
    }

    public VNDeviceInfo getDeviceInfo() {
        VNDeviceInfo deviceInfo = (VNDeviceInfo) MmkvUtil.getInstance().decodeParcelable(MMKV_KEY_BOUNDED_DEVICE_INFO, VNDeviceInfo.class);
        return deviceInfo;
    }

    public void removeDeviceInfo() {
        MmkvUtil.getInstance().removeKey(MMKV_KEY_BOUNDED_DEVICE_INFO);
    }

    /**
     * 当首次bind，及每次connected时需要调用。
     * <p>
     * 已废弃：<s>检查本地未存储DeviceInfo信息时，将向设备端请求获取；已有存储时跳过。</s>
     * 原因：设备在与App未连接的情况下，设备上的DeviceInfo由于其他原因发生变化，而APP不能获知，所以需要每次连接都获取最新的。
     */
    private void fetchDeviceInfoIfNull() {
        /*
        if (null != getDeviceInfo()) {
            LogUtil.i("fetchDeviceInfo ignore");
            return;
        }
        */

        VNCommon.getDeviceInfo(new VNDeviceInfoCallBack() {
            @Override
            public void onSuccess(VNDeviceInfo deviceInfo) {
                LogUtil.i("fetchDeviceInfo succeed");
                MmkvUtil.getInstance().encodeParcelable(MMKV_KEY_BOUNDED_DEVICE_INFO, deviceInfo);
            }

            @Override
            public void onTimeOut() {
                LogUtil.i("fetchDeviceInfo timeout");
                handler.sendEmptyMessageDelayed(MSG_FETCH_DEVICE_INFO, 1000);
            }

            @Override
            public void onFailed() {
                LogUtil.i("fetchDeviceInfo failed");
                handler.sendEmptyMessageDelayed(MSG_FETCH_DEVICE_INFO, 1000);
            }
        });
    }

    /* SERVICE LISTENER START */
    public List<DeviceServiceListener> deviceServiceListeners = new ArrayList<>();

    public void addDeviceServiceListener(DeviceServiceListener listener) {
        synchronized (deviceServiceListeners) {
            if (!deviceServiceListeners.contains(listener)) {
                deviceServiceListeners.add(listener);

                //listener.onStateChanged(mCurVenusDeviceConnectState);
            }
        }
    }

    public void removeDeviceServiceListener(DeviceServiceListener listener) {
        synchronized (deviceServiceListeners) {
            deviceServiceListeners.remove(listener);
        }
    }

    public interface DeviceServiceListener {
        void onStateChanged(int sysState);
    }
    /* SERVICE LISTENER END */

    private VNDeviceServiceListener venusDeviceServiceListener = new VNDeviceServiceListener() {
        @Override
        public void onStateChanged(int state) {
            LogUtil.i("[" + " state=" + state + " autoConnect=" + autoConnect);

            // FIXME onStateChanged的判断目前只能放这里，不能放到SDK中，否则收不到autoConnect的结果
            if (mCurVenusDeviceConnectState != state) {
                mCurVenusDeviceConnectState = state;
                handler.sendEmptyMessage(MSG_NOTIFY_SERVICE_STATE_CHANGED);
            }

            if (mCurVenusDeviceConnectState == CONNECT_SUCCESS) {
                handler.sendEmptyMessage(MSG_FETCH_SYSTEM_STATUS);
                handler.sendEmptyMessageDelayed(MSG_FETCH_DEVICE_INFO, 500);
            } else {
                handler.removeMessages(MSG_FETCH_SYSTEM_STATUS);
                handler.removeMessages(MSG_FETCH_DEVICE_INFO);

                resetStatus();
                if (autoConnect) {
                    if (!handler.hasMessages(MSG_CONNECT)) {
                        handler.sendEmptyMessageDelayed(MSG_CONNECT, 1000 * 5);
                    }
                }
            }
        }
    };

    /* STATUS LISTENER START */
    public List<DeviceStatusListener> deviceStatusListeners = new ArrayList<>();

    public void addDeviceStatusListener(DeviceStatusListener listener) {
        synchronized (deviceStatusListeners) {
            if (!deviceStatusListeners.contains(listener)) {
                deviceStatusListeners.add(listener);

                listener.onSysStateChanged(mCurSysState);
                listener.onChargeStateChanged(mCurChargeState);
                listener.onBatteryChanged(mCurBattery);
            }
        }
    }

    public void removeDeviceStatusListener(DeviceStatusListener listener) {
        synchronized (deviceStatusListeners) {
            if (deviceStatusListeners.contains(listener)) {
                deviceStatusListeners.remove(listener);
            }
        }
    }

    public interface DeviceStatusListener {
        void onSysStateChanged(int sysState);

        void onChargeStateChanged(int chargeState);

        void onBatteryChanged(int battery);

        void onViewChanged(int venusView);
    }
    /* STATUS LISTENER END */

    private VNDeviceStatusListener venusDeviceStatusListener = new VNDeviceStatusListener() {
        @Override
        public void onSysStateChanged(int sysState) {
            mCurSysState = sysState;
            handler.sendEmptyMessage(MSG_NOTIFY_SYS_STATE_CHANGED);
        }

        @Override
        public void onChargeStateChanged(int chargeState) {
            mCurChargeState = chargeState;
            handler.sendEmptyMessage(MSG_NOTIFY_CHARGE_STATE_CHANGED);
        }

        @Override
        public void onBatteryChanged(int battery) {
            mCurBattery = battery;
            handler.sendEmptyMessage(MSG_NOTIFY_BATTERY_STATE_CHANGED);
        }

        @Override
        public void onViewChanged(int venusView) {
            for (DeviceStatusListener listener : deviceStatusListeners) {
                if (null != listener) {
                    listener.onViewChanged(venusView);
                }
            }

            switch (venusView) {
                case VNConstant.View.HOME:
                    LogUtil.i("HOME");
                    exitAllVenusApp();
                    SttWorker.getInstance().stopWork();
                    break;
                case VNConstant.View.TRANSCRIBE:
                    LogUtil.i("TRANSCRIBE");
                    SttWorker.getInstance().startWork(STT_FUNC_TRANSCRIBE);
                    break;
                case VNConstant.View.TRANSLATE:
                    LogUtil.i("TRANSLATE");
                    SttWorker.getInstance().startWork(STT_FUNC_TRANSLATE);
                    break;
                case VNConstant.View.MAP:
                    LogUtil.i("MAP");
                    toMapAct();
                    break;
                case VNConstant.View.PROMPTER:
                    LogUtil.i("PROMPTER");
                    toPrompterAct();
                    break;
            }
        }

        @Override
        public void onTTMReceived(byte[] data) {
            if (ttmListener != null) {
                ttmListener.onTTMReceived(data);
            }
        }
    };

    private VNDeviceUnsolicitedListener venusDeviceUnsolicitedListener = new VNDeviceUnsolicitedListener() {
        @Override
        public void onSttRequest() {
            String func = MmkvUtil.decodeString(MMKV_STT_FUNC_KEY, STT_FUNC_DEFAULT);
            LogUtil.i("STT REQUEST");

            int venusApp = VNConstant.View.TRANSCRIBE;
            if (STT_FUNC_TRANSCRIBE.equals(func)) {
                venusApp = VNConstant.View.TRANSCRIBE;
            } else if (STT_FUNC_TRANSLATE.equals(func)) {
                venusApp = VNConstant.View.TRANSLATE;
            }
            VNCommon.setView(venusApp, null);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SttWorker.getInstance().startWork(func);
                }
            }, 500);
        }
    };

    private void toRecordAct(String func, boolean enterFromVenus) {
        Intent recordIntent = new Intent(mContext, RecorderActivity.class);
        recordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        recordIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        recordIntent.putExtra("func", func);
        recordIntent.putExtra("ENTER_FROM_VENUS", enterFromVenus);
        mContext.startActivity(recordIntent);
    }

    private void toMapAct() {
        /*
        Intent mapIntent = new Intent(mContext, EmulatorActivity.class);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        mapIntent.putExtra("useInnerVoice", false);
        mapIntent.putExtra("ENTER_FROM_VENUS", true);
        mContext.startActivity(mapIntent);
        */

        // 这一步操作就设置高德地图中的隐私合规，不然可能会出现地图无法正确加载的问题
        MapsInitializer.updatePrivacyShow(mContext, true, true);
        MapsInitializer.updatePrivacyAgree(mContext, true);

        AmapNaviPage.getInstance().showRouteActivity(
                mContext.getApplicationContext(),
                new AmapNaviParams(null, null, null, AmapNaviType.DRIVER, AmapPageType.ROUTE),
                NaviActivity.iNaviInfoCallback,
                NaviActivity.class);
    }

    private void toPrompterAct() {
        Intent prompterIntent = new Intent(mContext, PrompterActivity.class);
        prompterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        prompterIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        prompterIntent.putExtra("ENTER_FROM_VENUS", true);
        mContext.startActivity(prompterIntent);
    }

    private void exitAllVenusApp() {
        Intent intent = new Intent("com.jueyuantech.glasses.ACTION_EXIT_FROM_VENUS");
        mContext.sendBroadcast(intent);
    }

    /* -----------[START]----------- */
    private TTMListener ttmListener;

    public TTMListener getTtmListener() {
        return ttmListener;
    }

    public void setTtmListener(TTMListener listener) {
        ttmListener = listener;
    }

    public interface TTMListener {
        void onTTMReceived(byte[] data);
    }
    /* -----------[End]----------- */
}

