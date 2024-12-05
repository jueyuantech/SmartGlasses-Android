package com.jueyuantech.glasses;

import static com.jueyuantech.glasses.common.Constants.AUDIO_INPUT_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.MMKV_AUDIO_INPUT_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_TRANS_SHOW_MODE_KEY;
import static com.jueyuantech.glasses.common.Constants.TRANS_SHOW_MODE_DEFAULT;

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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.SwitchCompat;

import com.google.gson.Gson;
import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.venussdk.VenusConstant;
import com.jueyuantech.venussdk.VenusSDK;
import com.jueyuantech.venussdk.bean.DeviceInfo;
import com.jueyuantech.venussdk.bean.SystemConfig;
import com.jueyuantech.venussdk.cb.DeviceInfoCallBack;
import com.jueyuantech.venussdk.cb.SystemConfigCallBack;

public class DeviceInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private Gson gson = new Gson();

    private String[] LANGUAGE_CONFIG_TITLE;
    private String[] LANGUAGE_CONFIG_KEY;

    private TextView mFontSizeSmallBtn;
    private TextView mFontSizeMediumBtn;
    private TextView mFontSizeLargeBtn;

    private ImageView mBackIv;
    private Button mRemoveBtn;

    private RelativeLayout mBatteryLevelRl;
    private TextView mBatteryLevelTv;
    private ImageView mBatteryLevelIv;
    private ImageView mBatteryChargeIv;

    private TextView mBrightnessTv;
    private SwitchCompat mAutoBrightnessEnabledSwitchBtn;
    private SeekBar mBrightnessSkb;
    private TextView mFontSizeTv;
    private AppCompatSeekBar mFontSizeSkb;
    private SwitchCompat mWearDetectionEnabledSwitchBtn;
    private SwitchCompat mTouchpadEnabledSwitchBtn;
    private RelativeLayout mLanguageRl, mTransShowModeRl, mAudioInputRl;
    private TextView mLanguageTv, mTransShowModeTv, mAudioInputTv;
    private RelativeLayout mMenuRl;
    private RelativeLayout mFirmwareRl;
    private TextView mFirmwareTv, mBthTv;
    private TextView mModelTv;

    private TextView mDeviceNameTv;

    private static final int MSG_DEVICE_INFO_GET = 2;
    private static final int MSG_SYSTEM_CONFIG_GET = 3;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_DEVICE_INFO_GET:
                    getDeviceInfo();
                    break;
                case MSG_SYSTEM_CONFIG_GET:
                    getSystemConfig();
                    break;
                default:
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        LANGUAGE_CONFIG_TITLE = getResources().getStringArray(R.array.language_config_title);
        LANGUAGE_CONFIG_KEY = getResources().getStringArray(R.array.language_config_key);

        mFontSizeSmallBtn = findViewById(R.id.tv_font_size_small);
        mFontSizeSmallBtn.setOnClickListener(this);
        mFontSizeMediumBtn = findViewById(R.id.tv_font_size_medium);
        mFontSizeMediumBtn.setOnClickListener(this);
        mFontSizeLargeBtn = findViewById(R.id.tv_font_size_large);
        mFontSizeLargeBtn.setOnClickListener(this);

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);
        mRemoveBtn = findViewById(R.id.btn_remove);
        mRemoveBtn.setOnClickListener(this);

        mDeviceNameTv = findViewById(R.id.tv_device_name);

        mBatteryLevelRl = findViewById(R.id.rl_container_battery);
        mBatteryLevelTv = findViewById(R.id.tv_battery_level);
        mBatteryLevelIv = findViewById(R.id.iv_battery_level);
        mBatteryChargeIv = findViewById(R.id.iv_battery_charge);

        mLanguageRl = findViewById(R.id.rl_container_language);
        mLanguageRl.setOnClickListener(this);
        mTransShowModeRl = findViewById(R.id.rl_container_trans_show_mode);
        mTransShowModeRl.setOnClickListener(this);
        mAudioInputRl = findViewById(R.id.rl_container_audio_input);
        mAudioInputRl.setOnClickListener(this);
        mMenuRl = findViewById(R.id.rl_container_menu);
        mMenuRl.setOnClickListener(this);
        mFirmwareRl = findViewById(R.id.rl_container_firmware);
        mFirmwareRl.setOnClickListener(this);

        mBrightnessTv = findViewById(R.id.tv_brightness);
        mFontSizeTv = findViewById(R.id.tv_font_size);
        mLanguageTv = findViewById(R.id.tv_language);
        mTransShowModeTv = findViewById(R.id.tv_trans_show_mode);
        mAudioInputTv = findViewById(R.id.tv_audio_input);
        mFirmwareTv = findViewById(R.id.tv_firmware);
        mBthTv = findViewById(R.id.tv_bth);
        mBthTv.setVisibility(View.INVISIBLE);

        mModelTv = findViewById(R.id.tv_model);

        mBrightnessSkb = findViewById(R.id.skb_brightness);
        mBrightnessSkb.setMax(255);
        mBrightnessSkb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBrightnessTv.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setBrightness(seekBar.getProgress());
            }
        });

        mAutoBrightnessEnabledSwitchBtn = findViewById(R.id.switch_auto_brightness_enabled);
        mAutoBrightnessEnabledSwitchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAutoBrightnessEnabled(isChecked);
                getSystemConfig();
            }
        });

        mFontSizeSkb = findViewById(R.id.skb_font_size);
        mFontSizeSkb.setMax(100);
        mFontSizeSkb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFontSizeTv.setText(String.valueOf(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setFontSize(seekBar.getProgress());
            }
        });

        mWearDetectionEnabledSwitchBtn = findViewById(R.id.switch_wear_detection_enabled);
        mWearDetectionEnabledSwitchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setWearDetectionEnabled(isChecked);
                getSystemConfig();
            }
        });

        mTouchpadEnabledSwitchBtn = findViewById(R.id.switch_touchpad_enabled);
        mTouchpadEnabledSwitchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setTouchpadEnabled(isChecked);
                getSystemConfig();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        DeviceManager.getInstance().addDeviceStatusListener(statusListener);
        DeviceManager.getInstance().addDeviceServiceListener(serviceListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshTransShowMode();
        refreshAudioInput();

        updateDeviceState();
        initDevice();
        getDeviceInfo();
        getSystemConfig();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeMessages(MSG_DEVICE_INFO_GET);
        handler.removeMessages(MSG_SYSTEM_CONFIG_GET);
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
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.btn_remove:
                showRemoveConfirmDialog();
                break;
            case R.id.rl_container_language:
                showLanguageConfigDialog();
                break;
            case R.id.rl_container_trans_show_mode:
                showTransShowModeConfigDialog();
                break;
            case R.id.rl_container_audio_input:
                showAudioInputConfigDialog();
                break;
            case R.id.rl_container_menu:
                toHomeMenuConfig();
                break;
            case R.id.rl_container_firmware:
                finish();
                toDeviceOtaAct();
                break;
            case R.id.tv_font_size_small:
                setFontSize(VenusConstant.FontSize.SMALL);
                break;
            case R.id.tv_font_size_medium:
                setFontSize(VenusConstant.FontSize.MEDIUM);
                break;
            case R.id.tv_font_size_large:
                setFontSize(VenusConstant.FontSize.LARGE);
                break;
            default:
        }
    }

    private void showLanguageConfigDialog() {
        String lanStr = mLanguageTv.getText().toString();
        String lanKey = "";
        for (int i = 0; i < LANGUAGE_CONFIG_TITLE.length; i++) {
            if (LANGUAGE_CONFIG_TITLE[i].equals(lanStr)) {
                lanKey = LANGUAGE_CONFIG_KEY[i];
                break;
            }
        }

        LanguageConfigFragment languageConfigFragment = new LanguageConfigFragment(lanKey);
        languageConfigFragment.setListener(new LanguageConfigFragment.Listener() {
            @Override
            public void onDestroyView() {
                getSystemConfig();
            }
        });
        languageConfigFragment.show(getSupportFragmentManager(), "languageConfig");
    }

    private void showTransShowModeConfigDialog() {
        TransShowModeConfigFragment transShowModeConfigFragment = new TransShowModeConfigFragment();
        transShowModeConfigFragment.setListener(new TransShowModeConfigFragment.Listener() {
            @Override
            public void onDestroyView() {
                refreshTransShowMode();
            }
        });
        transShowModeConfigFragment.show(getSupportFragmentManager(), "transShowModeConfigFragment");
    }

    private void showAudioInputConfigDialog() {
        AudioInputConfigFragment audioInputConfigFragment = new AudioInputConfigFragment();
        audioInputConfigFragment.setListener(new AudioInputConfigFragment.Listener() {
            @Override
            public void onDestroyView() {
                refreshAudioInput();
            }
        });
        audioInputConfigFragment.show(getSupportFragmentManager(), "audioInputConfigFragment");
    }

    private void refreshLanguageTitle(String lanKey) {
        String title = LANGUAGE_CONFIG_TITLE[0];
        for (int i = 0; i < LANGUAGE_CONFIG_TITLE.length; i++) {
            if (LANGUAGE_CONFIG_KEY[i].equals(lanKey)) {
                title = LANGUAGE_CONFIG_TITLE[i];
                break;
            }
        }
        mLanguageTv.setText(title);
    }

    private void refreshTransShowMode() {
        String[] CONFIG_TITLE = getResources().getStringArray(R.array.trans_show_mode_config_title);
        int[] CONFIG_KEY = getResources().getIntArray(R.array.trans_show_mode_config_key);

        int transShowMode = (int) MmkvUtil.decode(MMKV_TRANS_SHOW_MODE_KEY, TRANS_SHOW_MODE_DEFAULT);
        for (int i = 0; i < CONFIG_KEY.length; i++) {
            if (CONFIG_KEY[i] == transShowMode) {
                mTransShowModeTv.setText(CONFIG_TITLE[i]);
            }
        }
    }

    private void refreshAudioInput() {
        String[] CONFIG_TITLE = getResources().getStringArray(R.array.audio_input_config_title);
        int[] CONFIG_KEY = getResources().getIntArray(R.array.audio_input_config_key);

        int audioInput = (int) MmkvUtil.decode(MMKV_AUDIO_INPUT_KEY, AUDIO_INPUT_DEFAULT);
        for (int i = 0; i < CONFIG_KEY.length; i++) {
            if (CONFIG_KEY[i] == audioInput) {
                mAudioInputTv.setText(CONFIG_TITLE[i]);
            }
        }
    }

    private void refreshFontSize(int fontSize) {
        mFontSizeSmallBtn.setBackgroundResource(R.mipmap.bg_item_unselected);
        mFontSizeMediumBtn.setBackgroundResource(R.mipmap.bg_item_unselected);
        mFontSizeLargeBtn.setBackgroundResource(R.mipmap.bg_item_unselected);
        if (fontSize == VenusConstant.FontSize.SMALL) {
            mFontSizeSmallBtn.setBackgroundResource(R.mipmap.bg_item_selected);
        } else if (fontSize == VenusConstant.FontSize.MEDIUM) {
            mFontSizeMediumBtn.setBackgroundResource(R.mipmap.bg_item_selected);
        } else if (fontSize == VenusConstant.FontSize.LARGE) {
            mFontSizeLargeBtn.setBackgroundResource(R.mipmap.bg_item_selected);
        }
    }

    private void initDevice() {
        if (DeviceManager.getInstance().isBound()) {
            BluetoothDevice device = DeviceManager.getInstance().getBoundDevice();
            mDeviceNameTv.setText(device.getName());
        } else {
            mDeviceNameTv.setText("");
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

    private void updateDeviceState() {
        if (VenusSDK.isConnected()) {
            mBrightnessSkb.setEnabled(true);
            mAutoBrightnessEnabledSwitchBtn.setEnabled(true);
            mFontSizeSkb.setEnabled(true);
            mFontSizeSmallBtn.setEnabled(true);
            mFontSizeMediumBtn.setEnabled(true);
            mFontSizeLargeBtn.setEnabled(true);
            mLanguageRl.setEnabled(true);
            mWearDetectionEnabledSwitchBtn.setEnabled(true);
            mTouchpadEnabledSwitchBtn.setEnabled(true);
            mMenuRl.setEnabled(true);

            getSystemConfig();
            getDeviceInfo();
        } else {
            mBrightnessSkb.setEnabled(false);
            mAutoBrightnessEnabledSwitchBtn.setEnabled(false);
            mFontSizeSkb.setEnabled(false);
            mFontSizeSmallBtn.setEnabled(false);
            mFontSizeMediumBtn.setEnabled(false);
            mFontSizeLargeBtn.setEnabled(false);
            mLanguageRl.setEnabled(false);
            mWearDetectionEnabledSwitchBtn.setEnabled(false);
            mTouchpadEnabledSwitchBtn.setEnabled(false);
            mMenuRl.setEnabled(false);
        }

        // TODO if no bound device,
        // mFirmwareRl.setEnabled(false);
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
                VenusSDK.disconnect();
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

    private void getDeviceInfo() {
        VenusSDK.getDeviceInfo(new DeviceInfoCallBack() {
            @Override
            public void onSuccess(DeviceInfo deviceInfo) {
                LogUtil.i(gson.toJson(deviceInfo));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mFirmwareTv.setText(deviceInfo.getFirmware());
                            mBthTv.setText(deviceInfo.getBth());

                            mModelTv.setText((TextUtils.isEmpty(deviceInfo.getManufacturer()) ? "" : deviceInfo.getManufacturer()) + " " +
                                    (TextUtils.isEmpty(deviceInfo.getModel()) ? "" : deviceInfo.getModel()) + " " +
                                    (TextUtils.isEmpty(deviceInfo.getEdition()) ? "" : deviceInfo.getEdition())
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onTimeOut() {
                LogUtil.mark();
                handler.sendEmptyMessageDelayed(MSG_DEVICE_INFO_GET, 1000);
            }

            @Override
            public void onFailed() {
                LogUtil.mark();
                handler.sendEmptyMessageDelayed(MSG_DEVICE_INFO_GET, 1000);
            }
        });
    }

    private void getSystemConfig() {
        VenusSDK.getSystemConfig(new SystemConfigCallBack() {
            @Override
            public void onSuccess(SystemConfig systemConfig) {
                LogUtil.i(gson.toJson(systemConfig));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (null != systemConfig.getBrightness()) {
                                mBrightnessSkb.setProgress(systemConfig.getBrightness());
                                mBrightnessTv.setText(String.valueOf(systemConfig.getBrightness()));
                            }

                            if (null != systemConfig.getAutoBrightnessEnabled()) {
                                mAutoBrightnessEnabledSwitchBtn.setChecked(1 == systemConfig.getAutoBrightnessEnabled());
                            }

                            if (null != systemConfig.getFontSize()) {
                                refreshFontSize(systemConfig.getFontSize());
                            }

                            if (null != systemConfig.getLanguage()) {
                                refreshLanguageTitle(systemConfig.getLanguage());
                            }

                            if (null != systemConfig.getWearDetectionEnabled()) {
                                mWearDetectionEnabledSwitchBtn.setChecked(1 == systemConfig.getWearDetectionEnabled());
                            }

                            if (null != systemConfig.getTouchpadEnabled()) {
                                mTouchpadEnabledSwitchBtn.setChecked(1 == systemConfig.getTouchpadEnabled());
                            }

                            //mFontSizeSkb.setProgress(systemConfig.getFontSize());
                            //mFontSizeTv.setText(String.valueOf(systemConfig.getFontSize()));

                            //mRowSpaceSkb.setProgress(systemConfig.getRowSpace());
                            //mRowSpaceTv.setText(String.valueOf(systemConfig.getRowSpace()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onTimeOut() {
                LogUtil.mark();
                handler.sendEmptyMessageDelayed(MSG_SYSTEM_CONFIG_GET, 1000);
            }

            @Override
            public void onFailed() {
                LogUtil.mark();
                handler.sendEmptyMessageDelayed(MSG_SYSTEM_CONFIG_GET, 1000);
            }
        });
    }

    private void getBrightness() {
        VenusSDK.getBrightness(null);
    }

    private void setBrightness(int brightness) {
        VenusSDK.setBrightness(brightness, null);
    }

    private void setAutoBrightnessEnabled(boolean isChecked) {
        VenusSDK.setAutoBrightnessEnabled(isChecked ? 1 : 0, null);
        getSystemConfig();
    }

    private void setWearDetectionEnabled(boolean isChecked) {
        VenusSDK.setWearDetectionEnabled(isChecked ? 1 : 0, null);
        getSystemConfig();
    }

    private void setTouchpadEnabled(boolean isChecked) {
        VenusSDK.setTouchpadEnabled(isChecked ? 1 : 0, null);
        getSystemConfig();
    }

    private void getFontSize() {
        VenusSDK.getFontSize(null);
    }

    private void setFontSize(int fontSize) {
        VenusSDK.setFontSize(fontSize, null);
        getSystemConfig();
    }

    private void getRowSpace() {
        VenusSDK.getRowSpace(null);
    }

    private void setRowSpace(int rowSpace) {
        VenusSDK.setRowSpace(rowSpace, null);
    }

    private void toHomeMenuConfig() {
        Intent homeMenuIntent = new Intent();
        homeMenuIntent.setClass(this, HomeMenuActivity.class);
        startActivity(homeMenuIntent);
    }

    private void toDeviceOtaAct() {
        Intent deviceOtaIntent = new Intent(this, DeviceOtaActivity.class);
        startActivity(deviceOtaIntent);
    }

    private DeviceManager.DeviceServiceListener serviceListener = new DeviceManager.DeviceServiceListener() {
        @Override
        public void onStateChanged(int sysState) {
            updateDeviceState();
        }
    };

    private DeviceManager.DeviceStatusListener statusListener = new DeviceManager.DeviceStatusListener() {
        @Override
        public void onSysStateChanged(int sysState) {

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