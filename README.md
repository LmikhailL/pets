# pets

A small Spring Boot REST service that returns a list of pets, used as a playground for a Jenkins CI/CD pipeline
(Spotless, tests, SonarCloud, Nexus, deployment to Vagrant boxes).

## Tech stack

- Java 25
- Spring Boot 4 (Web MVC)
- Maven (wrapper included)
- JaCoCo, Spotless (Google Java Format), SonarCloud

## API

| Method | Path    | Description       |
|--------|---------|-------------------|
| GET    | `/pets` | List all the pets |

```bash
curl http://localhost:8080/pets
```

```json
[
  { "petName": "kiki", "petType": "cat" },
  { "petName": "rex", "petType": "dog" },
  ...
]
```

## Build and run

```bash
./mvnw spring-boot:run                       # run locally on port 8080
./mvnw test                                  # unit tests
./mvnw verify                                # unit + integration tests, JaCoCo report, Spotless check
./mvnw verify -DskipUTs=true                 # integration tests only
./mvnw package -DskipUTs=true -DskipITs=true # build target/app-<commit>.jar
./mvnw spotless:apply                        # fix formatting
```

The jar is named after the abbreviated git commit id (`app-<commit>.jar`), so the build must run inside a git repository.

## CI/CD

Jenkins and Nexus run locally via Docker Compose:

```bash
docker compose up -d
```

- Jenkins: http://localhost:8080
- Nexus: http://localhost:8081 (raw repository `pets`)

### `Jenkinsfile` — build pipeline

Compile → Spotless → Unit Tests → Integration Tests → SonarCloud → Build → Upload to Nexus → Deploy to dev.

### `Jenkinsfile.promote` — promotion pipeline

Deploys an existing jar from Nexus to another environment. Run it in Jenkins with **Build with Parameters**:

- `ARTIFACT` — jar name in Nexus, e.g. `app-1b021cf.jar`
- `ENV` — `test`, `uat` or `dev`

### Environments

Each environment is a Vagrant box reached from Jenkins over SSH via `host.docker.internal`:

| Env  | Jenkins SSH credential | SSH port |
|------|------------------------|----------|
| dev  | `vagrant-dev`          | 2250     |
| test | `vagrant-test`         | 2249     |
| uat  | `vagrant-uat`          | 2248     |

Deployment stops the running app, uploads the jar as `app.jar` and starts it in the background (logs in `app.log`).

### Required Jenkins setup

- Tools: Maven `mvn3.9`, JDK `jdk25`
- Credentials: `nexus` (username/password), `vagrant-dev`, `vagrant-test`, `vagrant-uat` (SSH private keys)
- SonarQube server named `SonarCloud`

tst
