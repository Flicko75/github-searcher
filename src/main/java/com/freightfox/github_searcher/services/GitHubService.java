package com.freightfox.github_searcher.services;

import com.freightfox.github_searcher.client.GitHubClient;
import com.freightfox.github_searcher.dto.GitHubApiRepository;
import com.freightfox.github_searcher.dto.GitHubApiResponse;
import com.freightfox.github_searcher.entities.GithubRepository;
import com.freightfox.github_searcher.repository.GithubRepositoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GitHubService {

    private final GitHubClient client;

    private final GithubRepositoryRepo repository;

    public List<GithubRepository> searchRepository(
            String entity,
            String language,
            String sort
    ) {
        GitHubApiResponse response =
                client.searchRepository(entity, language, sort);

        List<GitHubApiRepository> repositories = response.items();
        List<GithubRepository> entities = new ArrayList<>();

        repositories.forEach(repository -> {
            entities.add(toEntity(repository));
        });

        List<GithubRepository> savedRepositories = repository.saveAll(entities);

        return savedRepositories;
    }

    private GithubRepository toEntity(GitHubApiRepository dto) {
        return new GithubRepository(
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
