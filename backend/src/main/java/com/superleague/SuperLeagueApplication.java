package com.superleague;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SuperLeagueApplication {
    public static void main(String[] args) {
        SpringApplication.run(SuperLeagueApplication.class, args);
    }
}
