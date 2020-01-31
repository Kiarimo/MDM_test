package com.abupdate.mdm.manager;

import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.database.DataBaseHelper;
import com.abupdate.mdm.model.PolicyDataModel;
import com.abupdate.mdm.model.PolicyModel;
import com.abupdate.mdm.utils.LogUtils;

import java.util.List;

/*
 * @date   : 2019/10/10
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class PolicyManager {
    private static PolicyManager mPolicyManager = null;

    public PolicyManager() {
    }

    public static PolicyManager getInstance() {
        if (mPolicyManager == null) {
            mPolicyManager = new PolicyManager();
        }
        return mPolicyManager;
    }

    private void applyData(PolicyDataModel data, String version) {
        boolean ret = false;
        String name = data.getName();

        int type = PrefManager.getInstance().getLockType();
        LogUtils.d("name = " + name + "; version = " + version + "; type = " + type);
        if (name.equals(Const.POLICY_REBOOT) || name.equals(Const.POLICY_SHUTDOWN) || name.equals(Const.POLICY_FACTORY_RESET)) {
            LogUtils.e("updatePolicyDataResult ");
            DBManager.getInstance().updatePolicyDataResult(version, name);
        }
        switch (name) {
            case Const.POLICY_INSTALL_APP:
                ret = ApplyPolicyManager.getInstance().doInstallApp(data);
                break;
            case Const.POLICY_UNINSTALL_APP:
                ret = ApplyPolicyManager.getInstance().doUninstallApp(data);
                break;
            case Const.POLICY_UNKNOWN_SOURCES:
                ret = ApplyPolicyManager.getInstance().doUnknownSources(data);
                break;
            case Const.POLICY_KIOSK:
                ret = ApplyPolicyManager.getInstance().doKiosk(data);
                break;
            case Const.POLICY_ENABLE_GPS:
                ret = ApplyPolicyManager.getInstance().doEnableGPS(data);
                break;
            case Const.POLICY_GPS_STATUS:
                ret = ApplyPolicyManager.getInstance().doGPSStatus(data);
                break;
            case Const.POLICY_BLUETOOTH_FILE:
                ret = ApplyPolicyManager.getInstance().doBluetoothFile(data);
                break;
            case Const.POLICY_CHARGE_ONLY:
                ret = ApplyPolicyManager.getInstance().doCharageOnly(data);
                break;
            case Const.POLICY_ENABLE_FACTORY_RESET:
                ret = ApplyPolicyManager.getInstance().doEnableFactoryReset(data);
                break;
            case Const.POLICY_EXTEND_STATUS_BAR:
                ret = ApplyPolicyManager.getInstance().doExtendStatusBar(data);
                break;
            case Const.POLICY_LOCKSCREEN_PASSWORD:
                ret = ApplyPolicyManager.getInstance().doLockscreenPassword(data);
                break;
            case Const.POLICY_CAMERA_STATUS:
                ret = ApplyPolicyManager.getInstance().doEnableCamera(data);
                break;
            case Const.POLICY_STORAGE_STATUS:
                ret = ApplyPolicyManager.getInstance().doEnableStorage(data);
                break;
            case Const.POLICY_LBS_STATUS:
                ret = ApplyPolicyManager.getInstance().doLBS(data);
                break;
            case Const.POLICY_USE_LIFE:
                ret = ApplyPolicyManager.getInstance().doUseLife(data);
                break;
            case Const.POLICY_LOCK:
                if (type == Const.LOCK_TYPE_UNKNOWN) {
                    ret = ApplyPolicyManager.getInstance().doLock();
                    PrefManager.getInstance().setLockType(Const.LOCK_TYPE_POLICY_LOCK);
                }
                break;
            case Const.POLICY_UNLOCK:
                ret = ApplyPolicyManager.getInstance().doUnlock();
                break;
            case Const.POLICY_CAPTURE:
                ret = ApplyPolicyManager.getInstance().doCapture();
                break;
            case Const.POLICY_FACTORY_RESET:
                ret = ApplyPolicyManager.getInstance().doFactoryReset();
                break;
            case Const.POLICY_REBOOT:
                ret = ApplyPolicyManager.getInstance().doReboot();
                break;
            case Const.POLICY_SHUTDOWN:
                ret = ApplyPolicyManager.getInstance().doShutdown();
                break;
            case Const.POLICY_CLEAR_PASSWORD:
                ret = ApplyPolicyManager.getInstance().doClearPassword();
                break;
            case Const.POLICY_CHANGE_WALLPAPER:
                ret = ApplyPolicyManager.getInstance().doChangeWallpaper(data);
                break;
            case Const.POLICY_BLOCK_NOTICE:
                ret = ApplyPolicyManager.getInstance().doBlockNotice(data);
                break;
            case Const.POLICY_INSTALL_TYPE:
                ret = ApplyPolicyManager.getInstance().doInstallType(data);
                break;
            case Const.POLICY_USE_APP:
                ret = ApplyPolicyManager.getInstance().doUseApp(data);
                break;
            case Const.POLICY_SHOW_ICON:
                ret = ApplyPolicyManager.getInstance().doShowIcon(data);
                break;
            case Const.POLICY_CHANGE_PASSWORD:
                ret = ApplyPolicyManager.getInstance().doChangePassword(data);
                break;
            case Const.POLICY_UPDATE_APP:
                ret = ApplyPolicyManager.getInstance().doUpdateApp(data);
                break;
            case Const.POLICY_CLEAR_APPDATA:
                ret = ApplyPolicyManager.getInstance().doClearAppData(data);
                break;
            case Const.POLICY_OCCUPY_SCREEN:
                ret = ApplyPolicyManager.getInstance().doOccupyScreen(data);
                break;
            default:
                if (name.startsWith(Const.POLICY_APP_PERMISSION)) {
                    ret = ApplyPolicyManager.getInstance().doAppPermission(data);
                }
        }
        LogUtils.d("ret = " + ret);
        if (ret) {
            DBManager.getInstance().updatePolicyDataResult(version, name);
        }
    }

    public void updatePolicyStatus(String version) {
        if (DBManager.getInstance().hasPolicyDataFailed(version)) {
            DBManager.getInstance().updatePolicyMainStatus(version, 2);
            LogUtils.e("policy apply falied!");
        } else {
            DBManager.getInstance().updatePolicyMainStatus(version, 1);
            LogUtils.d("policy apply success!");
        }
    }

    private void applyPolicy(PolicyModel reportList) {
        LogUtils.d("");
        List<PolicyDataModel> dataList = reportList.getData();
        int size = dataList.size();
        LogUtils.d("size = " + size);
        for (int i = 0; i < size; i++) {
            PolicyDataModel data = dataList.get(i);
            LogUtils.d("dataname = " + data.getName());
            LogUtils.d("getResult = " + data.getResult());
            if (data.getResult() == 0) {
                applyData(data, reportList.getVersion());
            }
        }

        updatePolicyStatus(reportList.getVersion());
        ReportManager.getInstance().doReport();
    }

    public void applyPolicyIfNeed() {

        List<PolicyModel> reportLists = DBManager.getInstance().getReportList();
        int size = reportLists.size();
        LogUtils.d("size = " + size);

        for (int i = 0; i < size; i++) {
            PolicyModel policyModel = reportLists.get(i);
            LogUtils.d("getStatus = " + policyModel.getStatus());
            LogUtils.d("getVersion = " + policyModel.getVersion());
            if (policyModel.getStatus() != 1) {
                applyPolicy(policyModel);
            }
        }
    }
}
