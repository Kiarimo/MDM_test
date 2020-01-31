package com.abupdate.mdm;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import com.abupdate.mdm.manager.ApplyPolicyManager;
import com.abupdate.mdm.manager.DeviceManager;
import com.abupdate.mdm.manager.PushManager;
import com.abupdate.mdm.receiver.AlarmReceiver;
import com.abupdate.mdm.receiver.DataChangeReceiver;
import com.abupdate.mdm.receiver.NetChangeReceiver;
import com.abupdate.mdm.receiver.ScreenReceiver;
import com.abupdate.mdm.service.MDMService;
import com.abupdate.mdm.utils.AlarmUtils;
import com.abupdate.mdm.utils.LogUtils;

/*
 * @date   : 2019/09/20
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d("onCreate");
        mContext = getApplicationContext();
//        DeviceManager.getInstance().setDeviceOwner();
        checkDeviceOwner();
        rigisterDateChangeBroadcast();
        rigisterNetChangeBroadcast();
        rigisterScreenBroadcast();
        rigisterAlarmBroadcast();
//        ApplyPolicyManager.getInstance().doSomethingIfNeed();
        PushManager.getInstance().HPushInit();




    }

    public static Context getContext() {
        return mContext;
    }

    private void checkDeviceOwner() {
        String enrollCode = DeviceManager.getInstance().getEnrollCode();
        LogUtils.d( "enrollCode = " + enrollCode + "; versionname = " + BuildConfig.VERSION_NAME);
//        if (enrollCode != null && !ApplyPolicyManager.getInstance().isDeviceOwner()) {
//            // This app is set up as the device owner. Show the main features.
//            LogUtils.e( "The app is not the device owner. set it");
//            DeviceManager.getInstance().setDeviceOwner();
//
//        } else if (ApplyPolicyManager.getInstance().isDeviceOwner()) {
            AlarmUtils.Sync_data(this,1000 * 60 * 60);
            MDMService.startService(mContext);
//        }
    }

    private void rigisterDateChangeBroadcast() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_DATE_CHANGED);
        DataChangeReceiver receiver = new DataChangeReceiver();
        this.registerReceiver(receiver, intentFilter);
    }

    private void rigisterNetChangeBroadcast() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        NetChangeReceiver receiver = new NetChangeReceiver();
        this.registerReceiver(receiver, intentFilter);
    }



    private void rigisterAlarmBroadcast() {
        IntentFilter intentFilter = new IntentFilter(AlarmUtils.SEND_CUSTOM_SERVICE_ACTION);
        AlarmReceiver receiver = new AlarmReceiver();
        this.registerReceiver(receiver, intentFilter);
    }

    private void rigisterScreenBroadcast() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON); // 屏幕亮屏广播
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF); // 屏幕灭屏广播
        intentFilter.addAction(Intent.ACTION_USER_PRESENT); // 屏幕解锁广播
        ScreenReceiver receiver = new ScreenReceiver();
        this.registerReceiver(receiver, intentFilter);
    }
}
