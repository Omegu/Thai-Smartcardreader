package com.idbridge;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class ThaiIdBridgeApplication {
    public static void main(String[] args) {
        // สำคัญที่สุด: ต้องเป็น false เพื่อให้ UI (AWT/Swing) รันควบคู่กับ Spring ได้
        System.setProperty("java.awt.headless", "false");

        new SpringApplicationBuilder(ThaiIdBridgeApplication.class)
                .run(args);
    }
}
