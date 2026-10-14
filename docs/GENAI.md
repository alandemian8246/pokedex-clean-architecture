# GenAI exercise: Task management API

This answers the brief's "Generative AI tools" section. Tool: **Claude Code** (CLI agent). The same workflow
(scope the prompt, generate, verify independently, correct) was used while building this repository.

---

## 1. The prompt

A good prompt states the stack, the constraints that matter to reviewers, and how the result will be verified.
Ambiguity ("make a task API") produces generic code; explicit non-functional requirements produce reviewable code.

```text
You are working in an empty Spring Boot 3.5 / Java 21 / Maven project.

Build a RESTful API for task management.

Domain
- Task: id, title (required, 1-120 chars), description (optional, max 2000),
  status (TODO | IN_PROGRESS | DONE), due_date (ISO date, optional, not in the past on create),
  owner (the authenticated user), created_at, updated_at, version (optimistic locking).
- Assume a User entity exists (id, username, password_hash). Do not build registration.

API (JSON, base path /api/tasks, all endpoints require a JWT bearer token)
- POST   /api/tasks            -> 201 + Location header
- GET    /api/tasks            -> paginated (page, size<=100), filter by status, sort by due_date
- GET    /api/tasks/{id}       -> 200 | 404
- PUT    /api/tasks/{id}       -> full update, requires "version", 409 on stale version
- PATCH  /api/tasks/{id}/status -> change status only
- DELETE /api/tasks/{id}       -> 204
- A user may only see and modify their own tasks. Another user's task must return 404 (not 403),
  so ids are not enumerable.

Architecture and quality
- Layers: web (controllers + DTOs) -> application (use cases) -> domain (entities, rules) <- persistence adapter.
  Never return JPA entities from controllers.
- Constructor injection only. No Lombok.
- Errors as RFC 9457 ProblemDetail with an "errors" map for field validation; never leak stack traces.
- Flyway migration for the schema (PostgreSQL), H2 for tests.

Tests (write them first, then the implementation)
- Unit tests for domain rules and use cases (Mockito).
- @WebMvcTest per controller covering 201/200/400/401/404/409.
- One @DataJpaTest proving ownership filtering and optimistic locking.

Output the file tree first, then each file. Stop and list any assumption you had to make.
```

Why it is written this way:

| Prompt element | Reason |
|---|---|
| Exact versions and build tool | Prevents outdated APIs (`WebSecurityConfigurerAdapter`, `javax.*`). |
| Field constraints spelled out | Validation becomes testable instead of guessed. |
| Status codes per endpoint | Fixes the HTTP contract up front. |
| "404, not 403, for another user's task" | Forces an explicit decision on the most commonly missed security bug (IDOR). |
| "Never return JPA entities" | Avoids leaking `owner.passwordHash` and lazy-loading exceptions. |
| Tests first | Gives an objective acceptance check for the generated code. |
| "List any assumption" | Makes the model surface decisions instead of hiding them. |

---

## 2. Representative output (after review)

```java
// domain
public enum TaskStatus { TODO, IN_PROGRESS, DONE }

public final class Task {
    private final Long id;
    private final long ownerId;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;
    private final Long version;

    public static Task create(long ownerId, String title, String description, LocalDate dueDate, Clock clock) {
        Task task = new Task(null, ownerId, title, description, TaskStatus.TODO, dueDate, null);
        if (dueDate != null && dueDate.isBefore(LocalDate.now(clock))) {
            throw ValidationException.of("dueDate", "must not be in the past");
        }
        return task;
    }

    public void changeStatus(TaskStatus next) {
        if (status == TaskStatus.DONE && next == TaskStatus.TODO) {
            throw new ConflictException("A finished task cannot go back to TODO");
        }
        this.status = next;
    }
    // constructor validates title (1-120) and description (<= 2000)
}
```

