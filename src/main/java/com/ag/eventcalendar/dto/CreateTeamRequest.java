package com.ag.eventcalendar.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateTeamRequest {
	
    private String name;
    
    private List<String> userIds;
}

