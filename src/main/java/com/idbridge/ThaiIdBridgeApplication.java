package com.idbridge;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;

@SpringBootApplication
public class ThaiIdBridgeApplication {
    public static void main(String[] args) {
        // สำคัญที่สุด: ต้องเป็น false เพื่อให้ UI (AWT/Swing) รันควบคู่กับ Spring ได้
        System.setProperty("java.awt.headless", "false");

        // ตรวจสอบว่ามี --add-opens หรือไม่ หากไม่มีจะเตือนผู้ใช้
        if (!hasAddOpensArgument()) {
            System.out.println("WARNING: Missing --add-opens java.smartcardio/sun.security.smartcardio=ALL-UNNAMED");
            System.out.println("The application may not work properly with smart card readers.");
            System.out.println("Please run with: java --add-opens java.smartcardio/sun.security.smartcardio=ALL-UNNAMED -jar thai-id-bridge-1.0.0.jar");
        }

        new SpringApplicationBuilder(ThaiIdBridgeApplication.class)
                .run(args);
    }

    private static boolean hasAddOpensArgument() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        List<String> inputArguments = runtimeMXBean.getInputArguments();
        for (String arg : inputArguments) {
            if (arg.contains("add-opens") && arg.contains("java.smartcardio")) {
                return true;
            }
        }
        return false;
    }
}
