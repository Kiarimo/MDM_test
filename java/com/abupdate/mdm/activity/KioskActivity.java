package com.abupdate.mdm.activity;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.R;
import com.abupdate.mdm.callback.HttpsCallBack;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.manager.ApplyPolicyManager;
import com.abupdate.mdm.manager.DeviceManager;
import com.abupdate.mdm.manager.NoticeManager;
import com.abupdate.mdm.manager.PrefManager;
import com.abupdate.mdm.manager.RequestManager;
import com.abupdate.mdm.manager.ToastManager;
import com.abupdate.mdm.model.UserModel;
import com.abupdate.mdm.receiver.DeviceOwnerReceiver;
import com.abupdate.mdm.utils.LogUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class KioskActivity extends BaseActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private ComponentName mAdminComponentName;
    private DevicePolicyManager mDevicePolicyManager;
    private PackageManager mPackageManager;
    private TextView mStatusBarTime, mStatusBarName, mStatusBarGroup;
    private ImageView mStatusBarWifi, mBtnmenu;
    private GridView mGridView;
    private List<Drawable> listIcon;
    private List<String> listName;
    private ArrayList<String> mKioskPackages;
    private ArrayList<String> nameArrayList;
    String[] pkgname;

    View getlistview;
    AlertDialog.Builder builder;

    private ManageAppAdapter adapter;


    public static KioskActivity mKioskactivity = null;

    //    Boolean[] bl = {false, false, false, false, false, false, false, false};
    private List<Drawable> allIcon;
    private List<String> allName;
    private List<String> allPackageAppName;
    private static final String[] KIOSK_USER_RESTRICTIONS = {
            UserManager.DISALLOW_SAFE_BOOT,
            UserManager.DISALLOW_FACTORY_RESET,
            UserManager.DISALLOW_ADD_USER,
            UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,
            UserManager.DISALLOW_ADJUST_VOLUME};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.e("");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kiosk);
        mKioskactivity = this;
        mAdminComponentName = DeviceOwnerReceiver.getComponentName(getApplicationContext());
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mPackageManager = getPackageManager();
        queryFilterAppInfo();
        setDefaultKioskPolicies(true);
        findViewById(R.id.btn_menu).setOnClickListener(this);
        initView();
        nameArrayList = new ArrayList<>();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean value = intent.getBooleanExtra("dismiss", false);
        LogUtils.e("value = " + value);
        if (value) {
            mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, false);
            com.abupdate.mdm.manager.ActivityManager.removeAllActivity();
            onBackdoorClicked();
        }


    }

    private void initView() {
        mStatusBarTime = findViewById(R.id.tv_datetime);
        mStatusBarWifi = findViewById(R.id.ic_wifi);
        mGridView = findViewById(R.id.gridView);
        mBtnmenu = findViewById(R.id.btn_menu);
        mStatusBarName = findViewById(R.id.tv_appname);
        mStatusBarGroup = findViewById(R.id.tv_group);
    }


    @Override
    protected void onStart() {
        super.onStart();

        PrefManager.getInstance().setInKiosk(true);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtils.d("keyCode = " + keyCode);
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        LogUtils.d("keyCode = " + keyCode);
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.e("");
        registerBroadcast();
        updateTime();
        updateWifi();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.e("");
        unregisterReceiver(broadcastReceiver);
    }

    public void onBackdoorClicked() {
        stopLockTask();

        setDefaultKioskPolicies(false);
        mDevicePolicyManager.clearPackagePersistentPreferredActivities(mAdminComponentName,
                getPackageName());

        ComponentName oldLauncher = new ComponentName("com.android.launcher3", "com.android.launcher3.Launcher");
        mPackageManager.setComponentEnabledSetting(
                oldLauncher,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        mDevicePolicyManager.addPersistentPreferredActivity(mAdminComponentName, getHomeIntentFilter(), oldLauncher);

        mPackageManager.setComponentEnabledSetting(
                new ComponentName(getPackageName(), getClass().getName()),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        com.abupdate.mdm.manager.ActivityManager.removeAllActivity();
        finish();
    }


    private void setUserRestriction(String restriction, boolean disallow) {
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction);
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction);
        }
    }

    private void setDefaultKioskPolicies(boolean active) {
        if (active) {
            saveCurrentUserRestricitions();
        }
        if (allPackageAppName != null) {
            mKioskPackages = new ArrayList<>();
            for (String pkg : allPackageAppName) {
                mKioskPackages.add(pkg);
            }
            mKioskPackages.remove(getPackageName());
            mKioskPackages.add(getPackageName());
        }
        setUserRestricitions(!active);
        // set lock task packages
        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, active ? mKioskPackages.toArray(new String[]{}) : new String[]{});

    }


    public void setOccupyScreenPolicies(boolean active, String name) {
        if (active) {
            saveCurrentUserRestricitions();
        }
        mKioskPackages = new ArrayList<>();
        mKioskPackages.add(name);
        mKioskPackages.remove(getPackageName());
        mKioskPackages.add(name);
        setUserRestricitions(!active);
        // set lock task packages
        LogUtils.d("name = " + mKioskPackages.toString());
        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName, mKioskPackages.toArray(new String[]{name}));

    }


    private void saveCurrentUserRestricitions() {
        Bundle settingsBundle = mDevicePolicyManager.getUserRestrictions(mAdminComponentName);
        for (String userRestriction : KIOSK_USER_RESTRICTIONS) {
            boolean currentSettingValue = settingsBundle.getBoolean(userRestriction);
            PrefManager.getInstance().putBoolean(userRestriction, currentSettingValue);
        }
    }

    private void setUserRestricitions(boolean restore) {
        for (String userRestriction : KIOSK_USER_RESTRICTIONS) {
            boolean value = true;
            if (restore) {
                value = PrefManager.getInstance().getBoolean(userRestriction);
            }
            setUserRestriction(userRestriction, value);
        }
    }

    private IntentFilter getHomeIntentFilter() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        return filter;
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(broadcastReceiver, filter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d(action);
            if (Intent.ACTION_TIME_TICK.equals(action)
                    || Intent.ACTION_TIME_CHANGED.equals(action)) {
                updateTime();//每一分钟更新时间
                updateWifi();
            }
        }
    };


    private void updateTime() {
        String regex = "MM-dd EE";
        if (Locale.getDefault().getLanguage().equals("zh")) {
            regex = "MM月dd日 EE";
        }

        if (DateFormat.is24HourFormat(this)) {
            regex = regex + " HH:mm";
        } else {
            regex = regex + " hh:mm aa";
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(regex);// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        mStatusBarTime.setText(simpleDateFormat.format(date));
    }


    private boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }


    private void updateWifi() {
        if (isWifiConnect()) {
            WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            int wifi = mWifiInfo.getRssi();//获取wifi信号强度
            LogUtils.d("wifi level = " + wifi);
            int resId;
            if (wifi > -50 && wifi < 0) {//最强
                resId = R.drawable.ic_signal_wifi_4_bar_black_24dp;
            } else if (wifi > -70 && wifi < -50) {//较强
                resId = R.drawable.ic_signal_wifi_3_bar_black_24dp;
            } else if (wifi > -80 && wifi < -70) {//较弱
                resId = R.drawable.ic_signal_wifi_2_bar_black_24dp;
            } else if (wifi > -100 && wifi < -80) {//微弱
                resId = R.drawable.ic_signal_wifi_1_bar_black_24dp;
            } else {
                resId = R.drawable.ic_signal_wifi_0_bar_black_24dp;
            }
            mStatusBarWifi.setImageResource(resId);
            mStatusBarWifi.setVisibility(View.VISIBLE);
        } else {
            mStatusBarWifi.setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_menu:
                //创建弹出式菜单对象（最低版本11）
                PopupMenu popup = new PopupMenu(getApplicationContext(), v);//第二个参数是绑定的那个view
                //获取菜单填充器
                MenuInflater inflater = popup.getMenuInflater();
                //填充菜单
                inflater.inflate(R.menu.kiosk_menu, popup.getMenu());
                //绑定菜单项的点击事件
                popup.setOnMenuItemClickListener(this);
                //显示(这一行代码不要忘记了)
                popup.show();
                break;
            default:
        }

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setup_wifi:
//                com.abupdate.mdm.manager.ActivityManager.showLockScreen(this);

                Intent intent_wifi = new Intent(Settings.ACTION_WIFI_SETTINGS);
                intent_wifi.putExtra("mdm", true);
                startActivity(intent_wifi); //直接进入手机中的wifi网络设置界面
                break;
            case R.id.menu_sync_data:
                LogUtils.d("click menu refresh!!");
                PrefManager.getInstance().setLastConnectTime(System.currentTimeMillis());
                doSync();
                break;
            case R.id.menu_manage_app:
                CreateDialog();
                break;

            case R.id.menu_about:
                LogUtils.d("click menu about!!");
                showAbout();
                break;
            case R.id.menu_exit:
                LogUtils.d("click menu exit!!");
                NoticeManager.getInstance().showInputPasswordDialog(this);
//                com.abupdate.mdm.manager.ActivityManager.dismissKioskScreen(this);
//                onBackdoorClicked();
//                setOccupyScreenPolicies(true,"com.adups.fota");
//                ApplyPolicyManager.getInstance().doOccupyScreen("com.adups.fota");

                break;
            default:
                break;
        }
        return false;
    }


    public void queryFilterAppInfo() {
        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<String> appNameList = new ArrayList<>();
        List<String> appName = new ArrayList<>();
        List<Drawable> appIconList = new ArrayList<>();

        // 通过getPackageManager()的queryIntentActivities方法遍历,得到所有能打开的app的packageName
        List<ResolveInfo> resolveinfoList = mPackageManager.queryIntentActivities(resolveIntent, 0);
        Set<String> allowPackages = new HashSet();
        for (ResolveInfo resolveInfo : resolveinfoList) {
            allowPackages.add(resolveInfo.activityInfo.packageName);
        }

        // 查询所有已经安装的应用程序,GET_UNINSTALLED_PACKAGES代表已删除，但还有安装目录的
        List<ApplicationInfo> applicationInfos = mPackageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo info : applicationInfos) {
            if (allowPackages.contains(info.packageName)) {

                //获取应用的名称
                String app_name = info.loadLabel(mPackageManager).toString();
                Drawable app_icon = info.loadIcon(mPackageManager);
                //获取应用的包名
                String packageName = info.packageName;
                appIconList.add(app_icon);
                appNameList.add(app_name);
                appName.add(packageName);
            }
        }
        allName = appNameList;
        allIcon = appIconList;
        allPackageAppName = appName;
        LogUtils.e("appName = " + appName);
    }


    public void CreateDialog() {

        // 动态加载一个listview的布局文件进来
        LayoutInflater inflater = LayoutInflater.from(this);
        getlistview = inflater.inflate(R.layout.activity_manage_app, null);
        nameArrayList.clear();
        // 给ListView绑定内容
        ListView listview = getlistview.findViewById(R.id.apps_list);
        // 给listview加入适配器
        adapter = new ManageAppAdapter();
        listview.setAdapter(adapter);
        listview.setItemsCanFocus(false);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listview.setOnItemClickListener(new ItemOnClick());

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Your Applications");
        //设置加载的listview
        builder.setView(getlistview);
        builder.setPositiveButton(R.string.btn_ok, new DialogOnClick());
        builder.setCancelable(false);
        builder.create().show();


    }

    public class ManageAppAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return allIcon.size();
        }

        @Override
        public Object getItem(int i) {
            return allIcon.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = null;

//            if (view == null) {
            viewHolder = new ViewHolder();
            LayoutInflater mInflater = LayoutInflater.from(getApplicationContext());
            view = mInflater.inflate(R.layout.manage_app_item, null);

            viewHolder.appIconImg = view.findViewById(R.id.item_icon);
            viewHolder.appNameText = view.findViewById(R.id.item_text);

            viewHolder.appIconImg.setBackground(allIcon.get(i));
            viewHolder.appNameText.setText(allName.get(i));
            view.setTag(viewHolder);
//            }

            return view;
        }
    }


    //重写simpleadapterd的getview方法
