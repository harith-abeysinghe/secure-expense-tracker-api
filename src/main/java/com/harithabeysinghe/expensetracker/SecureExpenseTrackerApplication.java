package com.harithabeysinghe.expensetracker;

import com.harithabeysinghe.expensetracker.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class SecureExpenseTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecureExpenseTrackerApplication.class, args);
    }
}

