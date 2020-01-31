package com.abupdate.mdm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.abupdate.mdm.activity.MainActivity;

/*
 * @date   : 2019/09/18
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class SecretCodeReceiver extends BroadcastReceiver {
    private static final String SHOW_CHANNEL_ACTION = "android.provider.Telephony.SECRET_CODE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //LogUtil.d("onReceive: " + action);
        if (SHOW_CHANNEL_ACTION.equalsIgnoreCase(action)) {
            String dataString = intent.getDataString();
            if ("android_secret_code://372".equalsIgnoreCase(dataString)) {
                Intent intent2 = new Intent(context, MainActivity.class);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent2);
            }
        }
    }
}
