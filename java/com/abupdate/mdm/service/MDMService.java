package com.abupdate.mdm.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.abupdate.mdm.R;

public class MDMService extends Service {
    private static final int NOTIFICATION_FOREGROUND = 1;
    private static final String NOTIFICATION_CHANNEL = "mdm-notification";
    private NotificationManager mNm;

    public MDMService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNm = getSystemService(NotificationManager.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    private void createNotificationChannel() {
        NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL,
                getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
        mChannel.setSound(null, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mChannel.setAllowBubbles(false);
        }
        mChannel.enableVibration(false);
        mNm.createNotificationChannel(mChannel);
    }

    private void startForeground() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_lock)
                .setChannelId(NOTIFICATION_CHANNEL)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setShowWhen(false)
                .build();

        startForeground(NOTIFICATION_FOREGROUND, notification);
    }

    public static void startService(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, MDMService.class);
        context.startService(intent);
    }
}
