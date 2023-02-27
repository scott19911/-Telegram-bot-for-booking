package com.heroku.nwl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heroku.nwl.dto.ButtonDto;
import com.heroku.nwl.dto.CalendarDayDto;
import com.heroku.nwl.model.Orders;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.heroku.nwl.constants.Commands.ALL_RESERVATION_ON_DATE;
import static com.heroku.nwl.constants.Commands.AVAILABLE_DATE_TO_RESERVE;
import static com.heroku.nwl.constants.Commands.CANCEL_RESERVE;
import static com.heroku.nwl.constants.Commands.CHANGE_MONTH;
import static com.heroku.nwl.constants.Commands.DELETE_RESERVE;
import static com.heroku.nwl.constants.Commands.JSON_COMMAND_CANCEL_RESERVATION;
import static com.heroku.nwl.constants.Commands.JSON_COMMAND_CHANGE_MONTH;
import static com.heroku.nwl.constants.Commands.JSON_COMMAND_DATA_TIME;
import static com.heroku.nwl.constants.Commands.JSON_COMMAND_NAVIGATE_DATA;
import static com.heroku.nwl.constants.Commands.JSON_COMMAND_RESERVATION;
import static com.heroku.nwl.constants.Commands.JSON_COMMAND_RETURN_BACK;
import static com.heroku.nwl.constants.Commands.ORDER_TIME;
import static com.heroku.nwl.constants.Commands.WORKDAY;
import static com.heroku.nwl.constants.Constants.BACK;
import static com.heroku.nwl.constants.Constants.NEXT;
import static com.heroku.nwl.constants.Constants.PRIVIES;
import static com.heroku.nwl.service.Calendar.ADD_DAY_OFF;
import static com.heroku.nwl.service.Calendar.EMPTY_DATA;
import static com.heroku.nwl.service.Calendar.WEEK_DAYS;

@Service
public class KeyboardService {


    private final WorkSchedule workSchedule;
    private final ReservationService reservationService;

    public KeyboardService(WorkSchedule workSchedule, ReservationService reservationService) {
        this.workSchedule = workSchedule;

        this.reservationService = reservationService;
    }

    public InlineKeyboardMarkup getScheduleDays() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<LocalDate> workDays = workSchedule.getWorkingDays();
        List<InlineKeyboardButton> firstLine = new ArrayList<>();
        List<InlineKeyboardButton> secondLine = new ArrayList<>();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (int i = 0; i < workDays.size(); i++) {
            LocalDate date = workDays.get(i);
            String month = date.getMonth().getValue() < 10 ?
                    ".0" + date.getMonth().getValue() : "." + date.getMonth().getValue();
            String buttonText = WEEK_DAYS[date.getDayOfWeek().getValue() - 1]
                    + " " + date.getDayOfMonth() + month;
            Map<String,LocalDate> navigateDate = getNextDate(workDays, i);
            String outputData = String.valueOf(new Formatter().format(
                    JSON_COMMAND_NAVIGATE_DATA,WORKDAY,
                    date,
                    navigateDate.get(PRIVIES),
                    navigateDate.get(NEXT)));
            if (i <= (workDays.size() / 2) - 1) {
                firstLine.add(getInlineKeyboardButton(buttonText, outputData));
            } else {
                secondLine.add(getInlineKeyboardButton(buttonText, outputData));
            }
        }

