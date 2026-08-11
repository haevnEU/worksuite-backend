package de.haevn.worksuite.retro;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Spring Data MongoDB lifecycle callback ensuring proper initialization of {@link Retro} document fields.
 *
 * <p>Ensures that new instances obtain an initialized {@link UUID}, creation timestamp, and non-null lists
 * prior to BSON serialization.
 *
 * <p>Example trigger:
 * <pre>{@code
 * Retro retro = new Retro();
 * retro.setSprintName("Sprint 12");
 * // Callback automatically generates UUID, timestamp, and empty collections before mongoRepository.save(retro)
 * }</pre>
 */
@Component
public class RetroBeforeConvertCallback implements BeforeConvertCallback<Retro> {

    /**
     * Invoked prior to entity conversion into a MongoDB document.
     *
     * @param entity the {@link Retro} entity being saved
     * @param collection target collection name
     * @return the prepared {@link Retro} entity
     */
    @Override
    @NonNull
    public Retro onBeforeConvert(@NonNull final Retro entity, @NonNull final String collection) {
        initializeDefaults(entity);
        return entity;
    }

    /**
     * Initializes default values for missing identifiers, timestamps, and feedback collections.
     *
     * <p>Example usage:
     * <pre>{@code
     * initializeDefaults(retro);
     * }</pre>
     *
     * @param entity the target {@link Retro} entity
     */
    private void initializeDefaults(final Retro entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        if (entity.getPositive() == null) {
            entity.setPositive(new ArrayList<>());
        }
        if (entity.getNegative() == null) {
            entity.setNegative(new ArrayList<>());
        }
        if (entity.getActionItems() == null) {
            entity.setActionItems(new ArrayList<>());
        }
    }
}