package de.haevn.worksuite.cheatsheet;

import de.haevn.worksuite.cheatsheet.CheatSheetModel.Level;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Response payload representing a cheat sheet item")
public record CheatSheetResponseDto(
    @Schema(description = "Unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "Category group", example = "git")
    String category,

    @Schema(description = "Subcategory for fine-grained grouping", example = "branching")
    String subcategory,

    @Schema(description = "Display title", example = "Create & switch branch")
    String title,

    @Schema(description = "Language syntax", example = "BASH")
    String language,

    @Schema(description = "Difficulty level", example = "BASIC")
    Level level,

    @Schema(description = "Command template or syntax", example = "git switch -c <branch-name>")
    String syntax,

    @Schema(description = "Functional explanation", example = "Creates a new branch from current HEAD and switches to it directly.")
    String explanation,

    @Schema(description = "Supported options and flags")
    List<FlagDto> flags,

    @Schema(description = "Concrete practical examples")
    List<ExampleDto> examples,

    @Schema(description = "Search and filter tags")
    List<String> tags,

    @Schema(description = "Destructive action flag", example = "false")
    boolean destructive,

    @Schema(description = "Optional warning message")
    String warning,

    @Schema(description = "Link to official documentation")
    String docUrl,

    @Schema(description = "Creation timestamp")
    LocalDateTime createdAt,

    @Schema(description = "Last modification timestamp")
    LocalDateTime updatedAt
) {
    @Builder
    public record FlagDto(
        @Schema(description = "Flag expression", example = "-d")
        String flag,

        @Schema(description = "Flag explanation", example = "Deletes branch only if already fully merged")
        String description
    ) {}

    @Builder
    public record ExampleDto(
        @Schema(description = "Example title", example = "Create feature branch")
        String title,

        @Schema(description = "Concrete command snippet", example = "git switch -c feat/user-service-reauth")
        String command
    ) {}
}