package com.ag.eventcalendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ag.eventcalendar.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}

