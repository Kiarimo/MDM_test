package com.abupdate.mdm.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.manager.PrefManager;
import com.abupdate.mdm.model.PolicyModel;
import com.abupdate.mdm.model.PolicyDataModel;
import com.abupdate.mdm.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/*
 * @date   : 2019/09/24
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "emm_policy.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ";

    private static final String POLICY_MAIN_TABLE_NAME = "emm_policy_main";
    private static final String POLICY_MAIN_COLUMN_ID = "mainId";

    private static final String POLICY_DATA_TABLE_NAME = "emm_policy_data";
    private static final String POLICY_DATA_COLUMN_ID = "dataId";

    private static DataBaseHelper helper;
    private static SQLiteDatabase sqlDB;

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DataBaseHelper getInstance() {
        if (helper == null)
            helper = new DataBaseHelper(MyApplication.getContext());
            try {
                if (sqlDB == null)
                    sqlDB = helper.getWritableDatabase();
            } catch (Exception e) {
                LogUtils.d(e.getMessage());
            }
        return helper;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.beginTransaction();
        try {
            createPolicyMainTables(sqLiteDatabase);
            createPolicyDataTables(sqLiteDatabase);
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        onCreate(sqLiteDatabase);
    }

    private void createPolicyMainTables(SQLiteDatabase db) {
        String cmd = CREATE_TABLE + POLICY_MAIN_TABLE_NAME + " (" +
                POLICY_MAIN_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Const.POLICY_TASK_ID + " INTEGER," +
                Const.POLICY_INTF_TYPE + " INTEGER," +
                Const.POLICY_POLICY_ID + " INTEGER," +
                Const.POLICY_VERSION + " TEXT UNIQUE," +
                Const.POLICY_STATUS + " INTEGER  DEFAULT 0 )";
        db.execSQL(cmd);
    }

    public void insertPolicyMain(PolicyModel reportList) {
        if (reportList == null || sqlDB == null) return;
        ContentValues value = new ContentValues();
        value.put(Const.POLICY_TASK_ID, reportList.getTaskId());
        value.put(Const.POLICY_INTF_TYPE, reportList.getIntfType());
        value.put(Const.POLICY_POLICY_ID, reportList.getPolicyId());
        value.put(Const.POLICY_VERSION, reportList.getVersion());
        sqlDB.insert(POLICY_MAIN_TABLE_NAME, null, value);

        int size = reportList.getData().size();
        for (int i = 0; i < size; i++) {
            PolicyDataModel report = reportList.getData().get(i);
            insertPolicyData(reportList.getVersion(), report);
        }
    }

    public List<PolicyModel> getPolicyMain() {
        if (sqlDB == null) return null;
        List<PolicyModel> list = new ArrayList<>();
        Cursor cursor = sqlDB.query(POLICY_MAIN_TABLE_NAME,
                null, null, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                PolicyModel policyModel = new PolicyModel();
                policyModel.setTaskId(cursor.getString(cursor.getColumnIndex(Const.POLICY_TASK_ID)));
                policyModel.setIntfType(cursor.getString(cursor.getColumnIndex(Const.POLICY_INTF_TYPE)));
                policyModel.setPolicyId(cursor.getString(cursor.getColumnIndex(Const.POLICY_POLICY_ID)));
                policyModel.setStatus(cursor.getInt(cursor.getColumnIndex(Const.POLICY_STATUS)));
                String version = cursor.getString(cursor.getColumnIndex(Const.POLICY_VERSION));
                policyModel.setVersion(version);
                policyModel.setData(getPolicyData(version));
                list.add(policyModel);
            }
            cursor.close();
        }
        return list;
    }

    public void updatePolicyMainStatus(String version, int status) {
        if (TextUtils.isEmpty(version) || sqlDB == null) return;
        ContentValues value = new ContentValues();
        value.put(Const.POLICY_STATUS, status);
        sqlDB.update(POLICY_MAIN_TABLE_NAME, value, Const.POLICY_VERSION + " =?",new String[]{version});
    }

    public void deletePolicyMain(String version) {
        if (TextUtils.isEmpty(version) || sqlDB == null) return;
        sqlDB.delete(POLICY_MAIN_TABLE_NAME, Const.POLICY_VERSION + " =? ", new String[]{version});
        deletePolicyData(version);
    }

    private void createPolicyDataTables(SQLiteDatabase db) {
        String cmd = CREATE_TABLE + POLICY_DATA_TABLE_NAME + " (" +
                POLICY_DATA_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Const.POLICY_DATA_VERSION + " TEXT," +
                Const.POLICY_DATA_NAME + " TEXT," +
                Const. POLICY_DATA_VALUE + " TEXT," +
                Const.POLICY_DATA_EXTEND1 + " TEXT," +
                Const.POLICY_DATA_EXTEND2 + " TEXT," +
                Const.POLICY_DATA_EXTEND3 + " TEXT," +
                Const.POLICY_DATA_RESULT + " INTEGER  DEFAULT 0)";
        db.execSQL(cmd);
    }

    private void insertPolicyData(String version, PolicyDataModel report) {
        if (TextUtils.isEmpty(version) || report == null || sqlDB == null) return;
        ContentValues value = new ContentValues();
        value.put(Const.POLICY_DATA_VERSION, version);
        value.put(Const.POLICY_DATA_NAME, report.getName());
        value.put(Const. POLICY_DATA_VALUE, report.getValue());
        value.put(Const. POLICY_DATA_EXTEND1, report.getExtend1());
        value.put(Const. POLICY_DATA_EXTEND2, report.getExtend2());
        value.put(Const. POLICY_DATA_EXTEND3, report.getExtend3());
        sqlDB.insert(POLICY_DATA_TABLE_NAME, null, value);
    }

    public List<PolicyDataModel> getPolicyData(String version) {
        if (sqlDB == null) return null;
        List<PolicyDataModel> list = new ArrayList<>();
        Cursor cursor = sqlDB.query(POLICY_DATA_TABLE_NAME,
                null, Const.POLICY_DATA_VERSION + " =? ", new String[]{version}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                PolicyDataModel report = new PolicyDataModel();
                report.setName(cursor.getString(cursor.getColumnIndex(Const.POLICY_DATA_NAME)));
                report.setValue(cursor.getString(cursor.getColumnIndex(Const.POLICY_DATA_VALUE)));
                report.setExtend1(cursor.getString(cursor.getColumnIndex(Const.POLICY_DATA_EXTEND1)));
                report.setExtend2(cursor.getString(cursor.getColumnIndex(Const.POLICY_DATA_EXTEND2)));
                report.setExtend3(cursor.getString(cursor.getColumnIndex(Const.POLICY_DATA_EXTEND3)));
                report.setResult(cursor.getInt(cursor.getColumnIndex(Const.POLICY_DATA_RESULT)));
                list.add(report);
            }
            cursor.close();
        }
        return list;
    }

    public void updatePolicyDataResult(String version, String name) {
        if (TextUtils.isEmpty(version) || TextUtils.isEmpty(name) || sqlDB == null) return;
        ContentValues value = new ContentValues();
        value.put(Const.POLICY_DATA_RESULT, 1);
        sqlDB.update(POLICY_DATA_TABLE_NAME, value, Const.POLICY_DATA_VERSION + " =? and " + Const.POLICY_DATA_NAME + " =?",new String[]{version, name});
    }

    private void deletePolicyData(String version) {
        if (TextUtils.isEmpty(version) || sqlDB == null) return;
        sqlDB.delete(POLICY_DATA_TABLE_NAME, Const.POLICY_DATA_VERSION + " =? ", new String[]{version});
    }

    public boolean hasPolicyDataFailed(String version) {
        if (sqlDB == null) return false;
        Cursor cursor = sqlDB.query(POLICY_DATA_TABLE_NAME,
                null, Const.POLICY_DATA_VERSION + " =? and " + Const.POLICY_DATA_RESULT + " =?", new String[]{version, "0"}, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            return true;
        }
        return false;
    }
}
