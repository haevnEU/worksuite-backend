package de.haevn.worksuite.gitlab;

public record MergeRequestDto(
    String id,
    long iid,
    String title,
    AuthorDto author,
    String sourceBranch,
    String targetBranch,
    String webUrl,
    PipelineStatus pipelineStatus,
    int userNotesCount,
    boolean hasConflicts,
    boolean isDraft,
    boolean approved,
    String updatedAt,
    String projectName
) {
    public record AuthorDto(
        String name,
        String avatarUrl
    ) {}
}