package com.jueyuantech.glasses;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class BackgroundPermissionActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mBackIv;

    private RelativeLayout mPersistentNotificationContainerRl;
    private RelativeLayout mBatteryOptimizationContainerRl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_permission);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(getColor(R.color.bg_main_gradient_start));
        }

        mBackIv = findViewById(R.id.iv_back);
        mBackIv.setOnClickListener(this);
        mPersistentNotificationContainerRl = findViewById(R.id.rl_container_persistent_notification);
        mPersistentNotificationContainerRl.setOnClickListener(this);
        mBatteryOptimizationContainerRl = findViewById(R.id.rl_container_battery_optimization);
        mBatteryOptimizationContainerRl.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.rl_container_persistent_notification:
                openPush(BackgroundPermissionActivity.this);
                break;
            case R.id.rl_container_battery_optimization:
                toPermissionSetting(BackgroundPermissionActivity.this);
                break;
        }
    }


    /**
     * 是否打开通知按钮
     *
     * @param context
     * @return
     */
    public static boolean isNotificationEnabled(Context context) {
        return NotificationManagerCompat.from(context.getApplicationContext()).areNotificationsEnabled();
    }

    public static void openPush(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, activity.getApplicationInfo().uid);
            activity.startActivity(intent);
        } else {
            toPermissionSetting(activity);
        }
    }


    /**
     * 跳转到权限设置
     *
     * @param activity
     */
    public static void toPermissionSetting(Activity activity) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            toSystemConfig(activity);
        } else {
            try {
                toApplicationInfo(activity);
            } catch (Exception e) {
                e.printStackTrace();
                toSystemConfig(activity);
            }
        }
    }

    /**
     * 应用信息界面
     *
     * @param activity
     */
    public static void toApplicationInfo(Activity activity) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        localIntent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(localIntent);
    }

    /**
     * 系统设置界面
     *
     * @param activity
     */
    public static void toSystemConfig(Activity activity) {
        try {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            activity.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 忽略电池优化
     */
    public void ignoreBatteryOptimization(Activity activity) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean hasIgnored = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if (!hasIgnored) {
                try {//先调用系统显示 电池优化权限
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {//如果失败了则引导用户到电池优化界面
                    try {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);
                        ComponentName cn = ComponentName.unflattenFromString("com.android.settings/.Settings$HighPowerApplicationsActivity");
                        intent.setComponent(cn);
                        startActivity(intent);
                    } catch (Exception ex) {//如果全部失败则说明没有电池优化功能

                    }
                }
            }
        }
    }
}