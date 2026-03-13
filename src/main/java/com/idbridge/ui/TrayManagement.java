package com.idbridge.ui;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import com.idbridge.service.CardService;

import java.awt.*;
import java.awt.image.BufferedImage;

@Component
public class TrayManagement {
    private final LogWindow logWindow = new LogWindow();
    private final ApplicationContext applicationContext;
    private TrayIcon trayIcon;
    private Image greenIcon;
    private Image redIcon;

    public TrayManagement(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        createTrayIcon();
        log("Service Initialized and ready to use.");
    }

    private void createTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.err.println("SystemTray is not supported on this platform.");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();

        // สร้าง Icon เขียว (Running) และ แดง (Stopped)
        greenIcon = createDefaultIcon();
        redIcon = createStoppedIcon();

        PopupMenu menu = new PopupMenu();

        MenuItem statusItem = new MenuItem("Status: Running on Port 9000");
        statusItem.setEnabled(false); // ให้เป็นแค่ข้อความบอกสถานะ

        MenuItem startItem = new MenuItem("Start Service");
        MenuItem stopItem = new MenuItem("Stop Service");
        MenuItem restartItem = new MenuItem("Restart Hardware Connection");

        startItem.setEnabled(false); // เริ่มต้นมามัน Start อยู่แล้ว

        MenuItem logItem = new MenuItem("Open Logs Viewer");
        logItem.addActionListener(e -> logWindow.setVisible(true));

        MenuItem testReadItem = new MenuItem("Test Read Card");
        testReadItem.addActionListener(e -> {
            logWindow.setVisible(true);
            try {
                // Get Controller
                com.idbridge.controller.CardController api = applicationContext.getBean(com.idbridge.controller.CardController.class);
                if (!api.isServiceRunning()) {
                    log("Test failed: Service is currently STOPPED.");
                    return;
                }

                log("Manual test triggered from system tray...");
                CardService cardService = applicationContext.getBean(CardService.class);
                com.idbridge.model.PersonalData data = cardService.readThaiId();
                log("Read Success: " + data.getFirstNameTH() + " " + data.getLastNameTH() + " (" + data.getCid() + ")");
            } catch (Exception ex) {
                log("Test failed: " + ex.getMessage());
            }
        });

        // ------------------ Actions ------------------ //
        startItem.addActionListener(e -> {
            applicationContext.getBean(com.idbridge.controller.CardController.class).setServiceRunning(true);
            log("Service has been STARTED.");
            statusItem.setLabel("Status: Running on Port 9000");
            startItem.setEnabled(false);
            stopItem.setEnabled(true);
            testReadItem.setEnabled(true);
            // เปลี่ยนไอคอนเป็นสีเขียว
            trayIcon.setImage(greenIcon);
        });

        stopItem.addActionListener(e -> {
            applicationContext.getBean(com.idbridge.controller.CardController.class).setServiceRunning(false);
            log("Service has been STOPPED.");
            statusItem.setLabel("Status: Stopped");
            startItem.setEnabled(true);
            stopItem.setEnabled(false);
            testReadItem.setEnabled(false);
            // เปลี่ยนไอคอนเป็นสีแดง
            trayIcon.setImage(redIcon);
        });

        restartItem.addActionListener(e -> {
            logWindow.setVisible(true);
            log("Restarting service and clearing hardware cache...");

            com.idbridge.controller.CardController api = applicationContext.getBean(com.idbridge.controller.CardController.class);
            api.setServiceRunning(false);

            statusItem.setLabel("Status: Restarting...");
            startItem.setEnabled(false);
            stopItem.setEnabled(false);
            testReadItem.setEnabled(false);
            // เปลี่ยนไอคอนเป็นสีแดงชั่วคราว
            trayIcon.setImage(redIcon);

            new Thread(() -> {
                try {
                    log("Releasing smart card native context...");
                    applicationContext.getBean(com.idbridge.service.SmartCardReader.class).refreshHardwareConnection();

                    // แฮ็กสำหรับเคลียร์ TerminalFactory Object Cache
                    System.gc();
                    Thread.sleep(1000); // รอเคลียร์

                    api.setServiceRunning(true);
                    log("Hardware context has been refreshed.");
                    log("Service is back online.");

                    statusItem.setLabel("Status: Running on Port 9000");
                    startItem.setEnabled(false);
                    stopItem.setEnabled(true);
                    testReadItem.setEnabled(true);
                    // เปลี่ยนไอคอนกลับเป็นสีเขียว
                    trayIcon.setImage(greenIcon);
                } catch (Exception ex) {
                    log("Restart Failed: " + ex.getMessage());
                }
            }).start();
        });
        // --------------------------------------------- //

        MenuItem exitItem = new MenuItem("Exit Service");
        exitItem.addActionListener(e -> {
            log("Shutting down service...");
            System.exit(0);
        });

        menu.add(statusItem);
        menu.addSeparator();
        menu.add(startItem);
        menu.add(stopItem);
        menu.add(restartItem);
        menu.addSeparator();
        menu.add(logItem);
        menu.add(testReadItem);
        menu.addSeparator();
        menu.add(exitItem);

        trayIcon = new TrayIcon(greenIcon, "Thai ID Bridge", menu);
        trayIcon.setImageAutoSize(true);
        // หากคลิกซ้ายสองครั้ง ให้เปิดหน้า Log ด้วย
        trayIcon.addActionListener(e -> logWindow.setVisible(true));

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private Image createDefaultIcon() {
        // สร้างภาพ 16x16 พิกเซล เป็นวงกลมสีเขียว
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        // ใส่ Anti-Aliasing ให้ขอบเนียน
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 200, 0)); // สีเขียวสว่าง
        g2.fillOval(1, 1, 14, 14);

        g2.setColor(new Color(0, 100, 0)); // ขอบสีเขียวเข้ม
        g2.drawOval(1, 1, 14, 14);

        g2.dispose();
        return img;
    }

    private Image createStoppedIcon() {
        // สร้างภาพ 16x16 พิกเซล เป็นวงกลมสีแดง (สถานะ Stop)
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();

        // ใส่ Anti-Aliasing ให้ขอบเนียน
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(200, 0, 0)); // สีแดงสว่าง
        g2.fillOval(1, 1, 14, 14);

        g2.setColor(new Color(100, 0, 0)); // ขอบสีแดงเข้ม
        g2.drawOval(1, 1, 14, 14);

        g2.dispose();
        return img;
    }

    // เมธอดสำหรับเรียกใช้เพื่อบันทึก Log ลงไปในหน้าต่าง UI
    public void log(String msg) {
        logWindow.log(msg);
        System.out.println(msg); // แสดงใน Console เผื่อรันผ่าน IDE ด้วย
    }
}
