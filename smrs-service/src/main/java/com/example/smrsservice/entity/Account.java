package com.example.smrsservice.entity;

import com.example.smrsservice.common.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "account")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;

    private String email;
    private String password;
    private String avatar;
    private String phone;
    private String name;
    private Integer age;

    @Column(name = "create_date")
    private Date createDate = new Date();

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private AccountStatus status;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
}
