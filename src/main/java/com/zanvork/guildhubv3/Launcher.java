package com.zanvork.guildhubv3;

import com.zanvork.guildhubv3.configuration.TomcatConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 * @author zanvork
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan({"com.zanvork"})
@Import(TomcatConfiguration.class)
public class Launcher {
       
    public static void main(String args[]) {
        SpringApplication.run(Launcher.class);
    }
    
}
