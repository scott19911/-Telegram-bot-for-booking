package com.heroku.nwl.service;

import com.heroku.nwl.dto.CalendarDayDto;
import com.heroku.nwl.model.DayOff;

import java.time.LocalDate;
import java.util.List;

public interface CalendarService {
    List<List<CalendarDayDto>> getCalendar(LocalDate date, String command);
    List<CalendarDayDto> getDateRow(LocalDate date, int shift, List<DayOff> dayOffList, String command);
}
