package com.jueyuantech.glasses;

import static com.jueyuantech.glasses.common.Constants.MMKV_STT_FUNC_KEY;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.MapsInitializer;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.google.android.material.snackbar.Snackbar;
import com.jueyuantech.glasses.amap.NaviActivity;
import com.jueyuantech.glasses.bean.LanguageTag;
import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.stt.SttConfigManager;
import com.jueyuantech.glasses.stt.SttWorker;
import com.jueyuantech.glasses.stt.SttWorkerCallback;
import com.jueyuantech.glasses.ui.main.FuncConfigFragment;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNCommon;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private NotificationPushManager mNotificationPushManager;
    private SwitchCompat mNotificationSwitcher;

    private TextView mDeviceLabelTv;
    private ImageView mAvatarIv;
    private ImageView mDeviceIv;
    private TextView mSttPreviewTv;
    private RelativeLayout mDeviceAddRl;
    private RelativeLayout mFuncSttSwitchRl;
    private TextView mFuncSttTv;
    private TextView mFuncSttConfigTv;
    private ImageView mRecordBtn;
    private ImageView mSttSessionListBtn;

    private RelativeLayout mBatteryLevelRl;
    private TextView mBatteryLevelTv;
    private ImageView mBatteryLevelIv;
    private ImageView mBatteryChargeIv;
    private ImageView mDeviceSysStateIv;
    private RelativeLayout mDeviceStatusRl;
    private RelativeLayout mTouchPanelRl;
    private RelativeLayout mDeviceSettingRl;
    private RelativeLayout mFuncMapRl;
    private RelativeLayout mFuncPrompterLegacyRl;
    private RelativeLayout mFuncPrompterRl;
    private RelativeLayout mFuncReaderRl;
    private RelativeLayout mFuncNotificationRl;
    private RelativeLayout mFuncAiRl;
    private RelativeLayout mDeviceBoundContainerRl;
    private RelativeLayout mBtStateContainerRl;
    private ImageView mBtStateIv;
    private TextView mBtStateTv;

    private WindowManager mWindowManager;
    private View mPermissionTipView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        mNotificationPushManager = NotificationPushManager.getInstance();

        mDeviceLabelTv = findViewById(R.id.tv_label_device);

        mAvatarIv = findViewById(R.id.iv_avatar);
        mAvatarIv.setOnClickListener(this);
        mAvatarIv.setOnTouchListener(this);

        mDeviceIv = findViewById(R.id.iv_device);
        mDeviceIv.setOnClickListener(this);
        mDeviceIv.setOnTouchListener(this);

        mSttPreviewTv = findViewById(R.id.tv_stt_preview);

        mDeviceAddRl = findViewById(R.id.rl_container_add_device);
        mDeviceAddRl.setOnClickListener(this);
        mDeviceAddRl.setOnTouchListener(this);

        mFuncSttSwitchRl = findViewById(R.id.rl_func_stt_switch);
        mFuncSttSwitchRl.setOnClickListener(this);
        mFuncSttSwitchRl.setOnTouchListener(this);

        mFuncSttTv = findViewById(R.id.tv_func_stt);
        mFuncSttConfigTv = findViewById(R.id.tv_func_stt_config);

        mRecordBtn = findViewById(R.id.ib_record);
        mRecordBtn.setOnClickListener(this);
        mRecordBtn.setOnTouchListener(this);

        mSttSessionListBtn = findViewById(R.id.iv_stt_session_list);
        mSttSessionListBtn.setOnClickListener(this);
        mSttSessionListBtn.setOnTouchListener(this);

        mBatteryLevelRl = findViewById(R.id.rl_container_battery);
        mBatteryLevelRl.setOnClickListener(this);
        mBatteryLevelRl.setOnTouchListener(this);
        mBatteryLevelTv = findViewById(R.id.tv_battery_level);
        mBatteryLevelIv = findViewById(R.id.iv_battery_level);
        mBatteryChargeIv = findViewById(R.id.iv_battery_charge);

        mDeviceStatusRl = findViewById(R.id.rl_container_device_status);
        mDeviceSysStateIv = findViewById(R.id.iv_device_sys_state);

        mFuncMapRl = findViewById(R.id.rl_func_nav);
        mFuncMapRl.setOnClickListener(this);
        mFuncMapRl.setOnTouchListener(this);

        mFuncPrompterLegacyRl = findViewById(R.id.rl_func_prompter_legacy);
        mFuncPrompterLegacyRl.setOnClickListener(this);
        mFuncPrompterLegacyRl.setOnTouchListener(this);

        mFuncPrompterRl = findViewById(R.id.rl_func_prompter);
        mFuncPrompterRl.setOnClickListener(this);
        mFuncPrompterRl.setOnTouchListener(this);

        mFuncReaderRl = findViewById(R.id.rl_func_reader);
        mFuncReaderRl.setOnClickListener(this);
        mFuncReaderRl.setOnTouchListener(this);

        mFuncNotificationRl = findViewById(R.id.rl_container_notification_push);
        mFuncNotificationRl.setOnClickListener(this);
        mFuncNotificationRl.setOnTouchListener(this);

        mNotificationSwitcher = findViewById(R.id.switch_notification);
        mNotificationSwitcher.setClickable(false);

        mFuncAiRl = findViewById(R.id.rl_func_ai);
        mFuncAiRl.setOnClickListener(this);
        mFuncAiRl.setOnTouchListener(this);

        mTouchPanelRl = findViewById(R.id.rl_container_touch_panel);
        mTouchPanelRl.setOnClickListener(this);
        mTouchPanelRl.setOnTouchListener(this);

        mDeviceSettingRl = findViewById(R.id.rl_container_device_settings);
        mDeviceSettingRl.setOnClickListener(this);
        mDeviceSettingRl.setOnTouchListener(this);

        mDeviceBoundContainerRl = findViewById(R.id.rl_container_device_bound);

        mBtStateContainerRl = findViewById(R.id.rl_container_bt_state);
        mBtStateContainerRl.setOnClickListener(this);
        mBtStateContainerRl.setOnTouchListener(this);
        mBtStateIv = findViewById(R.id.iv_bt_state);
        mBtStateTv = findViewById(R.id.tv_bt_state);
    }

    @Override
    public void onStart() {
        super.onStart();
        DeviceManager.getInstance().addDeviceStatusListener(statusListener);
        DeviceManager.getInstance().addDeviceServiceListener(serviceListener);

        checkDiscoverPermissionAndInit();
    }

    @Override
    public void onResume() {
        super.onResume();

        updateSettingsViews();
        updateDeviceViews();

        SttWorker.getInstance().addSttWorkerCallback(sttWorkerCallback);
    }

    @Override
    protected void onPause() {
        SttWorker.getInstance().addSttWorkerCallback(sttWorkerCallback);

        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        DeviceManager.getInstance().removeDeviceStatusListener(statusListener);
        DeviceManager.getInstance().removeDeviceServiceListener(serviceListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_avatar:
                toProfileAct();
                break;
            case R.id.ib_record:
                if (redirectToBindingIfUnbind()) {
                    return;
                }
                if (showSthIfNoConnected()) {
                    return;
                }
                if (isSttPermissionGranted()) {
                    int sttState = SttWorker.getInstance().getCurState();
                    if (sttState == SttWorker.STATE_STOPPED) {
                        startStt();
                    } else if (sttState == SttWorker.STATE_STARTED) {
                        stopStt();
                    }
                } else {
                    showPermissionTip(
                            getString(R.string.permission_dialog_stt_title),
                            getString(R.string.permission_dialog_stt_content)
                    );
                    requestPermissions(new String[]{
                            android.Manifest.permission.RECORD_AUDIO
                    }, PERMISSION_REQUEST_CODE_STT);
                }
                break;
            case R.id.iv_stt_session_list:
                int sttState = SttWorker.getInstance().getCurState();
                if (sttState == SttWorker.STATE_STOPPED) {
                    toSessionListAct();
                } else if (sttState == SttWorker.STATE_STARTED) {
                    toSttAct();
                }
                break;
            case R.id.rl_container_add_device:
                toScanAct();
                break;
            case R.id.rl_func_stt_switch:
                if (showSthIfBusy()) {
                    return;
                }
                showFuncSttConfigDialog();
                break;
            case R.id.rl_func_nav:
                if (redirectToBindingIfUnbind()) {
                    return;
                }
                if (showSthIfNoConnected()) {
                    return;
                }
                if (showSthIfBusy()) {
                    return;
                }
                if (isMapPermissionGranted()) {
                    toMapAct();
                } else {
                    showPermissionTip(
                            getString(R.string.permission_dialog_map_title),
                            getString(R.string.permission_dialog_map_content)
                    );
                    requestPermissions(new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    }, PERMISSION_REQUEST_CODE_MAP);
                }
                break;
            case R.id.rl_func_prompter_legacy:
                if (redirectToBindingIfUnbind()) {
                    return;
                }
                if (showSthIfNoConnected()) {
                    return;
                }
                if (showSthIfBusy()) {
                    return;
                }
                if (isPrompterPermissionGranted()) {
                    toPrompterLegacyAct();
                } else {
                    showPermissionTip(
                            getString(R.string.permission_dialog_prompter_title),
                            getString(R.string.permission_dialog_prompter_content)
                    );
                    requestPermissions(new String[]{
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, PERMISSION_REQUEST_CODE_PROMPTER_LEGACY);
                }
                break;
            case R.id.rl_func_prompter:
                if (redirectToBindingIfUnbind()) {
                    return;
                }
                if (showSthIfNoConnected()) {
                    return;
                }
                if (showSthIfBusy()) {
                    return;
                }
                if (isPrompterPermissionGranted()) {
                    toPrompterAct();
                } else {
                    showPermissionTip(
                            getString(R.string.permission_dialog_prompter_title),
                            getString(R.string.permission_dialog_prompter_content)
                    );
                    requestPermissions(new String[]{
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, PERMISSION_REQUEST_CODE_PROMPTER);
                }
                break;
            case R.id.rl_func_reader:
                if (redirectToBindingIfUnbind()) {
                    return;
                }
                if (showSthIfNoConnected()) {
                    return;
                }
                if (showSthIfBusy()) {
                    return;
                }
                if (isReaderPermissionGranted()) {
                    toReaderAct();
                } else {
                    showPermissionTip(
                            getString(R.string.permission_dialog_reader_title),
                            getString(R.string.permission_dialog_reader_content)
                    );
                    requestPermissions(new String[]{
                            android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, PERMISSION_REQUEST_CODE_READER);
                }
                break;
            case R.id.rl_container_notification_push:
                toNotificationConfigAct();
                break;
            case R.id.rl_func_ai:
                if (redirectToBindingIfUnbind()) {
                    return;
                }
                if (showSthIfNoConnected()) {
                    return;
                }
                if (showSthIfBusy()) {
                    return;
                }
                if (isSttPermissionGranted()) {
                    toSpark40Act();
                } else {
                    showPermissionTip(
                            getString(R.string.permission_dialog_stt_title),
                            getString(R.string.permission_dialog_stt_content)
                    );
                    requestPermissions(new String[]{
                            Manifest.permission.RECORD_AUDIO
                    }, PERMISSION_REQUEST_CODE_ASSISTANT);
                }
                break;
            case R.id.rl_container_touch_panel:
                if (redirectToBindingIfUnbind()) {
                    return;
                }
                if (showSthIfNoConnected()) {
                    return;
                }
                toTouchPanelAct();
                break;
            case R.id.rl_container_device_settings:
                if (redirectToBindingIfUnbind()) {
                    return;
                }
                if (showSthIfNoConnected()) {
                    return;
                }
                toDeviceSettingAct();
                break;
            default:
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                break;
            case MotionEvent.ACTION_UP:
                //view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY_RELEASE);
                break;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean redirectToBindingIfUnbind() {
        if (DeviceManager.getInstance().isBound()) {
            return false;
        } else {
            toScanAct();
            return true;
        }
    }

    private boolean showSthIfNoConnected() {
        if (VNCommon.isConnected()) {
            return false;
        } else {
            ToastUtil.toast(this, R.string.tips_no_device_connected);
            return true;
        }
    }

    private boolean showSthIfBusy() {
        if (SttWorker.getInstance().isBusy()) {
            ToastUtil.toast(getApplicationContext(), R.string.tips_stt_running);
            return true;
        } else {
            return false;
        }
    }

    private void startStt() {
        String func = MmkvUtil.decodeString(MMKV_STT_FUNC_KEY, STT_FUNC_DEFAULT);

        int venusApp = VNConstant.View.TRANSCRIBE;
        if (STT_FUNC_TRANSCRIBE.equals(func)) {
            venusApp = VNConstant.View.TRANSCRIBE;
        } else if (STT_FUNC_TRANSLATE.equals(func)) {
            venusApp = VNConstant.View.TRANSLATE;
        }
        VNCommon.setView(venusApp, null);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SttWorker.getInstance().startWork(func);
            }
        }, 500);
    }

    private void stopStt() {
        SttWorker.getInstance().stopWork();
        VNCommon.setView(VNConstant.View.HOME, null);
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

    /* Permission Check START */
    private void checkDiscoverPermissionAndInit() {
        if (DeviceManager.getInstance().isBound()) {
            if (isDiscoverPermissionGranted()) {
                DeviceManager.getInstance().initDevice();
            } else {
                requestDiscoverPermissions();
            }
        } else {
            // Do nothing, no device bound to Connect
        }
    }

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
        // FIXME get file by ACTION_GET_CONTENT is granted always?
        return true;

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
        */
    }

    private boolean isReaderPermissionGranted() {
        // FIXME get file by ACTION_GET_CONTENT is granted always?
        return true;

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
        */
    }

    private static final int PERMISSION_REQUEST_CODE_DISCOVER = 1;
    private static final int PERMISSION_REQUEST_CODE_STT = 2;
    private static final int PERMISSION_REQUEST_CODE_ASSISTANT = 3;
    private static final int PERMISSION_REQUEST_CODE_MAP = 4;
    private static final int PERMISSION_REQUEST_CODE_PROMPTER_LEGACY = 105;
    private static final int PERMISSION_REQUEST_CODE_PROMPTER = 5;
    private static final int PERMISSION_REQUEST_CODE_READER = 6;

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_DISCOVER:
                if (granted) {
                    DeviceManager.getInstance().initDevice();
                } else {
                    showGrantPermissionDialog(requestCode);
                }
                break;
            case PERMISSION_REQUEST_CODE_STT:
                if (granted) {
                    startStt();
                } else {
                    showGrantPermissionDialog(requestCode);
                }
                break;
            case PERMISSION_REQUEST_CODE_ASSISTANT:
                if (granted) {
                    toSpark40Act();
                } else {
                    showGrantPermissionDialog(requestCode);
                }
                break;
            case PERMISSION_REQUEST_CODE_MAP:
                if (granted) {
                    toMapAct();
                } else {
                    showGrantPermissionDialog(requestCode);
                }
                break;
            case PERMISSION_REQUEST_CODE_PROMPTER_LEGACY:
                if (granted) {
                    toPrompterLegacyAct();
                } else {
                    showGrantPermissionDialog(requestCode);
                }
                break;
            case PERMISSION_REQUEST_CODE_PROMPTER:
                if (granted) {
                    toPrompterAct();
                } else {
                    showGrantPermissionDialog(requestCode);
                }
                break;
            case PERMISSION_REQUEST_CODE_READER:
                if (granted) {
                    toReaderAct();
                } else {
                    showGrantPermissionDialog(requestCode);
                }
                break;
            default:

        }

        dismissPermissionTip();
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
            case PERMISSION_REQUEST_CODE_PROMPTER_LEGACY:
            case PERMISSION_REQUEST_CODE_PROMPTER:
                titleResId = R.string.permission_dialog_prompter_title;
                contentResId = R.string.permission_dialog_prompter_content;
                break;
            case PERMISSION_REQUEST_CODE_READER:
                titleResId = R.string.permission_dialog_reader_title;
                contentResId = R.string.permission_dialog_reader_content;
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

    private void updateSettingsViews() {
        String engine = SttConfigManager.getInstance().getEngine();
        String func = SttConfigManager.getInstance().getFunc();
        if (STT_FUNC_TRANSCRIBE.equals(func)) {
            mFuncSttTv.setText(R.string.func_stt);
            LanguageTag sourceLanTag = SttConfigManager.getInstance().getSourceLanTag(engine, func);
            if (null == sourceLanTag) {
                mFuncSttConfigTv.setText(R.string.tips_coming);
            } else {
                mFuncSttConfigTv.setText(sourceLanTag.getTitle());
            }
        } else if (STT_FUNC_TRANSLATE.equals(func)) {
            mFuncSttTv.setText(R.string.func_trans);
            LanguageTag sourceLanTag = SttConfigManager.getInstance().getSourceLanTag(engine, func);
            LanguageTag targetLanTag = SttConfigManager.getInstance().getTargetLanTag(engine, func, (null == sourceLanTag ? "" : sourceLanTag.getTag()));
            if (null == sourceLanTag || null == targetLanTag) {
                mFuncSttConfigTv.setText(R.string.tips_coming);
            } else {
                mFuncSttConfigTv.setText(sourceLanTag.getTitle() + " -> " + targetLanTag.getTitle());
            }
        }

        int sttState = SttWorker.getInstance().getCurState();
        switch (sttState) {
            case SttWorker.STATE_STARTED:
                mRecordBtn.setImageResource(R.drawable.ic_mic_off);
                break;
            case SttWorker.STATE_STOPPED:
                mRecordBtn.setImageResource(R.drawable.ic_mic);
                break;
            default:
                mRecordBtn.setImageResource(R.drawable.ic_mic_busy);
        }

        if (mNotificationPushManager.isPushEnable()) {
            mNotificationSwitcher.setChecked(true);
        } else {
            mNotificationSwitcher.setChecked(false);
        }
    }

    private void updateDeviceViews() {
        if (DeviceManager.getInstance().isBound()) {
            mDeviceBoundContainerRl.setVisibility(View.VISIBLE);
            mDeviceIv.setImageResource(R.mipmap.ic_device_model_h1);
            mDeviceAddRl.setVisibility(View.GONE);

            if (VNCommon.isConnected()) {
                mBtStateTv.setText(R.string.bt_state_connected);
                mBtStateTv.setTextColor(getResources().getColor(R.color.venus_green));
                mBtStateIv.setImageResource(R.drawable.ic_device_state_connected);

                mBatteryLevelRl.setVisibility(View.VISIBLE);
                mDeviceStatusRl.setVisibility(View.VISIBLE);
            } else {
                mBtStateTv.setText(R.string.bt_state_disconnected);
                mBtStateTv.setTextColor(getResources().getColor(R.color.gray_700));
                mBtStateIv.setImageResource(R.drawable.ic_device_state_disconnected);

                mBatteryLevelRl.setVisibility(View.GONE);
                mDeviceStatusRl.setVisibility(View.GONE);
            }
        } else {
            mDeviceBoundContainerRl.setVisibility(View.GONE);
            mDeviceIv.setImageResource(R.mipmap.ic_device_model_h1_offline);
            mDeviceAddRl.setVisibility(View.VISIBLE);
        }
    }

    private void updateBatteryLevel(int batteryLevel) {
        LogUtil.i("batteryLevel=" + batteryLevel);
        if (batteryLevel < 0) {
            mBatteryLevelRl.setVisibility(View.GONE);
        } else {
            mBatteryLevelTv.setText(batteryLevel + "%");
            if (batteryLevel >= 0 && batteryLevel <= 10) {
                mBatteryLevelIv.setImageResource(R.drawable.ic_battery_level_0);
            } else if (batteryLevel > 10 && batteryLevel <= 20) {
                mBatteryLevelIv.setImageResource(R.drawable.ic_battery_level_1);
            } else if (batteryLevel > 20 && batteryLevel <= 55) {
                mBatteryLevelIv.setImageResource(R.drawable.ic_battery_level_2);
            } else if (batteryLevel > 55 && batteryLevel <= 90) {
                mBatteryLevelIv.setImageResource(R.drawable.ic_battery_level_3);
            } else if (batteryLevel > 90 && batteryLevel <= 100) {
                mBatteryLevelIv.setImageResource(R.drawable.ic_battery_level_4);
            }

            mBatteryLevelRl.setVisibility(View.VISIBLE);
        }
    }

    private void updateConnectingViews() {
        mBtStateTv.setText(R.string.bt_state_connecting);
        mBtStateTv.setTextColor(getResources().getColor(R.color.venus_green));
        mBtStateIv.setImageResource(R.drawable.ic_device_state_connectting);
    }

    private void toProfileAct() {
        Intent profileIntent = new Intent();
        profileIntent.setClass(this, ProfileActivity.class);
        startActivity(profileIntent);
    }

    private void toScanAct() {
        Intent scanIntent = new Intent();
        scanIntent.setClass(this, ScanActivity.class);
        startActivity(scanIntent);
    }

    private void toRecordAct() {
        String func = MmkvUtil.decodeString(MMKV_STT_FUNC_KEY, STT_FUNC_DEFAULT);
        Intent recordIntent = new Intent(this, RecorderActivity.class);
        recordIntent.putExtra("func", func);
        startActivity(recordIntent);
    }

    private void toSttAct() {
        String func = MmkvUtil.decodeString(MMKV_STT_FUNC_KEY, STT_FUNC_DEFAULT);
        if (SttWorker.getInstance().isBusy()) {
            func = SttWorker.getInstance().getWorkingFunc();
        }
        Intent sttIntent = new Intent(this, SttActivity.class);
        sttIntent.putExtra("func", func);
        startActivity(sttIntent);
    }

    private void toSessionListAct() {
        Intent sessionListIntent = new Intent(this, SpeechSessionListActivity.class);
        startActivity(sessionListIntent);
    }

    private void toTouchPanelAct() {
        Intent touchPanelIntent = new Intent(this, GestureActivity.class);
        startActivity(touchPanelIntent);
    }

    private void toDeviceSettingAct() {
        Intent deviceConfigIntent = new Intent(this, DeviceConfigActivity.class);
        startActivity(deviceConfigIntent);
    }

    private void toMapAct() {
        //Intent intent = new Intent(getActivity(), NaviActivity.class);
        //intent.putExtra("useInnerVoice", false);
        //startActivity(intent);

        //Intent mapIntent = new Intent(getActivity(), MapActivity.class);
        //getActivity().startActivity(mapIntent);

        // 这一步操作就设置高德地图中的隐私合规，不然可能会出现地图无法正确加载的问题
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);

        AmapNaviPage.getInstance().showRouteActivity(
                getApplicationContext(),
                new AmapNaviParams(null, null, null, AmapNaviType.DRIVER, AmapPageType.ROUTE),
                NaviActivity.iNaviInfoCallback,
                NaviActivity.class);
    }

    private void toPrompterLegacyAct() {
        Intent prompterLegacyIntent = new Intent(this, PrompterLegacyActivity.class);
        startActivity(prompterLegacyIntent);
    }

    private void toPrompterAct() {
        Intent prompterIntent = new Intent(this, PrompterActivity.class);
        startActivity(prompterIntent);
    }

    private void toReaderAct() {
        Intent readerIntent = new Intent(this, ReaderActivity.class);
        startActivity(readerIntent);
    }

    private void toNotificationConfigAct() {
        Intent notificationConfigIntent = new Intent(this, NotificationPushConfigActivity.class);
        startActivity(notificationConfigIntent);
    }

    private void toSpark40Act() {
        Intent spark40Intent = new Intent(this, Spark40Activity.class);
        startActivity(spark40Intent);
    }

    private void showFuncSttConfigDialog() {
        FuncConfigFragment funcConfigFragment = new FuncConfigFragment();
        funcConfigFragment.setListener(new FuncConfigFragment.Listener() {
            @Override
            public void onDestroyView() {
                updateSettingsViews();
            }
        });
        funcConfigFragment.show(getSupportFragmentManager(), "funcConfig");
    }

    private DeviceManager.DeviceServiceListener serviceListener = new DeviceManager.DeviceServiceListener() {
        @Override
        public void onStateChanged(int sysState) {
            updateDeviceViews();
        }
    };

    private DeviceManager.DeviceStatusListener statusListener = new DeviceManager.DeviceStatusListener() {
        @Override
        public void onSysStateChanged(int sysState) {
            LogUtil.i(sysState);
            if (VNConstant.SysState.SLEEP == sysState) {
                mDeviceSysStateIv.setVisibility(View.VISIBLE);
            } else {
                mDeviceSysStateIv.setVisibility(View.GONE);
            }
        }

        @Override
        public void onChargeStateChanged(int chargeState) {
            if (VNConstant.ChargeState.CHARGING == chargeState) {
                mBatteryChargeIv.setVisibility(View.VISIBLE);
            } else {
                mBatteryChargeIv.setVisibility(View.GONE);
            }
        }

        @Override
        public void onBatteryChanged(int battery) {
            updateBatteryLevel(battery);
        }

        @Override
        public void onViewChanged(int venusView) {
            if (VNConstant.View.HOME == venusView) {
                mSttPreviewTv.setText("");
            }
        }
    };

    private SttWorkerCallback sttWorkerCallback = new SttWorkerCallback() {
        @Override
        public void onWorkerInitComplete(String funcType, String engineType, String audioSource) {

        }

        @Override
        public void onWorkerStarting() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateSettingsViews();
                }
            });
        }

        @Override
        public void onWorkerStart() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateSettingsViews();
                }
            });
        }

        @Override
        public void onWorkerStopping() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateSettingsViews();
                }
            });
        }

        @Override
        public void onWorkerStop() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSttPreviewTv.setText("");
                    updateSettingsViews();
                }
            });
        }

        @Override
        public void onWorkerErr(int code, String msg, String cause) {
            if (1001401 == code) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoginSnakeBar(getString(R.string.tips_stt_err_service_auth_failed));
                    }
                });
            } else {
                toastUiThread(msg);
            }
        }

        @Override
        public void onEngineStart() {

        }

        @Override
        public void onEngineStop() {

        }

        @Override
        public void onEngineErr(int code, String msg, String cause) {
            toastUiThread(msg);
        }

        @Override
        public void onEngineTick(long time) {

        }

        @Override
        public void onRecorderStart(String audioFilePath, String audioFileName) {

        }

        @Override
        public void onRecorderStop() {

        }

        @Override
        public void onRecorderErr(int code, String msg, String cause) {
            toastUiThread(msg);
        }

        @Override
        public void onSttMessage(int type, String transcribeStr, String translateStr, boolean isEnd) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (TextUtils.isEmpty(translateStr)) {
                        mSttPreviewTv.setText(transcribeStr);
                    } else {
                        mSttPreviewTv.setText(translateStr);
                    }
                }
            });
        }

        @Override
        public void onSysMessage(int level, String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSttPreviewTv.setText(msg);
                }
            });
        }

        @Override
        public void onHintMessage(String transcribeHintStr, String translateHintStr) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (TextUtils.isEmpty(translateHintStr)) {
                        mSttPreviewTv.setText(transcribeHintStr);
                    } else {
                        mSttPreviewTv.setText(translateHintStr);
                    }
                }
            });
        }

        @Override
        public void onAudioTrackStateChanged(boolean silence) {

        }
    };

    private void toastUiThread(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.toast(getApplicationContext(), msg);
            }
        });
    }

    private void showLoginSnakeBar(String msg) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.setAction(R.string.label_go_login, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toProfileAct();
            }
        });
        View snackBarView = snackbar.getView();
        ViewGroup.LayoutParams layoutParams = snackBarView.getLayoutParams();
        // 重新设置属性参数
        FrameLayout.LayoutParams cl = new FrameLayout.LayoutParams(layoutParams.width, layoutParams.height);
        // 设置显示位置在上居中
        cl.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        cl.leftMargin = 20;
        cl.rightMargin = 20;
        snackBarView.setLayoutParams(cl);
        snackbar.show();
    }
}