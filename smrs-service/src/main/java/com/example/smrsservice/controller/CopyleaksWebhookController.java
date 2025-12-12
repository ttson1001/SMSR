package com.example.smrsservice.controller;

import com.example.smrsservice.service.CopyleaksService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/plagiarism/webhook")
@PreAuthorize("permitAll()")
public class CopyleaksWebhookController {

    private final CopyleaksService service;

    public CopyleaksWebhookController(CopyleaksService service) {
        this.service = service;
    }

    @PostMapping("/status/{status}/{scanId}")
    public Map<String, Object> webhook(
            @PathVariable String status,
            @PathVariable String scanId,
            @RequestBody Map<String, Object> body
    ) {
        service.saveWebhook(scanId, status, body);
        return Map.of("ok", true);
    }
}
