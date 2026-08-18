package de.haevn.worksuite.ticket;

import de.haevn.redmine.api.RedmineException;
import de.haevn.redmine.model.Issue;
import de.haevn.worksuite.common.RestApiController;
import de.haevn.worksuite.common.exceptions.ErrorResponseDTO;
import de.haevn.worksuite.vcs.MrProtocolRequest;
import de.haevn.worksuite.vcs.VcsProvider;
import de.haevn.worksuite.vcs.VcsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller exposing REST endpoints for managing Redmine tickets, work logs, comments, QA handovers, and VCS integrations.
 *
 * <p>Example HTTP requests:
 * <pre>{@code
 * GET /api/v1/ticket
 * GET /api/v1/ticket/4021
 * POST /api/v1/ticket/4021/move-to-qs
 * POST /api/v1/ticket/4021/time-entries
 * POST /api/v1/ticket/4021/comment
 * }</pre>
 */
@Log4j2
@Tag(name = "Ticket Management", description = "Endpoints for managing Redmine issues, QA protocols, and time booking")
@RestApiController("/api/v1/ticket")
@RequiredArgsConstructor
public class RedmineController {

    private final RedmineService redmineService;
    private final VcsService vcsService;

    /**
     * Fetches all issues assigned to the authenticated user.
     *
     * @return list of assigned {@link Issue} objects
     * @throws RedmineException if Redmine communication fails
     */
    @Operation(summary = "List assigned tickets",
        description = "Retrieves all Redmine tickets currently assigned to the active user.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Tickets retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            array = @ArraySchema(schema = @Schema(implementation = Issue.class)))),
        @ApiResponse(responseCode = "502", description = "Failed to communicate with Redmine",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Issue> listTickets() throws RedmineException {
        log.info("Request received to list assigned tickets");
        return redmineService.fetch();
    }

    /**
     * Retrieves detailed information for a single ticket by its ID.
     *
     * @param id ticket issue ID
     * @return the resolved {@link Issue}
     * @throws RedmineException if Redmine communication fails
     */
    @Operation(summary = "Get ticket by ID",
        description = "Retrieves complete metadata and journal history for a specific ticket.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Ticket retrieved successfully",
        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = Issue.class))),
        @ApiResponse(responseCode = "404", description = "Ticket not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Issue getTicket(
        @Parameter(description = "Ticket numerical identifier", example = "4021") @PathVariable final long id)
        throws RedmineException {
        log.info("Request received to fetch ticket #{}", id);
        return redmineService.getByIssuedId(id).orElseThrow();
    }

    /**
     * Endpoint stub for inspecting ticket checklists.
     *
     * @param id ticket identifier string
     * @param state optional checklist state filter
     * @throws RedmineException if communication fails
     */
    @Operation(summary = "Get ticket checklist", description = "Retrieves checklist status items for a ticket.")
    @GetMapping("/{id}/checklist")
    public void getTicketChecklist(
        @Parameter(description = "Ticket identifier", example = "4021") @PathVariable final String id,
        @Parameter(description = "Checklist state filter", example = "true")
        @RequestParam(name = "state", required = false) final Optional<Boolean> state) throws RedmineException {
        log.info("Checklist requested for ticket ID: '{}' with state filter: {}", id, state.isPresent());
    }

    /**
     * Moves a ticket to Quality Assurance with an attached QA protocol comment.
     *
     * @param id ticket issue ID
     * @param data validated QA protocol payload
     * @throws RedmineException if status transition fails
     */
    @Operation(summary = "Move ticket to QA",
        description = "Transitions ticket to QA status and appends a formatted protocol comment.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Ticket moved to QA successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid QA protocol payload supplied",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping(value = "/{id}/move-to-qs", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void moveToQs(@Parameter(description = "Ticket identifier", example = "4021") @PathVariable final long id,
        @Valid @RequestBody final QaProtocolRequest data) throws RedmineException {
        log.info("Moving ticket #{} to QS", id);
        redmineService.moveToQs(id, data);
    }

    /**
     * Creates a Merge Request in the VCS provider and links it into Redmine.
     *
     * @param id ticket issue ID
     * @param protocol Merge Request creation parameters
     * @throws RedmineException if updating Redmine fails
     */
    @Operation(summary = "Create Merge Request",
        description = "Creates a VCS Merge Request and links the resulting URL to the Redmine ticket.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Merge Request created and linked successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid MR payload",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping(value = "/{id}/merge-request", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createMergeRequest(
        @Parameter(description = "Ticket identifier", example = "4021", required = true) @PathVariable final long id,
        @Parameter(description = "VCS provider type (defaults to GITLAB)", example = "GITLAB")
        @RequestParam(name = "provider", defaultValue = "GITLAB") final VcsProvider provider,
        @RequestBody final MrProtocolRequest protocol) throws RedmineException {
        log.info("Creating Merge Request for ticket #{} using provider {}", id, provider);
        final String mrLink = vcsService.createMergeRequest(provider, id, protocol);
        redmineService.addMergeRequestLink(id, mrLink);
    }

    /**
     * Books work time against a ticket in Redmine and the local tracking repository.
     *
     * @param id ticket issue ID
     * @param request validated {@link LogTimeRequest} payload
     * @throws RedmineException if logging fails
     */
    @Operation(summary = "Log work time",
        description = "Logs hours and minutes against the ticket in Redmine and local storage.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Time logged successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid time booking data",
            content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))})
    @PostMapping(value = "/{id}/time-entries", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void bookTicket(@Parameter(description = "Ticket identifier", example = "4021") @PathVariable final long id,
        @Valid @RequestBody final LogTimeRequest request) throws RedmineException {
        log.info("Booking {}h {}m on ticket #{}", request.hours(), request.minutes(), id);
        redmineService.bookTicket(id, request);
    }

    /**
     * Appends a comment to the specified ticket.
     *
     * @param id ticket issue ID
     * @param comment raw comment text
     * @throws RedmineException if adding the comment fails
     */
    @Operation(summary = "Add comment", description = "Appends a new journal note/comment to the Redmine ticket.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Comment added successfully")})
    @PostMapping(value = "/{id}/comment", consumes = MediaType.TEXT_PLAIN_VALUE)
    public void createComment(
        @Parameter(description = "Ticket identifier", example = "4021") @PathVariable final long id,
        @RequestBody final String comment) throws RedmineException {
        log.info("Adding comment to ticket #{}", id);
        redmineService.addComment(id, comment);
    }
}