//    class SetSimpleAdapter extends SimpleAdapter {
//
//        public SetSimpleAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from,
//                                int[] to) {
//            super(context, data, resource, from, to);
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = LinearLayout.inflate(getBaseContext(), R.layout.manage_app_item, null);
//            }
//            CheckBox ckBox = (CheckBox) convertView.findViewById(R.id.item_checkbox);
    //每次都根据 bl[]来更新checkbox
//            if (bl[position] == true) {
//                ckBox.setChecked(true);
//            } else if (bl[position] == false) {
//                ckBox.setChecked(false);
//            }
//            return super.getView(position, convertView, parent);
//        }
//    }


    class ItemOnClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
            CheckBox cBox = view.findViewById(R.id.item_checkbox);
            String[] name;
            LogUtils.d("position = " + position);

            if (!cBox.isChecked()) {
                LogUtils.d("选中 " + allName.get(position));
                nameArrayList.add(allPackageAppName.get(position));
                cBox.setChecked(true);
            } else {
                LogUtils.d("取消 " + allName.get(position));
                nameArrayList.remove(allPackageAppName.get(position));
                cBox.setChecked(false);
            }

//            if (position == 0 && (cBox.isChecked())) {
//                //如果是选中 全选  就把所有的都选上 然后更新
//                for (int i = 0; i < bl.length; i++) {
//                    bl[i] = true;
//                }
//                adapter.notifyDataSetChanged();
//            } else if (position == 0 && (!cBox.isChecked())) {
//                //如果是取消全选 就把所有的都取消 然后更新
//                for (int i = 0; i < bl.length; i++) {
//                    bl[i] = false;
//                }
//                adapter.notifyDataSetChanged();
//            }


