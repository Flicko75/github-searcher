package com.freightfox.github_searcher.controllers;

import com.freightfox.github_searcher.dto.GitHubSearchRequest;
import com.freightfox.github_searcher.dto.GitHubSearchResponse;
import com.freightfox.github_searcher.entities.GitHubRepository;
import com.freightfox.github_searcher.services.GitHubService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService service;

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

    @GetMapping("/repositories")
    public List<GitHubRepository> fetchRepositories(
            @RequestParam(required = false) String language,
            @Min (value = 0, message = "minStars must be zero or greater")
            @RequestParam(required = false) Integer minStars,
            @Pattern(
                    regexp = "(?i)stars|forks|updated",
                    message = "Sort must be stars, forks, or updated"
            )
            @RequestParam(required = false) String sort
    ) {
        return service.fetchRepository(language, minStars, sort);
    }

}
