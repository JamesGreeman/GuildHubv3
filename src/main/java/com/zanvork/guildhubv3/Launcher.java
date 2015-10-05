package com.zanvork.guildhubv3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 * @author zanvork
 */
@SpringBootApplication
@EnableScheduling
public class Launcher {
       
    public static void main(String args[]) {
        SpringApplication.run(Launcher.class);
    }
}
