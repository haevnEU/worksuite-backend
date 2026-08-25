package de.haevn.worksuite.vcs.provider.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitLabUser(
    @JsonProperty("id") Long id,
    @JsonProperty("username") String username,
    @JsonProperty("name") String name,
    @JsonProperty("avatar_url") String avatarUrl
) {}