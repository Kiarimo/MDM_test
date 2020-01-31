package com.abupdate.mdm.activity;


import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.R;
import com.abupdate.mdm.callback.HttpsCallBack;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.download.DownloadUtils;
import com.abupdate.mdm.manager.AppManager;
import com.abupdate.mdm.manager.ApplyPolicyManager;
import com.abupdate.mdm.manager.DBManager;
import com.abupdate.mdm.manager.DeviceManager;
import com.abupdate.mdm.manager.PrefManager;
import com.abupdate.mdm.manager.RequestManager;
import com.abupdate.mdm.manager.ToastManager;
import com.abupdate.mdm.model.PolicyDataModel;
import com.abupdate.mdm.model.PolicyModel;
import com.abupdate.mdm.receiver.DeviceOwnerReceiver;
import com.abupdate.mdm.receiver.PackageInstallReceiver;
import com.abupdate.mdm.utils.FileUtil;
import com.abupdate.mdm.utils.LogUtils;
import com.abupdate.mdm.utils.PrefUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DownloadActivity extends BaseActivity implements View.OnClickListener {
    private ComponentName mAdminComponentName;
    private DevicePolicyManager mDevicePolicyManager;
    private PackageManager mPackageManager;
    private ProgressBar mProgress;
    private TextView mProtv, mStatusBarTime, mUpdateName;
    private ImageView mStatusBarWifi;
    private Button mReset, mResetData;
    private ArrayList<String> mKioskPackages;
    private ProgressDialog progressdialog;
    private static final int UPDATE_PROGRESSBAR = 11;

    private static final int DOWNLOAD_FAIL = 22;

    private static final int DOWNLOAD_SUCCESS = 33;


    private static final int INSTALL_SUCCESS = 44;

    private static final int INSTALL_FAIL = 55;

    private AlertDialog dialog;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.e("");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        mAdminComponentName = DeviceOwnerReceiver.getComponentName(getApplicationContext());
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mPackageManager = getPackageManager();
        setOccupyScreenPolicies(true);
        rigisterPackageInstallBroadcast();
        ApplyPolicyManager.getInstance().setEnterWiFiPolicies(true, DownloadActivity.class);

        initView();
        download();
    }


    private void download() {
        String url = PrefManager.getInstance().getUpdateFileUrl();
        LogUtils.d("url = " + url);
//        String url = "http://119.28.8.172:8080/html/FOTA5.26_withIcon_20190821.apk";
        downloadApk(url);

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


    private void initView() {
        mStatusBarTime = findViewById(R.id.tv_datetime);
        mStatusBarWifi = findViewById(R.id.ic_wifi);
        mProgress = findViewById(R.id.progress_bar);
        mProtv = findViewById(R.id.pro_txt);
        mUpdateName = findViewById(R.id.update_apkname);
        mUpdateName.setText("Updating " + AppManager.getInstance().getAppName(DownloadActivity.this, PrefManager.getInstance().getUpdateApkName()));
        mReset = findViewById(R.id.reset_wifi);
        mReset.setOnClickListener(this);
        mResetData = findViewById(R.id.reset_data);
        mResetData.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();

//        PrefManager.getInstance().setInKiosk(true);
        // start lock task mode if it's not already active
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        // ActivityManager.getLockTaskModeState api is not available in pre-M.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (!am.isInLockTaskMode()) {
                startLockTask();
            }
        } else {
            if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
                startLockTask();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.reset_wifi:
                if (CommonUtils.isFastDoubleClick()) {
                    ToastManager.getInstance().showToast(getString(R.string.button_click_toast));
                    return;
                }
                CommonUtils.setLastTime();

                LogUtils.d("setup_wifi");
                Intent intent_wifi = new Intent(Settings.ACTION_WIFI_SETTINGS);
                intent_wifi.putExtra("mdm", true);
                startActivity(intent_wifi); //直接进入手机中的wifi网络设置界面
                break;


            case R.id.reset_data:
                LogUtils.d("reset_data");
                if (DeviceManager.getInstance().isNetworkAvailable()) {
                    mResetData.setVisibility(View.GONE);
                    mReset.setVisibility(View.GONE);
//                    NoticeManager.getInstance().showLoadingDialog(DownloadActivity.this, getString(R.string.sync_data));
//                    showLoadingDialog(DownloadActivity.this, getString(R.string.sync_data));
                    setDownloadProgress(0);
                    resetData();
                } else {
                    mResetData.setVisibility(View.VISIBLE);
                    mReset.setVisibility(View.VISIBLE);
//                    Toast.makeText(DownloadActivity.this, R.string.network_error_content, Toast.LENGTH_SHORT).show();
                    ToastManager.getInstance().showToast(getString(R.string.network_error_content));
                }
                break;

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtils.d("keyCode = " + keyCode);
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        LogUtils.d("keyCode = " + keyCode);
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.e("");
        ApplyPolicyManager.getInstance().setEnterWiFiPolicies(true, DownloadActivity.class);
        registerBroadcast();
        updateTime();
        updateWifi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.e("");
    }

