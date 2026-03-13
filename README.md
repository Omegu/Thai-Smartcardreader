# 🇹🇭 Thai ID Card Reader Service

โปรแกรมอ่านข้อมูลจากบัตรประชาชน (Smart Card) ผ่านทาง API บนเครื่อง Local

![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?style=flat-square&logo=spring-boot)
![Platform](https://img.shields.io/badge/Platform-Windows%20x64-0078D4?style=flat-square&logo=windows)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

---

## 📋 สารบัญ

- [คุณสมบัติ](#-คุณสมบัติ)
- [Tech Stack](#-tech-stack)
- [ข้อกำหนดระบบ](#-ข้อกำหนดระบบ)
- [การติดตั้ง](#-การติดตั้ง)
- [วิธีใช้งาน](#-วิธีใช้งาน)
- [API Documentation](#-api-documentation)
- [System Tray Features](#-system-tray-features)
- [การ Build จาก Source Code](#-การ-build-source-code)
- [Troubleshooting](#-troubleshooting)
- [License](#-license)

---

## ✨ คุณสมบัติ

- 📖 **อ่านข้อมูลบัตรประชาชน** - ดึงข้อมูลชื่อ, นามสกุล, เลขบัตร, วันเกิด, ที่อยู่
- 🚀 **API บน Localhost** - เรียกใช้ผ่าน HTTP GET ได้ทันที
- 🎯 **System Tray Icon** - ควบคุมบริการจาก system tray (Start/Stop/Restart)
- 🔄 **Auto Hardware Recovery** - กู้คืนการเชื่อมต่อหลังจากถอด/เสียบ card reader
- 🛡️ **ปลอดภัย** - รันบนเครื่องท้องถิ่น ไม่ส่งข้อมูลออกภายนอก
- 🎨 **UI ทันสมัย** - ใช้ FlatLaf Look and Feel


ซอฟต์แวร์นี้เป็นเพียงเครื่องมือสำหรับอ่านข้อมูล ผู้ที่นำไปใช้งานต้องรับผิดชอบในการปฏิบัติตามกฎหมาย PDPA ด้วยตนเอง ผู้พัฒนาไม่รับผิดชอบต่อการนำข้อมูลไปใช้อย่างผิดกฎหมาย
---

## 🛠️ Tech Stack

### Backend
| Technology | Version | Description |
|------------|---------|-------------|
| **Java** | 17+ | Programming Language |
| **Spring Boot** | 3.2.0 | Web Framework |
| **JNA** | 5.14.0 | Java Native Access (เรียกใช้ WinSCard.dll) |
| **Lombok** | Latest | Code Generator |

### UI / Desktop
| Technology | Version | Description |
|------------|---------|-------------|
| **Java Swing/AWT** | Built-in | System Tray & Log Window |
| **FlatLaf** | 3.2.5 | Modern Look and Feel |

### Build Tools
| Technology | Version | Description |
|------------|---------|-------------|
| **Maven** | 3.8+ | Build & Dependency Management |
| **Launch4j** | 2.5.1 | Create Windows .exe with Icon |

### Platform Specific
| Technology | Description |
|------------|-------------|
| **Java SmartCardIO** | อ่านบัตรประชาชนผ่าน JDK |
| **WinSCard.dll** | Windows Smart Card API (ผ่าน JNA) |
| **TIS-620** | Thai Character Encoding |

---

## 💻 ข้อกำหนดระบบ

### ความต้องการขั้นต่ำ
- **OS:** Windows 10/11 (64-bit)
- **Java:** JDK/JRE 17 หรือสูงกว่า
- **RAM:** 512 MB (แนะนำ 1 GB+)
- **Storage:** 50 MB

### ฮาร์ดแวร์ที่รองรับ
- Smart Card Reader ที่รองรับมาตรฐาน PC/SC
- บัตรประชาชน (Smart Card)

---

## 📦 การติดตั้ง

### วิธีที่ 1: ดาวน์โหลดไฟล์ .exe (แนะนำ)

1. ดาวน์โหลด `ThaiIDBridge.exe` จาก [Releases](https://github.com/your-repo/thai-smartcardreader/releases)
2. วางไฟล์ในโฟลเดอร์ที่ต้องการ
3. ดับเบิ้ลคลิก `ThaiIDBridge.exe` เพื่อรัน

### วิธีที่ 2: Build จาก Source Code

```bash
# Clone repository
git clone https://github.com/your-repo/thai-smartcardreader.git
cd thai-smartcardreader

# Build ด้วย Maven
mvn -DskipTests clean package

# ไฟล์ .exe จะอยู่ที่ target/ThaiIDBridge.exe
```

---

## 🚀 วิธีใช้งาน

### 1. รันโปรแกรม

**แบบ .exe:**
```bash
target/ThaiIDBridge.exe
```

**แบบ JAR:**
```bash
java --add-opens java.smartcardio/sun.security.smartcardio=ALL-UNNAMED -jar target/thai-id-bridge-1.0.0.jar
```

**แบบ Maven:**
```bash
mvn spring-boot:run
```

### 2. ตรวจสอบสถานะ

โปรแกรมจะแสดง icon ใน system tray:
- 🟢 **สีเขียว** = Service กำลังทำงาน (Port 9000)
- 🔴 **สีแดง** = Service ถูกหยุด

### 3. เรียก API

เปิด Browser หรือใช้ cURL:
```bash
curl http://localhost:9000/api/read-card
```

---

## 📡 API Documentation

### Read Card Data

**Endpoint:**
```
GET /api/read-card
```

**Response (Success):**
```json
{
  "status": "success",
  "data": {
    "cid": "1100800123456",
    "titleTH": "นาย",
    "firstNameTH": "สมชาย",
    "lastNameTH": "ใจดี",
    "titleEN": "Mr.",
    "firstNameEN": "Somchai",
    "lastNameEN": "Jaidee",
    "birthDate": "01012530",
    "gender": "Male",
    "address": "123 ถ.สุขุมวิท แขวงคลองเตย เขตคลองเตย กทม 10110",
    "issueDate": "01012560",
    "expireDate": "01012570",
    "photoBase64": ""
  }
}
```

**Response (Error):**
```json
{
  "status": "error",
  "message": "Card read failed: No smart card detected"
}
```

### HTTP Status Codes
| Code | Description |
|------|-------------|
| 200 | อ่านข้อมูลสำเร็จ |
| 503 | Service ถูก Stop |
| 500 | เกิดข้อผิดพลาด (ไม่พบบัตร, reader ไม่พร้อม) |

---

## 🖥️ System Tray Features

คลิกขวาที่ icon ใน system tray เพื่อเข้าถึงเมนู:

| เมนู | คำอธิบาย |
|------|----------|
| **Start Service** | เริ่มบริการ (เมื่อถูก Stop) |
| **Stop Service** | หยุดบริการชั่วคราว |
| **Restart Hardware Connection** | กู้คืนการเชื่อมต่อหลังจากถอด/เสียบ reader |
| **Open Logs Viewer** | ดู Log การทำงาน |
| **Test Read Card** | ทดสอบอ่านบัตรด้วยตนเอง |
| **Exit Service** | ปิดโปรแกรม |

---

## 🔧 การ Build Source Code

### Prerequisites
- Java 17+
- Maven 3.8+

### Steps

```bash
# 1. Clone repository
git clone https://github.com/your-repo/thai-smartcardreader.git
cd thai-smartcardreader

# 2. Build JAR และ EXE
mvn -DskipTests clean package

# 3. ไฟล์ที่สร้าง
# - target/thai-id-bridge-1.0.0.jar
# - target/ThaiIDBridge.exe
```

### โครงสร้างโปรเจค
```
thai-smartcardreader/
├── src/main/java/com/idbridge/
│   ├── ThaiIdBridgeApplication.java    # Main Application
│   ├── controller/
│   │   └── CardController.java         # REST API Controller
│   ├── service/
│   │   ├── CardService.java            # Card Reading Logic
│   │   └── SmartCardReader.java        # Hardware Interface
│   ├── ui/
│   │   ├── TrayManagement.java         # System Tray Icon
│   │   └── LogWindow.java              # Log Viewer UI
│   ├── model/
│   │   └── PersonalData.java           # Data Model
│   └── util/
│       └── ThaiIdApdu.java             # APDU Commands
├── pom.xml                             # Maven Configuration
├── smart.png                           # Application Icon
└── README.md
```

---

## ❓ Troubleshooting

### ปัญหา: ไม่สามารถอ่านบัตรได้หลังจากถอด/เสียบ Reader

**วิธีแก้:**
1. คลิกขวาที่ system tray icon
2. เลือก **"Restart Hardware Connection"**
3. รอจน icon กลับเป็นสีเขียว

### ปัญหา: Service ไม่ทำงาน

**ตรวจสอบ:**
1. Java 17+ ติดตั้งแล้วหรือยัง
   ```bash
   java -version
   ```
2. Smart Card Reader ถูกต้องหรือไม่ (ตรวจสอบใน Device Manager)
3. มีโปรแกรมอื่นใช้งาน Reader อยู่หรือไม่

### ปัญหา: Icon ไม่แสดงใน System Tray

**วิธีแก้:**
- ตรวจสอบว่า Windows ไม่ได้ซ่อน icon
- คลิกที่ลูกศร ^ ใน system tray เพื่อแสดง icon ที่ซ่อน

### ปัญหา: EXE ไม่ทำงาน

**ตรวจสอบ:**
1. ติดตั้ง Java 17+ แล้ว
2. ตั้งค่า JAVA_HOME environment variable
3. รันแบบ Administrator (หากจำเป็น)

---

## 📄 License

MIT License - ดูรายละเอียดในไฟล์ [LICENSE](LICENSE)

---


<div align="center">

**Made with ❤️ for Thai ID Card Integration**

![Stars](https://img.shields.io/github/stars/your-repo/thai-smartcardreader?style=social)
![Forks](https://img.shields.io/github/forks/your-repo/thai-smartcardreader?style=social)

</div>
