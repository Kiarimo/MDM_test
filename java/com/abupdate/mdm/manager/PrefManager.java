package com.abupdate.mdm.manager;

import android.content.Context;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.utils.PrefUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/*
 * @date   : 2019/09/20
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class PrefManager {
    private static PrefManager mPrefManager = null;
    private static Context mContext = null;

    //当前服务器地址
    private static final String CURRENT_SERVER_URL = "current_server_url";
    private static final String LBS_STATUS = "lbs_status";
    private static final String LBS_DATA = "lbs_data";
    private static final String USE_LIFE_STATUS = "use_life_status";
    private static final String USE_LIFE_DATA = "use_life_data";
    private static final String PASSWORD_TOKEN = "password_token";
    private static final String USER_INFORMATION = "user_information";
    private static final String IN_KIOSK = "in_kiosk";
    private static final String LOCK_TYPE = "lock_type";
    private static final String ADB_STATUS = "abupdata_adb_status";

    private static final String KIOSK_APPS_KEY = "kiosk_apps";

    private static final String INSTALL_APP = "installApp";
    private static final String EXIT_PASSWORD = "exitPassword";

    private static final String LAST_CONNECT_TIME = "lastConnectTime";


    private static final String SERVER_MD5 = "md5";


    private static final String UPDATE_RESULT = "updateResult";

    private static final String OCCUPY_SCREEN_NAME = "occupyScreenName";

    private static final String UPDATE_APK_NAME = "updateApkName";

    private static final String UPDATE_APK_URL = "downloadUrl";

    public PrefManager() {
        mContext = MyApplication.getContext();
    }

    public static PrefManager getInstance() {
        if (mPrefManager == null) {
            mPrefManager = new PrefManager();
        }
        return mPrefManager;
    }

    public boolean setServerUrl(String vlue) {
        return PrefUtils.putString(mContext, CURRENT_SERVER_URL, vlue);
    }

    public String getServerUrl(String defaultUrl) {
        return PrefUtils.getString(mContext, CURRENT_SERVER_URL, defaultUrl);
    }

    public boolean setLBSStatus(String vlue) {
        return PrefUtils.putString(mContext, LBS_STATUS, vlue);
    }

    public String getLBSStatus() {
        return PrefUtils.getString(mContext, LBS_STATUS, Const.UNKNOW_STRING);
    }

    public boolean setLBSData(String vlue) {
        return PrefUtils.putString(mContext, LBS_DATA, vlue);
    }

    public String getLBSData() {
        return PrefUtils.getString(mContext, LBS_DATA, Const.UNKNOW_STRING);
    }

    public boolean setUseLifeStatus(String vlue) {
        return PrefUtils.putString(mContext, USE_LIFE_STATUS, vlue);
    }

    public String getUseLifeStatus() {
        return PrefUtils.getString(mContext, USE_LIFE_STATUS, Const.UNKNOW_STRING);
    }

    public boolean setUseLifeData(String vlue) {
        return PrefUtils.putString(mContext, USE_LIFE_DATA, vlue);
    }

    public String getUseLifeData() {
        return PrefUtils.getString(mContext, USE_LIFE_DATA, Const.UNKNOW_STRING);
    }

    public boolean putBoolean(String key, boolean vlue) {
        return PrefUtils.putBoolean(mContext, key, vlue);
    }

    public boolean getBoolean(String key) {
        return PrefUtils.getBoolean(mContext, key, false);
    }


    public boolean putADBStatus(int vlue) {
        return PrefUtils.putInt(mContext, ADB_STATUS, vlue);
    }

    public int getADBStatus() {
        return PrefUtils.getInt(mContext, ADB_STATUS, 0);
    }


    public boolean setPasswordToken(byte[] token) {
        String tokenStr = Base64.getEncoder().encodeToString(token);
        return PrefUtils.putString(mContext, PASSWORD_TOKEN, tokenStr);
    }

    public byte[] getPasswordToken() {
        String tokenStr = PrefUtils.getString(mContext, PASSWORD_TOKEN, Const.UNKNOW_STRING);
        if (tokenStr == null || Const.UNKNOW_STRING.equals(tokenStr)) {
            return null;
        }
        return Base64.getDecoder().decode(tokenStr.getBytes(StandardCharsets.UTF_8));
    }

    public boolean setUserInformation(String vlue) {
        return PrefUtils.putString(mContext, USER_INFORMATION, vlue);
    }

    public String getUserInformation() {
        return PrefUtils.getString(mContext, USER_INFORMATION, Const.UNKNOW_STRING);
    }

    public boolean setInKiosk(boolean vlue) {
        return PrefUtils.putBoolean(mContext, IN_KIOSK, vlue);
    }

    public boolean isInKiosk() {
        return PrefUtils.getBoolean(mContext, IN_KIOSK, false);
    }

    public boolean setKioskApp(String value) {
        return PrefUtils.putString(mContext, KIOSK_APPS_KEY, value);

    }

    public String getKioskApp() {
        return PrefUtils.getString(mContext, KIOSK_APPS_KEY, Const.UNKNOW_STRING);
    }

    public boolean setLockType(int type) {

        return PrefUtils.putInt(mContext, LOCK_TYPE, type);
    }

    public int getLockType() {
        return PrefUtils.getInt(mContext, LOCK_TYPE, Const.LOCK_TYPE_UNKNOWN);
    }


    public boolean setInstallApp(String type) {

        return PrefUtils.putString(mContext, INSTALL_APP, type);
    }

    public String getInstallApp() {
        return PrefUtils.getString(mContext, INSTALL_APP, Const.UNKNOW_STRING);
    }

    public boolean setExitPassword(String value) {

        return PrefUtils.putString(mContext, EXIT_PASSWORD, value);
    }

    public String getExitPassword() {
        return PrefUtils.getString(mContext, EXIT_PASSWORD, Const.UNKNOW_STRING);
    }


    public boolean setLastConnectTime(long time) {

        return PrefUtils.putLong(mContext, LAST_CONNECT_TIME, time);
    }

    public Long getLastConnectTime() {
        return PrefUtils.getLong(mContext, LAST_CONNECT_TIME, 0L);
    }


    public String getMd5() {

        return PrefUtils.getString(mContext, SERVER_MD5, Const.UNKNOW_STRING);
    }

    public boolean setMd5(String md5) {

        return PrefUtils.putString(mContext, SERVER_MD5, md5);
    }

    public String getOccupyName() {

        return PrefUtils.getString(mContext, OCCUPY_SCREEN_NAME, "com.myxfitness.app");
    }

    public boolean setOccupyName(String name) {

        return PrefUtils.putString(mContext, OCCUPY_SCREEN_NAME, name);
    }



    public String getUpdateApkName() {

        return PrefUtils.getString(mContext, UPDATE_APK_NAME, Const.UNKNOW_STRING);
    }

    public boolean setUpdateApkName(String name) {

        return PrefUtils.putString(mContext, UPDATE_APK_NAME, name);
    }



    public boolean getUpdateResult() {

        return PrefUtils.getBoolean(mContext, UPDATE_RESULT, false);
    }

    public boolean setUpdateResult(boolean result) {

        return PrefUtils.putBoolean(mContext, UPDATE_RESULT, result);
    }


    public boolean setUpdateFileUrl(String url) {

        return PrefUtils.putString(mContext, UPDATE_APK_URL, url);
    }

    public String getUpdateFileUrl() {
        return PrefUtils.getString(mContext, UPDATE_APK_URL, Const.UNKNOW_STRING);
    }


}
