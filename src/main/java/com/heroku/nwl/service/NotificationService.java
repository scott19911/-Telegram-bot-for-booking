package com.heroku.nwl.service;

import com.heroku.nwl.config.CustomBotException;
import com.heroku.nwl.constants.Commands;
import com.heroku.nwl.model.Reservation;
import com.heroku.nwl.model.ReservationRepository;
import com.heroku.nwl.model.ReservationStatus;
import com.heroku.nwl.model.Role;
import com.heroku.nwl.model.User;
import com.heroku.nwl.model.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import static com.heroku.nwl.constants.Constants.CAME_CLIENT;
import static com.heroku.nwl.constants.Constants.MINUTES_CHECK_CAME_USER;
import static com.heroku.nwl.constants.Constants.NOTIFY_SESSION;
import static com.heroku.nwl.constants.Constants.USER_CANCEL_RESERVATION_MESSAGE;
import static com.heroku.nwl.constants.ErrorMessage.ERROR_PERMISSION;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final KeyboardService keyboardService;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<SendMessage> adminNotify(Long chatId, LocalDate reserveDate, LocalTime reserveTime, String message) {
        List<User> adminList = userService.getUsersByRole(Role.ADMIN);
        User user = userService.getUserById(chatId);
        List<SendMessage> messages = new ArrayList<>();
        String messageText = new Formatter().format(message
                , reserveTime, reserveDate, user.getFirstName(), user.getPhoneNumber()).toString();
        for (User admin : adminList) {
            messages.add(prepareMessage(admin.getChatId(), messageText));
        }
        return messages;
    }

    public SendMessage notifyUser(Long orderId) {
        Reservation order = reservationRepository.findByOrderId(orderId);
        String text = new Formatter().format(USER_CANCEL_RESERVATION_MESSAGE,
                order.getOrderTime(), order.getOrderDate()).toString();
        return prepareMessage(order.getUser().getChatId(), text);
    }

    private SendMessage prepareMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        return message;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<SendMessage> sendMessageFromAdmin(Update update) throws CustomBotException {
        List<SendMessage> messageList = new ArrayList<>();
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        if(messageText.startsWith(Commands.SEND_MESSAGE)) {
            if (!userService.getUserRole(chatId).equals(Role.ADMIN)) {
                throw new CustomBotException(ERROR_PERMISSION);
            }
            String text = messageText.replace(Commands.SEND_MESSAGE, "");
            List<User> users = userService.getAllUser();
            for (User user : users
            ) {
                SendMessage message = prepareMessage(user.getChatId(), text);
                messageList.add(message);
            }
            return messageList;
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<SendMessage> changeReservationStatus() {
        Reservation reservation = reservationRepository
                .findByOrderDateAndOrderTimeAndReservationStatus(
                        LocalDate.now(),
                        LocalTime.now().minusMinutes(MINUTES_CHECK_CAME_USER),
                        ReservationStatus.ACTIVE);
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        List<SendMessage> messageList = new ArrayList<>();
        if (reservation != null) {
            for (User admin : admins
            ) {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(admin.getChatId()));
                message.setText(
                        String.format(
                                CAME_CLIENT,
                                reservation.getUser().getFirstName(),
                                reservation.getOrderDate(),
                                reservation.getOrderTime()));
                message.setReplyMarkup(keyboardService.changeReservationStatus(reservation));
                messageList.add(message);
            }
        }
        return messageList;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SendMessage userNotifyAboutSession() {
        SendMessage message = new SendMessage();
        Reservation reservation = reservationRepository
                .findByOrderDateAndOrderTimeAndReservationStatus(
                        LocalDate.now(),
                        LocalTime.now().plusHours(MINUTES_CHECK_CAME_USER),
                        ReservationStatus.ACTIVE);

        if (reservation != null) {
            message.setChatId(String.valueOf(reservation.getUser().getChatId()));
            message.setText(String.format(NOTIFY_SESSION, reservation.getOrderTime()));
            return message;
        }
        return null;
    }
}
