package com.freightfox.github_searcher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record GitHubApiRepository(
        Long id,
        String name,
        String description,
        GitHubOwner owner,
        String language,
        @JsonProperty("stargazers_count") Integer stargazersCount,
        @JsonProperty("forks_count") Integer forksCount,
        @JsonProperty("updated_at") OffsetDateTime updatedAt
) {}
