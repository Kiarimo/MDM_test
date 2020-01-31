package com.abupdate.mdm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.abupdate.mdm.callback.HttpsCallBack;
import com.abupdate.mdm.manager.RequestManager;
import com.abupdate.mdm.model.UserModel;
import com.abupdate.mdm.utils.AlarmUtils;
import com.abupdate.mdm.utils.LogUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.d("action = " + action);
        if(action.equals(AlarmUtils.SEND_CUSTOM_SERVICE_ACTION)){
            RequestManager.getInstance().syncData(new HttpsCallBack() {
                @Override
                public void onFailure(int errCode, String message) {
                    LogUtils.e("errCode = " + errCode + "; message = " + message);
                }

                @Override
                public void onSuccess(JSONObject json) {
                    LogUtils.e("json = " + json);
                    Gson gson = new Gson();
                    UserModel userModel = gson.fromJson(json.toString(), UserModel.class);
                    if ("1".equals(userModel.getUpdatePolicy())) {
                        RequestManager.getInstance().getPolicy("0", "5");
                    }
                }
            });
        }

    }
}
