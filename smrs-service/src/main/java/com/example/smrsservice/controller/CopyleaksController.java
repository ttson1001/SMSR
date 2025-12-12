package com.example.smrsservice.controller;

import com.example.smrsservice.service.CopyleaksService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/plagiarism")
public class CopyleaksController {

    private final CopyleaksService service;

    public CopyleaksController(CopyleaksService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public Map<String, Object> login() {
        return Map.of("token", service.getToken());
    }

    @PostMapping("/submit/{scanId}")
    public Map<String, Object> submit(
            @PathVariable String scanId,
            @RequestBody Map<String, Object> body
    ) {
        if (body.containsKey("url") && !body.containsKey("filename")) {
            throw new RuntimeException("For URL submission, 'filename' is required.");
        }

        String webhookUrl = "https://smrs.space/api/plagiarism/webhook/status/{STATUS}/" + scanId;

        Map<String, Object> properties = (Map<String, Object>) body.getOrDefault("properties", new HashMap<>());

        Map<String, Object> webhooks = new HashMap<>();
        webhooks.put("status", webhookUrl);

        properties.put("sandbox", true);
        properties.put("webhooks", webhooks);

        body.put("properties", properties);

        service.submitScan(scanId, body);

        return Map.of("ok", true);
    }


    @PostMapping("/start/{scanId}")
    public Map<String, Object> start(@PathVariable String scanId) {
        service.startScan(scanId);
        return Map.of("ok", true);
    }

    @GetMapping("/result/{scanId}")
    public Object getResult(@PathVariable String scanId) {
        return service.getScanResult(scanId);
    }
}

