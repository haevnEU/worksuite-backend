package de.haevn.worksuite.notes;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
class NoteBeforeConvertCallback implements BeforeConvertCallback<Note> {

    @Override
    public Note onBeforeConvert(final Note entity, final String collection) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }

        return entity;
    }
}