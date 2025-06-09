package com.jueyuantech.glasses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jueyuantech.glasses.device.DeviceManager;

public class NormalPermissionActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackIv;

    private RelativeLayout mMicrophoneContainerRl;
    private ImageView mMicrophoneStateIv;
    private RelativeLayout mDiscoverContainerRl;
    private ImageView mDiscoverStateIv;
    private RelativeLayout mLocationContainerRl;
    private ImageView mLocationStateIv;
    private RelativeLayout mFileContainerRl;
    private ImageView mFileStateIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_permission);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mMicrophoneContainerRl = findViewById(R.id.rl_container_microphone);
        mMicrophoneContainerRl.setOnClickListener(this);
        mMicrophoneStateIv = findViewById(R.id.iv_microphone_state);

        mDiscoverContainerRl = findViewById(R.id.rl_container_discover);
        mDiscoverContainerRl.setOnClickListener(this);
        mDiscoverStateIv = findViewById(R.id.iv_discover_state);

        mLocationContainerRl = findViewById(R.id.rl_container_location);
        mLocationContainerRl.setOnClickListener(this);
        mLocationStateIv = findViewById(R.id.iv_location_state);

        mFileContainerRl = findViewById(R.id.rl_container_file);
        mFileContainerRl.setOnClickListener(this);
        mFileStateIv = findViewById(R.id.iv_file_state);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.rl_container_discover:
                if (isDiscoverPermissionGranted()) {

                } else {
                    requestDiscoverPermissions();
                }
            case R.id.rl_container_microphone:
                if (isSttPermissionGranted()) {

                } else {
                    requestSttPermissions();
                }
                break;
            case R.id.rl_container_location:
                if (isMapPermissionGranted()) {

                } else {
                    requestMapPermissions();
                }
                break;
            case R.id.rl_container_file:
                if (isPrompterPermissionGranted()) {

                } else {
                    requestFilePermissions();
                }
                break;
        }
    }

    private void updateView() {
        if (isDiscoverPermissionGranted()) {
            mDiscoverStateIv.setImageResource(R.drawable.baseline_check_circle_24);
        } else {
            mDiscoverStateIv.setImageResource(R.drawable.baseline_help_24);
        }
        if (isSttPermissionGranted()) {
            mMicrophoneStateIv.setImageResource(R.drawable.baseline_check_circle_24);
        } else {
            mMicrophoneStateIv.setImageResource(R.drawable.baseline_help_24);
        }
        if (isMapPermissionGranted()) {
            mLocationStateIv.setImageResource(R.drawable.baseline_check_circle_24);
        } else {
            mLocationStateIv.setImageResource(R.drawable.baseline_help_24);
        }
        if (isPrompterPermissionGranted()) {
            mFileStateIv.setImageResource(R.drawable.baseline_check_circle_24);
        } else {
            mFileStateIv.setImageResource(R.drawable.baseline_help_24);
        }
    }

    /* Permission Check START */
    private boolean isDiscoverPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private boolean isSttPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private boolean isMapPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private boolean isPrompterPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private static final int PERMISSION_REQUEST_CODE_DISCOVER = 1;
    private static final int PERMISSION_REQUEST_CODE_STT = 2;
    private static final int PERMISSION_REQUEST_CODE_ASSISTANT = 3;
    private static final int PERMISSION_REQUEST_CODE_MAP = 4;
    private static final int PERMISSION_REQUEST_CODE_PROMPTER = 5;

    private void requestDiscoverPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.BLUETOOTH_SCAN,
                            android.Manifest.permission.BLUETOOTH_CONNECT
                    }, PERMISSION_REQUEST_CODE_DISCOVER);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, PERMISSION_REQUEST_CODE_DISCOVER);
        }
    }

    private void requestSttPermissions() {
        requestPermissions(new String[]{
                Manifest.permission.RECORD_AUDIO
        }, PERMISSION_REQUEST_CODE_ASSISTANT);
    }

    private void requestMapPermissions() {
        requestPermissions(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }, PERMISSION_REQUEST_CODE_MAP);
    }

    private void requestFilePermissions() {
        requestPermissions(new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, PERMISSION_REQUEST_CODE_PROMPTER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }

        updateView();
        if (granted) {

        } else {
            showGrantPermissionDialog(requestCode);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showGrantPermissionDialog(int permissionCode) {
        int titleResId;
        int contentResId;
        switch (permissionCode) {
            case PERMISSION_REQUEST_CODE_STT:
            case PERMISSION_REQUEST_CODE_ASSISTANT:
                titleResId = R.string.permission_dialog_stt_title;
                contentResId = R.string.permission_dialog_stt_content;
                break;
            case PERMISSION_REQUEST_CODE_MAP:
                titleResId = R.string.permission_dialog_map_title;
                contentResId = R.string.permission_dialog_map_content;
                break;
            case PERMISSION_REQUEST_CODE_PROMPTER:
                titleResId = R.string.permission_dialog_prompter_title;
                contentResId = R.string.permission_dialog_prompter_content;
                break;
            default:
                titleResId = R.string.permission_dialog_discover_title;
                contentResId = R.string.permission_dialog_discover_content;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleResId);
        builder.setMessage(contentResId);
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
    /* Permission Check END */
}