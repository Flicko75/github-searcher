package com.freightfox.github_searcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record GitHubSearchRequest(
        @Schema(description = "Keywords to search for on GitHub",
                example = "spring boot",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Query must not be blank.")
        String query,

        @Schema(description = "Optional filter by programming language",
                example = "Java",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String language,

        @Schema(description = "Optionally sort results by stars, forks, or updated (default = stars)",
                example = "stars",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Pattern(
                regexp = "(?i)stars|forks|updated",
                message = "Sort must be stars, forks, or updated"
        )
        String sort
) {
}
