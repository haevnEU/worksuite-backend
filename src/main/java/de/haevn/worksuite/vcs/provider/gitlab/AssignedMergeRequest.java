package de.haevn.worksuite.vcs.provider.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AssignedMergeRequest(
    @JsonProperty("id") Long id,
    @JsonProperty("iid") Long iid,
    @JsonProperty("project_id") Long projectId,
    @JsonProperty("title") String title,
    @JsonProperty("description") String description,
    @JsonProperty("state") String state,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("updated_at") String updatedAt,
    @JsonProperty("target_branch") String targetBranch,
    @JsonProperty("source_branch") String sourceBranch,
    @JsonProperty("author") GitLabUser author,
    @JsonProperty("assignees") List<GitLabUser> assignees,
    @JsonProperty("assignee") GitLabUser assignee,
    @JsonProperty("reviewers") List<GitLabUser> reviewers,
    @JsonProperty("source_project_id") Long sourceProjectId,
    @JsonProperty("target_project_id") Long targetProjectId,
    @JsonProperty("merge_status") String mergeStatus,
    @JsonProperty("detailed_merge_status") String detailedMergeStatus,
    @JsonProperty("sha") String sha,
    @JsonProperty("web_url") String webUrl
) {}