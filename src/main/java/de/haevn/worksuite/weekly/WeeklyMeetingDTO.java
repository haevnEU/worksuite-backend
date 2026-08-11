package de.haevn.worksuite.weekly;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Data transfer object representing a weekly meeting protocol.
 *
 * <p>Example usage:
 * <pre>{@code
 * WeeklyMeetingDTO dto = WeeklyMeetingDTO.fromModel(weeklyMeetingEntity);
 * }</pre>
 *
 * @param id primary unique identifier of the weekly meeting
 * @param title headline or date range of the sprint meeting
 * @param summary overall weekly retro/progress summary
 * @param daySummaries collection of daily breakdowns
 * @param createdAt creation timestamp
 */
@Schema(description = "Weekly sprint meeting protocol with daily summaries")
public record WeeklyMeetingDTO(

    @Schema(description = "Unique weekly meeting identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d") UUID id,

    @Schema(description = "Title of the weekly meeting",
        example = "Weekly Sprint (11.08.2026 - 18.08.2026)") String title,

    @Schema(description = "Overall sprint summary notes",
        example = "Sprint goals reached without blockers.") String summary,

    @ArraySchema(schema = @Schema(implementation = DaySummaryDTO.class)) List<DaySummaryDTO> daySummaries,

    @Schema(description = "Record creation instant", example = "2026-08-17T12:00:00.000Z") Instant createdAt) {

    /**
     * Converts a {@link WeeklyMeeting} domain entity to a {@link WeeklyMeetingDTO}.
     *
     * <p>Example:
     * <pre>{@code
     * WeeklyMeetingDTO dto = WeeklyMeetingDTO.fromModel(meetingEntity);
     * }</pre>
     *
     * @param weeklyMeeting the source {@link WeeklyMeeting}
     * @return the mapped {@link WeeklyMeetingDTO}, or {@code null} if source is null
     */
    public static WeeklyMeetingDTO fromModel(final WeeklyMeeting weeklyMeeting) {
        if (weeklyMeeting == null) {
            return null;
        }

        final List<DaySummaryDTO> daySummaryDTOs = (weeklyMeeting.getDaySummaries() == null) ?
            List.of() :
            weeklyMeeting.getDaySummaries().stream().map(DaySummaryDTO::fromModel).toList();

        return new WeeklyMeetingDTO(weeklyMeeting.getId(), weeklyMeeting.getTitle(), weeklyMeeting.getSummary(),
            daySummaryDTOs, weeklyMeeting.getCreatedAt());
    }

    /**
     * Converts this DTO into a {@link WeeklyMeeting} domain model.
     *
     * <p>Example:
     * <pre>{@code
     * WeeklyMeeting model = dto.toModel();
     * }</pre>
     *
     * @return the populated {@link WeeklyMeeting} model
     */
    public WeeklyMeeting toModel() {
        final WeeklyMeeting weeklyMeeting =
            WeeklyMeeting.builder().id(id).title(title).summary(summary).createdAt(createdAt).build();

        if (daySummaries != null) {
            daySummaries.stream().map(DaySummaryDTO::toModel).forEach(weeklyMeeting::addDaySummary);
        }

        return weeklyMeeting;
    }
}