package com.example.userservice.service;

import com.example.userservice.client.WalletServiceClient;
import com.example.userservice.dto.UserRequestDto;
import com.example.userservice.dto.UserResponseDto;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final WalletServiceClient walletServiceClient;
    private final ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getEncryptedPwd(),
                true, true, true, true,
                new ArrayList<>());
    }

    @Override
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        User user = User.builder()
                .name(userRequestDto.getName())
                .email(userRequestDto.getEmail())
                .encryptedPwd(passwordEncoder.encode(userRequestDto.getPwd()))
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        walletServiceClient.createWallet(user.getId());
        return modelMapper.map(user, UserResponseDto.class);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserResponseDto userResponseDto = modelMapper.map(user, UserResponseDto.class);

        Long balance = walletServiceClient.getBalance(id);
        userResponseDto.setBalance(balance);

        return userResponseDto;
    }

    @Override
    public List<UserResponseDto> getUserByAll() {
        Iterable<User> userList = userRepository.findAll();

        List<UserResponseDto> userResponseDtoList = new ArrayList<>();
        userList.forEach(v -> userResponseDtoList.add(modelMapper.map(v, UserResponseDto.class)));
        return userResponseDtoList;
    }

    @Override
    public UserResponseDto getUserDetailsByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return modelMapper.map(user, UserResponseDto.class);
    }
}
