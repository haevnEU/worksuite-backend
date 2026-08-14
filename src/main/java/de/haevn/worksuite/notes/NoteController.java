package de.haevn.worksuite.notes;

import de.haevn.worksuite.common.RestApiController;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Log4j2
@RequiredArgsConstructor
@RestApiController("/api/v1/notes")
public class NoteController {

    private final NoteService noteService;

    @PostMapping()
    public NoteDTO shareText(@RequestBody final NoteDTO textShareDTO) {
        log.info("Sharing text");
        return noteService.create(textShareDTO);
    }

    @PutMapping("/{id}")
    public NoteDTO updateText(@PathVariable final UUID id, @RequestBody final NoteDTO textShareDTO) {
        log.info("Updating text");
        return noteService.update(id, textShareDTO);
    }

    @GetMapping()
    public List<NoteDTO> getAllTextShares() {
        log.info("Getting all text");
        return noteService.getAll();
    }

    @GetMapping("/{id}")
    public NoteDTO getText(@PathVariable final UUID id) {
        log.info("Getting text with id: {}", id);
        return noteService.getById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteText(@PathVariable final UUID id) {
        log.info("Deleting text with id: {}", id);
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