```java
// application
public class UpdateTaskUseCase {
    private final TaskRepository tasks;

    public UpdateTaskUseCase(TaskRepository tasks) { this.tasks = tasks; }

    public Task execute(long currentUserId, UpdateTaskCommand command) {
        Task task = tasks.findByIdAndOwnerId(command.id(), currentUserId)          // ownership in the query
                .orElseThrow(() -> new NotFoundException("Task", command.id())); // 404, not 403
        task.ensureVersion(command.version());                                    // 409 on stale data
        task.update(command.title(), command.description(), command.status(), command.dueDate());
        return tasks.save(task);
    }
}
```

```java
// web
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable long id,
                               @Valid @RequestBody UpdateTaskRequest request,
                               @AuthenticationPrincipal Jwt jwt) {
        long userId = Long.parseLong(jwt.getSubject());
        return TaskResponse.from(updateTask.execute(userId, request.toCommand(id)));
    }
}

public record UpdateTaskRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 2000) String description,
        @NotNull TaskStatus status,
        LocalDate dueDate,
        @NotNull Long version) { ... }
```

---

## 3. How the output was validated

1. **Compile and run the tests.** The generated tests must pass, but passing tests only prove the code matches
   the tests. Next I read each test to confirm it asserts behaviour (status code and body), not just "no exception".
2. **Add adversarial tests by hand** for what models typically miss:
   - user B requests user A's task → must be 404;
   - `PUT` with a stale `version` → 409;
   - malformed JSON, wrong types, `size=1000`, negative ids → 400 with a ProblemDetail body;
   - missing or expired token → 401.
3. **Read the diff like a code review**, focusing on security and data integrity before style.
4. **Run it**: start the app, exercise endpoints with curl, and look at the actual JSON and status codes.
5. **Check dependencies**: confirm versions exist and are current (`npm view`, Maven Central metadata), since
   models often suggest outdated or non-existent versions.

## 4. What typically needs correcting

| Issue found in a first draft | Risk | Correction |
|---|---|---|
| `findById(id)` followed by an `if (!task.getOwner().equals(user)) throw new AccessDeniedException()` | Returns 403, which confirms the id exists; easy to forget on new endpoints | Ownership in the repository query (`findByIdAndOwnerId`), 404 everywhere |
| Controller returning the `Task` entity | Serializes `owner` including the password hash; lazy-loading errors | Response DTO records |
| `status` as a free `String` | Invalid states persisted | `enum` plus domain transition rule |
| No `@Version` | Lost updates under concurrency | Optimistic locking, 409 mapped in the global handler |
| `catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }` | Leaks internals, wrong status codes | `@RestControllerAdvice` with typed exceptions and a generic 500 |
| Unbounded `findAll()` | Memory and latency blow-up | Pageable with a maximum page size |
| Field injection with `@Autowired` | Hard to unit test | Constructor injection |
| Deprecated security config | Does not compile on Spring Security 6 | `SecurityFilterChain` bean, stateless JWT resource server |

## 5. Edge cases, authentication and validation

- **Authentication:** stateless JWT (HS256) validated by Spring's resource server. The user id comes from the
  token subject and never from the request body, so a client cannot act as another user.
- **Authorization:** ownership is enforced in the data-access query, the single choke point for every endpoint.
- **Validation:** Bean Validation for request shape (400 before any business logic), domain rules for
  invariants (title length, due date not in the past, allowed status transitions), and the database
  (NOT NULL, CHECK, FK) as the last line of defence.
- **Edge cases covered by tests:** empty or whitespace title, title at exactly 120 and 121 characters,
  due date today versus yesterday (fixed `Clock`), concurrent update (two versions), deleting an already
  deleted task (404), pagination past the last page (empty page, not an error).

## 6. How GenAI was used in this repository

- **Generation:** layer scaffolding, test skeletons, PokeAPI DTO records, and repetitive mapping and response code.
- **Kept under my control:** architecture boundaries, the HTTP contract and status codes, the security rules,
  and what each test must prove.
- **Verified by running, not by trusting:**
  - the full test suite;
  - manual calls against the live PokeAPI, which revealed that evolution details are now listed per game
    version with an `is_default` flag, and led to a mapper fix plus a regression test;
  - a headless-browser walkthrough of every user story to confirm the UI works and the console stays clean.
