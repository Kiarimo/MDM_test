package com.abupdate.mdm.config;

import android.text.TextUtils;

import com.abupdate.mdm.manager.PrefManager;

/*
 * @date   : 2019/09/20
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class ServerApi {
    private static final String URL_DEFAULT = "https://mdm.abupdate.com";//"https://testsub.adups.com";

    private static final String URL_TEST = "https://mdm-testwx.abupdate.com";

    /**
     * project emm path
     */
//    private final static String PROJECT_EMM_PATH = "/emm";
    private final static String PROJECT_EMM_PATH = "/emmsw";
//    private final static String PROJECT_EMM_PATH = "/emmap";//自研
    /**
     * project push path
     */
    private final static String PROJECT_PUSH_PATH = "/push";

    /**
     * enroll
     */
    private final static String API_ENROLL = "/enroll";

    /**
     * report
     */
    private final static String API_REPORT = "/report";

    /**
     * control
     */
    private final static String API_CONTROL = "/control";

    /**
     * capture
     */
    private final static String API_CAPTURE = "/capture";

    /**
     * app list
     */
    private final static String API_APP = "/app";

    /**
     * notice
     */
    private final static String API_NOTICE = "/notify";

    /**
     * notice
     */
    private final static String API_SYNC = "/synchro";


    /**
     * check
     */
    private final static String API_CHECK = "/initCheck";


    /**
     * enroll
     */
    private final static String API_REGISTER = "/register";

    public static String getHostName() {
        String url = URL_DEFAULT;
        if (Const.DEBUG_MODE) {
            url = URL_TEST;
        }
        return url;
    }

    private static String getEmmServer() {
        String url = getHostName();
        return PrefManager.getInstance().getServerUrl(url) + PROJECT_EMM_PATH;
    }


    private static String getPushServer() {
        String url = getHostName();
        return PrefManager.getInstance().getServerUrl(url) + PROJECT_PUSH_PATH;
    }

    public static void setServerURL(String url) {
        String mURL;
        if (TextUtils.isEmpty(url) || !url.startsWith("https://")) {
            mURL = getHostName();
        } else {
            mURL = url;
        }

        PrefManager.getInstance().setServerUrl(mURL);
    }

    public static String getApiEnroll() {
        return getEmmServer() + API_ENROLL;
    }

    public static String getApiReport() {
        return getEmmServer() + API_REPORT;
    }

    public static String getApiControl() {
        return getEmmServer() + API_CONTROL;
    }

    public static String getApiCapture() {
        return getEmmServer() + API_CAPTURE;
    }

    public static String getApiApp() {
        return getEmmServer() + API_APP;
    }

    public static String getApiNotice() {
        return getEmmServer() + API_NOTICE;
    }

    public static String getApiSync() {
        return getEmmServer() + API_SYNC;
    }

    public static String getApiCheck() {
        return getEmmServer() + API_CHECK;
    }

    public static String getApiRegister() {
        return getPushServer() + API_REGISTER;
    }
}
