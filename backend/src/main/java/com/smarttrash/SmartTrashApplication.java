package com.smarttrash;

import com.smarttrash.config.SupabaseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SupabaseProperties.class)
public class SmartTrashApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartTrashApplication.class, args);
    }
}
