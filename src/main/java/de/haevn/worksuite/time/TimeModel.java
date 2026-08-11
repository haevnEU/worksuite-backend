package de.haevn.worksuite.time;



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
@Table(name = "time_entries")
public class TimeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hours")
    private int hours;

    @Column(name = "minutes")
    private int minutes;

    @Column(name = "date")
    private Instant date;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "activity_id")
    private long activityId;

    @Column(name = "ticket_id")
    private long ticketId;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

}
