package com.abupdate.mdm.manager;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.receiver.DeviceOwnerReceiver;
import com.abupdate.mdm.utils.LogUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/*
 * @date   : 2019/09/27
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class PasswordManager {
    private static PasswordManager mPasswordManager = null;
    private static Context mContext = null;
    private DevicePolicyManager mDpm;
    private ComponentName admin;


    public PasswordManager() {
        mContext = MyApplication.getContext();
        mDpm = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        admin = DeviceOwnerReceiver.getComponentName(mContext);
    }

    public static PasswordManager getInstance(){
        if(mPasswordManager == null){
            mPasswordManager = new PasswordManager();
        }
        return mPasswordManager;
    }


    private byte[] generateRandomPasswordToken() {
        try {
            return SecureRandom.getInstance("SHA1PRNG").generateSeed(32);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] createNewPasswordToken() {
        byte[] token = generateRandomPasswordToken();
        if (!mDpm.setResetPasswordToken(admin, token)) {
            LogUtils.e("setResetPasswordToken failed!!!");
            return null;
        }

        PrefManager.getInstance().setPasswordToken(token);
        return token;
    }

    public byte[] getActiveResetPasswordToken() {
        byte[] token = PrefManager.getInstance().getPasswordToken();
        if (token == null) {
            LogUtils.i("Token is null, create new!!");
            token = createNewPasswordToken();
        }

        if (!mDpm.isResetPasswordTokenActive(admin)) {
            LogUtils.e("Token exists but is not activated.");
            if (!mDpm.setResetPasswordToken(admin, token)) {
                LogUtils.e("setResetPasswordToken failed!!!");
            }
            return null;
        }
        return token;
    }

    public boolean doResetPassword(String password) {
        byte[] token = getActiveResetPasswordToken();
        boolean result;
        if (token != null) {
            result = mDpm.resetPasswordWithToken(admin, password, token, 0);
        } else {
            LogUtils.e("Cannot reset password without token");
            result = false;
        }
        return result;
    }

    public void removePasswordToken() {
        if (!mDpm.clearResetPasswordToken(admin)) {
            LogUtils.e("Failed to remove password token!");
        }
    }

    public boolean needActivePasswordToken() {
        if (mDpm.isDeviceOwnerApp(admin.getPackageName())) {
            boolean ret = (getActiveResetPasswordToken() == null);
            LogUtils.d("ret = " + ret);
            return ret;
        }
        return false;
    }
}
