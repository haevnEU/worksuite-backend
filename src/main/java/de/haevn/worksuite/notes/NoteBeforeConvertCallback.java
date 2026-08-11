package de.haevn.worksuite.notes;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Spring Data MongoDB callback lifecycle listener for {@link Note} entities.
 *
 * <p>Ensures that new note instances receive an initialized {@link UUID} and creation timestamp
 * prior to being serialized and converted into MongoDB BSON documents.
 *
 * <p>Example trigger:
 * <pre>{@code
 * Note note = new Note();
 * note.setTitle("New Note");
 * // Callback automatically generates UUID and timestamp before mongoRepository.save(note)
 * }</pre>
 */
@Component
public class NoteBeforeConvertCallback implements BeforeConvertCallback<Note> {

    /**
     * Lifecycle callback invoked before the {@link Note} entity is converted and saved into MongoDB.
     *
     * @param entity the note entity being prepared for persistence
     * @param collection target MongoDB collection name
     * @return the populated note entity
     */
    @Override
    @NonNull
    public Note onBeforeConvert(@NonNull final Note entity, @NonNull final String collection) {
        assignDefaultsIfMissing(entity);
        return entity;
    }

    /**
     * Sets default identifier and timestamp values if they have not been explicitly initialized.
     *
     * <p>Example usage:
     * <pre>{@code
     * assignDefaultsIfMissing(note);
     * }</pre>
     *
     * @param entity the target {@link Note}
     */
    private void assignDefaultsIfMissing(final Note entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
    }
}