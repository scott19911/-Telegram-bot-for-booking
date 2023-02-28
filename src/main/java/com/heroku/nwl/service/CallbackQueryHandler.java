package com.heroku.nwl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heroku.nwl.constants.Constants;
import com.heroku.nwl.dto.ButtonDto;
import com.heroku.nwl.dto.CalendarDayDto;
import com.heroku.nwl.model.DayOff;
import com.heroku.nwl.model.DayOffRepository;
import com.heroku.nwl.model.OrderRepository;
import com.heroku.nwl.model.Orders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static com.heroku.nwl.constants.Commands.ALL_RESERVATION_ON_DATE;
import static com.heroku.nwl.constants.Commands.AVAILABLE_DATE_TO_RESERVE;
import static com.heroku.nwl.constants.Commands.CANCEL_RESERVE;
import static com.heroku.nwl.constants.Commands.CHANGE_MONTH;
import static com.heroku.nwl.constants.Commands.DELETE_RESERVE;
import static com.heroku.nwl.constants.Commands.GO_BACK;
import static com.heroku.nwl.constants.Commands.ORDER_TIME;
import static com.heroku.nwl.constants.Commands.WORKDAY;
import static com.heroku.nwl.constants.Constants.CHOOSE_DATE;
import static com.heroku.nwl.constants.Constants.CHOOSE_DAY_OFF;
import static com.heroku.nwl.constants.Constants.DATE_PATTERN;
import static com.heroku.nwl.constants.Constants.DELETE_RESERVATION_MESSAGE;
import static com.heroku.nwl.constants.Constants.ERROR;
import static com.heroku.nwl.constants.Constants.ERROR_DELETE_RESERVATION_MESSAGE;
import static com.heroku.nwl.service.Calendar.ADD_DAY_OFF;
import static com.heroku.nwl.service.Calendar.DELETE_DAY_OFF;



@Service
@RequiredArgsConstructor
public class CallbackQueryHandler {
    private final Calendar calendarService;
    private final ReservationService reservationService;
    private final DayOffRepository dayOffRepository;
    private final KeyboardService keyboardService;
    private final OrderRepository orderRepository;

    public EditMessageText getEditMessage(Update update) {
        EditMessageText message;
        String callbackData = update.getCallbackQuery().getData();
        String text = Constants.INCORRECT_DATE;
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        ButtonDto buttonDto;
        try {
            buttonDto = new ObjectMapper().readValue(callbackData, ButtonDto.class);
        } catch (JsonProcessingException e) {
            return prepareEditMessageText(ERROR, chatId, messageId, null);
        }
        message = switch (buttonDto.getCommand()) {
            case CHANGE_MONTH -> getChangeMonthMessage(buttonDto, chatId, messageId);
            case ALL_RESERVATION_ON_DATE -> prepareEditMessageText(
                    CHOOSE_DATE,
                    chatId,
                    messageId,
                    keyboardService.getReservationOnDate(buttonDto.getCurrentDate()));
            case ADD_DAY_OFF -> getAddDayOffMessage(buttonDto, text, chatId, messageId);
            case DELETE_DAY_OFF -> getDeleteDayOffMessage(buttonDto, text, chatId, messageId);
            case WORKDAY -> getWorkDayMessage(buttonDto, chatId, messageId);
            case ORDER_TIME -> getReservationOnTimeMessage(buttonDto, chatId, messageId);
            case DELETE_RESERVE -> getDeleteReservationMessage(buttonDto, chatId, messageId);
            case CANCEL_RESERVE -> canselReservation(buttonDto, chatId, messageId);
            case GO_BACK -> getPriviesMenuMessage(buttonDto, chatId, messageId);
            default -> prepareEditMessageText(ERROR, chatId, messageId, null);
        };
        return message;
    }

    private EditMessageText getChangeMonthMessage(ButtonDto buttonDto, Long chatId, Long messageId) {
        List<List<CalendarDayDto>> calendar = calendarService.getCalendar(buttonDto.getCurrentDate(), buttonDto.getReturnTo());
        InlineKeyboardMarkup keyboardMarkup = keyboardService.getCalendar(calendar, buttonDto.getCurrentDate());
        return prepareEditMessageText(CHOOSE_DAY_OFF, chatId, messageId, keyboardMarkup);
    }

