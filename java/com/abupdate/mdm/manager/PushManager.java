package com.abupdate.mdm.manager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.abupdate.hpush.HPushManager;
import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.callback.HttpsCallBack;
import com.abupdate.mdm.callback.MDMHPushListener;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.config.ServerApi;
import com.abupdate.mdm.utils.LogUtils;
import com.abupdate.mdm.utils.OkHttpUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/*
 * @date   : 2019/09/27
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class PushManager {
    private static PushManager mPushManager = null;
    private static Context mContext = null;
    private static final String DEVICE_ID = "deviceId";
    private static final String PROJECT_CODE = "projectCode";

    private static final String PUSH_TYPE_NOTICE = "0";
    private static final String PUSH_TYPE_POLICY = "1";
    private static final String PUSH_TYPE_CMD = "2";
    private static final String PUSH_TYPE_APP = "3";
    private static final String PUSH_TYPE_LIST = "4";
    private static final String PUSH_TYPE_SYNC_DATA = "5";

    private boolean isRegistered;

    public PushManager() {
        mContext = MyApplication.getContext();
        isRegistered = false;
    }

    public static PushManager getInstance() {
        if (mPushManager == null) {
            mPushManager = new PushManager();
        }
        return mPushManager;
    }

    public void dispatchType(String type, String taskId) {
        switch (type) {
            case PUSH_TYPE_NOTICE:
                doNotice(taskId);
                break;

            case PUSH_TYPE_POLICY:
                doPolicy(taskId);
                break;

            case PUSH_TYPE_CMD:
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Thread.sleep(3000);//休眠3秒
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        /**
                         * 要执行的操作
                         */

                        doCmd(taskId);
                    }
                }.start();
                break;

            case PUSH_TYPE_APP:
                doApp(taskId);
                break;

            case PUSH_TYPE_LIST:
                doList();
                break;

            case PUSH_TYPE_SYNC_DATA:
                doSyncData();
                break;

            default:
                break;
        }
    }

    private void doNotice(String taskId) {
        RequestManager.getInstance().getNotice(taskId);
    }

    private void doPolicy(String taskId) {
        RequestManager.getInstance().getPolicy(taskId, PUSH_TYPE_POLICY);
    }

    private void doCmd(String taskId) {
        RequestManager.getInstance().getPolicy(taskId, PUSH_TYPE_CMD);
    }

    private void doApp(String taskId) {
        RequestManager.getInstance().getPolicy(taskId, PUSH_TYPE_APP);
    }

    private void doList() {
        ReportManager.getInstance().reportAppList();
    }

    private void doSyncData() {
        RequestManager.getInstance().syncData(null);
    }

    public void HPushInit() {
        HPushManager hPushManager = new HPushManager(mContext);
        hPushManager.init(DeviceManager.getInstance().getSN(), new MDMHPushListener());
    }

    private String getPushKey() {
        String pushKey = null;
        try {
            ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(),
                    PackageManager.GET_META_DATA);
            Bundle metaData = appInfo != null ? appInfo.metaData : null;
            if (null != metaData) {
                pushKey = metaData.getString("hpush_key");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return pushKey;
    }

    private String getHPushRegisterJson() {
        Map<String, String> params = new HashMap<>();
        if (Const.DEBUG_MODE) {
            params.put(DEVICE_ID, "0190927112932");
            params.put(PROJECT_CODE, "258666");
        } else {
            params.put(DEVICE_ID, DeviceManager.getInstance().getSN());
            params.put(PROJECT_CODE, getPushKey());
        }

        //Map 转成  JSONObject 字符串
        JSONObject jsonObj = new JSONObject(params);
        return jsonObj.toString();
    }

    public void HPushRegister() {
        LogUtils.d("isRegistered = " + isRegistered);
        if (isRegistered) {
            return;
        }
        String url = ServerApi.getApiRegister();
        LogUtils.d("url = " + url);
        String json = getHPushRegisterJson();
        LogUtils.d("json = " + json);
        OkHttpUtils.getInstance().postJson(url, json, new HttpsCallBack() {
            @Override
            public void onFailure(int errCode, String message) {
                LogUtils.e("errCode = " + errCode);
            }

            @Override
            public void onSuccess(JSONObject response) {
                LogUtils.d(response.toString());
                isRegistered = true;
            }
        });
    }
}
