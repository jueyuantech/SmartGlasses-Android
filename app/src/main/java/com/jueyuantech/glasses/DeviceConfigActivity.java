package com.jueyuantech.glasses;

import static com.jueyuantech.glasses.common.Constants.AUDIO_INPUT_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.MIC_DIRECTIONAL_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.MMKV_AUDIO_INPUT_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_MIC_DIRECTIONAL_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_SIMPLIFIED_MODE_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_TEXT_MODE_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_TRANS_SHOW_MODE_KEY;
import static com.jueyuantech.glasses.common.Constants.SIMPLIFIED_MODE_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.SIMPLIFIED_MODE_DISABLED;
import static com.jueyuantech.glasses.common.Constants.SIMPLIFIED_MODE_ENABLED;
import static com.jueyuantech.glasses.common.Constants.TEXT_MODE_DEFAULT;
import static com.jueyuantech.glasses.common.Constants.TRANS_SHOW_MODE_DEFAULT;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.SwitchCompat;

import com.google.gson.Gson;
import com.jueyuantech.glasses.bean.LatestFwInfo;
import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.stt.SttConfigManager;
import com.jueyuantech.glasses.stt.SttWorker;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;
import com.jueyuantech.glasses.util.ToastUtil;
import com.jueyuantech.venussdk.VNConstant;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.bean.VNSystemConfig;
import com.jueyuantech.venussdk.cb.VNSystemConfigCallBack;

