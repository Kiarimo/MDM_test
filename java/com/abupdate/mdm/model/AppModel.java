package com.abupdate.mdm.model;

/*
 * @date   : 2019/09/27
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class AppModel {
    private String appName;
    private String packageName;
    private String version;
    private int code;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
