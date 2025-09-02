package com.jueyuantech.glasses.ui.main;

import static com.jueyuantech.glasses.common.Constants.STT_FUNC_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.STT_FUNC_TRANSLATE;
import static com.jueyuantech.glasses.common.Constants.MMKV_STT_FUNC_KEY;

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
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.MapsInitializer;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.jueyuantech.glasses.DeviceInfoActivity;
import com.jueyuantech.glasses.GestureActivity;
import com.jueyuantech.glasses.NotificationPushConfigActivity;
import com.jueyuantech.glasses.NotificationPushManager;
import com.jueyuantech.glasses.PrompterLegacyActivity;
import com.jueyuantech.glasses.R;
import com.jueyuantech.glasses.RecorderActivity;
import com.jueyuantech.glasses.ScanActivity;
import com.jueyuantech.glasses.Spark40Activity;
import com.jueyuantech.glasses.amap.NaviActivity;
import com.jueyuantech.glasses.bean.LanguageTag;
import com.jueyuantech.glasses.databinding.FragmentMainBinding;
import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.stt.SttConfigManager;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.VNConstant;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.config.IndicatorConfig;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.RectangleIndicator;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private FragmentMainBinding binding;

    private NotificationPushManager mNotificationPushManager;
    private SwitchCompat mNotificationSwitcher;

    private Banner mBanner;
    private TextView mDeviceLabelTv;
    private ImageView mDeviceIv;
    private RelativeLayout mDeviceAddRl;
    private RelativeLayout mFuncSttSwitchRl;
    private TextView mFuncSttTv;
    private TextView mFuncSttConfigTv;
    private ImageView mRecordBtn;

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
    private RelativeLayout mFuncNotificationRl;
    private RelativeLayout mFuncAiRl;
    private RelativeLayout mDeviceBoundContainerRl;
    private RelativeLayout mBtStateContainerRl;
    private ImageView mBtStateIv;
    private TextView mBtStateTv;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mNotificationPushManager = NotificationPushManager.getInstance();

        MainViewModel mainViewModel =
                new ViewModelProvider(this).get(MainViewModel.class);

        binding = FragmentMainBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mBanner = binding.banner;
        mDeviceLabelTv = binding.tvLabelDevice;

        mDeviceIv = binding.ivDevice;
        mDeviceIv.setOnClickListener(this);
        mDeviceIv.setOnTouchListener(this);

        mDeviceAddRl = binding.rlContainerAddDevice;
        mDeviceAddRl.setOnClickListener(this);
        mDeviceAddRl.setOnTouchListener(this);

        mFuncSttSwitchRl = binding.rlFuncSttSwitch;
        mFuncSttSwitchRl.setOnClickListener(this);
        mFuncSttSwitchRl.setOnTouchListener(this);

        mFuncSttTv = binding.tvFuncStt;
        mFuncSttConfigTv = binding.tvFuncSttConfig;

        mRecordBtn = binding.ibRecord;
        mRecordBtn.setOnClickListener(this);
        mRecordBtn.setOnTouchListener(this);

        mBatteryLevelRl = binding.rlContainerBattery;
        mBatteryLevelRl.setOnClickListener(this);
        mBatteryLevelRl.setOnTouchListener(this);
        mBatteryLevelTv = binding.tvBatteryLevel;
        mBatteryLevelIv = binding.ivBatteryLevel;
        mBatteryChargeIv = binding.ivBatteryCharge;

        mDeviceStatusRl = binding.rlContainerDeviceStatus;
        mDeviceSysStateIv = binding.ivDeviceSysState;

        mFuncMapRl = binding.rlFuncNav;
        mFuncMapRl.setOnClickListener(this);
        mFuncMapRl.setOnTouchListener(this);

        mFuncPrompterLegacyRl = binding.rlFuncPrompterLegacy;
        mFuncPrompterLegacyRl.setOnClickListener(this);
        mFuncPrompterLegacyRl.setOnTouchListener(this);

        mFuncNotificationRl = binding.rlContainerNotificationPush;
        mFuncNotificationRl.setOnClickListener(this);
        mFuncNotificationRl.setOnTouchListener(this);

        mNotificationSwitcher = binding.switchNotification;
            mNotificationSwitcher.setClickable(false);

        mFuncAiRl = binding.rlFuncAi;
        mFuncAiRl.setOnClickListener(this);
        mFuncAiRl.setOnTouchListener(this);

        mTouchPanelRl = binding.rlContainerTouchPanel;
        mTouchPanelRl.setOnClickListener(this);
        mTouchPanelRl.setOnTouchListener(this);

        mDeviceSettingRl = binding.rlContainerDeviceSettings;
        mDeviceSettingRl.setOnClickListener(this);
        mDeviceSettingRl.setOnTouchListener(this);

        mDeviceBoundContainerRl = binding.rlContainerDeviceBound;

        mBtStateContainerRl = binding.rlContainerBtState;
        mBtStateContainerRl.setOnClickListener(this);
        mBtStateContainerRl.setOnTouchListener(this);
        mBtStateIv = binding.ivBtState;
        mBtStateTv = binding.tvBtState;

        return root;
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

        //initBannerPlayer();
        updateSettingsViews();
        updateDeviceViews();
    }

    @Override
    public void onStop() {
        super.onStop();
        DeviceManager.getInstance().removeDeviceStatusListener(statusListener);
        DeviceManager.getInstance().removeDeviceServiceListener(serviceListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_record:
                if (DeviceManager.getInstance().isBound()) {
                    if (VNCommon.isConnected()) {
                        if (isSttPermissionGranted()) {
                            toRecordAct();
                        } else {
                            requestPermissions(new String[]{
                                    Manifest.permission.RECORD_AUDIO
                            }, PERMISSION_REQUEST_CODE_STT);
                        }
                    } else {
                        ToastUtil.toast(getActivity(), R.string.tips_no_device_connected);
                    }
                } else {
                    toScanAct();
                }
                break;
            case R.id.rl_container_add_device:
                toScanAct();
                break;
            case R.id.rl_func_stt_switch:
                showFuncSttConfigDialog();
                break;
            case R.id.rl_func_nav:
                if (isMapPermissionGranted()) {
                    toMapAct();
                } else {
                    requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    }, PERMISSION_REQUEST_CODE_MAP);
                }
                break;
            case R.id.rl_func_prompter_legacy:
                if (isPrompterPermissionGranted()) {
                    toPrompterLegacyAct();
                } else {
                    requestPermissions(new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, PERMISSION_REQUEST_CODE_PROMPTER);
                }
                break;
            case R.id.rl_container_notification_push:
                toNotificationConfigAct();
                break;
            case R.id.rl_func_ai:
                if (DeviceManager.getInstance().isBound()) {
                    if (VNCommon.isConnected()) {
                        if (isSttPermissionGranted()) {
                            toSpark40Act();
                        } else {
                            requestPermissions(new String[]{
                                    Manifest.permission.RECORD_AUDIO
                            }, PERMISSION_REQUEST_CODE_ASSISTANT);
                        }
                    } else {
                        ToastUtil.toast(getActivity(), R.string.tips_no_device_connected);
                    }
                } else {
                    toSpark40Act();
                }
                break;
            case R.id.rl_container_touch_panel:
                toTouchPanelAct();
                break;
            case R.id.rl_container_device_settings:
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
            return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private boolean isSttPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private boolean isMapPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private boolean isPrompterPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
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
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{
                            android.Manifest.permission.BLUETOOTH_SCAN,
                            android.Manifest.permission.BLUETOOTH_CONNECT
                    }, PERMISSION_REQUEST_CODE_DISCOVER);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(getActivity(),
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
                    toRecordAct();
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
            case PERMISSION_REQUEST_CODE_PROMPTER:
                if (granted) {
                    toPrompterLegacyAct();
                } else {
                    showGrantPermissionDialog(requestCode);
                }
                break;
            default:

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
            lp.width = getActivity().getWindowManager().getDefaultDisplay().getWidth() / 10 * 9;
            lp.gravity = Gravity.CENTER;
            mAlertDialog.getWindow().setAttributes(lp);
        }
    }

    private void toPermissionGrantAct() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
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

    private void initBannerPlayer() {
        List images = new ArrayList();
        images.add(R.mipmap.ic_device_model_h1);

        mBanner.setAdapter(new BannerImageAdapter<Integer>(images) {
            @Override
            public void onBindView(BannerImageHolder bannerImageHolder, Integer integer, int i, int i1) {
                bannerImageHolder.imageView.setImageResource(integer);
            }
        });
        mBanner.isAutoLoop(true);
        mBanner.setIndicator(new RectangleIndicator(getContext()));
        mBanner.setScrollBarFadeDuration(3000);
        mBanner.setIndicatorSelectedColor(getActivity().getColor(R.color.blue_sky));
        mBanner.setIndicatorGravity(IndicatorConfig.Direction.CENTER);
        mBanner.start();
    }

    private void updateConnectingViews() {
        mBtStateTv.setText(R.string.bt_state_connecting);
        mBtStateTv.setTextColor(getResources().getColor(R.color.venus_green));
        mBtStateIv.setImageResource(R.drawable.ic_device_state_connectting);
    }

    private void toScanAct() {
        Intent scanIntent = new Intent();
        scanIntent.setClass(getActivity(), ScanActivity.class);
        getActivity().startActivity(scanIntent);
    }

    private void toRecordAct() {
        String func = MmkvUtil.decodeString(MMKV_STT_FUNC_KEY, STT_FUNC_DEFAULT);
        Intent recordIntent = new Intent(getActivity(), RecorderActivity.class);
        recordIntent.putExtra("func", func);
        getActivity().startActivity(recordIntent);
    }

    private void toTouchPanelAct() {
        Intent touchPanelIntent = new Intent(getActivity(), GestureActivity.class);
        getActivity().startActivity(touchPanelIntent);
    }

    private void toDeviceSettingAct() {
        Intent deviceInfoIntent = new Intent(getActivity(), DeviceInfoActivity.class);
        getActivity().startActivity(deviceInfoIntent);
    }

    private void toMapAct() {
        //Intent intent = new Intent(getActivity(), NaviActivity.class);
        //intent.putExtra("useInnerVoice", false);
        //startActivity(intent);

        //Intent mapIntent = new Intent(getActivity(), MapActivity.class);
        //getActivity().startActivity(mapIntent);

        // 这一步操作就设置高德地图中的隐私合规，不然可能会出现地图无法正确加载的问题
        MapsInitializer.updatePrivacyShow(getContext(), true, true);
        MapsInitializer.updatePrivacyAgree(getContext(), true);

        AmapNaviPage.getInstance().showRouteActivity(
                getActivity().getApplicationContext(),
                new AmapNaviParams(null, null, null, AmapNaviType.DRIVER, AmapPageType.ROUTE),
                NaviActivity.iNaviInfoCallback,
                NaviActivity.class);
    }

    private void toPrompterLegacyAct() {
        Intent prompterLegacyIntent = new Intent(getActivity(), PrompterLegacyActivity.class);
        getActivity().startActivity(prompterLegacyIntent);
    }

    private void toNotificationConfigAct() {
        Intent notificationConfigIntent = new Intent(getActivity(), NotificationPushConfigActivity.class);
        startActivity(notificationConfigIntent);
    }

    private void toSpark40Act() {
        Intent spark40Intent = new Intent(getActivity(), Spark40Activity.class);
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
        funcConfigFragment.show(getChildFragmentManager(), "funcConfig");
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

        }
    };
}