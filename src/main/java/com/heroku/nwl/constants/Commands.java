package com.heroku.nwl.constants;

public class Commands {
    public static final String START = "/start";
    public static final String CHAT_ID = "/chat_id";
    public static final String ADMIN_CALENDAR = "/admin_calendar";
    public static final String ADMIN_CALENDAR_RESERVE = "/admin_calendar_reserve";
    public static final String AVAILABLE_DATE_TO_RESERVE = "/reserve";
    public static final String SHOW_USER_RESERVATION = "/my_reserve";
    public static final String HELP = "/help";
    public static final String JSON_COMMAND_CHANGE_MONTH = "{\"c\":\"%s\",\"cD\":\"%s\",\"to\":\"%s\"}";
    public static final String JSON_COMMAND_WITH_CURRENT_DATE = "{\"c\":\"%s\",\"cD\":\"%s\"}";
    public static final String JSON_COMMAND_SELECT_SERVICE = "{\"c\":\"%s\",\"cD\":\"%s\",\"pD\":\"%s\",\"nD\":\"%s\",\"sId\":\"%d\"}";
    public static final String JSON_COMMAND_NAVIGATE_DATA = "{\"c\":\"%s\",\"cD\":\"%s\",\"pD\":\"%s\",\"nD\":\"%s\"}";
    public static final String JSON_COMMAND_DATA_TIME = "{\"c\":\"%s\",\"cD\":\"%s\",\"oT\":\"%s\",\"sId\":\"%d\"}";
    public static final String JSON_COMMAND_RESERVATION = "{\"c\":\"%s\",\"id\":\"%d\",\"cD\":\"%s\",\"oT\":\"%s\"}";
    public static final String JSON_COMMAND_CANCEL_RESERVATION = "{\"c\":\"%s\",\"id\":\"%d\",\"cD\":\"%s\"}";
    public static final String JSON_COMMAND_RETURN_BACK = "{\"c\":\"GO\",\"to\":\"%s\",\"cD\":\"%s\"}";
    public static final String CHANGE_MONTH = "cM";
    public static final String WORKDAY = "wD";
    public static final String SELECT_SERVICE = "sS";
    public static final String ORDER_TIME = "rT";
    public static final String DELETE_RESERVE = "dR";
    public static final String CANCEL_RESERVE = "cR";
    public static final String ALL_RESERVATION = "aR";
    public static final String GO_BACK = "GO";
    public static final String CONTACTS = "/contact";
    public static final String SEND_MESSAGE = "/send";
    public static final String CHANGE_RESERVATION_STATUS = "cRS";
    public static final String GET_SERVICE_CATALOG = "/get_service";
    public static final String GET_SETTING_FILE = "/get_setting";
    public static final String JSON_CHANGE_RESERVATION_STATUS = "{\"c\":\"%s\",\"id\":\"%d\",\"cRS\":\"%s\"}";
}
