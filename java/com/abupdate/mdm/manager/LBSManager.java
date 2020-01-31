package com.abupdate.mdm.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.abupdate.mdm.MyApplication;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.utils.LogUtils;
import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.DPoint;

import java.util.List;

/*
 * @date   : 2019/10/11
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class LBSManager {
    private static LBSManager mLBSManager = null;
    private static GeoFenceClient mGeoFenceClient = null;

    // 地理围栏的广播action
    private static final String GEOFENCE_BROADCAST_ACTION = "com.abupdate.geofence.round";

    private static Context mContext = null;

    public LBSManager() {
        mContext = MyApplication.getContext();
        mGeoFenceClient = new GeoFenceClient(mContext);
    }

    public static LBSManager getInstance() {
        if (mLBSManager == null) {
            mLBSManager = new LBSManager();
        }
        return mLBSManager;
    }

    private boolean startLBS() {
        String val = PrefManager.getInstance().getLBSData();
        if (Const.DEBUG_MODE) {
            val = "120.52#30.40#3000";
        }
        LogUtils.d(val);


        if (Const.UNKNOW_STRING.equals(val)) {
            LogUtils.e("no lbs data");
            return false;
        }
        String[] listLbs = val.split("#");

        IntentFilter filter = new IntentFilter();
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        mContext.registerReceiver(mGeoFenceReceiver, filter);

        /**
         * 创建pendingIntent
         */
        mGeoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
        mGeoFenceClient.setGeoFenceListener(mGeoFenceListener);

        //设置希望侦测的围栏触发行为，默认只侦测用户进入围栏的行为
        // GEOFENCE_IN 进入地理围栏
        // GEOFENCE_OUT 退出地理围栏
        // GEOFENCE_STAYED 停留在地理围栏内10分钟
        mGeoFenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN | GeoFenceClient.GEOFENCE_OUT);
        //创建一个中心点坐标
        DPoint centerPoint = new DPoint(Double.valueOf(listLbs[1]), Double.valueOf(listLbs[0])); //latitude,longitude

        mGeoFenceClient.addGeoFence(centerPoint, Float.parseFloat(listLbs[2]), "mdm"); // point,fenceRadius,customId
        return true;
    }

    private boolean stopLBS() {
        LogUtils.d("");
        int type = PrefManager.getInstance().getLockType();
        LogUtils.d("type = " + type);
        try {
            mContext.unregisterReceiver(mGeoFenceReceiver);
        } catch (Throwable e) {
        }
        if (mGeoFenceClient != null) {
            mGeoFenceClient.removeGeoFence();
        }
        if (type == 1) {
            ApplyPolicyManager.getInstance().doUnlock();
        }

        return true;
    }

    public boolean setLBS() {
        String status = PrefManager.getInstance().getLBSStatus();
        LogUtils.d("status = " + status);
        if (Const.DEBUG_MODE) {
            status = "0";
        }
        if ("1".equals(status)) {
            return startLBS();
        } else {
            return stopLBS();
        }
    }

    private GeoFenceListener mGeoFenceListener = new GeoFenceListener() {
        @Override
        public void onGeoFenceCreateFinished(final List<GeoFence> geoFenceList,
                                             int errorCode, String customId) {
            LogUtils.d("errorCode = " + errorCode);
            if (errorCode == GeoFence.ADDGEOFENCE_SUCCESS) {
                LogUtils.d("电子围栏创建成功");
            } else {
                LogUtils.e("电子围栏创建失败");
            }
        }
    };

    /**
     * 接收触发围栏后的广播,当添加围栏成功之后，会立即对所有围栏状态进行一次侦测，如果当前状态与用户设置的触发行为相符将会立即触发一次围栏广播；
     * 只有当触发围栏之后才会收到广播,对于同一触发行为只会发送一次广播不会重复发送，除非位置和围栏的关系再次发生了改变。
     */
    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 接收广播
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                Bundle bundle = intent.getExtras();
                //status标识的是当前的围栏状态，不是围栏行为
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
                int type = PrefManager.getInstance().getLockType();
                LogUtils.e("status = " + status + "; type = " + type);
                switch (status) {
                    case GeoFence.STATUS_LOCFAIL:
                        LogUtils.e("定位失败");
                        break;
                    case GeoFence.STATUS_IN:
                        LogUtils.d("进入围栏 ");
                        if (type == Const.LOCK_TYPE_LBS) {
                            ApplyPolicyManager.getInstance().doUnlock();
                            UseLifeManager.getInstance().setUseLife(true);
                        }

                        break;
                    case GeoFence.STATUS_OUT:
                        LogUtils.d("离开围栏 ");
                        if (type == Const.LOCK_TYPE_UNKNOWN) {
                            ApplyPolicyManager.getInstance().doLock();
                            PrefManager.getInstance().setLockType(Const.LOCK_TYPE_LBS);
                        }
                        break;
                    case GeoFence.STATUS_STAYED:
                        LogUtils.d("停留在围栏内 ");
                        break;
                    default:
                        break;
                }
            }
        }
    };


}
