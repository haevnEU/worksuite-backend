package de.haevn.worksuite.time;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimeRepository extends JpaRepository<TimeEntry, UUID> {
    List<TimeEntry> findByDateBetweenOrderByDateDesc(Instant start, Instant end);

    @Query("SELECT t FROM TimeEntry t WHERE t.date >= :startDate ORDER BY t.date DESC")
    List<TimeEntry> findEntriesFromDate(@Param("startDate") Instant startDate);
}