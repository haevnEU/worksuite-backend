package de.haevn.worksuite.stats;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatsRepository extends JpaRepository<StatsModel, UUID> {
    List<StatsModel> findByCreatedAtBeforeOrderByCreatedAtDesc(final Instant date, final Pageable pageable);

    default List<StatsModel> findStatsBefore(final Instant date, final int amount) {
        return findByCreatedAtBeforeOrderByCreatedAtDesc(date, Pageable.ofSize(amount));
    }
}