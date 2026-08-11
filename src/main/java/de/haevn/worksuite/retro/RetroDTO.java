package de.haevn.worksuite.retro;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RetroDTO(UUID id, String sprintName, List<String> positive, List<String> negative,
                       List<String> actionItems, LocalDateTime createdAt) {

    public static RetroDTO fromModel(final RetroModel retroModel) {
        return new RetroDTO(retroModel.getId(), retroModel.getSprintName(), retroModel.getPositive(),
            retroModel.getNegative(), retroModel.getActionItems(), retroModel.getCreatedAt());
    }

    public RetroModel toModel() {
        final RetroModel retroModel = new RetroModel();
        retroModel.setSprintName(sprintName);
        retroModel.setPositive(positive);
        retroModel.setNegative(negative);
        retroModel.setActionItems(actionItems);
        retroModel.setCreatedAt(createdAt);
        retroModel.setId(id);
        return retroModel;
    }
}
