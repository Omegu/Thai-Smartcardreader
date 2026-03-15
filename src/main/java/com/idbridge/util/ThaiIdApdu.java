package com.idbridge.util;

public class ThaiIdApdu {
    // -------------------------------------------------------------
    // คำสั่ง APDU สำหรับอ่านบัตรประชาชนไทย
    // -------------------------------------------------------------
    
    // Command สำหรับเลือก Applet ของกรมการปกครอง
    public static final byte[] SELECT_APPLET = {
        (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x08,
        (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x54, (byte) 0x48, (byte) 0x00, (byte) 0x01
    };

    // คำสั่งอ่านเลขบัตรประจำตัวประชาชน
    public static final byte[] GET_CID = { (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0x04, (byte) 0x02, (byte) 0x00, (byte) 0x0D };
    
    // คำสั่งอ่านชื่อ-สุกล (ไทย + อังกฤษ)
    public static final byte[] GET_NAME = { (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0x11, (byte) 0x02, (byte) 0x00, (byte) 0x64 };

    // คำสั่งอ่านวันเกิด
    public static final byte[] GET_DOB = { (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0xD9, (byte) 0x02, (byte) 0x00, (byte) 0x08 };

    // คำสั่งอ่านเพศ
    public static final byte[] GET_GENDER = { (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0xE1, (byte) 0x02, (byte) 0x00, (byte) 0x01 };

    // คำสั่งอ่านที่อยู่
    public static final byte[] GET_ADDRESS = { (byte) 0x80, (byte) 0xB0, (byte) 0x15, (byte) 0x79, (byte) 0x02, (byte) 0x00, (byte) 0x64 };

    // คำสั่งวันออกบัตร/วันหมดอายุ
    public static final byte[] GET_ISSUE_EXPIRE = { (byte) 0x80, (byte) 0xB0, (byte) 0x01, (byte) 0x67, (byte) 0x02, (byte) 0x00, (byte) 0x12 };

    // คำสั่งอ่านชื่อ-สุกล อังกฤษ
    public static final byte[] GET_NAME_EN = { (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0x75, (byte) 0x02, (byte) 0x00, (byte) 0x64 };

    // คำสั่งอ่านผู้ออกบัตร
    public static final byte[] GET_ISSUER = { (byte) 0x80, (byte) 0xB0, (byte) 0x00, (byte) 0xF6, (byte) 0x02, (byte) 0x00, (byte) 0x64 };

    // คำสั่งอ่านวันออกบัตร (แยกจากวันหมดอายุ)
    public static final byte[] GET_ISSUE_DATE = { (byte) 0x80, (byte) 0xB0, (byte) 0x01, (byte) 0x67, (byte) 0x02, (byte) 0x00, (byte) 0x08 };

    // คำสั่งอ่านวันหมดอายุบัตร (แยกจากวันออกบัตร)
    public static final byte[] GET_EXPIRE_DATE = { (byte) 0x80, (byte) 0xB0, (byte) 0x01, (byte) 0x6F, (byte) 0x02, (byte) 0x00, (byte) 0x08 };

    /**
     * สำหรับการอ่านรูปภาพ (Photo) จะต้องส่งเป็น Loop Command
     * เพื่อดึงข้อมูลทีละส่วน เนื่องจากข้อจำกัดของบัตรสมาร์ทการ์ด
     * โดยปกติจะอ่านทีละประมาณ 254 bytes
     */
}
