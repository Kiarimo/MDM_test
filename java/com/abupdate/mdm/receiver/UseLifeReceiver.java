package com.abupdate.mdm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.abupdate.mdm.service.UseLifeService;
import com.abupdate.mdm.utils.LogUtils;

public class UseLifeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("");
        UseLifeService.startAction(context, intent);
    }
}
