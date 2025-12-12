package com.example.smrsservice.service;

import com.example.smrsservice.config.CopyleaksClient;
import com.example.smrsservice.dto.copyleaks.CopyleaksTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CopyleaksService {

    private final CopyleaksClient client;

    @Value("${copyleaks.email}")
    private String email;

    @Value("${copyleaks.key}")
    private String apiKey;

    private String cachedToken;
    private Instant tokenExpiresAt;
    private Instant lastLoginAttempt;

    private final Map<String, Object> scanResults = new ConcurrentHashMap<>();

    public CopyleaksService(CopyleaksClient client) {
        this.client = client;
    }

    public synchronized String getToken() {

        if (cachedToken != null && tokenExpiresAt != null && Instant.now().isBefore(tokenExpiresAt))
            return cachedToken;

        if (lastLoginAttempt != null &&
                Instant.now().minusSeconds(5).isBefore(lastLoginAttempt)) {
            throw new RuntimeException("Rate limit: wait 5 seconds");
        }

        lastLoginAttempt = Instant.now();

        CopyleaksTokenResponse res = client.login(email, apiKey);

        cachedToken = res.getAccess_token();
        tokenExpiresAt = Instant.now().plus(47, ChronoUnit.HOURS);

        return cachedToken;
    }

    public void submitScan(String scanId, Object body) {
        String token = getToken();
        client.submitScan(token, scanId, body);
    }

    public void startScan(String scanId) {
        String token = getToken();
        client.startScan(token, scanId);
    }

    public void submitUrlScan(String scanId, Object body) {
        String token = getToken();
        client.submitUrlScan(token, scanId, body);
    }

    public void saveWebhook(String scanId, String status, Object payload) {
        scanResults.put(scanId, Map.of(
                "status", status,
                "payload", payload,
                "receivedAt", Instant.now().toString()
        ));
    }

    public Object getScanResult(String scanId) {
        return scanResults.get(scanId);
    }
}
