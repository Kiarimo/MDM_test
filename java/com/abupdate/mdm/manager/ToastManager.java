package com.abupdate.mdm.manager;

import android.content.Context;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.R;

public class ToastManager {
    private static ToastManager mToastManager = null;
    private static Context mContext = null;

    public ToastManager() {
        mContext = MyApplication.getContext();
    }

    public static ToastManager getInstance() {
        if (mToastManager == null) {
            mToastManager = new ToastManager();
        }
        return mToastManager;
    }


    public void showToast(String content) {
//        Looper.prepare();
        Toast.makeText(mContext, content, Toast.LENGTH_SHORT).show();
//        Looper.loop();
    }
}
