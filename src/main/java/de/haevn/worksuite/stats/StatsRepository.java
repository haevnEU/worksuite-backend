package de.haevn.worksuite.stats;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository managing database persistence and historical queries for {@link Stats} entities.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private StatsRepository statsRepository;
 *
 * List<Stats> lastSevenDays = statsRepository.findStatsBefore(Instant.now(), 7);
 * }</pre>
 */
@Repository
public interface StatsRepository extends JpaRepository<Stats, UUID> {

    /**
     * Finds statistics records created before the specified instant, ordered by creation date descending with pagination.
     *
     * @param date upper boundary timestamp
     * @param pageable pagination and limit constraints
     * @return list of matching {@link Stats} records
     */
    List<Stats> findByCreatedAtBeforeOrderByCreatedAtDesc(Instant date, Pageable pageable);

    /**
     * Convenience query method to retrieve the latest statistics records prior to a given instant.
     *
     * <p>Example:
     * <pre>{@code
     * List<Stats> recentStats = statsRepository.findStatsBefore(Instant.now(), 14);
     * }</pre>
     *
     * @param date upper boundary timestamp
     * @param amount maximum number of records to retrieve
     * @return limited list of {@link Stats} records
     */
    default List<Stats> findStatsBefore(final Instant date, final int amount) {
        return findByCreatedAtBeforeOrderByCreatedAtDesc(date, Pageable.ofSize(Math.max(amount, 1)));
    }
}