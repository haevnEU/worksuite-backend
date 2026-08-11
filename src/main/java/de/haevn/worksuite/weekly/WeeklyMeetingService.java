package de.haevn.worksuite.weekly;

import de.haevn.worksuite.common.exceptions.BadRequestException;
import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.push.WebsocketPushService;
import de.haevn.worksuite.push.events.Priority;
import de.haevn.worksuite.push.events.WsEvent;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing weekly sprint meeting generation, task tracking, daily progress notes, and WebSocket broadcasts.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private WeeklyMeetingService weeklyMeetingService;
 *
 * WeeklyMeeting meeting = weeklyMeetingService.generateNextWeek();
 * weeklyMeetingService.addToMeeting(LocalDate.now(), meeting.getId(), "Refactor service layer");
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class WeeklyMeetingService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String EVENT_TASK_ADDED = "Added new task '%s' to day %s for meeting '%s'.";
    private static final String EVENT_DAY_SUMMARY_UPDATED = "Updated summary for day %s in meeting '%s'.";
    private static final String EVENT_OVERALL_SUMMARY_UPDATED = "Updated overall weekly summary for meeting '%s'.";

    private final WebsocketPushService websocketPushService;
    private final WeeklyMeetingRepository weeklyMeetingRepository;

    /**
     * Scheduled cron task generating the weekly sprint meeting protocol every Tuesday at 12:00 UTC.
     */
    @Scheduled(cron = "0 0 12 * * TUE", zone = "UTC")
    @Transactional
    public void scheduledWeeklyGeneration() {
        log.info("Executing scheduled weekly sprint meeting generation");
        generateNextWeek();
    }

    /**
     * Generates a new {@link WeeklyMeeting} for the current week starting on Tuesday and covering 7 working days.
     *
     * @return the saved {@link WeeklyMeeting} entity
     * @throws BadRequestException if a meeting for the current Tuesday has already been generated
     */
    @Transactional
    public WeeklyMeeting generateNextWeek() {
        final LocalDate targetTuesday =
            LocalDate.now(ZoneOffset.UTC).with(TemporalAdjusters.previousOrSame(DayOfWeek.TUESDAY));

        final Instant startOfDay = targetTuesday.atStartOfDay(ZoneOffset.UTC).toInstant();
        final Instant endOfDay = targetTuesday.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        if (weeklyMeetingRepository.existsByCreatedAtBetween(startOfDay, endOfDay)) {
            throw new BadRequestException("A weekly meeting for Tuesday, " + targetTuesday + " already exists.");
        }

        final LocalDate endTuesday = targetTuesday.plusWeeks(1);
        final String meetingTitle = "Weekly Sprint (%s - %s)".formatted(targetTuesday.format(DATE_FORMATTER),
            endTuesday.format(DATE_FORMATTER));

        final WeeklyMeeting meeting =
            WeeklyMeeting.builder().title(meetingTitle).summary("").daySummaries(new ArrayList<>()).build();

        populateWorkdaySummaries(meeting, targetTuesday);
        final WeeklyMeeting savedMeeting = weeklyMeetingRepository.save(meeting);
        log.info("Generated weekly meeting with ID: '{}' and title: '{}'", savedMeeting.getId(),
            savedMeeting.getTitle());

        return savedMeeting;
    }

    /**
     * Retrieves all weekly meeting records as DTOs.
     *
     * @return list of {@link WeeklyMeetingDTO} instances
     */
    @Transactional(readOnly = true)
    public List<WeeklyMeetingDTO> getAll() {
        return weeklyMeetingRepository.findAll().stream().map(WeeklyMeetingDTO::fromModel).toList();
    }

    /**
     * Retrieves a single weekly meeting by its identifier.
     *
     * @param meetingId meeting unique identifier
     * @return the matching {@link WeeklyMeetingDTO}
     * @throws NotFoundException if the meeting is not found
     */
    @Transactional(readOnly = true)
    public WeeklyMeetingDTO getById(final UUID meetingId) {
        return WeeklyMeetingDTO.fromModel(findMeetingEntity(meetingId));
    }

    /**
     * Adds a task to a specific day within the given meeting protocol.
     *
     * @param day target calendar date
     * @param meetingId meeting unique identifier
     * @param task description of the task completed
     */
    @Transactional
    public void addToMeeting(final LocalDate day, final UUID meetingId, final String task) {
        Objects.requireNonNull(day, "Date must not be null");
        Objects.requireNonNull(task, "Task must not be null");

        final WeeklyMeeting meeting = findMeetingEntity(meetingId);
        final DaySummary daySummary = findOrCreateDaySummary(meeting, day);
        daySummary.getTasks().add(task.trim());
        weeklyMeetingRepository.save(meeting);

        broadcastEvent(Priority.INFO, EVENT_TASK_ADDED.formatted(task.trim(), day, meeting.getTitle()));
    }

    /**
     * Updates the daily summary text for a specific day in the meeting protocol.
     *
     * @param day target calendar date
     * @param meetingId meeting unique identifier
     * @param summary summary progress text
     */
    @Transactional
    public void addDaySummary(final LocalDate day, final UUID meetingId, final String summary) {
        Objects.requireNonNull(day, "Date must not be null");

        final WeeklyMeeting meeting = findMeetingEntity(meetingId);
        final DaySummary daySummary = findOrCreateDaySummary(meeting, day);
        daySummary.setSummary(summary != null ? summary.trim() : "");
        weeklyMeetingRepository.save(meeting);

        broadcastEvent(Priority.INFO, EVENT_DAY_SUMMARY_UPDATED.formatted(day, meeting.getTitle()));
    }

    /**
     * Updates the overall weekly sprint summary for the meeting.
     *
     * @param meetingId meeting unique identifier
     * @param summary overarching summary text
     */
    @Transactional
    public void addSummary(final UUID meetingId, final String summary) {
        final WeeklyMeeting meeting = findMeetingEntity(meetingId);
        meeting.setSummary(summary != null ? summary.trim() : "");
        weeklyMeetingRepository.save(meeting);

        broadcastEvent(Priority.INFO, EVENT_OVERALL_SUMMARY_UPDATED.formatted(meeting.getTitle()));
    }

    /**
     * Finds a persistent {@link WeeklyMeeting} entity by ID or throws {@link NotFoundException}.
     *
     * @param meetingId meeting unique identifier
     * @return the persistent {@link WeeklyMeeting} entity
     * @throws NotFoundException if the meeting is not found
     */
    public WeeklyMeeting findMeetingEntity(final UUID meetingId) {
        Objects.requireNonNull(meetingId, "Meeting ID must not be null");
        return weeklyMeetingRepository.findById(meetingId).orElseThrow(NotFoundException::new);
    }

    /**
     * Initializes default {@link DaySummary} child entries for weekdays over the sprint timeframe.
     *
     * <p>Example usage:
     * <pre>{@code
     * populateWorkdaySummaries(meeting, startDate);
     * }</pre>
     *
     * @param meeting target {@link WeeklyMeeting} entity
     * @param startTuesday start date of the sprint
     */
    private void populateWorkdaySummaries(final WeeklyMeeting meeting, final LocalDate startTuesday) {
        for (int i = 0; i <= 7; i++) {
            final LocalDate currentDate = startTuesday.plusDays(i);
            final DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                continue;
            }

            final DaySummary daySummary =
                DaySummary.builder().date(currentDate.atStartOfDay(ZoneOffset.UTC).toInstant()).summary("")
                    .tasks(new ArrayList<>()).build();

            meeting.addDaySummary(daySummary);
        }
    }

    /**
     * Finds the {@link DaySummary} matching the given date, or initializes a new one if absent.
     *
     * <p>Example usage:
     * <pre>{@code
     * DaySummary daySummary = findOrCreateDaySummary(meeting, LocalDate.now());
     * }</pre>
     *
     * @param meeting target {@link WeeklyMeeting} entity
     * @param day target calendar date
     * @return matching or newly attached {@link DaySummary}
     */
    private DaySummary findOrCreateDaySummary(final WeeklyMeeting meeting, final LocalDate day) {
        return meeting.getDaySummaries().stream().filter(
                ds -> ds.getDate() != null && ds.getDate().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(day))
            .findFirst().orElseGet(() -> {
                final DaySummary newDay =
                    DaySummary.builder().date(day.atStartOfDay(ZoneId.systemDefault()).toInstant()).summary("")
                        .tasks(new ArrayList<>()).build();
                meeting.addDaySummary(newDay);
                return newDay;
            });
    }

    /**
     * Broadcasts a real-time event through {@link WebsocketPushService}.
     *
     * <p>Example usage:
     * <pre>{@code
     * broadcastEvent(Priority.INFO, "Meeting updated");
     * }</pre>
     *
     * @param priority severity level
     * @param message notification message
     */
    private void broadcastEvent(final Priority priority, final String message) {
        websocketPushService.dispatch(new WsEvent(getClass(), priority, message));
    }
}