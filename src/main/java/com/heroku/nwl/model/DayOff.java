package com.heroku.nwl.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class DayOff {
    @Id
    private LocalDate dayOffDate;

    public DayOff() {
    }

    public DayOff(LocalDate dayOffDate) {
        this.dayOffDate = dayOffDate;
    }
}
