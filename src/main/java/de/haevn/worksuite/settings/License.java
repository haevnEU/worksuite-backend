package de.haevn.worksuite.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a user's subscription license and validity period.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * License license = License.builder()
 *     .userId(UUID.randomUUID())
 *     .licenseKey("HAUSHELD-PRO-2026-XYZ")
 *     .plan("ENTERPRISE")
 *     .expiresAt(Instant.now().plus(365, ChronoUnit.DAYS))
 *     .build();
 * }</pre>
 */
@Entity
@Table(name = "licenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class License {

    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "license_key", nullable = false)
    private String licenseKey;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "plan", nullable = false, length = 50)
    private String plan;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Checks whether the license is missing or has exceeded its expiration date.
     *
     * @return {@code true} if the license has expired, {@code false} if still active
     */
    public boolean isExpired() {
        return this.expiresAt == null || Instant.now().isAfter(this.expiresAt);
    }

    /**
     * Lifecycle callback invoked before persisting a new license.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    /**
     * Lifecycle callback invoked before updating an existing license.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}