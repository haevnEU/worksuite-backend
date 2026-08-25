package de.haevn.worksuite.vcs.provider.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitLabPipeline(
    @JsonProperty("id") Long id,
    @JsonProperty("iid") Long iid,
    @JsonProperty("project_id") Long projectId,
    @JsonProperty("sha") String sha,
    @JsonProperty("ref") String ref,
    @JsonProperty("status") String status,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("updated_at") String updatedAt,
    @JsonProperty("web_url") String webUrl
) {}