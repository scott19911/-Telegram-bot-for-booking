package com.heroku.nwl.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heroku.nwl.config.BotConfig;
import com.heroku.nwl.config.CustomBotException;
import com.heroku.nwl.constants.Constants;
import com.heroku.nwl.constants.ErrorMessage;
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
import static com.heroku.nwl.constants.Constants.RESERVE;

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
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("start", "get a welcome message"));
        botCommands.add(new BotCommand("chat_id", "get your ID"));
        botCommands.add(new BotCommand("admin_calendar", "Calendar with a day off"));
        botCommands.add(new BotCommand("admin_calendar_reserve", "View bookings by date"));
        botCommands.add(new BotCommand("reserve","The next 10 working days for booking"));
        botCommands.add(new BotCommand("my_reserve", "View your active bookings"));
        botCommands.add(new BotCommand("contact", "Contact Information"));
        botCommands.add(new BotCommand("send", "Send message to all users"));
        try {
            this.execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
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
        long chatId = update.hasMessage() ?
                update.getMessage().getChatId() :
                update.getCallbackQuery().getMessage().getChatId();
        try {
            receiveContactHandler(update);
            receiveFileHandler(update);
            receiveMessageHandler(update);
            receiveCallbackHandler(update);
        }catch (CustomBotException ex){
            SendMessage errorMessage = SendMessage.builder().chatId(String.valueOf(chatId)).text(ex.getMessage()).build();
            executeMessage(errorMessage);
        }
    }
    public void receiveContactHandler(Update update){
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
    }
    public void receiveMessageHandler(Update update) throws CustomBotException {
        if (update.hasMessage() && update.getMessage().hasText()) {
           List<SendMessage> sendMessages = notificationService.sendMessageFromAdmin(update);
           if (sendMessages != null){
               for (SendMessage message:sendMessages
                    ) {
                   executeMessage(message);
               }
           } else {
               executeMessage(messageHandler.getMessage(update));
           }
        }
    }
    public void receiveFileHandler(Update update) throws CustomBotException {
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
    }
    public void receiveCallbackHandler(Update update) throws CustomBotException {
        if (update.hasCallbackQuery()) {
            SendMessage notify = notifyUserAboutCancelingReservation(update);
            EditMessageText editMessageText = callbackQueryHandler.getEditMessage(update);
            executeEditMessageText(editMessageText);
            notifyAdmin(editMessageText, update);
            if (notify != null && editMessageText.getText().startsWith(RESERVE)) {
                executeMessage(notify);
            }
        }
    }
    public SendMessage receiveFile(Update update, SendMessage message) throws CustomBotException {
        Document document = update.getMessage().getDocument();
        GetFile getFile = new GetFile();
        getFile.setFileId(document.getFileId());
        try {
            org.telegram.telegrambots.meta.api.objects.File telegramFile = execute(getFile);
            File file = downloadFile(telegramFile);
            message.setText(fileHandler.fileHandler(document.getFileName(),file.getPath()));
        } catch (TelegramApiException e) {
            log.error(e.getMessage(),e);
            throw new CustomBotException(ErrorMessage.ERROR_RECEIVE_FILE);
        }
        return message;
    }
    private void notifyAdmin(EditMessageText editMessageText, Update update) {
        String messageText = null;
        if (editMessageText.getText().startsWith(RESERVE)){
            messageText = Constants.NEW_RESERVATION;
        } if (editMessageText.getText().startsWith(Constants.DELETE_RESERVE)){
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

    private SendMessage notifyUserAboutCancelingReservation(Update update) throws CustomBotException {
        String callbackData = update.getCallbackQuery().getData();
        ButtonDto buttonDto;
        try {
            buttonDto = new ObjectMapper().readValue(callbackData, ButtonDto.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(),e);
            throw new CustomBotException(ErrorMessage.ERROR_DATA_FORMAT_INCORRECT);
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
            log.error(e.getMessage(), e);
        }
    }
    private void sendFile(Long chatId){
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(String.valueOf(chatId));
        InputFile inputFile = new InputFile(new File(Constants.SETTING_FILE_NAME));
        sendDocument.setDocument(inputFile);
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(),e);
        }
    }
}
