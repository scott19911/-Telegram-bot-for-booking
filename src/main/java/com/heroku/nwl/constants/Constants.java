package com.heroku.nwl.constants;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Constants {
    public static final String PRIVIES = "privies";
    public static final String NEXT = "next";
    public static final String NULL = "null";
    public static final String NOTIFY_SESSION = "We remind you that you are scheduled for a session today at %і";
    public static final String NEW_RESERVATION = "New booking for %s %s In the name of %s phone %s";
    public static final String USER_CANSEL_RESERVATION = "Canceled reservation for %s %s\nIn the name of %s\nphone %s";
    public static final String BACK = "BACK";
    public static final String CHOOSE_DATE = "Choose a date:";
    public static final String CHOOSE_DAY_OFF = "Choose a weekend";
    public static final String INCORRECT_DATE = "Invalid date, choose another one";
    public static final String SHARE_PHONE = "Share phone number >";
    public static final String WELCOME_TEXT = "Hi, %s, nice to meet you! Please share your phone number with me :blush:";
    public static final String ERROR = "Unknown command";
    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final String DELETE_RESERVATION_MESSAGE = "You are delete reserve ";
    public static final String ERROR_DELETE_RESERVATION_MESSAGE = "Sorry you can't delete reserve ";
    public static final String ERROR_FILE = "Wrong file data format";
    public static final String FILE_SETTINGS = "settings.xlsx";
    public static final String SUCCESS_FILE_SETTINGS = "You are successful load setting file";
    public static final String CHOOSE_SERVICE = "Choose a service";
    public static final String SERVICE_CATALOG_FILE = "serviceCatalog.xlsx";
    public static final String SERVICE_BUTTON_TEXT = "%s price %.2f";
    public static final String USER_DATA = "We received next information:\nChatId %d \nFirst Name %s\nLast Name %s\nUser Name %s\nPhone number %s";
    public static final int AFTER = -1;
    public static final int MAX_ROWS = 12;
    public static final int MINUTES_CHECK_CAME_USER = 1;
    public static final int SERVICE_CATALOG_COLUMNS = 2;
    public static final int WORK_SETTINGS_COLUMNS = 9;
    public static final int SERVICE_CATALOG_SHEET_INDEX = 1;
    public static final int WORK_SETTINGS_SHEET_INDEX = 0;
    public static final String DOT = ".";
    public static final String LEFT_ARROW = "<";
    public static final String RIGHT_ARROW = ">";
    public static final String ACTIVE_BOOKING = "There are active bookings for this date. You need cancel all active bookings";
    public static final String YOUR_ID = "Your Id= " ;
    public static final String RESERVE_AT = "reserve at ";
    public static final String SEND_SETTING_FILE = "Template for setting up the program ";
    public static final String SEND_SERVICE_FILE = "List of available services ";
    public static final String DELETE_RESERVE = "You are delete reserve";
    public static final String USER_RESERVATION = "Your reservations, click on the required request to cancel";
    public static final String USER_NEW_RESERVATION_MESSAGE = "You are reserve %s %s";
    public static final String USER_NEW_RESERVATION = "You are reserve";
    public static final String TIME_ALREADY_TAKEN = "Sorry this time is already taken or you don't register";
    public static final String USER_CANCEL_RESERVATION_MESSAGE = "Your reservation for %s %s has been canceled by an administrator";
    public static final String DOT_ZERO = ".0";
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
            /get_service - List of available services
            
            Commands are available only to the administrator:
            /admin_calendar - lets you manage your weekend
            /admin_calendar_reserve - allows you to view reservations by date and cancel them
            /send {your message} - allows you send message to all users, replace {your message}  on yours text
            /get_setting - Template for setting up the program 
             """;
    public static final String MESSAGE_START_WORK = "To get started, you need to download and install the " +
            "file attached to this message. Please note that in file 2, both letters must be filled out, " +
            "then send this file to the bot.";
    public static final String SETTING_FILE_NAME = "settings.xlsx";
    public static final String MESSAGE_USER_CANSEL_RESERVE = "You are cansel reserve %s %s";
    public static final String EMPTY_DATA = " ";
    public static final String ADD_DAY_OFF = "add_day_off";
    public static final String DELETE_DAY_OFF = "delete_day_off";
    public static final String YES = "YES";
    public static final String NO = "NO";
    public static final String CHANGE_RESERVATION_STATUS = "You change reservation status to ";
    public static final String CAME_CLIENT = "Did the client %s %s %S come at the appointed time?";
    public static final String[] WEEK_DAYS = new String[]{"пн", "вт", "ср", "чт", "пт", "сб", "нд"};
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("ddMMyy");
    public static final String NAME = "name";
    public static final String PRICE = "price";
    public static final String AVERAGE_TIME = "average_time";
    public static final String A1 = "A1";
    public static final String A_ROW = "A";
    public static final String B_ROW = "B";
    public static final String B1 = "B1";
    public static final String C_ROW = "C";
    public static final String C1 = "C1";
}
