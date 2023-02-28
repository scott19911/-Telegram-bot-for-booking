package com.heroku.nwl.service;

import com.heroku.nwl.constants.Constants;
import com.heroku.nwl.model.DayOffRepository;
import com.heroku.nwl.model.OrderRepository;
import com.heroku.nwl.model.WorkTimeSettingsRepository;
import com.vdurmont.emoji.EmojiParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalTime;

//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkScheduleTest {
//    @Autowired
//    private WorkTimeSettingsRepository workTimeSettingsRepository;
//    @Autowired
//    private DayOffRepository dayOffRepository;
//    @Autowired
//    private OrderRepository orderRepository;

    @Test
    public void test() {
        LocalTime localDate1 = LocalTime.now();
        LocalTime localDate2 = localDate1.plusHours(1);
        if (localDate2.compareTo(localDate1) == Constants.AFTER) {
            System.out.println(true);
        }
        System.out.println("localDate1 = " + localDate1);
        System.out.println("localDate2 = " + localDate2);
        System.out.println(localDate1.compareTo(localDate2));

    }

}