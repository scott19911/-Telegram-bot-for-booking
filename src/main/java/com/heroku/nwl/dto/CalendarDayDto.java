package com.heroku.nwl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarDayDto {
    private String text;
    private String jsonData;
}
