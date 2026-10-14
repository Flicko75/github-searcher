package com.freightfox.github_searcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@Schema(description = "Structured error response")
public record ApiErrorResponse(
        @Schema(description = "Time the error occurred", example = "2026-10-14T12:00:00+05:30")
        OffsetDateTime timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "HTTP status text", example = "Bad Request")
        String error,

        @Schema(description = "Explanation of the error", example = "Query must not be blank.")
        String message,

        @Schema(description = "Request path that produced the error", example = "/api/github/search")
        String path
) {}
