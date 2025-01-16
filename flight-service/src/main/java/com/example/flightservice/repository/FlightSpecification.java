package com.example.flightservice.repository;

import com.example.flightservice.entity.Airport;
import com.example.flightservice.entity.Flight;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class FlightSpecification {

    public static Specification<Flight> equalDeparture(Airport departure) {
        return (root, query, CriteriaBuilder) -> CriteriaBuilder.equal(root.get("departure"), String.valueOf(departure));
    }

    public static Specification<Flight> equalDestination(Airport destination) {
        return (root, query, CriteriaBuilder) -> CriteriaBuilder.equal(root.get("destination"), String.valueOf(destination));
    }

    public static Specification<Flight> betweenStartDateAndEndDate(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, CriteriaBuilder) -> CriteriaBuilder.between(root.get("departureTime"), startDate, endDate);
    }
}