package com.ag.eventcalendar.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class CreateUserRequest {
	
    private String name;
    
    private LocalTime workingHoursStart;
    
    private LocalTime workingHoursEnd;
}
