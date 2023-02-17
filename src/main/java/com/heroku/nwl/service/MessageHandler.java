package com.heroku.nwl.service;

import com.heroku.nwl.constants.Commands;
import com.heroku.nwl.model.DayOffRepository;
import com.vdurmont.emoji.EmojiParser;
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

import static com.heroku.nwl.service.CalendarForBot.ADD_DAY_OFF;

@Slf4j
@Service
public class MessageHandler {
    public static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /mydata to see data stored about yourself\n\n" +
            "Type /help to see this message again";
    private final CalendarForBot calendarService;
    private final KeyboardService keyboardService;

    public MessageHandler(CalendarForBot calendarService, DayOffRepository dayOffRepository, KeyboardService keyboardService) {
        this.calendarService = calendarService;
        this.keyboardService = keyboardService;
    }

    public SendMessage getMessage(Update update) {
        SendMessage message;
        String messageText = update.getMessage().getText();
        LocalDate currentDate = LocalDate.now();
        long chatId = update.getMessage().getChatId();
        switch (messageText) {
            case Commands.START:
                message = startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                break;
            case Commands.HELP:
                message = prepareSendMessage(chatId, HELP_TEXT, null);
                break;
            case Commands.ADMIN_CALENDAR:
                message = prepareSendMessage(chatId, "Оберіть вихідні", calendarService.getCalendarKeyboard(currentDate, ADD_DAY_OFF));
                break;
            case Commands.ADMIN_CALENDAR_RESERVE:
                message = prepareSendMessage(chatId, "Оберіть вихідні", calendarService.getCalendarKeyboard(currentDate, "show"));
                break;
            case Commands.AVAILABLE_DATE_TO_RESERVE:
                message = prepareSendMessage(chatId, "Оберіть дату", keyboardService.getScheduleDays());
                break;
            case Commands.SHOW_USER_RESERVATION:
                message = prepareSendMessage(chatId, "Ваші бронювання, натисніть на необхідну заявку щоб відмінити", keyboardService.getUserReservation(chatId));
                break;
            case Commands.CHAT_ID:
                message = prepareSendMessage(chatId, "Ваш Id= " + chatId, null);
                break;
            default:
                message = prepareSendMessage(chatId, "Sorry, command was not recognized", null);
        }
        return message;
    }

    private SendMessage startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Вітаю, " + name +
                ", рад знайомству! Будьласка поділись зі мною номером телефону" + " :blush:");
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
        keyboardButton.setText("Поділитись номером телефону >");
        keyboardButton.setRequestContact(true);
        keyboardFirstRow.add(keyboardButton);
        keyboard.add(keyboardFirstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        return message;
    }

    private SendMessage prepareSendMessage(long chatId, String textToSend, InlineKeyboardMarkup replyKeyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setReplyMarkup(replyKeyboardMarkup);
        return message;
    }
}