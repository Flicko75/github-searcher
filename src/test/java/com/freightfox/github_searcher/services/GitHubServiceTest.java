package com.freightfox.github_searcher.services;

import com.freightfox.github_searcher.client.GitHubClient;
import com.freightfox.github_searcher.dto.GitHubApiRepository;
import com.freightfox.github_searcher.dto.GitHubApiResponse;
import com.freightfox.github_searcher.dto.GitHubOwner;
import com.freightfox.github_searcher.entities.GitHubRepository;
import com.freightfox.github_searcher.exceptions.GitHubApiException;
import com.freightfox.github_searcher.repository.GitHubRepositoryRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubServiceTest {

    @Mock
    private GitHubClient client;

    @Mock
    private GitHubRepositoryRepo repository;

    @InjectMocks
    private GitHubService service;

    @Test
    void searchRepository_mapSaveAndReturnRepositories() {
        OffsetDateTime updatedAt = OffsetDateTime.parse("2024-01-01T12:00:00Z");
        GitHubApiRepository apiRepository = new GitHubApiRepository(
                123L,
                "spring-boot-example",
                "Example project",
                new GitHubOwner("user123"),
                "Java",
                450,
                120,
                updatedAt
        );
        GitHubApiResponse apiResponse = new GitHubApiResponse(List.of(apiRepository));

        GitHubRepository savedEntity = new GitHubRepository(
                123L,
                "spring-boot-example",
                "Example project",
                "user123",
                "Java",
                450,
                120,
                updatedAt
        );

        when(client.searchRepository("spring boot", "Java", "stars"))
                .thenReturn(apiResponse);
        when(repository.saveAll(any())).thenReturn(List.of(savedEntity));

        List<GitHubRepository> result =
                service.searchRepository("spring boot", "Java", "stars");

        assertEquals(1, result.size());
        assertSame(savedEntity, result.getFirst());

        verify(client).searchRepository("spring boot", "Java", "stars");
        verify(repository).saveAll(argThat((Iterable<GitHubRepository> entities) -> {
            List<GitHubRepository> actualEntities = new ArrayList<>();
            entities.forEach(actualEntities::add);

            if (actualEntities.size() != 1) {
                return false;
            }

            GitHubRepository actual = actualEntities.getFirst();
            return Objects.equals(123L, actual.getId())
                    && Objects.equals("spring-boot-example", actual.getName())
                    && Objects.equals("Example project", actual.getDescription())
                    && Objects.equals("user123", actual.getOwner())
                    && Objects.equals("Java", actual.getLanguage())
                    && Objects.equals(450, actual.getStars())
                    && Objects.equals(120, actual.getForks())
                    && Objects.equals(updatedAt, actual.getLastUpdated());
        }));
        verifyNoMoreInteractions(client, repository);
    }

    @Test
    void searchRepository_returnEmptyListWhenNoRepositoriesFound() {
        when(client.searchRepository("unknown-project", null, "stars"))
                .thenReturn(new GitHubApiResponse(List.of()));
        when(repository.saveAll(any())).thenReturn(List.of());

        List<GitHubRepository> result =
                service.searchRepository("unknown-project", null, "stars");

        assertTrue(result.isEmpty());

        verify(client).searchRepository("unknown-project", null, "stars");
        verify(repository).saveAll(argThat((Iterable<GitHubRepository> entities) ->
                !entities.iterator().hasNext()));
        verifyNoMoreInteractions(client, repository);
    }

    @Test
    void searchRepository_propagateGitHubApiException() {
        GitHubApiException apiException = new GitHubApiException(
                "Unable to connect to GitHub API",
                HttpStatus.SERVICE_UNAVAILABLE
        );
        when(client.searchRepository("spring boot", "Java", "stars"))
                .thenThrow(apiException);

        GitHubApiException thrown = assertThrows(
                GitHubApiException.class,
                () -> service.searchRepository("spring boot", "Java", "stars")
        );

        assertSame(apiException, thrown);
        verify(client).searchRepository("spring boot", "Java", "stars");
        verifyNoMoreInteractions(client);
        verifyNoInteractions(repository);
    }

    @Test
    void fetchRepository_applyAllProvidedParameters() {
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "forks");
        List<GitHubRepository> expectedRepositories = List.of(
                createRepository(1L, "spring-boot", "Java", 500, 200)
        );
        when(repository.findByLanguageIgnoreCaseAndStarsGreaterThanEqual(
                "Java", 100, expectedSort))
                .thenReturn(expectedRepositories);

        List<GitHubRepository> result =
                service.fetchRepository("Java", 100, "forks");

        assertEquals(1, result.size());
        assertEquals("spring-boot", result.getFirst().getName());
        verify(repository).findByLanguageIgnoreCaseAndStarsGreaterThanEqual(
                "Java", 100, expectedSort);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(client);
    }

    @Test
    void fetchRepository_defaultToStarsWhenNoParametersProvided() {
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "stars");
        List<GitHubRepository> expectedRepositories = List.of(
                createRepository(1L, "repo-a", "Java", 500, 100),
                createRepository(2L, "repo-b", "Python", 300, 50)
        );
        when(repository.findAll(expectedSort)).thenReturn(expectedRepositories);

        List<GitHubRepository> result = service.fetchRepository(null, null, null);

        assertEquals(2, result.size());
        assertEquals("repo-a", result.get(0).getName());
        assertEquals("repo-b", result.get(1).getName());
        verify(repository).findAll(expectedSort);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(client);
    }

    @Test
    void fetchRepository_filterByLanguageAndDefaultToStars() {
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "stars");
        List<GitHubRepository> expectedRepositories = List.of(
                createRepository(1L, "spring-app", "Java", 500, 100)
        );
        when(repository.findByLanguageIgnoreCase("Java", expectedSort))
                .thenReturn(expectedRepositories);

        List<GitHubRepository> result = service.fetchRepository("Java", null, null);

        assertEquals(1, result.size());
        assertEquals("spring-app", result.getFirst().getName());
        verify(repository).findByLanguageIgnoreCase("Java", expectedSort);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(client);
    }

    @Test
    void fetchRepository_filterByMinimumStarsAndDefaultToStars() {
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "stars");
        List<GitHubRepository> expectedRepositories = List.of(
                createRepository(1L, "popular-repo", "Java", 500, 100)
        );
        when(repository.findByStarsGreaterThanEqual(100, expectedSort))
                .thenReturn(expectedRepositories);

        List<GitHubRepository> result = service.fetchRepository(null, 100, null);

        assertEquals(1, result.size());
        assertEquals("popular-repo", result.getFirst().getName());
        verify(repository).findByStarsGreaterThanEqual(100, expectedSort);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(client);
    }

    @Test
    void fetchRepository_applyLanguageAndMinimumStarsTogether() {
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "stars");
        List<GitHubRepository> expectedRepositories = List.of(
                createRepository(1L, "spring-app", "Java", 500, 100)
        );
        when(repository.findByLanguageIgnoreCaseAndStarsGreaterThanEqual(
                "Java", 100, expectedSort))
                .thenReturn(expectedRepositories);

        List<GitHubRepository> result = service.fetchRepository("Java", 100, null);

        assertEquals(1, result.size());
        assertEquals("spring-app", result.getFirst().getName());
        verify(repository).findByLanguageIgnoreCaseAndStarsGreaterThanEqual(
                "Java", 100, expectedSort);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(client);
    }

    @Test
    void fetchRepository_defaultToStarsWhenSortIsBlank() {
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "stars");
        List<GitHubRepository> expectedRepositories = List.of(
                createRepository(1L, "repo-a", "Java", 500, 100)
        );
        when(repository.findAll(expectedSort)).thenReturn(expectedRepositories);

        List<GitHubRepository> result = service.fetchRepository(null, null, "   ");

        assertEquals(1, result.size());
        assertEquals("repo-a", result.getFirst().getName());
        verify(repository).findAll(expectedSort);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(client);
    }

    @Test
    void fetchRepository_treatBlankLanguageAsNoLanguageFilter() {
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "stars");
        List<GitHubRepository> expectedRepositories = List.of(
                createRepository(1L, "repo-a", "Java", 500, 100)
        );
        when(repository.findAll(expectedSort)).thenReturn(expectedRepositories);

        List<GitHubRepository> result = service.fetchRepository("   ", null, null);

        assertEquals(1, result.size());
        assertEquals("repo-a", result.getFirst().getName());
        verify(repository).findAll(expectedSort);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(client);
    }

    @Test
    void fetchRepository_sortByForksWhenRequested() {
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "forks");
        List<GitHubRepository> expectedRepositories = List.of(
                createRepository(1L, "forked-repo", "Java", 200, 500)
        );
        when(repository.findAll(expectedSort)).thenReturn(expectedRepositories);

        List<GitHubRepository> result = service.fetchRepository(null, null, "forks");

        assertEquals(1, result.size());
        assertEquals("forked-repo", result.getFirst().getName());
        verify(repository).findAll(expectedSort);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(client);
    }

    @Test
    void fetchRepository_sortByLastUpdatedWhenRequested() {
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "lastUpdated");
        List<GitHubRepository> expectedRepositories = List.of(
                createRepository(1L, "recent-repo", "Java", 200, 100)
        );
        when(repository.findAll(expectedSort)).thenReturn(expectedRepositories);

        List<GitHubRepository> result = service.fetchRepository(null, null, "updated");

        assertEquals(1, result.size());
        assertEquals("recent-repo", result.getFirst().getName());
        verify(repository).findAll(expectedSort);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(client);
    }

    @Test
    void fetchRepository_acceptUppercaseSortOption() {
        Sort expectedSort = Sort.by(Sort.Direction.DESC, "forks");
        List<GitHubRepository> expectedRepositories = List.of(
                createRepository(1L, "forked-repo", "Java", 200, 500)
        );
        when(repository.findAll(expectedSort)).thenReturn(expectedRepositories);

        List<GitHubRepository> result = service.fetchRepository(null, null, "FORKS");

        assertEquals(1, result.size());
        assertEquals("forked-repo", result.getFirst().getName());
        verify(repository).findAll(expectedSort);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(client);
    }

    @Test
    void fetchRepository_rejectInvalidSortOption() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.fetchRepository(null, null, "random")
        );

        assertEquals("Invalid sort option: random", exception.getMessage());
        verifyNoInteractions(repository, client);
    }

    private GitHubRepository createRepository(
            Long id,
            String name,
            String language,
            Integer stars,
            Integer forks
    ) {
        return new GitHubRepository(
                id,
                name,
                "Test description",
                "test-owner",
                language,
                stars,
                forks,
                OffsetDateTime.parse("2024-01-01T12:00:00Z")
        );
    }

}
