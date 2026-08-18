package de.haevn.worksuite.ticket.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload containing QA handover protocol details, test execution results, and review metadata.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * QaProtocolRequest request = new QaProtocolRequest(
 *     true,
 *     null,
 *     true,
 *     "Implemented new SSO authentication flow.",
 *     true,
 *     "- User can log in via OIDC\n- Token is verified",
 *     true,
 *     "Start Docker container and run test suite",
 *     true,
 *     "AuthenticationServiceTest#testValidLogin",
 *     false,
 *     null,
 *     false,
 *     null,
 *     true,
 *     "POST /api/v1/auth/login"
 * );
 * }</pre>
 *
 * @param pipelineSuccess indicates whether the CI/CD pipeline succeeded
 * @param pipelineFailReason explanation for pipeline failure if {@code pipelineSuccess} is {@code false}
 * @param rebaseExecuted indicates whether the branch was rebased against target base branch
 * @param intro introduction and general context for the QA team
 * @param hasAcceptanceCriteria flag enabling the acceptance criteria section
 * @param acceptanceCriteria list or markdown text of acceptance criteria
 * @param hasTestSetup flag enabling the test setup section
 * @param testSetup step-by-step instructions for testing
 * @param hasUnitTests flag enabling the unit test section
 * @param unitTests description of covered unit and integration test cases
 * @param hasTestDatasets flag enabling dataset definitions
 * @param testDatasets pipe-separated dataset table rows (file|description)
 * @param hasSideEffects flag enabling side effects warnings
 * @param sideEffects notes on potential side effects or breaking changes
 * @param hasChangedEndpoints flag enabling changed REST endpoints documentation
 * @param changedEndpoints list of modified or added API endpoints
 */
@Schema(description = "Payload for submitting a QA handover protocol")
public record QaProtocolRequest(

    @NotNull(message = "Pipeline status must be provided") @Schema(
        description = "Indicates whether the CI pipeline execution passed", example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED) Boolean pipelineSuccess,

    @Schema(description = "Explanation of why the CI pipeline failed (required if pipelineSuccess is false)",
        example = "Flaky integration test timed out in step 4") String pipelineFailReason,

    @NotNull(message = "Rebase status must be provided") @Schema(
        description = "Indicates whether the feature branch has been rebased against main", example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED) Boolean rebaseExecuted,

    @Schema(description = "Introduction and summary of the implemented feature/fix",
        example = "Refactored user authentication to use stateless JWT validation.") String intro,

    @Schema(description = "Flag indicating whether acceptance criteria are provided",
        example = "true") Boolean hasAcceptanceCriteria,

    @Schema(description = "Newline-separated list of acceptance criteria",
        example = "- User can log in\n- Refresh token is stored safely") String acceptanceCriteria,

    @Schema(description = "Flag indicating whether test setup instructions are provided",
        example = "true") Boolean hasTestSetup,

    @Schema(description = "Instructions on setting up test data and prerequisites",
        example = "Run database seed script 'test_users.sql' before verifying.") String testSetup,

    @Schema(description = "Flag indicating whether unit test descriptions are provided",
        example = "true") Boolean hasUnitTests,

    @Schema(description = "Executed unit tests or test coverage details",
        example = "UserServiceTest#testJwtValidation_ValidToken_ReturnsSuccess") String unitTests,

    @Schema(description = "Flag indicating whether test dataset files are attached",
        example = "false") Boolean hasTestDatasets,

    @Schema(
        description = "Pipe-separated list of test dataset files and descriptions (e.g. 'users.json|Sample user accounts')",
        example = "test-payload.json|Input payload with multiple roles") String testDatasets,

    @Schema(description = "Flag indicating whether potential side effects are documented",
        example = "false") Boolean hasSideEffects,

    @Schema(description = "Description of potential side effects or backward compatibility warnings",
        example = "Database column 'legacy_auth_id' is deprecated and will be removed in next sprint.") String sideEffects,

    @Schema(description = "Flag indicating whether modified REST endpoints are documented",
        example = "true") Boolean hasChangedEndpoints,

    @Schema(description = "List or code block of newly added or modified REST endpoints",
        example = "POST /api/v1/auth/token\nGET /api/v1/users/me") String changedEndpoints) {

    /**
     * Compact constructor normalizing boolean flags to non-null defaults and validating domain constraints.
     *
     * @throws IllegalArgumentException if {@code pipelineSuccess} is {@code false} but no {@code pipelineFailReason} is provided
     */
    public QaProtocolRequest {
        hasAcceptanceCriteria = Boolean.TRUE.equals(hasAcceptanceCriteria);
        hasTestSetup = Boolean.TRUE.equals(hasTestSetup);
        hasUnitTests = Boolean.TRUE.equals(hasUnitTests);
        hasTestDatasets = Boolean.TRUE.equals(hasTestDatasets);
        hasSideEffects = Boolean.TRUE.equals(hasSideEffects);
        hasChangedEndpoints = Boolean.TRUE.equals(hasChangedEndpoints);

        if (Boolean.FALSE.equals(pipelineSuccess) && (pipelineFailReason == null || pipelineFailReason.isBlank())) {
            throw new IllegalArgumentException("A reason for the pipeline failure must be provided.");
        }
    }
}