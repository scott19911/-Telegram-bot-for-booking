package com.heroku.nwl.service;

import com.heroku.nwl.model.DayOff;
import com.heroku.nwl.model.DayOffRepository;
import com.heroku.nwl.model.OrderRepository;
import com.heroku.nwl.model.Orders;
import com.heroku.nwl.model.WorkTimeSettings;
import com.heroku.nwl.model.WorkTimeSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkSchedule {
    public static final int ONE = 1;
    private static final int SHOW_NUMBER_OF_DAYS = 10;
    private final WorkTimeSettingsRepository workTimeSettingsRepository;
    private final DayOffRepository dayOffRepository;
    private final OrderRepository orderRepository;

    public WorkSchedule(WorkTimeSettingsRepository workTimeSettingsRepository, DayOffRepository dayOffRepository, OrderRepository orderRepository) {
        this.workTimeSettingsRepository = workTimeSettingsRepository;
        this.dayOffRepository = dayOffRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<LocalTime> getWorkTime(LocalDate date) {
        WorkTimeSettings workTimeSettings = workTimeSettingsRepository.findById(1L).orElse(new WorkTimeSettings());
        List<Orders> ordersList = orderRepository.findByOrderDate(date);
        List<LocalTime> reservedTime = getOrderTimeListFromOrders(ordersList);
        List<LocalTime> timePerDay = new ArrayList<>();
        LocalTime startTime = workTimeSettings.getOpenTime();
        LocalTime notAvailableTime = date.equals(LocalDate.now()) ? LocalTime.now() : LocalTime.of(0, 0);
        while (startTime.plusMinutes(workTimeSettings.getTimePerPerson()).isBefore(workTimeSettings.getCloseTime().plusMinutes(ONE))) {
            boolean inWorkingTime = startTime.plusMinutes(workTimeSettings.getTimePerPerson()).isBefore(workTimeSettings.getBreakFrom())
                    || startTime.isAfter(workTimeSettings.getBreakTo())
                    || startTime.equals(workTimeSettings.getBreakTo());
            if (!reservedTime.contains(startTime) && inWorkingTime && !reservedTime.contains(startTime) && startTime.isAfter(notAvailableTime)) {
                timePerDay.add(startTime);
            }
            startTime = startTime.plusMinutes(workTimeSettings.getTimePerPerson());
            if (startTime.isAfter(workTimeSettings.getBreakFrom()) && startTime.isBefore(workTimeSettings.getBreakTo())) {
                startTime = workTimeSettings.getBreakTo();
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

    private List<LocalTime> getOrderTimeListFromOrders(List<Orders> ordersList) {
        return ordersList.stream().map(Orders::getOrderTime).collect(Collectors.toList());
    }
}
