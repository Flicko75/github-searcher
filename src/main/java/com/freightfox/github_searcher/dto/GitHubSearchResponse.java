package com.freightfox.github_searcher.dto;

import com.freightfox.github_searcher.entities.GitHubRepository;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GitHubSearchResponse(
        @Schema(
                description = "Message describing the search result",
                example = "Repositories fetched and saved successfully"
        )
        String message,

        @Schema(description = "Repositories fetched from GitHub and saved to PostgreSQL")
        List<GitHubRepository> repositories
) {}
