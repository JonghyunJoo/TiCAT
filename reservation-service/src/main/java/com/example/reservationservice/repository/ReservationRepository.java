package com.example.reservationservice.repository;

import java.util.List;
import java.util.Optional;

import com.example.reservationservice.entity.Reservation;
import com.example.reservationservice.entity.ReservationStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByUserId(Long userId);
    List<Reservation> findAllByReservationGroupId(Long reservationGroupId);
    @Modifying
    @Query("UPDATE Reservation r SET r.reservationStatus = :status WHERE r.reservationGroup.id = :reservationGroupId")
    int updateReservationStatusByGroupId(@Param("status") ReservationStatus status,
                                         @Param("reservationGroupId") Long reservationGroupId);

    @Query("SELECT COALESCE(SUM(r.price), 0) FROM Reservation r WHERE r.reservationGroup.id = :reservationGroupId")
    Optional<Long> findTotalPriceByReservationGroupId(@Param("reservationGroupId") Long reservationGroupId);
}