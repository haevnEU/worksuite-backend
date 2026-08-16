package de.haevn.worksuite.snippets;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
public class SnippetBeforeConvertCallback implements BeforeConvertCallback<Snippet> {

    @Override
    public Snippet onBeforeConvert(Snippet entity, String collection) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }

        if (entity.getTags() == null) {
            entity.setTags(List.of("Text"));
        } else if (entity.getTags().isEmpty()) {
            entity.setTags(List.of("Text"));
        }

        return entity;
    }
}