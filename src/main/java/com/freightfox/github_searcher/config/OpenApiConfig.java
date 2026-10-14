package com.freightfox.github_searcher.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI githubSearcherOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GitHub Repository Searcher API")
                        .version("1.0.0")
                        .description("""
                                Search GitHub repositories by keyword and save the results to PostgreSQL. Existing repositories are updated when fetched again.
                                Saved repositories can be filtered by programming language or minimum stars, and sorted by stars, forks, or last-updated time.
                                """));
    }
}
