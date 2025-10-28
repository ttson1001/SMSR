package com.example.smrsservice.dto.account;
import com.example.smrsservice.common.AccountStatus;
import com.example.smrsservice.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateResponseDto {
    Integer accountId;
    String email;
    String name;
    Role role;
    AccountStatus status;
}
