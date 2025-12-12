package com.example.smrsservice.config;

import com.example.smrsservice.dto.copyleaks.CopyleaksTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class CopyleaksClient {

    @Value("${copyleaks.login-url}")
    private String loginUrl;

    @Value("${copyleaks.base-url}")
    private String apiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public CopyleaksTokenResponse login(String email, String apiKey) {

        Map<String, String> body = Map.of(
                "email", email,
                "key", apiKey
        );

        return restTemplate.postForObject(
                loginUrl,
                body,
                CopyleaksTokenResponse.class
        );
    }

    public void submitScan(String token, String scanId, Object payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(payload, headers);

        restTemplate.exchange(
                apiBaseUrl + "/scans/submit/url/" + scanId,
                HttpMethod.PUT,
                entity,
                Void.class
        );

    }

    public void submitUrlScan(String token, String scanId, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(
                apiBaseUrl + "/scans/submit/url/" + scanId,
                HttpMethod.PUT,
                entity,
                Void.class
        );
    }

    public void startScan(String token, String scanId) {

        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);

        restTemplate.postForEntity(
                apiBaseUrl + "/scans/" + scanId + "/start",
                new HttpEntity<>(h),
                Void.class
        );
    }
}

