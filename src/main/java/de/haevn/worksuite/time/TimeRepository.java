package de.haevn.worksuite.time;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimeRepository extends JpaRepository<TimeModel, UUID> {
    List<TimeModel> findByDateBetweenOrderByDateDesc(Instant start, Instant end);

    @Query("SELECT t FROM TimeModel t WHERE t.date >= :startDate ORDER BY t.date DESC")
    List<TimeModel> findEntriesFromDate(@Param("startDate") Instant startDate);
}