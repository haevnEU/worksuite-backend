package de.haevn.worksuite.config;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * Contextual model holding third-party service integration credentials for the authenticated user.
 *
 * <p>Used across interceptors and downstream services to interact with integrated VCS and issue tracking
 * systems on behalf of the user.
 *
 * <p>Example usage:
 * <pre>{@code
 * UserIntegrationContext context = new UserIntegrationContext(
 *     UUID.randomUUID(),
 *     "redmine_api_key_12345",
 *     "glpat-vcs_token_abcdef"
 * );
 * }</pre>
 *
 * @param userId the unique identifier of the authenticated user
 * @param redmineApiKey the API token for Redmine integration
 * @param vcsToken the personal access token for VCS integration (e.g., GitLab/GitHub)
 */
@Schema(description = "User integration credentials and identity context")
public record UserIntegrationContext(

    @Schema(description = "Unique user identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") UUID userId,

    @Schema(description = "Redmine REST API access token",
        example = "9f8e7d6c5b4a3210fedcba9876543210") String redmineApiKey,

    @Schema(description = "Version Control System personal access token",
        example = "glpat-xxxxxxxxxxxxxxxxxxxx") String vcsToken) {
}