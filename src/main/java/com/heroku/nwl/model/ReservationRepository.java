package com.heroku.nwl.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends CrudRepository<Reservation, Long> {
    List<Reservation> findByOrderDateAndReservationStatus(LocalDate orderDate, ReservationStatus status);
    List<Reservation> findByOrderDate(LocalDate orderDate);
    Reservation findByOrderDateAndOrderTime(LocalDate orderDate, LocalTime orderTime);

    @Query(value = "select * " +
            "from orders o1_0 " +
            "where o1_0.user_chat_id= :chatId and o1_0.reservation_status ='ACTIVE' " +
            "and (DATE (o1_0.order_date) > :today " +
            "or (DATE(o1_0.order_date)= :today " +
            "and TIME(o1_0.order_time)> :currentTime))", nativeQuery = true)
    List<Reservation> getUserReservations(
            @Param("chatId") Long chatId,
            @Param("today") LocalDate date,
            @Param("currentTime") LocalTime time);

    Reservation findByOrderId(Long orderId);

    Reservation findByOrderDateAndOrderTimeAndReservationStatus(LocalDate date, LocalTime time, ReservationStatus status);
}

