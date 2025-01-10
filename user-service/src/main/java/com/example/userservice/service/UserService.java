package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    void createUser(UserDto userDto);

    Iterable<User> getUserByAll();

    UserDto getUserDetailsByEmail(String userName);

    UserDto getUserById(Long id);
}
