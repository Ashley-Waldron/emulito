package com.emulito.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ComponentScan({
        "com.emulito.common",
        "com.emulito.app"
})
public class EmulitoEmulator {

    public static void main(String[] args) {
        SpringApplication.run(EmulitoEmulator.class, args);
    }
}
