package com.heroku.nwl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heroku.nwl.config.CustomBotException;
import com.heroku.nwl.dto.ButtonDto;
import com.heroku.nwl.dto.CalendarDayDto;
import com.heroku.nwl.model.Reservation;
import com.heroku.nwl.model.ReservationStatus;
import com.heroku.nwl.model.ServiceCatalog;
import com.heroku.nwl.model.ServiceCatalogRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.heroku.nwl.constants.Commands.ALL_RESERVATION;
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
import static com.heroku.nwl.constants.Commands.JSON_COMMAND_SELECT_SERVICE;
import static com.heroku.nwl.constants.Commands.ORDER_TIME;
import static com.heroku.nwl.constants.Commands.SELECT_SERVICE;
import static com.heroku.nwl.constants.Commands.WORKDAY;
import static com.heroku.nwl.constants.Constants.ADD_DAY_OFF;
import static com.heroku.nwl.constants.Constants.BACK;
import static com.heroku.nwl.constants.Constants.DOT;
import static com.heroku.nwl.constants.Constants.DOT_ZERO;
import static com.heroku.nwl.constants.Constants.EMPTY_DATA;
import static com.heroku.nwl.constants.Constants.FORMATTER;
import static com.heroku.nwl.constants.Constants.LEFT_ARROW;
import static com.heroku.nwl.constants.Constants.NEXT;
import static com.heroku.nwl.constants.Constants.PRIVIES;
import static com.heroku.nwl.constants.Constants.RESERVE_AT;
import static com.heroku.nwl.constants.Constants.RIGHT_ARROW;
import static com.heroku.nwl.constants.Constants.SERVICE_BUTTON_TEXT;
import static com.heroku.nwl.constants.Constants.WEEK_DAYS;
import static com.heroku.nwl.constants.ErrorMessage.ERROR_SERVICE_NOT_AVAILABLE;

@RequiredArgsConstructor
@Service
public class KeyboardService {


    private final WorkSchedule workSchedule;
    private final ReservationService reservationService;
    private final ServiceCatalogRepository serviceCatalogRepository;

