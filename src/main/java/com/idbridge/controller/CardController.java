package com.idbridge.controller;

import com.idbridge.model.PersonalData;
import com.idbridge.service.CardService;
import com.idbridge.service.RateLimitService;
import com.idbridge.service.StatsService;
import com.idbridge.ui.TrayManagement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CardController {

    private final CardService cardService;
    private final TrayManagement trayManagement;
    private final StatsService statsService;
    private final RateLimitService rateLimitService;
    private boolean isServiceRunning = true; // สถานะของระบบ Start/Stop

    public CardController(CardService cardService, TrayManagement trayManagement,
                          StatsService statsService, RateLimitService rateLimitService) {
        this.cardService = cardService;
        this.trayManagement = trayManagement;
        this.statsService = statsService;
        this.rateLimitService = rateLimitService;
    }

    public boolean isServiceRunning() {
        return isServiceRunning;
    }

    public void setServiceRunning(boolean serviceRunning) {
        this.isServiceRunning = serviceRunning;
    }

    @GetMapping("/read-card")
    public ResponseEntity<?> readCard(@RequestHeader(value = "X-Forwarded-For", required = false) String xForwardedFor,
                                      @RequestHeader(value = "X-Real-IP", required = false) String xRealIp,
                                      HttpServletRequest request) {
        if (!isServiceRunning) {
            trayManagement.log("API: Request rejected (Service is stopped).");
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("status", "error");
            errorResp.put("message", "Service is currently stopped manually. Please start via System Tray.");
            return ResponseEntity.status(503).body(errorResp);
        }

        // Rate limiting - get client IP
        String clientIp = extractClientIp(xForwardedFor, xRealIp, request);
        if (!rateLimitService.tryConsume(clientIp)) {
            trayManagement.log("API: Rate limit exceeded for IP: " + clientIp);
            Map<String, Object> errorResp = new HashMap<>();
            errorResp.put("status", "error");
            errorResp.put("message", "Rate limit exceeded. Please try again later.");
            return ResponseEntity.status(429).body(errorResp);
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

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", statsService.getStats());
        return ResponseEntity.ok(response);
    }

    /**
     * Extract client IP address from headers or request
     */
    private String extractClientIp(String xForwardedFor, String xRealIp, HttpServletRequest request) {
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
