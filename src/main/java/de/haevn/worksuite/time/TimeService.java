package de.haevn.worksuite.time;

import de.haevn.worksuite.ticket.dtos.LogTimeRequest;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing local time entry queries, aggregations, and synchronization with ticket logging.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private TimeService timeService;
 *
 * List<TimeEntry> todayEntries = timeService.getForToday();
 * List<TimeEntry> pastWeek = timeService.getAll(7);
 * }</pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeService {

    private final TimeRepository timeRepository;

    /**
     * Retrieves all time entries recorded for the current day.
     *
     * @return list of today's {@link TimeEntry} records
     */
    @Transactional(readOnly = true)
    public List<TimeEntry> getForToday() {
        final Instant startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        final Instant endOfToday = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return timeRepository.findByDateBetweenOrderByDateDesc(startOfToday, endOfToday);
    }

    /**
     * Retrieves all time entries recorded within the past specified number of days.
     *
     * @param historyDays count of past days to query
     * @return list of {@link TimeEntry} records
     */
    @Transactional(readOnly = true)
    public List<TimeEntry> getAll(final int historyDays) {
        final Instant startThreshold =
            LocalDate.now().minusDays(Math.max(historyDays, 0)).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return timeRepository.findEntriesFromDate(startThreshold);
    }

    /**
     * Persists a local {@link TimeEntry} matching a remote ticket booking.
     *
     * @param ticketId associated ticket identifier
     * @param timeDTO time payload details
     */
    @Transactional
    public void book(final long ticketId, final LogTimeRequest timeDTO) {
        Objects.requireNonNull(timeDTO, "LogTimeRequest must not be null");

        final Instant workDate = LocalDate.parse(timeDTO.day()).atStartOfDay(ZoneId.systemDefault()).toInstant();

        final TimeEntry timeEntry =
            TimeEntry.builder().ticketId(ticketId).activityId(timeDTO.activityId()).hours(timeDTO.hours())
                .minutes(timeDTO.minutes()).description(timeDTO.comment()).date(workDate).build();

        timeRepository.save(timeEntry);
        log.info("Recorded local time entry: {}h {}m on ticket #{}", timeDTO.hours(), timeDTO.minutes(), ticketId);
    }

    public TotalTimeDTO getWeeklyTotal() {
        final Instant startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay(ZoneId.systemDefault()).toInstant();
        final Instant endOfWeek = startOfWeek.plus(7, ChronoUnit.DAYS);

        final List<TimeEntry> entries = timeRepository.findByDateBetweenOrderByDateDesc(startOfWeek, endOfWeek);
        final int totalHours = entries.stream().mapToInt(TimeEntry::getHours).sum();
        final int totalMinutes = entries.stream().mapToInt(TimeEntry::getMinutes).sum();

        // normalize hours and minutes
        int normalizedHours = totalHours + totalMinutes / 60;
        int normalizedMinutes = totalMinutes % 60;

        return new TotalTimeDTO(normalizedHours, normalizedMinutes);
    }
}