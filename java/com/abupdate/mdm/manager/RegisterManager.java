package com.abupdate.mdm.manager;

import android.os.Looper;
import android.text.TextUtils;

import com.abupdate.mdm.R;
import com.abupdate.mdm.callback.HttpsCallBack;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.config.ServerApi;
import com.abupdate.mdm.model.RegisterModel;
import com.abupdate.mdm.utils.LogUtils;
import com.abupdate.mdm.utils.OkHttpUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

/*
 * @date   : 2019/09/26
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class RegisterManager {
    private static RegisterManager mCheckManager = null;
    private static boolean mRegistering = false;

    public RegisterManager() {
        mRegistering = false;
    }

    public static RegisterManager getInstance() {
        if (mCheckManager == null) {
            mCheckManager = new RegisterManager();
        }
        return mCheckManager;
    }

    public void doRegister(final String type, final String code, final String name, final HttpsCallBack callBack) {
//        if (mRegistering) {
//            LogUtils.d("Last register was not completed!");
//            Toast.makeText(MyApplication.getContext(), "Last register was not completed!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        mRegistering = true;

        String content = getContent(type, code, name);
        String url = ServerApi.getApiEnroll();
        LogUtils.d("doRegister url = " + url);
        LogUtils.d("doRegister content = " + content);
        OkHttpUtils.getInstance().postJson(url, content, new HttpsCallBack() {
            @Override
            public void onFailure(int errCode, String message) {
                if (callBack != null) {
                    callBack.onFailure(errCode, message);
                }

//                mRegistering = false;
                if (message == null || TextUtils.isEmpty(message)) {
                    message = String.valueOf(R.string.register_fail);
                }
                LogUtils.e("errCode = " + errCode + "; message = " + message);

                Looper.prepare();
                ToastManager.getInstance().showToast(message);
                Looper.loop();
            }

            @Override
            public void onSuccess(JSONObject json) {
                LogUtils.d("json = " + json);
//                mRegistering = false;
                if (json.has(Const.RESPONSE_MSG)) {
                    String returnCode = json.optString(Const.RESPONSE_MSG);
                    LogUtils.d("returnCode = " + returnCode);
                    if (!returnCode.equals("{}")) {
                        String[] msg = returnCode.split("#");
                        String password = msg[1];
                        String EnrollCode = msg[0];
                        LogUtils.d("password = " + password);
                        LogUtils.d("enrollCode = " + EnrollCode);
                        PrefManager.getInstance().setExitPassword(password);
                        DeviceManager.getInstance().setEnrollCode(EnrollCode);
                    }

                } else {
                    DeviceManager.getInstance().setEnrollCode(code);
                }

                if (callBack != null) {
                    callBack.onSuccess(json);
                }

                RequestManager.getInstance().initCheck(null);

            }
        });
    }

    private String getContent(String type, String code, String name) {
        RegisterModel registerModel = new RegisterModel();
        registerModel.setSn(DeviceManager.getInstance().getSN());
        registerModel.setImei1(DeviceManager.getInstance().getImei(Const.SLOT_ID_1));
        registerModel.setImei2(DeviceManager.getInstance().getImei(Const.SLOT_ID_2));
        registerModel.setMac(DeviceManager.getInstance().getMAC());
        registerModel.setBrand(DeviceManager.getInstance().getBrand());
        registerModel.setModel(DeviceManager.getInstance().getModel());
        registerModel.setSdkLevel(DeviceManager.getInstance().getSdkLevel());
        registerModel.setOsVersion(DeviceManager.getInstance().getOSVersion());
        registerModel.setChipset(DeviceManager.getInstance().getPlatform());
        registerModel.setAppVersion(DeviceManager.getInstance().getAppVersion());
        registerModel.setAppCode(DeviceManager.getInstance().getAppCode());

        registerModel.setNetworkType(DeviceManager.getInstance().getConnectedType());
        registerModel.setScrsize(DeviceManager.getInstance().getScreenSize());
        registerModel.setStospace(DeviceManager.getInstance().getTotalInternalMemorySize());
        registerModel.setFingerprint(DeviceManager.getInstance().getSystemProperties(Const.RO_PRODUCT_BUILD_FINGERPRINT));
        registerModel.setFlavor(DeviceManager.getInstance().getSystemProperties(Const.RO_BUILD_FLAVOR));
        registerModel.setProduct(DeviceManager.getInstance().getSystemProperties(Const.RO_BUILD_PRODUCT));
        registerModel.setCompileType(DeviceManager.getInstance().getSystemProperties(Const.RO_BUILD_TYPE));
        registerModel.setHardware(DeviceManager.getInstance().getSystemProperties(Const.RO_HARDWARE));
        registerModel.setHardwareType(DeviceManager.getInstance().getSystemProperties(Const.RO_PRODUCT_HARDWARE));
        registerModel.setCpu(DeviceManager.getInstance().getSystemProperties(Const.RO_PRODUCT_CPU_ABI));
        registerModel.setLocale(DeviceManager.getInstance().getSystemProperties(Const.RO_PRODUCT_LOCALE));
        registerModel.setManufacturer(DeviceManager.getInstance().getSystemProperties(Const.RO_PRODUCT_MANUFACTURER));
        registerModel.setRamsize(DeviceManager.getInstance().getSystemProperties(Const.RO_RAMSIZE));

        registerModel.setEnrollType(type);
        registerModel.setEnrollCode(code);
        registerModel.setName(name);

        Gson gson = new Gson();
        String json = gson.toJson(registerModel);

        return json;
    }

}
