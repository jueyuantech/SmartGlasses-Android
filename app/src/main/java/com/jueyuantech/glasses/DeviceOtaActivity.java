package com.jueyuantech.glasses;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jueyuantech.glasses.bean.LatestFwInfo;
import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.listener.VNOtaServiceListener;

import java.io.File;

public class DeviceOtaActivity extends AppCompatActivity implements View.OnClickListener {

    private static final boolean DEBUG = false;

    private static final int MIN_BATTERY_LEVEL_FOR_OTA = 40;

    private ImageView mBackIv;
    private Button mDownloadBtn, mUpgradeBtn;
    private RelativeLayout mVersionInfoRl;
    private TextView mVersionNameTv;
    private TextView mVersionInfoTv;
    private RelativeLayout mProgressContainerRl;
    private TextView mProgressTv;
    private SeekBar mProgressSkb;

    private String user;
    private String filePath;
    private String mHwInfo;
    private LatestFwInfo mFwInfo;

    public static final int OTA_TYPE_1 = 1;
    public static final int OTA_TYPE_2 = 2;
    public static final int OTA_TYPE_3 = 3;

    private int mCurType = OTA_TYPE_1;

    private static final int MSG_PROGRESS_UPDATE = 1;
    private static final int MSG_DOWNLOAD_FINISHED = 2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PROGRESS_UPDATE:
                    try {
                        String progressStr = (String) msg.obj;
                        mProgressTv.setText(progressStr);
                        mProgressSkb.setProgress((int) Math.floor(Float.parseFloat(progressStr)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_DOWNLOAD_FINISHED:
                    processDownloadFinished();
                    break;
                default:
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_ota);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mDownloadBtn = findViewById(R.id.btn_download);
        mDownloadBtn.setOnClickListener(this);
        mUpgradeBtn = findViewById(R.id.btn_upgrade);
        mUpgradeBtn.setOnClickListener(this);
        mProgressContainerRl = findViewById(R.id.rl_container_progress);
        mVersionInfoRl = findViewById(R.id.rl_container_version_info);
        mVersionNameTv = findViewById(R.id.tv_version_name);
        mVersionInfoTv = findViewById(R.id.tv_version_info);
        mProgressTv = findViewById(R.id.tv_progress);
        mProgressSkb = findViewById(R.id.skb_progress);
        mProgressSkb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.mark();
        updateView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                if (isUpgrading) {
                    showExistDialog();
                } else {
                    finish();
                }
                break;
            case R.id.btn_download:
                downloadFirmware();
                break;
            case R.id.btn_upgrade:
                upgrade();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isUpgrading) {
                showExistDialog();
            } else {
                finish();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showExistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.ota_processing_title));
        builder.setMessage(getString(R.string.ota_processing_message));
        builder.setNegativeButton(R.string.ota_processing_negative, null);

