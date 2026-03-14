package com.idbridge.service;

import com.idbridge.model.PersonalData;
import com.idbridge.ui.TrayManagement;
import com.idbridge.util.ThaiIdApdu;
import org.springframework.stereotype.Service;

import javax.smartcardio.Card;

@Service
public class CardService {

    private final SmartCardReader smartCardReader;
    private final TrayManagement trayManagement;
    private final StatsService statsService;
    private final WebhookService webhookService;

    // Cache for 3 seconds to avoid repeated reads of the same card
    private volatile PersonalData cachedData = null;
    private volatile long cacheTimestamp = 0;
    private static final long CACHE_TTL_MS = 3000L;

    public CardService(SmartCardReader smartCardReader, TrayManagement trayManagement,
                       StatsService statsService, WebhookService webhookService) {
        this.smartCardReader = smartCardReader;
        this.trayManagement = trayManagement;
        this.statsService = statsService;
        this.webhookService = webhookService;
    }

    public PersonalData readThaiId() throws Exception {
        // Check cache first (valid for 3 seconds)
        long now = System.currentTimeMillis();
        if (cachedData != null && (now - cacheTimestamp) < CACHE_TTL_MS) {
            trayManagement.log("Returning cached data (age: " + (now - cacheTimestamp) + "ms)");
            return cachedData;
        }

        Card card = null;
        statsService.incrementTotal();
        try {
            trayManagement.log("Connecting to card reader...");
            card = smartCardReader.connect();
            trayManagement.log("Connected to Thai ID card successfully.");

            // ดึงข้อมูล
            byte[] cidBytes = smartCardReader.sendCommand(card, ThaiIdApdu.GET_CID);
            String cid = parseString(cidBytes);

            byte[] nameBytes = smartCardReader.sendCommand(card, ThaiIdApdu.GET_NAME);
            String nameRaw = parseString(nameBytes);
            // โครงสร้างชื่อ: คำนำหน้าไทย#ชื่อไทย#นามสกุลไทย##คำนำหน้าอังกฤษ#ชื่ออังกฤษ#นามสกุลอังกฤษ
            String[] names = nameRaw.split("#");
            
            byte[] dobBytes = smartCardReader.sendCommand(card, ThaiIdApdu.GET_DOB);
            String dob = parseString(dobBytes);

            byte[] genderBytes = smartCardReader.sendCommand(card, ThaiIdApdu.GET_GENDER);
            String gender = parseString(genderBytes).equals("1") ? "Male" : "Female";
            
            byte[] addressBytes = smartCardReader.sendCommand(card, ThaiIdApdu.GET_ADDRESS);
            String address = parseString(addressBytes).replace("#", " ").trim();

            byte[] issueExpireBytes = smartCardReader.sendCommand(card, ThaiIdApdu.GET_ISSUE_EXPIRE);
            String issueExpireRaw = parseString(issueExpireBytes);
            String issueDate = issueExpireRaw.substring(0, 8);
            String expireDate = issueExpireRaw.substring(8, 16);

            // TODO: หากต้องการอ่านรูปภาพ ต้องใช้ลูปทะยอยดึงทีละ 254 byte แล้วรวมร่างแปลง base64 

            PersonalData data = new PersonalData();
            data.setCid(cid);
            data.setTitleTH(names.length > 0 ? names[0] : "");
            data.setFirstNameTH(names.length > 1 ? names[1] : "");
            data.setLastNameTH(names.length > 3 ? names[3] : "");
            data.setTitleEN(names.length > 5 ? names[5] : "");
            data.setFirstNameEN(names.length > 6 ? names[6] : "");
            data.setLastNameEN(names.length > 8 ? names[8] : "");
            data.setBirthDate(dob);
            data.setGender(gender);
            data.setAddress(address);
            data.setIssueDate(issueDate);
            data.setExpireDate(expireDate);
            data.setPhotoBase64(""); // ชั่วคราว ปล่อยว่าง

            statsService.incrementSuccess();
            // Update cache
            cachedData = data;
            cacheTimestamp = System.currentTimeMillis();

            // Send webhook asynchronously (don't wait for it)
            webhookService.sendCardReadEvent(data);

            return data;

        } catch (Exception e) {
            String error = "Card read failed: " + e.getMessage();
            trayManagement.log(error);
            statsService.incrementFailure(e.getClass().getSimpleName());
            throw e;
        } finally {
            if (card != null) {
                try {
                    card.disconnect(false);
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * แปลง byte array กลับเป็น String โดยใช้ TIS-620
     * เพราะข้อมูลในการ์ดเก็บในรูปแบบภาษาไทย TIS-620
     */
    private String parseString(byte[] data) throws Exception {
        if (data == null) return "";
        // ลบช่องว่างหรือ null byte ด้านหลังออก
        String result = new String(data, "TIS-620").trim();
        return result.replaceAll("\0", ""); 
    }
}
