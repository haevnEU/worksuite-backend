package de.haevn.worksuite.cheatsheet;

import de.haevn.worksuite.cheatsheet.CheatSheetModel.Level;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Request payload for creating or updating a cheat sheet item")
public record CheatSheetRequestDto(
    @NotBlank(message = "Category is required")
    @Schema(description = "Category group", example = "git", requiredMode = Schema.RequiredMode.REQUIRED)
    String category,

    @Schema(description = "Subcategory for fine-grained grouping", example = "branching")
    String subcategory,

    @NotBlank(message = "Title is required")
    @Schema(description = "Display title", example = "Create & switch branch", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @NotBlank(message = "Language is required")
    @Schema(description = "Language syntax", example = "BASH", requiredMode = Schema.RequiredMode.REQUIRED)
    String language,

    @NotNull(message = "Level is required")
    @Schema(description = "Difficulty level", example = "BASIC", requiredMode = Schema.RequiredMode.REQUIRED)
    Level level,

    @NotBlank(message = "Syntax is required")
    @Schema(description = "Command template or syntax", example = "git switch -c <branch-name>", requiredMode = Schema.RequiredMode.REQUIRED)
    String syntax,

    @NotBlank(message = "Explanation is required")
    @Schema(description = "Functional explanation", example = "Creates a new branch from current HEAD and switches to it directly.", requiredMode = Schema.RequiredMode.REQUIRED)
    String explanation,

    @Valid
    @Schema(description = "Supported options and flags")
    List<FlagItemRequestDto> flags,

    @Valid
    @Schema(description = "Practical examples")
    List<ExampleItemRequestDto> examples,

    @Schema(description = "Search and filter tags")
    List<String> tags,

    @Schema(description = "Indicates whether the command can cause irreversible changes", example = "false")
    boolean destructive,

    @Schema(description = "Warning message for risky operations")
    String warning,

    @Schema(description = "Documentation link")
    String docUrl
) {
    @Builder
    public record FlagItemRequestDto(
        @NotBlank(message = "Flag expression cannot be blank")
        @Schema(description = "Flag expression", example = "-d", requiredMode = Schema.RequiredMode.REQUIRED)
        String flag,

        @NotBlank(message = "Flag description cannot be blank")
        @Schema(description = "Flag explanation", example = "Deletes branch only if already fully merged", requiredMode = Schema.RequiredMode.REQUIRED)
        String description
    ) {}

    @Builder
    public record ExampleItemRequestDto(
        @NotBlank(message = "Example title cannot be blank")
        @Schema(description = "Example title", example = "Create feature branch", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,

        @NotBlank(message = "Example command cannot be blank")
        @Schema(description = "Concrete command snippet", example = "git switch -c feat/user-service-reauth", requiredMode = Schema.RequiredMode.REQUIRED)
        String command
    ) {}
}