package de.haevn.worksuite.time;

import de.haevn.worksuite.common.RestApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller exposing REST endpoints for inspecting recorded developer time entries.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * GET /api/v1/time-entries
 * GET /api/v1/time-entries?history=7
 * }</pre>
 */
@Log4j2
@Tag(name = "Time Tracking", description = "Endpoints for retrieving logged work hours and daily time entries")
@RestApiController("/api/v1/time-entries")
@RequiredArgsConstructor
public class TimeController {

    private final TimeService timeService;

    /**
     * Retrieves recorded time entries for today or across a specified past history window.
     *
     * @param history optional number of past days to query
     * @return list of matching {@link TimeDTO} records
     */
    @Operation(summary = "Get time entries",
        description = "Retrieves logged time entries for today, or over the specified history timeframe.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Time entries retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = TimeDTO.class))))})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TimeDTO> getTimeEntries(
        @Parameter(description = "Optional number of past days to include", example = "7")
        @RequestParam(name = "history", required = false) final Optional<Integer> history) {
        log.info("Request received to fetch time entries (history specified: {})", history.isPresent());

        final List<TimeEntry> entries = history.map(timeService::getAll).orElseGet(timeService::getForToday);

        return entries.stream().map(TimeDTO::new).toList();
    }
}