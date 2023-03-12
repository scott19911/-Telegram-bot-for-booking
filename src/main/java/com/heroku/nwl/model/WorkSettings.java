package com.heroku.nwl.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalTime;

@Entity()
@Data
public class WorkSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalTime breakFrom;
    private LocalTime breakTo;
    private String phoneNumber;
    private String city;
    private String street;
    private String building;
    private String apartment;
    private String link;
}
