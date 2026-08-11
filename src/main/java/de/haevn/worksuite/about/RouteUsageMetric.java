package de.haevn.worksuite.about;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing invocation and usage metrics for an individual REST API route.
 *
 * <p>Tracks total request counts and access timestamps per unique HTTP method and URL pattern combination
 * to identify dead endpoints or high-traffic paths.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * RouteUsageMetric metric = RouteUsageMetric.builder()
 *     .controllerClass("NoteController")
 *     .controllerMethod("getNoteById")
 *     .httpMethod("GET")
 *     .pattern("/api/v1/notes/{id}")
 *     .invocationCount(42L)
 *     .firstSeenAt(Instant.now())
 *     .lastInvokedAt(Instant.now())
 *     .build();
 * }</pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "route_usage_metrics",
    schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_route_usage_method_pattern",
            columnNames = {"http_method", "pattern"}
        )
    }
)
@Schema(description = "Represents aggregated invocation metrics for a registered API route")
public class RouteUsageMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @Schema(
        description = "Unique metric entry identifier",
        example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"
    )
    private UUID id;

    @Column(name = "controller_class", nullable = false)
    @Schema(
        description = "Declaring controller simple class name",
        example = "NoteController"
    )
    private String controllerClass;

    @Column(name = "controller_method", nullable = false)
    @Schema(
        description = "Target controller handler method name",
        example = "getNoteById"
    )
    private String controllerMethod;

    @Column(name = "http_method", length = 10, nullable = false)
    @Schema(
        description = "HTTP request method",
        example = "GET"
    )
    private String httpMethod;

    @Column(name = "pattern", nullable = false)
    @Schema(
        description = "Resolved URL path pattern",
        example = "/api/v1/notes/{id}"
    )
    private String pattern;

    @Builder.Default
    @Column(name = "invocation_count", nullable = false)
    @Schema(
        description = "Total number of request invocations recorded",
        example = "128"
    )
    private long invocationCount = 0L;

    @Builder.Default
    @Column(name = "first_seen_at", nullable = false, updatable = false)
    @Schema(
        description = "Timestamp when the route was first cataloged",
        example = "2026-08-17T18:00:00.000Z"
    )
    private Instant firstSeenAt = Instant.now();

    @Column(name = "last_invoked_at")
    @Schema(
        description = "Timestamp of the most recent invocation",
        example = "2026-08-17T20:30:00.000Z"
    )
    private Instant lastInvokedAt;

    /**
     * Lifecycle callback initializing default timestamps prior to database persistence.
     */
    @PrePersist
    protected void onCreate() {
        if (this.firstSeenAt == null) {
            this.firstSeenAt = Instant.now();
        }
    }

    /**
     * Increments the invocation count by 1 and updates the {@code lastInvokedAt} timestamp.
     *
     * <p>Example usage:
     * <pre>{@code
     * metric.recordAccess();
     * }</pre>
     */
    public void recordAccess() {
        this.invocationCount++;
        this.lastInvokedAt = Instant.now();
    }
}