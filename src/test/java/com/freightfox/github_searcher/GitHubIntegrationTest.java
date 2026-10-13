package com.freightfox.github_searcher;

import com.freightfox.github_searcher.client.GitHubClient;
import com.freightfox.github_searcher.dto.GitHubApiRepository;
import com.freightfox.github_searcher.dto.GitHubApiResponse;
import com.freightfox.github_searcher.dto.GitHubOwner;
import com.freightfox.github_searcher.entities.GitHubRepository;
import com.freightfox.github_searcher.repository.GitHubRepositoryRepo;
import com.freightfox.github_searcher.services.GitHubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = "spring.profiles.active=test")
public class GitHubIntegrationTest {

    @Autowired
    private GitHubRepositoryRepo repository;

    @Test
    void applicationStartsAndConnectsToPostgreSQL() {
        List<GitHubRepository> repositories = repository.findAll();

        assertNotNull(repositories);
    }

    @Test
    void savesAndRetrievesRepository() {
        GitHubRepository repo = new GitHubRepository(
                987654321L,
                "integration-test-repo",
                "Test description",
                "test-owner",
                "Java",
                100,
                20,
                OffsetDateTime.parse("2026-01-01T10:00:00Z")
        );

        repository.save(repo);

        GitHubRepository saved = repository.findById(987654321L)
                .orElseThrow();

        assertEquals("integration-test-repo", saved.getName());
        assertEquals("test-owner", saved.getOwner());
        assertEquals(100, saved.getStars());
    }

    @Test
    void updatesExistingRepositoryInsteadOfCreatingDuplicate() {
        GitHubRepository original = new GitHubRepository(
                987654322L,
                "original-name",
                "Original description",
                "test-owner",
                "Java",
                100,
                20,
                OffsetDateTime.parse("2026-01-01T10:00:00Z")
        );

        repository.save(original);

        GitHubRepository updated = new GitHubRepository(
                987654322L,
                "updated-name",
                "Updated description",
                "test-owner",
                "Java",
                200,
                40,
                OffsetDateTime.parse("2026-02-01T10:00:00Z")
        );

        repository.save(updated);

        GitHubRepository result = repository.findById(987654322L)
                .orElseThrow();

        assertEquals("updated-name", result.getName());
        assertEquals(200, result.getStars());
        assertEquals(40, result.getForks());
        assertEquals(1, repository.findAll().stream()
                .filter(r -> r.getId().equals(987654322L))
                .count());
    }


    @Test
    void searchRepositoryUpdatesExistingRepositoryInPostgreSQL() {
        GitHubClient client = mock(GitHubClient.class);

        GitHubService service = new GitHubService(client, repository);

        OffsetDateTime firstUpdate = OffsetDateTime.parse("2026-01-01T10:00:00Z");

        OffsetDateTime secondUpdate = OffsetDateTime.parse("2026-02-01T10:00:00Z");

        GitHubApiRepository firstResult = new GitHubApiRepository(
                987654323L,
                "original-name",
                "Original description",
                new GitHubOwner("test-owner"),
                "Java",
                100,
                20,
                firstUpdate
        );

        GitHubApiRepository secondResult = new GitHubApiRepository(
                987654323L,
                "updated-name",
                "Updated description",
                new GitHubOwner("test-owner"),
                "Java",
                200,
                40,
                secondUpdate
        );

        when(
                client.searchRepository("integration-test", "Java", "stars")
        ).thenReturn(
                new GitHubApiResponse(List.of(firstResult)),
                new GitHubApiResponse(List.of(secondResult))
        );

        service.searchRepository("integration-test", "Java", "stars");
        service.searchRepository("integration-test", "Java", "stars");

        GitHubRepository saved = repository.findById(987654323L)
                .orElseThrow();

        assertEquals("updated-name", saved.getName());
        assertEquals("Updated description", saved.getDescription());
        assertEquals(200, saved.getStars());
        assertEquals(40, saved.getForks());
        assertEquals(secondUpdate, saved.getLastUpdated());

        assertEquals(
                1,
                repository.findAll().stream()
                        .filter(r -> r.getId().equals(987654323L))
                        .count()
        );
    }

}