public class DeviceConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private Gson gson = new Gson();

    private String[] LANGUAGE_CONFIG_TITLE;
    private String[] LANGUAGE_CONFIG_KEY;

    private String[] AUTO_SLEEP_CONFIG_TITLE;
    private int[] AUTO_SLEEP_CONFIG_KEY;
    private String[] AUTO_POWEROFF_CONFIG_TITLE;
    private int[] AUTO_POWEROFF_CONFIG_KEY;

    private TextView mFontSizeSmallBtn;
    private TextView mFontSizeMediumBtn;
    private TextView mFontSizeLargeBtn;

    private static final int FONT_SIZE_SMALL = 14;
    private static final int FONT_SIZE_MEDIUM = 22;
    private static final int FONT_SIZE_LARGE = 32;

    private ImageView mBackIv;

    private RelativeLayout mBatteryLevelRl;
    private TextView mBatteryLevelTv;
    private ImageView mBatteryLevelIv;
    private ImageView mBatteryChargeIv;

    private TextView mBrightnessTv;
    private LinearLayout mBrightnessDebugContainer;
    private TextView mBrightnessDecreaseTv, mBrightnessIncreaseTv;
    private SwitchCompat mAutoBrightnessEnabledSwitchBtn;
    private SeekBar mBrightnessSkb;
    private TextView mFontSizeTv;
    private AppCompatSeekBar mFontSizeSkb;
    private SwitchCompat mSimplifiedModeEnabledSwitchBtn;
    private SwitchCompat mDeviceNotificationEnabledSwitchBtn;
    private SwitchCompat mWearDetectionEnabledSwitchBtn;
    private SwitchCompat mTouchpadEnabledSwitchBtn;
    private SwitchCompat mIdleDetectionEnabledSwitchBtn;
    private RelativeLayout mLanguageRl, mTextModeRl, mTransShowModeRl, mAudioInputRl, mMicDirectionalRl, mSttEngineRl, mAutoSleepRl, mAutoPoweroffRl;
    private TextView mLanguageTv, mTextModeTv, mTransShowModeTv, mAudioInputTv, mMicDirectionalTv, mSttEngineTv, mAutoSleepTv, mAutoPoweroffTv;
    private RelativeLayout mMenuRl;
    private RelativeLayout mFuncDeviceDemoRl;
    private RelativeLayout mFuncCustomCRl;
    private RelativeLayout mFuncCustomDRl;
    private RelativeLayout mFuncCustomERl;
    private RelativeLayout mFuncCustomFRl;
    private TextView mDeviceNameTv;

    private static final int MSG_SYSTEM_CONFIG_GET = 3;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
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
        setContentView(R.layout.activity_device_config);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        LANGUAGE_CONFIG_TITLE = getResources().getStringArray(R.array.language_config_title);
        LANGUAGE_CONFIG_KEY = getResources().getStringArray(R.array.language_config_key);

        AUTO_SLEEP_CONFIG_TITLE = getResources().getStringArray(R.array.auto_sleep_config_title);
        AUTO_SLEEP_CONFIG_KEY = getResources().getIntArray(R.array.auto_sleep_config_key);
        AUTO_POWEROFF_CONFIG_TITLE = getResources().getStringArray(R.array.auto_poweroff_config_title);
        AUTO_POWEROFF_CONFIG_KEY = getResources().getIntArray(R.array.auto_poweroff_config_key);

        mFontSizeSmallBtn = findViewById(R.id.tv_font_size_small);
        mFontSizeSmallBtn.setOnClickListener(this);
        mFontSizeMediumBtn = findViewById(R.id.tv_font_size_medium);
        mFontSizeMediumBtn.setOnClickListener(this);
        mFontSizeLargeBtn = findViewById(R.id.tv_font_size_large);
        mFontSizeLargeBtn.setOnClickListener(this);

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);

        mDeviceNameTv = findViewById(R.id.tv_device_name);

        mBatteryLevelRl = findViewById(R.id.rl_container_battery);
        mBatteryLevelTv = findViewById(R.id.tv_battery_level);
        mBatteryLevelIv = findViewById(R.id.iv_battery_level);
        mBatteryChargeIv = findViewById(R.id.iv_battery_charge);

        mLanguageRl = findViewById(R.id.rl_container_language);
        mLanguageRl.setOnClickListener(this);
        mTextModeRl = findViewById(R.id.rl_container_text_mode);
        mTextModeRl.setOnClickListener(this);
        mTransShowModeRl = findViewById(R.id.rl_container_trans_show_mode);
        mTransShowModeRl.setOnClickListener(this);
        mAudioInputRl = findViewById(R.id.rl_container_audio_input);
        mAudioInputRl.setOnClickListener(this);
        mMicDirectionalRl = findViewById(R.id.rl_container_mic_directional);
        mMicDirectionalRl.setOnClickListener(this);
        mSttEngineRl = findViewById(R.id.rl_container_stt_engine);
        mSttEngineRl.setOnClickListener(this);
        mAutoSleepRl = findViewById(R.id.rl_container_auto_sleep);
        mAutoSleepRl.setOnClickListener(this);
        mAutoPoweroffRl = findViewById(R.id.rl_container_auto_poweroff);
        mAutoPoweroffRl.setOnClickListener(this);
        mMenuRl = findViewById(R.id.rl_container_menu);
        mMenuRl.setOnClickListener(this);
        mFuncDeviceDemoRl = findViewById(R.id.rl_container_exhibit);
        mFuncDeviceDemoRl.setOnClickListener(this);
        mFuncCustomCRl = findViewById(R.id.rl_container_custom_c);
        mFuncCustomCRl.setOnClickListener(this);
        mFuncCustomDRl = findViewById(R.id.rl_container_custom_d);
        mFuncCustomDRl.setOnClickListener(this);
        mFuncCustomERl = findViewById(R.id.rl_container_custom_e);
        mFuncCustomERl.setOnClickListener(this);
        mFuncCustomFRl = findViewById(R.id.rl_container_custom_f);
        mFuncCustomFRl.setOnClickListener(this);

        mBrightnessTv = findViewById(R.id.tv_brightness);
        mBrightnessDecreaseTv = findViewById(R.id.tv_brightness_decrease);
        mBrightnessDecreaseTv.setOnClickListener(this);
        mBrightnessIncreaseTv = findViewById(R.id.tv_brightness_increase);
        mBrightnessIncreaseTv.setOnClickListener(this);
        mBrightnessDebugContainer = findViewById(R.id.ll_brightness_debug);
        if (BuildConfig.DEBUG) {
            mBrightnessDebugContainer.setVisibility(View.VISIBLE);
        } else {
            mBrightnessDebugContainer.setVisibility(View.GONE);
        }
        mFontSizeTv = findViewById(R.id.tv_font_size);
        mLanguageTv = findViewById(R.id.tv_language);
        mTextModeTv = findViewById(R.id.tv_text_mode);
        mTransShowModeTv = findViewById(R.id.tv_trans_show_mode);
        mAudioInputTv = findViewById(R.id.tv_audio_input);
        mMicDirectionalTv = findViewById(R.id.tv_mic_directional);
        mSttEngineTv = findViewById(R.id.tv_stt_engine);
        mAutoSleepTv = findViewById(R.id.tv_auto_sleep);
        mAutoPoweroffTv = findViewById(R.id.tv_auto_poweroff);

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

        mSimplifiedModeEnabledSwitchBtn = findViewById(R.id.switch_simplified_mode_enabled);
        mSimplifiedModeEnabledSwitchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSimplifiedModeEnabled(isChecked);
            }
        });

        mDeviceNotificationEnabledSwitchBtn = findViewById(R.id.switch_device_notification_enabled);
        mDeviceNotificationEnabledSwitchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setDeviceNotificationEnabled(isChecked);
                getSystemConfig();
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

        mIdleDetectionEnabledSwitchBtn = findViewById(R.id.switch_idle_detection_enabled);
        mIdleDetectionEnabledSwitchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setIdleDetectionEnabled(isChecked);
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

        refreshSimplifiedMode();
        refreshTextMode();
        refreshTransShowMode();
        refreshAudioInput();
        refreshMicDirectional();
        refreshSttEngine();

        updateDeviceState();
        initDevice();
        getSystemConfig();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
            case R.id.rl_container_language:
                showLanguageConfigDialog();
                break;
            case R.id.rl_container_text_mode:
                showTextModeConfigDialog();
                break;
            case R.id.rl_container_trans_show_mode:
                showTransShowModeConfigDialog();
                break;
            case R.id.rl_container_audio_input:
                if (SttWorker.getInstance().isBusy()) {
                    ToastUtil.toast(getApplicationContext(), R.string.tips_stt_running);
                } else {
                    showAudioInputConfigDialog();
                }
                break;
            case R.id.rl_container_mic_directional:
                if (SttWorker.getInstance().isBusy()) {
                    ToastUtil.toast(getApplicationContext(), R.string.tips_stt_running);
                } else {
                    showMicDirectionalConfigDialog();
                }
                break;
            case R.id.rl_container_stt_engine:
                showSttEngineConfigDialog();
                break;
            case R.id.rl_container_auto_sleep:
                showAutoSleepConfigDialog();
                break;
            case R.id.rl_container_auto_poweroff:
                showAutoPoweroffConfigDialog();
                break;
            case R.id.rl_container_menu:
                toHomeMenuConfig();
                break;
            case R.id.rl_container_exhibit:
                toExhibitAct();
                break;
            case R.id.rl_container_custom_c:
                toCustomCAct();
                break;
            case R.id.rl_container_custom_d:
                toCustomDAct();
                break;
            case R.id.rl_container_custom_e:
                toCustomEAct();
                break;
            case R.id.rl_container_custom_f:
                toCustomFAct();
                break;
            case R.id.tv_font_size_small:
                setFontSize(FONT_SIZE_SMALL);
                break;
            case R.id.tv_font_size_medium:
                setFontSize(FONT_SIZE_MEDIUM);
                break;
            case R.id.tv_font_size_large:
                setFontSize(FONT_SIZE_LARGE);
                break;
            case R.id.tv_brightness_decrease:
                decreaseBrightness();
                break;
            case R.id.tv_brightness_increase:
                increaseBrightness();
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

    private void showTextModeConfigDialog() {
        TextModeConfigFragment textModeConfigFragment = new TextModeConfigFragment();
        textModeConfigFragment.setListener(new TextModeConfigFragment.Listener() {
            @Override
            public void onDestroyView() {
                refreshTextMode();
            }
        });
        textModeConfigFragment.show(getSupportFragmentManager(), "textModeConfigFragment");
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

    private void showMicDirectionalConfigDialog() {
        MicDirectionalConfigFragment micDirectionalConfigFragment = new MicDirectionalConfigFragment();
        micDirectionalConfigFragment.setListener(new MicDirectionalConfigFragment.Listener() {
            @Override
            public void onDestroyView() {
                refreshMicDirectional();
            }
        });
        micDirectionalConfigFragment.show(getSupportFragmentManager(), "micDirectionalConfigFragment");
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

    private void refreshSimplifiedMode() {
        int simplified = (int) MmkvUtil.decode(MMKV_SIMPLIFIED_MODE_KEY, SIMPLIFIED_MODE_DEFAULT);
        mSimplifiedModeEnabledSwitchBtn.setChecked(simplified == SIMPLIFIED_MODE_ENABLED);
    }

    private void refreshTextMode() {
        String[] CONFIG_TITLE = getResources().getStringArray(R.array.text_mode_config_title);
        int[] CONFIG_KEY = getResources().getIntArray(R.array.text_mode_config_key);

        int textMode = (int) MmkvUtil.decode(MMKV_TEXT_MODE_KEY, TEXT_MODE_DEFAULT);
        for (int i = 0; i < CONFIG_KEY.length; i++) {
            if (CONFIG_KEY[i] == textMode) {
                mTextModeTv.setText(CONFIG_TITLE[i]);
            }
        }
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

    private void refreshMicDirectional() {
        String[] CONFIG_TITLE = getResources().getStringArray(R.array.mic_directional_config_title);
        int[] CONFIG_KEY = getResources().getIntArray(R.array.mic_directional_config_key);

        int micDirectional = (int) MmkvUtil.decode(MMKV_MIC_DIRECTIONAL_KEY, MIC_DIRECTIONAL_DEFAULT);
        for (int i = 0; i < CONFIG_KEY.length; i++) {
            if (CONFIG_KEY[i] == micDirectional) {
                mMicDirectionalTv.setText(CONFIG_TITLE[i]);
            }
        }
    }

    private void refreshFontSize(int fontSize) {
        mFontSizeSmallBtn.setBackgroundResource(R.mipmap.bg_item_unselected);
        mFontSizeMediumBtn.setBackgroundResource(R.mipmap.bg_item_unselected);
        mFontSizeLargeBtn.setBackgroundResource(R.mipmap.bg_item_unselected);
        if (fontSize == FONT_SIZE_SMALL) {
            mFontSizeSmallBtn.setBackgroundResource(R.mipmap.bg_item_selected);
        } else if (fontSize == FONT_SIZE_MEDIUM) {
            mFontSizeMediumBtn.setBackgroundResource(R.mipmap.bg_item_selected);
        } else if (fontSize == FONT_SIZE_LARGE) {
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
        if (VNCommon.isConnected()) {
            mBrightnessSkb.setEnabled(true);
            mAutoBrightnessEnabledSwitchBtn.setEnabled(true);
            mFontSizeSkb.setEnabled(true);
            mFontSizeSmallBtn.setEnabled(true);
            mFontSizeMediumBtn.setEnabled(true);
            mFontSizeLargeBtn.setEnabled(true);
            mLanguageRl.setEnabled(true);
            mDeviceNotificationEnabledSwitchBtn.setEnabled(true);
            mWearDetectionEnabledSwitchBtn.setEnabled(true);
            mTouchpadEnabledSwitchBtn.setEnabled(true);
            mIdleDetectionEnabledSwitchBtn.setEnabled(true);
            mMenuRl.setEnabled(true);

            getSystemConfig();
        } else {
            mBrightnessSkb.setEnabled(false);
            mAutoBrightnessEnabledSwitchBtn.setEnabled(false);
            mFontSizeSkb.setEnabled(false);
            mFontSizeSmallBtn.setEnabled(false);
            mFontSizeMediumBtn.setEnabled(false);
            mFontSizeLargeBtn.setEnabled(false);
            mLanguageRl.setEnabled(false);
            mDeviceNotificationEnabledSwitchBtn.setEnabled(false);
            mWearDetectionEnabledSwitchBtn.setEnabled(false);
            mTouchpadEnabledSwitchBtn.setEnabled(false);
            mIdleDetectionEnabledSwitchBtn.setEnabled(false);
            mMenuRl.setEnabled(false);
        }
    }

    private void getSystemConfig() {
        VNCommon.getSystemConfig(new VNSystemConfigCallBack() {
            @Override
            public void onSuccess(VNSystemConfig systemConfig) {
                LogUtil.i(gson.toJson(systemConfig));

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

                    if (null != systemConfig.getNotificationEnabled()) {
                        mDeviceNotificationEnabledSwitchBtn.setChecked(1 == systemConfig.getNotificationEnabled());
                    }

                    if (null != systemConfig.getInactivityTimeout()) {
                        int inactivityTimeout = systemConfig.getInactivityTimeout();

                        String displayText = "";
                        for (int i = 0; i < AUTO_SLEEP_CONFIG_KEY.length; i++) {
                            if (AUTO_SLEEP_CONFIG_KEY[i] == inactivityTimeout) {
                                displayText = AUTO_SLEEP_CONFIG_TITLE[i];
                                break;
                            }
                        }

                        mAutoSleepTv.setText(displayText);
                    }

                    if (null != systemConfig.getPoweroffTimeout()) {
                        int poweroffTimeout = systemConfig.getPoweroffTimeout();

                        String displayText = "";
                        for (int i = 0; i < AUTO_POWEROFF_CONFIG_KEY.length; i++) {
                            if (AUTO_POWEROFF_CONFIG_KEY[i] == poweroffTimeout) {
                                displayText = AUTO_POWEROFF_CONFIG_TITLE[i];
                                break;
                            }
                        }

                        mAutoPoweroffTv.setText(displayText);
                    }

                    if (null != systemConfig.getWearDetectionEnabled()) {
                        mWearDetectionEnabledSwitchBtn.setChecked(1 == systemConfig.getWearDetectionEnabled());
                    }

                    if (null != systemConfig.getTouchpadEnabled()) {
                        mTouchpadEnabledSwitchBtn.setChecked(1 == systemConfig.getTouchpadEnabled());
                    }

                    if (null != systemConfig.getIdleDetectionEnabled()) {
                        mIdleDetectionEnabledSwitchBtn.setChecked(1 == systemConfig.getIdleDetectionEnabled());
                    }

                    //mFontSizeSkb.setProgress(systemConfig.getFontSize());
                    //mFontSizeTv.setText(String.valueOf(systemConfig.getFontSize()));

                    //mRowSpaceSkb.setProgress(systemConfig.getRowSpace());
                    //mRowSpaceTv.setText(String.valueOf(systemConfig.getRowSpace()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        VNCommon.getBrightness(null);
    }

    private void setBrightness(int brightness) {
        VNCommon.setBrightness(brightness, null);
    }

    private void decreaseBrightness() {
        int target = mBrightnessSkb.getProgress() - 1;
        if (target < 0) {
            return;
        }
        setBrightness(target);
        mBrightnessTv.setText(String.valueOf(target));
        mBrightnessSkb.setProgress(target);
    }

    private void increaseBrightness() {
        int target = mBrightnessSkb.getProgress() + 1;
        if (target > 255) {
            return;
        }
        setBrightness(target);
        mBrightnessTv.setText(String.valueOf(target));
        mBrightnessSkb.setProgress(target);
    }

    private void setAutoBrightnessEnabled(boolean isChecked) {
        VNCommon.setAutoBrightnessEnabled(isChecked ? VNConstant.EnableState.ENABLED : VNConstant.EnableState.DISABLED, null);
        getSystemConfig();
    }

    private void setSimplifiedModeEnabled(boolean isChecked) {
        if (isChecked) {
            MmkvUtil.encode(MMKV_SIMPLIFIED_MODE_KEY, SIMPLIFIED_MODE_ENABLED);
        } else {
            MmkvUtil.encode(MMKV_SIMPLIFIED_MODE_KEY, SIMPLIFIED_MODE_DISABLED);
        }
    }

    private void setDeviceNotificationEnabled(boolean isChecked) {
        VNCommon.setNotificationEnabled(isChecked ? VNConstant.EnableState.ENABLED : VNConstant.EnableState.DISABLED, null);
        getSystemConfig();
    }

    private void setWearDetectionEnabled(boolean isChecked) {
        VNCommon.setWearDetectionEnabled(isChecked ? VNConstant.EnableState.ENABLED : VNConstant.EnableState.DISABLED, null);
        getSystemConfig();
    }

    private void setTouchpadEnabled(boolean isChecked) {
        VNCommon.setTouchpadEnabled(isChecked ? VNConstant.EnableState.ENABLED : VNConstant.EnableState.DISABLED, null);
        getSystemConfig();
    }

    private void setIdleDetectionEnabled(boolean isChecked) {
        VNCommon.setIdleDetectionEnabled(isChecked ? VNConstant.EnableState.ENABLED : VNConstant.EnableState.DISABLED, null);
        getSystemConfig();
    }

    private void getFontSize() {
        // TODO
    }

    private void setFontSize(int fontSize) {
        // TODO
        getSystemConfig();
    }

    private void getRowSpace() {
        // TODO
    }

    private void setRowSpace(int rowSpace) {
        // TODO
    }

    private void toHomeMenuConfig() {
        Intent homeMenuIntent = new Intent();
        homeMenuIntent.setClass(this, HomeMenuActivity.class);
        startActivity(homeMenuIntent);
    }

    private void toExhibitAct() {
        Intent exhibitIntent = new Intent(this, ExhibitActivity.class);
        startActivity(exhibitIntent);
    }

    private void toCustomCAct() {
        Intent customCIntent = new Intent(this, CustomCActivity.class);
        startActivity(customCIntent);
    }

    private void toCustomDAct() {
        Intent customDIntent = new Intent(this, CustomDActivity.class);
        startActivity(customDIntent);
    }

    private void toCustomEAct() {
        Intent customEIntent = new Intent(this, CustomEActivity.class);
        startActivity(customEIntent);
    }

    private void toCustomFAct() {
        Intent customFIntent = new Intent(this, CustomFActivity.class);
        startActivity(customFIntent);
    }

    private void toDeviceOtaAct(LatestFwInfo latestFwInfo) {
        Intent deviceOtaIntent = new Intent(this, DeviceOtaActivity.class);
        deviceOtaIntent.putExtra("ota_type", DeviceOtaActivity.OTA_TYPE_1);
        deviceOtaIntent.putExtra("ota_fw_info", latestFwInfo);
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

    private void refreshSttEngine() {
        String sttEngine = SttConfigManager.getInstance().getEngine();
        mSttEngineTv.setText(sttEngine);
    }

    private void showSttEngineConfigDialog() {
        SttEngineConfigFragment sttEngineConfigFragment = new SttEngineConfigFragment();
        sttEngineConfigFragment.setListener(new SttEngineConfigFragment.Listener() {
            @Override
            public void onDestroyView() {
                String sttEngineAfter = SttConfigManager.getInstance().getEngine();
                mSttEngineTv.setText(sttEngineAfter);

                ToastUtil.toast(getApplicationContext(), getString(R.string.tips_switched, sttEngineAfter));
            }
        });
        sttEngineConfigFragment.show(getSupportFragmentManager(), "sttEngineConfig");
    }

    private void showAutoSleepConfigDialog() {
        String autoSleepStr = mAutoSleepTv.getText().toString();
        int autoSleepKey = 0; // 默认值为0（Never）
        for (int i = 0; i < AUTO_SLEEP_CONFIG_TITLE.length; i++) {
            if (AUTO_SLEEP_CONFIG_TITLE[i].equals(autoSleepStr)) {
                autoSleepKey = AUTO_SLEEP_CONFIG_KEY[i];
                break;
            }
        }

        AutoSleepConfigFragment autoSleepConfigFragment = new AutoSleepConfigFragment(autoSleepKey);
        autoSleepConfigFragment.setListener(new AutoSleepConfigFragment.Listener() {
            @Override
            public void onDestroyView() {
                getSystemConfig();
            }
        });
        autoSleepConfigFragment.show(getSupportFragmentManager(), "autoSleepConfig");
    }

    private void showAutoPoweroffConfigDialog() {
        String autoPoweroffStr = mAutoPoweroffTv.getText().toString();
        int autoPoweroffKey = 0; // 默认值为0（Never）
        for (int i = 0; i < AUTO_POWEROFF_CONFIG_TITLE.length; i++) {
            if (AUTO_POWEROFF_CONFIG_TITLE[i].equals(autoPoweroffStr)) {
                autoPoweroffKey = AUTO_POWEROFF_CONFIG_KEY[i];
                break;
            }
        }

        AutoPoweroffConfigFragment autoPoweroffConfigFragment = new AutoPoweroffConfigFragment(autoPoweroffKey);
        autoPoweroffConfigFragment.setListener(new AutoPoweroffConfigFragment.Listener() {
            @Override
            public void onDestroyView() {
                getSystemConfig();
            }
        });
        autoPoweroffConfigFragment.show(getSupportFragmentManager(), "autoPoweroffConfig");
    }
}