package com.jueyuantech.glasses;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.jueyuantech.glasses.device.DeviceManager;
import com.jueyuantech.glasses.util.LogUtil;
import com.jueyuantech.venussdk.VNCommon;

public class HomeService extends Service {

    private static final boolean DEBUG = true;

    private String CHANNEL_ID = "CHANNEL_ID_" + R.string.app_name;
    private String CHANNEL_NAME = "CHANNEL_NAME_" + R.string.app_name;
    private NotificationManager mNotificationManager;
    private NotificationChannel mNotificationChannel;
    private NotificationCompat.Builder mBuilder;

    public HomeService() {
        if (DEBUG) LogUtil.mark();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEBUG) LogUtil.mark();

        mNotificationManager = getSystemService(NotificationManager.class);
        createForeNotification();
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        HomeService getService() {
            if (DEBUG) LogUtil.mark();
            return HomeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");

        if (DEBUG) LogUtil.mark();
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (DEBUG) LogUtil.mark();

        updateView();
        DeviceManager.getInstance().addDeviceServiceListener(deviceServiceListener);

        return START_STICKY;
    }

    private void createForeNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (DEBUG) LogUtil.mark();

            mNotificationChannel = mNotificationManager.getNotificationChannel(CHANNEL_ID);
            if (null == mNotificationChannel) {
                mNotificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                mNotificationManager.createNotificationChannel(mNotificationChannel);
            }

            Intent homeIntent = new Intent(this, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, homeIntent, PendingIntent.FLAG_MUTABLE);

            mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        } else {
            if (DEBUG) LogUtil.mark();
        }
    }

    private void updateView() {
        if (VNCommon.isConnected()) {
            updateNotification(
                    getString(R.string.notification_title_device_connected),
                    getString(R.string.notification_content_device_connected)
            );
        } else {
            updateNotification(
                    getString(R.string.notification_title_device_not_connected),
                    getString(R.string.notification_content_device_not_connected)
            );
        }
    }

    private void updateNotification(String title, String content) {
        mBuilder.setContentTitle(title);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mBuilder.setContentTitle(getResources().getString(R.string.app_name));
        }
        mBuilder.setContentText(content);

        Notification notification = mBuilder.build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(R.string.app_name, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE | ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
        } else {
            startForeground(R.string.app_name, notification);
        }
    }

    private DeviceManager.DeviceServiceListener deviceServiceListener = new DeviceManager.DeviceServiceListener() {
        @Override
        public void onStateChanged(int sysState) {
            updateView();
        }
    };
}