    public InlineKeyboardMarkup getScheduleDays() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<LocalDate> workDays = workSchedule.getWorkingDays();
        List<InlineKeyboardButton> firstLine = new ArrayList<>();
        List<InlineKeyboardButton> secondLine = new ArrayList<>();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (int i = 0; i < workDays.size(); i++) {
            LocalDate date = workDays.get(i);
            String month = date.getMonth().getValue() < 10 ?
                    DOT_ZERO + date.getMonth().getValue() : DOT + date.getMonth().getValue();
            String buttonText = WEEK_DAYS[date.getDayOfWeek().getValue() - 1]
                    + EMPTY_DATA + date.getDayOfMonth() + month;
            Map<String, String> navigateDate = getNextDate(workDays, i);
            String outputData = String.valueOf(new Formatter().format(
                    JSON_COMMAND_NAVIGATE_DATA, SELECT_SERVICE,
                    date.format(FORMATTER),
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

    public InlineKeyboardMarkup getAvailableTimeKeyboard(ButtonDto dto) throws CustomBotException {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<LocalTime> workTime = workSchedule.getWorkTime(dto.getCurrentDate(), dto.getServiceId());
        List<InlineKeyboardButton> navigateButtons = new ArrayList<>();
        List<LocalDate> workDays = workSchedule.getWorkingDays();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        if (dto.getPreviousDate() != null) {
            int index = workDays.indexOf(dto.getPreviousDate());
            Map<String, String> navigateDate = getNextDate(workDays, index);
            String outputData = String.valueOf(new Formatter().format(
                    JSON_COMMAND_SELECT_SERVICE, WORKDAY,
                    dto.getPreviousDate().format(FORMATTER),
                    navigateDate.get(PRIVIES),
                    navigateDate.get(NEXT),
                    dto.getServiceId()));
            navigateButtons.add(getInlineKeyboardButton(LEFT_ARROW, outputData));
        }
        if (dto.getNextDate() != null) {
            int index = workDays.indexOf(dto.getNextDate());
            Map<String, String> navigateDate = getNextDate(workDays, index);
            String outputData = String.valueOf(new Formatter().format(
                    JSON_COMMAND_SELECT_SERVICE, WORKDAY,
                    dto.getNextDate().format(FORMATTER),
                    navigateDate.get(PRIVIES),
                    navigateDate.get(NEXT),
                    dto.getServiceId()));
            navigateButtons.add(getInlineKeyboardButton(RIGHT_ARROW, outputData));
        }
        rowList.add(navigateButtons);
        for (LocalTime time : workTime
        ) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            String buttonText = RESERVE_AT + time;
            String outputData = String.valueOf(new Formatter().format(
                    JSON_COMMAND_DATA_TIME, ORDER_TIME,
                    dto.getCurrentDate().format(FORMATTER),
                    time,
                    dto.getServiceId()));
            buttons.add(getInlineKeyboardButton(buttonText, outputData));
            rowList.add(buttons);
        }
        rowList.add(getBackButton(AVAILABLE_DATE_TO_RESERVE, dto.getCurrentDate()));
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private Map<String, String> getNextDate(List<LocalDate> workDays, int index) {
        String priviesDate = null;
        String nextDate = null;
        Map<String, String> result = new HashMap<>();
        if (index > 0) {
            priviesDate = workDays.get(index - 1).format(FORMATTER);
        }
        if (index < workDays.size() - 1) {
            nextDate = workDays.get(index + 1).format(FORMATTER);
        }
        result.put(PRIVIES, priviesDate);
        result.put(NEXT, nextDate);
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
        String yearMonth = date.getMonth() + EMPTY_DATA + date.getYear();
        yearMonthRow.add(getInlineKeyboardButton(yearMonth, EMPTY_DATA));
        String priviesMonth =  String.valueOf(
                new Formatter()
                        .format(JSON_COMMAND_CHANGE_MONTH,
                                CHANGE_MONTH,
                                date.minusMonths(1).format(FORMATTER),
                                command));
        String nextMonth =  String.valueOf(
                new Formatter()
                        .format(JSON_COMMAND_CHANGE_MONTH,
                                CHANGE_MONTH,
                                date.plusMonths(1).format(FORMATTER),
                                command));
        navigateRow.add(getInlineKeyboardButton(LEFT_ARROW,priviesMonth));
        navigateRow.add(getInlineKeyboardButton(EMPTY_DATA, EMPTY_DATA));
        navigateRow.add(getInlineKeyboardButton(RIGHT_ARROW, nextMonth));
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
        List<Reservation> reservationList = reservationService.getReservationsByDate(date);
        for (Reservation order : reservationList
        ) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            String outputData = String.valueOf(new Formatter().format(
                    JSON_COMMAND_CANCEL_RESERVATION, CANCEL_RESERVE,
                    order.getOrderId(), date.format(FORMATTER)));
            String text = order.getOrderTime() + EMPTY_DATA
                    + order.getOrderDate() + EMPTY_DATA
                    + order.getUser().getFirstName() + EMPTY_DATA
                    + order.getUser().getPhoneNumber();
            if (!order.getReservationStatus().equals(ReservationStatus.ACTIVE)) {
                outputData = EMPTY_DATA;
                text = EmojiParser.parseToUnicode(":x:") + EMPTY_DATA + text;
            }
            buttons.add(getInlineKeyboardButton(text, outputData));
            rowList.add(buttons);
        }
        rowList.add(getBackButton(ALL_RESERVATION, date));
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getUserReservation(Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<Reservation> reservationList = reservationService.getUserReservations(chatId, LocalDate.now(), LocalTime.now());
        for (Reservation order : reservationList
        ) {
            String outputData = String.valueOf(new Formatter().format(
                    JSON_COMMAND_RESERVATION, DELETE_RESERVE,
                    order.getOrderId(), order.getOrderDate().format(FORMATTER), order.getOrderTime()));
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            buttons.add(getInlineKeyboardButton(order.getOrderTime() + EMPTY_DATA + order.getOrderDate(), outputData));
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
        dayOfWeek.add(getInlineKeyboardButton(BACK, String.format(JSON_COMMAND_RETURN_BACK, command, date.format(FORMATTER))));
        return dayOfWeek;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public InlineKeyboardMarkup getServiceCatalog(ButtonDto buttonDto) throws CustomBotException {
        List<ServiceCatalog> serviceCatalogs = serviceCatalogRepository.findAllByActiveService(true);
        if (serviceCatalogs.isEmpty()){
            throw new CustomBotException(ERROR_SERVICE_NOT_AVAILABLE);
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        if (serviceCatalogs.size() == 1) {
            buttonDto.setServiceId(serviceCatalogs.get(0).getServiceId());
            return getAvailableTimeKeyboard(buttonDto);
        }
        for (ServiceCatalog service : serviceCatalogs
        ) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            String text = String.format(SERVICE_BUTTON_TEXT, service.getName(), service.getPrice());
            String previousDate = buttonDto.getPreviousDate() == null ? null : buttonDto.getPreviousDate().format(FORMATTER);
            String nextDate = buttonDto.getNextDate() == null ? null : buttonDto.getNextDate().format(FORMATTER);
            String data = String.format(JSON_COMMAND_SELECT_SERVICE, WORKDAY, buttonDto.getCurrentDate().format(FORMATTER),
                    previousDate,
                    nextDate,
                    service.getServiceId());
            row.add(getInlineKeyboardButton(text, data));
            rowList.add(row);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }
}
