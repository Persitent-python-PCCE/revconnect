package com.revconnect.analytics.service;

import com.revconnect.analytics.dto.*;
import com.revconnect.analytics.entity.AnalyticsEvent;
import com.revconnect.analytics.repository.AnalyticsEventRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AnalyticsService {
 private final AnalyticsEventRepository repository;
 public AnalyticsService(AnalyticsEventRepository repository){this.repository=repository;}
 public void record(AnalyticsEventRequest r){ AnalyticsEvent e=new AnalyticsEvent(); e.setOwnerId(r.ownerId()); e.setOwnerType(r.ownerType().toUpperCase()); e.setMetricType(r.metricType().toUpperCase()); e.setPostId(r.postId()); e.setMetricValue(r.value()); repository.save(e); }
 public AnalyticsSummary summary(Long ownerId,String ownerType){ String type=ownerType.toUpperCase(); Map<String,Long> metrics=new LinkedHashMap<>(); repository.findByOwnerIdAndOwnerType(ownerId,type).stream().map(AnalyticsEvent::getMetricType).distinct().forEach(m->metrics.put(m,repository.sumMetric(ownerId,type,m))); return new AnalyticsSummary(ownerId,type,metrics); }
}
