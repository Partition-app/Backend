package com.partition.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "households")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Household extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "household_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    // 초대 코드는 유니크해야 하며, 보통 6~10자리 정도로 생성합니다.
    @Column(name = "invite_code", nullable = false, unique = true, length = 10)
    private String inviteCode;

    @Builder
    public Household(String name, String inviteCode) {
        this.name = name;
        this.inviteCode = inviteCode;
    }

    // 초대 코드 재발급 기능 등이 필요할 때 사용
    public void updateInviteCode(String newCode) {
        this.inviteCode = newCode;
    }

    // 그룹 이름 변경 기능 등이 필요할 때 사용
    public void updateName(String name) {
        this.name = name;
    }
}