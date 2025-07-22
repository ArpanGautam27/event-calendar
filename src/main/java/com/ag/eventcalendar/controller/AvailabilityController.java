package com.ag.eventcalendar.controller;

import com.ag.eventcalendar.dto.AvailableSlotRequest;
import com.ag.eventcalendar.entity.TimeSlot;
import com.ag.eventcalendar.service.AvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/availability")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    // Slots in JSON Format
    @PostMapping("/slots")
    public List<TimeSlot> getAvailableSlots(@RequestBody AvailableSlotRequest request) {
        return availabilityService.getAvailableSlotsForUserAndTeam(request);
    }
    
    //Slots in Required Format(10 AM to 3 PM and 4 PM to 7 PM)
    @PostMapping("/slots/simple")
    public String getAvailableSlotsSimple(@RequestBody AvailableSlotRequest request) {
        List<TimeSlot> slots = availabilityService.getAvailableSlotsForUserAndTeam(request);
        
        if (slots.isEmpty()) {
            return "No available slots";
        }
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h a");
        
        return slots.stream()
            .map(slot -> String.format("%s to %s", 
                slot.getStart().format(timeFormatter), 
                slot.getEnd().format(timeFormatter)))
            .collect(Collectors.joining(" and "));
    }
}
