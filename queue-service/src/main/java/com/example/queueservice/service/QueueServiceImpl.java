package com.example.queueservice.service;

import com.example.queueservice.dto.QueueResponseDto;
import com.example.queueservice.entity.Queue;
import com.example.queueservice.exception.CustomException;
import com.example.queueservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueServiceImpl implements QueueService {

    private final RedisTemplate<String, Queue> redisTemplate;

    private static final long EXPIRATION_TIME = 10;
    private static final int MAX_ACTIVE_TOKENS = 200;
    private static final String WAIT_KEY = "WAIT_KEY";
    private static final String ACTIVE_KEY = "ACTIVE_KEY";

    @Override
    public QueueResponseDto addToQueue(Long userId, Long concertScheduleId) {
        try {
            log.debug("addToQueue called for userId: {}, concertScheduleId: {}", userId, concertScheduleId);
            deleteTokens(userId);

            long currentTime = System.currentTimeMillis();

            Long activeTokenCount = redisTemplate.opsForZSet().size(ACTIVE_KEY);
            activeTokenCount = (activeTokenCount != null) ? activeTokenCount : 0L;

            Queue queue = Queue.builder()
                    .userId(userId)
                    .concertScheduleId(concertScheduleId)
                    .requestTime(currentTime)
                    .build();

            log.info("Active token count: {}, Max allowed active tokens: {}", activeTokenCount, MAX_ACTIVE_TOKENS);

            if (activeTokenCount < MAX_ACTIVE_TOKENS) {
                redisTemplate.opsForZSet().add(ACTIVE_KEY, queue, currentTime);
                redisTemplate.expire(ACTIVE_KEY, EXPIRATION_TIME, TimeUnit.MINUTES);

                log.info("User added to ACTIVE_KEY. UserId: {}", userId);
                return QueueResponseDto.builder()
                        .concertScheduleId(concertScheduleId)
                        .status("active")
                        .build();
            } else {
                redisTemplate.opsForZSet().add(WAIT_KEY, queue, currentTime);
                redisTemplate.expire(WAIT_KEY, EXPIRATION_TIME, TimeUnit.MINUTES);

                Long waitingOrder = redisTemplate.opsForZSet().rank(WAIT_KEY, queue);
                waitingOrder = (waitingOrder != null) ? waitingOrder : -1L;
                long waitTime = (waitingOrder + 1) * 1000;

                log.info("User added to WAIT_KEY. Waiting order: {}, Estimated wait time: {} seconds", waitingOrder + 1, waitTime / 1000);
                return QueueResponseDto.builder()
                        .concertScheduleId(concertScheduleId)
                        .status("waiting")
                        .waitingOrder(waitingOrder + 1)
                        .remainingTime(waitTime / 1000)
                        .build();
            }
        } catch (Exception e) {
            log.error("Error occurred while adding user to queue. UserId: {}, ConcertScheduleId: {}", userId, concertScheduleId, e);
            throw new CustomException(ErrorCode.QUEUE_ERROR);
        }
    }

    private Queue findTokenInQueue(Long userId, String key) {
        try {
            log.debug("Searching for token in queue. UserId: {}, Key: {}", userId, key);
            return Optional.ofNullable(redisTemplate.opsForZSet().range(key, 0, -1))
                    .orElse(Set.of())
                    .stream()
                    .filter(token -> token.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error occurred while searching for token in queue. UserId: {}, Key: {}", userId, key, e);
            throw new CustomException(ErrorCode.REDIS_ERROR);
        }
    }

    @Override
    public QueueResponseDto getQueueStatus(Long userId, Long concertScheduleId) {
        try {
            log.debug("getQueueStatus called for userId: {}, concertScheduleId: {}", userId, concertScheduleId);

            Queue token = findTokenInQueue(userId, ACTIVE_KEY);
            if (token != null) {
                redisTemplate.opsForZSet().remove(ACTIVE_KEY, token);
                log.info("User found in ACTIVE_KEY. Removing from ACTIVE_KEY. UserId: {}", userId);
                return QueueResponseDto.builder()
                        .concertScheduleId(concertScheduleId)
                        .status("active")
                        .build();
            }

            token = findTokenInQueue(userId, WAIT_KEY);
            if (token != null) {
                Long waitingOrder = redisTemplate.opsForZSet().rank(WAIT_KEY, token);
                waitingOrder = (waitingOrder != null) ? waitingOrder : -1L;
                long waitTime = ((waitingOrder / MAX_ACTIVE_TOKENS) * 10);

                log.info("User found in WAIT_KEY. Waiting order: {}, Estimated wait time: {} seconds", waitingOrder + 1, waitTime);
                return QueueResponseDto.builder()
                        .concertScheduleId(concertScheduleId)
                        .status("waiting")
                        .waitingOrder(waitingOrder + 1)
                        .remainingTime(waitTime)
                        .build();
            }

            log.warn("Token not found for userId: {} in both WAIT_KEY and ACTIVE_KEY", userId);
            throw new CustomException(ErrorCode.TOKEN_NOT_FOUND);
        } catch (Exception e) {
            log.error("Error occurred while retrieving queue status for userId: {}, concertScheduleId: {}", userId, concertScheduleId, e);
            throw new CustomException(ErrorCode.QUEUE_ERROR);
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void activateTokens() {
        try {
            log.info("Activating tokens...");

            long currentTime = System.currentTimeMillis();
            log.info("Current time: {}", currentTime);

            Long activeTokenCount = redisTemplate.opsForZSet().size(ACTIVE_KEY);
            activeTokenCount = (activeTokenCount != null) ? activeTokenCount : 0L;

            long tokensToActivateCount = Math.max(0, MAX_ACTIVE_TOKENS - activeTokenCount);

            log.info("Currently active tokens: {}, Tokens to activate: {}", activeTokenCount, tokensToActivateCount);

            if (tokensToActivateCount != 0) {
                Set<Queue> tokensToActivate = Optional.ofNullable(
                        redisTemplate.opsForZSet().range(WAIT_KEY, 0, tokensToActivateCount - 1)
                ).orElse(Set.of());

                log.info("Tokens to activate from WAIT_KEY: {}", tokensToActivate.size());

                for (Queue token : tokensToActivate) {
                    log.info("Activating token {} from WAIT_KEY to ACTIVE_KEY...", token);
                    redisTemplate.opsForZSet().remove(WAIT_KEY, token);
                    redisTemplate.opsForZSet().add(ACTIVE_KEY, token, token.getRequestTime());
                }

                log.info("Token activation process completed.");
            }

        } catch (Exception e) {
            log.error("Error during token activation process: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.ACTIVATE_ERROR);
        }
    }


    public void deleteTokens(Long userId) {
        try {
            log.debug("Deleting tokens for userId: {}", userId);

            Queue waitToken = findTokenInQueue(userId, WAIT_KEY);
            if (waitToken != null) {
                redisTemplate.opsForZSet().remove(WAIT_KEY, waitToken);
                log.info("Token removed from WAIT_KEY. UserId: {}", userId);
            }

            Queue activeToken = findTokenInQueue(userId, ACTIVE_KEY);
            if (activeToken != null) {
                redisTemplate.opsForZSet().remove(ACTIVE_KEY, activeToken);
                log.info("Token removed from ACTIVE_KEY. UserId: {}", userId);
            }
        } catch (Exception e) {
            log.error("Error occurred while deleting tokens for userId: {}", userId, e);
            throw new CustomException(ErrorCode.REDIS_ERROR);
        }
    }
}
