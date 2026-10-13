package com.freightfox.github_searcher.client;

import com.freightfox.github_searcher.dto.GitHubApiResponse;
import com.freightfox.github_searcher.exceptions.GitHubApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

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

        try {
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
        } catch (RestClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();
            HttpHeaders headers = e.getResponseHeaders();

            boolean rateLimitExceeded = headers != null
                    && "0".equals(headers.getFirst("X-RateLimit-Remaining"));

            String message;

            if (rateLimitExceeded) {
                message = "GitHub rate limit exceeded";
            } else {
                message = "GitHub API request failed with status: " + statusCode.value();
            }

            throw new GitHubApiException(
                    message,
                    statusCode
            );
        } catch (ResourceAccessException e) {
            throw new GitHubApiException(
                    "Unable to connect to GitHub API",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

}
