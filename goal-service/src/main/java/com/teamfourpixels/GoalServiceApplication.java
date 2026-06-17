package com.teamfourpixels;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GoalServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GoalServiceApplication.class, args);
    }
}