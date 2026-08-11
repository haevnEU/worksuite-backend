package de.haevn.worksuite.cheatsheet;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cheatsheet")
@Schema(description = "Represents a developer cheat sheet entry")
public class CheatSheetModel {

    @Id
    @Schema(description = "Unique identifier of the cheat sheet item", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Indexed
    @Schema(description = "Category group (e.g. git, docker, postgres, linux)", example = "git")
    private String category;

    @Indexed
    @Schema(description = "Subcategory for fine-grained grouping", example = "branching")
    private String subcategory;

    @Schema(description = "Display title of the cheat sheet", example = "Create & switch branch")
    private String title;

    @Schema(description = "Target language or shell syntax", example = "BASH")
    private String language;

    @Schema(description = "Difficulty or scope level", example = "BASIC")
    private Level level;

    @Schema(description = "Command template or syntax", example = "git switch -c <branch-name>")
    private String syntax;

    @Schema(description = "Functional explanation of the command", example = "Creates a new branch from current HEAD and switches to it directly.")
    private String explanation;

    @Builder.Default
    @Schema(description = "Supported command options and flags")
    private List<FlagItem> flags = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Concrete practical execution examples")
    private List<ExampleItem> examples = new ArrayList<>();

    @Builder.Default
    @Schema(description = "Search and filter tags")
    private List<String> tags = new ArrayList<>();

    @Schema(description = "Indicates whether the command can cause irreversible changes/loss", example = "false")
    private boolean destructive;

    @Schema(description = "Warning message for destructive or tricky commands", example = "Force-deleting unmerged branches discards commits permanently.")
    private String warning;

    @Schema(description = "Link to official documentation", example = "https://git-scm.com/docs/git-switch")
    private String docUrl;

    @CreatedDate
    @Schema(description = "Timestamp when the document was initially created")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Schema(description = "Timestamp when the document was last updated")
    private LocalDateTime updatedAt;

    public enum Level {
        BASIC,
        INTERMEDIATE,
        ADVANCED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Command flag specification")
    public static class FlagItem {
        @Schema(description = "Flag expression", example = "-d")
        private String flag;

        @Schema(description = "Flag behavior explanation", example = "Deletes branch only if already fully merged")
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Practical command example")
    public static class ExampleItem {
        @Schema(description = "Context or title of the example", example = "Create feature branch")
        private String title;

        @Schema(description = "Concrete runnable command snippet", example = "git switch -c feat/user-service-reauth")
        private String command;
    }
}