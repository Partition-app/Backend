package com.partition.domain.homeshare.repository;

import com.partition.entity.Household;
import com.partition.entity.NearHomeEvent;
import com.partition.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NearHomeEventRepository extends JpaRepository<NearHomeEvent, Long> {

    @Query("""
        SELECT e FROM NearHomeEvent e
        WHERE e.user = :user
          AND e.household = :household
          AND e.createdAt >= :cutoff
        ORDER BY e.createdAt DESC
        """)
    List<NearHomeEvent> findRecentEvents(
            @Param("user") User user,
            @Param("household") Household household,
            @Param("cutoff") LocalDateTime cutoff
    );
}
