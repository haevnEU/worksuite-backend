package de.haevn.worksuite.vcs;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Data transfer object representing a GitLab repository with open Merge Requests and pipeline health.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * GitLabRepository repo = new GitLabRepository(
 *     108L,
 *     "https://gitlab.example.com/worksuite/work-suite-backend",
 *     "work-suite-backend",
 *     "success",
 *     "worksuite/work-suite-backend",
 *     2,
 *     List.of()
 * );
 * }</pre>
 *
 * @param number unique project numerical identifier
 * @param webUrl fully qualified web URL to the repository
 * @param name repository project display name
 * @param lastPipelineStatus status label of the most recent pipeline on default branch
 * @param path full repository path including group namespace
 * @param openMRCount total count of open Merge Requests
 * @param mergeRequests list of active {@link MergeRequestDto} items belonging to the repository
 */
@Schema(description = "GitLab repository summary with associated open Merge Requests")
public record RepositoryDTO(

    @Schema(description = "Project numerical ID", example = "108") long number,

    @Schema(description = "Repository web link",
        example = "https://gitlab.example.com/worksuite/work-suite-backend") String webUrl,

    @Schema(description = "Project name", example = "work-suite-backend") String name,

    @Schema(description = "Latest pipeline status on default branch", example = "success") String lastPipelineStatus,

    @Schema(description = "Full namespaced project path", example = "worksuite/work-suite-backend") String path,

    @Schema(description = "Total number of open Merge Requests", example = "3") int openMRCount,

    @ArraySchema(schema = @Schema(implementation = MergeRequestDto.class)) List<MergeRequestDto> mergeRequests) {
}