package de.haevn.worksuite.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "lastName")
    private String lastName;

    @Column(name = "role")
    private String role;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "redmine_key")
    private String redmineKey;

    @Column(name = "vcs_key")
    private String vcsKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "license_expiration")
    private Instant licenseExpiration;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.licenseExpiration = Instant.now().plus(30, ChronoUnit.DAYS);
    }
}
