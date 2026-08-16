package de.haevn.worksuite.retro;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
public class RetroBeforeConvertCallback implements BeforeConvertCallback<Retro> {

    @Override
    public Retro onBeforeConvert(final Retro entity, final String collection) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }

        if (entity.getPositive() == null) {
            entity.setPositive(List.of());
        }

        if (entity.getNegative() == null) {
            entity.setNegative(List.of());
        }

        if (entity.getActionItems() == null) {
            entity.setActionItems(List.of());
        }

        return entity;
    }
}