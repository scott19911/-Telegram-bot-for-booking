package com.heroku.nwl.service;

import com.heroku.nwl.config.CustomBotException;
import com.heroku.nwl.dto.ButtonDto;
import com.heroku.nwl.dto.CalendarDayDto;
import com.heroku.nwl.model.Reservation;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.List;

public interface KeyboardService {
    InlineKeyboardMarkup getScheduleDays();
    InlineKeyboardMarkup changeReservationStatus(Reservation reservation);
    InlineKeyboardMarkup getAvailableTimeKeyboard(ButtonDto dto) throws CustomBotException;
    InlineKeyboardButton getInlineKeyboardButton(String text, String callbackData);
    InlineKeyboardMarkup getCalendar(List<List<CalendarDayDto>> calendar, LocalDate date);
    InlineKeyboardMarkup getReservationOnDate(LocalDate date);
    InlineKeyboardMarkup getUserReservation(Long chatId);
    List<InlineKeyboardButton> getDayOfWeekButton();
    List<InlineKeyboardButton> getBackButton(String command, LocalDate date);
    InlineKeyboardMarkup getServiceCatalog(ButtonDto buttonDto) throws CustomBotException;
}
