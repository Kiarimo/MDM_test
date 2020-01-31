package com.abupdate.mdm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.manager.DeviceManager;
import com.abupdate.mdm.manager.PushManager;
import com.abupdate.mdm.manager.RegisterManager;
import com.abupdate.mdm.manager.ReportManager;
import com.abupdate.mdm.utils.LogUtils;

public class NetChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("NetChangeReceiver");
        String code = DeviceManager.getInstance().getEnrollCode();
        LogUtils.d("code = " + code);
        if (DeviceManager.getInstance().isNetworkAvailable()) {
            ReportManager.getInstance().doReport();
//            if (code == null) {
//                RegisterManager.getInstance().doRegister(Const.EMM_REGISTER_TYPE_SYNC + "", null, null, null);
//            }
            PushManager.getInstance().HPushRegister();
        }
    }
}
