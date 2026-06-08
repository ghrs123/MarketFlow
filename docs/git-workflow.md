# Git Workflow

## Branch naming

Each phase or feature must use the branch convention:

```text
feature/NN-slug
```

Examples:

```text
feature/01-foundation-order-api
feature/02-persistence-jpa-transactions
feature/03-testing-quality-api-docs
```

## Branch lifecycle

The expected lifecycle is:

```text
feature branch -> pull request -> main
```

Rules:

- create the branch from current `main`
- keep the branch scoped to one complete vertical slice
- merge only when the phase is demonstrable and documented

## Conventional Commits

Format:

```text
type(scope): description
```

Common types:

- `feat`
- `fix`
- `refactor`
- `test`
- `docs`
- `chore`
- `perf`
- `build`
- `ci`

Examples:

```text
feat(openapi): add Swagger UI and OpenAPI metadata
test(order): add standalone MockMvc controller tests
test(order): add PostgreSQL repository integration coverage
ci(build): add GitHub Actions build-and-test workflow
docs(phase-03): add testing strategy and workflow documentation
```

## Pull request process

Every PR should follow this flow:

1. `plan`: define objective, scope, endpoints, tests, risks, and definition of done.
2. `implement`: build the phase incrementally, keeping the branch working.
3. `review`: validate architecture, tests, documentation, and project rules.
4. `merge`: merge only after CI passes and the phase can be demonstrated end-to-end.

## Rebase main

Update your branch from `main` with a linear history:

```bash
git fetch origin
git switch main
git pull origin main
git switch feature/03-testing-quality-api-docs
git rebase main
```

If conflicts appear:

1. resolve the files manually
2. run the relevant tests
3. continue the rebase

```bash
git add <resolved-files>
git rebase --continue
```

## What blocks merge

Merge should be blocked when any of the following is true:

- branch name does not follow `feature/NN-slug`
- commit messages do not follow Conventional Commits
- `mvn test` fails
- `mvn package` fails
- CI is red
- documentation is missing or outdated
- the feature is incomplete or not demonstrable
- secrets or internal-only values were committed
- error handling leaks internal details to clients
