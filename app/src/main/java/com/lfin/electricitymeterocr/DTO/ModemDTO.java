package com.lfin.electricitymeterocr.DTO;

import java.io.Serializable;
import java.util.Date;

public class ModemDTO implements Serializable {
    // 모뎀 코드
    private String modemCd;
    // 제조 코드
    private String serialCd;
    // 모뎀저장날짜
    private String modemSaveDate;

    private String modemFilename;

    private ElectricityMeterDTO electricityMeterDTO;

    public String getModemCd() {
        return modemCd;
    }

    public void setModemCd(String modemCd) {
        this.modemCd = modemCd;
    }

    public String getSerialCd() {
        return serialCd;
    }

    public void setSerialCd(String serialCd) {
        this.serialCd = serialCd;
    }

    public String getModemSaveDate() {
        return modemSaveDate;
    }

    public void setModemSaveDate(String modemSaveDate) {
        this.modemSaveDate = modemSaveDate;
    }

    public String getModemFilename() { return modemFilename; }

    public void setModemFilename(String modemFilename) { this.modemFilename = modemFilename;}

    public ElectricityMeterDTO getElectricityMeterDTO() {
        return electricityMeterDTO;
    }

    public void setElectricityMeterDTO(ElectricityMeterDTO electricityMeterDTO) {
        this.electricityMeterDTO = electricityMeterDTO;
    }

    public ModemDTO(){}

    public ModemDTO(String modemCd, String serialCd, String modemSaveDate) {
        this.modemCd = modemCd;
        this.serialCd = serialCd;
        this.modemSaveDate = modemSaveDate;
    }
    
    @Override
    public String toString() {
        return "ModelDTO{" +
                "modemCd='" + modemCd + '\'' +
                ", serialCd='" + serialCd + '\'' +
                ", modemSaveDate=" + modemSaveDate +
                '}';
    }

}
