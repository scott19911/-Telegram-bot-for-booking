package com.heroku.nwl.service;

import com.heroku.nwl.config.CustomBotException;
import com.heroku.nwl.constants.Commands;
import com.heroku.nwl.dto.CalendarDayDto;
import com.heroku.nwl.model.Role;
import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.heroku.nwl.constants.Commands.ALL_RESERVATION;
import static com.heroku.nwl.constants.Constants.ADD_DAY_OFF;
import static com.heroku.nwl.constants.Constants.CHOOSE_DATE;
import static com.heroku.nwl.constants.Constants.CHOOSE_DAY_OFF;
import static com.heroku.nwl.constants.Constants.ERROR;
import static com.heroku.nwl.constants.Constants.HELP_TEXT;
import static com.heroku.nwl.constants.Constants.SHARE_PHONE;
import static com.heroku.nwl.constants.Constants.USER_RESERVATION;
import static com.heroku.nwl.constants.Constants.WELCOME_TEXT;
import static com.heroku.nwl.constants.Constants.YOUR_ID;
import static com.heroku.nwl.constants.ErrorMessage.ERROR_PERMISSION;

@RequiredArgsConstructor
@Slf4j
@Service
public class MessageHandler {

    private final Calendar calendarService;
    private final KeyboardService keyboardService;
    private final UserService userService;

    public SendMessage getMessage(Update update) throws CustomBotException {
        SendMessage message;
        String messageText = update.getMessage().getText();
        LocalDate currentDate = LocalDate.now();
        long chatId = update.getMessage().getChatId();
        switch (messageText) {
            case Commands.START -> message = startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
            case Commands.HELP -> message = prepareSendMessage(chatId, HELP_TEXT, null);
            case Commands.ADMIN_CALENDAR ->
                 message = getCalendarSendMessage(currentDate, chatId, ADD_DAY_OFF, CHOOSE_DAY_OFF);
            case Commands.ADMIN_CALENDAR_RESERVE ->
                    message = getCalendarSendMessage(currentDate, chatId, ALL_RESERVATION, CHOOSE_DATE);
            case Commands.AVAILABLE_DATE_TO_RESERVE ->
                    message = prepareSendMessage(chatId, CHOOSE_DATE, keyboardService.getScheduleDays());
            case Commands.SHOW_USER_RESERVATION ->
                    message = prepareSendMessage(chatId, USER_RESERVATION, keyboardService.getUserReservation(chatId));
            case Commands.CHAT_ID -> message = prepareSendMessage(chatId, YOUR_ID + chatId, null);
            case Commands.CONTACTS -> message = prepareSendMessage(chatId,userService.getContact(),null);
            default -> message = prepareSendMessage(chatId, ERROR, null);
        }
        return message;
    }

    private SendMessage getCalendarSendMessage(LocalDate currentDate, long chatId, String allReservation, String chooseDate) throws CustomBotException {
        SendMessage message;
        if (!userService.getUserRole(chatId).equals(Role.ADMIN)) {
            throw new CustomBotException(ERROR_PERMISSION);
        }
        List<List<CalendarDayDto>> calendar = calendarService.getCalendar(currentDate, allReservation);
        InlineKeyboardMarkup keyboardMarkup = keyboardService.getCalendar(calendar, currentDate);
        message = prepareSendMessage(chatId, chooseDate, keyboardMarkup);
        return message;
    }

    private SendMessage startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode(String.format(WELCOME_TEXT,name));
        log.info("Replied to user " + name);
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(answer);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton();
        keyboardButton.setText(SHARE_PHONE);
        keyboardButton.setRequestContact(true);
        keyboardFirstRow.add(keyboardButton);
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return message;
    }

    public SendMessage prepareSendMessage(long chatId, String textToSend, InlineKeyboardMarkup replyKeyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setReplyMarkup(replyKeyboardMarkup);
        return message;
    }
}
