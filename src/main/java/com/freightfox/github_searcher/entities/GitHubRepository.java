package com.freightfox.github_searcher.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Table(name = "github_repositories")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepository {

    @Id
    private Long id;

    private String name;

    private String description;

    private String owner;

    private String language;

    private Integer stars;

    private Integer forks;

    private OffsetDateTime lastUpdated;

}
