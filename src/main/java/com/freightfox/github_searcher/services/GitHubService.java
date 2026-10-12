package com.freightfox.github_searcher.services;

import com.freightfox.github_searcher.client.GitHubClient;
import com.freightfox.github_searcher.dto.GitHubApiRepository;
import com.freightfox.github_searcher.dto.GitHubApiResponse;
import com.freightfox.github_searcher.entities.GitHubRepository;
import com.freightfox.github_searcher.repository.GitHubRepositoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GitHubService {

    private final GitHubClient client;

    private final GitHubRepositoryRepo repository;

    public List<GitHubRepository> searchRepository(
            String query,
            String language,
            String sort
    ) {
        GitHubApiResponse response =
                client.searchRepository(query, language, sort);

        List<GitHubApiRepository> repositories = response.items();
        List<GitHubRepository> entities = new ArrayList<>();

        repositories.forEach(repository -> {
            entities.add(toEntity(repository));
        });

        List<GitHubRepository> savedRepositories = repository.saveAll(entities);

        return savedRepositories;
    }

    public List<GitHubRepository> fetchRepository(
            String language,
            Integer minStars,
            String sort
    ) {
        if (sort == null || sort.isBlank()) {
            sort = "stars";
        } else {
            sort = sort.toLowerCase(Locale.ROOT);
        }

        String sortField = switch (sort) {
            case "stars" -> "stars";
            case "forks" -> "forks";
            case "updated" -> "lastUpdated";
            default -> throw new IllegalArgumentException("Invalid sort option: " + sort);
        };

        Sort ordering = Sort.by(Sort.Direction.DESC, sortField);

        if (language != null && !language.isBlank() && minStars != null) {
            return repository.findByLanguageIgnoreCaseAndStarsGreaterThanEqual(language, minStars, ordering);
        }

        if (language != null && !language.isBlank()) {
            return repository.findByLanguageIgnoreCase(language, ordering);
        }

        if (minStars != null) {
            return repository.findByStarsGreaterThanEqual(minStars, ordering);
        }

        return repository.findAll(ordering);
    }

    private GitHubRepository toEntity(GitHubApiRepository dto) {
        return new GitHubRepository(
                dto.id(),
                dto.name(),
                dto.description(),
                dto.owner().login(),
                dto.language(),
                dto.stargazersCount(),
                dto.forksCount(),
                dto.updatedAt()
        );
    }

}
