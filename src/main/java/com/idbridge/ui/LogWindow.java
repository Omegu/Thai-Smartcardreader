package com.idbridge.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogWindow extends JFrame {
    private final JTextArea logArea;

    public LogWindow() {
        FlatDarkLaf.setup(); // ใช้ Dark Theme ของ FlatLaf
        setTitle("ID Bridge Service - Activity Logs");
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); // ปิดหน้าต่างแล้วให้ซ่อนไว้ ไม่ได้ปิดโปรแกรม
        setLocationRelativeTo(null); // ให้หน้าต่างขึ้นมาตรงกลางจอ

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(25, 25, 25)); // สีพื้นหลังเทาเข้ม
        logArea.setForeground(new Color(153, 204, 255)); // สีตัวอักษรฟ้าอ่อน อ่านง่าย
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        logArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        // แถบสถานะด้านล่าง
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Status: Running on port 9000"));
        add(statusPanel, BorderLayout.SOUTH);
    }

    // เมธอดสำหรับเพิ่มข้อความลงใน Log Window
    public void log(String message) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + time + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength()); // เลื่อนสกอร์บาร์ลงล่างอัตโนมัติ
        });
    }
}
