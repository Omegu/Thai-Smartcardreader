package com.idbridge.service;

import com.idbridge.model.PersonalData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Webhook service to send notifications on card read events
 */
@Service
public class WebhookService {

    @Value("${webhook.enabled:false}")
    private boolean webhookEnabled;

    @Value("${webhook.url:}")
    private String webhookUrl;

    @Value("${webhook.secret:}")
    private String webhookSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send webhook notification when card is read successfully
     * @param data PersonalData from the card
     */
    public void sendCardReadEvent(PersonalData data) {
        if (!webhookEnabled || webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                "event", "card.read",
                "timestamp", System.currentTimeMillis(),
                "data", Map.of(
                    "cid", data.getCid(),
                    "firstNameTH", data.getFirstNameTH(),
                    "firstNameEN", data.getFirstNameEN(),
                    "birthDate", data.getBirthDate()
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (webhookSecret != null && !webhookSecret.isEmpty()) {
                headers.set("X-Webhook-Secret", webhookSecret);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(webhookUrl, request, Void.class);
        } catch (Exception e) {
            // Log but don't throw - webhook failure shouldn't break the main flow
            System.err.println("Webhook failed: " + e.getMessage());
        }
    }
}