    private EditMessageText getCreateCalendarMessage(ButtonDto buttonDto, String text, long chatId, long messageId) {
        List<List<CalendarDayDto>> calendar = calendarService.getCalendar(buttonDto.getCurrentDate(), ADD_DAY_OFF);
        InlineKeyboardMarkup keyboardMarkup = keyboardService.getCalendar(calendar, buttonDto.getCurrentDate());
        return prepareEditMessageText(text, chatId, messageId, keyboardMarkup);
    }

    private EditMessageText getAddDayOffMessage(ButtonDto buttonDto, String text, long chatId, long messageId) {
        DayOff dayOff = new DayOff(buttonDto.getCurrentDate());
        if (LocalDate.now().isBefore(dayOff.getDayOffDate())) {
            dayOffRepository.save(dayOff);
            text = CHOOSE_DAY_OFF;
        }
        return getCreateCalendarMessage(buttonDto, text, chatId, messageId);
    }

    private EditMessageText getReservationOnTimeMessage(ButtonDto buttonDto, long chatId, long messageId) {
        EditMessageText message;
        LocalDate orderDate = buttonDto.getCurrentDate();
        LocalTime orderTime = buttonDto.getReservedTime();
        if (reservationService.createReservation(orderTime, orderDate, chatId)) {
            message = prepareEditMessageText("You are reserve " + orderDate + " " + orderTime, chatId, messageId, null);
        } else {
            message = prepareEditMessageText("Sorry this time is already taken or you don't register", chatId, messageId, null);
        }
        return message;
    }

    private EditMessageText getDeleteReservationMessage(ButtonDto buttonDto, long chatId, long messageId) {
        if (reservationService.deleteReservation(buttonDto.getReservationId())) {
            return prepareEditMessageText(
                    DELETE_RESERVATION_MESSAGE,
                    chatId,
                    messageId,
                    keyboardService.getUserReservation(chatId));
        }
        return prepareEditMessageText(
                ERROR_DELETE_RESERVATION_MESSAGE,
                chatId,
                messageId,
                keyboardService.getUserReservation(chatId));
    }

    private EditMessageText getDeleteDayOffMessage(ButtonDto buttonDto, String text, long chatId, long messageId) {
        DayOff dayOff = new DayOff(buttonDto.getCurrentDate());
        if (LocalDate.now().isBefore(dayOff.getDayOffDate())) {
            dayOffRepository.delete(dayOff);
            text = CHOOSE_DAY_OFF;
        }
        return getCreateCalendarMessage(buttonDto, text, chatId, messageId);
    }

    private EditMessageText getWorkDayMessage(ButtonDto buttonDto, long chatId, long messageId) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        LocalDate currentDate = buttonDto.getCurrentDate();
        String formattedDate = currentDate.getDayOfWeek() + "\n" + currentDate.format(dateTimeFormatter);
        return prepareEditMessageText(
                formattedDate,
                chatId,
                messageId,
                keyboardService.getAvailableTimeKeyboard(buttonDto));
    }

    private EditMessageText getPriviesMenuMessage(ButtonDto buttonDto, long chatId, long messageId) {
        EditMessageText message = prepareEditMessageText(ERROR, chatId, messageId, null);
        if (Objects.equals(buttonDto.getReturnTo(), AVAILABLE_DATE_TO_RESERVE)) {
            message = prepareEditMessageText(CHOOSE_DATE, chatId, messageId, keyboardService.getScheduleDays());
        }
        if (buttonDto.getReturnTo().equals(ALL_RESERVATION_ON_DATE)) {
            List<List<CalendarDayDto>> calendar = calendarService.getCalendar(buttonDto.getCurrentDate(), ALL_RESERVATION_ON_DATE);
            InlineKeyboardMarkup keyboardMarkup = keyboardService.getCalendar(calendar, buttonDto.getCurrentDate());
            message = prepareEditMessageText(CHOOSE_DATE, chatId, messageId, keyboardMarkup);
        }

        return message;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EditMessageText canselReservation(ButtonDto buttonDto, Long chatId, Long messageId) {
        Orders order = orderRepository.findByOrderId(buttonDto.getReservationId());
        reservationService.deleteReservation(buttonDto.getReservationId());
        String text = "You are cansel reserve " + order.getOrderTime() + " " + order.getOrderDate();
        return prepareEditMessageText(text, chatId, messageId, keyboardService.getReservationOnDate(buttonDto.getCurrentDate()));
    }

    private EditMessageText prepareEditMessageText(String text, long chatId, long messageId, InlineKeyboardMarkup inlineKeyboardMarkup) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }
}
