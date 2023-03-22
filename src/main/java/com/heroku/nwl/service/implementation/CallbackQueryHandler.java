package com.heroku.nwl.service.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heroku.nwl.config.CustomBotException;
import com.heroku.nwl.constants.Constants;
import com.heroku.nwl.dto.ButtonDto;
import com.heroku.nwl.dto.CalendarDayDto;
import com.heroku.nwl.model.DayOff;
import com.heroku.nwl.model.DayOffRepository;
import com.heroku.nwl.model.Reservation;
import com.heroku.nwl.model.ReservationRepository;
import com.heroku.nwl.model.ReservationStatus;
import com.heroku.nwl.model.Role;
import com.heroku.nwl.service.CalendarService;
import com.heroku.nwl.service.KeyboardService;
import com.heroku.nwl.service.ReservationService;
import com.heroku.nwl.service.UserService;
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

import static com.heroku.nwl.constants.Commands.ALL_RESERVATION;
import static com.heroku.nwl.constants.Commands.AVAILABLE_DATE_TO_RESERVE;
import static com.heroku.nwl.constants.Commands.CANCEL_RESERVE;
import static com.heroku.nwl.constants.Commands.CHANGE_MONTH;
import static com.heroku.nwl.constants.Commands.CHANGE_RESERVATION_STATUS;
import static com.heroku.nwl.constants.Commands.DELETE_RESERVE;
import static com.heroku.nwl.constants.Commands.GO_BACK;
import static com.heroku.nwl.constants.Commands.ORDER_TIME;
import static com.heroku.nwl.constants.Commands.SELECT_SERVICE;
import static com.heroku.nwl.constants.Commands.WORKDAY;
import static com.heroku.nwl.constants.Constants.ACTIVE_BOOKING;
import static com.heroku.nwl.constants.Constants.ADD_DAY_OFF;
import static com.heroku.nwl.constants.Constants.CHOOSE_DATE;
import static com.heroku.nwl.constants.Constants.CHOOSE_DAY_OFF;
import static com.heroku.nwl.constants.Constants.CHOOSE_SERVICE;
import static com.heroku.nwl.constants.Constants.DATE_PATTERN;
import static com.heroku.nwl.constants.Constants.DELETE_DAY_OFF;
import static com.heroku.nwl.constants.Constants.DELETE_RESERVATION_MESSAGE;
import static com.heroku.nwl.constants.Constants.ERROR;
import static com.heroku.nwl.constants.Constants.ERROR_DELETE_RESERVATION_MESSAGE;
import static com.heroku.nwl.constants.Constants.TIME_ALREADY_TAKEN;
import static com.heroku.nwl.constants.Constants.USER_NEW_RESERVATION_MESSAGE;
import static com.heroku.nwl.constants.ErrorMessage.ERROR_NEED_REGISTER;
import static com.heroku.nwl.constants.ErrorMessage.ERROR_PERMISSION;




@Service
@RequiredArgsConstructor
public class CallbackQueryHandler {
    private final CalendarService calendarService;
    private final ReservationService reservationService;
    private final DayOffRepository dayOffRepository;
    private final KeyboardService keyboardService;
    private final ReservationRepository reservationRepository;
    private final UserService userService;

