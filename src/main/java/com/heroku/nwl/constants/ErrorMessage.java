package com.heroku.nwl.constants;

public class ErrorMessage {
    public static final String ERROR_RECEIVE_FILE = "Unable to upload file, file size must be less than 50 MB";
    public static final String ERROR_DATA_FORMAT_INCORRECT = "Incorrect data format";
    public static final String ERROR_WRONG_FILE_NAME = "Invalid file name or type. Should be " + Constants.SETTING_FILE_NAME;
    public static final String ERROR_SERVICE_NOT_AVAILABLE = "The service is not available. Try again later.";
    public static final String ERROR_PERMISSION = "Sorry, you don't have access to this functionality.";
}