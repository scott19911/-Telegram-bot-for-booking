package com.heroku.nwl.service;

import com.heroku.nwl.model.DayOff;
import com.heroku.nwl.model.DayOffRepository;
import com.heroku.nwl.model.Reservation;
import com.heroku.nwl.model.ReservationStatus;
import com.heroku.nwl.model.ServiceCatalogRepository;
import com.heroku.nwl.model.WorkSettings;
import com.heroku.nwl.model.WorkSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkSchedule {
    public static final int ONE = 1;
    private static final int SHOW_NUMBER_OF_DAYS = 10;
    private final WorkSettingsRepository workSettingsRepository;
    private final DayOffRepository dayOffRepository;
    private final ReservationService reservationService;
    private final ServiceCatalogRepository serviceCatalogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<LocalTime> getWorkTime(LocalDate date, long serviceId) {
        int timePerPerson = serviceCatalogRepository.findById(serviceId).get().getAverageTime();
        WorkSettings workSettings = workSettingsRepository.findById(1L).orElse(new WorkSettings());
        List<Reservation> reservationList = reservationService.getReservationsByDateAndReservationStatus(date, ReservationStatus.ACTIVE);
        List<LocalTime> timePerDay = new ArrayList<>();
        LocalTime startTime = workSettings.getOpenTime();
        LocalTime notAvailableTime = date.equals(LocalDate.now()) ? LocalTime.now() : LocalTime.of(0, 0);
        while (startTime.plusMinutes(timePerPerson).isBefore(workSettings.getCloseTime().plusMinutes(ONE))) {
            boolean inWorkingTime = startTime.plusMinutes(timePerPerson).isBefore(workSettings.getBreakFrom())
                    || startTime.isAfter(workSettings.getBreakTo())
                    || startTime.equals(workSettings.getBreakTo());
            if (availableTime(startTime,reservationList,timePerPerson) && inWorkingTime && startTime.isAfter(notAvailableTime)) {
                timePerDay.add(startTime);
            }
            if (!availableTime(startTime,reservationList,timePerPerson)){
                startTime = getEndTime(startTime,reservationList,timePerPerson);
            } else {
                startTime = startTime.plusMinutes(timePerPerson);
            }
            if (startTime.isAfter(workSettings.getBreakFrom()) && startTime.isBefore(workSettings.getBreakTo())) {
                startTime = workSettings.getBreakTo();
            }
        }
        return timePerDay;
    }

    public List<LocalDate> getWorkingDays() {
        LocalDate today = LocalDate.now();
        List<DayOff> dayOffDateBetween = dayOffRepository.findByDayOffDateBetween(today, today.plusMonths(ONE));
        List<LocalDate> dayOffList = getLocalDateFromDayOff(dayOffDateBetween);
        List<LocalDate> scheduleList = new ArrayList<>();
        for (int i = 0; i < SHOW_NUMBER_OF_DAYS; i++) {
            if (dayOffList.contains(today.plusDays(i))) {
                today = today.plusDays(ONE);
                i--;
            } else {
                scheduleList.add(today.plusDays(i));
            }
        }
        return scheduleList;
    }

    private List<LocalDate> getLocalDateFromDayOff(List<DayOff> list) {
        return list.stream().map(DayOff::getDayOffDate).collect(Collectors.toList());
    }

    private boolean availableTime(LocalTime currentTime, List<Reservation> reservationList, int timePerPerson){
      return getEndTime(currentTime, reservationList, timePerPerson) == null;
    }
    private LocalTime getEndTime(LocalTime currentTime, List<Reservation> reservationList, int timePerPerson){
        for (Reservation reservation: reservationList
        ) {
            LocalTime startTime = reservation.getOrderTime();
            LocalTime endTime = reservation.getEndTime();
            if (currentTime.equals(startTime)){
                return endTime;
            }
            if(currentTime.isBefore(startTime)){
                if(currentTime.plusMinutes(timePerPerson).isAfter(startTime)){
                    return endTime;
                }
            }
            if (currentTime.isAfter(startTime)){
                if(currentTime.plusMinutes(timePerPerson).isBefore(endTime)){
                    return endTime;
                }
            }
        }
        return null;
    }
}
