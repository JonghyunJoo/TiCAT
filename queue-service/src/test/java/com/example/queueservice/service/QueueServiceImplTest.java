package com.example.queueservice.service;

import com.example.queueservice.dto.QueueStatusResponse;
import com.example.queueservice.entity.QueueToken;
import com.example.queueservice.vo.QueueStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueServiceImplTest {

    @Mock
    private RedisTemplate<String, QueueToken> redisTemplate;

    @Mock
    private ZSetOperations<String, QueueToken> zSetOperations;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private QueueServiceImpl queueService;

    private static final String WAIT_KEY = "WAIT_KEY";
    private static final String ACTIVE_KEY = "ACTIVE_KEY";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void shouldAddToActiveQueue_whenActiveQueueHasLessThan200Tokens() {
        // given: ACTIVE_KEY에 200개 미만의 토큰이 있을 때
        when(zSetOperations.size(ACTIVE_KEY)).thenReturn(199L);
        when(modelMapper.map(any(QueueToken.class), eq(QueueStatusResponse.class)))
                .thenReturn(new QueueStatusResponse());

        // when: addToQueue 메서드를 호출
        QueueStatusResponse response = queueService.addToQueue("user1", "flight1");

        // then: 토큰이 ACTIVE_KEY에 추가되고, 응답 상태는 active여야 함
        verify(zSetOperations).add(eq(ACTIVE_KEY), any(QueueToken.class), anyDouble());
        assertThat(response.getStatus()).isEqualTo("active");
    }

    @Test
    void shouldAddToWaitQueue_whenActiveQueueHas200OrMoreTokens() {
        // given: ACTIVE_KEY에 200개 이상의 토큰이 있을 때
        when(zSetOperations.size(ACTIVE_KEY)).thenReturn(200L);
        when(zSetOperations.rank(eq(WAIT_KEY), any(QueueToken.class))).thenReturn(0L);
        when(modelMapper.map(any(QueueToken.class), eq(QueueStatusResponse.class)))
                .thenReturn(new QueueStatusResponse());

        // when: addToQueue 메서드를 호출
        QueueStatusResponse response = queueService.addToQueue("user2", "flight2");

        // then: 토큰이 WAIT_KEY에 추가되고, 응답 상태는 waiting이어야 함
        verify(zSetOperations).add(eq(WAIT_KEY), any(QueueToken.class), anyDouble());
        assertThat(response.getStatus()).isEqualTo("waiting");
    }

    @Test
    void shouldReturnActiveStatus_whenTokenIsInActiveQueue() {
        // given: ACTIVE_KEY에 토큰이 존재하는 경우
        QueueToken activeToken = new QueueToken("user1", "flight1", System.currentTimeMillis(), System.currentTimeMillis() + 10000);
        when(zSetOperations.range(WAIT_KEY, 0, -1)).thenReturn(Set.of(activeToken));
        when(redisTemplate.opsForZSet().rank(ACTIVE_KEY, activeToken)).thenReturn(1L);
        when(modelMapper.map(activeToken, QueueStatusResponse.class)).thenReturn(new QueueStatusResponse());

        // when: getQueueStatus 호출
        QueueStatusResponse response = queueService.getQueueStatus("user1", "flight1");

        // then: 응답 상태는 active여야 함
        assertThat(response.getStatus()).isEqualTo("active");
    }

    @Test
    void shouldReturnQueueStatus_whenTokenIsInWaitQueue() {
        // given: WAIT_KEY에 토큰이 존재하고, 만료되지 않은 경우
        QueueToken waitingToken = new QueueToken("user1", "flight1", System.currentTimeMillis(), System.currentTimeMillis() + 10000);
        when(zSetOperations.range(WAIT_KEY, 0, -1)).thenReturn(Set.of(waitingToken));
        when(redisTemplate.opsForZSet().rank(ACTIVE_KEY, waitingToken)).thenReturn(null);
        when(redisTemplate.opsForZSet().rank(WAIT_KEY, waitingToken)).thenReturn(500L);
        when(modelMapper.map(waitingToken, QueueStatusResponse.class)).thenReturn(new QueueStatusResponse());

        // when: getQueueStatus 호출
        QueueStatusResponse response = queueService.getQueueStatus("user1", "flight1");

        // then: 응답 상태는 waiting이고, 남은 대기 시간이 계산되어야 함
        assertThat(response.getStatus()).isEqualTo("waiting");
        assertThat(response.getQueueStatus().getWaitingOrder()).isEqualTo(501L); // Rank는 0부터 시작하므로 +1
        assertThat(response.getQueueStatus().getRemainingTime()).isEqualTo(20);
    }

    @Test
    void shouldReturnExpiredStatusAndRemoveToken_whenTokenIsExpired() {
        // given: WAIT_KEY에 만료된 토큰이 존재하는 경우
        QueueToken expiredToken = new QueueToken("user1", "flight1", System.currentTimeMillis() - 20000, System.currentTimeMillis() - 10000);
        when(zSetOperations.range(WAIT_KEY, 0, -1)).thenReturn(Set.of(expiredToken));
        when(redisTemplate.opsForZSet().rank(ACTIVE_KEY, expiredToken)).thenReturn(null);
        when(modelMapper.map(expiredToken, QueueStatusResponse.class)).thenReturn(new QueueStatusResponse("expired"));

        // when: getQueueStatus 호출
        QueueStatusResponse response = queueService.getQueueStatus("user1", "flight1");

        // then: 응답 상태는 expired이고, 대기열에서 삭제되어야 함
        assertThat(response.getStatus()).isEqualTo("expired");
        verify(zSetOperations).remove(WAIT_KEY, expiredToken);
    }

    @Test
    void shouldReturnNotFoundStatus_whenTokenNotFound() {
        // given: 대기열에 토큰이 존재하지 않는 경우
        when(zSetOperations.range(WAIT_KEY, 0, -1)).thenReturn(Set.of());

        // when: getQueueStatus 호출
        QueueStatusResponse response = queueService.getQueueStatus("user1", "flight1");

        // then: 응답 상태는 not found여야 함
        assertThat(response.getStatus()).isEqualTo("not found");
    }


    @Test
    void shouldActivateTokensFromWaitQueueToActiveQueue() {
        // given: WAIT_KEY에 활성화 가능한 토큰이 있는 경우
        QueueToken token = new QueueToken("user1", "flight1", System.currentTimeMillis(), System.currentTimeMillis() + 10000);
        when(zSetOperations.range(WAIT_KEY, 0, 199)).thenReturn(Set.of(token));

        // when: activateTokens 메서드를 호출
        queueService.activateTokens();

        // then: 토큰이 WAIT_KEY에서 ACTIVE_KEY로 이동해야 함
        verify(zSetOperations).remove(WAIT_KEY, token);
        verify(zSetOperations).add(ACTIVE_KEY, token, token.getRequestTime());
    }

    @Test
    void shouldDeleteExpiredTokensFromAllQueues() {
        // given: WAIT_KEY와 ACTIVE_KEY에 만료된 토큰이 있는 경우
        QueueToken expiredToken = new QueueToken("user1", "flight1", System.currentTimeMillis() - 20000, System.currentTimeMillis() - 10000);
        when(zSetOperations.range(WAIT_KEY, 0, -1)).thenReturn(Set.of(expiredToken));
        when(zSetOperations.range(ACTIVE_KEY, 0, -1)).thenReturn(Set.of(expiredToken));

        // when: deleteExpiredTokens 메서드를 호출
        queueService.deleteExpiredTokens();

        // then: 만료된 토큰이 제거되어야 함
        verify(zSetOperations).remove(WAIT_KEY, expiredToken);
        verify(zSetOperations).remove(ACTIVE_KEY, expiredToken);
    }
}
