package com.heroku.nwl.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

@Configuration
@EnableScheduling
@Data
public class BotConfig {
    @Value("${timezone}")
    String zone;
    @PostConstruct
    public void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(zone));
    }
    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String token;
}
