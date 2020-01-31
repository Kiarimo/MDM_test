package com.abupdate.mdm.callback;

import org.json.JSONObject;

/*
 * @date   : 2019/09/20
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public interface HttpsCallBack {
    void onFailure(int errCode, String message);

    void onSuccess(JSONObject json);
}
