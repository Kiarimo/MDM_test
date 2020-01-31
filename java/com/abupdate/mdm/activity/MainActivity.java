package com.abupdate.mdm.activity;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.abupdate.mdm.BuildConfig;
import com.abupdate.mdm.R;
import com.abupdate.mdm.callback.HttpsCallBack;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.manager.ActivityManager;
import com.abupdate.mdm.manager.AppManager;
import com.abupdate.mdm.manager.ApplyPolicyManager;
import com.abupdate.mdm.manager.DeviceManager;
import com.abupdate.mdm.manager.NoticeManager;
import com.abupdate.mdm.manager.PrefManager;
import com.abupdate.mdm.manager.RegisterManager;
import com.abupdate.mdm.manager.RequestManager;
import com.abupdate.mdm.manager.ToastManager;
import com.abupdate.mdm.receiver.DeviceOwnerReceiver;
import com.abupdate.mdm.utils.LogUtils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private TextView mSetup, mActivate, mStatusBarTime, mInfo;
    private ImageView mStatusBarWifi;
    private ComponentName mAdminComponentName;
    private DevicePolicyManager mDevicePolicyManager;
    private PackageManager mPackageManager;

    private ArrayList<String> mKioskPackages;

    private static MainActivity mainActivity = null;
    private int mClickCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtils.d("");
        setContentView(R.layout.activity_connect);
        mAdminComponentName = DeviceOwnerReceiver.getComponentName(getApplicationContext());
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mPackageManager = getPackageManager();

        mainActivity = this;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
        }
        initView();

        if (ApplyPolicyManager.getInstance().isDeviceOwner()) {
            LogUtils.d("The app is the device owner.");
        } else {
            DeviceManager.getInstance().setDeviceOwner();
            LogUtils.e("The app is not the device owner. set it");
        }
        setOccupyScreenPolicies(true);
    }


    @Override
    protected void onPause() {
        LogUtils.d("");
//        NoticeManager.getInstance().dismissIfNeed();
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    private void initView() {
        mSetup = findViewById(R.id.setup_wifi);
        mSetup.setOnClickListener(this);
        mActivate = findViewById(R.id.btn_activate);
        mActivate.setOnClickListener(this);
        mStatusBarTime = findViewById(R.id.tv_datetime);
        mStatusBarWifi = findViewById(R.id.ic_wifi);
        mInfo = findViewById(R.id.tv_info);
        mInfo.setOnClickListener(this);

    }


    public void setOccupyScreenPolicies(boolean active) {
//        if (active) {
//            saveCurrentUserRestricitions();
//        }
        mKioskPackages = new ArrayList<>();
        mKioskPackages.remove(getPackageName());
        mKioskPackages.add(getPackageName());
//        setUserRestricitions(!active);
        // set lock task packages
        LogUtils.d("name = " + mKioskPackages.toString());
        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, active ? mKioskPackages.toArray(new String[]{}) : new String[]{});

    }


    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.d("");
        registerBroadcast();
        // start lock task mode if it's not already active
        android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        // ActivityManager.getLockTaskModeState api is not available in pre-M.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (!am.isInLockTaskMode()) {
                startLockTask();
            }
        } else {
            if (am.getLockTaskModeState() == android.app.ActivityManager.LOCK_TASK_MODE_NONE) {
                startLockTask();
            }
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        LogUtils.d("keyCode = " + keyCode);
        return false;
    }

