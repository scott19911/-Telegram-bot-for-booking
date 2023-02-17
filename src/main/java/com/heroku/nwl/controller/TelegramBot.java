package com.heroku.nwl.controller;


import com.heroku.nwl.Role;
import com.heroku.nwl.config.BotConfig;
import com.heroku.nwl.model.User;
import com.heroku.nwl.model.UserRepository;
import com.heroku.nwl.service.CallbackQueryHandler;
import com.heroku.nwl.service.MessageHandler;
import com.heroku.nwl.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.heroku.nwl.service.KeyboardService.ORDER_TIME;

@Slf4j
@Controller
public class TelegramBot extends TelegramLongPollingBot {
    static final String ERROR_TEXT = "Error occurred: ";
    private final UserRepository userRepository;
    private final BotConfig config;
    private final CallbackQueryHandler callbackQueryHandler;
    private final MessageHandler messageHandler;
    private final NotificationService notificationService;
    @Value("${bot.admin.phone}")
    private String botAdminPhone;

    public TelegramBot(UserRepository userRepository, BotConfig config,
                       CallbackQueryHandler callbackQueryHandler, MessageHandler messageHandler, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.config = config;
        this.callbackQueryHandler = callbackQueryHandler;
        this.messageHandler = messageHandler;
        this.notificationService = notificationService;
        setBotMenu();
    }

    private void setBotMenu() {
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("start", "get a welcome message"));
        listofCommands.add(new BotCommand("chat_id", "get your ID"));
        listofCommands.add(new BotCommand("admin_calendar", "Календар з вихідним"));
        listofCommands.add(new BotCommand("admin_calendar_reserve", "Перегляд бронювань за датою"));
        listofCommands.add(new BotCommand("reserve", "Найближчі 10 робочих днів для бронювання"));
        listofCommands.add(new BotCommand("my_reserve", "Перегляд ваши активних бронювань"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().getContact() != null) {
            registerUser(update.getMessage());
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            executeMessage(messageHandler.getMessage(update));
        } else if (update.hasCallbackQuery()) {
            SendMessage notify = notifyUserAboutCancelingReservation(update);
            EditMessageText editMessageText = callbackQueryHandler.getEditMessage(update);
            executeEditMessageText(editMessageText);
            notifyAdmin(editMessageText, update);
            if (notify != null && editMessageText.getText().startsWith("You are cansel reserve ")) {
                executeMessage(notify);
            }
        }
    }

    public void registerUser(Message msg) {
        User user = userRepository.findById(msg.getChatId()).orElse(new User());
        var chatId = msg.getChatId();
        var chat = msg.getChat();

        user.setChatId(chatId);
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setUserName(chat.getUserName());
        user.setPhoneNumber(msg.getContact().getPhoneNumber());
        user.setRole(Role.USER);
        if (user.getPhoneNumber().equals(botAdminPhone)) {
            user.setRole(Role.ADMIN);
        }
        userRepository.save(user);
        log.info("user saved: " + user);
    }

    private void notifyAdmin(EditMessageText editMessageText, Update update) {
        if (editMessageText.getText().startsWith("You are reserve ")) {
            String callbackData = update.getCallbackQuery().getData();
            String[] orderData = callbackData.replace(ORDER_TIME, "").split(";");
            LocalDate orderDate = LocalDate.parse(orderData[0]);
            LocalTime orderTime = LocalTime.parse(orderData[1]);
            List<SendMessage> messages = notificationService.adminNotify
                    (update.getCallbackQuery().getMessage().getChatId(), orderDate, orderTime);
            for (SendMessage message : messages
            ) {
                executeMessage(message);
            }
        }
    }

    private SendMessage notifyUserAboutCancelingReservation(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        if (callbackData.contains("canselReservation")) {
            String orderIdString = callbackData.replace("canselReservation", "");
            String[] dataString = orderIdString.split(";");
            Long orderId = Long.parseLong(dataString[0]);
            return notificationService.notifyUser(orderId);
        }
        return null;
    }

    private void executeEditMessageText(EditMessageText message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }

    public void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_TEXT + e.getMessage());
        }
    }
}
