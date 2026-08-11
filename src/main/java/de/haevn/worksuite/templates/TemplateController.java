package de.haevn.worksuite.templates;

import de.haevn.worksuite.common.RestApiController;
import java.util.List;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@RequiredArgsConstructor
@RestApiController("/api/v1/share/templates")
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public List<TemplateShareDTO> getAllTemplates(@RequestParam final Optional<String> tag) {
        log.info("Getting all templates");
        return templateService.getAllTemplates(tag);
    }

    @GetMapping("/{id}")
    public TemplateShareDTO getTemplate(@PathVariable final UUID id) {
        log.info("Getting template with id: {}", id);
        return templateService.getTemplate(id);
    }

    @PostMapping
    public TemplateShareDTO createTemplate(@RequestBody final TemplateShareDTO templateShareDTO) {
        log.info("Creating template");
        return templateService.shareTemplate(templateShareDTO);
    }

    @PutMapping("/{id}")
    public TemplateShareDTO updateTemplate(@PathVariable final UUID id,
        @RequestBody final TemplateShareDTO templateShareDTO) {
        log.info("Updating template with id: {}", id);
        return templateService.updateTemplate(id, templateShareDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable final UUID id) {
        log.info("Deleting template with id: {}", id);
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }
}
