package com.heroku.nwl.model;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface DayOffRepository extends CrudRepository<DayOff, Long> {

    List<DayOff> findByDayOffDateBetween(LocalDate first, LocalDate end);
}
