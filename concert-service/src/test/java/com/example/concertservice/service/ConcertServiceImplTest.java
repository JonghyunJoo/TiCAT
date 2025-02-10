package com.example.concertservice.service;

import com.example.concertservice.dto.*;
import com.example.concertservice.entity.Concert;
import com.example.concertservice.exception.CustomException;
import com.example.concertservice.exception.ErrorCode;
import com.example.concertservice.repository.ConcertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConcertServiceImplTest {

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ConcertServiceImpl concertService;

    private Concert concert;
    private ConcertRequestDto concertRequestDto;
    private ConcertResponseDto concertResponseDto;

    @BeforeEach
    void setUp() {
        concert = new Concert();
        concert.setId(1L);
        concert.setTitle("Test Concert");

        concertRequestDto = new ConcertRequestDto();
        concertRequestDto.setTitle("Test Concert");

        concertResponseDto = new ConcertResponseDto();
        concertResponseDto.setId(1L);
        concertResponseDto.setTitle("Test Concert");
    }

    @Test
    void createConcert_ShouldReturnConcertResponseDto() {
        when(modelMapper.map(concertRequestDto, Concert.class)).thenReturn(concert);
        when(concertRepository.save(concert)).thenReturn(concert);
        when(modelMapper.map(concert, ConcertResponseDto.class)).thenReturn(concertResponseDto);

        ConcertResponseDto result = concertService.createConcert(concertRequestDto);

        assertNotNull(result);
        assertEquals("Test Concert", result.getTitle());
        verify(concertRepository, times(1)).save(concert);
    }

    @Test
    void getConcertById_WhenConcertExists_ShouldReturnConcertResponseDto() {
        when(concertRepository.findById(1L)).thenReturn(Optional.of(concert));
        when(modelMapper.map(concert, ConcertResponseDto.class)).thenReturn(concertResponseDto);

        ConcertResponseDto result = concertService.getConcertById(1L);

        assertNotNull(result);
        assertEquals("Test Concert", result.getTitle());
        verify(concertRepository, times(1)).findById(1L);
    }

    @Test
    void getConcertById_WhenConcertDoesNotExist_ShouldThrowException() {
        when(concertRepository.findById(1L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> concertService.getConcertById(1L));
        assertEquals(ErrorCode.CONCERT_NOT_FOUND, exception.getErrorCode());
        verify(concertRepository, times(1)).findById(1L);
    }

    @Test
    void deleteConcert_WhenConcertExists_ShouldDeleteConcert() {
        when(concertRepository.findById(1L)).thenReturn(Optional.of(concert));
        doNothing().when(concertRepository).delete(concert);

        assertDoesNotThrow(() -> concertService.deleteConcert(1L));
        verify(concertRepository, times(1)).delete(concert);
    }

    @Test
    void getConcertsByConditions_ShouldReturnConcertPageDto() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
        Page<Concert> concertPage = new PageImpl<>(Collections.singletonList(concert));

        when(concertRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(concertPage);

        when(modelMapper.map(concert, ConcertResponseDto.class)).thenReturn(concertResponseDto);

        ConcertPageDto<ConcertResponseDto> result = concertService.getConcertsByConditions("Test", null, null, 0, 10, "title", "ASC");

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        verify(concertRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getConcertsByConditions_WhenDatabaseErrorOccurs_ShouldThrowException() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));

        when(concertRepository.findAll(any(Specification.class), eq(pageable)))
                .thenThrow(new DataAccessException("DB Error") {});

        CustomException exception = assertThrows(CustomException.class, () ->
                concertService.getConcertsByConditions("Test", null, null, 0, 10, "title", "ASC"));
        assertEquals(ErrorCode.DATABASE_ERROR, exception.getErrorCode());
    }

}
