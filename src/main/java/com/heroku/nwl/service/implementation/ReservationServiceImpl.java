package com.heroku.nwl.service.implementation;

import com.heroku.nwl.config.CustomBotException;
import com.heroku.nwl.constants.Constants;
import com.heroku.nwl.model.Reservation;
import com.heroku.nwl.model.ReservationRepository;
import com.heroku.nwl.model.ReservationStatus;
import com.heroku.nwl.model.ServiceCatalog;
import com.heroku.nwl.model.ServiceCatalogRepository;
import com.heroku.nwl.model.UserRepository;
import com.heroku.nwl.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.heroku.nwl.constants.ErrorMessage.ERROR_SERVICE_NOT_AVAILABLE;


@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ServiceCatalogRepository serviceCatalogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean createReservation(LocalTime reserveTime, LocalDate reserveDate, Long chatId, Long serviceCatalogId) throws CustomBotException {
        List<Reservation> reservations = reservationRepository.findByOrderDateAndReservationStatus(reserveDate, ReservationStatus.ACTIVE);
        if (userRepository.findByChatId(chatId) == null
                || timeAlreadyTaken(reservations, reserveTime)
                || reserveDate.isBefore(LocalDate.now())) {
            return false;
        }
        Reservation newReservation = new Reservation();
        ServiceCatalog serviceCatalog = serviceCatalogRepository.findById(serviceCatalogId).orElse(null);
        if (serviceCatalog == null) {
            throw new CustomBotException(ERROR_SERVICE_NOT_AVAILABLE);
        }
        newReservation.setOrderTime(reserveTime);
        newReservation.setOrderDate(reserveDate);
        newReservation.setServiceCatalog(serviceCatalog);
        newReservation.setEndTime(reserveTime.plusMinutes(serviceCatalog.getAverageTime()));
        newReservation.setReservationStatus(ReservationStatus.ACTIVE);
        newReservation.setUser(userRepository.findByChatId(chatId));
        reservationRepository.save(newReservation);
        return true;
    }

    private boolean timeAlreadyTaken(List<Reservation> reservationList, LocalTime reserveTime) {
        for (Reservation reservation : reservationList
        ) {
            if ((reserveTime.equals(reservation.getOrderTime())
                    || reserveTime.isAfter(reservation.getOrderTime()))
                    && reserveTime.isBefore(reservation.getEndTime())) {
                return true;
            }
        }
        return false;
    }

    @Override
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

    @Override
    public List<Reservation> getUserReservations(Long chatId, LocalDate date, LocalTime time) {
        return reservationRepository.getUserReservations(chatId, date, time);
    }

    @Override
    public List<Reservation> getReservationsByDate(LocalDate date) {
        return reservationRepository.findByOrderDate(date);
    }

    @Override
    public List<Reservation> getReservationsByDateAndReservationStatus(LocalDate date, ReservationStatus status) {
        return reservationRepository.findByOrderDateAndReservationStatus(date, status);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeStatus(Long reservationId, ReservationStatus reservationStatus) {
        Reservation reservation = reservationRepository.findByOrderId(reservationId);
        reservation.setReservationStatus(reservationStatus);
        reservationRepository.save(reservation);
    }
}
