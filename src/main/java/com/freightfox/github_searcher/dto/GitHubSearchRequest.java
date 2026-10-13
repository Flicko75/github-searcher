package com.freightfox.github_searcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record GitHubSearchRequest(
        @NotBlank(message = "Query must not be blank.")
        String query,

        String language,

        @Pattern(
                regexp = "(?i)stars|forks|updated",
                message = "Sort must be stars, forks, or updated"
        )
        String sort
) {
}
