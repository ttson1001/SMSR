package com.example.smrsservice.dto.account;

import com.example.smrsservice.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDetailResponse {
    private Integer id;
    private String email;
    private String avatar;
    private String phone;
    private String name;
    private Integer age;
    private String status;
    private Role role;
    private boolean locked;
}
