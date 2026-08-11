package de.haevn.worksuite.weekly;

import de.haevn.worksuite.common.PdfService;
import de.haevn.worksuite.common.exceptions.BadRequestException;
import de.haevn.worksuite.push.WebsocketPushService;
import de.haevn.worksuite.push.events.Priority;
import de.haevn.worksuite.push.events.WsEvent;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class WeeklyMeetingService {

    private final WebsocketPushService websocketPushService;
    private final WeeklyMeetingRepository weeklyMeetingRepository;
    private final PdfService pdfService;

    @Scheduled(cron = "0 0 12 * * TUE", zone = "UTC")
    @Transactional
    public void scheduledWeeklyGeneration() {
        generateNextWeek();
    }

    @Transactional
    public WeeklyMeeting generateNextWeek() {
        // 1. Berechne den maßgeblichen Dienstag (heute oder der vorherige Dienstag)
        LocalDate targetTuesday =
            LocalDate.now(ZoneOffset.UTC).with(TemporalAdjusters.previousOrSame(DayOfWeek.TUESDAY));

        Instant startOfDay = targetTuesday.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = targetTuesday.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        // 2. Duplikats-Prüfung: Existiert bereits ein Meeting, das an diesem Dienstag erstellt wurde?
        if (weeklyMeetingRepository.existsByCreatedAtBetween(startOfDay, endOfDay)) {
            throw new BadRequestException("A weekly meeting for Tuesday, " + targetTuesday + " already exists.");
        }

        // 3. Entity & Titel aufbauen (Dienstag bis Dienstag der Folgewoche)
        LocalDate endTuesday = targetTuesday.plusWeeks(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String meetingTitle =
            String.format("Weekly Sprint (%s - %s)", targetTuesday.format(formatter), endTuesday.format(formatter));

        WeeklyMeeting meeting = new WeeklyMeeting();
        meeting.setTitle(meetingTitle);
        meeting.setCreatedAt(Instant.now());
        meeting.setSummary("");
        meeting.setDaySummaries(new ArrayList<>());

        // 4. Tage anlegen (0 bis 7 = 8 Tage-Spanne, Samstag/Sonntag überspringen)
        for (int i = 0; i <= 7; i++) {
            LocalDate currentDate = targetTuesday.plusDays(i);
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                continue;
            }

            DaySummary daySummary = new DaySummary();
            daySummary.setDate(currentDate.atStartOfDay(ZoneOffset.UTC).toInstant());
            daySummary.setSummary("");
            daySummary.setTasks(new ArrayList<>());
            daySummary.setCreatedAt(Instant.now());

            daySummary.setWeeklyMeeting(meeting);
            meeting.getDaySummaries().add(daySummary);
        }

        // 5. Persistieren
        return weeklyMeetingRepository.save(meeting);
    }

    @Transactional(readOnly = true)
    public List<WeeklyMeetingDTO> getAll() {
        return weeklyMeetingRepository.findAll().stream().map(WeeklyMeetingDTO::fromModel).toList();
    }

    @Transactional(readOnly = true)
    public WeeklyMeetingDTO getById(UUID meetingId) {
        return weeklyMeetingRepository.findById(meetingId).map(WeeklyMeetingDTO::fromModel)
            .orElseThrow(() -> new NoSuchElementException("Meeting nicht gefunden: " + meetingId));
    }

    @Transactional
    public void addToMeeting(LocalDate day, UUID meetingId, String task) {
        WeeklyMeeting meeting = findMeetingEntity(meetingId);
        DaySummary daySummary = findOrCreateDaySummary(meeting, day);
        daySummary.getTasks().add(task);
        weeklyMeetingRepository.save(meeting);

        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO,
            String.format("Added new task '%s' to day %s for meeting '%s'.", task, day, meeting.getTitle())));
    }

    @Transactional
    public void addDaySummary(LocalDate day, UUID meetingId, String summary) {
        WeeklyMeeting meeting = findMeetingEntity(meetingId);
        DaySummary daySummary = findOrCreateDaySummary(meeting, day);
        daySummary.setSummary(summary);
        weeklyMeetingRepository.save(meeting);

        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO,
            String.format("Updated summary for day %s in meeting '%s'.", day, meeting.getTitle())));
    }

    @Transactional
    public void addSummary(UUID meetingId, String summary) {
        WeeklyMeeting meeting = findMeetingEntity(meetingId);
        meeting.setSummary(summary);
        weeklyMeetingRepository.save(meeting);

        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO,
            String.format("Updated overall weekly summary for meeting '%s'.", meeting.getTitle())));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> exportPdf(final UUID meetingId, final boolean isDraft) {
        final WeeklyMeeting meeting = findMeetingEntity(meetingId);
        final List<DaySummary> filteredSummaries = meeting.getDaySummaries().stream().filter(day -> {
            if (day.getDate() == null) {
                return true;
            }
            final DayOfWeek dow = day.getDate().atZone(ZoneId.of("Europe/Berlin")).getDayOfWeek();
            return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
        }).toList();
        meeting.setDaySummaries(filteredSummaries);

        final Map<String, Object> variables = Map.of("meeting", meeting);
        final Resource pdfResource = pdfService.generatePdfResource("pdf/weekly-meeting", variables, isDraft);

        final String filename = "weekly-meeting-" + meetingId + ".pdf";
        final ContentDisposition contentDisposition =
            ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build();


        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString()).body(pdfResource);
    }

    // --- Helper Methods ---

    private WeeklyMeeting findMeetingEntity(UUID meetingId) {
        return weeklyMeetingRepository.findById(meetingId)
            .orElseThrow(() -> new NoSuchElementException("Meeting nicht gefunden: " + meetingId));
    }

    private DaySummary findOrCreateDaySummary(WeeklyMeeting meeting, LocalDate day) {
        return meeting.getDaySummaries().stream().filter(
                ds -> ds.getDate() != null && ds.getDate().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(day))
            .findFirst().orElseGet(() -> {
                DaySummary newDay = new DaySummary();
                newDay.setDate(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
                meeting.addDaySummary(newDay);
                return newDay;
            });
    }
}