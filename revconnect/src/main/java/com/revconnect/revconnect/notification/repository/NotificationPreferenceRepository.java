package com.revconnect.revconnect.notification.repository;

import com.revconnect.revconnect.notification.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
}