//    @Override
//    protected void onDestroy() {
//        stopLockTask();
//        unregisterReceiver(broadcastReceiver);
//        super.onDestroy();
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean value = intent.getBooleanExtra("dismiss", false);
        LogUtils.e("value = " + value);
        if (value) {
            onBackdoorClicked();
        }
    }


    private void onClickTitle() {

        StringBuilder sb = new StringBuilder();
        sb.append("Equipment model：");
        sb.append(Build.MODEL);
        sb.append("\n");

        sb.append("Android version：");
        sb.append(Build.VERSION.RELEASE);
        sb.append("\n");

        sb.append("System version：");
        sb.append(Build.DISPLAY);
        sb.append("\n");

        sb.append("APP version：");
        sb.append(BuildConfig.VERSION_NAME);
        sb.append("\n");

        sb.append("Device  SN：");
        sb.append(Build.getSerial());

        NoticeManager.getInstance().showInfoDialog(this, null, sb.toString());

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_info:
                if (++mClickCount > 4) {
                    LogUtils.d("show info");
                    mClickCount = 0;
                    onClickTitle();
                }
                break;

            case R.id.setup_wifi:
                LogUtils.d("setup_wifi");
                Intent intent_wifi = new Intent(Settings.ACTION_WIFI_SETTINGS);
                intent_wifi.putExtra("mdm", true);
                startActivity(intent_wifi); //直接进入手机中的wifi网络设置界面
                break;

            case R.id.btn_activate:
                LogUtils.d("next step");


//                ActivityManager.dismissMainScreen(MainActivity.this);

//                ActivityManager.removeAllActivity();
//                ActivityManager.showDownloadScreen(MainActivity.this);


                if (CommonUtils.isFastDoubleClick()) {
                    ToastManager.getInstance().showToast(getString(R.string.button_click_toast));
                    return;
                }
                CommonUtils.setLastTime();


                String code = DeviceManager.getInstance().getEnrollCode();
                LogUtils.d("enrollCode = " + code);
                String name = PrefManager.getInstance().getOccupyName();
                boolean updateresult = PrefManager.getInstance().getUpdateResult();
                LogUtils.d("name = " + name + "; updateresult = " + updateresult);
                if (code == null) {
                    if (DeviceManager.getInstance().isNetworkAvailable()) {
                        RegisterManager.getInstance().doRegister(Const.EMM_REGISTER_TYPE_SYNC + "", null, null, null);
                    } else {
                        ToastManager.getInstance().showToast(getString(R.string.network_error_content));
                    }
                } else {


                    if (updateresult) {
                        ActivityManager.dismissMainScreen(MainActivity.this);
                        ActivityManager.removeAllActivity();
                        AppManager.getInstance().startActivity(name);
                    } else {
                        RequestManager.getInstance().initCheck(new HttpsCallBack() {
                            @Override
                            public void onFailure(int errCode, String message) {
                                LogUtils.e("errCode = " + errCode + "; message = " + message);
                                ToastManager.getInstance().showToast(message);
                            }

                            @Override
                            public void onSuccess(JSONObject json) {

                            }
                        });
                    }


                }
                break;

        }
    }


    public void onBackdoorClicked() {
        stopLockTask();
        mDevicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName,
                getPackageName());

        ComponentName oldLauncher = new ComponentName("com.android.launcher3", "com.android.launcher3.Launcher");
        mPackageManager.setComponentEnabledSetting(
                oldLauncher,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        mDevicePolicyManager.addPersistentPreferredActivity(mAdminComponentName, getHomeIntentFilter(), oldLauncher);

        mPackageManager.setComponentEnabledSetting(
                new ComponentName(getPackageName(), getClass().getName()),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        finish();
    }

    private IntentFilter getHomeIntentFilter() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        return filter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcast();
        updateTime();
        updateWifi();
    }

    private void updateWifi() {
        if (isWifiConnect()) {
            WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            int wifi = mWifiInfo.getRssi();//获取wifi信号强度
            LogUtils.d("wifi level = " + wifi);
            int resId;
            if (wifi > -50 && wifi < 0) {//最强
                resId = R.drawable.ic_signal_wifi_4_bar_black_24dp;
            } else if (wifi > -70 && wifi < -50) {//较强
                resId = R.drawable.ic_signal_wifi_3_bar_black_24dp;
            } else if (wifi > -80 && wifi < -70) {//较弱
                resId = R.drawable.ic_signal_wifi_2_bar_black_24dp;
            } else if (wifi > -100 && wifi < -80) {//微弱
                resId = R.drawable.ic_signal_wifi_1_bar_black_24dp;
            } else {
                resId = R.drawable.ic_signal_wifi_0_bar_black_24dp;
            }
            mStatusBarWifi.setImageResource(resId);
            mStatusBarWifi.setVisibility(View.VISIBLE);
        } else {
            mStatusBarWifi.setVisibility(View.GONE);
        }
    }


    private void updateTime() {
        String regex = "MM-dd EE";
        if (Locale.getDefault().getLanguage().equals("zh")) {
            regex = "MM月dd日 EE";
        }

        if (DateFormat.is24HourFormat(this)) {
            regex = regex + " HH:mm";
        } else {
            regex = regex + " hh:mm aa";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(regex);// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        mStatusBarTime.setText(simpleDateFormat.format(date));
    }


    private boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d(action);
            if (Intent.ACTION_TIME_TICK.equals(action)
                    || Intent.ACTION_TIME_CHANGED.equals(action)) {
                updateTime();//每一分钟更新时间
                updateWifi();
            }
        }
    };

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(broadcastReceiver, filter);
    }

    private static class CommonUtils {
        private static long lastClickTime;

        public static boolean isFastDoubleClick() {
            long time = System.currentTimeMillis();
            long timeD = time - lastClickTime;
            return 0 < timeD && timeD < 5000;
        }

        public static void setLastTime() {
            lastClickTime = System.currentTimeMillis();
        }
    }


}
