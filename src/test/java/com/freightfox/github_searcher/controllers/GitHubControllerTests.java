package com.freightfox.github_searcher.controllers;

import com.freightfox.github_searcher.exceptions.GitHubApiException;
import com.freightfox.github_searcher.services.GitHubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
public class GitHubControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GitHubService service;

    @Test
    void searchRepositoryWithValidRequestReturnsSuccess() throws Exception {
        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "query": "spring boot",
                                  "language": "Java",
                                  "sort": "stars"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Repositories fetched and saved successfully"))
                .andExpect(jsonPath("$.repositories").isArray());
    }

    @Test
    void searchRepositoryWithBlankQueryReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "query": " ",
                              "language": "Java",
                              "sort": "stars"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Query must not be blank."))
                .andExpect(jsonPath("$.path").value("/api/github/search"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(service);
    }

    @Test
    void searchRepositoryWithInvalidSortReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "query": "spring boot",
                              "language": "Java",
                              "sort": "random"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Sort must be stars, forks, or updated"))
                .andExpect(jsonPath("$.path").value("/api/github/search"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(service);
    }

    @Test
    void fetchRepositoriesReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/github/repositories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void fetchRepositoriesWithLanguageFilterReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/github/repositories")
                        .param("language", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service).fetchRepository("Java", null, null);
    }

    @Test
    void fetchRepositoriesWithMinStarsFilterReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/github/repositories")
                        .param("minStars", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service).fetchRepository(null, 100, null);
    }

    @Test
    void fetchRepositoriesWithCombinedFiltersReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/github/repositories")
                        .param("language", "Java")
                        .param("minStars", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service).fetchRepository("Java", 100, null);
    }

    @Test
    void fetchRepositoriesWithSortReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/github/repositories")
                        .param("sort", "forks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service).fetchRepository(null, null, "forks");
    }

    @Test
    void fetchRepositoriesWithUpdatedSortReturnsSuccess() throws Exception {
        mockMvc.perform(get("/api/github/repositories")
                        .param("sort", "updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service).fetchRepository(null, null, "updated");
    }

    @Test
    void fetchRepositoriesWithoutParametersUsesDefaultSort() throws Exception {
        mockMvc.perform(get("/api/github/repositories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(service).fetchRepository(null, null, null);
    }

    @Test
    void fetchRepositoriesWithNegativeMinStarsReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/github/repositories")
                        .param("minStars", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request parameter"))
                .andExpect(jsonPath("$.path").value("/api/github/repositories"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(service);
    }

    @Test
    void fetchRepositoriesWithInvalidSortReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/github/repositories")
                        .param("sort", "random"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request parameter"))
                .andExpect(jsonPath("$.path").value("/api/github/repositories"))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(service);
    }

    @Test
    void searchRepositoryWhenRateLimitedReturnsTooManyRequests() throws Exception {
        when(service.searchRepository(anyString(), anyString(), anyString()))
                .thenThrow(new GitHubApiException(
                        "GitHub rate limit exceeded",
                        HttpStatus.FORBIDDEN
                ));

        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "query": "spring boot",
                              "language": "Java",
                              "sort": "stars"
                            }
                            """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("GitHub rate limit exceeded"))
                .andExpect(jsonPath("$.path").value("/api/github/search"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void searchRepositoryWhenUnexpectedExceptionOccursReturnsInternalServerError() throws Exception {
        when(service.searchRepository(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Internal details"));

        mockMvc.perform(post("/api/github/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "query": "spring boot",
                              "language": "Java",
                              "sort": "stars"
                            }
                            """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.path").value("/api/github/search"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
