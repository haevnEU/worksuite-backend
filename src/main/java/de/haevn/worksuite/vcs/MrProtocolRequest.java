package de.haevn.worksuite.vcs;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload containing parameters for generating and submitting a Merge Request with structured review protocols.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * MrProtocolRequest request = new MrProtocolRequest(
 *     "Implement JWT Token Validation",
 *     "Adds JwtAuthenticationFilter to Spring Security chain.",
 *     "TICK-4021",
 *     true,
 *     "Switched authentication filter order.",
 *     true,
 *     true,
 *     "Added JwtAuthenticationFilterTest suite.",
 *     true,
 *     "Tested with Postman using mock Bearer tokens.",
 *     false,
 *     false,
 *     false
 * );
 * }</pre>
 *
 * @param title headline or summary of the Merge Request
 * @param description detailed description of the changes
 * @param ticketId issue tracking ticket identifier
 * @param hasImportantChanges flag indicating whether important architectural changes are documented
 * @param importantChanges description of key architectural or behavioral changes
 * @param hasTestSetup flag indicating whether test setup instructions are provided
 * @param hasUnitTests flag indicating whether unit test details are present
 * @param unitTests description of covered unit test scenarios
 * @param hasManualTests flag indicating whether manual test instructions are present
 * @param manualTests step-by-step instructions for manual verification
 * @param hasBreakingChanges flag indicating potential breaking changes or API incompatibilities
 * @param hasDatabaseSchemaChanges flag indicating database schema migrations (e.g. Flyway / Liquibase)
 * @param hasDatabaseViewsChanges flag indicating changes to database views or stored procedures
 */
@Schema(description = "Payload for generating and submitting a Merge Request protocol")
public record MrProtocolRequest(

    @Schema(description = "Merge Request title", example = "Feature: Stateless JWT Authentication") String title,

    @Schema(description = "Detailed Merge Request description",
        example = "Configures JwtAuthenticationFilter and populates SecurityContextHolder with user claims.") String description,

    @NotBlank(message = "Ticket ID must be provided") @Schema(description = "Associated ticket identifier",
        example = "4021", requiredMode = Schema.RequiredMode.REQUIRED) String ticketId,

    @Schema(description = "Flag indicating whether key architectural changes are documented",
        example = "true") Boolean hasImportantChanges,

    @Schema(description = "Description of important structural or architectural changes",
        example = "Replaced session-based security with stateless JWT filter.") String importantChanges,

    @Schema(description = "Flag indicating whether test setup steps are required",
        example = "false") Boolean hasTestSetup,

    @Schema(description = "Flag indicating whether unit tests are included", example = "true") Boolean hasUnitTests,

    @Schema(description = "Summary of newly added or updated unit tests",
        example = "JwtAuthenticationFilterTest covers valid, expired, and malformed token scenarios.") String unitTests,

    @Schema(description = "Flag indicating whether manual test instructions are documented",
        example = "true") Boolean hasManualTests,

    @Schema(description = "Step-by-step instructions for manual verification",
        example = "1. Send request without Bearer token -> verify HTTP 401\n2. Supply valid token -> verify HTTP 200") String manualTests,

    @Schema(description = "Flag indicating potential breaking changes", example = "false") Boolean hasBreakingChanges,

    @Schema(description = "Flag indicating database schema changes",
        example = "false") Boolean hasDatabaseSchemaChanges,

    @Schema(description = "Flag indicating database view or function changes",
        example = "false") Boolean hasDatabaseViewsChanges) {

    /**
     * Compact constructor normalizing boolean flags to non-null boolean values.
     */
    public MrProtocolRequest {
        hasImportantChanges = Boolean.TRUE.equals(hasImportantChanges);
        hasTestSetup = Boolean.TRUE.equals(hasTestSetup);
        hasUnitTests = Boolean.TRUE.equals(hasUnitTests);
        hasManualTests = Boolean.TRUE.equals(hasManualTests);
        hasBreakingChanges = Boolean.TRUE.equals(hasBreakingChanges);
        hasDatabaseSchemaChanges = Boolean.TRUE.equals(hasDatabaseSchemaChanges);
        hasDatabaseViewsChanges = Boolean.TRUE.equals(hasDatabaseViewsChanges);
    }
}