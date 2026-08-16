package de.haevn.worksuite.stats;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "stats")
public class Stats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "moved_to_qa")
    private int movedToQa;

    @Column(name = "moved_to_review")
    private int movedToReview;

    @Column(name = "return_from_qa")
    private int returnFromQa;

    @Column(name = "return_from_review")
    private int returnFromReview;

    @Column(name = "hours_spent")
    private int hoursSpent;

    @Column(name = "day")
    private Instant day;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

}
