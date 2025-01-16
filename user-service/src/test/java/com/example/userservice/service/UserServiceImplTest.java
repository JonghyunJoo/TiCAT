package com.example.userservice.service;

import com.example.userservice.client.WalletServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletServiceClient walletServiceClient;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .encryptedPwd("encryptedPwd")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .pwd("password")
                .createdAt(new Date())
                .decryptedPwd("password")
                .encryptedPwd("encryptedPwd")
                .balance(100L)
                .build();
    }


    /**
     * 사용자가 존재하지 않으면 예외가 발생하는지 테스트.
     *
     * 사용자가 존재하지 않으면 UsernameNotFoundException이 발생해야 하는지 확인.
     */
    @Test
    void shouldThrowException_whenUserNotFound() {
        // Given: 존재하지 않는 사용자 이메일
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        // When / Then: 사용자 없으면 예외 발생
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nonexistent@example.com"));
        assertEquals("nonexistent@example.com: not found", exception.getMessage());
    }

    /**
     * 사용자 생성이 정상적으로 처리되는지 테스트.
     *
     * 주어진 UserDto를 바탕으로 사용자가 생성되고, UserRepository에 저장되는지 확인.
     */
    @Test
    void shouldCreateUser_whenValidUserDto() {
        // Given: 유효한 UserDto
        when(passwordEncoder.encode(userDto.getPwd())).thenReturn("encodedPassword");

        // When: createUser 메서드 호출
        userService.createUser(userDto);

        // Then: UserRepository에 저장된 사용자 확인
        verify(userRepository, times(1)).save(any(User.class));
    }

    /**
     * 주어진 ID로 사용자를 찾을 수 있는지 테스트.
     *
     * 사용자가 존재하면 UserDto로 변환되어 반환되어야 하고, 잔액도 포함되어야 함.
     */
    @Test
    void shouldGetUserById_whenUserExists() {
        // Given: 존재하는 사용자 ID
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(walletServiceClient.getBalance(1L)).thenReturn(100L);

        // When: getUserById 메서드 호출
        UserDto result = userService.getUserById(1L);

        // Then: 사용자 정보와 잔액을 포함한 UserDto가 반환되어야 함
        assertNotNull(result);
        assertEquals(100L, result.getBalance());
    }

    /**
     * 주어진 ID로 사용자가 존재하지 않으면 예외가 발생하는지 테스트.
     *
     * 사용자가 없으면 UsernameNotFoundException이 발생해야 함.
     */
    @Test
    void shouldThrowException_whenUserNotFoundById() {
        // Given: 존재하지 않는 사용자 ID
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        // When / Then: 사용자 없으면 예외 발생
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserById(1L));
        assertEquals("User not found", exception.getMessage());
    }

    /**
     * 이메일로 사용자 정보를 가져오는 테스트.
     *
     * 이메일로 사용자 정보를 가져오고, UserDto로 변환된 결과를 반환하는지 확인.
     */
    @Test
    void shouldGetUserDetailsByEmail_whenUserExists() {
        // Given: 이메일로 사용자 정보가 존재하는 경우
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        // When: getUserDetailsByEmail 메서드 호출
        UserDto result = userService.getUserDetailsByEmail("test@example.com");

        // Then: 사용자 정보가 UserDto로 반환되어야 함
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    /**
     * 이메일로 사용자가 존재하지 않으면 예외가 발생하는지 테스트.
     *
     * 이메일로 사용자가 없으면 UsernameNotFoundException이 발생해야 하는지 확인.
     */
    @Test
    void shouldThrowException_whenUserNotFoundByEmail() {
        // Given: 존재하지 않는 이메일
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        // When / Then: 사용자 없으면 예외 발생
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserDetailsByEmail("nonexistent@example.com"));
        assertEquals("nonexistent@example.com", exception.getMessage());
    }
}
