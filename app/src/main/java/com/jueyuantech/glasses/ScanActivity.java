package com.jueyuantech.glasses;

import static android.bluetooth.BluetoothDevice.DEVICE_TYPE_CLASSIC;
import static com.jueyuantech.venussdk.VNConstant.ServiceState.CONNECT_ERROR;
import static com.jueyuantech.venussdk.VNConstant.ServiceState.CONNECT_SUCCESS;

import android.Manifest;
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
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VNCommon;

import java.util.ArrayList;
import java.util.List;

public class ScanActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackIv;

    private RelativeLayout mSearchBtn, mCancelBtn;
    private TextView mSearchTv;
    private LottieAnimationView mScanAni;

    private List<BluetoothDevice> mDevices = new ArrayList<>();
    private ListView mDeviceLv;
    private View mDeviceEmptyView;
    private ListAdapter mDeviceAdapter;

    private BluetoothDevice curDevice;
    private BluetoothAdapter bluetoothAdapter;

    private WindowManager mWindowManager;
    private View mPermissionTipView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mSearchBtn = findViewById(R.id.rl_container_search);
        mSearchBtn.setOnClickListener(this);
        mSearchTv = findViewById(R.id.tv_search);

        mCancelBtn = findViewById(R.id.rl_container_cancel);
        mCancelBtn.setOnClickListener(this);

        mScanAni = findViewById(R.id.ani_scan);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mDevices.clear();
        mDeviceAdapter = new ListAdapter(this, mDevices);
        mDeviceLv = findViewById(R.id.lv_device);
        mDeviceEmptyView = findViewById(R.id.v_device_empty);
        mDeviceLv.setEmptyView(mDeviceEmptyView);
        mDeviceLv.setAdapter(mDeviceAdapter);

        mDeviceLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stopScan();

                curDevice = mDevices.get(position);
                showConnectConfirmDialog();
            }
        });
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

        checkPermissionAndScan();
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
                checkPermissionAndScan();
                break;
            default:
        }
    }

    private void checkPermissionAndScan() {
        if (isPermissionGranted()) {
            startScan();
        } else {
            showPermissionTip(
                    getString(R.string.permission_dialog_discover_title),
                    getString(R.string.permission_dialog_discover_content)
            );
            requestPermissions();
        }
    }

    private boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private static final int REQUEST_CODE = 1;

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.BLUETOOTH_SCAN,
                            android.Manifest.permission.BLUETOOTH_CONNECT
                    }, REQUEST_CODE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                boolean granted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        granted = false;
                    }
                }

                if (granted) {
                    startScan();
                } else {
                    showGrantPermissionDialog();
                }
                break;
            default:
        }
        dismissPermissionTip();

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showPermissionTip(String title, String content) {
        if (null == mPermissionTipView) {
            mPermissionTipView = LayoutInflater.from(this).inflate(R.layout.view_top_tip, null);
        }

        TextView titleTv = mPermissionTipView.findViewById(R.id.tv_title);
        titleTv.setText(title);
        TextView contentTv = mPermissionTipView.findViewById(R.id.tv_content);
        contentTv.setText(content);

        if (null != mWindowManager) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |    //不拦截页面点击事件
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            layoutParams.format = PixelFormat.TRANSLUCENT;
            layoutParams.gravity = Gravity.TOP;
            mWindowManager.addView(mPermissionTipView, layoutParams);
        }
    }

    private void dismissPermissionTip() {
        if (null != mPermissionTipView) {
            mWindowManager.removeViewImmediate(mPermissionTipView);
        }
    }

    private void showGrantPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permission_dialog_discover_title);
        builder.setMessage(R.string.permission_dialog_discover_content);
        builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toPermissionGrantAct();
            }
        });

        Dialog mAlertDialog = builder.create();
        mAlertDialog.show();
        if (mAlertDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = mAlertDialog.getWindow().getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 9;
            lp.gravity = Gravity.CENTER;
            mAlertDialog.getWindow().setAttributes(lp);
        }
    }

    private void toPermissionGrantAct() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void startScan() {
        mDevices.clear();
        mDeviceAdapter.notifyDataSetChanged();

        mSearchBtn.setEnabled(false);
        curDevice = null;

        // 获取已连接（被系统）的设备
        getConnectBtDetails(getConnectBt());
        // 搜索附近未连接的设备
        if (isPermissionGranted()) {
            bluetoothAdapter.startDiscovery();
        }
    }

    private void stopScan() {
        if (isPermissionGranted()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    private void onScanStarted() {
        mSearchTv.setText(R.string.btn_searching);
        if (!mScanAni.isAnimating()) {
            mScanAni.playAnimation();
        }
        mScanAni.setVisibility(View.VISIBLE);
    }

    private void onScanFinished() {
        mSearchTv.setText(R.string.btn_search);
        mSearchBtn.setEnabled(true);
        if (mScanAni.isAnimating()) {
            mScanAni.cancelAnimation();
        }
        mScanAni.setVisibility(View.GONE);
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
            mDeviceAdapter.notifyDataSetChanged();
        }
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
            VNCommon.connect(device);
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
                    ToastUtil.toast(ScanActivity.this, getString(R.string.tips_connected, curDevice.getName()));
                    dismissProgressDialog();

                    onBackPressed();
                    break;
                case CONNECT_ERROR:
                    ToastUtil.toast(ScanActivity.this, getString(R.string.tips_connect_failed));
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
            public void onServiceDisconnected(int profile) {

            }

            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
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
        } else if (headset == BluetoothProfile.STATE_CONNECTED) {
            flag = headset;
        } else if (health == BluetoothProfile.STATE_CONNECTED) {
            flag = health;
        }
        return flag;
    }

    class ListAdapter extends BaseAdapter {
        private Context context;
        private List<BluetoothDevice> items;

        public ListAdapter(Context context, List<BluetoothDevice> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
                holder = new ViewHolder();
                holder.nameTv = convertView.findViewById(R.id.tv_name);
                holder.macTv = convertView.findViewById(R.id.tv_mac);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            BluetoothDevice item = items.get(position);
            holder.nameTv.setText(item.getName());
            holder.macTv.setText(item.getAddress());

            return convertView;
        }
    }

    class ViewHolder {
        TextView nameTv;
        TextView macTv;
    }
}