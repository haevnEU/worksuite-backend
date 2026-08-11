package de.haevn.worksuite.notes;

import de.haevn.worksuite.common.PdfService;
import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.push.WebsocketPushService;
import de.haevn.worksuite.push.events.Priority;
import de.haevn.worksuite.push.events.WsEvent;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
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

@Service
@RequiredArgsConstructor
@Log4j2
public class NoteService {

    private final WebsocketPushService  websocketPushService;
    private final NoteRepository noteRepository;
    private final PdfService pdfService;

    @Transactional
    public NoteDTO create(final NoteDTO noteDTO) {
        final NoteModel noteModel = noteRepository.save(noteDTO.toModel());
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "Note created: " + noteModel.getTitle()));
        return NoteDTO.fromModel(noteModel);
    }

    @Transactional
    public NoteDTO getById(final UUID id) {
        final NoteModel model = noteRepository.findById(id).orElseThrow(NotFoundException::new);
        return NoteDTO.fromModel(model);
    }

    @Transactional
    public List<NoteDTO> getAll() {
        return noteRepository.findAll().stream().map(NoteDTO::fromModel).toList();
    }

    @Transactional
    public NoteDTO update(final UUID id, final NoteDTO noteDTO) {
        final NoteModel model = noteRepository.findById(id).orElseThrow(NotFoundException::new);
        model.setContent(noteDTO.content());
        model.setTicketId(noteDTO.ticketId());
        model.setTitle(noteDTO.title());
        noteRepository.save(model);
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "Note updated: " + model.getTitle()));
        return NoteDTO.fromModel(model);
    }

    @Transactional
    public void delete(final UUID id) {
        final NoteModel model = noteRepository.findById(id).orElseThrow(NotFoundException::new);
        noteRepository.delete(model);
        websocketPushService.dispatch(new WsEvent(this.getClass(), Priority.INFO, "Note deleted: " + model.getTitle()));
    }

    @Transactional
    public ResponseEntity<Resource> exportPdf(final UUID id, final boolean isDraft) {
        final NoteModel model = noteRepository.findById(id).orElseThrow(NotFoundException::new);

        final Map<String, Object> variables = Map.of("note", model);
        final Resource pdfResource = pdfService.generatePdfResource("pdf/note", variables, isDraft);

        final String filename = "note-" + model.getTitle() + ".pdf";
        final ContentDisposition contentDisposition =
            ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString()).body(pdfResource);
    }
}
