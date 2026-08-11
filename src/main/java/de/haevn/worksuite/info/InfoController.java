package de.haevn.worksuite.info;

import de.haevn.redmine.api.RedmineException;
import de.haevn.redmine.model.RedmineInfoResponses;
import de.haevn.worksuite.common.RestApiController;
import de.haevn.worksuite.common.exceptions.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller exposing endpoints for inspecting workspace integration metadata and system dictionaries.
 *
 * <p>Example HTTP request:
 * <pre>{@code
 * GET /api/v1/info/redmine
 * }</pre>
 */
@Log4j2
@Tag(name = "System Info",
    description = "Endpoints for retrieving system settings and third-party integration metadata")
@RestApiController("/api/v1/info")
@RequiredArgsConstructor
public class InfoController {

    private final InfoService infoService;

    /**
     * Retrieves Redmine metadata dictionaries such as available statuses, activities, and priorities.
     *
     * @return dictionary map containing metadata collections
     * @throws RedmineException if fetching metadata from the Redmine backend fails
     */
    @Operation(summary = "Get Redmine metadata",
        description = "Fetches dictionaries of ticket activities, issue statuses, and priority levels from the configured Redmine service.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Redmine metadata retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(type = "object", additionalProperties = Schema.AdditionalPropertiesValue.TRUE))),
        @ApiResponse(responseCode = "502", description = "Failed to communicate with the remote Redmine API",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/redmine", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<RedmineInfoResponses.InfoResponse>> getRedmineInfo() throws RedmineException {
        log.info("Handling request to fetch Redmine metadata catalogues");
        return infoService.getRedmineInfo();
    }
}