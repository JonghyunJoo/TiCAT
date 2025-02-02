package com.example.reservationservice.repository;

import java.util.List;

import com.example.reservationservice.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByUserId(Long userId);
    List<Reservation> findAllByReservationGroupId(Long reservationGroupId);
}