//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    public void initData() {
//        List<PolicyModel> reportLists = DBManager.getInstance().getReportList();
//        int size = reportLists.size();
//        LogUtils.d("size = " + size);
//        for (int i = 0; i < size; i++) {
//            PolicyModel policyModel = reportLists.get(i);
//            LogUtils.d("getStatus = " + policyModel.getStatus());
//            LogUtils.d("getVersion = " + policyModel.getVersion());
//            if (policyModel.getStatus() != 1) {
//                applyPolicy(policyModel);
//            }
//        }
//
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    private void applyPolicy(PolicyModel reportList) {
//        LogUtils.d("");
//        List<PolicyDataModel> dataList = reportList.getData();
//        int size = dataList.size();
//        LogUtils.d("size = " + size);
//        for (int i = 0; i < size; i++) {
//            PolicyDataModel data = dataList.get(i);
//            LogUtils.d("getName = " + data.getName());
//            LogUtils.d("getResult = " + data.getResult());
//            if (data.getName().equals(Const.POLICY_INITIAL_CHECK)) {
////                initData(data);
//                initApk(data);
//            }
//
//
//        }

//        PolicyManager.getInstance().updatePolicyStatus(reportList.getVersion());
//        ReportManager.getInstance().doReport();
//    }


//    @RequiresApi(api = Build.VERSION_CODES.Q)
//    private void initApk(PolicyDataModel data) {
//        String packagename = data.getExtend1();
//        LogUtils.d("packagename = " + packagename);
//        mUpdateName.setText("Updating " + AppManager.getInstance().getAppName(MyApplication.getContext(), packagename));
//        String url = data.getExtend3();
//        LogUtils.d("url = " + url);
//        downloadApk(url);
//
//
//    }


//    private void initData(PolicyDataModel data) {
//        String name = data.getName();
//        LogUtils.d("name = " + name);
//        String packagename = data.getExtend1();
//        LogUtils.d("packagename = " + packagename);

//        String code = data.getExtend2();
//        LogUtils.d("code = " + code);
//        String url = data.getExtend3();
//        LogUtils.d("url = " + url);

//        String[] result = code.split("#");
//        String apkcode = result[0];
//        LogUtils.d("apkcode = " + apkcode);
//        long apksize = Long.parseLong(result[1]);
//        LogUtils.d("apksize = " + apksize);


//        String md5 = result[2];
//        LogUtils.d("md5 = " + md5);
//        PrefManager.getInstance().setMd5(md5);

//        DBManager.getInstance().updatePolicyDataResult(version, name);
//    }

    public void showLoadingDialog(Context context, String message) {
        LogUtils.e("showLoadingDialog");
        progressdialog = new ProgressDialog(context);
        progressdialog.setMessage(message);
        progressdialog.setCancelable(false);
        progressdialog.show();
    }


    private void downloadApk(String url) {

        LogUtils.d("");
        String downloadpath = Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_DOWNLOADS) + File.separator;
        LogUtils.d("downloadpath = " + downloadpath);
        String filename = new File(url).getName();
        File file = new File(downloadpath + filename);

        String md5 = PrefManager.getInstance().getMd5();
        LogUtils.d("md5 = " + md5);


        DownloadUtils.getInstance().download(url, downloadpath, filename, new DownloadUtils.OnDownloadListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onDownloadSuccess() {
                LogUtils.e("onDownloadSuccess");

//                com.abupdate.mdm.manager.ActivityManager.removeAllActivity();
//                AppManager.getInstance().startActivity(PrefManager.getInstance().getOccupyName());


                mHandler.sendEmptyMessageDelayed(DOWNLOAD_SUCCESS, 1000);
                String apkmd5 = getFileMD5(downloadpath + filename);
                LogUtils.d("servicemd5 = " + md5);
                LogUtils.d("apkmd5 = " + apkmd5);

                String occupyname = PrefManager.getInstance().getOccupyName();
                LogUtils.d("occupyname = " + occupyname);

                if (md5.equals(apkmd5)) {
                    try {
                        installPackage(MyApplication.getContext(), new FileInputStream(file), null);

//                        if (installPackage(MyApplication.getContext(), new FileInputStream(file), null)) {
//                            mHandler.removeCallbacksAndMessages(null);
//                            LogUtils.d("installPackage Success");
//                            NoticeManager.getInstance().dismissIfNeed();
//
//                            if (file.exists()) {
//                                LogUtils.d("file exist and delete");
//                                file.delete();
//                            }
//
//                            com.abupdate.mdm.manager.ActivityManager.removeAllActivity();
//                            AppManager.getInstance().startActivity(occupyname);
//                            PrefManager.getInstance().setUpdateResult(true);
//
//                        } else {
//                            mHandler.sendEmptyMessageDelayed(INSTALL_FAIL, 1000);
//                        }

                    } catch (IOException e) {
                        mHandler.sendEmptyMessageDelayed(INSTALL_FAIL, 1000);
                        LogUtils.d("install fail" + e.getMessage());
                        LogUtils.d("IOException = " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    mHandler.sendEmptyMessageDelayed(INSTALL_FAIL, 1000);
                    LogUtils.d("install fail,sha256 different");
                }

            }

            @Override
            public void onDownloadProgress(int pro) {
//                int percent = getPercent();
//                LogUtils.d("percent = " + percent);
//                if (percent == 100) {
//                    mHandler.sendEmptyMessageDelayed(DOWNLOAD_SUCCESS, 1000);
//                } else if (percent < 100) {
                mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESSBAR, 1000);
//                }
            }

            @Override
            public void onDownloadFailed() {
                mHandler.sendEmptyMessageDelayed(DOWNLOAD_FAIL, 1000);
                LogUtils.e("onDownloadFailed");
            }
        });


    }


    public int getPercent() {
        String url = PrefManager.getInstance().getUpdateFileUrl();
//        LogUtils.d("url = " + url);
        String filename = new File(url).getName();
        String downloadpath = Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_DOWNLOADS) + File.separator;
        long total = PrefUtils.getLong(this, "apksize", 0L);
        long download = FileUtil.getFileSize(downloadpath + filename);
        int percent = (total > 0) ? Long.valueOf((100 * download) / total).intValue() : 0;
        return percent == 0 ? mProgress.getProgress() : percent;
    }


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            int what = msg.what;
//            LogUtils.d("what = " + what);
            switch (what) {
                case UPDATE_PROGRESSBAR:
                    int percent = getPercent();
                    LogUtils.d("percent = " + percent);
                    setDownloadProgress(percent);
                    break;
                case DOWNLOAD_FAIL:
//                    NoticeManager.getInstance().dismissIfNeed();
                    LogUtils.d("DOWNLOAD_FAIL");
//                    showFailDialog(DownloadActivity.this, null, getString(R.string.dialog_download_fail_message));
                    mResetData.setVisibility(View.VISIBLE);
                    mReset.setVisibility(View.VISIBLE);
//                    Toast.makeText(DownloadActivity.this, getString(R.string.dialog_download_fail_message), Toast.LENGTH_SHORT).show();
                    ToastManager.getInstance().showToast(getString(R.string.dialog_download_fail_message));
                    break;
                case DOWNLOAD_SUCCESS:
                    setDownloadProgress(100);
                    if (progressdialog == null) {
                        showLoadingDialog(DownloadActivity.this, "Installing");
                    }
                    break;
                case INSTALL_FAIL:
//                    NoticeManager.getInstance().dismissIfNeed();
//                    showFailDialog(DownloadActivity.this, null, getString(R.string.dialog_install_fail_message));
                    mResetData.setVisibility(View.VISIBLE);
                    mReset.setVisibility(View.VISIBLE);
//                    Toast.makeText(DownloadActivity.this, getString(R.string.dialog_install_fail_message), Toast.LENGTH_SHORT).show();
                    ToastManager.getInstance().showToast(getString(R.string.dialog_install_fail_message));
                    break;
            }

        }
    };


    public void installPackage(Context context, InputStream in, String packageName)
            throws IOException {
        LogUtils.e("installing");

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
        LogUtils.e("installed");
    }

    private static IntentSender createInstallIntentSender(Context context, int sessionId) {
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, sessionId,
                new Intent("com.abupdate.mdm.INSTALL_COMPLETE"), 0);
        return pendingIntent.getIntentSender();
    }


    private void setDownloadProgress(int pro) {
        mProtv.setText(pro + "%");
        mProgress.setProgress(pro);
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


    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(broadcastReceiver, filter);
    }


    private void rigisterPackageInstallBroadcast() {
        LogUtils.e("rigisterPackageInstall");
        PackageInstallReceiver installedReceiver = new PackageInstallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        this.registerReceiver(installedReceiver, filter);
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.e("action = " + action);
            if (Intent.ACTION_TIME_TICK.equals(action)
                    || Intent.ACTION_TIME_CHANGED.equals(action)) {
                updateTime();//每一分钟更新时间
                updateWifi();
            }

            if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                LogUtils.e("ACTION_PACKAGE_ADDED");
            }


        }
    };

    public static String getFileMD5(String file) {
        LogUtils.d("getFileMD5");
        FileInputStream fis = null;
        StringBuffer buf = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 256];
            int length = -1;
            // Trace.d(logTag, "getFileMD5, GenMd5 start");
            long s = System.currentTimeMillis();
            if (fis == null || md == null) {
                return null;
            }
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            byte[] bytes = md.digest();
            if (bytes == null) {
                return null;
            }
            for (int i = 0; i < bytes.length; i++) {
                String md5s = Integer.toHexString(bytes[i] & 0xff);
                if (md5s == null || buf == null) {
                    return null;
                }
                if (md5s.length() == 1) {
                    buf.append("0");
                }
                buf.append(md5s);
            }
            // Trace.d(logTag, "getFileMD5, GenMd5 success! spend the time: "+ (System.currentTimeMillis() - s) + "ms");
            return buf.toString();
        } catch (Exception ex) {
            LogUtils.d("getFileMD5, Exception " + ex.toString());
            ex.printStackTrace();
            return null;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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


    private void showFailDialog(Context context, String title, String content) {
        if (TextUtils.isEmpty(content)) {
            return;
        }

//        if (dialog != null && dialog.isShowing()) {
//            dialog.dismiss();
//        }
//
//
//        if (progressdialog != null && progressdialog.isShowing()) {
//            progressdialog.dismiss();
//        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setMessage(content)
                .setPositiveButton(R.string.dialog_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                        if (DeviceManager.getInstance().isNetworkAvailable()) {
                            mResetData.setVisibility(View.GONE);
                            mReset.setVisibility(View.GONE);
//                            NoticeManager.getInstance().showLoadingDialog(DownloadActivity.this, getString(R.string.sync_data));
                            showLoadingDialog(DownloadActivity.this, getString(R.string.sync_data));
                            setDownloadProgress(0);
                            resetData();
                        } else {
                            mResetData.setVisibility(View.VISIBLE);
                            mReset.setVisibility(View.VISIBLE);
//                            Toast.makeText(context, R.string.network_error_content, Toast.LENGTH_SHORT).show();
                            ToastManager.getInstance().showToast(getString(R.string.network_error_content));
                        }


                    }
                });

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        builder.setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }


    private void resetData() {
        RequestManager.getInstance().initCheck(new HttpsCallBack() {
            @Override
            public void onFailure(int errCode, String message) {
                LogUtils.e("errCode = " + errCode + "; message = " + message);
//                if (dialog != null) {
//                    dialog.dismiss();
//                }
//                showFailDialog(DownloadActivity.this, null, message);

//                Looper.prepare();
//                Toast.makeText(DownloadActivity.this, message, Toast.LENGTH_SHORT).show();
//                Looper.loop();
//                mResetData.setVisibility(View.VISIBLE);
                mHandler.sendEmptyMessageDelayed(DOWNLOAD_FAIL, 1000);
//                Looper.prepare();
//                ToastManager.getInstance().showToast(message);
//                Looper.loop();
            }

            @Override
            public void onSuccess(JSONObject json) {
                LogUtils.d("json = " + json);
                getData(json);
                if (dialog != null) {
                    dialog.dismiss();
                }

                if (progressdialog != null) {
                    progressdialog.dismiss();
                }


                download();
            }
        });
    }

    private void getData(JSONObject json) {

        Gson gson = new Gson();
        PolicyModel policyModel = gson.fromJson(json.toString(), PolicyModel.class);
        DBManager.getInstance().insertPolicyMain(policyModel);
        List<PolicyDataModel> dataList = policyModel.getData();
        int size = dataList.size();
        LogUtils.d("size = " + size);

        if (size == 0) {
            AppManager.getInstance().startActivity(PrefManager.getInstance().getOccupyName());
            return;
        }


        List<String> dataname = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            PolicyDataModel data = dataList.get(i);
            LogUtils.d("getName = " + data.getName());
            dataname.add(data.getName());
        }


        for (int j = 0; j < dataname.size(); j++) {
            PolicyDataModel data = dataList.get(j);
            LogUtils.d("getName = " + data.getName());
            if (data.getName().equals(Const.POLICY_INITIAL_CHECK)) {
                String packagename = data.getExtend1();
                LogUtils.d("packagename = " + packagename);

                PrefManager.getInstance().setUpdateApkName(packagename);

                String codestring = data.getExtend2();
                LogUtils.d("codestring = " + codestring);

                String[] result = codestring.split("#");
                String code = result[0];
                LogUtils.d("code = " + code);
                initData(data);
                int apkcode = AppManager.getInstance().getAppCode(MyApplication.getContext(), packagename);
                LogUtils.d("apkcode = " + apkcode);

//                if (AppManager.getInstance().appExist(MyApplication.getContext(), packagename)) {
//                    if (Integer.parseInt(code) > apkcode) {
//                        com.abupdate.mdm.manager.ActivityManager.removeAllActivity();
//                        com.abupdate.mdm.manager.ActivityManager.showDownloadScreen(MyApplication.getContext());
//                        return;
//                    } else {
//                        break;
//                    }
//                } else {
//                    LogUtils.d("The app does not exist");
//                    break;
//                }
            }
        }

        for (int k = 0; k < dataname.size(); k++) {
            PolicyDataModel data = dataList.get(k);
            LogUtils.d("getName = " + data.getName());
            if (data.getName().equals(Const.POLICY_OCCUPY_SCREEN)) {
                if (!AppManager.getInstance().appExist(MyApplication.getContext(), data.getExtend1())) {
                    LogUtils.d("The app does not exist");
                    return;
                }
                String name = data.getExtend1();
                String value = data.getValue();
                LogUtils.d("name = " + name + "; value = " + value);
                if (data.getValue().equals("1")) {
                    PrefManager.getInstance().setOccupyName(name);
                }

                AppManager.getInstance().startActivity(name);
            }

        }


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

    private void initData(PolicyDataModel data) {
        String name = data.getName();
        LogUtils.d("name = " + name);
        String packagename = data.getExtend1();
        LogUtils.d("packagename = " + packagename);

        String code = data.getExtend2();
        LogUtils.d("code = " + code);
        String url = data.getExtend3();
        LogUtils.d("url = " + url);

        PrefManager.getInstance().setUpdateFileUrl(url);

        String[] result = code.split("#");
        String apkcode = result[0];
        LogUtils.d("apkcode = " + apkcode);
        long apksize = Long.parseLong(result[1]);
        LogUtils.d("apksize = " + apksize);

        String md5 = result[2];
        LogUtils.d("md5 = " + md5);
        PrefManager.getInstance().setMd5(md5);
    }


}
