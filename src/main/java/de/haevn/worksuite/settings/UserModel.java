package de.haevn.worksuite.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing a user account and integration settings in the relational database.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * UserModel user = UserModel.builder()
 *     .firstName("Nils")
 *     .lastName("Milewski")
 *     .role("ADMIN")
 *     .redmineKey("redmine-secret-key")
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserModel {

    private static final int DEFAULT_TRIAL_DAYS = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
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

    /**
     * Initializes creation timestamp and default trial expiration period before database persistence.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.licenseExpiration == null) {
            this.licenseExpiration = Instant.now().plus(DEFAULT_TRIAL_DAYS, ChronoUnit.DAYS);
        }
    }
}