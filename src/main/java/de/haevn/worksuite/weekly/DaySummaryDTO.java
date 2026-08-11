package de.haevn.worksuite.weekly;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data transfer object representing a daily summary and task list.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * DaySummaryDTO dto = new DaySummaryDTO(
 *     UUID.randomUUID(),
 *     Instant.now(),
 *     "Worked on JWT verification and database indexing.",
 *     List.of("Merge Request #102", "Deploy staging"),
 *     Instant.now()
 * );
 * }</pre>
 *
 * @param id primary unique identifier of the day summary
 * @param date target work date instant
 * @param summary detailed textual summary of the day's progress
 * @param tasks list of task descriptions completed or tracked
 * @param createdAt creation timestamp
 */
@Schema(description = "Daily summary and task list representation")
public record DaySummaryDTO(

    @Schema(description = "Unique day summary identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d") UUID id,

    @Schema(description = "Date timestamp for this day", example = "2026-08-17T00:00:00.000Z") Instant date,

    @Schema(description = "Textual summary of achievements and progress",
        example = "Finished refactoring API error response envelopes.") String summary,

    @ArraySchema(schema = @Schema(description = "Tasks completed during the day",
        example = "Refactor GlobalExceptionHandler")) List<String> tasks,

    @Schema(description = "Creation instant", example = "2026-08-17T18:00:00.000Z") Instant createdAt) {

    /**
     * Maps a {@link DaySummary} entity to its corresponding {@link DaySummaryDTO}.
     *
     * <p>Example:
     * <pre>{@code
     * DaySummaryDTO dto = DaySummaryDTO.fromModel(daySummaryEntity);
     * }</pre>
     *
     * @param model the source {@link DaySummary} entity
     * @return the populated {@link DaySummaryDTO}, or {@code null} if model is null
     */
    public static DaySummaryDTO fromModel(final DaySummary model) {
        if (model == null) {
            return null;
        }
        return new DaySummaryDTO(model.getId(), model.getDate(), model.getSummary(),
            model.getTasks() != null ? List.copyOf(model.getTasks()) : List.of(), model.getCreatedAt());
    }

    /**
     * Converts this DTO into a mutable persistent {@link DaySummary} entity.
     *
     * <p>Example:
     * <pre>{@code
     * DaySummary entity = dto.toModel();
     * }</pre>
     *
     * @return the populated {@link DaySummary} entity
     */
    public DaySummary toModel() {
        return DaySummary.builder().id(id).date(date).summary(summary)
            .tasks(tasks != null ? new ArrayList<>(tasks) : new ArrayList<>()).createdAt(createdAt).build();
    }
}