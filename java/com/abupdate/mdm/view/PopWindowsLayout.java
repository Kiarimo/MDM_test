package com.abupdate.mdm.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.R;
import com.abupdate.mdm.callback.HttpsCallBack;
import com.abupdate.mdm.manager.NoticeManager;
import com.abupdate.mdm.manager.RequestManager;
import com.abupdate.mdm.utils.LogUtils;
import com.abupdate.mdm.utils.ScreenUtils;

import org.json.JSONObject;

import java.util.Locale;


public class PopWindowsLayout {

    private PopupWindow set_menu_win;
    private View popupWindow_view;
    private static final int MSG_DELAY_TIME = 44;
    private int delay_time_counts = 30;
    private AlertDialog dialog;
    private Context mContext;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setPopWinLayout(final Activity activity, View clickView) {
        popupWindow_view = LayoutInflater.from(activity).inflate(R.layout.settings_pop, null);
        mContext = MyApplication.getContext();
        set_menu_win = new PopupWindow(popupWindow_view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        set_menu_win.setFocusable(true);
        set_menu_win.setBackgroundDrawable(new ColorDrawable(0x00000000));

        int mPopRightWidth = ScreenUtils.phoneWidthPixels(activity) / 60;
        int titleHeight = (int) activity.getResources().getDimension(R.dimen.activity_title_height);
        int statusBarHeight = ScreenUtils.getStatusBarHeight();
        int offset = (int) ScreenUtils.dpToPx(activity.getBaseContext(), 8.0f);
        int gravity = Gravity.RIGHT | Gravity.TOP;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (View.LAYOUT_DIRECTION_RTL == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
                gravity = Gravity.LEFT | Gravity.TOP;
            }
        }
        set_menu_win.showAtLocation(clickView, gravity, mPopRightWidth, titleHeight + statusBarHeight - offset);


        LinearLayout pop_setup_wifi = popupWindow_view.findViewById(R.id.menu_setup_wifi);
        LinearLayout pop_refresh = popupWindow_view.findViewById(R.id.menu_refresh);
        LinearLayout pop_manage_app = popupWindow_view.findViewById(R.id.menu_manage_app);
        LinearLayout pop_about = popupWindow_view.findViewById(R.id.menu_about);
        LinearLayout pop_exit = popupWindow_view.findViewById(R.id.menu_exit);

        //设置wifi
        pop_setup_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_menu_win.dismiss();
                set_menu_win = null;
            }
        });

        // 同步数据
        pop_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_menu_win.dismiss();
                set_menu_win = null;
                doSync();
            }
        });

        //manage_app
        pop_manage_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_menu_win.dismiss();
                set_menu_win = null;
            }
        });

        //关于应用
        pop_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_menu_win.dismiss();
                set_menu_win = null;
                showAbout();
            }
        });


        //退出应用
        pop_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set_menu_win.dismiss();
                set_menu_win = null;
//                NoticeManager.getInstance().showInputPasswordDialog(MyApplication.getContext());
                showInputPasswordDialog(mContext);
            }
        });
    }

    private void doSync() {
        RequestManager.getInstance().syncData(new HttpsCallBack() {
            @Override
            public void onFailure(int errCode, String message) {
                LogUtils.e("errCode = " + errCode + "; message = " + message);
            }

            @Override
            public void onSuccess(JSONObject json) {
                LogUtils.e("json = " + json);
            }
        });
    }


    private void showAbout() {
        StringBuilder sb = new StringBuilder();
        sb.append("设备型号：");
        sb.append(Build.MODEL);
        sb.append("\n");

        sb.append("安卓版本：");
        sb.append(Build.VERSION.RELEASE);
        sb.append("\n");

        sb.append("系统版本：");
        sb.append(Build.DISPLAY);
        sb.append("\n");

        sb.append("APP版本：");
        try {
            sb.append(MyApplication.getContext().getPackageManager().getPackageInfo(MyApplication.getContext().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        sb.append("\n");

        sb.append("设备  SN：");
        sb.append(Build.getSerial());
        sb.append("\n");

        NoticeManager.getInstance().showDialog(MyApplication.getContext(), null, sb.toString(), false);
    }


    public void showInputPasswordDialog(Context context) {
        delay_time_counts = 30;
        String title = context.getString(R.string.menu_password_required) + "(" + delay_time_counts + "S)";

        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setTitle(title);
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_exit_password, null);
        final EditText et_password = view.findViewById(R.id.input_password);
        builder.setView(view);
        builder.setCancelable(false);//设置为不可取消
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String pass = et_password.getText().toString().trim();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        dialog = builder.create();
        dialog.show();//显示Dialog对话框
        delay_time_counts--;
        mHandler.sendEmptyMessageDelayed(MSG_DELAY_TIME, 1000);

//        mDialog = new MaterialDialog.Builder(context)
//                .title(title)
//                .titleGravity(Gravity.CENTER)
//                .customView(R.layout.dialog_exit_password, false)
//                .positiveText("Exit")
//                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        String text = inputtext.getText().toString();
//                        if (comparePassword(text)) {
//                            dialog.dismiss();
//                            mHandler.removeCallbacksAndMessages(null);
//                            ActivityManager.dismissKioskScreen(context);
//                        } else {
//                            Toast.makeText(context, R.string.password_error, Toast.LENGTH_LONG).show();
//                            return;
//                        }
//                    }
//                })
//                .negativeText("Cancel")
//                .onNegative(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        mHandler.removeCallbacksAndMessages(null);
//                        dialog.dismiss();
//                    }
//                })
//                .cancelable(false)
//                .build();

//        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)
//                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                            Activity activity = ActivityManager.getActivity(context.getClass().getName());
//                            LogUtils.d("activity = " + activity);
//                            if (activity != null) {
//                                activity.finish();
//                            }
//                    }
//                });
//
//        builder.setCancelable(false);
//        dialog = builder.create();
//        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        dialog.show();
//
//
//        EditText inputtext = mDialog.findViewById(R.id.input_password);
//        inputtext.requestFocus();
//        String text = inputtext.getText().toString();


//        mDialog.show();


    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            int what = msg.what;
            LogUtils.d("what = " + what);

            switch (what) {
                case MSG_DELAY_TIME:
                    if(dialog!=null){
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

}
