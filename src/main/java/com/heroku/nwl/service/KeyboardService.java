package com.heroku.nwl.service;

import com.heroku.nwl.model.Orders;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.heroku.nwl.service.CalendarForBot.EMPTY_DATA;
import static com.heroku.nwl.service.CalendarForBot.WEEK_DAYS;

@Service
public class KeyboardService {
    public static final String WORKDAY = "workday";
    public static final String ORDER_TIME = "order_time";
    private final WorkSchedule workSchedule;
    private final ReservationService reservationService;

    public KeyboardService(WorkSchedule workSchedule, ReservationService reservationService) {
        this.workSchedule = workSchedule;

        this.reservationService = reservationService;
    }

    public InlineKeyboardMarkup getScheduleDays() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<LocalDate> workDays = workSchedule.getWorkingDays();
        List<InlineKeyboardButton> firstLine = new ArrayList<>();
        List<InlineKeyboardButton> secondLine = new ArrayList<>();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        for (int i = 0; i < workDays.size(); i++) {
            LocalDate date = workDays.get(i);
            String month = date.getMonth().getValue() < 10 ?
                    ".0" + date.getMonth().getValue() : "." + date.getMonth().getValue();
            String buttonText = WEEK_DAYS[date.getDayOfWeek().getValue() - 1]
                    + " " + date.getDayOfMonth() + month;
            String navigateDate = getNextDate(workDays, i);
            if (i <= (workDays.size() / 2) - 1) {
                firstLine.add(getInlineKeyboardButton(buttonText, WORKDAY + date + navigateDate));
            } else {
                secondLine.add(getInlineKeyboardButton(buttonText, WORKDAY + date + navigateDate));
            }
        }
        rowList.add(firstLine);
        rowList.add(secondLine);
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getAvailableTimeKeyboard(String dateString) {
        String[] dates = dateString.split(";");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<LocalTime> workTime = workSchedule.getWorkTime(LocalDate.parse(dates[0]));
        List<InlineKeyboardButton> navigateButtons = new ArrayList<>();
        List<LocalDate> workDays = workSchedule.getWorkingDays();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        if (!dates[1].equals("null")) {
            int index = workDays.indexOf(LocalDate.parse(dates[1]));
            String navigateDate = getNextDate(workDays, index);
            navigateButtons.add(getInlineKeyboardButton("<", WORKDAY + dates[1] + navigateDate));
        }
        if (!dates[2].equals("null")) {
            int index = workDays.indexOf(LocalDate.parse(dates[2]));
            String navigateDate = getNextDate(workDays, index);
            navigateButtons.add(getInlineKeyboardButton(">", WORKDAY + dates[2] + navigateDate));
        }
        rowList.add(navigateButtons);
        for (LocalTime time : workTime
        ) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            String buttonText = "reserve at " + time;
            buttons.add(getInlineKeyboardButton(buttonText, ORDER_TIME + dates[0] + ";" + time));
            rowList.add(buttons);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    private String getNextDate(List<LocalDate> workDays, int index) {
        LocalDate priviesDate = null;
        LocalDate nextDate = null;
        if (index > 0) {
            priviesDate = workDays.get(index - 1);
        }
        if (index < workDays.size() - 1) {
            nextDate = workDays.get(index + 1);
        }
        return ";" + priviesDate + ";" + nextDate;
    }

    public InlineKeyboardButton getInlineKeyboardButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    public InlineKeyboardMarkup getReservationOnDate(LocalDate date) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<Orders> ordersList = reservationService.getReservationsByDate(date);
        for (Orders order : ordersList
        ) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            buttons.add(getInlineKeyboardButton(order.getOrderTime() + " " + order.getOrderDate() + " "
                            + order.getUser().getFirstName() + " " + order.getUser().getPhoneNumber(),
                    "canselReservation" + order.getOrderId() + ";" + date));
            rowList.add(buttons);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getUserReservation(Long chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        List<Orders> ordersList = reservationService.getUserReservations(chatId, LocalDate.now(), LocalTime.now());
        for (Orders order : ordersList
        ) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            buttons.add(getInlineKeyboardButton(order.getOrderTime() + " " + order.getOrderDate(),
                    "deleteReservation" + order.getOrderId()));
            rowList.add(buttons);
        }
        inlineKeyboardMarkup.setKeyboard(rowList);
        return inlineKeyboardMarkup;
    }

    public List<InlineKeyboardButton> getDayButton() {
        List<InlineKeyboardButton> dayOfWeek = new ArrayList<>();
        for (int i = 0; i < WEEK_DAYS.length; i++) {
            dayOfWeek.add(getInlineKeyboardButton(WEEK_DAYS[i], EMPTY_DATA));
        }
        return dayOfWeek;
    }
}
