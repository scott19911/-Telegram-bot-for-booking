package com.heroku.nwl.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalTime;

@Entity()
@Data
public class WorkTimeSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private LocalTime openTime = LocalTime.of(9, 0);
    private LocalTime closeTime = LocalTime.of(18, 0);
    @Column(columnDefinition = "TIME default '23:59:00'")
    private LocalTime breakFrom = LocalTime.of(23, 59);
    @Column(columnDefinition = "TIME default '00:00:00'")
    private LocalTime breakTo = LocalTime.of(0, 0);
}
