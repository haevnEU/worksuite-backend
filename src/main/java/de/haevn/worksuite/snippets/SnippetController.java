package de.haevn.worksuite.snippets;

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
@RestApiController("/api/v1/snippets")
public class SnippetController {

    private final SnippetService snippetService;

    @GetMapping
    public List<SnippetShareDTO> getAllSnippets() {
        log.info("Getting all snippets");
        return snippetService.getAllSnippets();
    }

    @PostMapping
    public SnippetShareDTO createSnippet(@RequestBody final SnippetShareDTO snippetShareDTO) {
        log.info("Creating snippet");
        return snippetService.shareSnippet(snippetShareDTO);
    }

    @GetMapping("/{id}")
    public SnippetShareDTO getSnippet(@PathVariable final UUID id) {
        log.info("Getting snippet with id: {}", id);
        return snippetService.getSnippet(id);
    }

    @PutMapping("/{id}")
    public SnippetShareDTO updateSnippet(@PathVariable final UUID id,
        @RequestBody final SnippetShareDTO snippetShareDTO) {
        log.info("Updating snippet with id: {}", id);
        return snippetService.updateSnippet(id, snippetShareDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSnippet(@PathVariable final UUID id) {
        log.info("Deleting snippet with id: {}", id);
        snippetService.deleteSnippet(id);
        return ResponseEntity.noContent().build();
    }

}
