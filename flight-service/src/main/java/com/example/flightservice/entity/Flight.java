package com.example.flightservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "flights",	indexes = {
        @Index(columnList = "departureTime"),
        @Index(columnList = "departure, destination")})
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Airport departure;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Airport destination;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;
}
