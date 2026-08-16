package de.haevn.worksuite.retro;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RetroDTO(UUID id, String sprintName, List<String> positive, List<String> negative,
                       List<String> actionItems, LocalDateTime createdAt) {

    public static RetroDTO fromModel(final Retro retro) {
        return new RetroDTO(retro.getId(), retro.getSprintName(), retro.getPositive(),
            retro.getNegative(), retro.getActionItems(), retro.getCreatedAt());
    }

    public Retro toModel() {
        final Retro retro = new Retro();
        retro.setSprintName(sprintName);
        retro.setPositive(positive);
        retro.setNegative(negative);
        retro.setActionItems(actionItems);
        retro.setCreatedAt(createdAt);
        retro.setId(id);
        return retro;
    }
}
