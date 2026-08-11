package de.haevn.worksuite.vcs;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Data transfer object representing the execution status of a CI/CD pipeline on a protected branch.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * ProtectedBranchPipelineDto pipeline = new ProtectedBranchPipelineDto(
 *     "pipe-9901",
 *     "work-suite-backend",
 *     "main",
 *     PipelineStatus.SUCCESS,
 *     "Merge branch 'feature/auth' into 'main'",
 *     "https://gitlab.example.com/group/repo/-/pipelines/9901",
 *     "2026-08-17T18:20:00.000Z"
 * );
 * }</pre>
 *
 * @param id unique pipeline identifier
 * @param projectName repository project name
 * @param branchName name of the protected branch (e.g. {@code main}, {@code release/*})
 * @param status pipeline status
 * @param commitMessage message of the commit triggering the pipeline
 * @param webUrl URL to the pipeline view in GitLab
 * @param updatedAt last update timestamp string
 */
@Schema(description = "Execution status of a pipeline on a protected Git branch")
public record ProtectedBranchPipelineDto(

    @Schema(description = "Pipeline identifier", example = "9901") String id,

    @Schema(description = "Repository project name", example = "work-suite-backend") String projectName,

    @Schema(description = "Protected branch name", example = "main") String branchName,

    @Schema(description = "Pipeline execution status", example = "SUCCESS") PipelineStatus status,

    @Schema(description = "Commit message", example = "chore: bump dependencies to 2026.8") String commitMessage,

    @Schema(description = "Web link to pipeline execution",
        example = "https://gitlab.example.com/repo/-/pipelines/9901") String webUrl,

    @Schema(description = "Timestamp of the last status update",
        example = "2026-08-17T18:20:00.000Z") String updatedAt) {
}