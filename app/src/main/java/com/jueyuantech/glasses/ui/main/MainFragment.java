package com.jueyuantech.glasses.ui.main;

import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_TRANSCRIBE;
import static com.jueyuantech.glasses.common.Constants.ASR_FUNC_TRANSLATE;
import static com.jueyuantech.glasses.common.Constants.MMKV_ASR_FUNC_KEY;

import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.MapsInitializer;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.AmapNaviType;
import com.amap.api.navi.AmapPageType;
import com.jueyuantech.glasses.DeviceInfoActivity;
import com.jueyuantech.glasses.ExhibitActivity;
import com.jueyuantech.glasses.GestureActivity;
import com.jueyuantech.glasses.NotificationPushConfigActivity;
import com.jueyuantech.glasses.NotificationPushManager;
import com.jueyuantech.glasses.PrompterActivity;
import com.jueyuantech.glasses.R;
import com.jueyuantech.glasses.RecorderActivity;
import com.jueyuantech.glasses.Scan2Activity;
import com.jueyuantech.glasses.Spark40Activity;
import com.jueyuantech.glasses.amap.NaviActivity;
import com.jueyuantech.glasses.bean.LanguageTag;
import com.jueyuantech.glasses.databinding.FragmentMainBinding;
import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.stt.AsrConfigManager;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VenusSDK;
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
    private RelativeLayout mFuncSwitchRl;
    private TextView mFuncTv;
    private TextView mFuncConfigTv;
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
    private RelativeLayout mFuncPrompterRl;
    private RelativeLayout mFuncNotificationRl;
    private RelativeLayout mFuncDeviceDemoRl;
    private RelativeLayout mFuncAiRl;
    private RelativeLayout mFuncBookRl;
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

        mFuncSwitchRl = binding.rlFuncSwitch;
        mFuncSwitchRl.setOnClickListener(this);
        mFuncSwitchRl.setOnTouchListener(this);

        mFuncTv = binding.tvFunc;
        mFuncConfigTv = binding.tvFuncConfig;

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

        mFuncPrompterRl = binding.rlFuncPrompter;
        mFuncPrompterRl.setOnClickListener(this);
        mFuncPrompterRl.setOnTouchListener(this);

        mFuncNotificationRl = binding.rlContainerNotificationPush;
        mFuncNotificationRl.setOnClickListener(this);
        mFuncNotificationRl.setOnTouchListener(this);

        mNotificationSwitcher = binding.switchNotification;
            mNotificationSwitcher.setClickable(false);

        mFuncDeviceDemoRl = binding.rlContainerDeviceExhibitMode;
        mFuncDeviceDemoRl.setOnClickListener(this);
        mFuncDeviceDemoRl.setOnTouchListener(this);

        mFuncAiRl = binding.rlFuncAi;
        mFuncAiRl.setOnClickListener(this);
        mFuncAiRl.setOnTouchListener(this);

        mFuncBookRl = binding.rlFuncBook;
        mFuncBookRl.setOnClickListener(this);
        mFuncBookRl.setOnTouchListener(this);

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
                    if (VenusSDK.isConnected()) {
                        toRecordAct();
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
            case R.id.rl_func_switch:
                showFuncConfigDialog();
                break;
            case R.id.rl_func_nav:
                toMapAct();
                break;
            case R.id.rl_func_prompter:
                toPrompterAct();
                break;
            case R.id.rl_container_notification_push:
                toNotificationConfigAct();
                break;
            case R.id.rl_container_device_exhibit_mode:
                toExhibitAct();
                break;
            case R.id.rl_func_ai:
                toSpark40Act();
                break;
            case R.id.rl_func_book:
                ToastUtil.toast(getActivity(), R.string.tips_coming);
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

    private void updateSettingsViews() {
        String engine = AsrConfigManager.getInstance().getEngine();
        String func = AsrConfigManager.getInstance().getFunc();
        if (ASR_FUNC_TRANSCRIBE.equals(func)) {
            mFuncTv.setText(R.string.func_stt);
            LanguageTag sourceLanTag = AsrConfigManager.getInstance().getSourceLanTag(engine, func);
            if (null == sourceLanTag) {
                mFuncConfigTv.setText(R.string.tips_coming);
            } else {
                mFuncConfigTv.setText(sourceLanTag.getTitle());
            }
        } else if (ASR_FUNC_TRANSLATE.equals(func)) {
            mFuncTv.setText(R.string.func_trans);
            LanguageTag sourceLanTag = AsrConfigManager.getInstance().getSourceLanTag(engine, func);
            LanguageTag targetLanTag = AsrConfigManager.getInstance().getTargetLanTag(engine, func, (null == sourceLanTag ? "" : sourceLanTag.getTag()));
            if (null == sourceLanTag || null == targetLanTag) {
                mFuncConfigTv.setText(R.string.tips_coming);
            } else {
                mFuncConfigTv.setText(sourceLanTag.getTitle() + " -> " + targetLanTag.getTitle());
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
            mDeviceIv.setImageResource(R.mipmap.ic_device_h1);
            mDeviceAddRl.setVisibility(View.GONE);

            if (VenusSDK.isConnected()) {
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
            mDeviceIv.setImageResource(R.mipmap.ic_device_h1_offline);
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
        images.add(R.drawable.banner_1);
        images.add(R.drawable.banner_2);
        images.add(R.drawable.banner_3);

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
        scanIntent.setClass(getActivity(), Scan2Activity.class);
        getActivity().startActivity(scanIntent);
    }

    private void toRecordAct() {
        String func = MmkvUtil.decodeString(MMKV_ASR_FUNC_KEY, ASR_FUNC_DEFAULT);
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

    private void toPrompterAct() {
        Intent prompterIntent = new Intent(getActivity(), PrompterActivity.class);
        getActivity().startActivity(prompterIntent);
    }

    private void toNotificationConfigAct() {
        Intent notificationConfigIntent = new Intent(getActivity(), NotificationPushConfigActivity.class);
        startActivity(notificationConfigIntent);
    }

    private void toExhibitAct() {
        Intent exhibitIntent = new Intent(getActivity(), ExhibitActivity.class);
        startActivity(exhibitIntent);
    }

    private void toSpark40Act() {
        Intent spark40Intent = new Intent(getActivity(), Spark40Activity.class);
        startActivity(spark40Intent);
    }

    private void showFuncConfigDialog() {
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
            if (0 == sysState) {
                mDeviceSysStateIv.setVisibility(View.VISIBLE);
            } else {
                mDeviceSysStateIv.setVisibility(View.GONE);
            }
        }

        @Override
        public void onChargeStateChanged(int chargeState) {
            if (1 == chargeState) {
                mBatteryChargeIv.setVisibility(View.VISIBLE);
            } else {
                mBatteryChargeIv.setVisibility(View.GONE);
            }
        }

        @Override
        public void onBatteryChanged(int battery) {
            updateBatteryLevel(battery);
        }
    };
}