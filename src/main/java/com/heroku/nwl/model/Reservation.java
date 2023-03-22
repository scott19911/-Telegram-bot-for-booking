package com.heroku.nwl.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity(name = "orders")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long orderId;
    private LocalDate orderDate;
    private LocalTime orderTime;
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus reservationStatus;
    private LocalTime endTime;
    @ManyToOne
    @JoinColumn(name = "service_id")
    private ServiceCatalog serviceCatalog;
    @ManyToOne
    @JoinColumn(name = "user_chat_id")
    private User user;
}
