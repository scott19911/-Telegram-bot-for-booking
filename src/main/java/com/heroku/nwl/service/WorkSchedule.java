package com.heroku.nwl.service;

import com.heroku.nwl.config.CustomBotException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface WorkSchedule {
    List<LocalTime> getWorkTime(LocalDate date, long serviceId) throws CustomBotException;
    List<LocalDate> getWorkingDays();
}
