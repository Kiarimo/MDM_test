package com.abupdate.mdm.manager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.callback.HttpsCallBack;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.config.ServerApi;
import com.abupdate.mdm.model.AppListModel;
import com.abupdate.mdm.model.AppModel;
import com.abupdate.mdm.model.PolicyModel;
import com.abupdate.mdm.utils.LogUtils;
import com.abupdate.mdm.utils.OkHttpUtils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @date   : 2019/09/26
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class ReportManager {
    private static ReportManager mReportManager = null;
    private static Context mContext = null;

    private static String IMEI1 = "imei1";
    private static String IMEI2 = "imei2";
    private static String MAC = "mac";
    private static String FILE = "file";


    public ReportManager() {
        mContext = MyApplication.getContext();
    }

    public static ReportManager getInstance() {
        if (mReportManager == null) {
            mReportManager = new ReportManager();
        }
        return mReportManager;
    }

    public void reportAppList() {
        String url = ServerApi.getApiApp();
        String json = getAppListJson(mContext);
        doReport(url, json);
    }

    public void doReport() {
        List<PolicyModel> list = DBManager.getInstance().getReportList();
        int size = list.size();
        if (size == 0) {
            LogUtils.e("no data to report!!");
            return;
        }
        LogUtils.d("");
        String url = ServerApi.getApiReport();
        for (int i = 0; i < size; i++) {
            PolicyModel policyModel = list.get(i);
            if (policyModel.getStatus() != 0) {
                policyModel.setSn(DeviceManager.getInstance().getSN());
                policyModel.setImei1(DeviceManager.getInstance().getImei(Const.SLOT_ID_1));
                policyModel.setImei2(DeviceManager.getInstance().getImei(Const.SLOT_ID_2));
                policyModel.setMac(DeviceManager.getInstance().getMAC());
                Gson gson = new Gson();
                String json = gson.toJson(policyModel);
                doReport(url, json);
            }
        }
    }

    public void doReportCapture(String path) {
        LogUtils.d("");
        String url = ServerApi.getApiCapture();
        Map<String, String> params = new HashMap<>();
        params.put(Const.EMM_SN, DeviceManager.getInstance().getSN());
        params.put(IMEI1, DeviceManager.getInstance().getImei(Const.SLOT_ID_1));
        params.put(IMEI2, DeviceManager.getInstance().getImei(Const.SLOT_ID_2));
        params.put(MAC, DeviceManager.getInstance().getMAC());
        params.put(FILE, path);
        OkHttpUtils.getInstance().postMultipart(url, params, new HttpsCallBack() {
            @Override
            public void onFailure(int errCode,String message) {
                LogUtils.d("onFailure = " + errCode);
            }

            @Override
            public void onSuccess(JSONObject json) {
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                    LogUtils.d("Upload successful, delete the screenshot");
                }
                LogUtils.d("onSuccess = " + json.toString());
            }
        });
    }

    private void doReport(final String url, final String json) {
        OkHttpUtils.getInstance().postJson(url, json, new HttpsCallBack() {
            @Override
            public void onFailure(int errCode,String message) {

            }

            @Override
            public void onSuccess(JSONObject response) {
                LogUtils.d("result = " + response);
                if (response.has(Const.POLICY_VERSION)) {
                    String version = null;
                    try {
                        version = response.getString(Const.POLICY_VERSION);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (!TextUtils.isEmpty(version)) {
                        LogUtils.d("delete db by version:" + version);
                        DBManager.getInstance().deleteReportList(version);
                    }
                }

            }
        });
    }

    private String getAppListJson(Context context) {
        AppListModel appList = new AppListModel();
        appList.setSn(DeviceManager.getInstance().getSN());
        appList.setImei1(DeviceManager.getInstance().getImei(Const.SLOT_ID_1));
        appList.setImei2(DeviceManager.getInstance().getImei(Const.SLOT_ID_2));
        appList.setMac(DeviceManager.getInstance().getMAC());

        List<AppModel> apps = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        // Return a List of all packages that are installed on the device.
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            AppModel app = new AppModel();
            app.setAppName(packageInfo.applicationInfo.loadLabel(pm).toString());
            app.setPackageName(packageInfo.packageName);
            app.setVersion(packageInfo.versionName);
            app.setCode(packageInfo.versionCode);

//            // 判断系统/非系统应用
//            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) // 非系统应用
//            {
//                System.out.println("MainActivity.getAppList, packageInfo=" + packageInfo.packageName);
//            } else {
//                // 系统应用
//            }
            apps.add(app);
        }
        appList.setData(apps);

        Gson gson = new Gson();
        String json = gson.toJson(appList);

        return json;
    }

}
