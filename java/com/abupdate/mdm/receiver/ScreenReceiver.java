package com.abupdate.mdm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.abupdate.mdm.manager.ApplyPolicyManager;
import com.abupdate.mdm.utils.LogUtils;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.d(action);
        if (Intent.ACTION_SCREEN_ON.equals(action)) {
//            ApplyPolicyManager.getInstance().doSomethingIfNeed();
        }
    }
}
