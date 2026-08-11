package de.haevn.worksuite.snippets;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

/**
 * Spring Data MongoDB callback lifecycle listener for {@link Snippet} entities.
 *
 * <p>Ensures that new snippet instances receive an initialized {@link UUID}, creation timestamp,
 * and default tags prior to BSON document conversion.
 *
 * <p>Example trigger:
 * <pre>{@code
 * Snippet snippet = new Snippet();
 * snippet.setTitle("Quick SQL snippet");
 * // Callback automatically generates UUID, timestamp, and default tags before saving
 * }</pre>
 */
@Component
public class SnippetBeforeConvertCallback implements BeforeConvertCallback<Snippet> {

    private static final String DEFAULT_TAG = "Text";

    /**
     * Callback invoked before converting and persisting a {@link Snippet} entity.
     *
     * @param entity the snippet entity being saved
     * @param collection target MongoDB collection name
     * @return the prepared snippet entity
     */
    @Override
    @NonNull
    public Snippet onBeforeConvert(@NonNull final Snippet entity, @NonNull final String collection) {
        assignDefaults(entity);
        return entity;
    }

    /**
     * Assigns default identifiers, timestamps, and fallback tags to a snippet if missing.
     *
     * <p>Example usage:
     * <pre>{@code
     * assignDefaults(snippet);
     * }</pre>
     *
     * @param entity the target {@link Snippet}
     */
    private void assignDefaults(final Snippet entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        if (entity.getTags() == null || entity.getTags().isEmpty()) {
            entity.setTags(new ArrayList<>(List.of(DEFAULT_TAG)));
        }
    }
}