        builder.setPositiveButton(R.string.ota_processing_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
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

    private void updateView() {
        Intent intent = getIntent();
        mCurType = intent.getIntExtra("ota_type", OTA_TYPE_1);
        if (OTA_TYPE_1 == mCurType) {
            mFwInfo = (LatestFwInfo) intent.getSerializableExtra("ota_fw_info");
            mVersionNameTv.setText(mFwInfo.getVerName() + " " + mFwInfo.getVerCode());
            mVersionInfoTv.setText(mFwInfo.getDescription());
            user = "firmware";
            mDownloadBtn.setVisibility(View.VISIBLE);
            mUpgradeBtn.setVisibility(View.GONE);
        } else if (OTA_TYPE_2 == mCurType) {

        } else if (OTA_TYPE_3 == mCurType) {
            user = intent.getStringExtra("user");
            filePath = intent.getStringExtra("filePath");
            mDownloadBtn.setVisibility(View.GONE);
            mUpgradeBtn.setVisibility(View.VISIBLE);

            mVersionInfoTv.setText("Local upgrade" + "\n" + filePath);
        }
    }

    private void refreshUI(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressTv.setText(msg);
            }
        });
    }

    private boolean isConfigReady() {
        File otaFile = new File(filePath);
        if (otaFile.exists() && otaFile.canRead() /* && crc */) {

        } else {
            LogUtil.e("file check failed");
            ToastUtil.toast(this, "file check failed");
            return false;
        }

        BluetoothDevice device = DeviceManager.getInstance().getBoundDevice();
        if (null == device) {
            LogUtil.e("device check failed");
            ToastUtil.toast(this, "device check failed");
            return false;
        }

        if ("firmware".equals(user)) {
            LogUtil.i("firmware");
        } else if ("bth".equals(user)) {
            LogUtil.i("bth");
        } else {
            LogUtil.e("user check failed");
            ToastUtil.toast(this, "user check failed");
            return false;
        }

        return true;
    }

    private int maxRetries = 3;
    private int retryCount = 0;
    private Handler handler = new Handler();
    private Runnable serviceStateCheckRunnable = new Runnable() {
        @Override
        public void run() {
            if (retryCount < maxRetries) {
                if (VNCommon.isConnected()) {
                    retryCount++;
                    handler.postDelayed(this, 1000);
                    refreshUI("reset datapath " + retryCount);
                } else {
                    startVenusOta();
                }
            } else {
                refreshUI("reset datapath failed");
            }
        }
    };

    private void upgrade() {
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(filePath)) {
            ToastUtil.toast(getApplicationContext(), R.string.ota_latest_already);
            return;
        }

        if (!isConfigReady()) {
            return;
        }

        if (DeviceManager.getInstance().getBattery() < MIN_BATTERY_LEVEL_FOR_OTA) {
            ToastUtil.toast(getApplicationContext(), getString(R.string.ota_battery_low, MIN_BATTERY_LEVEL_FOR_OTA));
            return;
        }

        mUpgradeBtn.setEnabled(false);
        mUpgradeBtn.setText(R.string.ota_upgrading);

        DeviceManager.getInstance().setAutoConnect(false);
        if (VNCommon.isConnected()) {
            VNCommon.disconnect();
            retryCount = 1;
            mHandler.postDelayed(serviceStateCheckRunnable, 1000);
            refreshUI("reset datapath");
        } else {
            startVenusOta();
        }
    }

    private void startVenusOta() {
        int otaUser = VNConstant.Ota.User.FIRMWARE;
        if ("firmware".equals(user)) {
            LogUtil.i("firmware");
            otaUser = VNConstant.Ota.User.FIRMWARE;
        } else if ("bth".equals(user)) {
            LogUtil.i("bth");
            otaUser = VNConstant.Ota.User.BTH;
        }
        VNCommon.setOtaServiceListener(venusOtaServiceListener);
        VNCommon.initOta(
                DeviceManager.getInstance().getBoundDevice(),
                filePath,
                otaUser);
        VNCommon.startOta();
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("升级成功");
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.btn_sure, (dialog, id) -> {
            finish();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showErrorDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("升级失败");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.btn_sure, (dialog, id) -> {
            finish();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isUpgrading = false;
    private VNOtaServiceListener venusOtaServiceListener = new VNOtaServiceListener() {
        @Override
        public void onConnected() {
            LogUtil.mark();
            isUpgrading = true;
        }

        @Override
        public void onDisconnected() {
            LogUtil.mark();
            isUpgrading = false;
            DeviceManager.getInstance().setAutoConnect(true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUpgradeBtn.setEnabled(true);
                    mUpgradeBtn.setText(R.string.ota_start_upgrade);
                }
            });
        }

        @Override
        public void onSuccess() {
            isUpgrading = false;
            DeviceManager.getInstance().removeDeviceInfo();
            DeviceManager.getInstance().setAutoConnect(true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showSuccessDialog();
                }
            });
        }

        @Override
        public void onError(int errCode, String message) {
            LogUtil.mark();
            DeviceManager.getInstance().setAutoConnect(true);
            refreshUI(message);
            String originalMsg = message;
            if (VNConstant.Ota.ErrorCode.IMAGE_SIZE_ERROR == errCode) {
                originalMsg = getString(R.string.ota_error_image_size);
            }
            final String finalMsg = originalMsg;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showErrorDialog(finalMsg);
                }
            });
        }

        @Override
        public void onStateChanged(int code, String message) {
            refreshUI(message);
        }

        @Override
        public void onProgressChanged(String progress) {
            Message.obtain(mHandler, MSG_PROGRESS_UPDATE, progress).sendToTarget();
        }
    };

    private void downloadFirmware() {
        mDownloadBtn.setEnabled(false);
        mDownloadBtn.setText(R.string.ota_downloading);

        File otaFilesDir = getExternalFilesDir("VenusOTATemp");

        // TODO backend service
    }

    private void processDownloadFinished() {
        mDownloadBtn.setVisibility(View.GONE);
        mUpgradeBtn.setVisibility(View.VISIBLE);
    }
}