# Thai ID Bridge - วิธีใช้งาน

## ไฟล์ที่สร้างแล้ว
- `target/ThaiIDBridge.exe` - ไฟล์ .exe พร้อม icon
- `target/thai-id-bridge-1.0.0.jar` - ไฟล์ JAR

---

## วิธีรันโปรแกรม

### วิธีที่ 1: ดับเบิ้ลคลิกไฟล์ .exe (แนะนำ)
```
target/ThaiIDBridge.exe
```
ไฟล์ .exe จะรันโดยอัตโนมัติพร้อม icon ของคุณ และรวม JVM arguments ที่จำเป็นทั้งหมด

### วิธีที่ 2: รันด้วย Java Command
```bash
java --add-opens java.smartcardio/sun.security.smartcardio=ALL-UNNAMED -jar target/thai-id-bridge-1.0.0.jar
```

### วิธีที่ 3: ใช้ Maven (สำหรับ Developer)
```bash
mvn spring-boot:run
```

---

## คุณสมบัติของไฟล์ .exe
- ✅ มี Icon เป็นรูปจาก `smart.png`
- ✅ รวม JVM arguments ที่จำเป็น (`--add-opens java.smartcardio/...`)
- ✅ ตรวจสอบ Java 17+ อัตโนมัติ
- ✅ รองรับ 64-bit เท่านั้น
- ✅ มี Version Info ถูกต้อง

---

## การ Build ใหม่
หากต้องการแก้ไขโค้ดและ build ใหม่:

```bash
mvn -DskipTests clean package
```

ไฟล์ .exe ใหม่จะถูกสร้างใน `target/ThaiIDBridge.exe`

---

## ระบบ Tray Icon
- 🟢 **ไอคอนสีเขียว** = Service กำลังทำงาน
- 🔴 **ไอคอนสีแดง** = Service ถูก Stop

คลิกขวาที่ icon ใน system tray เพื่อ:
- Start/Stop Service
- Restart Hardware Connection
- ดู Logs
- Test Read Card
- Exit Service

---

## API Endpoint
เมื่อ Service ทำงานแล้ว สามารถเรียกใช้ API ได้ที่:
```
GET http://localhost:9000/api/read-card
```
