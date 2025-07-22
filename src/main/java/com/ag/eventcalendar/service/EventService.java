package com.ag.eventcalendar.service;

import com.ag.eventcalendar.dto.*;
import com.ag.eventcalendar.entity.*;
import com.ag.eventcalendar.exception.*;
import com.ag.eventcalendar.repository.EventRepository;
import com.ag.eventcalendar.repository.TeamRepository;
import com.ag.eventcalendar.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserService userService;

    public Long createEvent(CreateEventRequest req) {
        List<User> eventUsers = new ArrayList<>();
        for (String userIdStr : req.getUserIds()) {
            Long userId = Long.parseLong(userIdStr);
            User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));

            if (!userService.isUserAvailable(userId, req.getStartTime(), req.getEndTime())) {
                throw new ConflictException("User busy: " + user.getName());
            }
            if (!isWithinWorkingHours(user, req.getStartTime(), req.getEndTime())) {
                throw new ConflictException("User not in working hours: " + user.getName());
            }
            eventUsers.add(user);
        }

        List<Team> eventTeams = new ArrayList<>();
        List<User> teamParticipants = new ArrayList<>();

        for (String teamIdStr : req.getTeamIds()) {
            Long teamId = Long.parseLong(teamIdStr);
            Team team = teamRepository.findById(teamId).orElseThrow(() -> new NotFoundException("Team not found: " + teamId));

            List<User> availableMembers = new ArrayList<>();
            for (User member : team.getMembers()) {
                if (userService.isUserAvailable(member.getId(), req.getStartTime(), req.getEndTime()) &&
                    isWithinWorkingHours(member, req.getStartTime(), req.getEndTime())) {
                    availableMembers.add(member);
                }
                if (availableMembers.size() == req.getRequiredRepsPerTeam()) {
                    break;
                }
            }

            if (availableMembers.size() < req.getRequiredRepsPerTeam()) {
                throw new ConflictException("Not enough team members available for team: " + team.getName());
            }

            teamParticipants.addAll(availableMembers);
            eventTeams.add(team);
        }

        for (User user : eventUsers) {
            userService.blockUserTime(user.getId(), req.getStartTime(), req.getEndTime());
        }
        for (User user : teamParticipants) {
            userService.blockUserTime(user.getId(), req.getStartTime(), req.getEndTime());
        }

        Event event = new Event();
        event.setName(req.getName());
        event.setStartTime(req.getStartTime());
        event.setEndTime(req.getEndTime());
        event.setUserParticipants(eventUsers);
        event.setTeamParticipants(eventTeams);
        event.setRequiredRepsPerTeam(req.getRequiredRepsPerTeam());

        return eventRepository.save(event).getId();
    }
    
    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
    }


    public List<Event> getEventsForUser(Long userId, LocalDateTime from, LocalDateTime to) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        List<Event> allEvents = eventRepository.findAll();
        List<Event> result = new ArrayList<>();
        for (Event event : allEvents) {
            if (overlaps(event.getStartTime(), event.getEndTime(), from, to) && isUserInvolved(event, user)) {
                result.add(event);
            }
        }
        return result;
    }

    private boolean isWithinWorkingHours(User user, LocalDateTime start, LocalDateTime end) {
        return !start.toLocalTime().isBefore(user.getWorkingHoursStart()) &&
               !end.toLocalTime().isAfter(user.getWorkingHoursEnd());
    }

    private boolean overlaps(LocalDateTime aStart, LocalDateTime aEnd,
                             LocalDateTime bStart, LocalDateTime bEnd) {
        return !(aEnd.isBefore(bStart) || aStart.isAfter(bEnd));
    }

    private boolean isUserInvolved(Event event, User user) {
        if (event.getUserParticipants().contains(user)) return true;
        for (Team team : event.getTeamParticipants()) {
            if (team.getMembers().contains(user)) return true;
        }
        return false;
    }
}
