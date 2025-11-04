package com.example.smrsservice.dto.project;

import lombok.Data;

import java.util.List;

@Data
public class InviteMemberRequest {
    private List<String> emails;
}
