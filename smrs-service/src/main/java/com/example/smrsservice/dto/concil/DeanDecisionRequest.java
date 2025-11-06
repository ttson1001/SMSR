package com.example.smrsservice.dto.concil;

import lombok.Data;

@Data
public class DeanDecisionRequest {
    private String decision;  // "APPROVED", "REJECTED"
    private String comment;
}
