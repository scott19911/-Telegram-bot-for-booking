package com.heroku.nwl.service;

import com.heroku.nwl.config.CustomBotException;
import com.heroku.nwl.model.Reservation;
import com.heroku.nwl.model.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationService {

    List<Reservation> getUserReservations(Long chatId, LocalDate date, LocalTime time);
    boolean canselReservation(Long reservationId);
    List<Reservation> getReservationsByDate(LocalDate date);
    List<Reservation> getReservationsByDateAndReservationStatus(LocalDate date, ReservationStatus status);
    void changeStatus(Long reservationId, ReservationStatus reservationStatus);
    boolean createReservation(LocalTime reserveTime, LocalDate reserveDate, Long chatId,Long serviceCatalogId) throws CustomBotException;
}
