package com.idbridge.model;

public class PersonalData {
    private String cid;         // หมายเลขบัตรประชาชน
    private String titleTH;     // คำนำหน้าไทย
    private String firstNameTH; // ชื่อไทย
    private String lastNameTH;  // นามสกุลไทย
    private String titleEN;     // คำนำหน้าอังกฤษ
    private String firstNameEN; // ชื่ออังกฤษ
    private String lastNameEN;  // นามสกุลอังกฤษ
    private String gender;      // เพศ
    private String birthDate;   // วันเกิด (YYYYMMDD)
    private String address;     // ที่อยู่
    private String issueDate;   // วันออกบัตร
    private String expireDate;  // วันหมดอายุบัตร
    private String photoBase64; // รูปถ่าย (Base64)

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getTitleTH() {
        return titleTH;
    }

    public void setTitleTH(String titleTH) {
        this.titleTH = titleTH;
    }

    public String getFirstNameTH() {
        return firstNameTH;
    }

    public void setFirstNameTH(String firstNameTH) {
        this.firstNameTH = firstNameTH;
    }

    public String getLastNameTH() {
        return lastNameTH;
    }

    public void setLastNameTH(String lastNameTH) {
        this.lastNameTH = lastNameTH;
    }

    public String getTitleEN() {
        return titleEN;
    }

    public void setTitleEN(String titleEN) {
        this.titleEN = titleEN;
    }

    public String getFirstNameEN() {
        return firstNameEN;
    }

    public void setFirstNameEN(String firstNameEN) {
        this.firstNameEN = firstNameEN;
    }

    public String getLastNameEN() {
        return lastNameEN;
    }

    public void setLastNameEN(String lastNameEN) {
        this.lastNameEN = lastNameEN;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getPhotoBase64() {
        return photoBase64;
    }

    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }
}
