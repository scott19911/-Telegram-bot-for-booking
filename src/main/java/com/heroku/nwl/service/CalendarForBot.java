package com.heroku.nwl.service;

import com.heroku.nwl.model.DayOff;
import com.heroku.nwl.model.DayOffRepository;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class CalendarForBot {
    public static final String EMPTY_DATA = " ";
    public static final String CHANGE_MONTH = "changeMonth";
    public static final String ADD_DAY_OFF = "add_day_off";
    public static final String DELETE_DAY_OFF = "delete_day_off";
    public static final String[] WEEK_DAYS = new String[]{"пн", "вт", "ср", "чт", "пт", "сб", "нд"};
    private final DayOffRepository dayOffRepository;
    private final KeyboardService keyboardService;

    public CalendarForBot(DayOffRepository dayOffRepository, KeyboardService keyboardService) {
        this.dayOffRepository = dayOffRepository;
        this.keyboardService = keyboardService;
    }

    public InlineKeyboardMarkup getCalendarKeyboard(LocalDate date, String command) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> yearMonthRow = new ArrayList<>();
        List<InlineKeyboardButton> navigateRow = new ArrayList<>();
        String yearMonth = date.getMonth() + " " + date.getYear();
        yearMonthRow.add(keyboardService.getInlineKeyboardButton(yearMonth, EMPTY_DATA));
        navigateRow.add(keyboardService.getInlineKeyboardButton("<", CHANGE_MONTH + command + date.minusMonths(1)));
        navigateRow.add(keyboardService.getInlineKeyboardButton(EMPTY_DATA, EMPTY_DATA));
        navigateRow.add(keyboardService.getInlineKeyboardButton(">", CHANGE_MONTH + command + date.plusMonths(1)));
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(yearMonthRow);
        rowList.add(keyboardService.getDayButton());
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
        rowList.add(navigateRow);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup changeMonth(String callbackData, String command) {
        LocalDate localDate = LocalDate.parse(callbackData.replace(CHANGE_MONTH + command, ""));
        return getCalendarKeyboard(localDate, command);
    }

    public List<InlineKeyboardButton> getDateRow(LocalDate date, int shift, List<DayOff> dayOffList, String command) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (int i = 0; i < shift; i++) {
            row.add(keyboardService.getInlineKeyboardButton(EMPTY_DATA, EMPTY_DATA));
        }
        for (int i = 0; i < 7 - shift; i++) {
            if (date.plusDays(i).getMonth().equals(date.getMonth())) {
                if (!dayOffList.isEmpty() && dayOffList.contains(new DayOff(date.plusDays(i)))) {
                    row.add(keyboardService.getInlineKeyboardButton(EmojiParser.parseToUnicode(":x:"), DELETE_DAY_OFF + date.plusDays(i)));
                } else {
                    row.add(keyboardService.getInlineKeyboardButton(String.valueOf(date.plusDays(i).getDayOfMonth()), command + date.plusDays(i)));
                }
            } else {
                row.add(keyboardService.getInlineKeyboardButton(EMPTY_DATA, EMPTY_DATA));
            }
        }
        return row;
    }
}
