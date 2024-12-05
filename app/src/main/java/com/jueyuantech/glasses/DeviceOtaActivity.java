package com.jueyuantech.glasses;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VenusConstant;
import com.jueyuantech.venussdk.VenusSDK;
import com.jueyuantech.venussdk.listener.VenusOtaServiceListener;

import java.io.File;

public class DeviceOtaActivity extends AppCompatActivity implements View.OnClickListener {

    private static final boolean DEBUG = false;
    private ImageView mDeviceIv;
    private Button mActionButton;
    private RelativeLayout mVersionInfoRl;
    private TextView mVersionInfoTv;
    private RelativeLayout mProgressContainerRl;
    private TextView mProgressTv;
    private SeekBar mProgressSkb;
    private ActivityResultLauncher<Intent> launcher;
    private TextView mLocalFileTv;

    private String user;
    private String filePath;
    private String mHwInfo;

    private static final int MSG_PROGRESS_UPDATE = 1;
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

        mDeviceIv = findViewById(R.id.iv_device);
        mDeviceIv.setOnClickListener(this);
        mActionButton = findViewById(R.id.btn_action);
        mActionButton.setOnClickListener(this);
        mProgressContainerRl = findViewById(R.id.rl_container_progress);
        mVersionInfoRl = findViewById(R.id.rl_container_version_info);
        mVersionInfoTv = findViewById(R.id.tv_version_info);
        mProgressTv = findViewById(R.id.tv_progress);
        mProgressSkb = findViewById(R.id.skb_progress);
        mProgressSkb.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mLocalFileTv = findViewById(R.id.tv_local_file);
        setAutoLink();

        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult activityResult) {
                        if (activityResult.getData() != null && activityResult.getResultCode() == Activity.RESULT_OK) {
                            user = activityResult.getData().getStringExtra("user");
                            filePath = activityResult.getData().getStringExtra("filePath");
                            LogUtil.i("From Local: USER " + user + " FILEPATH " + filePath);
                        }
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
            case R.id.iv_device:
                localOtaTrigger();
                break;
            case R.id.btn_action:
                if (TextUtils.isEmpty(user) || TextUtils.isEmpty(filePath)) {
                    ToastUtil.toast(getApplicationContext(), R.string.ota_latest_already);
                } else {
                    if (isConfigReady()) {
                        mActionButton.setEnabled(false);
                        mActionButton.setText(R.string.ota_upgrading);

                        DeviceManager.getInstance().setAutoConnect(false);
                        if (VenusSDK.isConnected()) {
                            VenusSDK.disconnect();
                            retryCount = 1;
                            mHandler.postDelayed(serviceStateCheckRunnable, 1000);
                            refreshUI("reset datapath");
                        } else {
                            startVenusOta();
                        }
                    }
                }
                break;
        }
    }

    private void setAutoLink() {
        String localFileStr = mLocalFileTv.getText().toString();
        SpannableString spannableString = new SpannableString(localFileStr);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                toOtaFileConfigActForResult();
            }

            @Override
            public void updateDrawState(TextPaint textPaint) {
                super.updateDrawState(textPaint);
                // 设置超链接的下划线
                textPaint.setUnderlineText(true);
            }
        }, 0, localFileStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mLocalFileTv.setMovementMethod(LinkMovementMethod.getInstance());
        mLocalFileTv.setText(spannableString);
    }

    private void updateView() {
        if (TextUtils.isEmpty(user) || TextUtils.isEmpty(filePath)) {
            mActionButton.setText(R.string.ota_check_updates);
            mVersionInfoTv.setText("");
        } else {
            // FIXME
            mActionButton.setText(R.string.ota_start_upgrade);
            mVersionInfoTv.setText("USER:" + user + "\n" + "FILE:" + filePath + "\n" + "CurVer:" + mHwInfo);
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
                if (VenusSDK.isConnected()) {
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

    private void startVenusOta() {
        int otaUser = VenusConstant.OtaUser.FIRMWARE;
        if ("firmware".equals(user)) {
            LogUtil.i("firmware");
            otaUser = VenusConstant.OtaUser.FIRMWARE;
        } else if ("bth".equals(user)) {
            LogUtil.i("bth");
            otaUser = VenusConstant.OtaUser.BTH;
        }
        VenusSDK.setOtaServiceListener(venusOtaServiceListener);
        VenusSDK.initOta(
                DeviceManager.getInstance().getBoundDevice(),
                filePath,
                otaUser);
        VenusSDK.startOta();
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

    private VenusOtaServiceListener venusOtaServiceListener = new VenusOtaServiceListener() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onSuccess() {
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
            refreshUI(message);
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

    final static int COUNTS = 5;
    final static long DURATION = 2 * 1000;
    long[] mHits = new long[COUNTS];

    private void localOtaTrigger() {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        //实现左移，然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
            mHits = new long[COUNTS];
            toOtaFileConfigActForResult();
        }
    }

    private void toOtaFileConfigActForResult() {
        Intent intent = new Intent(this, OtaFileConfigActivity.class);
        launcher.launch(intent);
    }
}