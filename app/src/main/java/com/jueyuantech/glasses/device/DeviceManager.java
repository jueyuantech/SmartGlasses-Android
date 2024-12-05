package com.jueyuantech.glasses.device;

import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_TRANSLATE;
import static com.jueyuantech.venussdk.VenusConstant.ServiceState.CONNECT_ERROR;
import static com.jueyuantech.venussdk.VenusConstant.ServiceState.CONNECT_SUCCESS;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.amap.api.maps.MapsInitializer;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.jueyuantech.glasses.PrompterActivity;
import com.jueyuantech.glasses.RecorderActivity;
import com.jueyuantech.glasses.amap.NaviActivity;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.venussdk.VenusConstant;
import com.jueyuantech.venussdk.VenusSDK;
import com.jueyuantech.venussdk.bean.SystemStatus;
import com.jueyuantech.venussdk.cb.SystemStatusCallBack;
import com.jueyuantech.venussdk.listener.VenusDeviceServiceListener;
import com.jueyuantech.venussdk.listener.VenusDeviceStatusListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceManager {
    private static volatile DeviceManager singleton;
    private Context mContext;

    private static final String MMKV_KEY_BOUNDED_DEVICE = "boundedDevice";
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
    private static final int MSG_GET_SYSTEM_STATUS = 101;

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
                case MSG_GET_SYSTEM_STATUS:
                    getSystemStatus();
                    break;
            }
        }
    };

    private DeviceManager(Context context) {
        mContext = context.getApplicationContext();
        initDevice();

        VenusSDK.setDeviceServiceListener(venusDeviceServiceListener);
        VenusSDK.setDeviceStatusListener(venusDeviceStatusListener);
    }

    public static DeviceManager getInstance() {
        if (singleton == null) {
            throw new IllegalStateException("DeviceManager is not initialized. Call init() before getInstance().");
        }
        return singleton;
    }

    /**
     * 初始化方法，应该在Application中调用
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

    private void initDevice() {
        if (isBound()) {
            boundedDevice = getBoundDevice();
            VenusSDK.initDevice(boundedDevice);
            if (!handler.hasMessages(MSG_CONNECT)) {
                handler.sendEmptyMessage(MSG_CONNECT);
            }
        }
    }

    public boolean isBound() {
        return MmkvUtil.getInstance().have(MMKV_KEY_BOUNDED_DEVICE);
    }

    public void setBoundDevice(BluetoothDevice device) {
        MmkvUtil.getInstance().encodeParcelable(MMKV_KEY_BOUNDED_DEVICE, device);
        initDevice();
    }

    public BluetoothDevice getBoundDevice() {
        return (BluetoothDevice) MmkvUtil.getInstance().decodeParcelable(MMKV_KEY_BOUNDED_DEVICE, BluetoothDevice.class);
    }

    public void removeBoundDevice() {
        MmkvUtil.getInstance().removeKey(MMKV_KEY_BOUNDED_DEVICE);
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
            if (VenusSDK.isConnected()) {

            } else {
                boundedDevice = getBoundDevice();
                VenusSDK.connect(boundedDevice);
            }
        }
    }

    private void resetStatus() {
        mCurVenusDeviceConnectState = CONNECT_ERROR;
        mCurSysState = 0;
        mCurChargeState = 0;
        mCurBattery = -1;
    }

    private void getSystemStatus() {
        VenusSDK.getSystemStatus(new SystemStatusCallBack() {
            @Override
            public void onSuccess(SystemStatus systemStatus) {
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
                if (CONNECT_SUCCESS == mCurVenusDeviceConnectState) {
                    handler.sendEmptyMessageDelayed(MSG_GET_SYSTEM_STATUS, 2000);
                }
            }

            @Override
            public void onFailed() {
                if (CONNECT_SUCCESS == mCurVenusDeviceConnectState) {
                    handler.sendEmptyMessageDelayed(MSG_GET_SYSTEM_STATUS, 2000);
                }
            }
        });
    }

    /* SERVICE LISTENER START */
    public List<DeviceServiceListener> deviceServiceListeners = new ArrayList<>();
    public void addDeviceServiceListener(DeviceServiceListener listener) {
        synchronized (deviceServiceListeners) {
            if (!deviceServiceListeners.contains(listener)) {
                deviceServiceListeners.add(listener);

                listener.onStateChanged(mCurVenusDeviceConnectState);
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

    private VenusDeviceServiceListener venusDeviceServiceListener = new VenusDeviceServiceListener() {
        @Override
        public void onStateChanged(int state) {
            LogUtil.i("[" + " state=" + state + " autoConnect=" + autoConnect);
            mCurVenusDeviceConnectState = state;
            handler.sendEmptyMessage(MSG_NOTIFY_SERVICE_STATE_CHANGED);

            if (mCurVenusDeviceConnectState == CONNECT_SUCCESS) {
                handler.sendEmptyMessage(MSG_GET_SYSTEM_STATUS);
            } else {
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
            deviceStatusListeners.remove(listener);
        }
    }
    public interface DeviceStatusListener {
        void onSysStateChanged(int sysState);
        void onChargeStateChanged(int chargeState);
        void onBatteryChanged(int battery);
    }
    /* STATUS LISTENER END */

    private VenusDeviceStatusListener venusDeviceStatusListener = new VenusDeviceStatusListener() {
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
            switch (venusView) {
                case VenusConstant.View.HOME:
                    LogUtil.i("HOME");
                    exitAllVenusApp();
                    break;
                case VenusConstant.View.ASR:
                    LogUtil.i("ASR");
                    toRecordAct(ASR_FUNC_TRANSCRIBE);
                    break;
                case VenusConstant.View.TRANS:
                    LogUtil.i("TRANS");
                    toRecordAct(ASR_FUNC_TRANSLATE);
                    break;
                case VenusConstant.View.MAP:
                    LogUtil.i("MAP");
                    toMapAct();
                    break;
                case VenusConstant.View.PROMPTER:
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

    private void toRecordAct(String func) {
        Intent recordIntent = new Intent(mContext, RecorderActivity.class);
        recordIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        recordIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        recordIntent.putExtra("func", func);
        recordIntent.putExtra("ENTER_FROM_VENUS", true);
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

