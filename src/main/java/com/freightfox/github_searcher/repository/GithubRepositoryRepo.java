package com.freightfox.github_searcher.repository;

import com.freightfox.github_searcher.entities.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GithubRepositoryRepo extends JpaRepository<GithubRepository, Long> {

}
