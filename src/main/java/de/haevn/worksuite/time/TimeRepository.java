package de.haevn.worksuite.time;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository managing persistence and queries for {@link TimeEntry} records.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private TimeRepository timeRepository;
 *
 * List<TimeEntry> entries = timeRepository.findEntriesFromDate(Instant.now().minus(7, ChronoUnit.DAYS));
 * }</pre>
 */
@Repository
public interface TimeRepository extends JpaRepository<TimeEntry, UUID> {

    /**
     * Finds time entries with dates falling between the start and end timestamps, ordered by date descending.
     *
     * @param start start timestamp boundary
     * @param end end timestamp boundary
     * @return list of matching {@link TimeEntry} records
     */
    List<TimeEntry> findByDateBetweenOrderByDateDesc(Instant start, Instant end);

    /**
     * Finds all time entries on or after the specified start date timestamp, ordered by date descending.
     *
     * @param startDate lower boundary timestamp
     * @return list of matching {@link TimeEntry} records
     */
    @Query("SELECT t FROM TimeEntry t WHERE t.date >= :startDate ORDER BY t.date DESC")
    List<TimeEntry> findEntriesFromDate(@Param("startDate") Instant startDate);
}