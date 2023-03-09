package com.heroku.nwl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.heroku.nwl.model.WorkTimeSettings;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Data
@Slf4j
public class WorkTimeDto {
    @JsonProperty(value = "open_time",required = true)
    private LocalTime openTime;
    @JsonProperty(value = "close_time",required = true)
    private LocalTime closeTime;
    @JsonProperty(value = "start_break")
    private LocalTime startBreak;
    @JsonProperty(value = "end_break")
    private LocalTime endBreak;

    public void setOpenTime(String openTime) {
        try {
            this.openTime = LocalTime.parse(openTime);
        } catch (DateTimeParseException exception){
            log.error(exception.getMessage());
            throw new RuntimeException();
        }
    }

    public void setCloseTime(String closeTime) {
        try {
            this.closeTime = LocalTime.parse(closeTime);
        } catch (DateTimeParseException exception){
            log.error(exception.getMessage());
            throw new RuntimeException();
        }
    }

    public void setStartBreak(String startBreak) {
        try {
            this.startBreak = LocalTime.parse(startBreak);
        } catch (DateTimeParseException exception){
            log.error(exception.getMessage());
            throw new RuntimeException();
        }
    }

    public void setEndBreak(String endBreak) {
        try {
            this.endBreak = LocalTime.parse(endBreak);
        } catch (DateTimeParseException exception){
            log.error(exception.getMessage());
            throw new RuntimeException();
        }
    }
    public WorkTimeSettings getWorkTimeSettings(){
        WorkTimeSettings workTimeSettings = new WorkTimeSettings();
        workTimeSettings.setCloseTime(closeTime);
        workTimeSettings.setOpenTime(openTime);
        workTimeSettings.setBreakFrom(startBreak);
        workTimeSettings.setBreakTo(endBreak);
        return workTimeSettings;
    }
}
