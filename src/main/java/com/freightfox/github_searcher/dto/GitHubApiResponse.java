package com.freightfox.github_searcher.dto;

import java.util.List;

public record GitHubApiResponse(
        List<GitHubApiRepository> items
) {}
