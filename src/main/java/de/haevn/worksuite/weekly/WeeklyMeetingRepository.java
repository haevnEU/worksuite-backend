package de.haevn.worksuite.weekly;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository managing database persistence for {@link WeeklyMeeting} entities.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private WeeklyMeetingRepository repository;
 *
 * boolean exists = repository.existsByCreatedAtBetween(startInstant, endInstant);
 * }</pre>
 */
@Repository
public interface WeeklyMeetingRepository extends JpaRepository<WeeklyMeeting, UUID> {

    /**
     * Checks if a meeting protocol exists created on or after the specified timestamp.
     *
     * @param startOfWeek boundary timestamp
     * @return {@code true} if a matching record exists, otherwise {@code false}
     */
    boolean existsByCreatedAtGreaterThanEqual(Instant startOfWeek);

    /**
     * Checks if a meeting protocol exists created within the designated time boundary.
     *
     * @param start start timestamp boundary
     * @param end end timestamp boundary
     * @return {@code true} if a matching meeting exists, otherwise {@code false}
     */
    boolean existsByCreatedAtBetween(Instant start, Instant end);
}