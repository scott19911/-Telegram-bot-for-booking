package com.heroku.nwl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.heroku.nwl.constants.Constants.NULL;
import static com.heroku.nwl.service.KeyboardService.FORMATTER;

@Data
public class ButtonDto {
    @JsonProperty("c")
    private String command;
    @JsonProperty("cD")
    private LocalDate currentDate;
    @JsonProperty("nD")
    private LocalDate nextDate;
    @JsonProperty("pD")
    private LocalDate previousDate;
    @JsonProperty("oT")
    private LocalTime reservedTime;
    @JsonProperty("id")
    private Long reservationId;
    @JsonProperty("to")
    private String returnTo;
    @JsonProperty("sId")
    private Long serviceId;

    public void setCurrentDate(String currentDate) {
        if (currentDate != null) this.currentDate = LocalDate.parse(currentDate,FORMATTER);
    }

    public void setNextDate(String nextDate) {
        if (!nextDate.equals(NULL)) this.nextDate = LocalDate.parse(nextDate,FORMATTER);
    }

    public void setPreviousDate(String previousDate) {
        if (!previousDate.equals(NULL)) this.previousDate = LocalDate.parse(previousDate,FORMATTER);
    }
    public void setReservedTime(String time) {
        if (!time.equals(NULL)) this.reservedTime = LocalTime.parse(time);
    }
}
