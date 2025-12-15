package com.example.smrsservice.controller;

import com.example.smrsservice.service.CopyleaksService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/plagiarism")
@PreAuthorize("permitAll()")
public class CopyleaksController {

    private final CopyleaksService service;

    public CopyleaksController(CopyleaksService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public Map<String, Object> login() {
        return Map.of("token", service.getToken());
    }

    @PostMapping("/submit-url/{scanId}")
    public Map<String, Object> submitUrl(
            @PathVariable String scanId,
            @RequestBody Map<String, Object> body
    ) {
        long newScanId = Long.parseLong(scanId) + 10000;
        String finalScanId = String.valueOf(newScanId);

        String webhookUrl =
                "https://smrs.space/api/plagiarism/webhook/status/{STATUS}/" + finalScanId;

        Map<String, Object> properties = new HashMap<>();
        properties.put("sandbox", true);
        properties.put("webhooks", Map.of("status", webhookUrl));

        body.put("properties", properties);

        service.submitUrlScan(finalScanId, body);

        return Map.of("ok", true);
    }


    @PostMapping("/start/{scanId}")
    public Map<String, Object> start(@PathVariable String scanId) {
        service.startScan(scanId);
        return Map.of("ok", true);
    }

    @GetMapping("/result/{scanId}")
    public Object getResult(@PathVariable String scanId) {
        long newScanId = Long.parseLong(scanId) + 10000;
        String finalScanId = String.valueOf(newScanId);

        return service.getByScanId(finalScanId);
    }
}

