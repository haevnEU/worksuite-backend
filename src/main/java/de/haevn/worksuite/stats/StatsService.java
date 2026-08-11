package de.haevn.worksuite.stats;

import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.time.TimeEntry;
import de.haevn.worksuite.time.TimeService;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing developer statistics, metric increments, and time logging aggregations.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private StatsService statsService;
 *
 * UUID recordId = statsService.createNewRecord();
 * statsService.incrementStat(recordId, Stat.MOVED_TO_REVIEW, 1);
 * List<Stats> lastWeek = statsService.findAllStatsModels(7);
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class StatsService {

    private static final int DEFAULT_DAYS_LIMIT = 7;

    private final StatsRepository statsRepository;
    private final TimeService timeService;

    /**
     * Finds a {@link Stats} entity by its unique ID.
     *
     * @param id primary unique identifier
     * @return the persistent {@link Stats} entity
     * @throws NotFoundException if the record is not found
     */
    @Transactional(readOnly = true)
    public Stats findStatsModelById(final UUID id) {
        Objects.requireNonNull(id, "Stats ID must not be null");
        return statsRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    /**
     * Retrieves statistics records up to the given limit and calculates logged task hours.
     *
     * @param limit number of days to inspect (defaults to 7 if non-positive)
     * @return updated list of {@link Stats} entities
     */
    @Transactional
    public List<Stats> findAllStatsModels(final int limit) {
        final int amount = (limit > 0) ? limit : DEFAULT_DAYS_LIMIT;

        final List<TimeEntry> times = timeService.getAll(amount);
        final List<Stats> modelList = statsRepository.findStatsBefore(Instant.now(), amount);
        final Map<LocalDate, Integer> hoursPerDay = aggregateHoursByDate(times);

        updateLoggedHours(modelList, hoursPerDay);
        return statsRepository.saveAll(modelList);
    }

    /**
     * Creates a new empty daily statistics record for the current timestamp.
     *
     * @return generated record UUID
     */
    @Transactional
    public UUID createNewRecord() {
        return createNewRecord(Instant.now());
    }

    /**
     * Creates a new empty statistics record for a designated target date.
     *
     * @param date target timestamp
     * @return generated record UUID
     */
    @Transactional
    public UUID createNewRecord(final Instant date) {
        final Stats statsModel =
            Stats.builder().day(Objects.requireNonNullElseGet(date, Instant::now)).hoursSpent(0).movedToQa(0)
                .movedToReview(0).returnFromQa(0).returnFromReview(0).build();

        return statsRepository.save(statsModel).getId();
    }

    /**
     * Increments a specific developer metric counter on a statistics record.
     *
     * <p>If the referenced record belongs to a previous day, a new entry is initialized for the current day.
     *
     * @param uuid record identifier
     * @param stat metric category to increment
     * @param amount amount to add
     */
    @Transactional
    public void incrementStat(final UUID uuid, final Stat stat, final int amount) {
        Objects.requireNonNull(stat, "Stat type must not be null");

        Stats statsModel = findStatsModelById(uuid);
        final Instant startOfToday = LocalDate.now(ZoneOffset.UTC).atStartOfDay(ZoneOffset.UTC).toInstant();

        if (statsModel.getDay() != null && statsModel.getDay().isBefore(startOfToday)) {
            statsModel = Stats.builder().day(Instant.now()).build();
        }

        applyStatIncrement(statsModel, stat, amount);
        statsRepository.save(statsModel);
    }

    /**
     * Groups logged time entries by {@link LocalDate} and calculates total hours.
     *
     * <p>Example usage:
     * <pre>{@code
     * Map<LocalDate, Integer> dailyHours = aggregateHoursByDate(timeEntries);
     * }</pre>
     *
     * @param times list of {@link TimeEntry} records
     * @return map of dates to total logged hours
     */
    private Map<LocalDate, Integer> aggregateHoursByDate(final List<TimeEntry> times) {
        if (times == null) {
            return Map.of();
        }
        return times.stream().filter(time -> time.getDate() != null).collect(
            Collectors.groupingBy(time -> time.getDate().atZone(ZoneOffset.UTC).toLocalDate(),
                Collectors.summingInt(time -> ((time.getHours() * 60) + time.getMinutes()) / 60)));
    }

    /**
     * Synchronizes calculated logged hours into existing {@link Stats} records.
     *
     * <p>Example usage:
     * <pre>{@code
     * updateLoggedHours(statsList, hoursMap);
     * }</pre>
     *
     * @param statsList list of stats models to update
     * @param hoursPerDay map of pre-computed hours per day
     */
    private void updateLoggedHours(final List<Stats> statsList, final Map<LocalDate, Integer> hoursPerDay) {
        statsList.forEach(stats -> {
            if (stats.getDay() != null) {
                final LocalDate statsDate = stats.getDay().atZone(ZoneOffset.UTC).toLocalDate();
                stats.setHoursSpent(hoursPerDay.getOrDefault(statsDate, 0));
            }
        });
    }

    /**
     * Applies the numerical increment to the matching field on a {@link Stats} entity.
     *
     * <p>Example usage:
     * <pre>{@code
     * applyStatIncrement(model, Stat.MOVED_TO_QA, 2);
     * }</pre>
     *
     * @param model target entity
     * @param stat metric category
     * @param amount increment step value
     */
    private void applyStatIncrement(final Stats model, final Stat stat, final int amount) {
        switch (stat) {
            case MOVED_TO_QA -> model.setMovedToQa(model.getMovedToQa() + amount);
            case RETURN_FROM_QA -> model.setReturnFromQa(model.getReturnFromQa() + amount);
            case MOVED_TO_REVIEW -> model.setMovedToReview(model.getMovedToReview() + amount);
            case RETURN_FROM_REVIEW -> model.setReturnFromReview(model.getReturnFromReview() + amount);
        }
    }
}