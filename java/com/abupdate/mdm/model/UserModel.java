package com.abupdate.mdm.model;

/*
 * @date   : 2019/10/30
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class UserModel {
    private String enterprise;
    private String group;
    private String name;
    private String id;
    private String exitPwd;
    private String updatePolicy;

    public String getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(String enterprise) {
        this.enterprise = enterprise;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExitPwd() {
        return exitPwd;
    }

    public void setExitPwd(String exitPwd) {
        this.exitPwd = exitPwd;
    }

    public String getUpdatePolicy() {
        return updatePolicy;
    }

    public void setUpdatePolicy(String updatePolicy) {
        this.updatePolicy = updatePolicy;
    }
}
