package com.vbs.demo.repositories;

import com.vbs.demo.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface NotificationRepo extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserId(int userId);

    Notification findTopByUserIdOrderByIdDesc(int userid);
}

