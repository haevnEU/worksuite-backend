package de.haevn.worksuite.notes;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
public class NoteBeforeConvertCallback implements BeforeConvertCallback<NoteModel> {

    @Override
    public NoteModel onBeforeConvert(final NoteModel entity, final String collection) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }

        return entity;
    }
}