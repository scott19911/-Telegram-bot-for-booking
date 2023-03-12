package com.heroku.nwl.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heroku.nwl.config.BotConfig;
import com.heroku.nwl.constants.Constants;
import com.heroku.nwl.dto.ButtonDto;
import com.heroku.nwl.model.Role;
import com.heroku.nwl.model.User;
import com.heroku.nwl.service.CallbackQueryHandler;
import com.heroku.nwl.service.FileHandler;
import com.heroku.nwl.service.MessageHandler;
import com.heroku.nwl.service.NotificationService;
import com.heroku.nwl.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.heroku.nwl.constants.Commands.CANCEL_RESERVE;
import static com.heroku.nwl.constants.Constants.HELP_TEXT;
import static com.heroku.nwl.constants.Constants.MESSAGE_START_WORK;

@Slf4j
@Controller
public class TelegramBot extends TelegramLongPollingBot {
    static final String ERROR_TEXT = "Error occurred: ";
    private final BotConfig config;
    private final CallbackQueryHandler callbackQueryHandler;
    private final MessageHandler messageHandler;
    private final NotificationService notificationService;
    private final FileHandler fileHandler;
    private final UserService userService;

    public TelegramBot(BotConfig config,
                       CallbackQueryHandler callbackQueryHandler, MessageHandler messageHandler, NotificationService notificationService, FileHandler fileHandler, UserService userService) {
        this.config = config;
        this.callbackQueryHandler = callbackQueryHandler;
        this.messageHandler = messageHandler;
        this.notificationService = notificationService;
        this.fileHandler = fileHandler;
        this.userService = userService;
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
        listofCommands.add(new BotCommand("contact", "Контактна інформація"));
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
            User user = userService.registerUser(update.getMessage());
            long chatId = update.getMessage().getChatId();
            String text = String.format(
                    Constants.USER_DATA,
                    chatId,
                    user.getFirstName(),
                    user.getLastName(),
                    user.getUserName(),
                    user.getPhoneNumber());
            SendMessage message = SendMessage
                    .builder()
                    .chatId(String.valueOf(update.getMessage().getChatId()))
                    .text(text)
                    .build();
            executeMessage(message);
            executeMessage(messageHandler.prepareSendMessage(chatId,HELP_TEXT,null));
            if (user.getRole().equals(Role.ADMIN)) {
                executeMessage(messageHandler.prepareSendMessage(chatId,MESSAGE_START_WORK,null));
                sendFile(chatId);
            }
        }
        if(update.hasMessage() && update.getMessage().getDocument() != null){
            SendMessage message = new SendMessage();
            long chatId = update.getMessage().getChatId();
            message.setChatId(String.valueOf(chatId));
            if(Role.ADMIN.equals(userService.getUserRole(chatId))){
                message = receiveFile(update,message);
            } else{
                message.setText(ERROR_TEXT);
            }
            executeMessage(message);
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            executeMessage(messageHandler.getMessage(update));
        } else if (update.hasCallbackQuery()) {
            System.out.println(update.getCallbackQuery().getData());
            SendMessage notify = notifyUserAboutCancelingReservation(update);
            EditMessageText editMessageText = callbackQueryHandler.getEditMessage(update);
            executeEditMessageText(editMessageText);
            notifyAdmin(editMessageText, update);
            if (notify != null && editMessageText.getText().startsWith("You are cansel reserve ")) {
                executeMessage(notify);
            }
        }
    }
    public SendMessage receiveFile(Update update, SendMessage message){
        Document document = update.getMessage().getDocument();
        GetFile getFile = new GetFile();
        getFile.setFileId(document.getFileId());
        try {
            org.telegram.telegrambots.meta.api.objects.File telegramFile = execute(getFile);
            File file = downloadFile(telegramFile);
            message.setText(fileHandler.fileHandler(document.getFileName(),file.getPath()));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return message;
    }
    private void notifyAdmin(EditMessageText editMessageText, Update update) {
        String messageText = null;
        if (editMessageText.getText().startsWith("You are reserve ")){
            messageText = Constants.NEW_RESERVATION;
        } if (editMessageText.getText().startsWith("You are delete reserve")){
            messageText = Constants.USER_CANSEL_RESERVATION;
        }
        if (messageText != null) {
            String callbackData = update.getCallbackQuery().getData();
            ButtonDto buttonDto;
            try {
                buttonDto = new ObjectMapper().readValue(callbackData, ButtonDto.class);
            } catch (JsonProcessingException e) {
                buttonDto = null;
            }
            assert buttonDto != null;
            LocalDate orderDate = buttonDto.getCurrentDate();
            LocalTime orderTime = buttonDto.getReservedTime();
            List<SendMessage> messages = notificationService.adminNotify(
                    update.getCallbackQuery().getMessage().getChatId(),
                    orderDate,
                    orderTime,
                    messageText);
            for (SendMessage message : messages
            ) {
                executeMessage(message);
            }
        }
    }

    private SendMessage notifyUserAboutCancelingReservation(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        ButtonDto buttonDto;
        try {
            buttonDto = new ObjectMapper().readValue(callbackData, ButtonDto.class);
        } catch (JsonProcessingException e) {
            buttonDto = null;
        }
        if (buttonDto != null && buttonDto.getCommand().equals(CANCEL_RESERVE)) {
            return notificationService.notifyUser(buttonDto.getReservationId());
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
    private void sendFile(Long chatId){
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(String.valueOf(chatId));
        InputFile inputFile = new InputFile(new File("settings.xlsx"));
        sendDocument.setDocument(inputFile);
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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
