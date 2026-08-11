package de.haevn.worksuite.templates;

import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.push.WebsocketPushService;
import de.haevn.worksuite.push.events.Priority;
import de.haevn.worksuite.push.events.WsEvent;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing template creation, updates, querying by tags, and WebSocket notifications.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private TemplateService templateService;
 *
 * TemplateShareDTO created = templateService.shareTemplate(
 *     new TemplateShareDTO(null, "PR Checklist", "- [ ] Tests passed", List.of("git"), "GitHub", null)
 * );
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class TemplateService {

    private static final String EVENT_TEMPLATE_CREATED = "Template created.";
    private static final String EVENT_TEMPLATE_UPDATED = "Template updated.";
    private static final String EVENT_TEMPLATE_DELETED = "Template deleted.";

    private final WebsocketPushService websocketPushService;
    private final TemplateShareRepository templateShareRepository;

    /**
     * Persists a new template and publishes a creation event.
     *
     * @param templateShareDTO template creation payload
     * @return the saved {@link TemplateShareDTO}
     */
    @Transactional
    public TemplateShareDTO shareTemplate(final TemplateShareDTO templateShareDTO) {
        Objects.requireNonNull(templateShareDTO, "TemplateShareDTO must not be null");

        final Template template = templateShareRepository.save(templateShareDTO.toModel());
        log.info("Created template with ID: '{}' and title: '{}'", template.getId(), template.getTitle());

        broadcastEvent(Priority.INFO, EVENT_TEMPLATE_CREATED);
        return TemplateShareDTO.fromModel(template);
    }

    /**
     * Retrieves a template by its identifier.
     *
     * @param id template unique identifier
     * @return the found {@link TemplateShareDTO}
     * @throws NotFoundException if the template does not exist
     */
    @Transactional(readOnly = true)
    public TemplateShareDTO getTemplate(final UUID id) {
        return TemplateShareDTO.fromModel(findTemplateEntity(id));
    }

    /**
     * Retrieves all templates, optionally filtered by a specific tag.
     *
     * @param tag optional tag filter
     * @return list of matching {@link TemplateShareDTO} records
     */
    @Transactional(readOnly = true)
    public List<TemplateShareDTO> getAllTemplates(final Optional<String> tag) {
        final List<Template> results = tag.filter(s -> !s.isBlank()).map(templateShareRepository::findByTagsContaining)
            .orElseGet(templateShareRepository::findAll);

        return results.stream().map(TemplateShareDTO::fromModel).toList();
    }

    /**
     * Updates an existing template.
     *
     * @param id template unique identifier
     * @param templateShareDTO updated template fields
     * @return the updated {@link TemplateShareDTO}
     * @throws NotFoundException if the template is not found
     */
    @Transactional
    public TemplateShareDTO updateTemplate(final UUID id, final TemplateShareDTO templateShareDTO) {
        Objects.requireNonNull(id, "Template ID must not be null");
        Objects.requireNonNull(templateShareDTO, "TemplateShareDTO must not be null");

        final Template template = findTemplateEntity(id);
        applyTemplateUpdates(template, templateShareDTO);

        final Template updated = templateShareRepository.save(template);
        log.info("Updated template with ID: '{}'", updated.getId());

        broadcastEvent(Priority.INFO, EVENT_TEMPLATE_UPDATED);
        return TemplateShareDTO.fromModel(updated);
    }

    /**
     * Deletes a template by its identifier.
     *
     * @param id template unique identifier
     * @throws NotFoundException if the template does not exist
     */
    @Transactional
    public void deleteTemplate(final UUID id) {
        final Template template = findTemplateEntity(id);
        templateShareRepository.delete(template);
        log.info("Deleted template with ID: '{}'", id);

        broadcastEvent(Priority.INFO, EVENT_TEMPLATE_DELETED);
    }

    /**
     * Resolves a persistent {@link Template} entity by ID or throws {@link NotFoundException}.
     *
     * <p>Example usage:
     * <pre>{@code
     * Template entity = findTemplateEntity(id);
     * }</pre>
     *
     * @param id target template identifier
     * @return the found {@link Template} entity
     * @throws NotFoundException if no template exists with the ID
     */
    private Template findTemplateEntity(final UUID id) {
        Objects.requireNonNull(id, "Template ID must not be null");
        return templateShareRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    /**
     * Copies field values from {@link TemplateShareDTO} to an existing {@link Template} entity.
     *
     * <p>Example usage:
     * <pre>{@code
     * applyTemplateUpdates(templateEntity, updateDto);
     * }</pre>
     *
     * @param target the target {@link Template} entity
     * @param source the source {@link TemplateShareDTO} payload
     */
    private void applyTemplateUpdates(final Template target, final TemplateShareDTO source) {
        target.setContent(source.content());
        target.setPlatform(source.platform());
        target.setTags(source.tags());
        target.setTitle(source.title());
    }

    /**
     * Helper method to broadcast a WebSocket event.
     *
     * <p>Example usage:
     * <pre>{@code
     * broadcastEvent(Priority.INFO, "Template created.");
     * }</pre>
     *
     * @param priority notification severity
     * @param message payload message
     */
    private void broadcastEvent(final Priority priority, final String message) {
        websocketPushService.dispatch(new WsEvent(getClass(), priority, message));
    }
}