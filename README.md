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

### GitHub webhook via ngrok

Jenkins runs locally, so GitHub cannot reach it directly. [ngrok](https://ngrok.com) exposes the local Jenkins
under a public HTTPS URL, which GitHub uses to notify Jenkins about pushes and pull requests:

```bash
ngrok http 8080           # prints a public URL, e.g. https://<random>.ngrok-free.app
```

In GitHub: repository **Settings → Webhooks → Add webhook**:

- Payload URL: `https://<your-ngrok-url>/github-webhook/` (the trailing `/` is required)
- Content type: `application/json`
- Events: **Pushes** and **Pull requests**

ngrok must keep running while you want builds to start automatically. The free URL changes every time ngrok
restarts (unless you use your free static domain: `ngrok http --url=<your-domain> 8080`), so update the webhook's
Payload URL after a restart. **Recent Deliveries** on the webhook page shows whether GitHub reached Jenkins.

### `Jenkinsfile` — build pipeline

Compile → Spotless → Unit Tests → Integration Tests → SonarCloud → Build → Upload to Nexus → Deploy to dev.

### `Jenkinsfile.promote` — promotion pipeline

Deploys an existing jar from Nexus to another environment. Run it in Jenkins with **Build with Parameters**:

- `ARTIFACT` — jar name in Nexus, e.g. `app-1b021cf.jar`
- `ENV` — `test`, `uat` or `dev`

### Environments

Each environment is a Vagrant box (VirtualBox, Ubuntu 24.04) defined in the `Vagrantfile`.
On first start every box gets the Java 25 runtime installed, so it can run the jar.

| Env  | IP              | App URL                         |
|------|-----------------|---------------------------------|
| dev  | `192.168.56.11` | http://192.168.56.11:8080/pets  |
| test | `192.168.56.12` | http://192.168.56.12:8080/pets  |
| uat  | `192.168.56.13` | http://192.168.56.13:8080/pets  |

```bash
vagrant up                # create and start all boxes (or: vagrant up dev)
vagrant ssh dev           # log into a box
vagrant ssh dev -c 'tail -f app.log'   # follow app logs
vagrant ssh-config dev    # show the box's SSH port and private key
vagrant halt              # stop all boxes
vagrant destroy -f uat    # delete a box (recreate with: vagrant up uat)
```

Jenkins runs in Docker and cannot reach the `192.168.56.x` network, so it connects to the boxes over SSH
through the ports Vagrant forwards on the host (`host.docker.internal:<port>`):

| Env  | Jenkins SSH credential | SSH port |
|------|------------------------|----------|
| dev  | `vagrant-dev`          | 2250     |
| test | `vagrant-test`         | 2249     |
| uat  | `vagrant-uat`          | 2248     |

The ports are picked by Vagrant and can change after `vagrant up` / `vagrant reload`.
Check them with `vagrant ssh-config <env>` and update `Jenkinsfile` / `Jenkinsfile.promote` if they differ.

Each credential is "SSH Username with private key" with username `vagrant` and the key from
`.vagrant/machines/<env>/virtualbox/private_key`. Recreating a box generates a new key, so update the credential too.

Deployment stops the running app, uploads the jar as `app.jar` and starts it in the background (logs in `app.log`).
The app is not restarted automatically when a box reboots, so run the deployment again.

### Required Jenkins setup

- Tools: Maven `mvn3.9`, JDK `jdk25`
- Credentials: `nexus` (username/password), `vagrant-dev`, `vagrant-test`, `vagrant-uat` (SSH private keys)
- SonarQube server named `SonarCloud`
