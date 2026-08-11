package de.haevn.worksuite.stats;

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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller exposing REST endpoints for managing and inspecting daily developer statistics.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * POST /api/v1/stats
 * GET /api/v1/stats?duration=7
 * GET /api/v1/stats/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a
 * PUT /api/v1/stats/c0a80101-8b9a-4e2b-9e9b-9c7f6d4e5f6a?stat=MOVED_TO_QA&amount=1
 * }</pre>
 */
@Log4j2
@Tag(name = "Developer Stats", description = "Endpoints for recording workflow metrics and tracking logged hours")
@RestApiController("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * Creates a new statistics record for a designated or current date.
     *
     * @param date optional target timestamp
     * @return the unique identifier of the created record
     */
    @Operation(summary = "Create stats record", description = "Initializes a new daily statistics record.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Stats record initialized successfully",
        content = @Content(
            schema = @Schema(type = "string", format = "uuid", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")))})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UUID createNewRecord(
        @Parameter(description = "Optional specific date timestamp", example = "2026-08-17T00:00:00.000Z") @RequestParam
        final Optional<Instant> date) {
        log.info("Request received to create stats record with explicit date present: {}", date.isPresent());
        return date.map(statsService::createNewRecord).orElseGet(statsService::createNewRecord);
    }

    /**
     * Retrieves a single statistics record by its unique ID.
     *
     * @param id statistics record identifier
     * @return the resolved {@link Stats} entity
     */
    @Operation(summary = "Get stats by ID", description = "Retrieves workflow statistics for a specific record.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Stats record found",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Stats.class))),
        @ApiResponse(responseCode = "404", description = "Stats record not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Stats getRecord(
        @Parameter(description = "Statistics unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id) {
        log.info("Request received to fetch stats record ID: '{}'", id);
        return statsService.findStatsModelById(id);
    }

    /**
     * Retrieves statistics across the specified duration in days.
     *
     * @param duration number of days to query (defaults to 7)
     * @return list of {@link Stats} records
     */
    @Operation(summary = "Get stats list",
        description = "Retrieves statistics records covering the requested number of past days.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Stats list retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = Stats.class))))})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Stats> getRecords(
        @Parameter(description = "Duration in days to query", example = "7") @RequestParam(defaultValue = "7")
        final int duration) {
        log.info("Request received to fetch stats for duration: {} days", duration);
        return statsService.findAllStatsModels(duration);
    }

    /**
     * Increments a specific developer metric counter on a record.
     *
     * @param id statistics record identifier
     * @param stat metric category to increment
     * @param amount increment step value (defaults to 1)
     */
    @Operation(summary = "Increment stat metric",
        description = "Increments a metric counter such as MOVED_TO_QA or MOVED_TO_REVIEW.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Metric incremented successfully"),
        @ApiResponse(responseCode = "404", description = "Stats record not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PutMapping("/{id}")
    public void incrementStats(
        @Parameter(description = "Statistics unique identifier", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        @PathVariable final UUID id,
        @Parameter(description = "Metric category to increment", example = "MOVED_TO_QA") @RequestParam final Stat stat,
        @Parameter(description = "Amount to add", example = "1") @RequestParam(defaultValue = "1") final int amount) {
        log.info("Request received to increment stat '{}' on record '{}' by {}", stat, id, amount);
        statsService.incrementStat(id, stat, amount);
    }
}