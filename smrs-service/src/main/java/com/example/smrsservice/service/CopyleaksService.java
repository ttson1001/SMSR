package com.example.smrsservice.service;

import com.example.smrsservice.config.CopyleaksClient;
import com.example.smrsservice.dto.copyleaks.CopyleaksTokenResponse;
import com.example.smrsservice.entity.PlagiarismResult;
import com.example.smrsservice.repository.PlagiarismResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CopyleaksService {

    private final CopyleaksClient client;
    private final ObjectMapper jacksonObjectMapper;
    private final PlagiarismResultRepository repo;

    @Value("${copyleaks.email}")
    private String email;

    @Value("${copyleaks.key}")
    private String apiKey;

    private String cachedToken;
    private Instant tokenExpiresAt;
    private Instant lastLoginAttempt;

    private final Map<String, Object> scanResults = new ConcurrentHashMap<>();

    public CopyleaksService(CopyleaksClient client, ObjectMapper jacksonObjectMapper, PlagiarismResultRepository repo) {
        this.client = client;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.repo = repo;
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
        try {
            String json = jacksonObjectMapper.writeValueAsString(payload);

            PlagiarismResult entity = new PlagiarismResult();
            entity.setScanId(scanId);
            entity.setStatus(status);
            entity.setPayload(json);
            entity.setReceivedAt(Instant.now());

            repo.save(entity);

        } catch (Exception e) {
            throw new RuntimeException("Save webhook failed", e);
        }
    }

    public PlagiarismResult getByScanId(String scanId) {
        return repo.findByScanId(scanId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "không tìm thấy Id: " + scanId
                        )
                );
    }

}
