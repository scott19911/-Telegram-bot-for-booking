package com.heroku.nwl.constants;

import java.time.LocalTime;

public class Constants {
    public static final String PRIVIES = "privies";
    public static final String NEXT = "next";
    public static final String NULL = "null";
    public static final String NEW_RESERVATION = "Нове бронювання на %s %s \nНа ім'я %s \nтелефон %s";
    public static final String USER_CANSEL_RESERVATION = "Відміненно бронювання на %s %s \nНа ім'я %s \nтелефон +%s";
    public static final String BACK = "BACK";
    public static final String CHOOSE_DATE = "Оберіть дату: ";
    public static final String CHOOSE_DAY_OFF = "Оберіть вихідні";
    public static final String INCORRECT_DATE = "Невірна дата, оберіть іншу";
    public static final String ERROR = "Unknown command";
    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final String DELETE_RESERVATION_MESSAGE = "You are delete reserve ";
    public static final String ERROR_DELETE_RESERVATION_MESSAGE = "Sorry you can't delete reserve ";
    public static final String ERROR_FILE = "Wrong file data format";
    public static final String FILE_SETTINGS = "settings.xlsx";
    public static final String SUCCESS_FILE_SETTINGS = "You are successful load setting file";
    public static final String CHOOSE_SERVICE = "Choose a service";
    public static final String SERVICE_BUTTON_TEXT = "%s price %.2f";
    public static final String USER_DATA = "We received next information:\nChatId %d \nFirst Name %s\nLast Name %s\nUser Name %s\nPhone number %s";
    public static final int AFTER = -1;
    public static final String CONTACT_MESSAGE_WITH_BRAKE = "Work time %s-%s\nBreak time %s-%s\nOur address %s str. %s building %s %s\nContact phone %s\nOur web site %s";
    public static final String CONTACT_MESSAGE_WITHOUT_BRAKE = "Work time %s-%s\nOur address %s str. %s building %s %s\nContact phone %s\nOur web site %s";
    public static final LocalTime DEFAULT_START_BRAKE = LocalTime.of(23, 59);
    public static final LocalTime DEFAULT_END_BRAKE = LocalTime.of(0, 0);
    public static final String HELP_TEXT = """
            This bot allows you to make an appointment.
             
            Available list of commands:
            /start - command to start communication with the bot
            /chat_id - get your identification number in this chat
            /reserve - allows you to choose the time and service for booking
            /my_reserve - View your active bookings
            /contact - contact information for communication with the administration
            
            Commands are available only to the administrator:
            /admin_calendar - lets you manage your weekend
            /admin_calendar_reserve - allows you to view reservations by date and cancel them
             """;
    public static final String MESSAGE_START_WORK = "To get started, you need to download and install the " +
            "file attached to this message. Please note that in file 2, both letters must be filled out, " +
            "then send this file to the bot.";
}
