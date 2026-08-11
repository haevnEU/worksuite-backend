package de.haevn.worksuite.snippets;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Data transfer object representing a code snippet.
 *
 * <p>Example usage:
 * <pre>{@code
 * SnippetShareDTO dto = new SnippetShareDTO(
 *     UUID.randomUUID(),
 *     "Spring Boot RestController",
 *     "@RestController\npublic class ApiController {}",
 *     List.of("java", "spring"),
 *     "java",
 *     LocalDateTime.now()
 * );
 * Snippet entity = dto.toModel();
 * }</pre>
 *
 * @param id unique snippet identifier
 * @param title headline or summary of the snippet
 * @param content the raw code or text content
 * @param tags categorization tags
 * @param language syntax highlighting language identifier (e.g., java, sql, bash)
 * @param createdAt creation timestamp
 */
@Schema(description = "Data transfer object for sharing and managing code snippets")
public record SnippetShareDTO(

    @Schema(description = "Unique snippet identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d") UUID id,

    @Schema(description = "Title or description of the snippet", example = "JWT Authentication Filter") String title,

    @Schema(description = "Code or text content of the snippet",
        example = "public class JwtFilter extends OncePerRequestFilter { ... }") String content,

    @ArraySchema(schema = @Schema(description = "Categorization tags", example = "java")) List<String> tags,

    @Schema(description = "Programming or markup language for syntax highlighting", example = "java") String language,

    @Schema(description = "Creation timestamp", example = "2026-08-17T18:50:00") LocalDateTime createdAt) {

    /**
     * Converts a {@link Snippet} model into its corresponding {@link SnippetShareDTO}.
     *
     * <p>Example:
     * <pre>{@code
     * SnippetShareDTO dto = SnippetShareDTO.fromModel(snippetEntity);
     * }</pre>
     *
     * @param snippet the source {@link Snippet} entity
     * @return the transformed {@link SnippetShareDTO}
     */
    public static SnippetShareDTO fromModel(final Snippet snippet) {
        Objects.requireNonNull(snippet, "Snippet entity must not be null");
        return new SnippetShareDTO(snippet.getId(), snippet.getTitle(), snippet.getContent(),
            snippet.getTags() != null ? List.copyOf(snippet.getTags()) : List.of(), snippet.getLanguage(),
            snippet.getCreatedAt());
    }

    /**
     * Maps this DTO to a persistent {@link Snippet} entity.
     *
     * <p>Example:
     * <pre>{@code
     * Snippet entity = dto.toModel();
     * }</pre>
     *
     * @return the populated {@link Snippet} model
     */
    public Snippet toModel() {
        return Snippet.builder().id(id).title(title).content(content)
            .tags(tags != null ? new ArrayList<>(tags) : new ArrayList<>()).language(language).createdAt(createdAt)
            .build();
    }
}