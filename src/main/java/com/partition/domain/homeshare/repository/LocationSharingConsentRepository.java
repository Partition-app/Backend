package com.partition.domain.homeshare.repository;

import com.partition.entity.Household;
import com.partition.entity.LocationSharingConsent;
import com.partition.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LocationSharingConsentRepository extends JpaRepository<LocationSharingConsent, Long> {

    Optional<LocationSharingConsent> findByUserAndHousehold(User user, Household household);

    @Query("""
        SELECT c FROM LocationSharingConsent c
        WHERE c.household = :household
          AND c.agreed = true
          AND c.user != :excludeUser
        """)
    List<LocationSharingConsent> findAgreedMembersExcluding(
            @Param("household") Household household,
            @Param("excludeUser") User excludeUser
    );
}
