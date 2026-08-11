package de.haevn.worksuite.notes;

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
 * Service orchestrating CRUD operations and WebSocket event dispatching for {@link Note} entities.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private NoteService noteService;
 *
 * NoteDTO created = noteService.create(new NoteDTO(null, "Refactoring", "Clean code", "TICK-1", null));
 * }</pre>
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class NoteService {

    private static final String EVENT_NOTE_CREATED = "Note created: %s";
    private static final String EVENT_NOTE_UPDATED = "Note updated: %s";
    private static final String EVENT_NOTE_DELETED = "Note deleted: %s";

    private final WebsocketPushService websocketPushService;
    private final NoteRepository noteRepository;

    /**
     * Persists a new note and publishes a real-time creation event via WebSocket.
     *
     * @param noteDTO note creation payload
     * @return the persisted {@link NoteDTO}
     */
    @Transactional
    public NoteDTO create(final NoteDTO noteDTO) {
        Objects.requireNonNull(noteDTO, "NoteDTO must not be null");

        final Note saved = noteRepository.save(noteDTO.toModel());
        log.info("Created note with ID: '{}' and title: '{}'", saved.getId(), saved.getTitle());

        broadcastEvent(Priority.INFO, EVENT_NOTE_CREATED.formatted(saved.getTitle()));
        return NoteDTO.fromModel(saved);
    }

    /**
     * Finds a note by its unique {@link UUID}.
     *
     * @param id target note identifier
     * @return the resolved {@link NoteDTO}
     * @throws NotFoundException if no note exists with the provided identifier
     */
    @Transactional(readOnly = true)
    public NoteDTO getById(final UUID id) {
        Objects.requireNonNull(id, "Note ID must not be null");
        return noteRepository.findById(id).map(NoteDTO::fromModel).orElseThrow(NotFoundException::new);
    }

    /**
     * Retrieves all existing notes.
     *
     * @return a list containing all stored {@link NoteDTO} records
     */
    @Transactional(readOnly = true)
    public List<NoteDTO> getAll() {
        return noteRepository.findAll().stream().map(NoteDTO::fromModel).toList();
    }

    /**
     * Updates an existing note and publishes an update event.
     *
     * @param id the identifier of the note to update
     * @param noteDTO the updated note fields
     * @return the updated {@link NoteDTO}
     * @throws NotFoundException if the note is not found
     */
    @Transactional
    public NoteDTO update(final UUID id, final NoteDTO noteDTO) {
        Objects.requireNonNull(id, "Note ID must not be null");
        Objects.requireNonNull(noteDTO, "NoteDTO must not be null");

        final Note existing = noteRepository.findById(id).orElseThrow(NotFoundException::new);
        existing.setTitle(noteDTO.title());
        existing.setContent(noteDTO.content());
        existing.setTicketId(noteDTO.ticketId());

        final Note updated = noteRepository.save(existing);
        log.info("Updated note with ID: '{}'", updated.getId());

        broadcastEvent(Priority.INFO, EVENT_NOTE_UPDATED.formatted(updated.getTitle()));
        return NoteDTO.fromModel(updated);
    }

    /**
     * Deletes a note by its identifier and broadcasts a deletion event.
     *
     * @param id the identifier of the note to remove
     * @throws NotFoundException if the note does not exist
     */
    @Transactional
    public void delete(final UUID id) {
        Objects.requireNonNull(id, "Note ID must not be null");

        final Note note = noteRepository.findById(id).orElseThrow(NotFoundException::new);
        noteRepository.delete(note);
        log.info("Deleted note with ID: '{}'", id);

        broadcastEvent(Priority.INFO, EVENT_NOTE_DELETED.formatted(note.getTitle()));
    }

    /**
     * Dispatches a {@link WsEvent} through the injected {@link WebsocketPushService}.
     *
     * <p>Example usage:
     * <pre>{@code
     * broadcastEvent(Priority.INFO, "Note updated: Release Plan");
     * }</pre>
     *
     * @param priority severity level of the event
     * @param message notification text
     */
    private void broadcastEvent(final Priority priority, final String message) {
        websocketPushService.dispatch(new WsEvent(getClass(), priority, message));
    }
}