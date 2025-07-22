package com.ag.eventcalendar.controller;

import com.ag.eventcalendar.dto.CreateTeamRequest;
import com.ag.eventcalendar.entity.Team;
import com.ag.eventcalendar.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @PostMapping
    public Long createTeam(@RequestBody CreateTeamRequest request) {
        return teamService.createTeam(request);
    }

    @GetMapping
    public List<Team> getAllTeams() {
        return teamService.getAllTeams();
    }
    
    @PutMapping("/{id}")
    public void updateTeam(@PathVariable Long id, @RequestBody CreateTeamRequest request) {
        teamService.updateTeam(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
    }

}
