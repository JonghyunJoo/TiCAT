package com.example.flightservice.repository;

import java.time.LocalDateTime;

import com.example.flightservice.entity.Airport;
import com.example.flightservice.entity.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    Page<Flight> findByDepartureTimeBetweenAndDepartureAndDestination(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Airport departure,
            Airport destination,
            Pageable pageable);
}