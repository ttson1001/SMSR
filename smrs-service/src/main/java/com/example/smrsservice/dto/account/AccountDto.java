package com.example.smrsservice.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Integer age;
    private String gender;
    private String address;
    private String role;
}
