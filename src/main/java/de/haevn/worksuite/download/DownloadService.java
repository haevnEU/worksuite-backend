package de.haevn.worksuite.download;

import de.haevn.worksuite.common.FileDownloadService;
import de.haevn.worksuite.common.PdfService;
import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.notes.Note;
import de.haevn.worksuite.notes.NoteRepository;
import de.haevn.worksuite.retro.Retro;
import de.haevn.worksuite.retro.RetroRepository;
import de.haevn.worksuite.share.ShareService;
import de.haevn.worksuite.weekly.DaySummary;
import de.haevn.worksuite.weekly.WeeklyMeeting;
import de.haevn.worksuite.weekly.WeeklyMeetingService;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service orchestrating synchronous file streaming, PDF generation, and external attachment downloads.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private DownloadService downloadService;
 *
 * RequestDTO dto = new RequestDTO(null, null, "c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a", false, null, null);
 * ResponseEntity<Resource> response = downloadService.downloadSynchronous(DownloadModule.NOTEBOOK_EXPORT, dto);
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class DownloadService {

    private static final String ZONE_BERLIN = "Europe/Berlin";
    private static final String REDMINE_API_KEY_HEADER = "X-Redmine-API-Key";

    private final WeeklyMeetingService weeklyMeetingService;
    private final FileDownloadService fileDownloadService;
    private final PdfService pdfService;
    private final NoteRepository noteRepository;
    private final RetroRepository retroRepository;
    private final ShareService shareService;

    /**
     * Dispatches download and rendering requests according to the selected {@link DownloadModule}.
     *
     * @param type target {@link DownloadModule} operation
     * @param dto request metadata containing entity identifiers and options
     * @return a {@link ResponseEntity} holding the streaming file {@link Resource}
     * @throws IOException if I/O or network streaming fails
     */
    public ResponseEntity<Resource> downloadSynchronous(final DownloadModule type, final RequestDTO dto)
        throws IOException {
        Objects.requireNonNull(type, "DownloadModule type must not be null");
        Objects.requireNonNull(dto, "RequestDTO payload must not be null");

        return switch (type) {
            case WEEKLY_MEETING_PROTOCOL -> downloadWeeklyMeetingProtocol(dto);
            case NOTEBOOK_EXPORT -> downloadNotebookExport(dto);
            case RETROSPECTIVE_PROTOCOL -> downloadRetrospectiveProtocol(dto);
            case TICKET_ATTACHMENT -> downloadTicketAttachment(dto);
            case FILE_SHARE -> downloadSharedFile(dto);
        };
    }

    /**
     * Retrieves and streams a shared file from storage.
     *
     * @param dto request payload specifying the shared file ID
     * @return a {@link ResponseEntity} containing the shared file {@link Resource}
     * @throws IOException if reading the file resource fails
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadSharedFile(final RequestDTO dto) throws IOException {
        log.info("Downloading shared file with id: '{}'", dto.id());
        final UUID fileId = UUID.fromString(dto.id());
        return shareService.downloadFile(fileId);
    }

    /**
     * Renders a weekly meeting protocol as a PDF document excluding weekend summaries.
     *
     * @param dto request payload specifying the meeting entity ID
     * @return a {@link ResponseEntity} containing the compiled PDF {@link Resource}
     */
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadWeeklyMeetingProtocol(final RequestDTO dto) {
        log.info("Downloading weekly meeting protocol with id: '{}'", dto.id());
        final UUID meetingId = UUID.fromString(dto.id());
        final boolean isDraft = dto.draftStatus();

        final WeeklyMeeting meeting = weeklyMeetingService.findMeetingEntity(meetingId);
        final List<DaySummary> workingDaySummaries = filterWorkdaySummaries(meeting.getDaySummaries());
        meeting.setDaySummaries(workingDaySummaries);

        final Map<String, Object> templateVariables = Map.of("meeting", meeting);
        final Resource pdfResource = pdfService.generatePdfResource("pdf/weekly-meeting", templateVariables, isDraft);
        final String filename = "weekly-meeting-%s.pdf".formatted(meetingId);

        return createPdfResponse(pdfResource, filename);
    }

    /**
     * Proxies remote ticket attachment downloads through {@link FileDownloadService}.
     *
     * @param dto request payload holding the target web URL and authentication key
     * @return a {@link ResponseEntity} streaming the remote attachment
     */
    public ResponseEntity<Resource> downloadTicketAttachment(final RequestDTO dto) {
        log.info("Proxying ticket attachment download from URL: '{}'", dto.webUrl());
        final URI downloadUri = URI.create(dto.webUrl());
        return fileDownloadService.downloadWithRestClient(downloadUri, REDMINE_API_KEY_HEADER, dto.apiKey());
    }

    /**
     * Exports a {@link Note} entity as a compiled PDF document.
     *
     * <p>Example usage:
     * <pre>{@code
     * ResponseEntity<Resource> response = downloadNotebookExport(dto);
     * }</pre>
     *
     * @param dto request payload specifying the note ID
     * @return a {@link ResponseEntity} containing the note PDF {@link Resource}
     */
    private ResponseEntity<Resource> downloadNotebookExport(final RequestDTO dto) {
        log.info("Downloading notebook export with id: '{}'", dto.id());
        final Note note = noteRepository.findById(UUID.fromString(dto.id())).orElseThrow(NotFoundException::new);

        final Map<String, Object> templateVariables = Map.of("note", note);
        final Resource pdfResource = pdfService.generatePdfResource("pdf/note", templateVariables, dto.draftStatus());
        final String sanitizedTitle = sanitizeFilename(note.getTitle());
        final String filename = "note-%s.pdf".formatted(sanitizedTitle);

        return createPdfResponse(pdfResource, filename);
    }

    /**
     * Exports a {@link Retro} sprint retrospective as a compiled PDF document.
     *
     * <p>Example usage:
     * <pre>{@code
     * ResponseEntity<Resource> response = downloadRetrospectiveProtocol(dto);
     * }</pre>
     *
     * @param dto request payload specifying the retrospective ID
     * @return a {@link ResponseEntity} containing the retrospective PDF {@link Resource}
     */
    private ResponseEntity<Resource> downloadRetrospectiveProtocol(final RequestDTO dto) {
        log.info("Downloading retrospective protocol with id: '{}'", dto.id());
        final Retro retro = retroRepository.findById(UUID.fromString(dto.id())).orElseThrow(NotFoundException::new);

        final Map<String, Object> templateVariables = Map.of("retro", retro);
        final Resource pdfResource = pdfService.generatePdfResource("pdf/retro", templateVariables, dto.draftStatus());
        final String sanitizedSprint = sanitizeFilename(retro.getSprintName());
        final String filename = "retro-%s.pdf".formatted(sanitizedSprint);

        return createPdfResponse(pdfResource, filename);
    }

    /**
     * Filters a list of day summaries to exclude Saturday and Sunday entries.
     *
     * <p>Example usage:
     * <pre>{@code
     * List<DaySummary> workdays = filterWorkdaySummaries(meeting.getDaySummaries());
     * }</pre>
     *
     * @param summaries the full list of day summaries
     * @return filtered list containing only weekday summaries or unassigned dates
     */
    private List<DaySummary> filterWorkdaySummaries(final List<DaySummary> summaries) {
        if (summaries == null) {
            return List.of();
        }
        return summaries.stream().filter(day -> {
            if (day.getDate() == null) {
                return true;
            }
            final DayOfWeek dow = day.getDate().atZone(ZoneId.of(ZONE_BERLIN)).getDayOfWeek();
            return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
        }).toList();
    }

    /**
     * Sanitizes string content into safe filename-compatible characters.
     *
     * <p>Example usage:
     * <pre>{@code
     * String clean = sanitizeFilename("Sprint 24 / Final Review");
     * // Result: "sprint-24-/-final-review" -> "sprint-24-final-review"
     * }</pre>
     *
     * @param input the raw string to sanitize
     * @return lowercase, dash-separated string
     */
    private String sanitizeFilename(final String input) {
        if (input == null || input.isBlank()) {
            return "document";
        }
        return input.trim().toLowerCase().replaceAll("[^a-z0-9-_]+", "-");
    }

    /**
     * Constructs a {@link ResponseEntity} configured with inline/attachment headers and PDF content type.
     *
     * <p>Example usage:
     * <pre>{@code
     * return createPdfResponse(resource, "summary.pdf");
     * }</pre>
     *
     * @param pdfResource the binary PDF resource
     * @param filename desired client download filename
     * @return fully configured {@link ResponseEntity}
     */
    private ResponseEntity<Resource> createPdfResponse(final Resource pdfResource, final String filename) {
        final ContentDisposition contentDisposition =
            ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString()).body(pdfResource);
    }
}