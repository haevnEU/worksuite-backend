package de.haevn.worksuite.database;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * REST controller exposing endpoints for querying database table records and schema discovery.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * GET /api/v1/database/tables
 * GET /api/v1/database?searchParam=key&value=ACTIVE
 * GET /api/v1/database/tables/user_roles?searchParam=id&value=10
 * }</pre>
 */
@Log4j2
@Tag(name = "Database Records", description = "Endpoints for inspecting database records and tables")
@RestApiController("/api/v1/database")
@RequiredArgsConstructor
public class DatabaseController {

    private final DatabaseService databaseService;

    /**
     * Fetches database records across all tables, optionally filtered by field and value.
     *
     * @param searchParam optional filter attribute (e.g. {@code id}, {@code key})
     * @param value the filter match value
     * @return map of table names to lists of {@link DatabaseRecord} entries
     */
    @Operation(summary = "Get database records",
        description = "Retrieves all database records grouped by table name with optional search filtering.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Database records retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(type = "object", additionalProperties = Schema.AdditionalPropertiesValue.TRUE))),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters provided",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<DatabaseRecord>> getDatabaseRecords(
        @Parameter(description = "Filter field name (e.g., 'id' or 'key')", example = "key")
        @RequestParam(required = false) final Optional<String> searchParam,
        @Parameter(description = "Target filter value", example = "STATUS_ACTIVE") @RequestParam(required = false)
        final Optional<String> value) {
        log.info("Fetching database records with searchParam='{}', value='{}'", searchParam.orElse(null),
            value.orElse(null));
        return databaseService.getDatabaseRecords(searchParam, value);
    }

    /**
     * Fetches all registered database table names.
     *
     * @return list of table name identifiers
     */
    @Operation(summary = "List database tables",
        description = "Retrieves a distinct list of all table names available in the data repository.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Table names retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(type = "string", example = "user_roles"))))})
    @GetMapping(value = "/tables", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getTables() {
        log.info("Fetching all database table names");
        return databaseService.getTables();
    }

    /**
     * Fetches records for a specific database table.
     *
     * @param table the target table identifier
     * @param searchParam optional filter attribute (e.g. {@code id}, {@code key})
     * @param value the filter match value
     * @return list of matching {@link DatabaseRecord} objects
     */
    @Operation(summary = "Get table records",
        description = "Retrieves records contained in the specified table name with optional search filtering.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Table records retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = DatabaseRecord.class)))),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters supplied",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/tables/{table}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DatabaseRecord> getTableRecords(
        @Parameter(description = "Target database table name", example = "user_roles") @PathVariable final String table,
        @Parameter(description = "Filter field name (e.g., 'id' or 'key')", example = "id")
        @RequestParam(required = false) final Optional<String> searchParam,
        @Parameter(description = "Target filter value", example = "1001") @RequestParam(required = false)
        final Optional<String> value) {
        log.info("Fetching records for table '{}' with searchParam='{}', value='{}'", table, searchParam.orElse(null),
            value.orElse(null));
        return databaseService.getDatabaseRecords(table, searchParam, value);
    }
}