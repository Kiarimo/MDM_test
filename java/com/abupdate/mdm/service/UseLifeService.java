package com.abupdate.mdm.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.manager.UseLifeManager;
import com.abupdate.mdm.utils.LogUtils;

public class UseLifeService extends IntentService {

    public UseLifeService() {
        super("UseLifeService");
    }

    public static void startAction(Context context, Intent intent) {
        intent.setClass(context, UseLifeService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            int alarmId = intent.getIntExtra(Const.ALARM_ID, Integer.MAX_VALUE);
            LogUtils.d("alarmId = " + alarmId + "; action = " + action);
            if (Const.ACTION_ALARM_RECEIVER.equals(action)) {
                UseLifeManager.getInstance().setUseLife(false);
            }
        }
    }

}