//            if (position != 0 && (!cBox.isChecked())) {
//                // 如果把其它的选项取消   把全选取消
//                bl[0] = false;
//                bl[position] = false;
//                adapter.notifyDataSetChanged();
//            } else if (position != 0 && (cBox.isChecked())) {
//                //如果选择其它的选项，看是否全部选择
//                //先把该选项选中 设置为true
//                bl[position] = true;
//                int a = 0;
//                for (int i = 1; i < bl.length; i++) {
//                    if (bl[i] == false) {
//                        //如果有一个没选中  就不是全选 直接跳出循环
//                        break;
//                    } else {
//                        //计算有多少个选中的
//                        a++;


//                        if (a == bl.length - 1) {
//                            //如果选项都选中，就把全选 选中，然后更新
//                            bl[0] = true;
//                            adapter.notifyDataSetChanged();
//                        }


//                    }
//                }
//            }


        }

    }


    class DialogOnClick implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    //确定按钮的事件
                    if (nameArrayList.size() != 0) {
                        updateGridview();
                        setDefaultKioskPolicies(true);
                    }
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    //取消按钮的事件
                    break;
                default:
                    break;
            }
        }
    }

    private void doSync() {
        RequestManager.getInstance().syncData(new HttpsCallBack() {
            @Override
            public void onFailure(int errCode, String message) {
//                ToastManager.getInstance().showSynoToast(false, getString(R.string.sync_data_fail));
                LogUtils.e("errCode = " + errCode + "; message = " + message);
            }

            @Override
            public void onSuccess(JSONObject json) {
                LogUtils.e("json = " + json);
                updateInfor();
//                ToastManager.getInstance().showSynoToast(true, getString(R.string.sync_data_success));
            }
        });
    }

    private void updateInfor() {
        String userInfor = PrefManager.getInstance().getUserInformation();
        if (Const.UNKNOW_STRING.equals(userInfor)) {
            doSync();
            return;
        }

        Gson gson = new Gson();
        UserModel userModel = gson.fromJson(userInfor, UserModel.class);
        if (null == userModel) {
            doSync();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (userModel.getGroup() != null) {
                        String password = userModel.getExitPwd();
                        String group = userModel.getGroup();
                        LogUtils.e("password = " + password + "; group = " + group);
                        PrefManager.getInstance().setExitPassword(password);
                        if (group != null) {
                            mStatusBarGroup.setVisibility(View.VISIBLE);
                            mStatusBarGroup.setText(userModel.getGroup());
                        }

                    }
                }
            });
        }
    }


    private void showAbout() {

        long lastConnectTime = PrefManager.getInstance().getLastConnectTime();
        LogUtils.d("lastConnectTime = " + lastConnectTime);
        if (lastConnectTime == 0) {
            lastConnectTime = System.currentTimeMillis();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Equipment model：");
        sb.append(Build.MODEL);
        sb.append("\n");

        sb.append("Android version：");
        sb.append(Build.VERSION.RELEASE);
        sb.append("\n");

        sb.append("System version：");
        sb.append(Build.DISPLAY);
        sb.append("\n");

        sb.append("APP version：");
        try {
            sb.append(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        sb.append("\n");

        sb.append("Device  SN：");
        sb.append(Build.getSerial());
        sb.append("\n");


        sb.append("Last login IP Address：");
        sb.append(getWifiIP());
        sb.append("\n");


        sb.append("Last Connection Time：");
        sb.append(SimpleDateFormat.getDateTimeInstance().format(lastConnectTime));
        sb.append("\n");


        NoticeManager.getInstance().showDialog(this, null, sb.toString(), false);
    }


    public String getWifiIP() {
        String ip = "";
        ConnectivityManager conMann = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = conMann.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            ip = intToIp(ipAddress);
        }
        return ip;
    }


    public static String intToIp(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }


    private List<Drawable> getImage() {
        PackageManager packageManager = getPackageManager();
        List<Drawable> mList = new ArrayList<>();
        for (String name : pkgname) {
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(name, PackageManager.GET_META_DATA);
                Drawable icon = getPackageManager().getApplicationIcon(appInfo);
                mList.add(icon);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }


        }
        return mList;
    }

    private List<String> getName() {
        PackageManager packageManager = getPackageManager();
        List<String> mList = new ArrayList<>();
        for (String name : pkgname) {
            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(name, PackageManager.GET_META_DATA);
                String lable = (String) getPackageManager().getApplicationLabel(appInfo);
                mList.add(lable);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return mList;
    }


    public void initData() {
        pkgname = nameArrayList.toArray(new String[nameArrayList.size()]);
        listIcon = getImage();
        listName = getName();
        mGridView.setAdapter(new KioskAdapter());
        mGridView.setSelection(0);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LogUtils.d("pkgname = " + pkgname[i]);
                PackageManager pm = getPackageManager();
                startActivity(pm.getLaunchIntentForPackage(pkgname[i]));
            }
        });
    }


    public class KioskAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return listIcon.size();
        }

        @Override
        public Object getItem(int i) {
            return listIcon.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = null;

//            if (view == null) {
            viewHolder = new ViewHolder();
            LayoutInflater mInflater = LayoutInflater.from(getApplicationContext());
            view = mInflater.inflate(R.layout.kiosk_mode_item, null);


            viewHolder.appIconImg = view.findViewById(R.id.image);
            viewHolder.appNameText = view.findViewById(R.id.text);


            viewHolder.appIconImg.setBackground(listIcon.get(i));
            viewHolder.appNameText.setText(listName.get(i));
            view.setTag(viewHolder);
//            }

            return view;
        }
    }

    private class ViewHolder {
        ImageView appIconImg;
        TextView appNameText;
    }

    private void updateGridview() {
        LogUtils.d("");
//        getData();
        initData();
//        setDefaultKioskPolicies(true);
    }

}
