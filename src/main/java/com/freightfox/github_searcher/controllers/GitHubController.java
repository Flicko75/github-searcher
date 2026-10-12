package com.freightfox.github_searcher.controllers;

import com.freightfox.github_searcher.dto.GitHubSearchRequest;
import com.freightfox.github_searcher.dto.GitHubSearchResponse;
import com.freightfox.github_searcher.entities.GitHubRepository;
import com.freightfox.github_searcher.services.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService service;

    @PostMapping("/search")
    public GitHubSearchResponse searchRepository(
            @RequestBody GitHubSearchRequest request
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
            @RequestParam(required = false) Integer minStars,
            @RequestParam(required = false) String sort
    ) {
        return service.fetchRepository(language, minStars, sort);
    }

}
