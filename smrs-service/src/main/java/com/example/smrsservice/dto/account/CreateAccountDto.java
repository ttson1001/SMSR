package com.example.smrsservice.dto.account;
import com.example.smrsservice.entity.Role;
import lombok.Data;

@Data

public class CreateAccountDto {
    private String name;
    private String email;
    private String password;
    private String phone;
    private Integer age;
    private Role roleId;

}
