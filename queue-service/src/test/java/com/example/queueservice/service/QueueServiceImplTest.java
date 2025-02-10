package com.example.queueservice.service;

import com.example.queueservice.dto.QueueResponseDto;
import com.example.queueservice.entity.Queue;
import com.example.queueservice.exception.CustomException;
import com.example.queueservice.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueServiceImplTest {

    @Mock
    private RedisTemplate<String, Queue> redisTemplate;

    @Mock
    private ZSetOperations<String, Queue> zSetOperations;

    @InjectMocks
    private QueueServiceImpl queueService;

    private final Long userId = 1L;
    private final Long concertScheduleId = 100L;
    private Queue queue;

    @BeforeEach
    void setUp() {
        queue = Queue.builder()
                .userId(userId)
                .concertScheduleId(concertScheduleId)
                .requestTime(System.currentTimeMillis())
                .build();
    }

    @Test
    void addToQueue_ShouldAddToActiveQueue_WhenCapacityAvailable() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.size("ACTIVE_KEY")).thenReturn(50L);
        when(zSetOperations.add(eq("ACTIVE_KEY"), any(Queue.class), anyDouble())).thenReturn(true);

        QueueResponseDto response = queueService.addToQueue(userId, concertScheduleId);

        assertNotNull(response);
        assertEquals("active", response.getStatus());
        verify(zSetOperations, times(1)).add(eq("ACTIVE_KEY"), any(Queue.class), anyDouble());
    }

    @Test
    void addToQueue_ShouldAddToWaitingQueue_WhenCapacityFull() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.size("ACTIVE_KEY")).thenReturn(200L); // 전체 용량 초과
        when(zSetOperations.add(eq("WAIT_KEY"), any(Queue.class), anyDouble())).thenReturn(true);
        lenient().when(zSetOperations.rank(eq("WAIT_KEY"), any(Queue.class))).thenReturn(1L);

        QueueResponseDto response = queueService.addToQueue(userId, concertScheduleId);

        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        assertEquals(2, response.getWaitingOrder());
    }

    @Test
    void getQueueStatus_ShouldReturnActiveStatus_WhenUserInActiveQueue() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        doReturn(Set.of(queue)).when(zSetOperations).range("ACTIVE_KEY", 0, -1);
        QueueResponseDto response = queueService.getQueueStatus(userId, concertScheduleId);

        assertNotNull(response);
        assertEquals("active", response.getStatus());
        verify(zSetOperations, times(1)).remove("ACTIVE_KEY", queue);
    }

    @Test
    void getQueueStatus_ShouldReturnWaitingStatus_WhenUserInWaitingQueue() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        doReturn(Set.of(queue)).when(zSetOperations).range("WAIT_KEY", 0, -1);
        when(zSetOperations.rank("WAIT_KEY", queue)).thenReturn(3L);
        doReturn(Set.of()).when(zSetOperations).range("ACTIVE_KEY", 0, -1);

        QueueResponseDto response = queueService.getQueueStatus(userId, concertScheduleId);

        assertNotNull(response);
        assertEquals("waiting", response.getStatus());
        assertEquals(4, response.getWaitingOrder());
    }


    @Test
    void getQueueStatus_ShouldThrowException_WhenUserNotFound() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        doReturn(Collections.emptySet()).when(zSetOperations).range("ACTIVE_KEY", 0, -1);  // ACTIVE_KEY에 사용자 없음
        doReturn(Collections.emptySet()).when(zSetOperations).range("WAIT_KEY", 0, -1);  // WAIT_KEY에 사용자 없음

        CustomException exception = assertThrows(CustomException.class, () ->
                queueService.getQueueStatus(userId, concertScheduleId));

        assertEquals(ErrorCode.TOKEN_NOT_FOUND, exception.getErrorCode());
    }
}


