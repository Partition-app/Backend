package com.partition.domain.homeshare.repository;

import com.partition.entity.HomeLocation;
import com.partition.entity.Household;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HomeLocationRepository extends JpaRepository<HomeLocation, Long> {

    Optional<HomeLocation> findByHousehold(Household household);
}
