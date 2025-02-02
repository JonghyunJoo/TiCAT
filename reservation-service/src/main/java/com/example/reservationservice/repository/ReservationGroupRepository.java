package com.example.reservationservice.repository;

import com.example.reservationservice.entity.ReservationGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationGroupRepository extends JpaRepository<ReservationGroup, Long> {
    List<ReservationGroup> findAllByUserId(Long userId);
}