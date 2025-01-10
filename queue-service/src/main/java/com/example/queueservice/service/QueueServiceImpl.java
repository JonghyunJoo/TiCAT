package com.example.queueservice.service;

import com.example.queueservice.dto.QueueStatusResponse;
import com.example.queueservice.entity.QueueToken;
import com.example.queueservice.vo.QueueStatus;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final RedisTemplate<String, QueueToken> redisTemplate;
    private final ModelMapper modelMapper;

    private static final long EXPIRATION_TIME = 10000; // 10초
    private static final String WAIT_KEY = "WAIT_KEY";
    private static final String ACTIVE_KEY = "ACTIVE_KEY";

    @Override
    public QueueStatusResponse addToQueue(String userId, String flightId) {
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + EXPIRATION_TIME;

        // 먼저 active 토큰 개수를 확인하여, 200개 이하이면 바로 접속
        long activeTokenCount = redisTemplate.opsForZSet().size(ACTIVE_KEY);

        QueueToken queueToken = new QueueToken(userId, flightId, currentTime, expirationTime);
        QueueStatusResponse response;

        if (activeTokenCount < 200) {
            redisTemplate.opsForZSet().add(ACTIVE_KEY, queueToken, currentTime);
            response = modelMapper.map(queueToken, QueueStatusResponse.class);
            response.setStatus("active");
        } else {
            redisTemplate.opsForZSet().add(WAIT_KEY, queueToken, currentTime);
            long waitingOrder = redisTemplate.opsForZSet().rank(WAIT_KEY, queueToken);
            long waitTime = (waitingOrder + 1) * 1000;
            response = modelMapper.map(queueToken, QueueStatusResponse.class);
            response.setStatus("waiting");
            response.setQueueStatus(new QueueStatus(waitingOrder + 1, waitTime / 1000));
        }
        return response;
    }

    @Override
    public QueueStatusResponse getQueueStatus(String userId, String flightId) {
        Set<QueueToken> tokens = redisTemplate.opsForZSet().range(WAIT_KEY, 0, -1);

        QueueToken token = tokens.stream()
                .filter(t -> t.getUserId().equals(userId))
                .findFirst()
                .orElse(null);

        if (token != null) {
            long currentTime = System.currentTimeMillis();

            // 대기열에서 ACTIVE 상태인지 확인
            if (redisTemplate.opsForZSet().rank(ACTIVE_KEY, token) != null) {
                QueueStatusResponse response = modelMapper.map(token, QueueStatusResponse.class);
                response.setStatus("active");
                return response;
            }

            long waitingOrder = redisTemplate.opsForZSet().rank(WAIT_KEY, token);
            long waitTime = (waitingOrder + 1) * 1000;

            // 만료된 토큰이라면 대기열에서 삭제
            if (currentTime > token.getExpirationTime()) {
                redisTemplate.opsForZSet().remove(WAIT_KEY, token);
                QueueStatusResponse response = modelMapper.map(token, QueueStatusResponse.class);
                response.setStatus("expired");
                return response;
            }

            // WAIT 상태라면 대기 순번과 남은 대기 시간 반환
            QueueStatusResponse response = modelMapper.map(token, QueueStatusResponse.class);
            response.setStatus("waiting");
            response.setQueueStatus(new QueueStatus(waitingOrder + 1, waitTime / 1000));
            return response;
        }

        return new QueueStatusResponse("not found");
    }

    @Scheduled(fixedDelay = 10000) // 10초마다 실행
    public void activateTokens() {
        long currentTime = System.currentTimeMillis();

        Set<QueueToken> tokensToActivate = redisTemplate.opsForZSet().range(WAIT_KEY, 0, 199); // 200개까지 조회
        for (QueueToken token : tokensToActivate) {
            if (currentTime > token.getExpirationTime()) {
                redisTemplate.opsForZSet().remove(WAIT_KEY, token);
                continue;
            }

            // 대기열에서 활성화
            redisTemplate.opsForZSet().remove(WAIT_KEY, token);  // 대기열에서 제거
            redisTemplate.opsForZSet().add(ACTIVE_KEY, token, token.getRequestTime());  // ACTIVE_KEY로 추가
        }
    }

    @Scheduled(fixedDelay = 10000) // 10초마다 실행
    public void deleteExpiredTokens() {
        long currentTime = System.currentTimeMillis();

        // 1. 대기열에서 만료된 토큰 삭제
        Set<QueueToken> waitingTokens = redisTemplate.opsForZSet().range(WAIT_KEY, 0, -1); // WAIT_KEY에서 모든 토큰 조회
        for (QueueToken token : waitingTokens) {
            if (currentTime > token.getExpirationTime()) {
                redisTemplate.opsForZSet().remove(WAIT_KEY, token);
            }
        }

        // 2. 활성화된 토큰에서 만료된 토큰 삭제
        Set<QueueToken> activeTokens = redisTemplate.opsForZSet().range(ACTIVE_KEY, 0, -1); // ACTIVE_KEY에서 모든 토큰 조회
        for (QueueToken token : activeTokens) {
            if (currentTime > token.getExpirationTime()) {
                redisTemplate.opsForZSet().remove(ACTIVE_KEY, token);
            }
        }
    }
}

