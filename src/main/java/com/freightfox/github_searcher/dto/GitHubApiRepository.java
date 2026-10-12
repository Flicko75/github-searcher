package com.freightfox.github_searcher.dto;

import java.time.OffsetDateTime;

public record GitHubApiRepository(
        Long id,
        String name,
        String description,
        GitHubOwner owner,
        String language,
        Integer stargazers_count,
        Integer forks_count,
        OffsetDateTime updated_at
) {}
