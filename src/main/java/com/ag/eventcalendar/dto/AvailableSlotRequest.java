package com.ag.eventcalendar.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AvailableSlotRequest {
	
    private Long userId;
    
    private Long teamId;
    
    private int requiredReps;
    
    private LocalDate date;
}

