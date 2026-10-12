package com.freightfox.github_searcher.repository;

import com.freightfox.github_searcher.entities.GitHubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitHubRepositoryRepo extends JpaRepository<GitHubRepository, Long> {

}
