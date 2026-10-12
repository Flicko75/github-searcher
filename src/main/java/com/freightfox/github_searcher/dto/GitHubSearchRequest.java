package com.freightfox.github_searcher.dto;

public record GitHubSearchRequest(
        String query,
        String language,
        String sort
) {
}
