package de.haevn.worksuite.retro;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Data transfer object representing a sprint retrospective.
 *
 * <p>Example usage:
 * <pre>{@code
 * RetroDTO dto = new RetroDTO(
 *     UUID.randomUUID(),
 *     "Sprint 24",
 *     List.of("High test coverage"),
 *     List.of("Deployment delays"),
 *     List.of("Automate database migrations"),
 *     LocalDateTime.now()
 * );
 * Retro entity = dto.toModel();
 * }</pre>
 *
 * @param id the unique retrospective identifier
 * @param sprintName the label or name of the sprint
 * @param positive list of positive retrospective feedback points
 * @param negative list of critical or negative feedback points
 * @param actionItems list of concrete action items defined during the retrospective
 * @param createdAt creation timestamp of the retrospective
 */
@Schema(description = "Data transfer object representing a sprint retrospective")
public record RetroDTO(

    @Schema(description = "Unique retrospective identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d") UUID id,

    @Schema(description = "Name or label of the sprint", example = "Sprint 24") String sprintName,

    @ArraySchema(schema = @Schema(description = "Positive feedback items",
        example = "Great team velocity")) List<String> positive,

    @ArraySchema(schema = @Schema(description = "Negative or improvement feedback items",
        example = "Too many meetings")) List<String> negative,

    @ArraySchema(schema = @Schema(description = "Action items to address in future sprints",
        example = "Shorten daily standups to 15m")) List<String> actionItems,

    @Schema(description = "Creation timestamp", example = "2026-08-17T18:00:00") LocalDateTime createdAt) {

    /**
     * Converts a {@link Retro} domain entity into a {@link RetroDTO}.
     *
     * <p>Example:
     * <pre>{@code
     * RetroDTO dto = RetroDTO.fromModel(retroEntity);
     * }</pre>
     *
     * @param retro the source {@link Retro} entity
     * @return the mapped {@link RetroDTO}
     */
    public static RetroDTO fromModel(final Retro retro) {
        Objects.requireNonNull(retro, "Retro entity must not be null");
        return new RetroDTO(retro.getId(), retro.getSprintName(),
            retro.getPositive() != null ? List.copyOf(retro.getPositive()) : List.of(),
            retro.getNegative() != null ? List.copyOf(retro.getNegative()) : List.of(),
            retro.getActionItems() != null ? List.copyOf(retro.getActionItems()) : List.of(), retro.getCreatedAt());
    }

    /**
     * Transforms this {@link RetroDTO} into a mutable persistent {@link Retro} entity.
     *
     * <p>Example:
     * <pre>{@code
     * Retro retro = dto.toModel();
     * }</pre>
     *
     * @return the populated {@link Retro} model
     */
    public Retro toModel() {
        return Retro.builder().id(id).sprintName(sprintName)
            .positive(positive != null ? new ArrayList<>(positive) : new ArrayList<>())
            .negative(negative != null ? new ArrayList<>(negative) : new ArrayList<>())
            .actionItems(actionItems != null ? new ArrayList<>(actionItems) : new ArrayList<>()).createdAt(createdAt)
            .build();
    }
}