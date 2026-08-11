package de.haevn.worksuite.weekly;

import de.haevn.worksuite.common.RestApiController;
import de.haevn.worksuite.common.exceptions.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller exposing REST endpoints for managing weekly sprint protocols, daily summaries, and tasks.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * GET /api/v1/weekly-meetings
 * POST /api/v1/weekly-meetings/generate
 * POST /api/v1/weekly-meetings/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a/tasks?day=2026-08-17
 * PUT /api/v1/weekly-meetings/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a/day-summary?day=2026-08-17
 * }</pre>
 */
@Log4j2
@Tag(name = "Weekly Meetings", description = "Endpoints for managing weekly sprint meeting protocols and tasks")
@RestApiController("/api/v1/weekly-meetings")
@RequiredArgsConstructor
public class WeeklyMeetingController {

    private final WeeklyMeetingService weeklyMeetingService;

    /**
     * Retrieves all weekly meeting protocols.
     *
     * @return {@link ResponseEntity} holding a list of {@link WeeklyMeetingDTO} records
     */
    @Operation(summary = "Get all weekly meetings", description = "Retrieves an array of all sprint meeting protocols.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Weekly meetings retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = WeeklyMeetingDTO.class))))})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<WeeklyMeetingDTO>> getAll() {
        log.info("Request received to fetch all weekly meetings");
        return ResponseEntity.ok(weeklyMeetingService.getAll());
    }

    /**
     * Retrieves a single weekly meeting protocol by its identifier.
     *
     * @param id meeting unique identifier
     * @return {@link ResponseEntity} holding the matching {@link WeeklyMeetingDTO}
     */
    @Operation(summary = "Get weekly meeting by ID",
        description = "Retrieves a specific meeting protocol by its unique UUID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Meeting protocol found",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = WeeklyMeetingDTO.class))),
        @ApiResponse(responseCode = "404", description = "Meeting not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WeeklyMeetingDTO> getById(
        @Parameter(description = "Meeting unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable("id") final UUID id) {
        log.info("Request received to fetch weekly meeting with ID: '{}'", id);
        return ResponseEntity.ok(weeklyMeetingService.getById(id));
    }

    /**
     * Generates a new weekly sprint meeting protocol for the upcoming sprint week.
     *
     * @return empty response with HTTP 204 No Content
     */
    @Operation(summary = "Generate weekly meeting",
        description = "Generates a new weekly meeting protocol starting from current Tuesday.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Weekly meeting generated successfully"),
        @ApiResponse(responseCode = "400", description = "Meeting for the target date already exists",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> generateWeeklyMeeting() {
        log.info("Request received to manually trigger weekly meeting generation");
        weeklyMeetingService.generateNextWeek();
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds a task to a specific day summary within a meeting protocol.
     *
     * @param meetingId meeting unique identifier
     * @param day target calendar date (ISO format YYYY-MM-DD)
     * @param request payload holding the task text
     * @return empty response with HTTP 204 No Content
     */
    @Operation(summary = "Add task to day", description = "Appends a completed task item to the specified day summary.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Task added successfully"),
        @ApiResponse(responseCode = "404", description = "Meeting not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping("/{id}/tasks")
    public ResponseEntity<Void> addTaskToDay(
        @Parameter(description = "Meeting unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable("id") final UUID meetingId,
        @Parameter(description = "Target date (YYYY-MM-DD)", example = "2026-08-17") @RequestParam("day")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate day, @RequestBody final AddTaskRequest request) {
        log.info("Request received to add task to meeting ID '{}' for day '{}'", meetingId, day);
        weeklyMeetingService.addToMeeting(day, meetingId, request.task());
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the summary note for a specific day within a meeting protocol.
     *
     * @param meetingId meeting unique identifier
     * @param day target calendar date (ISO format YYYY-MM-DD)
     * @param request payload holding the updated summary
     * @return empty response with HTTP 204 No Content
     */
    @Operation(summary = "Update day summary", description = "Updates progress notes for a specific day.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Day summary updated successfully"),
        @ApiResponse(responseCode = "404", description = "Meeting not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PutMapping("/{id}/day-summary")
    public ResponseEntity<Void> updateDaySummary(
        @Parameter(description = "Meeting unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable("id") final UUID meetingId,
        @Parameter(description = "Target date (YYYY-MM-DD)", example = "2026-08-17") @RequestParam("day")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate day,
        @RequestBody final UpdateSummaryRequest request) {
        log.info("Request received to update day summary for meeting ID '{}' on day '{}'", meetingId, day);
        weeklyMeetingService.addDaySummary(day, meetingId, request.summary());
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates the overall weekly meeting summary.
     *
     * @param meetingId meeting unique identifier
     * @param request payload holding the overarching summary
     * @return empty response with HTTP 204 No Content
     */
    @Operation(summary = "Update weekly summary", description = "Updates the overall weekly meeting retro summary.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Weekly summary updated successfully"),
        @ApiResponse(responseCode = "404", description = "Meeting not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PutMapping("/{id}/summary")
    public ResponseEntity<Void> updateWeeklySummary(
        @Parameter(description = "Meeting unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable("id") final UUID meetingId, @RequestBody final UpdateSummaryRequest request) {
        log.info("Request received to update overall summary for meeting ID '{}'", meetingId);
        weeklyMeetingService.addSummary(meetingId, request.summary());
        return ResponseEntity.noContent().build();
    }

    /**
     * Request payload for adding a task to a day summary.
     *
     * @param task task description text
     */
    @Schema(description = "Payload for adding a task to a day summary")
    public record AddTaskRequest(
        @Schema(description = "Task description text", example = "Reviewed Merge Request #42") String task) {
    }


    /**
     * Request payload for updating summary text.
     *
     * @param summary updated summary text
     */
    @Schema(description = "Payload for updating summary text")
    public record UpdateSummaryRequest(@Schema(description = "Summary progress text",
        example = "Completed sprint goal and resolved all QA bugs.") String summary) {
    }
}