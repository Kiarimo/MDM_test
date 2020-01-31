package com.abupdate.mdm.manager;

import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Environment;
import android.os.UserManager;

import androidx.annotation.RequiresApi;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.download.DownloadUtils;
import com.abupdate.mdm.receiver.DeviceOwnerReceiver;
import com.abupdate.mdm.utils.LogUtils;
import com.system.api.SystemApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class AppManager {

    private static AppManager mAppManager = null;
    private static Context mContext = null;
    private DevicePolicyManager mDpm;
    private ComponentName admin;

    public AppManager() {
        mContext = MyApplication.getContext();
        mDpm = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        admin = DeviceOwnerReceiver.getComponentName(mContext);
    }

    public static AppManager getInstance() {
        if (mAppManager == null) {
            mAppManager = new AppManager();
        }
        return mAppManager;
    }


    public boolean setInstallType(String type, String packageName, String url) {
        LogUtils.d("");
        LogUtils.d("type = " + type + "; packageName = " + packageName + "; url = " + url);
        if ("3".equals(type)) {
            if (appExist(mContext, packageName)) {
                mDpm.setUninstallBlocked(admin, packageName, false);
                unInstallApk(packageName);
            }
            return true;
        } else {
            if (!appExist(mContext, packageName)) {

                downloadApk(url, packageName, type);
                return mDpm.isUninstallBlocked(admin, packageName) == isUninstall(type);
            } else {
                mDpm.setUninstallBlocked(admin, packageName, isUninstall(type));
                return true;
            }
        }
    }


    public void downloadApk(String url, String packagename, String type) {
        LogUtils.d("");
        String downloadpath = Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_DOWNLOADS) + File.separator;
        String filename = new File(url).getName();
        File file = new File(downloadpath + filename);
        String installApp = PrefManager.getInstance().getInstallApp();
        LogUtils.d("installApp = " + installApp);
        DownloadUtils.getInstance().download(url, downloadpath, filename, new DownloadUtils.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                LogUtils.d("onDownloadSuccess");
                try {
                    if (installApp.equals("0")) {
                        mDpm.clearUserRestriction(admin, UserManager.DISALLOW_INSTALL_APPS);
                    }
                    if (installPackage(mContext, new FileInputStream(file), null)) {
                        mDpm.setUninstallBlocked(admin, packagename, isUninstall(type));
                        if (installApp.equals("0")) {
                            mDpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_APPS);
                        }
                        LogUtils.d("installPackage Success");
                    }

                } catch (IOException e) {
                    LogUtils.d("IOException = " + e.getMessage());
                    e.printStackTrace();
                }

            }

            @Override
            public void onDownloadProgress(int pro) {
                LogUtils.d("onDownloadProgress = " + pro);
            }

            @Override
            public void onDownloadFailed() {
                LogUtils.d("onDownloadFailed");
            }
        });
    }


    public boolean installPackage(Context context, InputStream in, String packageName)
            throws IOException {
        final PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        final PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(packageName);
        // set params
        final int sessionId = packageInstaller.createSession(params);
        final PackageInstaller.Session session = packageInstaller.openSession(sessionId);
        final OutputStream out = session.openWrite("MDM", 0, -1);
        final byte[] buffer = new byte[65536];
        int c;
        while ((c = in.read(buffer)) != -1) {
            out.write(buffer, 0, c);
        }
        session.fsync(out);
        in.close();
        out.close();

        session.commit(createInstallIntentSender(context, sessionId));
        return true;
    }

    private static IntentSender createInstallIntentSender(Context context, int sessionId) {
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, sessionId,
                new Intent("com.abupdate.mdm.INSTALL_COMPLETE"), 0);
        return pendingIntent.getIntentSender();
    }


    public void unInstallApk(String packageName) {
        LogUtils.d("");
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent sender = PendingIntent.getActivity(mContext, 0, intent, 0);
        PackageInstaller mPackageInstaller = mContext.getPackageManager().getPackageInstaller();
        mPackageInstaller.uninstall(packageName, sender.getIntentSender());// 卸载APK
        LogUtils.d("unInstallApk succeed");
    }


    /**
     * 判断应用是否存在
     *
     * @param context     上下文
     * @param packageName 包名
     * @return 是否存在
     */
    public boolean appExist(Context context, String packageName) {
        try {
            List<PackageInfo> packageInfoList = context.getPackageManager().getInstalledPackages(0);
            for (PackageInfo packageInfo : packageInfoList) {
                if (packageInfo.packageName.equalsIgnoreCase(packageName)) {
                    return true;
                }
            }
        } catch (Exception e) {

        }
        return false;
    }


    public boolean isUninstall(String value) {
        return "1".equals(value) ? false : true;
    }


    public void startAPP(String appPackageName) {
        try {
            getLauncherActivityNameByPackageName(mContext, appPackageName);
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(appPackageName);
            mContext.startActivity(intent);
        } catch (Exception e) {
            LogUtils.e("Exception = " + e.getMessage());
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void startActivity(String name) {
        LogUtils.d("");
        String occupyName = PrefManager.getInstance().getOccupyName();
        LogUtils.d("name = " + name + "; occupyName = " + occupyName);

        if (name != null) {
            mDpm.setLockTaskPackages(admin, new String[]{name});
            AppManager.getInstance().startAPP(name);
        } else {
            mDpm.setLockTaskPackages(admin, new String[]{occupyName});
            AppManager.getInstance().startAPP(occupyName);
        }


//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        android.app.ActivityManager am = (android.app.ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        int taskId = am.getRunningTasks(1).get(0).id;
        String activity = am.getRunningTasks(1).get(0).topActivity.getPackageName();
        LogUtils.e("activity = " + activity + "; taskId = " + taskId);

        SystemApi.getInstance(mContext).setOccupyScreen(taskId, false);

    }


    public String getLauncherActivityNameByPackageName(Context context, String packageName) {
        String className = null;
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);//android.intent.action.MAIN
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);//android.intent.category.LAUNCHER
        resolveIntent.setPackage(packageName);
        List<ResolveInfo> resolveinfoList = context.getPackageManager().queryIntentActivities(resolveIntent, 0);
        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            className = resolveinfo.activityInfo.name;
        }

        LogUtils.d("className = " + className);

        return className;
    }

    public int getAppCode(Context context, String name) {
        LogUtils.d("getAppCode");
        int code = 0;
        if (!AppManager.getInstance().appExist(context, name)) {
            return code;
        }

        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(name, 0);
            code = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.d("NameNotFoundException = " + e.getMessage());
            e.printStackTrace();
        }
        LogUtils.d("code = " + code);
        return code;
    }


    public String getAppName(Context context, String name) {

        if (!AppManager.getInstance().appExist(context, name)) {
            return "";
        }

        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo appInfo = null;
        String lable = null;
        try {
            appInfo = packageManager.getApplicationInfo(name, PackageManager.GET_META_DATA);
            lable = (String) context.getPackageManager().getApplicationLabel(appInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return lable;
    }

}
