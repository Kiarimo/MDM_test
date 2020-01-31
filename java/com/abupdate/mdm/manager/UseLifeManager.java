package com.abupdate.mdm.manager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.receiver.UseLifeReceiver;
import com.abupdate.mdm.utils.LogUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/*
 * @date   : 2019/10/10
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class UseLifeManager {
    private static UseLifeManager mUseLifeManager = null;
    private static Context mContext = null;

    public UseLifeManager() {
        mContext = MyApplication.getContext();
    }

    public static UseLifeManager getInstance() {
        if (mUseLifeManager == null) {
            mUseLifeManager = new UseLifeManager();
        }
        return mUseLifeManager;
    }

    private static void everyDayAlarm(Context context, int alarmId, long time, boolean setAlarm) {

        Intent intent = new Intent(context, UseLifeReceiver.class);
        intent.setAction(Const.ACTION_ALARM_RECEIVER);

        if (alarmId != Integer.MAX_VALUE)
            intent.putExtra(Const.ALARM_ID, alarmId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr != null) {
            alarmMgr.cancel(pendingIntent);
            if (setAlarm) {
                LogUtils.d("alarmId = " + alarmId + ", set alarm at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time));
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            }
        }
    }

    public boolean setUseLife(boolean setAlarm) {
        String status = PrefManager.getInstance().getUseLifeStatus();
        String val = PrefManager.getInstance().getUseLifeData();
        int type = PrefManager.getInstance().getLockType();
        if (Const.DEBUG_MODE) {
            status = "1";
            val = "09:00-12:00#14:00-22:00";
        }
        LogUtils.d("status = " + status + ", data = " + val);
        if (Const.UNKNOW_STRING.equals(val)) {
            LogUtils.e("no use life data");
            return false;
        }

        long currentTime = System.currentTimeMillis();
        ArrayList<Calendar> calendarList = new ArrayList<>();
        long maxTime = 0L;
        String[] listLife = val.split("#");
        for (String life : listLife) {
            String[] listTime = life.split("-");
            for (String time : listTime) {
                String[] str = time.split(":");
                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                calendar.setTimeInMillis(currentTime);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(str[0]));
                calendar.set(Calendar.MINUTE, Integer.valueOf(str[1]));
                calendarList.add(calendar);
                if (maxTime < calendar.getTimeInMillis()) {
                    maxTime = calendar.getTimeInMillis();
                }
            }
        }

        if (calendarList.size() % 2 != 0) {
            LogUtils.e("use life time error");
            return false;
        }

        boolean needShowLock = true;
        for (int i = 0; i < calendarList.size(); i++) {
            if (setAlarm) {
                Calendar cal = calendarList.get(i);
                if (currentTime > maxTime) {
                    cal.add(Calendar.DATE, 1);
                }
                everyDayAlarm(mContext, i, cal.getTimeInMillis(), "1".equals(status));
            }

            if (i % 2 == 0) {
                if (calendarList.get(i).getTimeInMillis() <= currentTime && currentTime <= calendarList.get(i + 1).getTimeInMillis()) {
                    needShowLock = false;
                }
            }
        }

        if (needShowLock && "1".equals(status)) {

            if (type == Const.LOCK_TYPE_UNKNOWN) {
                ApplyPolicyManager.getInstance().doLock();
                PrefManager.getInstance().setLockType(Const.LOCK_TYPE_USE_LIFE);
            }

        } else {
            if (type == Const.LOCK_TYPE_USE_LIFE) {
                ApplyPolicyManager.getInstance().doUnlock();
            }
        }
        return true;
    }

}
