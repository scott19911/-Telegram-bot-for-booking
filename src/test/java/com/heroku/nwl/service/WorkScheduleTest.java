package com.heroku.nwl.service;

import com.heroku.nwl.model.DayOffRepository;
import com.heroku.nwl.model.OrderRepository;
import com.heroku.nwl.model.WorkTimeSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkScheduleTest {
    @Autowired
    private WorkTimeSettingsRepository workTimeSettingsRepository;
    @Autowired
    private DayOffRepository dayOffRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void test() {
        WorkSchedule workSchedule = new WorkSchedule(workTimeSettingsRepository, dayOffRepository, orderRepository);

        System.out.println(workSchedule.getWorkTime(LocalDate.now()));
    }

}