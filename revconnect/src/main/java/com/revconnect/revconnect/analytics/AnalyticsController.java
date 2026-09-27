package com.revconnect.revconnect.analytics;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService s;

    public AnalyticsController(AnalyticsService s) {
        this.s = s;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CREATOR','BUSINESS')")
    public ResponseEntity<Map<String, Object>> me(Authentication a) {
        return ResponseEntity.ok(s.user((Long) a.getPrincipal()));
    }
}