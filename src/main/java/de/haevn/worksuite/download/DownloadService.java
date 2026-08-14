package de.haevn.worksuite.download;

import de.haevn.worksuite.common.FileDownloadService;
import de.haevn.worksuite.common.PdfService;
import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.notes.NoteModel;
import de.haevn.worksuite.notes.NoteRepository;
import de.haevn.worksuite.retro.RetroModel;
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

@Service
@RequiredArgsConstructor
@Log4j2
public class DownloadService {

    private final WeeklyMeetingService weeklyMeetingService;
    private final FileDownloadService fileDownloadService;
    private final PdfService pdfService;
    private final NoteRepository noteRepository;
    private final RetroRepository retroRepository;
    private final ShareService shareService;

    public ResponseEntity<Resource> downloadSynchronous(final DownloadModule type, final RequestDTO dto) throws IOException {
        return switch (type) {
            case WEEKLY_MEETING_PROTOCOL -> downloadWeeklyMeetingProtocol(dto);
            case NOTEBOOK_EXPORT -> downloadNotebookExport(dto);
            case RETROSPECTIVE_PROTOCOL -> downloadRetrospectiveProtocol(dto);
            case TICKET_ATTACHMENT -> downloadTicketAttachment(dto);
            case FILE_SHARE -> downloadSharedFile(dto);
        };
    }


    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadSharedFile(final RequestDTO dto) throws IOException {
        log.info("Downloading shared file with id: " + dto.id());
        final UUID fileId = UUID.fromString(dto.id());
        return shareService.downloadFile(fileId);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadWeeklyMeetingProtocol(final RequestDTO dto) {
        log.info("Downloading weekly meeting protocol with id: " + dto.id());
        final UUID meetingId = UUID.fromString(dto.id());
        final boolean isDraft = dto.isDraft();
        final WeeklyMeeting meeting = weeklyMeetingService.findMeetingEntity(meetingId);
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


    private ResponseEntity<Resource> downloadNotebookExport(final RequestDTO dto) {
        log.info("Downloading notebook export with id: " + dto.id());
        final NoteModel model = noteRepository.findById(UUID.fromString(dto.id())).orElseThrow(NotFoundException::new);

        final Map<String, Object> variables = Map.of("note", model);
        final Resource pdfResource = pdfService.generatePdfResource("pdf/note", variables, dto.isDraft());

        final String filename = "note-" + model.getTitle() + ".pdf";
        final ContentDisposition contentDisposition =
            ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString()).body(pdfResource);
    }

    private ResponseEntity<Resource> downloadRetrospectiveProtocol(final RequestDTO dto) {
        log.info("Downloading retrospective protocol with id: " + dto.id());
        final RetroModel model =
            retroRepository.findById(UUID.fromString(dto.id())).orElseThrow(NotFoundException::new);

        final Map<String, Object> variables = Map.of("retro", model);
        final Resource pdfResource = pdfService.generatePdfResource("pdf/retro", variables, dto.isDraft());

        final String filename = "retro-" + model.getSprintName().toLowerCase().replaceAll("\\s+", "-") + ".pdf";
        final ContentDisposition contentDisposition =
            ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString()).body(pdfResource);
    }

    public ResponseEntity<Resource> downloadTicketAttachment(final RequestDTO dto) {
        URI downloadUri = URI.create(dto.webUrl());
        return fileDownloadService.downloadWithRestClient(downloadUri, "X-Redmine-API-Key", dto.apiKey());
    }
}
