package de.haevn.worksuite.retro;

import de.haevn.worksuite.common.RestApiController;
import de.haevn.worksuite.common.exceptions.BadRequestException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@RequiredArgsConstructor
@RestApiController("/api/v1/retros")
public class RetroController {

    private final RetroService retroService;

    @GetMapping
    public List<RetroDTO> getAllRetros() {
        log.info("Getting all retros");
        return retroService.getAllRetros();
    }

    @PostMapping
    public RetroDTO createRetro(@RequestParam final String name) {
        log.info("Creating retro");
        return retroService.createRetro(name);
    }

    @GetMapping("/{id}")
    public RetroDTO getRetro(@PathVariable final UUID id) {
        log.info("Getting retro with id: {}", id);
        return retroService.getRetroWeek(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRetro(@PathVariable final UUID id) {
        log.info("Deleting retro with id: {}", id);
        retroService.deleteRetro(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/{list}")
    public void addToPositiveList(@PathVariable final UUID id, @PathVariable final String list,
        @RequestBody final String item) {
        log.info("Adding to {} list with id: {}", list, id);
        switch (list.toLowerCase()) {
            case "positive" -> retroService.addToPositiveList(id, item);
            case "negative" -> retroService.addToNegativeList(id, item);
            case "action" -> retroService.addToActionItemList(id, item);
            default -> throw new BadRequestException("Invalid list type: " + list);
        }
    }

    @DeleteMapping("/{id}/{list}")
    public void removeFromList(@PathVariable final UUID id, @PathVariable final String list,
        @RequestBody final String item) {
        log.info("Removing item from {} list of retro with id: {}", list, id);
        switch (list.toLowerCase()) {
            case "positive" -> retroService.removeFromPositiveList(id, item);
            case "negative" -> retroService.removeFromNegativeList(id, item);
            case "action" -> retroService.removeFromActionItemList(id, item);
            default -> throw new BadRequestException("Invalid list type: " + list);
        }
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<Resource> exportPdf(@PathVariable final UUID id,
        @RequestHeader(value = "isDraft", defaultValue = "false") final boolean isDraft) {
        log.info("Exporting PDF retro with id: {}", id);
        return retroService.exportPdf(id, isDraft);
    }
}
