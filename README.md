# GitHub Repository Searcher

A Spring Boot REST API that searches GitHub repositories, saves their details to PostgreSQL, and lets clients retrieve, filter, and sort the saved data.

## Features

- Search repositories by keyword and optional programming language.
- Save repository details returned by the GitHub API.
- Update existing records when a repository ID is already in the database.
- Filter saved repositories by language and minimum stars.
- Sort results by stars, forks, or last-updated time.
- Validate requests and return structured error responses.
- Handle GitHub API failures, rate limits, and connection errors.

## Tech Stack

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA and Hibernate
- PostgreSQL
- GitHub REST API
- Maven
- JUnit, Mockito, and Spring Boot Test
- Testcontainers

## Prerequisites

- JDK 21
- PostgreSQL
- Docker Desktop for integration tests
- Git
- Internet access to the GitHub API

## Configuration

Set the following environment variables before running the application.

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/github_searcher` |
| `DB_USERNAME` | PostgreSQL username | `postgres` |
| `DB_PASSWORD` | PostgreSQL password | No default |

Create a PostgreSQL database named `github_searcher`. The GitHub API base URL is configured as `https://api.github.com`.


## Running the Application

Start PostgreSQL and configure the environment variables above. Then run the application from the project root.

**Windows CMD**

```cmd
mvnw.cmd spring-boot:run
```

**Linux or macOS**

```bash
./mvnw spring-boot:run
```

The API runs at `http://localhost:8080` by default.

## API Reference

### Search GitHub Repositories

**`POST /api/github/search`**

Searches GitHub and saves the returned repositories to PostgreSQL.

Request:

```json
{
  "query": "spring boot",
  "language": "Java",
  "sort": "stars"
}
```

| Field | Required | Description |
|---|---|---|
| `query` | Yes | Search keywords; cannot be blank |
| `language` | No | Programming language filter |
| `sort` | No | GitHub search sort: `stars`, `forks`, or `updated` |

Example response:

```json
{
  "message": "Repositories fetched and saved successfully",
  "repositories": [
    {
      "id": 123456,
      "name": "spring-boot-example",
      "description": "An example repository for Spring Boot",
      "owner": "user123",
      "language": "Java",
      "stars": 450,
      "forks": 120,
      "lastUpdated": "2024-01-01T12:00:00Z"
    }
  ]
}
```

The repository values above are examples, not live results.

### Retrieve Saved Repositories

**`GET /api/github/repositories`**

Returns repositories stored in PostgreSQL. This endpoint does not call GitHub.

| Parameter | Description |
|---|---|
| `language` | Filter by programming language, ignoring case |
| `minStars` | Minimum star count; must be zero or greater |
| `sort` | `stars`, `forks`, or `updated`. Defaults to `stars`. |

Results are sorted in descending order.

Examples:

Retrieve all saved repositories:

```http
GET /api/github/repositories
```

Filter by language:

```http
GET /api/github/repositories?language=Java
```

Filter by minimum stars:

```http
GET /api/github/repositories?minStars=100
```

Combine filters:

```http
GET /api/github/repositories?language=Java&minStars=100&sort=stars
```

Sort by forks or last-updated time:

```http
GET /api/github/repositories?sort=forks
GET /api/github/repositories?sort=updated
```

## Error Responses

Errors return a JSON response containing the timestamp, HTTP status, error description, message, and request path.

Example:

```json
{
  "timestamp": "2026-10-14T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Query must not be blank",
  "path": "/api/github/search"
}
```

The timestamp is illustrative. The API handles invalid requests, GitHub API errors, rate limits, and connection failures.

## Running Tests

The integration tests use Testcontainers to start PostgreSQL 16, so Docker must be running.

**Windows CMD**

```cmd
mvnw.cmd clean test
```

**Linux or macOS**

```bash
./mvnw clean test
```

## Project Structure

```text
src/
├── main/
│   ├── java/com/freightfox/github_searcher/
│   │   ├── client/
│   │   ├── config/
│   │   ├── controllers/
│   │   ├── dto/
│   │   ├── entities/
│   │   ├── exceptions/
│   │   ├── repository/
│   │   └── services/
│   └── resources/
│       └── application.yaml
└── test/
    ├── java/
    └── resources/
        └── application-test.yml
```

## How It Works

For a search request, the controller validates the input and passes it to the service. The service calls the GitHub client, maps the API response to repository entities, and saves them through Spring Data JPA.

For a retrieval request, the service queries PostgreSQL using the requested filters and sort order.

