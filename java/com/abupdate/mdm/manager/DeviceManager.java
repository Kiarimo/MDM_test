package com.abupdate.mdm.manager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;
import android.telephony.TelephonyManager;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.receiver.DeviceOwnerReceiver;
import com.abupdate.mdm.utils.LogUtils;
import com.system.api.DpmApi;
import com.system.api.MdmApi;
import com.system.api.SysProp;
import com.system.api.SystemApi;

import java.io.File;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/*
 * @date   : 2019/09/26
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class DeviceManager {
    private static DeviceManager mDeviceManager = null;
    private static Context mContext = null;

    public DeviceManager() {
        mContext = MyApplication.getContext();
    }

    public static DeviceManager getInstance() {
        if (mDeviceManager == null) {
            mDeviceManager = new DeviceManager();
        }
        return mDeviceManager;
    }

    public String getSN() {
        if (Const.DEBUG_MODE) {
            Random r = new Random();
            return r.nextInt(10000000) + "";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Build.getSerial();
        } else {
            return Build.SERIAL;
        }
    }

    public String getImei(int slotId) {
        return SystemApi.getInstance(mContext).getImei(slotId);
    }

    public String getMAC() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    public String getBrand() {
        return Build.BRAND;
    }

    public String getModel() {
        return Build.MODEL;
    }

    public String getSdkLevel() {
        return Build.VERSION.SDK_INT + "";
    }

    public String getOSVersion() {
        return Build.DISPLAY;
    }

    public String getPlatform() {
        return Build.HARDWARE;
    }

    private PackageInfo getPackageInfo(Context context) {
        PackageInfo info = null;
        PackageManager pm = context.getPackageManager();
        try {
            info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

    public String getAppVersion() {
        PackageInfo info = getPackageInfo(mContext);
        if (info != null) {
            LogUtils.d("versionName = " + info.versionName);
            return info.versionName;
        }
        return null;
    }

    public String getAppCode() {
        PackageInfo info = getPackageInfo(mContext);
        if (info != null) {
            return String.valueOf(info.versionCode);
        }
        return null;
    }

    private NetworkInfo getNetworkInfo() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        return mNetworkInfo;
    }

    public String getConnectedType() {
        NetworkInfo mNetworkInfo = getNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            if (mNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return 2 + ""; //wifi
            }
        }
        return 1 + "";  //mobile
    }

    public boolean isNetworkAvailable() {
        NetworkInfo mNetworkInfo = getNetworkInfo();
        if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
            return true;
        }
        return false;
    }

    public boolean setDeviceOwner() {
        try {
            DpmApi.getInstance(mContext).setDeviceOwner(DeviceOwnerReceiver.getComponentName(mContext));
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setEnrollCode(String code) {
        if (code == null || code.length() < 8) {
            return false;
        }

        String data = "1" + code;
        int ret = MdmApi.getInstance(mContext).writeMdmData(0, data.getBytes());
        LogUtils.d("ret = " + ret + "; data = " + data);
        if (ret < 8) {
            return false;
        }
        return true;
    }


    public String getEnrollCode() {
        byte[] data = MdmApi.getInstance(mContext).readMdmData(0, 30);
        if (0 != data[0]) {
            int j = 0;
            for (int i = 1; i < 30; i++) {
                if (0 == data[i]) {
                    j = i;
                    break;
                }
            }
            return new String(data).substring(1, j);
        }
        return null;
    }

    public boolean resetEnrollCode() {
        byte[] data = new byte[1024];
        for (int i = 0; i < 1024; i++) {
            data[i] = 0;
        }
        int ret = MdmApi.getInstance(mContext).writeMdmData(0, data);
        LogUtils.d("ret = " + ret);
        if (ret < 0) {
            return false;
        }
        return true;
    }

    public boolean hasSimCard(Context context) {
        TelephonyManager telMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false; // 没有SIM卡
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        return result;
    }


    public String getSystemProperties(String key) {
        return SysProp.getString(key, "");
    }


    public String getScreenSize() {
        int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        return screenWidth + "x" + screenHeight;

    }


    public String getTotalInternalMemorySize() {
        //获取内部存储根目录
        File path = Environment.getDataDirectory();
        //系统的空间描述类
        StatFs stat = new StatFs(path.getPath());
        //每个区块占字节数
        long blockSize = stat.getBlockSize();
        //区块总数
        long totalBlocks = stat.getBlockCount();
        return convertFileSize(totalBlocks * blockSize);
    }


    public String convertFileSize(long size) {
        float result = size;
        String suffix = "B";
        if (result > 1024) {
            suffix = "KB";
            result = result / 1024;
        }
        if (result > 1024) {
            suffix = "MB";
            result = result / 1024;
        }
        if (result > 1024) {
            suffix = "GB";
            result = result / 1024;
        }
        if (result > 1024) {
            suffix = "TB";
            result = result / 1024;
        }

        final String roundFormat;
        if (result < 10) {
            roundFormat = "%.2f";
        } else if (result < 100) {
            roundFormat = "%.1f";
        } else {
            roundFormat = "%.0f";
        }
        return String.format(roundFormat + suffix, result);
    }


}
