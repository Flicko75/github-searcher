package com.freightfox.github_searcher.client;


import com.freightfox.github_searcher.exceptions.GitHubApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class GitHubClientTest {

    @Test
    void searchRepositoryBuildsCorrectRequest() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com");

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        GitHubClient client = new GitHubClient(builder.build());

        server.expect(requestTo(
                        "https://api.github.com/search/repositories?q=spring%20boot%20language:Java&sort=stars&order=desc&per_page=30"))
                .andExpect(method(GET))
                .andRespond(withSuccess(
                        "{\"items\":[]}",
                        MediaType.APPLICATION_JSON
                ));

        var response = client.searchRepository("spring boot", "Java", "stars");

        assertNotNull(response);
        server.verify();
    }

    @Test
    void searchRepositoryHandlesRateLimit() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com");

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        GitHubClient client = new GitHubClient(builder.build());

        server.expect(requestTo(
                        "https://api.github.com/search/repositories?q=spring%20boot&sort=stars&order=desc&per_page=30"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN)
                        .header("X-RateLimit-Remaining", "0")
                        .body("{\"message\":\"API rate limit exceeded\"}"));

        GitHubApiException exception = assertThrows(
                GitHubApiException.class,
                () -> client.searchRepository("spring boot", null, "stars")
        );

        assertEquals("GitHub rate limit exceeded", exception.getMessage());
        server.verify();
    }

    @Test
    void searchRepositoryHandlesServerError() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com");

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        GitHubClient client = new GitHubClient(builder.build());

        server.expect(requestTo(
                        "https://api.github.com/search/repositories?q=spring%20boot&sort=stars&order=desc&per_page=30"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        GitHubApiException exception = assertThrows(
                GitHubApiException.class,
                () -> client.searchRepository("spring boot", null, "stars")
        );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getStatusCode()
        );
        assertEquals(
                "GitHub API request failed with status: 500",
                exception.getMessage()
        );

        server.verify();
    }

    @Test
    void searchRepositoryHandlesConnectionFailure() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://api.github.com");

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        GitHubClient client = new GitHubClient(builder.build());

        server.expect(requestTo(
                        "https://api.github.com/search/repositories?q=spring%20boot&sort=stars&order=desc&per_page=30"))
                .andRespond(request -> {
                    throw new ResourceAccessException("Connection failed");
                });

        GitHubApiException exception = assertThrows(
                GitHubApiException.class,
                () -> client.searchRepository("spring boot", null, "stars")
        );

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        assertEquals("Unable to connect to GitHub API", exception.getMessage());

        server.verify();
    }

}
