package com.heroku.nwl.service;

import com.heroku.nwl.model.DayOff;
import com.heroku.nwl.model.DayOffRepository;
import com.heroku.nwl.model.OrderRepository;
import com.heroku.nwl.model.Orders;
import lombok.Data;
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

import static com.heroku.nwl.service.CalendarForBot.ADD_DAY_OFF;
import static com.heroku.nwl.service.CalendarForBot.CHANGE_MONTH;
import static com.heroku.nwl.service.CalendarForBot.DELETE_DAY_OFF;
import static com.heroku.nwl.service.KeyboardService.ORDER_TIME;
import static com.heroku.nwl.service.KeyboardService.WORKDAY;

@Service
@RequiredArgsConstructor
public class CallbackQueryHandler {
    public static final String DELETE_RESERVE = "deleteReservation";
    private final CalendarForBot calendarService;
    private final ReservationService reservationService;
    private final DayOffRepository dayOffRepository;
    private final KeyboardService keyboardService;
    private final OrderRepository orderRepository;

    public EditMessageText getEditMessage(Update update) {
        EditMessageText message = new EditMessageText();
        String callbackData = update.getCallbackQuery().getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (callbackData.contains(CHANGE_MONTH)) {
            if (callbackData.contains(ADD_DAY_OFF)) {
                message = prepareEditMessageText("Оберіть вихідні", chatId, messageId, calendarService.changeMonth(callbackData, ADD_DAY_OFF));
            } else {
                message = prepareEditMessageText("Оберіть вихідні", chatId, messageId, calendarService.changeMonth(callbackData, "show"));
            }
        }
        if (callbackData.startsWith("show")) {
            LocalDate localDate1 = LocalDate.parse(callbackData.replace("show", ""));
            message = prepareEditMessageText("Оберіть дату: ", chatId, messageId, keyboardService.getReservationOnDate(localDate1));
        }
        if (callbackData.contains(ADD_DAY_OFF)) {
            LocalDate localDate1 = LocalDate.parse(callbackData.replace(ADD_DAY_OFF, ""));
            DayOff dayOff = new DayOff(localDate1);
            dayOffRepository.save(dayOff);
            InlineKeyboardMarkup inlineKeyboardMarkup1 = calendarService.getCalendarKeyboard(localDate1, ADD_DAY_OFF);
            message = prepareEditMessageText("Оберіть вихідні", chatId, messageId, inlineKeyboardMarkup1);
        }
        if (callbackData.contains(DELETE_DAY_OFF)) {
            LocalDate localDate1 = LocalDate.parse(callbackData.replace(DELETE_DAY_OFF, ""));
            DayOff dayOff = new DayOff(localDate1);
            dayOffRepository.delete(dayOff);
            InlineKeyboardMarkup inlineKeyboardMarkup1 = calendarService.getCalendarKeyboard(localDate1, ADD_DAY_OFF);
            message = prepareEditMessageText("Оберіть вихідні", chatId, messageId, inlineKeyboardMarkup1);
        }
        if (callbackData.contains(WORKDAY)) {
            String textDate = callbackData.replace(WORKDAY, "");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(textDate.split(";")[0]);
            String formattedDate = date.getDayOfWeek() + "\n" + date.format(dateTimeFormatter);
            message = prepareEditMessageText(formattedDate, chatId, messageId, keyboardService.getAvailableTimeKeyboard(textDate));
        }
        if (callbackData.contains(ORDER_TIME)) {
            String[] orderData = callbackData.replace(ORDER_TIME, "").split(";");
            LocalDate orderDate = LocalDate.parse(orderData[0]);
            LocalTime orderTime = LocalTime.parse(orderData[1]);
            if (reservationService.createReservation(orderTime, orderDate, chatId)) {
                message = prepareEditMessageText("You are reserve " + orderDate + " " + orderTime, chatId, messageId, null);
            } else {
                message = prepareEditMessageText("Sorry this time is already taken or you don't register", chatId, messageId, null);
            }
        }
        if (callbackData.contains(DELETE_RESERVE)) {
            String orderIdString = callbackData.replace(DELETE_RESERVE, "");
            Long orderId = Long.parseLong(orderIdString);
            reservationService.deleteReservation(orderId);
            message = prepareEditMessageText("You are delete reserve ", chatId, messageId, keyboardService.getUserReservation(chatId));
        }
        if (callbackData.contains("canselReservation")) {
            message = canselReservation(callbackData, chatId, messageId);
        }
        return message;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EditMessageText canselReservation(String callbackData, Long chatId, Long messageId) {
        String orderIdString = callbackData.replace("canselReservation", "");
        String[] dataString = orderIdString.split(";");
        Long orderId = Long.parseLong(dataString[0]);
        Orders order = orderRepository.findByOrderId(orderId);
        LocalDate date = LocalDate.parse(dataString[1]);
        reservationService.deleteReservation(orderId);
        String text = "You are cansel reserve " + order.getOrderTime() + " " + order.getOrderDate();
        return prepareEditMessageText(text, chatId, messageId, keyboardService.getReservationOnDate(date));
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
