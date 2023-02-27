package com.heroku.nwl.service;

import com.heroku.nwl.dto.CalendarDayDto;
import com.heroku.nwl.model.DayOff;
import com.heroku.nwl.model.DayOffRepository;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.heroku.nwl.constants.Commands.ALL_RESERVATION_ON_DATE;
import static com.heroku.nwl.constants.Commands.JSON_COMMAND_WITH_CURRENT_DATE;

@Component
public class Calendar {
    public static final String EMPTY_DATA = " ";
    public static final String ADD_DAY_OFF = "add_day_off";
    public static final String DELETE_DAY_OFF = "delete_day_off";
    public static final String[] WEEK_DAYS = new String[]{"пн", "вт", "ср", "чт", "пт", "сб", "нд"};
    private final DayOffRepository dayOffRepository;



    public Calendar(DayOffRepository dayOffRepository) {
        this.dayOffRepository = dayOffRepository;
    }


    public List<List<CalendarDayDto>> getCalendar(LocalDate date, String command) {
        List<List<CalendarDayDto>> rowList = new ArrayList<>();
        int shift = date.withDayOfMonth(1).getDayOfWeek().getValue() - 1;
        int additionalRows = (date.lengthOfMonth() + shift) % 7 > 0 ? 1 : 0;
        int rows = ((date.lengthOfMonth() + shift) / 7) + additionalRows;
        LocalDate firstDay = date.withDayOfMonth(1);
        List<DayOff> dayOffList = dayOffRepository
                .findByDayOffDateBetween(date.withDayOfMonth(1), date.withDayOfMonth(date.lengthOfMonth()));
        for (int i = 0; i < rows; i++) {
            rowList.add(getDateRow(firstDay, shift, dayOffList, command));
            firstDay = firstDay.plusDays(7 - shift);
            shift = 0;
        }
        return rowList;
    }

    public List<CalendarDayDto> getDateRow(LocalDate date, int shift, List<DayOff> dayOffList, String command) {
        List<CalendarDayDto> row = new ArrayList<>();
        String xEmoji = EmojiParser.parseToUnicode(":x:");
        for (int i = 0; i < shift; i++) {
            row.add(new CalendarDayDto(EMPTY_DATA,EMPTY_DATA));
        }
        for (int i = 0; i < 7 - shift; i++) {
            if (date.plusDays(i).getMonth().equals(date.getMonth())) {
                if (!dayOffList.isEmpty() && dayOffList.contains(new DayOff(date.plusDays(i)))) {
                    String dayData = String.format(JSON_COMMAND_WITH_CURRENT_DATE, DELETE_DAY_OFF, date.plusDays(i));
                    dayData = command.equals(ALL_RESERVATION_ON_DATE) ?
                            EMPTY_DATA : dayData;
                    row.add(new CalendarDayDto(xEmoji,dayData));
                } else {
                    String dayData = String.format(JSON_COMMAND_WITH_CURRENT_DATE, command, date.plusDays(i));
                    row.add(new CalendarDayDto(String.valueOf(date.plusDays(i).getDayOfMonth()),dayData));
                }
            } else {
                row.add(new CalendarDayDto(EMPTY_DATA,EMPTY_DATA));
            }
        }
        return row;
    }
}
