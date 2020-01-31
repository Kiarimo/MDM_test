package com.abupdate.mdm.manager;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import com.abupdate.mdm.activity.DownloadActivity;
import com.abupdate.mdm.activity.KioskActivity;
import com.abupdate.mdm.activity.LockActivity;
import com.abupdate.mdm.activity.MainActivity;
import com.abupdate.mdm.activity.RegisterConnectActivity;
import com.abupdate.mdm.utils.LogUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * @date   : 2019/09/26
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class ActivityManager {
    /**
     * 存放Activity的map
     */
    private static Map<String, Activity> activitys = new HashMap<String, Activity>();

    /**
     * 获取管理类中注册的所有Activity的map
     *
     * @return
     */
    public static Map<String, Activity> getActivitys() {
        return activitys;
    }

    /**
     * 根据键值取对应的Activity
     *
     * @param key 键值
     * @return 键值对应的Activity
     */
    public static Activity getActivity(String key) {
        LogUtils.d("getActivity = " + key);
        return activitys.get(key);
    }

    /**
     * 注册Activity
     *
     * @param value
     * @param key
     */
    public static void addActivity(Activity value, String key) {
        activitys.put(key, value);
    }

    /**
     * 将key对应的Activity移除掉
     *
     * @param key
     */
    public static void removeActivity(String key) {
        activitys.remove(key);
    }

    /**
     * finish掉所有的Activity移除所有的Activity
     */
    public static void removeAllActivity() {
        Iterator<Activity> iterActivity = activitys.values().iterator();
        while (iterActivity.hasNext()) {
            Activity activity = iterActivity.next();
            iterActivity.remove();
            activity.finish();
        }
        activitys.clear();
    }

    public static void showScreen(Context context, Class<?> cls) {
        try {
            Intent intent = new Intent(context, cls);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            LogUtils.e("ActivityNotFoundException = " + e.getMessage());
        }

    }

    public static void dismissScreen(Class<?> cls) {
        Activity activity = getActivity(cls.getName());
        LogUtils.d("activity = " + activity);
        if (activity != null) {
            activity.finish();
        }
    }

    public static void showRegisterScreen(Context context) {
        showScreen(context, MainActivity.class);
    }

    public static void showLockScreen(Context context) {
        showScreen(context, LockActivity.class);
    }

    public static void showDownloadScreen(Context context) {
        showScreen(context, DownloadActivity.class);
    }


    public static void showKioskScreen(Context context) {
        LogUtils.d("");
        ApplyPolicyManager.getInstance().setKiosk();
        ApplyPolicyManager.getInstance().setEnterWiFiPolicies(true, KioskActivity.class);
        showScreen(context, KioskActivity.class);
    }

    public static void showMainScreen(Context context) {
        showScreen(context, MainActivity.class);
    }

    public static void dismissLockScreen(Context context) {
        LogUtils.d("");
//        dismissScreen(LockActivity.class);
        Intent intent = new Intent(context, LockActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("dismiss", true);
        context.startActivity(intent);
    }

    public static void dismissKioskScreen(Context context) {
        Intent intent = new Intent(context, KioskActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("dismiss", true);
        context.startActivity(intent);
        dismissScreen(RegisterConnectActivity.class);
    }


    public static void dismissDownloadScreen(Context context) {
        Intent intent = new Intent(context, DownloadActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("dismiss", true);
        context.startActivity(intent);
    }


    public static void dismissMainScreen(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("dismiss", true);
        context.startActivity(intent);
    }




}
