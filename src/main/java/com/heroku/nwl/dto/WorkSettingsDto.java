package com.heroku.nwl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.heroku.nwl.model.WorkSettings;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import static com.heroku.nwl.constants.Constants.DEFAULT_END_BRAKE;
import static com.heroku.nwl.constants.Constants.DEFAULT_START_BRAKE;

@Data
@Slf4j
public class WorkSettingsDto {
    @JsonProperty(value = "open_time",required = true)
    private LocalTime openTime;
    @JsonProperty(value = "close_time",required = true)
    private LocalTime closeTime;
    @JsonProperty(value = "start_break")
    private LocalTime startBreak;
    @JsonProperty(value = "end_break")
    private LocalTime endBreak;
    @JsonProperty(value = "phone_number")
    private String phoneNumber;
    @JsonProperty(value = "city")
    private String city;
    @JsonProperty(value = "street")
    private String street;
    @JsonProperty(value = "building")
    private String building;
    @JsonProperty(value = "apartment")
    private String apartment;
    @JsonProperty(value = "link")
    private String link;

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
            this.startBreak = DEFAULT_START_BRAKE;
        }
    }

    public void setEndBreak(String endBreak) {
        try {
            this.endBreak = LocalTime.parse(endBreak);
        } catch (DateTimeParseException exception){
            log.error(exception.getMessage());
            this.endBreak = DEFAULT_END_BRAKE;
        }
    }
    public WorkSettings getWorkTimeSettings(){
        WorkSettings workSettings = new WorkSettings();
        workSettings.setCloseTime(closeTime);
        workSettings.setOpenTime(openTime);
        workSettings.setBreakFrom(startBreak);
        workSettings.setBreakTo(endBreak);
        workSettings.setApartment(apartment);
        workSettings.setBuilding(building);
        workSettings.setCity(city);
        workSettings.setStreet(street);
        workSettings.setPhoneNumber(phoneNumber);
        workSettings.setLink(link);
        return workSettings;
    }
}
