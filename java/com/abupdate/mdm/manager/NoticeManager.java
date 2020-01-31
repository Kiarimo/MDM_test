package com.abupdate.mdm.manager;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.R;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.model.NoticeModel;
import com.abupdate.mdm.utils.LogUtils;
import com.system.api.NotificationApi;

/*
 * @date   : 2019/09/27
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class NoticeManager {
    private static NoticeManager mNoticeManager = null;
    private static Context mContext = null;
    private AlertDialog dialog;

    private ProgressDialog progressdialog;

    private static final int MSG_DELAY_TIME = 44;
    private int delay_time_counts = 30;

    public NoticeManager() {
        mContext = MyApplication.getContext();
    }

    public static NoticeManager getInstance() {
        if (mNoticeManager == null) {
            mNoticeManager = new NoticeManager();
        }
        return mNoticeManager;
    }

    public void showNotice(NoticeModel notice) {
        Looper.prepare();
        showDialog(mContext, notice.getTitle(), notice.getMsg(), false);
        Looper.loop();
    }

    public void showDialog(Context context, String title, String content, boolean finish) {
        if (TextUtils.isEmpty(content)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setMessage(content).setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (finish) {
                            Activity activity = ActivityManager.getActivity(context.getClass().getName());
                            LogUtils.d("activity = " + activity);
                            if (activity != null) {
                                activity.finish();
                            }
                        }
                    }
                });

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        builder.setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }


    public void showInfoDialog(Context context, String title, String content) {
        if (TextUtils.isEmpty(content)) {
            return;
        }


        View view = LayoutInflater.from(context).inflate(R.layout.dialog_debug_info, null);
        Button reset = view.findViewById(R.id.button_reset_code);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setMessage(content)
                .setView(view)
                .setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeviceManager.getInstance().resetEnrollCode();
//                Toast.makeText(context, R.string.reset_device_info_success, Toast.LENGTH_SHORT).show();
                ToastManager.getInstance().showToast(mContext.getString(R.string.reset_device_info_success));
            }
        });


        builder.setCancelable(false);
        dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }


    public void showLoadingDialog(Context context, String message) {
        LogUtils.e("showLoadingDialog");
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        progressdialog = new ProgressDialog(context);
        progressdialog.setMessage(message);
        progressdialog.setCancelable(false);
        progressdialog.show();
    }

    public void dismissIfNeed() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        if (progressdialog != null && progressdialog.isShowing()) {
            progressdialog.dismiss();
        }

    }

    public void setBlockNotice(String pkgName, String value) {

        NotificationApi.getInstance().setNotificationsDisabled(mContext, pkgName, isEnable(value));
    }

    public boolean isEnable(String value) {
        return "0".equals(value) ? false : true;
    }


    public void showInputPasswordDialog(Context context) {
        delay_time_counts = 30;
        String title = context.getString(R.string.menu_password_required) + "(" + delay_time_counts + "S)";


        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setTitle(title)
                .setView(R.layout.dialog_exit_password)
                .setPositiveButton(R.string.dialog_ok, null)
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHandler.removeCallbacksAndMessages(null);
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();
        final EditText inputtext = dialog.findViewById(R.id.input_password);
        inputtext.requestFocus();
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = inputtext.getText().toString();
                if (comparePassword(text)) {
                    ActivityManager.dismissKioskScreen(mContext);
                    mHandler.removeCallbacksAndMessages(null);
                    dialog.dismiss();
                } else {
                    ToastManager.getInstance().showToast(mContext.getString(R.string.password_error));
                    return;
                }
            }
        });


        delay_time_counts--;
        mHandler.sendEmptyMessageDelayed(MSG_DELAY_TIME, 1000);
    }


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            int what = msg.what;
            LogUtils.d("what = " + what);

            switch (what) {
                case MSG_DELAY_TIME:
                    if (dialog != null) {
                        if (delay_time_counts > 0) {
                            String title = mContext.getString(R.string.menu_password_required) + "(" + delay_time_counts + "S)";
                            dialog.setTitle(title);
                            delay_time_counts--;
                            mHandler.sendEmptyMessageDelayed(MSG_DELAY_TIME, 1000);
                        } else {
                            dialog.dismiss();
                        }
                    }
                    break;
            }

        }
    };

    private boolean comparePassword(String input) {
        String exitPassword = PrefManager.getInstance().getExitPassword();
        LogUtils.d("exitPassword = " + exitPassword);
        if (!TextUtils.isEmpty(input) && input.equals(exitPassword) && !exitPassword.equals(Const.UNKNOW_STRING)) {
            return true;
        }
        return false;
    }


}
