package de.haevn.worksuite.templates;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Data transfer object representing a text template.
 *
 * <p>Example usage:
 * <pre>{@code
 * TemplateShareDTO dto = new TemplateShareDTO(
 *     UUID.randomUUID(),
 *     "Daily Standup Summary",
 *     "Yesterday: ...\nToday: ...\nBlockers: ...",
 *     List.of("meeting", "scrum"),
 *     "Slack",
 *     LocalDateTime.now()
 * );
 * Template entity = dto.toModel();
 * }</pre>
 *
 * @param id unique template identifier
 * @param title headline or title of the template
 * @param content raw text or markdown template body
 * @param tags categorization tags
 * @param platform target platform (e.g., GitLab, Jira, Slack, Email)
 * @param createdAt creation timestamp
 */
@Schema(description = "Data transfer object for creating and retrieving text templates")
public record TemplateShareDTO(

    @Schema(description = "Unique template identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d") UUID id,

    @Schema(description = "Title or description of the template", example = "Bug Report Template") String title,

    @Schema(description = "Text or markdown template body",
        example = "### Steps to Reproduce\n1. Go to...\n2. Click...") String content,

    @ArraySchema(schema = @Schema(description = "Categorization tags", example = "support")) List<String> tags,

    @Schema(description = "Target service or application platform", example = "GitLab") String platform,

    @Schema(description = "Creation timestamp", example = "2026-08-17T19:00:00") LocalDateTime createdAt) {

    /**
     * Converts a {@link Template} entity into its corresponding {@link TemplateShareDTO}.
     *
     * <p>Example:
     * <pre>{@code
     * TemplateShareDTO dto = TemplateShareDTO.fromModel(templateEntity);
     * }</pre>
     *
     * @param template the source {@link Template} entity
     * @return the transformed {@link TemplateShareDTO}
     */
    public static TemplateShareDTO fromModel(final Template template) {
        Objects.requireNonNull(template, "Template entity must not be null");
        return new TemplateShareDTO(template.getId(), template.getTitle(), template.getContent(),
            template.getTags() != null ? List.copyOf(template.getTags()) : List.of(), template.getPlatform(),
            template.getCreatedAt());
    }

    /**
     * Maps this DTO to a persistent {@link Template} entity.
     *
     * <p>Example:
     * <pre>{@code
     * Template entity = dto.toModel();
     * }</pre>
     *
     * @return the populated {@link Template} model
     */
    public Template toModel() {
        return Template.builder().id(id).title(title).content(content)
            .tags(tags != null ? new ArrayList<>(tags) : new ArrayList<>()).platform(platform).createdAt(createdAt)
            .build();
    }
}