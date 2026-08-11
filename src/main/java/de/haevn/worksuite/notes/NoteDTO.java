package de.haevn.worksuite.notes;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Data transfer object representing a note's external representation.
 *
 * <p>Example usage:
 * <pre>{@code
 * NoteDTO noteDTO = new NoteDTO(
 *     UUID.randomUUID(),
 *     "Architecture Guidelines",
 *     "Prefer JDBC DAO patterns over heavy ORM mapping.",
 *     "TICK-1024",
 *     LocalDateTime.now()
 * );
 * Note entity = noteDTO.toModel();
 * }</pre>
 *
 * @param id unique identifier of the note
 * @param title headline or summary of the note
 * @param content markdown or plain text note content
 * @param ticketId associated issue tracking ticket identifier (e.g. Redmine or Jira)
 * @param createdAt timestamp of note creation
 */
@Schema(description = "Data transfer object for creating, viewing, and updating notes")
public record NoteDTO(

    @Schema(description = "Unique note identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d") UUID id,

    @Schema(description = "Title of the note", example = "Spring Boot 3 Migration Notes") String title,

    @Schema(description = "Main content or markdown text of the note",
        example = "Remember to replace javax.* with jakarta.* packages across all modules.") String content,

    @Schema(description = "Optional issue tracker ticket ID reference", example = "TICK-8080") String ticketId,

    @Schema(description = "Date and time when the note was created",
        example = "2026-08-17T18:30:00") LocalDateTime createdAt) {

    /**
     * Converts a {@link Note} domain model entity into its corresponding {@link NoteDTO}.
     *
     * <p>Example:
     * <pre>{@code
     * NoteDTO dto = NoteDTO.fromModel(noteEntity);
     * }</pre>
     *
     * @param note the source {@link Note} entity
     * @return the transformed {@link NoteDTO}
     */
    public static NoteDTO fromModel(final Note note) {
        Objects.requireNonNull(note, "Note entity must not be null");
        return new NoteDTO(note.getId(), note.getTitle(), note.getContent(), note.getTicketId(), note.getCreatedAt());
    }

    /**
     * Maps this DTO to a persistent {@link Note} entity.
     *
     * <p>Example:
     * <pre>{@code
     * Note entity = noteDTO.toModel();
     * }</pre>
     *
     * @return the populated {@link Note} model
     */
    public Note toModel() {
        return Note.builder().id(id).title(title).content(content).ticketId(ticketId).createdAt(createdAt).build();
    }
}