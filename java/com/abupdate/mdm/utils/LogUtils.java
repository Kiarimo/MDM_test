package com.abupdate.mdm.utils;

import android.util.Log;

/*
 * @date   : 2019/09/20
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class LogUtils {
    public static final String TAG = "MDMLog";
    static String className;//类名
    static String methodName;//方法名
    static int lineNumber;//行数

    public static void v(String msg) {
        getMethodNames(new Throwable().getStackTrace());
        Log.v(TAG,createLog(msg));
    }

    public static void d(String msg) {
        getMethodNames(new Throwable().getStackTrace());
        Log.d(TAG,createLog(msg));
    }

    public static void i(String msg) {
        getMethodNames(new Throwable().getStackTrace());
        Log.i(TAG,createLog(msg));
    }

    public static void w(String msg) {
        getMethodNames(new Throwable().getStackTrace());
        Log.w(TAG,createLog(msg));
    }

    public static void e(String msg) {
        getMethodNames(new Throwable().getStackTrace());
        Log.e(TAG,createLog(msg));
    }

    private static String createLog(String log) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(").append(className).append(":").append(lineNumber).append(")");
        buffer.append(methodName);
        buffer.append("->");
        buffer.append(log);
        return buffer.toString();
    }

    private static void getMethodNames(StackTraceElement[] sElements) {
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }
}