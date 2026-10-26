# Task Manager

A Java 17 / Spring Boot 3.5.16 learning project for Vrund Patel. Sign in, create tasks, edit notes, mark tasks complete, and delete them. The same features are exposed through a session-authenticated REST API.

## Run the ready-built app on macOS

1. Extract this ZIP into a folder, for example `~/Downloads/task-manager`.
2. Check Java in Terminal: `java -version`. Java 17 or newer is required. A supported Java 17 distribution is sufficient; Maven is only needed to change and rebuild the code.
3. Open Terminal in the extracted `task-manager` folder and run:

```bash
bash start.sh
```

The launcher asks for a username and a password without displaying the password. Choose 3-64 letters, digits, underscores, dots, or hyphens for the username. Passwords must have at least 12 characters and at most 72 UTF-8 bytes. No default account or password is shipped.

4. Keep Terminal open. Visit http://localhost:8080 and sign in with those credentials.
5. Press Control+C in Terminal to stop the app.

Tasks and password hashes are saved under `data/` in the project folder and survive restarts. Use the same folder on future runs. The launcher always changes into its own folder so the database location stays consistent. Back up the folder while the app is stopped if you want to preserve your tasks.

The bootstrap account is created only when its username is new. Entering another password on a later launch does **not** reset an existing account's password. Enter the original password to sign in. Starting with a new username creates a separate account with its own tasks. This starter has no password-reset or account-management screen.

If port 8080 is busy: `PORT=8081 bash start.sh`, then open http://localhost:8081.

You can also launch directly from the project folder:

```bash
export APP_USER=vrund
read -r -s -p 'Choose your password: ' APP_PASSWORD
printf '\n'
export APP_PASSWORD
java -jar task-manager.jar
unset APP_PASSWORD
```

## Build and test

Install Java 17+ and Maven 3.6.3+, then run from this folder:

```bash
mvn verify
```

The executable built from your source is `target/task-manager-1.0.0.jar`. Copy it to `task-manager.jar` to use your new build with `start.sh`. Tests use an isolated in-memory database. Coverage output is under `target/site/jacoco/index.html` after verification.

## Structure

- `TaskController`: HTTP endpoints and request validation.
- `TaskService`: transactions and ownership checks.
- `TaskRepository`: parameterized Spring Data JPA queries.
- `TaskInput` / `TaskView`: request and response records, separate from database entities.
- `SecurityConfig`: database-backed login, BCrypt hashing, CSRF protection, and security headers.
- `templates/`: server-rendered login and task pages.
- `static/`: browser JavaScript and CSS. User-provided strings are inserted with `textContent`, not HTML.
- `TaskApiTest`: end-to-end MVC, security, validation, and persistence tests.

## REST API

All endpoints require an authenticated browser session. Unauthenticated API reads return 401. Mutations also need a valid CSRF token. In browser developer tools, inspect a request made by the task page to see the cookie and CSRF header. Do not disable CSRF to simplify API calls.

| Method | Path | Result |
|---|---|---|
| GET | `/api/tasks?page=0&size=20` | Current user's tasks, newest first |
| GET | `/api/tasks/{id}` | One owned task, or 404 |
| POST | `/api/tasks` | Create; 201 with a Location header |
| PUT | `/api/tasks/{id}` | Replace title, notes, and completion flag |
| DELETE | `/api/tasks/{id}` | Delete; 204 |

POST and PUT JSON:

```json
{"title":"Learn Spring Boot","description":"Finish the first controller","completed":false}
```

Title is required and limited to 140 characters; notes are optional and limited to 2,000 characters; `completed` is required. Pagination starts at zero; page size must be 1-100. Attempts to read, change, or delete another account's task return 404. Responses never expose password hashes or task owners.

## ECC workflows used

This project follows selected guidance from [affaan-m/ECC](https://github.com/affaan-m/ECC):

- [springboot-patterns](https://github.com/affaan-m/ECC/blob/main/skills/springboot-patterns/SKILL.md): controller/service/repository separation, DTO validation, and transactional services.
- [springboot-tdd](https://github.com/affaan-m/ECC/blob/main/skills/springboot-tdd/SKILL.md): tests before implementation, MockMvc integration checks, and JaCoCo reporting.
- [springboot-security](https://github.com/affaan-m/ECC/blob/main/skills/springboot-security/SKILL.md): password hashing, task ownership, CSRF, and security headers.

The guidance was reviewed and applied to this project. ECC itself is not installed, and no Claude plugin commands, hooks, or background agents are included. No ECC code or skill files are redistributed in this package. This project uses the session-authentication option appropriate to its browser interface.

## Scope

This is a local learning app. It listens on 127.0.0.1, uses embedded H2, and has no public registration, email, JWT, or cloud dependency. It is not a hosted website. A production release would need HTTPS with secure cookies (`COOKIE_SECURE=true`), database migrations and backups, account recovery, login rate limiting, and a dependency vulnerability review. Those deployment checks have not been performed.

The initial database schema uses Hibernate `update` for convenience. Concurrent edits use last-write-wins behavior. H2's file database is intended for a single running app instance.
