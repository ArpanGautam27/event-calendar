package com.ag.eventcalendar.service;

import com.ag.eventcalendar.dto.CreateUserRequest;
import com.ag.eventcalendar.entity.TimeSlot;
import com.ag.eventcalendar.entity.User;
import com.ag.eventcalendar.exception.NotFoundException;
import com.ag.eventcalendar.repository.TimeSlotRepository;
import com.ag.eventcalendar.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    public Long createUser(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setWorkingHoursStart(request.getWorkingHoursStart());
        user.setWorkingHoursEnd(request.getWorkingHoursEnd());
        user.setBookedSlots(new ArrayList<>());
        return userRepository.save(user).getId();
    }
    
    public void updateUser(Long id, User updatedUser) {
        User existing = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        existing.setName(updatedUser.getName());
        existing.setWorkingHoursStart(updatedUser.getWorkingHoursStart());
        existing.setWorkingHoursEnd(updatedUser.getWorkingHoursEnd());
        userRepository.save(existing);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


    public boolean isUserAvailable(Long userId, LocalDateTime start, LocalDateTime end) {
        List<TimeSlot> slots = timeSlotRepository.findByUserId(userId);
        for (TimeSlot slot : slots) {
            if (timesOverlap(slot.getStart(), slot.getEnd(), start, end)) {
                return false;
            }
        }
        return true;
    }

    public void blockUserTime(Long userId, LocalDateTime start, LocalDateTime end) {
        User user = userRepository.findById(userId).orElseThrow();
        TimeSlot slot = new TimeSlot();
        slot.setStart(start);
        slot.setEnd(end);
        slot.setUser(user);
        timeSlotRepository.save(slot);
    }
    
    public List<TimeSlot> getUserFreeSlots(Long userId, LocalDate date) {
        User user = userRepository.findById(userId).orElseThrow();
        LocalDateTime dayStart = date.atTime(user.getWorkingHoursStart());
        LocalDateTime dayEnd = date.atTime(user.getWorkingHoursEnd());

        List<TimeSlot> busySlots = timeSlotRepository.findByUserId(userId).stream()
            .filter(slot -> slot.getStart().toLocalDate().isEqual(date))
            .sorted(Comparator.comparing(TimeSlot::getStart))
            .toList();

        List<TimeSlot> freeSlots = new ArrayList<>();
        LocalDateTime current = dayStart;

        for (TimeSlot slot : busySlots) {
            if (current.isBefore(slot.getStart())) {
                freeSlots.add(new TimeSlot(null, current, slot.getStart(), user));
            }
            if (current.isBefore(slot.getEnd())) {
                current = slot.getEnd();
            }
        }

        if (current.isBefore(dayEnd)) {
            freeSlots.add(new TimeSlot(null, current, dayEnd, user));
        }

        return freeSlots;
    }


    private boolean timesOverlap(LocalDateTime aStart, LocalDateTime aEnd,
                                 LocalDateTime bStart, LocalDateTime bEnd) {
        return !(aEnd.isBefore(bStart) || aStart.isAfter(bEnd));
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}

