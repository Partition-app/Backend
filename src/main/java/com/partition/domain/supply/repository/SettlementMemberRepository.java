package com.partition.domain.supply.repository;

import com.partition.entity.SettlementMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementMemberRepository extends JpaRepository<SettlementMember, Long> {

    List<SettlementMember> findAllBySettlementId(Long settlementId);
}
