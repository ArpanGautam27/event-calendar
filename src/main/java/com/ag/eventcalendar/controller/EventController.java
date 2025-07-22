package com.ag.eventcalendar.controller;

import com.ag.eventcalendar.dto.CreateEventRequest;
import com.ag.eventcalendar.entity.Event;
import com.ag.eventcalendar.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping
    public Long createEvent(@RequestBody CreateEventRequest request) {
        return eventService.createEvent(request);
    }

    @GetMapping("/user/{userId}")
    public List<Event> getEventsForUser(
            @PathVariable Long userId,
            @RequestParam("from") String from,
            @RequestParam("to") String to) {

        return eventService.getEventsForUser(
                userId,
                LocalDateTime.parse(from),
                LocalDateTime.parse(to)
        );
    }
    
    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }

}