        rowList.add(firstLine);
        rowList.add(secondLine);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getAvailableTimeKeyboard(ButtonDto dto) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<LocalTime> workTime = workSchedule.getWorkTime(dto.getCurrentDate());
        List<InlineKeyboardButton> navigateButtons = new ArrayList<>();
        List<LocalDate> workDays = workSchedule.getWorkingDays();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        if (dto.getPreviousDate() != null) {
            int index = workDays.indexOf(dto.getPreviousDate());
            Map<String,LocalDate> navigateDate = getNextDate(workDays, index);
            String outputData = String.valueOf(new Formatter().format(
                    JSON_COMMAND_NAVIGATE_DATA,WORKDAY,
                    dto.getPreviousDate(),
                    navigateDate.get(PRIVIES),
                    navigateDate.get(NEXT)));
            navigateButtons.add(getInlineKeyboardButton("<", outputData));
        }
        if (dto.getNextDate() != null) {
            int index = workDays.indexOf(dto.getNextDate());
            Map<String,LocalDate> navigateDate = getNextDate(workDays, index);
            String outputData = String.valueOf(new Formatter().format(
                    JSON_COMMAND_NAVIGATE_DATA,WORKDAY,
                    dto.getNextDate(),
                    navigateDate.get(PRIVIES),
                    navigateDate.get(NEXT)));
            navigateButtons.add(getInlineKeyboardButton(">", outputData));
        }
        rowList.add(navigateButtons);
        for (LocalTime time : workTime
        ) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            String buttonText = "reserve at " + time;
            String outputData = String.valueOf(new Formatter().format(
                    JSON_COMMAND_DATA_TIME,ORDER_TIME,
                    dto.getCurrentDate(),
                    time));
            buttons.add(getInlineKeyboardButton(buttonText, outputData));
            rowList.add(buttons);
        }
        rowList.add(getBackButton(AVAILABLE_DATE_TO_RESERVE, dto.getCurrentDate()));
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private Map<String,LocalDate> getNextDate(List<LocalDate> workDays, int index) {
        LocalDate priviesDate = null;
        LocalDate nextDate = null;
        Map<String,LocalDate> result = new HashMap<>();
        if (index > 0) {
            priviesDate = workDays.get(index - 1);
        }
        if (index < workDays.size() - 1) {
            nextDate = workDays.get(index + 1);
        }
        result.put(PRIVIES,priviesDate);
        result.put(NEXT,nextDate);
        return result;
    }

    public InlineKeyboardButton getInlineKeyboardButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
    public InlineKeyboardMarkup getCalendar(List<List<CalendarDayDto>> calendar,LocalDate date){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> yearMonthRow = new ArrayList<>();
        List<InlineKeyboardButton> navigateRow = new ArrayList<>();
        CalendarDayDto calendarDayDto = calendar.get(0).get(6);
        ButtonDto buttonDto;
        try {
             buttonDto = new ObjectMapper().readValue(calendarDayDto.getJsonData(), ButtonDto.class);
        } catch (JsonProcessingException e) {
            buttonDto = new ButtonDto();
            buttonDto.setCommand(ADD_DAY_OFF);
        }
        String command = buttonDto.getCommand();

        String yearMonth = date.getMonth() + " " + date.getYear();
        yearMonthRow.add(getInlineKeyboardButton(yearMonth, EMPTY_DATA));
        String priviesMonth =  String.valueOf(
                new Formatter()
                        .format(JSON_COMMAND_CHANGE_MONTH,
                                CHANGE_MONTH,
                                date.minusMonths(1),
                                command));
        String nextMonth =  String.valueOf(
                new Formatter()
                        .format(JSON_COMMAND_CHANGE_MONTH,
                                CHANGE_MONTH,
                                date.plusMonths(1),
                                command));
        navigateRow.add(getInlineKeyboardButton("<",priviesMonth));
        navigateRow.add(getInlineKeyboardButton(EMPTY_DATA, EMPTY_DATA));
        navigateRow.add(getInlineKeyboardButton(">", nextMonth));
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(yearMonthRow);
        rowList.add(getDayOfWeekButton());
        for (List<CalendarDayDto> week:calendar
             ) {
            List<InlineKeyboardButton> days = new ArrayList<>();
            for (CalendarDayDto day:week
                 ) {
                if (day.getText().equals(EMPTY_DATA)){
                    days.add(getInlineKeyboardButton(EMPTY_DATA,EMPTY_DATA));
                } else {
                    days.add(getInlineKeyboardButton(day.getText(),day.getJsonData()));
                }
            }
            rowList.add(days);
        }
        rowList.add(navigateRow);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getReservationOnDate(LocalDate date) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<Orders> ordersList = reservationService.getReservationsByDate(date);
        for (Orders order : ordersList
        ) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            String outputData = String.valueOf(new Formatter().format(
                    JSON_COMMAND_CANCEL_RESERVATION,CANCEL_RESERVE,
                    order.getOrderId(),date));
            String text = order.getOrderTime() + " "
                    + order.getOrderDate() + " "
                    + order.getUser().getFirstName() + " "
                    + order.getUser().getPhoneNumber();
            buttons.add(getInlineKeyboardButton(text, outputData));
            rowList.add(buttons);
        }
        rowList.add(getBackButton(ALL_RESERVATION_ON_DATE, date));
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getUserReservation(Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<Orders> ordersList = reservationService.getUserReservations(chatId, LocalDate.now(), LocalTime.now());
        for (Orders order : ordersList
        ) {
            String outputData = String.valueOf(new Formatter().format(
                    JSON_COMMAND_RESERVATION,DELETE_RESERVE,
                    order.getOrderId(),order.getOrderDate(),order.getOrderTime()));
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            buttons.add(getInlineKeyboardButton(order.getOrderTime() + " " + order.getOrderDate(),outputData));
            rowList.add(buttons);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public List<InlineKeyboardButton> getDayOfWeekButton() {
        List<InlineKeyboardButton> dayOfWeek = new ArrayList<>();
        for (int i = 0; i < WEEK_DAYS.length; i++) {
            dayOfWeek.add(getInlineKeyboardButton(WEEK_DAYS[i], EMPTY_DATA));
        }
        return dayOfWeek;
    }
    public List<InlineKeyboardButton> getBackButton(String command, LocalDate date) {
        List<InlineKeyboardButton> dayOfWeek = new ArrayList<>();
        dayOfWeek.add(getInlineKeyboardButton(BACK, String.format(JSON_COMMAND_RETURN_BACK,command,date)));
        return dayOfWeek;
    }
}
