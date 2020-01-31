package com.abupdate.mdm.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;

import com.abupdate.mdm.R;
import com.abupdate.mdm.utils.LogUtils;

public class LockActivity extends BaseActivity implements View.OnClickListener  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        findViewById(R.id.btn_wifi).setOnClickListener(this);
        findViewById(R.id.btn_data).setOnClickListener(this);
        findViewById(R.id.btn_emergency).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // start lock task mode if it's not already active
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        // ActivityManager.getLockTaskModeState api is not available in pre-M.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (!am.isInLockTaskMode()) {
                startLockTask();
            }
        } else {
            if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
                startLockTask();
            }
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean value = intent.getBooleanExtra("dismiss", false);
        LogUtils.e("value = " + value);
        if (value) {
            stopLockTask();
            finish();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        LogUtils.d("keyCode = " + keyCode);
        return false;//super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        stopLockTask();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_wifi:
                Intent intent_wifi = new Intent(Settings.ACTION_WIFI_SETTINGS);
                intent_wifi.putExtra("mdm",true);
                startActivity(intent_wifi); //直接进入手机中的wifi网络设置界面
                break;

            case R.id.btn_data:
                startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)); //直接进入手机中的wifi网络设置界面
                break;

            case R.id.btn_emergency:
                ComponentName comp = new ComponentName("com.android.phone",
                        "com.android.phone.EmergencyDialer");
                Intent intent = new Intent();
                intent.setComponent(comp);
                startActivity(intent);
                break;
        }
    }
}
