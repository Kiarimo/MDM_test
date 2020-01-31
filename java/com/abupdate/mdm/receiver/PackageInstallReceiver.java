package com.abupdate.mdm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.abupdate.mdm.manager.AppManager;
import com.abupdate.mdm.manager.PrefManager;
import com.abupdate.mdm.utils.LogUtils;

public class PackageInstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        LogUtils.d("action = " + action);

        String packageName = intent.getData().getSchemeSpecificPart();
        String updatename = PrefManager.getInstance().getUpdateApkName();
        LogUtils.d("packageName = " + packageName + "; updatename = " + updatename);



        if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            PrefManager.getInstance().setUpdateResult(true);
            if (packageName.equals(updatename)) {
                AppManager.getInstance().startActivity(packageName);
            }
        }


    }
}
