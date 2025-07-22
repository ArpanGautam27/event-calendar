package com.ag.eventcalendar.entity;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime startTime;
    
    private LocalDateTime endTime;

    @ManyToMany
    private List<User> userParticipants = new ArrayList<>();

    @ManyToMany
    private List<Team> teamParticipants = new ArrayList<>();

    private int requiredRepsPerTeam;
}

