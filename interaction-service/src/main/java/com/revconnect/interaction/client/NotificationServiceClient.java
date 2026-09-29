package com.revconnect.interaction.client;

import com.revconnect.interaction.client.dto.CreateNotificationRequest;
import com.revconnect.interaction.client.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {
    @PostMapping("/api/notifications")
    NotificationResponse createNotification(@RequestBody CreateNotificationRequest request);
}
