package com.heroku.nwl.service;

import com.heroku.nwl.constants.Constants;
import com.heroku.nwl.model.ReservationRepository;
import com.heroku.nwl.model.Reservation;
import com.heroku.nwl.model.ReservationStatus;
import com.heroku.nwl.model.ServiceCatalog;
import com.heroku.nwl.model.ServiceCatalogRepository;
import com.heroku.nwl.model.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean createReservation(LocalTime reserveTime, LocalDate reserveDate, Long chatId,Long serviceCatalogId) {
        Reservation reservation = reservationRepository.findByOrderDateAndOrderTime(reserveDate, reserveTime);
        if ((reservation != null
                && reservation.
                    getReservationStatus().
                    equals(ReservationStatus.ACTIVE))
                || userRepository.findByChatId(chatId) == null) {
            return false;
        }
        Reservation newReservation = new Reservation();
        ServiceCatalog serviceCatalog = serviceCatalogRepository.findById(serviceCatalogId).get();
        newReservation.setOrderTime(reserveTime);
        newReservation.setOrderDate(reserveDate);
        newReservation.setServiceCatalog(serviceCatalog);
        newReservation.setEndTime(reserveTime.plusMinutes(serviceCatalog.getAverageTime()));
        newReservation.setReservationStatus(ReservationStatus.ACTIVE);
        newReservation.setUser(userRepository.findByChatId(chatId));
        reservationRepository.save(newReservation);
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean canselReservation(Long reservationId) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        Reservation order = reservationRepository.findByOrderId(reservationId);
        order.setReservationStatus(ReservationStatus.CANSEL);
        if (order.getOrderDate().compareTo(currentDate) != Constants.AFTER) {
            reservationRepository.save(order);
            return true;
        }
        if (order.getOrderDate().equals(currentDate) && order.getOrderTime().compareTo(currentTime) != Constants.AFTER) {
            reservationRepository.save(order);
            return true;
        }
        return false;
    }

    public List<Reservation> getUserReservations(Long chatId, LocalDate date, LocalTime time) {
        return reservationRepository.getUserReservations(chatId, date, time);
    }

    public List<Reservation> getReservationsByDate(LocalDate date) {
        return reservationRepository.findByOrderDate(date);
    }
    public List<Reservation> getReservationsByDateAndReservationStatus(LocalDate date, ReservationStatus status) {
        return reservationRepository.findByOrderDateAndReservationStatus(date, status);
    }
}
