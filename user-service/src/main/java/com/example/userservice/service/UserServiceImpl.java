package com.example.userservice.service;

import com.example.userservice.client.WalletServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.vo.ResponseUser;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final WalletServiceClient walletServiceClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);

        if (user == null)
            throw new UsernameNotFoundException(username + ": not found");

        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getEncryptedPwd(),
                true, true, true, true,
                new ArrayList<>());
    }

    @Override
    public void createUser(UserDto userDto) {

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        User user = mapper.map(userDto, User.class);
        user.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));

        userRepository.save(user);
    }

    @Override
    public UserDto getUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);

        User user = optionalUser.orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDto userDto = new ModelMapper().map(user, UserDto.class);

        Long balance = getBalanceWithCircuitBreaker(id);
        userDto.setBalance(balance);

        return userDto;
    }


    @CircuitBreaker(name = "walletService", fallbackMethod = "getBalanceFallback")
    private Long getBalanceWithCircuitBreaker(Long id) {
        log.info("Calling order-service for user: {}", id);
        return walletServiceClient.getBalance(id);
    }

    public Long getBalanceFallback(Long userId, Throwable throwable) {
        return 0L;
    }

    @Override
    public Iterable<User> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.error("User not found by email: {}", email);
            throw new UsernameNotFoundException(email);
        }

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        return mapper.map(user, UserDto.class);
    }
}
