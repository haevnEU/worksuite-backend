package de.haevn.worksuite.templates;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TemplateShareDTO(UUID id, String title, String content, List<String> tags, String platform,
                               LocalDateTime createdAt) {

    public static TemplateShareDTO fromModel(final TemplateModel templateModel) {
        return new TemplateShareDTO(templateModel.getId(), templateModel.getTitle(), templateModel.getContent(),
            templateModel.getTags(), templateModel.getPlatform(), templateModel.getCreatedAt());
    }

    public TemplateModel toModel() {
        final TemplateModel templateModel = new TemplateModel();
        templateModel.setTags(tags);
        templateModel.setContent(content);
        templateModel.setTitle(title);
        templateModel.setPlatform(platform);
        templateModel.setCreatedAt(createdAt);
        templateModel.setId(id);
        return templateModel;
    }
}
