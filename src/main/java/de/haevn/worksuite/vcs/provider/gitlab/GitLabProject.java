package de.haevn.worksuite.vcs.provider.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitLabProject(
    @JsonProperty("id") Long id,
    @JsonProperty("description") String description,
    @JsonProperty("name") String name,
    @JsonProperty("name_with_namespace") String nameWithNamespace,
    @JsonProperty("path") String path,
    @JsonProperty("path_with_namespace") String pathWithNamespace,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("default_branch") String defaultBranch,
    @JsonProperty("ssh_url_to_repo") String sshUrlToRepo,
    @JsonProperty("http_url_to_repo") String httpUrlToRepo,
    @JsonProperty("web_url") String webUrl
) {}