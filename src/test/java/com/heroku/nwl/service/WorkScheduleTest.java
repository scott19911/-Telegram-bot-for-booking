package com.heroku.nwl.service;


import com.heroku.nwl.model.DayOffRepository;
import com.heroku.nwl.model.Reservation;
import com.heroku.nwl.model.ReservationRepository;
import com.heroku.nwl.model.ReservationStatus;
import com.heroku.nwl.model.UserRepository;
import com.heroku.nwl.model.WorkSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.heroku.nwl.constants.Constants.MINUTES_CHECK_CAME_USER;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkScheduleTest {
    @Autowired
    private WorkSettingsRepository workSettingsRepository;
    @Autowired
    private DayOffRepository dayOffRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;



    @Test
    public void test() {
        //UserService userService = new UserService(userRepository);
        //System.out.println(userService.getUserRole(877213051L));
        List<Reservation> reservation = reservationRepository
                .findByOrderDate(
                        LocalDate.now());
        System.out.println(reservation);
    }

}