package com.ag.eventcalendar.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateEventRequest {
	
    private String name;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private List<String> userIds;
    
    private List<String> teamIds;
    
    private int requiredRepsPerTeam;
}
