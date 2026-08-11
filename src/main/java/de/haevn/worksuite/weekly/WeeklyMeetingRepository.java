package de.haevn.worksuite.weekly;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyMeetingRepository extends JpaRepository<WeeklyMeeting, UUID> {
    boolean existsByCreatedAtGreaterThanEqual(Instant startOfWeek);
    boolean existsByCreatedAtBetween(Instant start, Instant end);
}