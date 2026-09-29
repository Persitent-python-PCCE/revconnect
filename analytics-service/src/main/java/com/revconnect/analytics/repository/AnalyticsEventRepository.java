package com.revconnect.analytics.repository;

import com.revconnect.analytics.entity.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent, Long> {

 List<AnalyticsEvent> findByOwnerIdAndOwnerType(Long ownerId, String ownerType);

 @Query("SELECT COALESCE(SUM(e.metricValue), 0) " +
         "FROM AnalyticsEvent e " +
         "WHERE e.ownerId = :ownerId " +
         "AND e.ownerType = :ownerType " +
         "AND e.metricType = :metricType")
 Long sumMetric(
         @Param("ownerId") Long ownerId,
         @Param("ownerType") String ownerType,
         @Param("metricType") String metricType
 );
}