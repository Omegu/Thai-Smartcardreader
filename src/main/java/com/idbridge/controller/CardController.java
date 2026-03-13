package com.idbridge.controller;

import com.idbridge.model.PersonalData;
import com.idbridge.service.CardService;
import com.idbridge.ui.TrayManagement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CardController {

    private final CardService cardService;
    private final TrayManagement trayManagement;
    private boolean isServiceRunning = true; // สถานะของระบบ Start/Stop

    public CardController(CardService cardService, TrayManagement trayManagement) {
        this.cardService = cardService;
        this.trayManagement = trayManagement;
    }

    public boolean isServiceRunning() {
        return isServiceRunning;
    }

    public void setServiceRunning(boolean serviceRunning) {
        this.isServiceRunning = serviceRunning;
    }

    @GetMapping("/read-card")
    public ResponseEntity<?> readCard() {
        if (!isServiceRunning) {
            trayManagement.log("API: Request rejected (Service is stopped).");
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("status", "error");
            errorResp.put("message", "Service is currently stopped manually. Please start via System Tray.");
            return ResponseEntity.status(503).body(errorResp);
        }

        trayManagement.log("API: Recieved request to read smart card...");
        try {
            PersonalData data = cardService.readThaiId();
            
            // ส่ง JSON กลับเป็นรูปแบบ response { "status": "success", "data": {...} }
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", data);
            
            trayManagement.log("API: Card read successfully.");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // ในกรณีไม่ได้เสียบบัตร, เครื่องอ่านไม่มี ฯลฯ
            trayManagement.log("Error: " + e.getMessage());
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("status", "error");
            errorResp.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResp);
        }
    }
}
