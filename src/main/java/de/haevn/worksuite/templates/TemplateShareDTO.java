package de.haevn.worksuite.templates;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TemplateShareDTO(UUID id, String title, String content, List<String> tags, String platform,
                               LocalDateTime createdAt) {

    public static TemplateShareDTO fromModel(final Template template) {
        return new TemplateShareDTO(template.getId(), template.getTitle(), template.getContent(),
            template.getTags(), template.getPlatform(), template.getCreatedAt());
    }

    public Template toModel() {
        final Template template = new Template();
        template.setTags(tags);
        template.setContent(content);
        template.setTitle(title);
        template.setPlatform(platform);
        template.setCreatedAt(createdAt);
        template.setId(id);
        return template;
    }
}
