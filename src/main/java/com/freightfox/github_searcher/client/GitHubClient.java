package com.freightfox.github_searcher.client;

import com.freightfox.github_searcher.dto.GitHubApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GitHubClient {

    private final RestClient githubRestClient;

    public GitHubApiResponse searchRepository(
            String query,
            String language,
            String sort
    ) {
        String searchQuery = query;

        if (language != null && !language.isBlank()) {
            searchQuery += " language:" + language;
        }
        String finalSearchQuery = searchQuery;

        return githubRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/repositories")
                        .queryParam("q", finalSearchQuery)
                        .queryParam("sort", sort)
                        .queryParam("order", "desc")
                        .queryParam("per_page", 30)
                        .build()
                )
                .retrieve()
                .body(GitHubApiResponse.class);
    }

}
