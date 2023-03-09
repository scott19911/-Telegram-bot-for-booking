package com.heroku.nwl.service;

import com.heroku.nwl.model.ReservationRepository;
import com.heroku.nwl.model.Reservation;
import com.heroku.nwl.model.Role;
import com.heroku.nwl.model.User;
import com.heroku.nwl.model.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

@Service
public class NotificationService {
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public NotificationService(UserRepository userRepository,
                               ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<SendMessage> adminNotify(Long chatId, LocalDate reserveDate, LocalTime reserveTime,String message) {
        List<User> adminList = userRepository.findByRole(Role.ADMIN);
        User user = userRepository.findByChatId(chatId);
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
        String text = new Formatter().format("Ваше бронювання на %s %s було скасованно адміністратором",
                order.getOrderTime(), order.getOrderDate()).toString();
        return prepareMessage(order.getUser().getChatId(), text);
    }

    private SendMessage prepareMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        return message;
    }

}
