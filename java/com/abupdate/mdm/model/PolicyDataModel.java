package com.abupdate.mdm.model;

/*
 * @date   : 2019/09/27
 * @author : LIRENQI
 * #eamil  : lirenqi@adups.com
 */
public class PolicyDataModel {
    private String name;
    private String value;
    private String extend1;  //一般为包名
    private String extend2;  //一般为 app code
    private String extend3;  //一般为 app下载地址
    private int result; //执行结果：1成功，0失败

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExtend1() {
        return extend1;
    }

    public void setExtend1(String extend1) {
        this.extend1 = extend1;
    }

    public String getExtend2() {
        return extend2;
    }

    public void setExtend2(String extend2) {
        this.extend2 = extend2;
    }

    public String getExtend3() {
        return extend3;
    }

    public void setExtend3(String extend3) {
        this.extend3 = extend3;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
