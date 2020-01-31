package com.abupdate.mdm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.abupdate.mdm.utils.LogUtils;

public class DataChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("action = " + intent.getAction());
//        ApplyPolicyManager.getInstance().doSomethingIfNeed();
    }
}
