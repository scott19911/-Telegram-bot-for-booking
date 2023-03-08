package com.heroku.nwl.service;

import com.heroku.nwl.constants.Constants;
import com.heroku.nwl.model.OrderRepository;
import com.heroku.nwl.model.Orders;
import com.heroku.nwl.model.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Service
public class ReservationService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ReservationService(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean createReservation(LocalTime reserveTime, LocalDate reserveDate, Long chatId) {
        if (orderRepository.findByOrderDateAndOrderTime(reserveDate, reserveTime) != null
                || userRepository.findByChatId(chatId) == null) {
            return false;
        }
        Orders order = new Orders();
        order.setOrderTime(reserveTime);
        order.setOrderDate(reserveDate);
        order.setUser(userRepository.findByChatId(chatId));
        orderRepository.save(order);
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean deleteReservation(Long reservationId) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        Orders order = orderRepository.findByOrderId(reservationId);
        if (order != null && order.getOrderDate().compareTo(currentDate) != Constants.AFTER) {
            orderRepository.deleteById(reservationId);
            return true;
        }
        if (order != null && order.getOrderDate().equals(currentDate) && order.getOrderTime().compareTo(currentTime) != Constants.AFTER) {
            orderRepository.deleteById(reservationId);
            return true;
        }
        return false;
    }

    public List<Orders> getUserReservations(Long chatId, LocalDate date, LocalTime time) {
        return orderRepository.getUserReservations(chatId, date, time);
    }

    public List<Orders> getReservationsByDate(LocalDate date) {
        return orderRepository.findByOrderDate(date);
    }
}
