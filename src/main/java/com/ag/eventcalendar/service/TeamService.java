package com.ag.eventcalendar.service;

import com.ag.eventcalendar.dto.CreateTeamRequest;
import com.ag.eventcalendar.entity.Team;
import com.ag.eventcalendar.entity.User;
import com.ag.eventcalendar.exception.NotFoundException;
import com.ag.eventcalendar.repository.TeamRepository;
import com.ag.eventcalendar.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    public Long createTeam(CreateTeamRequest request) {
        List<User> members = new ArrayList<>();
        for (String idStr : request.getUserIds()) {
            Long userId = Long.parseLong(idStr);
            User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
            members.add(user);
        }

        Team team = new Team();
        team.setName(request.getName());
        team.setMembers(members);
        return teamRepository.save(team).getId();
    }

    public Team getTeam(Long id) {
        return teamRepository.findById(id).orElse(null);
    }

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }
    
    public void updateTeam(Long id, CreateTeamRequest request) {
        Team team = teamRepository.findById(id).orElseThrow(() -> new NotFoundException("Team not found"));

        List<User> members = new ArrayList<>();
        for (String userIdStr : request.getUserIds()) {
            Long userId = Long.parseLong(userIdStr);
            User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
            members.add(user);
        }

        team.setName(request.getName());
        team.setMembers(members);
        teamRepository.save(team);
    }

    public void deleteTeam(Long id) {
        teamRepository.deleteById(id);
    }

}
