package com.abupdate.mdm.manager;

import com.abupdate.mdm.database.DataBaseHelper;
import com.abupdate.mdm.model.PolicyDataModel;
import com.abupdate.mdm.model.PolicyModel;

import java.util.List;

/*
 * @date   : 2019/09/27
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class DBManager {
    private static DBManager mDBManager = null;

    public DBManager() {
    }

    public static DBManager getInstance(){
        if(mDBManager == null){
            mDBManager = new DBManager();
        }
        return mDBManager;
    }

    public List<PolicyModel> getReportList() {
        return DataBaseHelper.getInstance().getPolicyMain();
    }


    public void insertPolicyMain(PolicyModel reportList) {
        DataBaseHelper.getInstance().insertPolicyMain(reportList);
    }

    public void updatePolicyMainStatus(String version, int status) {
        DataBaseHelper.getInstance().updatePolicyMainStatus(version, status);
    }

    public void deleteReportList(String version) {
        DataBaseHelper.getInstance().deletePolicyMain(version);
    }

    public void updatePolicyDataResult(String version, String name) {
        DataBaseHelper.getInstance().updatePolicyDataResult(version, name);
    }

    public boolean hasPolicyDataFailed(String version) {
        return DataBaseHelper.getInstance().hasPolicyDataFailed(version);
    }
}
