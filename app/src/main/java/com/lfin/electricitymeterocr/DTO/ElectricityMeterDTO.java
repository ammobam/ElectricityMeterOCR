package com.lfin.electricitymeterocr.DTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ElectricityMeterDTO implements Serializable {
    // 제조 코드
    private String serialCd;
    // 단상
    private String supplyType;
    // 계량기 타입명
    private String typeName;
    // 전력량계량기 워본 사진이름
    private String electricityFilename;
    // 지역코드 (미사용)
    private String regionCd;
    // 전력량계량기 등록날짜
    private String electricitySaveDate;
    // 삭제 Flag (0:존재, 1:삭제)
    private String delFlag;

    public String getSerialCd() {
        return serialCd;
    }

    public void setSerialCd(String serialCd) {
        this.serialCd = serialCd;
    }

    public String getSupplyType() {
        return supplyType;
    }

    public void setSupplyType(String supplyType) {
        this.supplyType = supplyType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getElectricityFilename() {
        return electricityFilename;
    }

    public void setElectricityFilename(String electricityFilename) {
        this.electricityFilename = electricityFilename;
    }

    public String getRegionCd() {
        return regionCd;
    }

    public void setRegionCd(String regionCd) {
        this.regionCd = regionCd;
    }

    public String getElectricitySaveDate() {
        return electricitySaveDate;
    }

    public void setElectricitySaveDate(String electricitySaveDate) {
        this.electricitySaveDate = electricitySaveDate;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }
    public ElectricityMeterDTO(){

    }
    public ElectricityMeterDTO(String serialCd, String supplyType, String typeName, String electricityFilename, String regionCd, String electricitySaveDate, String delFlag) {
        this.serialCd = serialCd;
        this.supplyType = supplyType;
        this.typeName = typeName;
        this.electricityFilename = electricityFilename;
        this.regionCd = regionCd;
        this.electricitySaveDate = electricitySaveDate;
        this.delFlag = delFlag;
    }

    @Override
    public String toString() {
        return "ElectricityMeterDTO{" +
                "serialCd='" + serialCd + '\'' +
                ", supplyType='" + supplyType + '\'' +
                ", typeName='" + typeName + '\'' +
                ", electricityFilename='" + electricityFilename + '\'' +
                ", regionCd='" + regionCd + '\'' +
                ", electricitySaveDate='" + electricitySaveDate + '\'' +
                ", delFlag='" + delFlag + '\'' +
                ", electDTOs=" + electDTOs.toString() +
                ", modemDTO=" + modemDTO.toString() +
                '}';
    }

    private List<ElectricityPreprocessingDTO> electDTOs = new ArrayList<>();

    public ElectricityMeterDTO setElectPreDTO(ElectricityPreprocessingDTO electDTO) {
        electDTOs.add(electDTO);
//        electDTO.setElectricityMeterDTO(this);
        return this;
    }

    private ModemDTO modemDTO = new ModemDTO();

    public ElectricityMeterDTO setModemDTO(ModemDTO mDTO) {
        modemDTO.setSerialCd(mDTO.getSerialCd());
        modemDTO.setModemCd(mDTO.getModemCd());
        modemDTO.setModemSaveDate(mDTO.getModemSaveDate());
        mDTO.setElectricityMeterDTO(this);
        return this;
    }

    public ModemDTO getModemDTO(){
        return this.modemDTO;
    }
}
