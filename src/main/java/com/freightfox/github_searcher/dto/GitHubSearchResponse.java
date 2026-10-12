package com.freightfox.github_searcher.dto;

import com.freightfox.github_searcher.entities.GitHubRepository;

import java.util.List;

public record GitHubSearchResponse(
        String message,
        List<GitHubRepository> repositories
) {}
