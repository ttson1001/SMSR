package com.example.smrsservice.dto.concil;

import com.example.smrsservice.common.DecisionStatus;
import lombok.Data;

@Data
public class DeanDecisionRequest {
    private DecisionStatus decision;  // "APPROVED", "REJECTED"
    private String comment;
}
