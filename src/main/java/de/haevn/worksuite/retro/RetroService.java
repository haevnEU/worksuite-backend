package de.haevn.worksuite.retro;

import de.haevn.worksuite.common.exceptions.NotFoundException;
import de.haevn.worksuite.push.WebsocketPushService;
import de.haevn.worksuite.push.events.Priority;
import de.haevn.worksuite.push.events.WsEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing sprint retrospective lifecycles, item list modifications, and WebSocket notifications.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private RetroService retroService;
 *
 * RetroDTO retro = retroService.createRetro("Sprint 25");
 * retroService.addToPositiveList(retro.id(), "Completed all stories on time");
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class RetroService {

    private static final String EVENT_RETRO_CREATED = "Retro created: %s";
    private static final String EVENT_RETRO_DELETED = "Retro deleted with ID: %s";
    private static final String EVENT_RETRO_UPDATED = "Retro list updated for: %s";

    private final WebsocketPushService websocketPushService;
    private final RetroRepository retroRepository;

    /**
     * Creates a new retrospective for a sprint and broadcasts a creation event.
     *
     * @param sprintName the name of the sprint
     * @return the created {@link RetroDTO}
     */
    @Transactional
    public RetroDTO createRetro(final String sprintName) {
        Objects.requireNonNull(sprintName, "Sprint name must not be null");

        final Retro retro =
            Retro.builder().sprintName(sprintName).positive(new ArrayList<>()).negative(new ArrayList<>())
                .actionItems(new ArrayList<>()).build();

        final Retro saved = retroRepository.save(retro);
        log.info("Created retro with ID: '{}' for sprint: '{}'", saved.getId(), sprintName);

        broadcastEvent(Priority.INFO, EVENT_RETRO_CREATED.formatted(sprintName));
        return RetroDTO.fromModel(saved);
    }

    /**
     * Retrieves a retrospective by its unique identifier.
     *
     * @param id target retrospective ID
     * @return the corresponding {@link RetroDTO}
     * @throws NotFoundException if no retrospective matches the provided ID
     */
    @Transactional(readOnly = true)
    public RetroDTO getRetroWeek(final UUID id) {
        return RetroDTO.fromModel(findRetroEntity(id));
    }

    /**
     * Retrieves all existing retrospectives.
     *
     * @return a list of all {@link RetroDTO} records
     */
    @Transactional(readOnly = true)
    public List<RetroDTO> getAllRetros() {
        return retroRepository.findAll().stream().map(RetroDTO::fromModel).toList();
    }

    /**
     * Adds an item to the positive feedback list if it is not already present.
     *
     * @param retroId target retrospective ID
     * @param item item text to append
     */
    @Transactional
    public void addToPositiveList(final UUID retroId, final String item) {
        final Retro retro = findRetroEntity(retroId);
        if (appendDistinct(retro.getPositive(), item)) {
            retroRepository.save(retro);
            broadcastEvent(Priority.INFO, EVENT_RETRO_UPDATED.formatted(retro.getSprintName()));
        }
    }

    /**
     * Adds an item to the negative feedback list if it is not already present.
     *
     * @param retroId target retrospective ID
     * @param item item text to append
     */
    @Transactional
    public void addToNegativeList(final UUID retroId, final String item) {
        final Retro retro = findRetroEntity(retroId);
        if (appendDistinct(retro.getNegative(), item)) {
            retroRepository.save(retro);
            broadcastEvent(Priority.INFO, EVENT_RETRO_UPDATED.formatted(retro.getSprintName()));
        }
    }

    /**
     * Adds an item to the action items list if it is not already present.
     *
     * @param retroId target retrospective ID
     * @param item item text to append
     */
    @Transactional
    public void addToActionItemList(final UUID retroId, final String item) {
        final Retro retro = findRetroEntity(retroId);
        if (appendDistinct(retro.getActionItems(), item)) {
            retroRepository.save(retro);
            broadcastEvent(Priority.INFO, EVENT_RETRO_UPDATED.formatted(retro.getSprintName()));
        }
    }

    /**
     * Removes an item from the positive feedback list.
     *
     * @param retroId target retrospective ID
     * @param item item text to remove
     */
    @Transactional
    public void removeFromPositiveList(final UUID retroId, final String item) {
        final Retro retro = findRetroEntity(retroId);
        if (retro.getPositive().remove(item)) {
            retroRepository.save(retro);
            broadcastEvent(Priority.INFO, EVENT_RETRO_UPDATED.formatted(retro.getSprintName()));
        }
    }

    /**
     * Removes an item from the negative feedback list.
     *
     * @param retroId target retrospective ID
     * @param item item text to remove
     */
    @Transactional
    public void removeFromNegativeList(final UUID retroId, final String item) {
        final Retro retro = findRetroEntity(retroId);
        if (retro.getNegative().remove(item)) {
            retroRepository.save(retro);
            broadcastEvent(Priority.INFO, EVENT_RETRO_UPDATED.formatted(retro.getSprintName()));
        }
    }

    /**
     * Removes an item from the action items list.
     *
     * @param retroId target retrospective ID
     * @param item item text to remove
     */
    @Transactional
    public void removeFromActionItemList(final UUID retroId, final String item) {
        final Retro retro = findRetroEntity(retroId);
        if (retro.getActionItems().remove(item)) {
            retroRepository.save(retro);
            broadcastEvent(Priority.INFO, EVENT_RETRO_UPDATED.formatted(retro.getSprintName()));
        }
    }

    /**
     * Deletes a retrospective by ID and emits a deletion event.
     *
     * @param retroId target retrospective ID
     */
    @Transactional
    public void deleteRetro(final UUID retroId) {
        Objects.requireNonNull(retroId, "Retro ID must not be null");

        if (!retroRepository.existsById(retroId)) {
            throw new NotFoundException();
        }

        retroRepository.deleteById(retroId);
        log.info("Deleted retro with ID: '{}'", retroId);
        broadcastEvent(Priority.INFO, EVENT_RETRO_DELETED.formatted(retroId));
    }

    /**
     * Finds and returns a persistent {@link Retro} entity or throws a {@link NotFoundException}.
     *
     * <p>Example usage:
     * <pre>{@code
     * Retro entity = findRetroEntity(id);
     * }</pre>
     *
     * @param id retrospective unique identifier
     * @return the resolved {@link Retro} entity
     * @throws NotFoundException if the retrospective cannot be found
     */
    private Retro findRetroEntity(final UUID id) {
        Objects.requireNonNull(id, "Retro ID must not be null");
        return retroRepository.findById(id).orElseThrow(NotFoundException::new);
    }

    /**
     * Adds an item to the target collection only if it is not already contained.
     *
     * <p>Example usage:
     * <pre>{@code
     * boolean modified = appendDistinct(retro.getPositive(), "Great velocity");
     * }</pre>
     *
     * @param list the collection to modify
     * @param item the string element to add
     * @return {@code true} if the item was added, {@code false} if already present
     */
    private boolean appendDistinct(final List<String> list, final String item) {
        if (item == null || item.isBlank() || list.contains(item)) {
            return false;
        }
        return list.add(item);
    }

    /**
     * Dispatches a {@link WsEvent} through {@link WebsocketPushService}.
     *
     * <p>Example usage:
     * <pre>{@code
     * broadcastEvent(Priority.INFO, "Retro created: Sprint 24");
     * }</pre>
     *
     * @param priority severity of the notification
     * @param message payload message
     */
    private void broadcastEvent(final Priority priority, final String message) {
        websocketPushService.dispatch(new WsEvent(getClass(), priority, message));
    }
}