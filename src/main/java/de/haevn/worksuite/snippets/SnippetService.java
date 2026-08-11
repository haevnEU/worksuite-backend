package de.haevn.worksuite.snippets;

import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.push.WebsocketPushService;
import de.haevn.worksuite.push.events.Priority;
import de.haevn.worksuite.push.events.WsEvent;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing snippet persistence, retrieval, modification, and WebSocket event dispatching.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private SnippetService snippetService;
 *
 * SnippetShareDTO created = snippetService.shareSnippet(
 *     new SnippetShareDTO(null, "Git log alias", "git log --oneline --graph", List.of("git"), "bash", null)
 * );
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class SnippetService {

    private static final String EVENT_SNIPPET_CREATED = "Snippet shared: %s";
    private static final String EVENT_SNIPPET_UPDATED = "Snippet updated: %s";
    private static final String EVENT_SNIPPET_DELETED = "Snippet deleted with ID: %s";

    private final WebsocketPushService websocketPushService;
    private final SnippetShareRepository snippetShareRepository;

    /**
     * Saves a new snippet and broadcasts a creation event.
     *
     * @param snippetShareDTO snippet creation payload
     * @return the saved {@link SnippetShareDTO}
     */
    @Transactional
    public SnippetShareDTO shareSnippet(final SnippetShareDTO snippetShareDTO) {
        Objects.requireNonNull(snippetShareDTO, "SnippetShareDTO must not be null");

        final Snippet snippet = snippetShareRepository.save(snippetShareDTO.toModel());
        log.info("Shared new snippet with ID: '{}' and title: '{}'", snippet.getId(), snippet.getTitle());

        broadcastEvent(Priority.INFO, EVENT_SNIPPET_CREATED.formatted(snippet.getId()));
        return SnippetShareDTO.fromModel(snippet);
    }

    /**
     * Retrieves a snippet by its unique identifier.
     *
     * @param id snippet unique identifier
     * @return the resolved {@link SnippetShareDTO}
     * @throws NotFoundException if the snippet does not exist
     */
    @Transactional(readOnly = true)
    public SnippetShareDTO getSnippet(final UUID id) {
        return SnippetShareDTO.fromModel(findSnippetEntity(id));
    }

    /**
     * Retrieves all saved snippets.
     *
     * @return list of all {@link SnippetShareDTO} records
     */
    @Transactional(readOnly = true)
    public List<SnippetShareDTO> getAllSnippets() {
        return snippetShareRepository.findAll().stream().map(SnippetShareDTO::fromModel).toList();
    }

    /**
     * Updates an existing snippet's content, language, tags, and title.
     *
     * @param id snippet unique identifier
     * @param snippetShareDTO updated snippet data
     * @return the updated {@link SnippetShareDTO}
     * @throws NotFoundException if the snippet is not found
     */
    @Transactional
    public SnippetShareDTO updateSnippet(final UUID id, final SnippetShareDTO snippetShareDTO) {
        Objects.requireNonNull(id, "Snippet ID must not be null");
        Objects.requireNonNull(snippetShareDTO, "SnippetShareDTO must not be null");

        final Snippet snippet = findSnippetEntity(id);
        applySnippetUpdates(snippet, snippetShareDTO);

        final Snippet updated = snippetShareRepository.save(snippet);
        log.info("Updated snippet with ID: '{}'", updated.getId());

        broadcastEvent(Priority.INFO, EVENT_SNIPPET_UPDATED.formatted(updated.getTitle()));
        return SnippetShareDTO.fromModel(updated);
    }

    /**
     * Deletes a snippet by its identifier and broadcasts a deletion event.
     *
     * @param id snippet unique identifier
     * @throws NotFoundException if the snippet does not exist
     */
    @Transactional
    public void deleteSnippet(final UUID id) {
        final Snippet snippet = findSnippetEntity(id);
        snippetShareRepository.delete(snippet);
        log.info("Deleted snippet with ID: '{}'", id);

        broadcastEvent(Priority.INFO, EVENT_SNIPPET_DELETED.formatted(id));
    }

    /**
     * Resolves a persistent {@link Snippet} entity by ID or throws {@link NotFoundException}.
     *
     * <p>Example usage:
     * <pre>{@code
     * Snippet entity = findSnippetEntity(id);
     * }</pre>
     *
     * @param id target snippet identifier
     * @return the found {@link Snippet} entity
     * @throws NotFoundException if no snippet exists with the ID
     */
    private Snippet findSnippetEntity(final UUID id) {
        Objects.requireNonNull(id, "Snippet ID must not be null");
        return snippetShareRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    /**
     * Applies field updates from {@link SnippetShareDTO} onto an existing {@link Snippet} model.
     *
     * <p>Example usage:
     * <pre>{@code
     * applySnippetUpdates(snippetEntity, updateDto);
     * }</pre>
     *
     * @param target the target {@link Snippet} entity
     * @param source the source {@link SnippetShareDTO} payload
     */
    private void applySnippetUpdates(final Snippet target, final SnippetShareDTO source) {
        target.setContent(source.content());
        target.setLanguage(source.language());
        target.setTags(source.tags());
        target.setTitle(source.title());
    }

    /**
     * Dispatches a real-time event through {@link WebsocketPushService}.
     *
     * <p>Example usage:
     * <pre>{@code
     * broadcastEvent(Priority.INFO, "Snippet shared");
     * }</pre>
     *
     * @param priority notification severity
     * @param message payload message
     */
    private void broadcastEvent(final Priority priority, final String message) {
        websocketPushService.dispatch(new WsEvent(getClass(), priority, message));
    }
}