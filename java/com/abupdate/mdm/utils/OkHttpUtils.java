package com.abupdate.mdm.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.abupdate.mdm.R;
import com.abupdate.mdm.callback.HttpsCallBack;
import com.abupdate.mdm.config.Const;
import com.abupdate.mdm.manager.DeviceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/*
 * @date   : 2019/09/20
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class OkHttpUtils {
    private static final int NETWORK_STATUS_OK = 200;
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");

    private static OkHttpUtils okHttpUtil = null;
    private OkHttpClient okHttpClient;

    public OkHttpUtils() {
        okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .hostnameVerifier(MDMHostnameVerifier.INSTANCE)
                .build();
    }

    public static OkHttpUtils getInstance() {
        if (okHttpUtil == null) {
            okHttpUtil = new OkHttpUtils();
        }
        return okHttpUtil;
    }

    public static Request.Builder getBuilder() {
        return new Request.Builder();
    }

    /**
     * 该不会开启异步线程。
     *
     * @param url
     * @return
     * @throws IOException
     */

    public Response get(String url, Map<String, String> params) throws IOException {
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (url.contains("?")) {
                    url = url + "&" + key + "=" + value;
                } else {
                    url = url + "?" + key + "=" + value;
                }
            }
        }

        Request request = getBuilder().url(url).build();
        Response response = okHttpClient.newCall(request).execute();
//        LogUtils.d(response.body().string());
        return response;
    }

    private void postRequest(String url, RequestBody body, final HttpsCallBack callback) {
        Request request = getBuilder().url(url).post(body).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtils.e("post err = " + e.getMessage());
                if (callback != null) {
                    callback.onFailure(Const.NETWORK_CODE_UNKNOWN, e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                int code = response.code();
                String message = response.body().string();
                LogUtils.d("code = " + code + "; message = " + message);
                if (NETWORK_STATUS_OK == code) {
                    try {
                        JSONObject json = new JSONObject(message);
                        LogUtils.d("json = " + json);
                        int returnCode = json.optInt(Const.RESPONSE_CODE);
                        LogUtils.d("returnCode = " + returnCode);
                        if (callback != null) {
                            if (Const.NETWORK_CODE_OK == returnCode) {
                                if (json.has(Const.RESPONSE_VALUE)) {
                                    String returnValue = json.optString(Const.RESPONSE_VALUE);
                                    JSONObject jsonStr = new JSONObject(returnValue);
                                    LogUtils.d("jsonStr = " + jsonStr);
                                    callback.onSuccess(jsonStr);
                                } else {
                                    callback.onSuccess(json);
                                }
                            } else {
                                String returnMessage;
                                if (json.has(Const.RESPONSE_VALUE)) {
                                    String returnValue = json.optString(Const.RESPONSE_VALUE);
                                    JSONObject jsonStr = new JSONObject(returnValue);
                                    returnMessage = jsonStr.optString(Const.RESPONSE_MSG);
                                } else {
                                    returnMessage = String.valueOf(R.string.register_fail);
                                }
                                LogUtils.d("returnMessage = " + returnMessage);
                                callback.onFailure(returnCode, returnMessage);
                            }
                        }
                    } catch (JSONException e) {
//                        e.printStackTrace();
                        LogUtils.e("JSONException = " + e.getMessage());
                        if (callback != null) {
                            callback.onFailure(Const.NETWORK_CODE_JSON_EXCEPTION, e.getMessage());
                        }
                    }
                } else if (callback != null) {
                    callback.onFailure(code, String.valueOf(R.string.register_fail));
                }
            }
        });
    }

    /**
     * 开启异步线程访问网络
     *
     * @param url
     * @param callback
     */
    public void post(String url, Map<String, String> params, final HttpsCallBack callback) {
        if (url == null || params == null) {
            LogUtils.d("url or params is null!!");
            return;
        }

        FormBody.Builder bodyBuilder = new FormBody.Builder();
//        for (Map.Entry<String, String> entry : params.entrySet()) {
//            String key = entry.getKey();
//            String value = entry.getValue()!=null ? entry.getValue(): Const.UNKNOW_STRING;
//            bodyBuilder.add(key, value);
//        }

        String paramsStr = StringUtils.mapToString(params);
        LogUtils.d("paramsStr = " + paramsStr);
        String value = AESUtils.encrypt(paramsStr);
//        LogUtils.d("value = " + value);
        bodyBuilder.add("key", value);
        postRequest(url, bodyBuilder.build(), callback);
    }

    /**
     * 开启异步线程访问网络
     *
     * @param url
     * @param callback
     */
    public void postMultipart(String url, Map<String, String> params, final HttpsCallBack callback) {
        if (url == null || params == null) {
            LogUtils.d("url or params is null!!");
            return;
        }

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() != null ? entry.getValue() : Const.UNKNOW_STRING;
            if ("file".equals(key)) {
                File img = new File(value);
                if (img.exists()) {
                    bodyBuilder.addFormDataPart(key, img.getName(), RequestBody.create(MEDIA_TYPE_PNG, img));
                }
            } else {
                bodyBuilder.addFormDataPart(key, entry.getValue());
            }
        }

        postRequest(url, bodyBuilder.build(), callback);
    }

    public void postJson(String url, String json, final HttpsCallBack callback) {
        if (url == null || json == null) {
            LogUtils.e("url or json is null!!");
            if (callback != null) {
                callback.onFailure(Const.NETWORK_CODE_PARAM_EXCEPTION, "url or json is null!!");
            }
            return;
        }

        if (!DeviceManager.getInstance().isNetworkAvailable()) {
            LogUtils.e("no network!!");
            if (callback != null) {
                callback.onFailure(Const.NETWORK_CODE_NO_NETWORK, "no network!!");
            }
            return;
        }
        LogUtils.d("json = " + json);
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , json);

        postRequest(url, requestBody, callback);
    }

    public Bitmap downloadImage(String url) {
        try {
            Response response = get(url, null);
            if (response.isSuccessful()) {
                InputStream inputStream = response.body().byteStream();//得到图片的流
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static void reset_DNS() {
        Security.setProperty("networkaddress.cache.ttl", String.valueOf(0));
        Security.setProperty("networkaddress.cache.negative.ttl", String.valueOf(0));
    }


}