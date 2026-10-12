package com.freightfox.github_searcher.controllers;

import com.freightfox.github_searcher.dto.GitHubSearchRequest;
import com.freightfox.github_searcher.dto.GitHubSearchResponse;
import com.freightfox.github_searcher.entities.GitHubRepository;
import com.freightfox.github_searcher.services.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private final GitHubService service;

    @PostMapping("/search")
    public GitHubSearchResponse searchRepository(
            @RequestBody GitHubSearchRequest request)
    {
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

}
