package com.ag.eventcalendar.service;

import com.ag.eventcalendar.dto.AvailableSlotRequest;
import com.ag.eventcalendar.entity.Team;
import com.ag.eventcalendar.entity.TimeSlot;
import com.ag.eventcalendar.entity.User;
import com.ag.eventcalendar.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AvailabilityService {

    @Autowired
    private UserService userService;

    @Autowired
    private TeamRepository teamRepository;

    public List<TimeSlot> getAvailableSlotsForUserAndTeam(AvailableSlotRequest req) {
        LocalDate date = req.getDate();
        User user = userService.getUser(req.getUserId());
        Team team = teamRepository.findById(req.getTeamId()).orElseThrow();

        // Step 1: User free slots for the given day
        List<TimeSlot> userFree = userService.getUserFreeSlots(user.getId(), date);

        // Step 2: Get free slots of all team members (excluding the user)
        List<TimeSlot> allTeamFreeSlots = new ArrayList<>();

        for (User member : team.getMembers()) {
            if (member.getId().equals(user.getId())) continue;

            List<TimeSlot> freeSlots = userService.getUserFreeSlots(member.getId(), date);
            allTeamFreeSlots.addAll(freeSlots);
        }

        // Step 3: Create a timeline of unique time points from team members' slots
        List<LocalDateTime> timePoints = new ArrayList<>();
        for (TimeSlot slot : allTeamFreeSlots) {
            timePoints.add(slot.getStart());
            timePoints.add(slot.getEnd());
        }

        // Use the requesting user's working hours as day boundaries
        LocalDateTime dayStart = date.atTime(user.getWorkingHoursStart());
        LocalDateTime dayEnd = date.atTime(user.getWorkingHoursEnd());

        timePoints.add(dayStart);
        timePoints.add(dayEnd);
        timePoints = timePoints.stream().distinct().sorted().toList();

        // Step 4: Use sliding window to check count of available members in each interval
        List<TimeSlot> teamAvailable = new ArrayList<>();

        for (int i = 0; i < timePoints.size() - 1; i++) {
            LocalDateTime windowStart = timePoints.get(i);
            LocalDateTime windowEnd = timePoints.get(i + 1);

            // Skip intervals outside user's working hours
            if (windowEnd.isBefore(dayStart) || windowStart.isAfter(dayEnd)) {
                continue;
            }

            // Adjust window to user's working hours
            LocalDateTime adjustedStart = windowStart.isBefore(dayStart) ? dayStart : windowStart;
            LocalDateTime adjustedEnd = windowEnd.isAfter(dayEnd) ? dayEnd : windowEnd;

            int count = 0;
            for (TimeSlot slot : allTeamFreeSlots) {
                // Check if team member slot overlaps with the adjusted window
                if (!slot.getEnd().isBefore(adjustedStart) && !slot.getStart().isAfter(adjustedEnd)) {
                    count++;
                }
            }

            if (count >= req.getRequiredReps()) {
                teamAvailable.add(new TimeSlot(null, adjustedStart, adjustedEnd, null));
            }
        }

        // Step 5: Intersect user free slots with teamAvailable slots
        return intersectSlots(userFree, teamAvailable);
    }

    // Utility method to intersect two time slot lists
    private List<TimeSlot> intersectSlots(List<TimeSlot> userSlots, List<TimeSlot> teamSlots) {
        List<TimeSlot> result = new ArrayList<>();

        for (TimeSlot u : userSlots) {
            for (TimeSlot t : teamSlots) {
                LocalDateTime maxStart = u.getStart().isAfter(t.getStart()) ? u.getStart() : t.getStart();
                LocalDateTime minEnd = u.getEnd().isBefore(t.getEnd()) ? u.getEnd() : t.getEnd();

                if (maxStart.isBefore(minEnd)) {
                    result.add(new TimeSlot(null, maxStart, minEnd, null));
                }
            }
        }

        return mergeContinuousSlots(result);
    }

    // Optional: Merge consecutive/continuous time slots for cleaner output
    private List<TimeSlot> mergeContinuousSlots(List<TimeSlot> slots) {
        if (slots.isEmpty()) return slots;

        List<TimeSlot> merged = new ArrayList<>();
        slots.sort(Comparator.comparing(TimeSlot::getStart));

        TimeSlot current = slots.get(0);

        for (int i = 1; i < slots.size(); i++) {
            TimeSlot next = slots.get(i);

            if (!current.getEnd().isBefore(next.getStart())) {
                current = new TimeSlot(null, current.getStart(),
                        current.getEnd().isAfter(next.getEnd()) ? current.getEnd() : next.getEnd(), null);
            } else {
                merged.add(current);
                current = next;
            }
        }

        merged.add(current);
        return merged;
    }
}