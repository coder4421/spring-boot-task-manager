# Verification

- `mvn verify`: passed.
- JUnit / MockMvc: 12 tests, 0 failures, 0 errors, 0 skipped.
- JaCoCo application line coverage: 51 of 57 executable lines, approximately 89.5%.
- JavaScript syntax: `node --check` passed.
- Launcher syntax: `bash -n` passed.
- Packaged JAR: launched successfully with Java 17.
- Real HTTP login: passed with a database-backed account and a form CSRF token.
- Real HTTP task creation: passed with the authenticated cookie and CSRF header.
- File persistence: created a task, stopped the app, started it again against the same temporary database, signed in again, and retrieved the task successfully.

The tests were written before the application routes. The initial functional run failed, then the implementation passed all 12 tests. The test fixture uses Mockito's subclass mock maker because this execution environment blocks Java self-attachment.

Browser automation was attempted but could not run because this environment blocks a socket type Chromium requires. The rendered page layout and JavaScript interactions have not been verified in a browser. The HTTP page rendering and API flows were tested separately. No macOS-specific runtime test or dependency vulnerability scan was performed.

This is a local learning project, not a production deployment.
