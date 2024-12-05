package com.jueyuantech.glasses;

import android.app.Notification;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Base64;

import com.jueyuantech.glasses.util.AppUtil;
import com.jueyuantech.glasses.util.BitmapUtil;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.venussdk.VenusSDK;
import com.jueyuantech.venussdk.bean.NotificationInfo;

public class NotificationPushListenerService extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // TODO Auto-generated method stub
        Bundle extras = sbn.getNotification().extras;

        int id = sbn.getId();
        LogUtil.i("Notification id " + id);
        String packageName = sbn.getPackageName();
        LogUtil.i("Notification packageName " + packageName);
        String title = extras.getString(Notification.EXTRA_TITLE);
        LogUtil.i("Notification title " + title);
        String text = extras.getString(Notification.EXTRA_TEXT);
        LogUtil.i("Notification text " + text);
        String sbnStr = sbn.toString();
        LogUtil.i("Notification sbnStr " + sbnStr);

        String notifyStr = sbn.getNotification().toString();
        LogUtil.i("Notification notifyStr " + notifyStr);

        if (!NotificationPushManager.getInstance().isPushEnable()) {
            return;
        }

        if (!NotificationPushManager.getInstance().isPkgAllowed(packageName)) {
            return;
        }

        NotificationInfo notificationInfo = new NotificationInfo();
        notificationInfo.setId(sbn.getId());
        notificationInfo.setTitle(title);
        notificationInfo.setMsg(text);
        notificationInfo.setPostTime(sbn.getPostTime() / 1000);

        Bitmap appIconBmp = AppUtil.getAppIcon(getApplicationContext(), packageName);
        Bitmap bmp = BitmapUtil.bitmap2OTSUBitmap(appIconBmp);
        Bitmap bmp32 = BitmapUtil.scaleImage(bmp, 32, 32);
        if (null != bmp32) {
            byte[] bmpBytes = getVenusBmpBytes(bmp32);
            //writeBytesToBinaryFile(bmpBytes, naviinfo.getIconType());
            String base64Bmp = Base64.encodeToString(bmpBytes, Base64.DEFAULT);
            notificationInfo.setIconBitmap(base64Bmp);
        }

        VenusSDK.addNotification(notificationInfo, null);
    }

    // 在删除消息时触发
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // TODO Auto-generated method stub
        Bundle extras = sbn.getNotification().extras;
        String packageName = sbn.getPackageName();
        LogUtil.i("Notification packageName " + packageName);
        if (!NotificationPushManager.getInstance().isPkgAllowed(packageName)) {
            return;
        }

        NotificationInfo notificationInfo = new NotificationInfo();
        notificationInfo.setId(sbn.getId());
        VenusSDK.removeNotification(notificationInfo, null);
    }

    /**
     * 监听断开
     */
    @Override
    public void onListenerDisconnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 通知侦听器断开连接 - 请求重新绑定
            requestRebind(new ComponentName(this, NotificationListenerService.class));
        }
    }

    private byte[] getVenusBmpBytes(Bitmap bitmap) {
        if (null == bitmap) {
            return new byte[0];
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        byte[] venusChannel = new byte[width * height];
        int index = 0;
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int clr = bitmap.getPixel(w, h);
                //LogUtil.i("r-" + Color.red(clr) + " g-" + Color.green(clr) + " b-" + Color.blue(clr) + " a-" + Color.alpha(clr));
                venusChannel[index++] = (byte) Color.green(clr);
            }
        }
        return venusChannel;
    }
}