package com.abupdate.mdm.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.abupdate.mdm.manager.ActivityManager;

/*
 * @date   : 2019/09/26
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        LogUtils.d(getClass().getName());
        ActivityManager.addActivity(this, getClass().getName());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
//        LogUtils.d(getClass().getName());
        ActivityManager.removeActivity(getClass().getName());
        super.onDestroy();
    }
}
