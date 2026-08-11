package de.haevn.worksuite.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a persistent sprint review entry in the relational database.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * Review review = Review.builder()
 *     .ticketNumber("TICK-100")
 *     .title("Kafka Consumer Optimization")
 *     .type(ReviewType.PRESENTATION)
 *     .content("Key point 1\nKey point 2")
 *     .build();
 * }</pre>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "ticket_number", nullable = false, length = 50)
    private String ticketNumber;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ReviewType type;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(name = "is_archived", nullable = false)
    private boolean isArchived = false;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    /**
     * Converts this JPA entity into an immutable {@link ReviewRecord}.
     *
     * <p>Example usage:
     * <pre>{@code
     * ReviewRecord record = reviewEntity.toRecord();
     * }</pre>
     *
     * @return the mapped {@link ReviewRecord}
     */
    public ReviewRecord toRecord() {
        return new ReviewRecord(id, ticketNumber, title, description, type, content, isArchived, createdAt);
    }
}