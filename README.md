# phase0-vib

Phase 0 scaffold: two entities, plain CRUD, one foreign-key relation. No auth, no
capabilities, no search — deliberately.

## Stack

| | |
|---|---|
| Java | 21 (toolchain target) |
| Spring Boot | 3.5.6 |
| Persistence | Spring Data JPA + Hibernate |
| Database | H2 in-memory (`MODE=PostgreSQL`) |
| Build | Maven |

## Run

```bash
mvn spring-boot:run          # http://localhost:8080
mvn test
mvn clean package            # executable jar in target/
```

H2 console: <http://localhost:8080/h2-console>.

## Domain

```
Client 1 ──── * Campaign
```

**Client** — `id`, `companyName`, `contactName`, `contactEmail` (unique), `industry`,
`status` (`ACTIVE` | `INACTIVE`)

**Campaign** — `id`, `clientId` (FK → Client, required), `name`, `targetCriteria`
(free-text description of who the client wants to reach), `status` (`DRAFT` | `ACTIVE`)

The FK is mapped as a lazy `@ManyToOne` on `Campaign`; the wire format exposes it as a
flat `clientId` so responses never leak the entity graph.

## API

Both resources expose the same five operations.

| Method | Path | Success |
|---|---|---|
| `POST` | `/api/{clients,campaigns}` | `201` + `Location` |
| `GET` | `/api/{clients,campaigns}/{id}` | `200` |
| `GET` | `/api/{clients,campaigns}` | `200` (unpaged list) |
| `PUT` | `/api/{clients,campaigns}/{id}` | `200` (full replace) |
| `DELETE` | `/api/{clients,campaigns}/{id}` | `204` |

`DELETE /api/clients/{id}` returns `409` if any `Campaign` still references the client.
Creating or updating a `Campaign` with an unknown `clientId` returns `404`.

## Out of scope (by design — see later phases)

- RBAC / auth
- Full-text search, filtering
- Status state machines (transitions are just a stored enum value here)
- Any second entity beyond the one FK relation
