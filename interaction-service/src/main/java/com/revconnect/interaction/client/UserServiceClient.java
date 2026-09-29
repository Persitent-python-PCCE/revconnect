package com.revconnect.interaction.client;

import com.revconnect.interaction.client.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/user/{id}")
    UserProfileResponse getUser(@PathVariable("id") Long userId);
}