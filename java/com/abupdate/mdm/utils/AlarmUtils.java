package com.abupdate.mdm.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.abupdate.mdm.receiver.AlarmReceiver;

import java.text.SimpleDateFormat;

public class AlarmUtils {

    public static final String TASK = "task";
    public static final int TASK_QUERY_AUTO = 1;
    //com.abupdate.mdm.custom_service
    public static final String SEND_CUSTOM_SERVICE_ACTION = String.valueOf(new char[]{'c', 'o', 'm', '.', 'a', 'b', 'u', 'p', 'd', 'a', 't', 'e', '.', 'm', 'd', 'm', '.', 'c', 'u', 's', 't', 'o', 'm', '_', 's', 'e', 'r', 'v', 'i', 'c', 'e',});

    private static void common_alarm(Context context, Class<?> cls, String action, int taskId, long time, int requestCode) {
        Intent alarm = new Intent(context, cls);
        if (action != null)
            alarm.setAction(action);
        if (taskId != Integer.MAX_VALUE)
            alarm.putExtra(TASK, taskId);
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent operation = PendingIntent.getBroadcast(context, requestCode, alarm, PendingIntent.FLAG_CANCEL_CURRENT);
        if (alarmMgr != null) {
            LogUtils.d("alarm time = " + SimpleDateFormat.getDateTimeInstance().format(time)
                    + " , requestCode = " + requestCode);
            int sdkInt = Build.VERSION.SDK_INT;
            if (sdkInt < Build.VERSION_CODES.KITKAT) {
                alarmMgr.set(AlarmManager.RTC_WAKEUP, time, operation);
            } else if (sdkInt < Build.VERSION_CODES.M) {
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, time, operation);
            } else {
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, operation);
            }
        }
    }


    public static void Sync_data(Context context, long interval_time) {
        long time = System.currentTimeMillis() + interval_time;
        LogUtils.d("time = " + time);
        common_alarm(context, AlarmReceiver.class, SEND_CUSTOM_SERVICE_ACTION, TASK_QUERY_AUTO, time, 1010);
    }


}
