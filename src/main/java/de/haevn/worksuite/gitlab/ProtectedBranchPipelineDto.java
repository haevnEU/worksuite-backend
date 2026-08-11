package de.haevn.worksuite.gitlab;
public record ProtectedBranchPipelineDto(
    String id,
    String projectName,
    String branchName,
    PipelineStatus status,
    String commitMessage,
    String webUrl,
    String updatedAt
) {}