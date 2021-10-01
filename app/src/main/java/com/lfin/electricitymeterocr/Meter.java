package com.lfin.electricitymeterocr;

import java.io.Serializable;

public class Meter implements Serializable {
    private String serialCd;
    private String supplyType;
    private String typename;
    private String electricityFilename;
    private String regionCd;
    private String electricitySaveDate;
    private String delFlag;

    public Meter(String serialCd,String supplyType,String typename,
                 String electricityFilename,String regionCd,
                 String electricitySaveDate,String delFlag){
        this.serialCd = serialCd;
        this.supplyType = supplyType;
        this.typename = typename;
        this.electricityFilename = electricityFilename;
        this.regionCd = regionCd;
        this.electricitySaveDate = electricitySaveDate;
        this.delFlag = delFlag;
    }

    public String getSerialCd() {
        return serialCd;
    }

    public String getSupplyType() {
        return supplyType;
    }

    public String getTypename() {
        return typename;
    }

    public String getElectricityFilename() {
        return electricityFilename;
    }

    public String getRegionCd() {
        return regionCd;
    }

    public String getElectricitySaveDate() {
        return electricitySaveDate;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setSerialCd(String serialCd) {
        this.serialCd = serialCd;
    }

    public void setSupplyType(String supplyType) {
        this.supplyType = supplyType;
    }

    public void setTypename(String typename) {
        this.typename = typename;
    }

    public void setElectricityFilename(String electricityFilename) {
        this.electricityFilename = electricityFilename;
    }

    public void setRegionCd(String regionCd) {
        this.regionCd = regionCd;
    }

    public void setElectricitySaveDate(String electricitySaveDate) {
        this.electricitySaveDate = electricitySaveDate;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    //디버깅을 위한 메소드
    // 인스턴스 이름을 출력하는 메소드에 대입하면 자동으로 호출됨
    @Override
    public String toString() {
        return "Meter{" +
                ", serialCd='" + serialCd + '\'' +
                ", supplyType='" + supplyType + '\'' +
                ", typename='" + typename + '\'' +
                ", electricityFilename='" + electricityFilename + '\'' +
                ", regionCd='" + regionCd + '\'' +
                ", electricitySaveDate='" + electricitySaveDate + '\'' +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}
