package de.haevn.worksuite.templates;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Spring Data MongoDB lifecycle listener for {@link Template} entities.
 *
 * <p>Ensures that new template instances receive an initialized {@link UUID}, creation timestamp,
 * and fallback default tags prior to BSON document conversion and persistence.
 *
 * <p>Example trigger:
 * <pre>{@code
 * Template template = new Template();
 * template.setTitle("MR Description Template");
 * // Callback automatically generates UUID, timestamp, and default tags before templateShareRepository.save(template)
 * }</pre>
 */
@Component
public class TemplateBeforeConvertCallback implements BeforeConvertCallback<Template> {

    private static final String DEFAULT_TAG = "Text";

    /**
     * Callback invoked before converting and persisting a {@link Template} entity into MongoDB.
     *
     * @param entity the template entity being prepared for persistence
     * @param collection target MongoDB collection name
     * @return the populated template entity
     */
    @Override
    @NonNull
    public Template onBeforeConvert(@NonNull final Template entity, @NonNull final String collection) {
        assignDefaultsIfMissing(entity);
        return entity;
    }

    /**
     * Sets default identifier, timestamp, and tag list values if they have not been initialized.
     *
     * <p>Example usage:
     * <pre>{@code
     * assignDefaultsIfMissing(template);
     * }</pre>
     *
     * @param entity the target {@link Template} to inspect and initialize
     */
    private void assignDefaultsIfMissing(final Template entity) {
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