package com.mouken.modules.notification.service;

import com.mouken.modules.notification.db.NotificationRepository;
import com.mouken.modules.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void markAsRead(List<Notification> notifications) {
        notifications.forEach(n -> n.setChecked(true));
        notificationRepository.saveAll(notifications);
    }
}
