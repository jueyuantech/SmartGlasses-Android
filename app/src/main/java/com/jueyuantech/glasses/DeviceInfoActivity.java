package com.jueyuantech.glasses;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.jueyuantech.glasses.bean.LatestFwInfo;
import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.bean.VNDeviceInfo;

public class DeviceInfoActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private Gson gson = new Gson();

    private ImageView mBackIv;
    private Button mRemoveBtn;

    private TextView mDeviceNameTv;
    private TextView mVendorTv;
    private TextView mModelTv;
    private TextView mEditionTv;
    private TextView mMacTv;
    private TextView mSnTv;

    private RelativeLayout mFirmwareRl;
    private TextView mFirmwareTv;

    private VNDeviceInfo mDeviceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mDeviceNameTv = findViewById(R.id.tv_device_name);
        mVendorTv = findViewById(R.id.tv_vendor);
        mModelTv = findViewById(R.id.tv_model);
        mEditionTv = findViewById(R.id.tv_edition);
        mMacTv = findViewById(R.id.tv_mac);
        mSnTv = findViewById(R.id.tv_sn);

        mFirmwareRl = findViewById(R.id.rl_container_firmware);
        mFirmwareRl.setOnClickListener(this);
        mFirmwareRl.setOnLongClickListener(this);
        mFirmwareTv = findViewById(R.id.tv_firmware);

        mRemoveBtn = findViewById(R.id.btn_remove);
        mRemoveBtn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (DeviceManager.getInstance().isBound()) {
            BluetoothDevice device = DeviceManager.getInstance().getBoundDevice();
            mDeviceNameTv.setText(device.getName());
        } else {
            mDeviceNameTv.setText("");
        }

        mDeviceInfo = DeviceManager.getInstance().getDeviceInfo();
        if (null != mDeviceInfo) {
            mVendorTv.setText(mDeviceInfo.getManufacturer());
            mModelTv.setText(mDeviceInfo.getModel());
            mEditionTv.setText(mDeviceInfo.getEdition());
            mMacTv.setText(mDeviceInfo.getMac());
            mSnTv.setText(mDeviceInfo.getSn());
            mFirmwareTv.setText(mDeviceInfo.getFirmware());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.btn_remove:
                showRemoveConfirmDialog();
                break;
            case R.id.rl_container_firmware:
                //getLatestFw();
                getTestFw();
                break;
            default:
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.rl_container_firmware:
                showFirmwareIdInputDialog();
                return true;
            default:
                return false;
        }
    }

    private void showFirmwareIdInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input Firmware Id");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.btn_sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String firmwareIdStr = input.getText().toString();
                if (!TextUtils.isEmpty(firmwareIdStr)) {
                    try {
                        int firmwareId = Integer.parseInt(firmwareIdStr);
                        getFwById(firmwareId);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        builder.setNegativeButton(getString(R.string.btn_cancel), null);

        builder.show();
    }

    private void showRemoveConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.tips_remove));
        builder.setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                VNCommon.disconnect();
                DeviceManager.getInstance().removeBoundDevice();
                showRemoveResultDialog();
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

    private void showRemoveResultDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.tips_removed));
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.btn_sure, new DialogInterface.OnClickListener() {
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

    private void toDeviceOtaAct(LatestFwInfo latestFwInfo) {
        if (!VNCommon.isConnected()) {
            ToastUtil.toast(DeviceInfoActivity.this, "Device is not connect");
            return;
        }

        Intent deviceOtaIntent = new Intent(this, DeviceOtaActivity.class);
        deviceOtaIntent.putExtra("ota_type", DeviceOtaActivity.OTA_TYPE_1);
        deviceOtaIntent.putExtra("ota_fw_info", latestFwInfo);
        startActivity(deviceOtaIntent);
    }

    private void toTestOtaAct(String fileName) {
        File otaFilesDir = getExternalFilesDir("VenusOTATemp");
        if (!otaFilesDir.exists()) {
            otaFilesDir.mkdir();
        }

        File targetFile = new File(otaFilesDir, fileName);
        targetFile.deleteOnExit();
        try {
            targetFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to copy firmware file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        try (InputStream inputStream = getAssets().open("VenusOTA/" + fileName);
             OutputStream outputStream = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to copy firmware file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        String filePath = targetFile.getAbsolutePath();

        Intent deviceOtaIntent = new Intent(this, DeviceOtaActivity.class);
        deviceOtaIntent.putExtra("ota_type", DeviceOtaActivity.OTA_TYPE_3);
        deviceOtaIntent.putExtra("user", "firmware");
        deviceOtaIntent.putExtra("filePath", filePath);
        startActivity(deviceOtaIntent);
        finish();
    }

    private void getFwById(int firmwareId) {
        if (null == mDeviceInfo) {
            return;
        }

        // TODO backend service
    }

    private void getLatestFw() {
        if (null == mDeviceInfo) {
            return;
        }

        // TODO backend service
    }

    private void getTestFw() {
        try {
            String[] otaFiles = getAssets().list("VenusOTA");
            if (otaFiles == null || otaFiles.length == 0) {
                Toast.makeText(this, "No firmware file found", Toast.LENGTH_SHORT).show();
                return;
            }
            showFirmwareSelectionDialog(otaFiles);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read firmware file list", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFirmwareSelectionDialog(String[] otaFiles) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Firmware File");
        builder.setItems(otaFiles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedFile = otaFiles[which];
                toTestOtaAct(selectedFile);
            }
        });
        builder.setNegativeButton(R.string.btn_cancel, null);

        Dialog mAlertDialog = builder.create();
        mAlertDialog.show();
        if (mAlertDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = mAlertDialog.getWindow().getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 8;
            lp.gravity = Gravity.CENTER;
            mAlertDialog.getWindow().setAttributes(lp);
        }
    }

    private void showNewFirmwareDialog(LatestFwInfo latestFwInfo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.ota_found_new));
        builder.setMessage(latestFwInfo.getVerName() + " " + latestFwInfo.getVerCode() + "\n" + latestFwInfo.getDescription());
        builder.setNegativeButton(R.string.btn_cancel, null);
        builder.setPositiveButton(R.string.ota_btn_new, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                toDeviceOtaAct(latestFwInfo);
            }
        });

        Dialog mAlertDialog = builder.create();
        mAlertDialog.show();
        if (mAlertDialog.getWindow() != null) {
            WindowManager.LayoutParams lp = mAlertDialog.getWindow().getAttributes();
            lp.width = getWindowManager().getDefaultDisplay().getWidth() / 10 * 8;
            lp.gravity = Gravity.CENTER;
            mAlertDialog.getWindow().setAttributes(lp);
        }
    }
}