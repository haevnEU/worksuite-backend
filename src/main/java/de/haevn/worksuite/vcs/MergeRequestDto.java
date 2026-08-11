package de.haevn.worksuite.vcs;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data transfer object representing a Version Control System Merge Request.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * MergeRequestDto mr = new MergeRequestDto(
 *     "mr-101",
 *     42L,
 *     "Resolve SSO session invalidation",
 *     new MergeRequestDto.AuthorDto("Nils Milewski", "https://gitlab.example.com/uploads/user/avatar.png"),
 *     "feature/sso-fix",
 *     "main",
 *     "https://gitlab.example.com/group/repo/-/merge_requests/42",
 *     PipelineStatus.SUCCESS,
 *     3,
 *     false,
 *     false,
 *     true,
 *     "2026-08-17T17:30:00.000Z",
 *     "work-suite-backend"
 * );
 * }</pre>
 *
 * @param id unique Merge Request ID string
 * @param iid project-internal sequential Merge Request identifier number
 * @param title headline or summary of the Merge Request
 * @param author author profile details
 * @param sourceBranch source branch containing the feature changes
 * @param targetBranch target branch receiving the merge
 * @param webUrl fully qualified web URL to view the Merge Request in browser
 * @param pipelineStatus status of the latest associated CI/CD pipeline
 * @param userNotesCount number of comments/discussions recorded on the Merge Request
 * @param hasConflicts indicates whether the branch has merge conflicts with the target
 * @param isDraft indicates whether the Merge Request is marked as draft/work-in-progress
 * @param approved indicates whether the Merge Request has received the required approvals
 * @param updatedAt ISO timestamp string of the last update
 * @param projectName name of the owning repository project
 */
@Schema(description = "VCS Merge Request representation with pipeline status and author details")
public record MergeRequestDto(

    @Schema(description = "Unique global MR identifier", example = "gid://gitlab/MergeRequest/1042") String id,

    @Schema(description = "Project-internal sequential MR number", example = "42") long iid,

    @Schema(description = "Merge Request title", example = "Feature: Implement JWT token validation") String title,

    @Schema(description = "Author metadata") AuthorDto author,

    @Schema(description = "Source feature branch", example = "feature/jwt-auth") String sourceBranch,

    @Schema(description = "Target destination branch", example = "main") String targetBranch,

    @Schema(description = "Web URL to the Merge Request",
        example = "https://gitlab.example.com/project/-/merge_requests/42") String webUrl,

    @Schema(description = "Status of the latest CI pipeline", example = "SUCCESS") PipelineStatus pipelineStatus,

    @Schema(description = "Total number of discussion notes", example = "5") int userNotesCount,

    @Schema(description = "Merge conflict indicator", example = "false") boolean hasConflicts,

    @Schema(description = "Draft or Work-in-Progress indicator", example = "false") boolean isDraft,

    @Schema(description = "Approval status indicator", example = "true") boolean approved,

    @Schema(description = "Last update timestamp", example = "2026-08-17T18:00:00.000Z") String updatedAt,

    @Schema(description = "Project repository name", example = "worksuite-core") String projectName) {

    /**
     * Nested representation of a Merge Request author.
     *
     * @param name display name of the author
     * @param avatarUrl avatar image URL
     */
    @Schema(description = "Author details for a Merge Request")
    public record AuthorDto(

        @Schema(description = "Author display name", example = "Nils Milewski") String name,

        @Schema(description = "Author avatar image link",
            example = "https://gitlab.example.com/uploads/avatar.png") String avatarUrl) {
    }
}