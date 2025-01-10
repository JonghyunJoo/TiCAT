package com.example.userservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private String pwd;
    private Date createdAt;

    private String decryptedPwd;

    private String encryptedPwd;

    private Long balance;

}
