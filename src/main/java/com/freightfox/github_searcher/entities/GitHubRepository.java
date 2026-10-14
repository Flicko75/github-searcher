package com.freightfox.github_searcher.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
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

    @Schema(description = "GitHub repository ID", example = "123456")
    @Id
    private Long id;

    @Schema(description = "Repository name", example = "spring-boot-example")
    private String name;

    @Schema(description = "Repository description", example = "An example Spring Boot project")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Schema(description = "Repository owner's username", example = "user123")
    private String owner;

    @Schema(description = "Primary programming language", example = "Java")
    private String language;

    @Schema(description = "Number of stars", example = "450")
    private Integer stars;

    @Schema(description = "Number of forks", example = "120")
    private Integer forks;

    @Schema(description = "Timestamp of the repository's last update", example = "2026-10-01T12:00:00Z")
    private OffsetDateTime lastUpdated;

}
