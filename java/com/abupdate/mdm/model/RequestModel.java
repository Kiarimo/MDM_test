package com.abupdate.mdm.model;

/*
 * @date   : 2019/10/10
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class RequestModel {
    private String taskId;  //任务id
    private String intfType; //接口类型：0-通知；1-策略；2-指令；3-应用
    private String sn;
    private String imei1;
    private String imei2;
    private String mac;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getIntfType() {
        return intfType;
    }

    public void setIntfType(String intfType) {
        this.intfType = intfType;
    }

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

}
