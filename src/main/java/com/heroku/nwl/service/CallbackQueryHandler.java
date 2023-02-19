package com.heroku.nwl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static com.heroku.nwl.constants.Commands.ALL_RESERVATION_ON_DATE;
import static com.heroku.nwl.constants.Commands.CANCEL_RESERVE;
import static com.heroku.nwl.constants.Commands.CHANGE_MONTH;
import static com.heroku.nwl.constants.Commands.DELETE_RESERVE;
import static com.heroku.nwl.constants.Commands.ORDER_TIME;
import static com.heroku.nwl.constants.Commands.WORKDAY;
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
        EditMessageText message = new EditMessageText();
        String callbackData = update.getCallbackQuery().getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        ButtonDto buttonDto;
        try {
            buttonDto = new ObjectMapper().readValue(callbackData, ButtonDto.class);
        } catch (JsonProcessingException e) {
            buttonDto = null;
        }
        if (buttonDto != null && buttonDto.getCommand().equals(CHANGE_MONTH)) {
            List<List<CalendarDayDto>> calendar = calendarService.getCalendar(buttonDto.getCurrentDate(), ADD_DAY_OFF);
            InlineKeyboardMarkup keyboardMarkup = keyboardService.getCalendar(calendar, buttonDto.getCurrentDate());
            message = prepareEditMessageText("Оберіть вихідні", chatId, messageId, keyboardMarkup);
        }
        if (buttonDto != null && buttonDto.getCommand().equals(ALL_RESERVATION_ON_DATE)) {
            message = prepareEditMessageText("Оберіть дату: ", chatId, messageId, keyboardService.getReservationOnDate(buttonDto.getCurrentDate()));
        }
        if (buttonDto != null && buttonDto.getCommand().equals(ADD_DAY_OFF)) {
            DayOff dayOff = new DayOff(buttonDto.getCurrentDate());
            dayOffRepository.save(dayOff);
            List<List<CalendarDayDto>> calendar = calendarService.getCalendar(buttonDto.getCurrentDate(), ADD_DAY_OFF);
            InlineKeyboardMarkup keyboardMarkup = keyboardService.getCalendar(calendar, buttonDto.getCurrentDate());
            message = prepareEditMessageText("Оберіть вихідні", chatId, messageId, keyboardMarkup);
        }
        if (buttonDto != null && buttonDto.getCommand().equals(DELETE_DAY_OFF)) {
            DayOff dayOff = new DayOff(buttonDto.getCurrentDate());
            dayOffRepository.delete(dayOff);
            List<List<CalendarDayDto>> calendar = calendarService.getCalendar(buttonDto.getCurrentDate(), ADD_DAY_OFF);
            InlineKeyboardMarkup keyboardMarkup = keyboardService.getCalendar(calendar, buttonDto.getCurrentDate());
            message = prepareEditMessageText("Оберіть вихідні", chatId, messageId, keyboardMarkup);
        }
        if (buttonDto != null && buttonDto.getCommand().equals(WORKDAY)) {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate currentDate = buttonDto.getCurrentDate();
            String formattedDate = currentDate.getDayOfWeek() + "\n" + currentDate.format(dateTimeFormatter);
            message = prepareEditMessageText(formattedDate, chatId, messageId, keyboardService.getAvailableTimeKeyboard(buttonDto));
        }
        if (buttonDto != null && buttonDto.getCommand().equals(ORDER_TIME)) {
            LocalDate orderDate = buttonDto.getCurrentDate();
            LocalTime orderTime = buttonDto.getReservedTime();
            if (reservationService.createReservation(orderTime, orderDate, chatId)) {
                message = prepareEditMessageText("You are reserve " + orderDate + " " + orderTime, chatId, messageId, null);
            } else {
                message = prepareEditMessageText("Sorry this time is already taken or you don't register", chatId, messageId, null);
            }
        }
        if (buttonDto != null && buttonDto.getCommand().equals(DELETE_RESERVE)) {
            reservationService.deleteReservation(buttonDto.getReservationId());
            message = prepareEditMessageText("You are delete reserve ", chatId, messageId, keyboardService.getUserReservation(chatId));
        }
        if (buttonDto != null && buttonDto.getCommand().equals(CANCEL_RESERVE)) {
            message = canselReservation(buttonDto, chatId, messageId);
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
