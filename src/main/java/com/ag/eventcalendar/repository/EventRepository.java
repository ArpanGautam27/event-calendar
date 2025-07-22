package com.ag.eventcalendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ag.eventcalendar.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}

