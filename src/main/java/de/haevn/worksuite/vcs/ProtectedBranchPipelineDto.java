package de.haevn.worksuite.vcs;
public record ProtectedBranchPipelineDto(
    String id,
    String projectName,
    String branchName,
    PipelineStatus status,
    String commitMessage,
    String webUrl,
    String updatedAt
) {}