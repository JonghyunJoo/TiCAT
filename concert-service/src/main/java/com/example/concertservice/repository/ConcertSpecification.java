package com.example.concertservice.repository;

import com.example.concertservice.entity.Concert;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ConcertSpecification {

    public static Specification<Concert> likeTitle(String title) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                root.get("departure"), "%" + title + "%"
        );
    }

    public static Specification<Concert> betweenStartDateAndEndDate(LocalDateTime searchStartDate, LocalDateTime searchEndDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.or(
                        criteriaBuilder.between(root.get("ConcertStartDate"), searchStartDate, searchEndDate),
                        criteriaBuilder.and(
                                criteriaBuilder.lessThanOrEqualTo(root.get("ConcertStartDate"), searchEndDate),
                                criteriaBuilder.greaterThanOrEqualTo(root.get("ConcertEndDate"), searchStartDate)
                        )
                );
    }
}