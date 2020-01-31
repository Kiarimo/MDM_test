package com.abupdate.mdm.manager;

import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.callback.HttpsCallBack;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.config.ServerApi;
import com.abupdate.mdm.model.NoticeModel;
import com.abupdate.mdm.model.PolicyDataModel;
import com.abupdate.mdm.model.PolicyModel;
import com.abupdate.mdm.model.RequestModel;
import com.abupdate.mdm.utils.LogUtils;
import com.abupdate.mdm.utils.OkHttpUtils;
import com.abupdate.mdm.utils.PrefUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @date   : 2019/10/10
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class RequestManager {
    private static final String CODE = "code";

    private static RequestManager mRequestManager = null;
    private static Context mContext = null;

    public RequestManager() {
        mContext = MyApplication.getContext();
    }

    public static RequestManager getInstance() {
        if (mRequestManager == null) {
            mRequestManager = new RequestManager();
        }
        return mRequestManager;
    }

    private String getRequestJson(String taskId, String intfType) {
        LogUtils.d("taskId = " + taskId + "; intfType = " + intfType);
        RequestModel requestModel = new RequestModel();
        requestModel.setTaskId(taskId);
        requestModel.setIntfType(intfType);
        requestModel.setSn(DeviceManager.getInstance().getSN());
        requestModel.setImei1(DeviceManager.getInstance().getImei(Const.SLOT_ID_1));
        requestModel.setImei2(DeviceManager.getInstance().getImei(Const.SLOT_ID_2));
        requestModel.setMac(DeviceManager.getInstance().getMAC());
        Gson gson = new Gson();
        return gson.toJson(requestModel);
    }

    public void getNotice(String taskId) {
        String url = ServerApi.getApiNotice();
        String json = getRequestJson(taskId, "0");
        OkHttpUtils.getInstance().postJson(url, json, new HttpsCallBack() {
            @Override
            public void onFailure(int errCode, String message) {
                LogUtils.e("errCode = " + errCode + "; message = " + message);
            }

            @Override
            public void onSuccess(JSONObject response) {
                LogUtils.d("result = " + response);
                Gson gson = new Gson();
                NoticeModel noticeModel = gson.fromJson(response.toString(), NoticeModel.class);
                NoticeManager.getInstance().showNotice(noticeModel);
            }
        });
    }

    public void getPolicy(String taskId, String intfType) {
        String url = ServerApi.getApiControl();
        LogUtils.d("url = " + url);
        String json = getRequestJson(taskId, intfType);
        LogUtils.d("json = " + json);
        OkHttpUtils.getInstance().postJson(url, json, new HttpsCallBack() {
            @Override
            public void onFailure(int errCode, String message) {
                LogUtils.e("errCode = " + errCode);
            }

            @Override
            public void onSuccess(JSONObject response) {
                LogUtils.d("result = " + response);
                Gson gson = new Gson();
                PolicyModel policyModel = gson.fromJson(response.toString(), PolicyModel.class);
                DBManager.getInstance().insertPolicyMain(policyModel);
                PolicyManager.getInstance().applyPolicyIfNeed();
            }
        });
    }

    private String getSyncDataJson() {
        Map<String, String> params = new HashMap<>();
        if (Const.DEBUG_MODE) {
            params.put(Const.EMM_SN, "2683676");
            params.put(CODE, "258666");
        } else {
            params.put(Const.EMM_SN, DeviceManager.getInstance().getSN());
            params.put(CODE, DeviceManager.getInstance().getEnrollCode());
        }

        //Map 转成  JSONObject 字符串
        JSONObject jsonObj = new JSONObject(params);
        return jsonObj.toString();

    }

    public void syncData(HttpsCallBack callBack) {
        String url = ServerApi.getApiSync();
        String json = getSyncDataJson();
        LogUtils.d("url = " + url);
        LogUtils.d("json = " + json);
        OkHttpUtils.getInstance().postJson(url, json, new HttpsCallBack() {
            @Override
            public void onFailure(int errCode, String message) {
                LogUtils.e("errCode = " + errCode);
                if (null != callBack) {
                    callBack.onFailure(errCode, message);
                }
            }

            @Override
            public void onSuccess(JSONObject response) {
                String userInfor = response.toString();
                LogUtils.d("result = " + userInfor);
//                PrefManager.getInstance().setUserInformation(userInfor);
//                Gson gson = new Gson();
//                UserModel userModel = gson.fromJson(response.toString(), UserModel.class);
//                if ("1".equals(userModel.getUpdatePolicy())) {
//                    RequestManager.getInstance().getPolicy("0", "5");
//                }
                if (null != callBack) {
                    callBack.onSuccess(response);
                }
            }
        });
    }


    public void initCheck(HttpsCallBack callBack) {
        String url = ServerApi.getApiCheck();
        String json = getSyncDataJson();
        LogUtils.d("url = " + url);
        LogUtils.d("json = " + json);
        OkHttpUtils.getInstance().postJson(url, json, new HttpsCallBack() {
            @Override
            public void onFailure(int errCode, String message) {
                LogUtils.e("errCode = " + errCode);

                LogUtils.e("callBack = " + callBack);
//                Looper.prepare();
//                Toast.makeText(mContext, R.string.init_check_error, Toast.LENGTH_SHORT).show();
//                Looper.loop();
//                Looper.prepare();
//                ToastManager.getInstance().showToast(mContext.getString(R.string.init_check_error));
//                Looper.loop();

//                NoticeManager.getInstance().dismissIfNeed();

                if (null != callBack) {
                    callBack.onFailure(errCode, message);
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onSuccess(JSONObject response) {
                LogUtils.d("response = " + response);
                LogUtils.d("callBack = " + callBack);


                UpdateData(response);

//                Gson gson = new Gson();
//                PolicyModel policyModel = gson.fromJson(response.toString(), PolicyModel.class);
//                DBManager.getInstance().insertPolicyMain(policyModel);
//                List<PolicyDataModel> dataList = policyModel.getData();
//                int size = dataList.size();
//                List<String> dataname = new ArrayList<>();
//                for (int i = 0; i < size; i++) {
//                    PolicyDataModel data = dataList.get(i);
//                    LogUtils.d("getName = " + data.getName());
//                    dataname.add(data.getName());
//                }
//
//
//                for (int j = 0; j < dataname.size(); j++) {
//                    PolicyDataModel data = dataList.get(j);
//                    LogUtils.d("getName = " + data.getName());
//                    if (data.getName().equals(Const.POLICY_INITIAL_CHECK)) {
//                        String packagename = data.getExtend1();
//                        LogUtils.d("packagename = " + packagename);
//                        String codestring = data.getExtend2();
//                        LogUtils.d("codestring = " + codestring);
//
//                        String[] result = codestring.split("#");
//                        String code = result[0];
//                        LogUtils.d("code = " + code);
//
//                        String url = data.getExtend3();
//                        LogUtils.d("url = " + url);
//
//                        int apkcode = AppManager.getInstance().getAppCode(MyApplication.getContext(), packagename);
//                        LogUtils.d("apkcode = " + apkcode);
//
//
//                        if (AppManager.getInstance().appExist(mContext, packagename)) {
//                            ActivityManager.removeAllActivity();
//                            ActivityManager.showDownloadScreen(mContext);
//                        }
//
//
//                        return;
//                    }
//                }
//
//                for (int k = 0; k < dataname.size(); k++) {
//                    PolicyDataModel data = dataList.get(k);
//                    LogUtils.d("getName = " + data.getName());
//                    if (data.getName().equals(Const.POLICY_OCCUPY_SCREEN)) {
//                        if (!AppManager.getInstance().appExist(MyApplication.getContext(), data.getName())) {
//                            LogUtils.d("The app does not exist");
//                            return;
//                        }
//                        String name = data.getExtend1();
//                        LogUtils.d("name = " + name);
//                        if (name != null) {
//                            AppManager.getInstance().startActivity(name);
//                        } else {
//                            AppManager.getInstance().startActivity(PrefManager.getInstance().getOccupyName());
//                        }
//
//
//                    }
//
//                }
                if (null != callBack) {
                    callBack.onSuccess(response);
                }

            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void UpdateData(JSONObject json) {
        LogUtils.d("UpdateData");
        Gson gson = new Gson();
        PolicyModel policyModel = gson.fromJson(json.toString(), PolicyModel.class);
        DBManager.getInstance().insertPolicyMain(policyModel);
        List<PolicyDataModel> dataList = policyModel.getData();
        int size = dataList.size();
        LogUtils.d("size = " + size);

        if (size == 0) {
            PrefManager.getInstance().setUpdateResult(true);
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

                if (AppManager.getInstance().appExist(MyApplication.getContext(), packagename)) {
                    LogUtils.d("AppManager ");
                    LogUtils.d("parseInt " + Integer.parseInt(code));
                    if (Integer.parseInt(code) > apkcode) {
                        ActivityManager.dismissMainScreen(mContext);
                        ActivityManager.removeAllActivity();
                        ActivityManager.showDownloadScreen(mContext);
                        return;
                    } else {
                        LogUtils.d("code < apkcode");
                        break;
                    }
                } else {
                    LogUtils.d("The app does not exist");
                    break;
                }
            }
        }

        for (int k = 0; k < dataname.size(); k++) {
            PolicyDataModel data = dataList.get(k);
            LogUtils.d("getName = " + data.getName());
            if (data.getName().equals(Const.POLICY_OCCUPY_SCREEN)) {
                if (!AppManager.getInstance().appExist(MyApplication.getContext(), data.getExtend1())) {
                    LogUtils.d("The app does not exist and occupyScreen");
//                    ActivityManager.dismissMainScreen(mContext);
                    ActivityManager.removeAllActivity();
                    AppManager.getInstance().startActivity(PrefManager.getInstance().getOccupyName());
                    return;
                }
                String name = data.getExtend1();
                String value = data.getValue();
                LogUtils.d("name = " + name + "; value = " + value);
                if (data.getValue().equals("1")) {
                    PrefManager.getInstance().setOccupyName(name);
                }
                PrefManager.getInstance().setUpdateResult(true);

//                ActivityManager.dismissMainScreen(mContext);
                ActivityManager.removeAllActivity();
                AppManager.getInstance().startActivity(name);
            } else {

//                ActivityManager.dismissMainScreen(mContext);
                ActivityManager.removeAllActivity();
                AppManager.getInstance().startActivity(PrefManager.getInstance().getOccupyName());
            }

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
        PrefUtils.putLong(mContext, "apksize", apksize);

        String md5 = result[2];
        LogUtils.d("md5 = " + md5);
        PrefManager.getInstance().setMd5(md5);

//        DBManager.getInstance().updatePolicyDataResult(version, name);
    }


}
