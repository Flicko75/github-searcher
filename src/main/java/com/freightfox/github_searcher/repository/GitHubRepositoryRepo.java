package com.freightfox.github_searcher.repository;

import com.freightfox.github_searcher.entities.GitHubRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GitHubRepositoryRepo extends JpaRepository<GitHubRepository, Long> {

    List<GitHubRepository> findByLanguageIgnoreCase(String language, Sort sort);

    List<GitHubRepository> findByStarsGreaterThanEqual(Integer minStars, Sort sort);

    List<GitHubRepository> findByLanguageIgnoreCaseAndStarsGreaterThanEqual(String language, Integer minStars, Sort sort);

}
