package com.abupdate.mdm.model;

import java.util.List;

/*
 * @date   : 2019/09/27
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class AppListModel {
    private String sn;
    private String imei1;
    private String imei2;
    private String mac;
    private List<AppModel> data;

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getImei1() {
        return imei1;
    }

    public void setImei1(String imei1) {
        this.imei1 = imei1;
    }

    public String getImei2() {
        return imei2;
    }

    public void setImei2(String imei2) {
        this.imei2 = imei2;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public List<AppModel> getData() {
        return data;
    }

    public void setData(List<AppModel> data) {
        this.data = data;
    }
}
