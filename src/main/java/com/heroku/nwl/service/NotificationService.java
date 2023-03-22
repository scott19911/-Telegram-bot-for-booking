package com.heroku.nwl.service;

import com.heroku.nwl.config.CustomBotException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface NotificationService {

    List<SendMessage> sendMessageFromAdmin(Update update) throws CustomBotException;

    List<SendMessage> adminNotify(Long chatId, LocalDate orderDate, LocalTime orderTime, String messageText);

    SendMessage notifyUser(Long reservationId);

    List<SendMessage> changeReservationStatus();

    SendMessage userNotifyAboutSession();
}
