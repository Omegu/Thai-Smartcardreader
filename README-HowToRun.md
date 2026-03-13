# Thai Smartcard Reader - การรันโปรแกรม

## วิธีรัน (ไม่ต้องใช้ไฟล์ bat ก็ได้)

### วิธีที่ 1: รันด้วยคำสั่ง Java โดยตรง (แนะนำ)
```bash
java --add-opens java.smartcardio/sun.security.smartcardio=ALL-UNNAMED -jar target/thai-id-bridge-1.0.0.jar
```

### วิธีที่ 2: รันด้วย Maven
```bash
mvn spring-boot:run
```

### วิธีที่ 3: ใช้ไฟล์ launch.bat (ถ้ามี)
```bash
launch.bat
```

## หมายเหตุสำคัญ

**ต้องเพิ่ม `--add-opens java.smartcardio/sun.security.smartcardio=ALL-UNNAMED`** เพื่อให้แอปพลิเคชันสามารถเข้าถึง smart card reader ได้หลังจากที่ถอดและเสียบ USB ใหม่

หากไม่เพิ่ม argument นี้ แอปพลิเคชันจะทำงานแต่ไม่สามารถอ่านบัตรได้หลังจากถอด/เสียบ card reader

## การ build โปรแกรม
```bash
mvn -DskipTests package
```
