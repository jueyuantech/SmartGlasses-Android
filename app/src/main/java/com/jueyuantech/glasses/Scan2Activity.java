package com.jueyuantech.glasses;

import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC;
import static com.jueyuantech.venussdk.VenusConstant.ServiceState.CONNECT_ERROR;
import static com.jueyuantech.venussdk.VenusConstant.ServiceState.CONNECT_SUCCESS;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VenusSDK;

import java.util.ArrayList;
import java.util.List;

public class Scan2Activity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackIv;

    private RelativeLayout mSearchBtn, mCancelBtn;
    private TextView mSearchTv;

    private List<BluetoothDevice> mDevices = new ArrayList<>();
    private int curDeviceIndex = -1;
    private BluetoothDevice curDevice;
    private TextView curDeviceNameTv;
    private TextView curDeviceMacTv;
    private TextView curDeviceIndexTv;
    private RelativeLayout curDeviceSwitcherRl;
    private ImageView preDeviceBtn, nextDeviceBtn;
    private RelativeLayout curDeviceConnectRl;
    private ImageView curDeviceIv;
    private ObjectAnimator scanAni;

    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mSearchBtn = findViewById(R.id.rl_container_search);
        mSearchBtn.setOnClickListener(this);
        mSearchTv = findViewById(R.id.tv_search);

        mCancelBtn = findViewById(R.id.rl_container_cancel);
        mCancelBtn.setOnClickListener(this);

        curDeviceNameTv = findViewById(R.id.tv_cur_device_name);
        curDeviceMacTv = findViewById(R.id.tv_cur_device_mac);
        curDeviceIndexTv = findViewById(R.id.tv_cur_device_index);
        curDeviceSwitcherRl = findViewById(R.id.rl_device_switcher);
        preDeviceBtn = findViewById(R.id.iv_device_pre);
        preDeviceBtn.setOnClickListener(this);
        nextDeviceBtn = findViewById(R.id.iv_device_next);
        nextDeviceBtn.setOnClickListener(this);
        curDeviceConnectRl = findViewById(R.id.rl_container_connect_device);
        curDeviceConnectRl.setOnClickListener(this);

        curDeviceIv = findViewById(R.id.iv_device);
        curDeviceIv.setOnClickListener(this);
        scanAni = ObjectAnimator.ofFloat(curDeviceIv, "translationY", 0F, -50F, 0F);
        scanAni.setDuration(1000);
        scanAni.setInterpolator(new AccelerateDecelerateInterpolator());
        scanAni.setRepeatCount(ValueAnimator.INFINITE);
        scanAni.setRepeatMode(ValueAnimator.REVERSE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, intentFilter);

        DeviceManager.getInstance().addDeviceServiceListener(serviceListener);

        startScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        stopScan();

        unregisterReceiver(receiver);
        DeviceManager.getInstance().removeDeviceServiceListener(serviceListener);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
            case R.id.rl_container_cancel:
                onBackPressed();
                break;
            case R.id.rl_container_search:
                startScan();
                break;
            case R.id.iv_device_pre:
                preDevice();
                break;
            case R.id.iv_device_next:
                nextDevice();
                break;
            case R.id.rl_container_connect_device:
            case R.id.iv_device:
                if (null != curDevice) {
                    showConnectConfirmDialog();
                }
                break;
            default:
        }
    }


    private void startScan() {
        mDevices.clear();

        mSearchBtn.setEnabled(false);
        curDeviceIndex = -1;
        curDevice = null;
        updateDeviceIndex();
        setCurDeviceIndex(curDeviceIndex);

        // 获取已连接（被系统）的设备
        getConnectBtDetails(getConnectBt());
        // 搜索附近未连接的设备
        bluetoothAdapter.startDiscovery();
    }

    private void stopScan() {
        bluetoothAdapter.cancelDiscovery();
    }

    private void onScanStarted() {
        mSearchTv.setText(R.string.btn_searching);
        if (!scanAni.isRunning()) {
            scanAni.start();
        }
        curDeviceMacTv.setVisibility(View.VISIBLE);
        curDeviceMacTv.setText(R.string.tips_power_on);
    }

    private void onScanFinished() {
        mSearchTv.setText(R.string.btn_search);
        mSearchBtn.setEnabled(true);
        if (scanAni.isRunning()) {
            scanAni.cancel();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                LogUtil.i("ACTION_DISCOVERY_STARTED");
                onScanStarted();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                LogUtil.i("ACTION_DISCOVERY_FINISHED");
                onScanFinished();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                LogUtil.i("ACTION_FOUND");

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a Toast
                if (TextUtils.isEmpty(device.getName())) {
                    return;
                }

                if ((device.getType() & DEVICE_TYPE_CLASSIC) == 0) {
                    return;
                }

                onDeviceFound(device);
            }
        }
    };

    private void onDeviceFound(BluetoothDevice device) {
        synchronized (mDevices) {
            for (BluetoothDevice dev : mDevices) {
                if (dev.getAddress().equals(device.getAddress())) {
                    return;
                }
            }
            LogUtil.i(device.getName() + " - " + device.getAddress());
            mDevices.add(device);
            updateDeviceIndex();
        }
    }

    private void updateDeviceIndex() {
        int total = mDevices.size();
        if (total <= 1) {
            curDeviceIndexTv.setText("");
            curDeviceSwitcherRl.setVisibility(View.GONE);
        } else {
            curDeviceIndexTv.setText(String.format("%d / %d", curDeviceIndex + 1, total));
            curDeviceSwitcherRl.setVisibility(View.VISIBLE);
        }

        if (-1 == curDeviceIndex && total > 0) {
            setCurDeviceIndex(0);
        }
    }

    private void preDevice() {
        if (curDeviceIndex <= 0) {

        } else {
            curDeviceIndex--;
        }
        setCurDeviceIndex(curDeviceIndex);
    }

    private void nextDevice() {
        if (curDeviceIndex >= (mDevices.size() - 1)) {

        } else {
            curDeviceIndex++;
        }
        setCurDeviceIndex(curDeviceIndex);
    }

    private void setCurDeviceIndex(int index) {
        curDeviceIndex = index;
        if (curDeviceIndex >= 0) {
            curDevice = mDevices.get(index);
        } else {
            curDevice = null;
        }

        if (null == curDevice) {
            curDeviceNameTv.setText("");
            curDeviceMacTv.setText("");
            curDeviceConnectRl.setVisibility(View.GONE);

            if (!scanAni.isRunning()) {
                scanAni.start();
            }
        } else {
            curDeviceNameTv.setText(String.valueOf(curDevice.getName()));
            curDeviceMacTv.setText(String.valueOf(curDevice.getAddress()));
            curDeviceConnectRl.setVisibility(View.VISIBLE);

            if (scanAni.isRunning()) {
                scanAni.cancel();
            }
        }

        updateDeviceIndex();
    }

    private void showConnectConfirmDialog() {
        if (null == curDevice) {
            ToastUtil.toast(this, R.string.tips_no_device_selected);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.tips_connect, curDevice.getName()));
        builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                connect(curDevice);
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

    private void connect(BluetoothDevice device) {
        if (null == device) {
            ToastUtil.toast(this, R.string.tips_no_device_selected);
        } else {
            VenusSDK.connect(device);
            showProgressDialog(getString(R.string.tips_connecting));
        }
    }

    private ProgressDialog mProgressDialog;

    private void showProgressDialog(String message) {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }

        mProgressDialog.setMessage(message);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();

            WindowManager.LayoutParams lp = mProgressDialog.getWindow().getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 8; // 宽度，可根据屏幕宽度进行计算
            lp.gravity = Gravity.CENTER;
            mProgressDialog.getWindow().setAttributes(lp);
        }
    }

    private void dismissProgressDialog() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private DeviceManager.DeviceServiceListener serviceListener = new DeviceManager.DeviceServiceListener() {
        @Override
        public void onStateChanged(int sysState) {
            switch (sysState) {
                case CONNECT_SUCCESS:
                    DeviceManager.getInstance().setBoundDevice(curDevice);
                    ToastUtil.toast(Scan2Activity.this, getString(R.string.tips_connected, curDevice.getName()));
                    dismissProgressDialog();

                    onBackPressed();
                    break;
                case CONNECT_ERROR:
                    ToastUtil.toast(Scan2Activity.this, getString(R.string.tips_connect_failed));
                    dismissProgressDialog();
                    break;
                default:
                    break;
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void getConnectBtDetails(int flag) {
        bluetoothAdapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceDisconnected(int profile)
            {

            }

            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy)
            {
                List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                if (mDevices != null && mDevices.size() > 0) {
                    for (BluetoothDevice device : mDevices) {
                        onDeviceFound(device);
                    }
                }
            }
        }, flag);
    }

    @SuppressLint("MissingPermission")
    private int getConnectBt() {
        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        int a2dp = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int headset = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int health = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEALTH);
        int flag = -1;
        if (a2dp == BluetoothProfile.STATE_CONNECTED) {
            flag = a2dp;
        }
        else if (headset == BluetoothProfile.STATE_CONNECTED) {
            flag = headset;
        }
        else if (health == BluetoothProfile.STATE_CONNECTED) {
            flag = health;
        }
        return flag;
    }
}