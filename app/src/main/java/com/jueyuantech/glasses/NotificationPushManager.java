package com.jueyuantech.glasses;

import static com.jueyuantech.glasses.common.Constants.MMKV_NOTIFICATION_PUSH_KEY;
import static com.jueyuantech.glasses.common.Constants.MMKV_NOTIFICATION_PUSH_PKG_SET_KEY;
import static com.jueyuantech.glasses.common.Constants.NOTIFICATION_PUSH_DISABLED;
import static com.jueyuantech.glasses.common.Constants.NOTIFICATION_PUSH_ENABLED;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.core.app.NotificationManagerCompat;

import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.glasses.util.MmkvUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationPushManager {

    private volatile static NotificationPushManager mInstance;

    private Context mContext;

    private List<PackageInfo> mPkgList = new ArrayList<>();
    private PackageManager mPackageManager;

    private NotificationPushManager() {

    }

    public static NotificationPushManager getInstance() {
        if (mInstance == null) {
            synchronized (NotificationPushManager.class) {
                if (mInstance == null) {
                    mInstance = new NotificationPushManager();
                }
            }
        }
        return mInstance;
    }

    public void init(Context context) {
        mContext = context;
        mPackageManager = mContext.getPackageManager();

        if (!isNotificationListenerEnabled()) {
            return;
        }

        if (!isPushEnable()) {
            return;
        }

        initData();
    }

    private void initData() {
        initPkgList();
        loadAllowedPkg();
    }

    public void release() {

    }

    public List<PackageInfo> getPkgList() {
        return mPkgList;
    }

    public void setPushEnable(boolean enable) {
        // TODO start of stop service
        if (enable) {
            MmkvUtil.encode(MMKV_NOTIFICATION_PUSH_KEY, NOTIFICATION_PUSH_ENABLED);
            wakeup();

            initData();
            //startService();
        } else {
            MmkvUtil.encode(MMKV_NOTIFICATION_PUSH_KEY, NOTIFICATION_PUSH_DISABLED);
            //stopService();
        }
    }

    public boolean isPushEnable() {
        int notificationPush = MmkvUtil.decodeInt(MMKV_NOTIFICATION_PUSH_KEY);
        return NOTIFICATION_PUSH_ENABLED == notificationPush;
    }

    private void initPkgList() {
        mPkgList.clear();
        List<PackageInfo> pkgList = mPackageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        for (int i = 0; i < pkgList.size(); i++) {
            boolean hasPostNotificationPermission = false;
            PackageInfo packageInfo = pkgList.get(i);
            if (null != packageInfo.requestedPermissions) {
                for (String permission : packageInfo.requestedPermissions) {
                    if ("android.permission.POST_NOTIFICATIONS".equals(permission)) {
                        // FIXME has AND granted
                        hasPostNotificationPermission = true;
                        break;
                    }
                }
            }

            if (hasPostNotificationPermission) {
                mPkgList.add(packageInfo);
            }
        }
    }

    /**
     * 切换通知监听器服务
     */
    private void wakeup() {
        LogUtil.mark();
        mPackageManager.setComponentEnabledSetting(new ComponentName(mContext, NotificationPushListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        mPackageManager.setComponentEnabledSetting(new ComponentName(mContext, NotificationPushListenerService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public boolean isNotificationListenerEnabled() {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(mContext);
        if (packageNames.contains(mContext.getPackageName())) {
            return true;
        }
        return false;
    }

    private Set<String> mAllowedPkgSet = new HashSet<>();
    public void resetAllowedPkgSet() {
        mAllowedPkgSet.clear();
        mAllowedPkgSet.add("com.tencent.mobileqq"); //QQ qq信息
        mAllowedPkgSet.add("com.tencent.mm"); //WX 微信信息
        mAllowedPkgSet.add("com.android.mms"); //MMS 短信
        mAllowedPkgSet.add("com.hihonor.mms"); //HONOR_MMS 荣耀短信
        mAllowedPkgSet.add("com.google.android.apps.messaging"); //MESSAGES 信息
        mAllowedPkgSet.add("com.android.incallui"); //IN_CALL 来电
        mAllowedPkgSet.add("com.android.server.telecom"); // MISSED_CALL 未接来电

        saveAllowedPkg();
        loadAllowedPkg();
    }

    public void selectAllPkg() {
        mAllowedPkgSet.clear();
        for (PackageInfo packageInfo : mPkgList) {
            mAllowedPkgSet.add(packageInfo.packageName);
        }
        saveAllowedPkg();
        loadAllowedPkg();
    }

    public void unselectAllPkg() {
        mAllowedPkgSet.clear();
        saveAllowedPkg();
        loadAllowedPkg();
    }

    public void addPkgToAllowed(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return;
        }
        if (!isPkgAllowed(pkgName)) {
            mAllowedPkgSet.add(pkgName);

            saveAllowedPkg();
            loadAllowedPkg();
        }
    }

    public void removePkgFromAllowed(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return;
        }
        if (isPkgAllowed(pkgName)) {
            mAllowedPkgSet.remove(pkgName);

            saveAllowedPkg();
            loadAllowedPkg();
        }
    }

    public boolean isPkgAllowed(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }

        return mAllowedPkgSet.contains(pkgName);
    }

    private void saveAllowedPkg() {
        MmkvUtil.encodeSet(MMKV_NOTIFICATION_PUSH_PKG_SET_KEY, mAllowedPkgSet);
    }

    private void loadAllowedPkg() {
        mAllowedPkgSet.clear();
        mAllowedPkgSet.addAll(MmkvUtil.decodeStringSet(MMKV_NOTIFICATION_PUSH_PKG_SET_KEY));
    }
}