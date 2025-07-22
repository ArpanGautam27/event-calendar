package com.ag.eventcalendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class EventcalendarApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventcalendarApplication.class, args);
	}

}
