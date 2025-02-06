package com.example.userservice.service;

import com.example.userservice.dto.UserRequestDto;
import com.example.userservice.dto.UserResponseDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    UserResponseDto createUser(UserRequestDto userRequestDto);

    List<UserResponseDto> getUserByAll();

    UserResponseDto getUserDetailsByEmail(String userName);

    UserResponseDto getUserById(Long id);
}