    public EditMessageText getEditMessage(Update update) throws CustomBotException {
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
            case ALL_RESERVATION -> {
                if (!userService.getUserRole(chatId).equals(Role.ADMIN)) {
                    throw new CustomBotException(ERROR_PERMISSION);
                }
                yield prepareEditMessageText(
                        CHOOSE_DATE,
                        chatId,
                        messageId,
                        keyboardService.getReservationOnDate(buttonDto.getCurrentDate()));
            }
            case ADD_DAY_OFF -> getAddDayOffMessage(buttonDto, text, chatId, messageId);
            case DELETE_DAY_OFF -> getDeleteDayOffMessage(buttonDto, text, chatId, messageId);
            case WORKDAY -> getWorkDayMessage(buttonDto, chatId, messageId);
            case ORDER_TIME -> getReservationOnTimeMessage(buttonDto, chatId, messageId);
            case DELETE_RESERVE -> getDeleteReservationMessage(buttonDto, chatId, messageId);
            case CANCEL_RESERVE -> canselReservation(buttonDto, chatId, messageId);
            case GO_BACK -> getPriviesMenuMessage(buttonDto, chatId, messageId);
            case SELECT_SERVICE -> getServiceCatalog(buttonDto, chatId, messageId);
            case CHANGE_RESERVATION_STATUS -> getChangeReservationStatus(buttonDto, chatId, messageId);
            default -> prepareEditMessageText(ERROR, chatId, messageId, null);
        };
        return message;
    }

    private EditMessageText getChangeReservationStatus(ButtonDto buttonDto, long chatId, long messageId) throws CustomBotException {
        if (!userService.getUserRole(chatId).equals(Role.ADMIN)) {
            throw new CustomBotException(ERROR_PERMISSION);
        }
        reservationService.changeStatus(buttonDto.getReservationId(), buttonDto.getReservationStatus());
        return prepareEditMessageText(
                Constants.CHANGE_RESERVATION_STATUS + buttonDto.getReservationStatus(),
                chatId,
                messageId,
                null);
    }

    private EditMessageText getChangeMonthMessage(ButtonDto buttonDto, Long chatId, Long messageId) throws CustomBotException {
        if (!userService.getUserRole(chatId).equals(Role.ADMIN)) {
            throw new CustomBotException(ERROR_PERMISSION);
        }
        List<List<CalendarDayDto>> calendar = calendarService.getCalendar(buttonDto.getCurrentDate(), buttonDto.getReturnTo());
        InlineKeyboardMarkup keyboardMarkup = keyboardService.getCalendar(calendar, buttonDto.getCurrentDate());
        return prepareEditMessageText(CHOOSE_DATE, chatId, messageId, keyboardMarkup);
    }

    private EditMessageText getCreateCalendarMessage(ButtonDto buttonDto, String text, long chatId, long messageId) throws CustomBotException {
        if (!userService.getUserRole(chatId).equals(Role.ADMIN)) {
            throw new CustomBotException(ERROR_PERMISSION);
        }
        List<List<CalendarDayDto>> calendar = calendarService.getCalendar(buttonDto.getCurrentDate(), ADD_DAY_OFF);
        InlineKeyboardMarkup keyboardMarkup = keyboardService.getCalendar(calendar, buttonDto.getCurrentDate());
        return prepareEditMessageText(text, chatId, messageId, keyboardMarkup);
    }

    private EditMessageText getAddDayOffMessage(ButtonDto buttonDto, String text, long chatId, long messageId) throws CustomBotException {
        if (!userService.getUserRole(chatId).equals(Role.ADMIN)) {
            throw new CustomBotException(ERROR_PERMISSION);
        }
        DayOff dayOff = new DayOff(buttonDto.getCurrentDate());
        List<Reservation> reservationList = reservationService.getReservationsByDate(dayOff.getDayOffDate());
        if(!reservationList.isEmpty()){
            for (Reservation reservation:reservationList
                 ) {
                if(reservation.getReservationStatus().equals(ReservationStatus.ACTIVE)){
                    return getCreateCalendarMessage(buttonDto, ACTIVE_BOOKING, chatId, messageId);
                }
            }
        }
        if (LocalDate.now().isBefore(dayOff.getDayOffDate())) {
            dayOffRepository.save(dayOff);
            text = CHOOSE_DAY_OFF;
        }
        return getCreateCalendarMessage(buttonDto, text, chatId, messageId);
    }

    private EditMessageText getReservationOnTimeMessage(ButtonDto buttonDto, long chatId, long messageId) throws CustomBotException {
        EditMessageText message;
        if (userService.getUserById(chatId) == null) {
            throw new CustomBotException(ERROR_NEED_REGISTER);
        }
        LocalDate orderDate = buttonDto.getCurrentDate();
        LocalTime orderTime = buttonDto.getReservedTime();
        if (reservationService.createReservation(orderTime, orderDate, chatId, buttonDto.getServiceId())) {
            message = prepareEditMessageText(String.format(USER_NEW_RESERVATION_MESSAGE, orderDate, orderTime), chatId, messageId, null);
        } else {
            message = prepareEditMessageText(TIME_ALREADY_TAKEN, chatId, messageId, null);
        }
        return message;
    }

    private EditMessageText getDeleteReservationMessage(ButtonDto buttonDto, long chatId, long messageId) {
        if (reservationService.canselReservation(buttonDto.getReservationId())) {
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

    private EditMessageText getDeleteDayOffMessage(ButtonDto buttonDto, String text, long chatId, long messageId) throws CustomBotException {
        if (!userService.getUserRole(chatId).equals(Role.ADMIN)) {
            throw new CustomBotException(ERROR_PERMISSION);
        }
        DayOff dayOff = new DayOff(buttonDto.getCurrentDate());
        if (LocalDate.now().isBefore(dayOff.getDayOffDate())) {
            dayOffRepository.delete(dayOff);
            text = CHOOSE_DAY_OFF;
        }
        return getCreateCalendarMessage(buttonDto, text, chatId, messageId);
    }

    private EditMessageText getWorkDayMessage(ButtonDto buttonDto, long chatId, long messageId) throws CustomBotException {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        LocalDate currentDate = buttonDto.getCurrentDate();
        String formattedDate = currentDate.getDayOfWeek() + "\n" + currentDate.format(dateTimeFormatter);
        return prepareEditMessageText(
                formattedDate,
                chatId,
                messageId,
                keyboardService.getAvailableTimeKeyboard(buttonDto));
    }

    private EditMessageText getServiceCatalog(ButtonDto buttonDto, long chatId, long messageId) throws CustomBotException {
        return prepareEditMessageText(
                CHOOSE_SERVICE,
                chatId,
                messageId,
                keyboardService.getServiceCatalog(buttonDto));
    }

    private EditMessageText getPriviesMenuMessage(ButtonDto buttonDto, long chatId, long messageId) {
        EditMessageText message = prepareEditMessageText(ERROR, chatId, messageId, null);
        if (Objects.equals(buttonDto.getReturnTo(), AVAILABLE_DATE_TO_RESERVE)) {
            message = prepareEditMessageText(CHOOSE_DATE, chatId, messageId, keyboardService.getScheduleDays());
        }
        if (buttonDto.getReturnTo().equals(ALL_RESERVATION)) {
            List<List<CalendarDayDto>> calendar = calendarService.getCalendar(buttonDto.getCurrentDate(), ALL_RESERVATION);
            InlineKeyboardMarkup keyboardMarkup = keyboardService.getCalendar(calendar, buttonDto.getCurrentDate());
            message = prepareEditMessageText(CHOOSE_DATE, chatId, messageId, keyboardMarkup);
        }
        return message;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EditMessageText canselReservation(ButtonDto buttonDto, Long chatId, Long messageId) throws CustomBotException {
        if (!userService.getUserRole(chatId).equals(Role.ADMIN)) {
            throw new CustomBotException(ERROR_PERMISSION);
        }
        Reservation order = reservationRepository.findByOrderId(buttonDto.getReservationId());
        reservationService.canselReservation(buttonDto.getReservationId());
        String text = String.format(Constants.MESSAGE_USER_CANSEL_RESERVE, order.getOrderTime(), order.getOrderDate());
        return prepareEditMessageText(
                text,
                chatId,
                messageId,
                keyboardService.getReservationOnDate(buttonDto.getCurrentDate()));
    }

    private EditMessageText prepareEditMessageText(String text,
                                                   long chatId,
                                                   long messageId,
                                                   InlineKeyboardMarkup inlineKeyboardMarkup) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }
}
