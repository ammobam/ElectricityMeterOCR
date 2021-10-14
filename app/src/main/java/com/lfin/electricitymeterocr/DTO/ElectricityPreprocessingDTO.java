package com.lfin.electricitymeterocr.DTO;

public class ElectricityPreprocessingDTO {
    // 시퀀스
    private int preId;
    // 제조 코드
    private String serialCd;
    // 전처리과정 이미지파일명
    private String preFilename;

    private ElectricityMeterDTO electricityMeterDTO;

    public int getPreId() {
        return preId;
    }

    public void setPreId(int preId) {
        this.preId = preId;
    }

    public String getSerialCd() {
        return serialCd;
    }

    public void setSerialCd(String serialCd) {
        this.serialCd = serialCd;
    }

    public String getPreFilename() {
        return preFilename;
    }

    public void setPreFilename(String preFilename) {
        this.preFilename = preFilename;
    }

    public ElectricityMeterDTO getElectricityMeterDTO() {
        return electricityMeterDTO;
    }

    public void setElectricityMeterDTO(ElectricityMeterDTO electricityMeterDTO) {
        this.electricityMeterDTO = electricityMeterDTO;
    }

    public ElectricityPreprocessingDTO(){}

    public ElectricityPreprocessingDTO(int preId, String serialCd, String preFilename) {
        this.preId = preId;
        this.serialCd = serialCd;
        this.preFilename = preFilename;
    }

    @Override
    public String toString() {
        return "ElectricityProprocessingDTO{" +
                "preId=" + preId +
                ", serialCd='" + serialCd + '\'' +
                ", preFilename='" + preFilename + '\'' +
                '}';
    }
}
