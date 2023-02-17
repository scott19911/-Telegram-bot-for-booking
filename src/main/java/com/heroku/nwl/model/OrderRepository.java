package com.heroku.nwl.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface OrderRepository extends CrudRepository<Orders, Long> {
    List<Orders> findByOrderDate(LocalDate orderDate);

    Orders findByOrderDateAndOrderTime(LocalDate orderDate, LocalTime orderTime);

    @Query(value = "select o1_0.order_id,o1_0.order_date,o1_0.order_time,o1_0.user_chat_id from orders o1_0 where o1_0.user_chat_id= :chatId and (DATE (o1_0.order_date) > :today or (DATE(o1_0.order_date)= :today and TIME(o1_0.order_time)> :currentTime))", nativeQuery = true)
    List<Orders> getUserReservations(@Param("chatId") Long chatId, @Param("today") LocalDate date, @Param("currentTime") LocalTime time);

    Orders findByOrderId(Long orderId);
}

