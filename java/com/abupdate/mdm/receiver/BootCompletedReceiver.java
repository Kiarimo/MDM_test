package com.abupdate.mdm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.manager.ActivityManager;
import com.abupdate.mdm.manager.AppManager;
import com.abupdate.mdm.manager.DeviceManager;
import com.abupdate.mdm.manager.PrefManager;
import com.abupdate.mdm.utils.LogUtils;

public class BootCompletedReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onReceive(Context context, Intent intent) {
        String code = DeviceManager.getInstance().getEnrollCode();
        boolean updateresult = PrefManager.getInstance().getUpdateResult();
        String name = PrefManager.getInstance().getOccupyName();
        LogUtils.d("code = " + code + "; updateresult = " + updateresult + "; name = " + name);
        if (code != null && updateresult) {
            AppManager.getInstance().startActivity(name);
        }else {
            ActivityManager.showMainScreen(MyApplication.getContext());
        }
    }
}
