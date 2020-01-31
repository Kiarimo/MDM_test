package com.abupdate.mdm.manager;

import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.activity.KioskActivity;
import com.abupdate.mdm.activity.LockActivity;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.model.PolicyDataModel;
import com.abupdate.mdm.receiver.DeviceOwnerReceiver;
import com.abupdate.mdm.utils.LogUtils;
import com.abupdate.mdm.utils.OkHttpUtils;
import com.system.api.SystemApi;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/*
 * @date   : 2019/10/10
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class ApplyPolicyManager {
    private static ApplyPolicyManager mApplyPolicyManager = null;
    private DevicePolicyManager mDpm;
    private ComponentName admin;
    private Context mContext;
    private PackageManager mPackageManager;
    protected boolean ret = false;
    private static final String[] LOCKSCREEN_USER_RESTRICTIONS = {
            UserManager.DISALLOW_SAFE_BOOT,
            UserManager.DISALLOW_FACTORY_RESET,
            UserManager.DISALLOW_ADD_USER,
            UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,
//            UserManager.DISALLOW_DEBUGGING_FEATURES,
            UserManager.DISALLOW_ADJUST_VOLUME};

    private static final String[] LOCKSCREEN_PACKAGES = {
            "com.abupdate.mdm",
            "com.android.settings",
            "com.android.phone",
            "com.android.dialer"};


    public ApplyPolicyManager() {
        mContext = MyApplication.getContext();
        mDpm = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        admin = DeviceOwnerReceiver.getComponentName(mContext);
        mPackageManager = mContext.getPackageManager();
    }

    public static ApplyPolicyManager getInstance() {
        if (mApplyPolicyManager == null) {
            mApplyPolicyManager = new ApplyPolicyManager();
        }
        return mApplyPolicyManager;
    }

    public boolean doInstallApp(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            PrefManager.getInstance().setInstallApp(data.getValue());
            setUserRestriction(UserManager.DISALLOW_INSTALL_APPS, isDisallow(data.getValue()));
            return true;
        }
        return false;
    }

    public boolean doUninstallApp(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            setUserRestriction(UserManager.DISALLOW_UNINSTALL_APPS, isDisallow(data.getValue()));
            return true;
        }
        return false;
    }


    public boolean doUnknownSources(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            setUserRestriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, isDisallow(data.getValue()));
            return true;
        }
        return false;
    }

    public boolean doKiosk(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");

            if (null != data && isDisallow(data.getValue())) {
                LogUtils.d("exit doKiosk");
                if (PrefManager.getInstance().isInKiosk()) {
//                    int status = PrefManager.getInstance().getADBStatus();
//                    LogUtils.d("status = " + status);
                    PrefManager.getInstance().setInKiosk(false);
//                    setUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER, false);
//                    setUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES, false);
//                    ActivityManager.dismissKioskScreen(mContext);
//                    Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, status);
                }

            } else {
                LogUtils.d("enter doKiosk");
                if (!PrefManager.getInstance().isInKiosk()) {

//                    setKiosk();

//                    getAdbStatus();
//                    setUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER, true);
//                    setUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES, true);
//                    ActivityManager.showKioskScreen(mContext);
                }

            }
            return true;
        }
        return false;
    }

    public void setKiosk() {
        final ComponentName customLauncher =
                new ComponentName(mContext, KioskActivity.class);

        mDpm.addPersistentPreferredActivity(admin, getHomeIntentFilter(), customLauncher);

        mPackageManager.setComponentEnabledSetting(customLauncher,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }


    public boolean doEnableGPS(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            setUserRestriction(UserManager.DISALLOW_CONFIG_LOCATION, isDisallow(data.getValue()));
            return true;
        }
        return false;
    }

    public boolean doGPSStatus(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            mDpm.setSecureSetting(admin, Settings.Secure.LOCATION_MODE, data.getValue());
        }
        return true;
    }

    public boolean doBluetoothFile(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            setUserRestriction(UserManager.DISALLOW_BLUETOOTH_SHARING, isDisallow(data.getValue()));
            return true;
        }
        return false;
    }

    public boolean doCharageOnly(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            if (!isDisallow(data.getValue())) {
//                getAdbStatus();
                setUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER, true);
                setUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES, true);
            } else {
//                int status = PrefManager.getInstance().getADBStatus();
//                LogUtils.d("status = " + status);
                setUserRestriction(UserManager.DISALLOW_USB_FILE_TRANSFER, false);
                setUserRestriction(UserManager.DISALLOW_DEBUGGING_FEATURES, false);
//                Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, status);
            }
            return true;
        }
        return false;
    }

    public boolean doEnableFactoryReset(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, isDisallow(data.getValue()));
            return true;
        }
        return false;
    }

    public boolean doExtendStatusBar(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            mDpm.setStatusBarDisabled(admin, isDisallow(data.getValue()));
            return true;
        }
        return false;
    }

    public boolean doLockscreenPassword(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            if (isDisallow(data.getValue())) {
                PasswordManager.getInstance().doResetPassword(null);
                Settings.System.putInt(mContext.getContentResolver(), "abupdate_mdm_reset_password", 2);
            } else {
                Settings.System.putInt(mContext.getContentResolver(), "abupdate_mdm_reset_password", 1);
            }
            return true;
        }
        return false;
    }

    public boolean doEnableCamera(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            mDpm.setCameraDisabled(admin, isDisallow(data.getValue()));
            return true;
        }
        return false;
    }

    public boolean doEnableStorage(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            if (isDisallow(data.getValue())) {
                SystemApi.getInstance(mContext).mountUnmountSD(false, mContext);
                setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, true);
            } else {
                setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, false);
                SystemApi.getInstance(mContext).mountUnmountSD(true, mContext);
            }

            return true;
        }
        return false;
    }


    public boolean doLBS(PolicyDataModel data) {
        LogUtils.d("");
        PrefManager.getInstance().setLBSData(data.getValue());
        PrefManager.getInstance().setLBSStatus(data.getExtend1());
        if (LBSManager.getInstance().setLBS()) {
            return true;
        }
        return false;
    }

    public boolean doUseLife(PolicyDataModel data) {
        LogUtils.d("");
        PrefManager.getInstance().setUseLifeData(data.getValue());
        PrefManager.getInstance().setUseLifeStatus(data.getExtend1());
        if (UseLifeManager.getInstance().setUseLife(true)) {
            return true;
        }
        return false;
    }

    public boolean doLock() {
        LogUtils.d("");
        if (isDeviceOwner()) {
            setLockScreenPolicies(true);
            ActivityManager.showLockScreen(mContext);
            return true;
        }
        return false;
    }

    public boolean doUnlock() {
        LogUtils.d("");
        if (isDeviceOwner()) {
            if (!PrefManager.getInstance().isInKiosk()) {
                setLockScreenPolicies(false);
            }
            PrefManager.getInstance().setLockType(Const.LOCK_TYPE_UNKNOWN);
            ActivityManager.dismissLockScreen(mContext);
            return true;
        }
        return false;
    }

    public boolean doCapture() {
        return doScreenShot();
    }

    public boolean doFactoryReset() {
        if (isDeviceOwner()) {
            LogUtils.e("");
            ReportManager.getInstance().doReport();
            mDpm.wipeData(0);
            return true;
        }
        return false;
    }

    public boolean doReboot() {
        if (isDeviceOwner()) {
            LogUtils.e("");
            ReportManager.getInstance().doReport();
            mDpm.reboot(admin);
            return true;
        }
        return false;
    }

    public boolean doShutdown() {
        if (isDeviceOwner()) {
            LogUtils.e("");
            ReportManager.getInstance().doReport();
            String action = "com.android.internal.intent.action.REQUEST_SHUTDOWN";
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                action = "android.intent.action.ACTION_REQUEST_SHUTDOWN";
            }
            Intent intent = new Intent(action);
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            mContext.startActivity(intent);

            return true;
        }
        return false;
    }

    public boolean doClearPassword() {
        if (isDeviceOwner()) {
            LogUtils.d("");
            return PasswordManager.getInstance().doResetPassword(null);
        }
        return false;
    }

    public boolean doChangeWallpaper(PolicyDataModel data) {
        String url = data.getValue();
//        String url = "https://c-ssl.duitang.com/uploads/item/201606/15/20160615081604_jeXSz.thumb.700_0.jpeg";
//        String url = "https://c-ssl.duitang.com/uploads/blog/201512/12/20151212120317_ec2CV.thumb.700_0.jpeg";
//        String url = "https://c-ssl.duitang.com/uploads/item/201703/21/20170321153911_E8YCa.thumb.700_0.jpeg";
        LogUtils.d(url);
        ret = false;
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = OkHttpUtils.getInstance().downloadImage(url);
                if (bitmap != null) {
                    try {
                        LogUtils.d("changeWallpaper");
                        WallpaperManager.getInstance(mContext).setBitmap(bitmap);
                        ret = true;
                    } catch (IOException e) {
                        LogUtils.e(e.getMessage());
                    }
                }
                countDownLatch.countDown();
            }
        }).start();

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            LogUtils.e(e.getMessage());
            //如果当前线程发生中断，再次wait
            try {
                countDownLatch.await();
            } catch (InterruptedException e1) {
                LogUtils.e(e1.getMessage());
            }
        }

        LogUtils.d("ret = " + ret);
        return ret;
    }

    public boolean doBlockNotice(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            NoticeManager.getInstance().setBlockNotice(data.getExtend1(), data.getValue());
            return true;
        }
        return false;
    }

    public boolean doInstallType(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            if (data.getExtend3() == null || TextUtils.isEmpty(data.getExtend3())) {
                LogUtils.d("url is empty");
                return false;
            }
            return AppManager.getInstance().setInstallType(data.getValue(), data.getExtend1(), data.getExtend3());
        }
        return false;
    }

    public boolean doUseApp(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            mDpm.setPackagesSuspended(admin, new String[]{data.getExtend1()}, isDisallow(data.getValue()));
            return true;
        }
        return false;
    }

    public boolean doShowIcon(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            mDpm.setApplicationHidden(admin, data.getExtend1(), isDisallow(data.getValue()));
            return true;
        }
        return false;
    }


    public boolean doChangePassword(PolicyDataModel data) {
        PrefManager.getInstance().setExitPassword(data.getValue());
        return true;
    }

    public boolean doUpdateApp(PolicyDataModel data) {
        if (isDeviceOwner()) {
            if (!isDisallow(data.getValue())) {
                doUpdateApp(data.getExtend1(), data.getExtend2(), data.getExtend3());
            }
            return true;
        }
        return false;
    }

    public boolean doClearAppData(PolicyDataModel data) {
        if (isDeviceOwner()) {

            if (!AppManager.getInstance().appExist(mContext, data.getExtend1())) {
                LogUtils.e("The app does not exist");
                return false;
            }


            String deleteCmd = "pm clear " + data.getExtend1();
            LogUtils.d("deleteCmd = " + deleteCmd);
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) {
                e.printStackTrace();
                LogUtils.d("IOException = " + e.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean doOccupyScreen(PolicyDataModel data) {
        if (isDeviceOwner()) {
            if (!AppManager.getInstance().appExist(mContext, data.getExtend1())) {
                LogUtils.e("The app does not exist");
                return false;
            }

            AppManager.getInstance().startAPP(data.getExtend1());
            android.app.ActivityManager am = (android.app.ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            int taskId = am.getRunningTasks(1).get(0).id;
            String activity = am.getRunningTasks(1).get(0).topActivity.getPackageName();
            LogUtils.e("activity = " + activity + "; taskId = " + taskId);
            if (!isDisallow(data.getValue())) {
                SystemApi.getInstance(mContext).setOccupyScreen(taskId, isDisallow(data.getValue()));

            } else {
                SystemApi.getInstance(mContext).setOccupyScreen(taskId, isDisallow(data.getValue()));
                LogUtils.e("取消霸屏");
            }


            return true;
        }
        return false;
    }

    public boolean doAppPermission(PolicyDataModel data) {
        if (isDeviceOwner()) {
            LogUtils.d("");
            mDpm.setPermissionGrantState(admin, data.getExtend1(), data.getName(), doAppPermission(data.getValue()));
            return true;
        }
        return false;
    }

    public boolean isDeviceOwner() {
        if (mDpm != null) {
            LogUtils.d("");
            return mDpm.isDeviceOwnerApp(mContext.getPackageName());
        }
        return false;
    }

    private IntentFilter getHomeIntentFilter() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        return filter;
    }

    private void setUserRestriction(String restriction, boolean disallow) {
        if (disallow) {
            mDpm.addUserRestriction(admin, restriction);
        } else {
            mDpm.clearUserRestriction(admin, restriction);
        }
    }

    private void saveCurrentUserRestricitions() {
        Bundle settingsBundle = mDpm.getUserRestrictions(admin);
        for (String userRestriction : LOCKSCREEN_USER_RESTRICTIONS) {
            boolean currentSettingValue = settingsBundle.getBoolean(userRestriction);
            PrefManager.getInstance().putBoolean(userRestriction, currentSettingValue);
        }
    }

    private void setLockScreenUserRestricitions(boolean restore) {
        for (String userRestriction : LOCKSCREEN_USER_RESTRICTIONS) {
            boolean value = true;
            if (restore) {
                value = PrefManager.getInstance().getBoolean(userRestriction);
            }
            setUserRestriction(userRestriction, value);
        }
    }


    public void setEnterWiFiPolicies(boolean locked, Class<?> cls) {
        int componentEnable = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        final ComponentName customLauncher =
                new ComponentName(mContext, cls);

        // restore or save previous configuration
        if (locked) {
            componentEnable = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            saveCurrentUserRestricitions();
            // set custom launcher as default home activity
            mDpm.addPersistentPreferredActivity(admin, getHomeIntentFilter(), customLauncher);
        } else {
            mDpm.clearPackagePersistentPreferredActivities(admin, mContext.getPackageName());
        }

        // enable custom launcher (it's disabled by default in manifest)
        mPackageManager.setComponentEnabledSetting(customLauncher,
                componentEnable,
                PackageManager.DONT_KILL_APP);


        // set lock task packages
        mDpm.setLockTaskPackages(admin, locked ? LOCKSCREEN_PACKAGES : new String[]{});
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mDpm.setLockTaskFeatures(admin, locked ? DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO : DevicePolicyManager.LOCK_TASK_FEATURE_NONE);
        }
    }


    public void setKioskScreenPolicies(boolean locked, String name) {
        int componentEnable = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        final ComponentName customLauncher =
                new ComponentName(mContext, KioskActivity.class);

        // restore or save previous configuration
        if (locked) {
            componentEnable = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            saveCurrentUserRestricitions();
            // set custom launcher as default home activity
            mDpm.addPersistentPreferredActivity(admin, getHomeIntentFilter(), customLauncher);
        } else {
            mDpm.clearPackagePersistentPreferredActivities(admin, mContext.getPackageName());
        }

        // enable custom launcher (it's disabled by default in manifest)
        mPackageManager.setComponentEnabledSetting(customLauncher,
                componentEnable,
                PackageManager.DONT_KILL_APP);

        setLockScreenUserRestricitions(!locked);

        // set lock task packages
//        mDpm.setLockTaskPackages(admin, locked ? LOCKSCREEN_PACKAGES : new String[]{});
        mDpm.setLockTaskPackages(admin, new String[]{name});
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mDpm.setLockTaskFeatures(admin, locked ? DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO : DevicePolicyManager.LOCK_TASK_FEATURE_NONE);
        }
    }


    private void setLockScreenPolicies(boolean locked) {
        int componentEnable = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        final ComponentName customLauncher =
                new ComponentName(mContext, LockActivity.class);

        // restore or save previous configuration
        if (locked) {
            componentEnable = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            saveCurrentUserRestricitions();
            // set custom launcher as default home activity
            mDpm.addPersistentPreferredActivity(admin, getHomeIntentFilter(), customLauncher);
        } else {
            mDpm.clearPackagePersistentPreferredActivities(admin, mContext.getPackageName());
        }

        // enable custom launcher (it's disabled by default in manifest)
        mPackageManager.setComponentEnabledSetting(customLauncher,
                componentEnable,
                PackageManager.DONT_KILL_APP);

        setLockScreenUserRestricitions(!locked);

        // set lock task packages
        mDpm.setLockTaskPackages(admin, locked ? LOCKSCREEN_PACKAGES : new String[]{});
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mDpm.setLockTaskFeatures(admin, locked ? DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO : DevicePolicyManager.LOCK_TASK_FEATURE_NONE);
        }
    }

    public void doSomethingIfNeed() {
        if (isDeviceOwner()) {
            UseLifeManager.getInstance().setUseLife(true);
            LBSManager.getInstance().setLBS();
        }
    }

    private boolean doScreenShot() {
        boolean result = true;

        String imagePath = Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_PICTURES) + File.separator + "screenshot.png";
        LogUtils.d("path = " + imagePath);

        try {
            Runtime.getRuntime().exec("screencap -p " + imagePath);
            File imagefile = new File(imagePath);
            if (imagefile.exists()) {
                //Upload a screenshot
                ReportManager.getInstance().doReportCapture(imagePath);
            } else {
                Runtime.getRuntime().exec("screencap -p " + imagePath);
                if (imagefile.exists()) {
                    ReportManager.getInstance().doReportCapture(imagePath);
                } else {
                    result = false;
                    LogUtils.d("screenshot fail ,File does not exist");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
            LogUtils.d("Exception = " + e.getMessage());
        }
        return result;

    }

    private boolean isDisallow(String value) {
        return "0".equals(value) ? true : false;
    }

    private void getAdbStatus() {
        int status = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
        LogUtils.d("getAdbStatus = " + status);
        PrefManager.getInstance().putADBStatus(status);
    }


    private int doAppPermission(String value) {

        return "0".equals(value) ? DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED : DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED;
    }


    public void doUpdateApp(String name, String code, String url) {
        try {
            PackageInfo manager = mContext.getPackageManager().getPackageInfo(
                    name, 0);
            int appcode = manager.versionCode;
            LogUtils.d("code = " + code);
            LogUtils.d("appcode = " + appcode);

            if (!AppManager.getInstance().appExist(mContext, name)) {
                LogUtils.d("The app does not exist");
                return;
            }

            try {
                if (appcode < Integer.parseInt(code)) {
                    AppManager.getInstance().downloadApk(url, name, "1");
                }
            } catch (NumberFormatException e) {
                LogUtils.d("NumberFormatException = " + e.getMessage());
            }


        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.d("NameNotFoundException = " + e.getMessage());
            e.printStackTrace();
        }
    }

}
