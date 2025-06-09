package com.jueyuantech.glasses;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Base64;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.jueyuantech.glasses.util.AppUtil;
import com.jueyuantech.glasses.util.BitmapUtil;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.venussdk.VNCommon;
import com.jueyuantech.venussdk.bean.VNNotificationInfo;

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

        VNNotificationInfo notificationInfo = new VNNotificationInfo();
        notificationInfo.setId(sbn.getId());
        notificationInfo.setTitle(title);
        notificationInfo.setMsg(text);
        notificationInfo.setPostTime(sbn.getPostTime() / 1000);

        //Bitmap bitmap = getBitmapFromVectorDrawable(this, R.drawable.outline_doorbell_24);
        Bitmap appIconBmp = AppUtil.getAppIcon(getApplicationContext(), packageName);
        String bmpStr = null;
        try {
            // 传入通知图标（Bitmap），获取设备可显示的图片RawData，通过setIconBitmap()传递
            bmpStr = VNCommon.getVenusBmpRawData(appIconBmp, 32, 32, false);
            if (!TextUtils.isEmpty(bmpStr)) {
                notificationInfo.setIconBitmap(bmpStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        VNCommon.addNotification(notificationInfo, null);
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

        VNNotificationInfo notificationInfo = new VNNotificationInfo();
        notificationInfo.setId(sbn.getId());
        VNCommon.removeNotification(notificationInfo, null);
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

    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}