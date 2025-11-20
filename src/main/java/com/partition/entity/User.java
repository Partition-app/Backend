package com.partition.entity;

import com.partition.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    // OAuth 제공자
    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "household_id")
    private Long householdId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column(length = 20)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(name = "profile_image")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role")
    private UserRole memberRole;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "moved_out_at")
    private LocalDateTime movedOutAt;

    @Builder
    public User(String email, String password, String name, String provider, String providerId, UserRole memberRole) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
        this.memberRole = memberRole;
        this.isActive = true;
    }

    public void updateProfile(String name, String profileImage) {
        this.name = name;
        this.profileImage = profileImage;
    }
}