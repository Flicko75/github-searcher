package com.freightfox.github_searcher.controllers;

import com.freightfox.github_searcher.dto.ApiErrorResponse;
import com.freightfox.github_searcher.dto.GitHubSearchRequest;
import com.freightfox.github_searcher.dto.GitHubSearchResponse;
import com.freightfox.github_searcher.entities.GitHubRepository;
import com.freightfox.github_searcher.services.GitHubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/github")
@Tag(name = "GitHub Repositories", description = "Search and retrieve repositories")
public class GitHubController {

    private final GitHubService service;

    @Operation(
            summary = "Search GitHub repositories",
            description = "Search by keyword and optional language, then save the results to PostgreSQL. Existing repositories are updated instead of duplicated."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully. An empty result list is valid.",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-14T12:00:00+05:30",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Query must not be blank",
                                      "path": "/api/github/search"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "GitHub API rate limit exceeded",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-14T12:00:00+05:30",
                                      "status": 429,
                                      "error": "Too Many Requests",
                                      "message": "GitHub rate limit exceeded",
                                      "path": "/api/github/search"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected application error or GitHub returned HTTP 500",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-14T12:00:00+05:30",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "An unexpected error occurred",
                                      "path": "/api/github/search"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "GitHub API request failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-14T12:00:00+05:30",
                                      "status": 502,
                                      "error": "Bad Gateway",
                                      "message": "GitHub API request failed with status: 404",
                                      "path": "/api/github/search"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Unable to connect to GitHub API",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-14T12:00:00+05:30",
                                      "status": 503,
                                      "error": "Service Unavailable",
                                      "message": "Unable to connect to GitHub API",
                                      "path": "/api/github/search"
                                    }
                                    """)
                    )
            )
    })
    @PostMapping("/search")
    public GitHubSearchResponse searchRepository(
            @Valid @RequestBody GitHubSearchRequest request
    ) {
        List<GitHubRepository> repositories = service.searchRepository(
                request.query(),
                request.language(),
                request.sort()
        );

        return new GitHubSearchResponse(
                "Repositories fetched and saved successfully",
                repositories
        );
    }

    @Operation(
            summary = "Get saved repositories",
            description = "Retrieve saved repositories with optional filters and sorting. Defaults to stars."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Saved repositories retrieved successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid query parameter",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-14T12:00:00+05:30",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Invalid request parameter",
                                      "path": "/api/github/repositories"
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected application error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2026-10-14T12:00:00+05:30",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "An unexpected error occurred",
                                      "path": "/api/github/repositories"
                                    }
                                    """)
                    )
            )
    })
    @GetMapping("/repositories")
    public List<GitHubRepository> fetchRepositories(
            @Parameter(
                    description = "Filter by programming language, ignoring case",
                    example = "Java"
            )
            @RequestParam(required = false) String language,

            @Parameter(
                    description = "Minimum star count, inclusive. Must be zero or greater.",
                    example = "100"
            )
            @Min (value = 0, message = "minStars must be zero or greater")
            @RequestParam(required = false) Integer minStars,

            @Parameter(
                    description = "Sort by stars, forks, or updated. Defaults to stars.",
                    example = "stars",
                    schema = @Schema(
                            allowableValues = {"stars", "forks", "updated"},
                            defaultValue = "stars"
                    )
            )
            @Pattern(
                    regexp = "(?i)stars|forks|updated",
                    message = "Sort must be stars, forks, or updated"
            )
            @RequestParam(required = false) String sort
    ) {
        return service.fetchRepository(language, minStars, sort);
    }

}
