package com.abupdate.mdm.callback;

import com.abupdate.hpush.HPushListener;
import com.abupdate.mdm.manager.PrefManager;
import com.abupdate.mdm.manager.PushManager;
import com.abupdate.mdm.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * @date   : 2019/10/15
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class MDMHPushListener implements HPushListener {
    private static final String TASK_ID = "taskId";
    private static final String MESSAGE = "message";
    @Override
    public void onMessageReceived(String message) {
        LogUtils.d(message);
        try {
            JSONObject json = new JSONObject(message);
            String taskId = null;
            if (null != json && json.has(TASK_ID)) {
                taskId = json.optString(TASK_ID);
            }

            String type = null;
            if (null != json && json.has(MESSAGE)) {
                type = json.optString(MESSAGE);
            }

            if (null != type && null != taskId) {
                LogUtils.d("type = " + type + "; taskId = " + taskId);
                PrefManager.getInstance().setLastConnectTime(System.currentTimeMillis());
                PushManager.getInstance().dispatchType(type, taskId);
            } else {
                LogUtils.e("push message format error");
            }
        } catch (JSONException e) {
            LogUtils.e(e.getMessage());
        }
    }

    @Override
    public void onStatusChanged(boolean connected) {
        LogUtils.d(connected + "");
    }
}
