# LogosFlow
The project for management, interaction and collaboration

## Deployment (Java)

To install Java applications (in project directory) run command:
```shell
make mvn/rebuild-all
```

## Local Development (Docker)

The local environment is managed via `make` and is split into three Docker Compose stacks:
- **Infrastructure (`infra`)**: Contains core services like Keycloak and its database.
- **LLM Services (`llm`)**: Contains the AI backend, including Ollama, LiteLLM, MCPO, and related tools.
- **UI Services (`ui`)**: Contains the user-facing components, including OpenWebUI and the Traefik reverse proxy.

### Prerequisites

- Docker and Docker Compose
- `make`

### Quick Start

To start the entire application stack (infra, llm, and ui), run:
```shell
make up
```
This will build the necessary images and start all services in the background. The first time you run this, the `model_prep` service will download the required LLM models, which may take some time.

### Available Commands

The main `Makefile` provides a set of commands to manage the environment. Run `make help` to see all available commands.

#### Full Stack Management
- `make up`: Start all services.
- `make down`: Stop all services.
- `make restart`: Restart all services.
- `make logs`: View logs for all services.
- `make ps`: Show the status of all running containers.

#### Individual Stack Management
- `make infra-up`, `make infra-down`, `make infra-logs`
- `make llm-up`, `make llm-down`, `make llm-logs`
- `make ui-up`, `make ui-down`, `make ui-logs`

#### Status & Cleanup
- `make status`: Show the status of running containers and the models available in LiteLLM and Ollama.
- `make clean`: Stop all services, prune the Docker system, and remove project-specific Docker volumes.

### Accessing Services

- **OpenWebUI**: [http://localhost:3000](http://localhost:3000)
- **Traefik Dashboard**: [http://localhost:8080](http://localhost:8080)
- **LiteLLM API**: [http://localhost:4000](http://localhost:4000)
- **Ollama API**: [http://localhost:11434](http://localhost:11434)
- **Keycloak Admin Console**: [http://localhost:49088/admin/](http://localhost:49088/admin/)

## Start Java applications:

in order to start each application (for instance, logosflow-eureka-discovery-service,  logosflow-gateway ect):

```shell
java -jar logosflow-eureka-discovery-service/target/logosflow-eureka-discovery-service.jar
```

or using maven (in directory of corresponding pom.xml):

```shell
mvn spring-boot:run
```


## Keycloak

Keycloak is an authorization provider (Server) that implements the OAuth2 and OpenID Connect protocols. It manages
software clients, users and their roles and claims for LogosFlow

### Administrators console

#### Administrators console for master (main) Realm.

```shell
http://localhost:49088/admin/master/console/
```

#### Administrators console for logosflow realm.

```shell
http://localhost:49088/admin/logosflow/console/
```

### Keycloak User-password information:

| User         |  Password  | Description                                               |
|--------------|:----------:|:----------------------------------------------------------|
| `logosadmin` | logosadmin | Superuser for "master" realm                              |
| `logosflow`  | logosflow  | Superuser for "logosflow" realm                           |
| `logosuser`  | logosuser  | Authorized user for "logosflow" realm                     |
| `logosguest` | logosguest | Authenticated user (without rights) for "logosflow" realm |

### Keycloak OAuth2 Endpoints and OAuth2 Server information:

```shell
http://localhost:49088/realms/logosflow/.well-known/openid-configuration
```

### Export new Keycloak settings (after changing in UI):
```shell
docker-compose exec /opt/keycloak/bin/kc.sh export --dir /opt/keycloak/data/import --realm logosflow
```

### Request OAuth2 Token (Public client):

#### 1. Request OAuth2 token through grant_type password (used only for developing, later this option will be deleted):

```shell
curl --location 'http://localhost:49088/realms/logosflow/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'username=logosflow' \
--data-urlencode 'password=logosflow' \
--data-urlencode 'grant_type=password' \
--data-urlencode 'client_id=logosflow-frontend-public'
```

or:
```shell
POST /realms/logosflow/protocol/openid-connect/token HTTP/1.1
Host: localhost:49088
Content-Type: application/x-www-form-urlencoded
Content-Length: 93

username=logosflow&password=logosflow&grant_type=password&client_id=logosflow-frontend-public
```

#### 2. Request OAuth2 token through grant_type password (used only for developing, later this option will be deleted):

- Requesting Auth Code (should be in browser)
```shell
curl --location 'http://localhost:49088/realms/logosflow/protocol/openid-connect/auth?/
response_type=code&/
state=hv8hf0h2i7X&/
redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fcallback&/
scope=openid&/
client_id=logosflow-frontend-public&/
code_challenge=LSykZkRCyzIw6jpFUPD1bL0AmCR2P4phNqXqVPPi36A&/
code_challenge_method=S256
```

or:

```shell
GET /realms/logosflow/protocol/openid-connect/auth?response_type=code&state=hv8hf0h2i7X&redirect_uri=http://localhost:8080/callback&scope=openid&client_id=logosflow-frontend-public&code_challenge=LSykZkRCyzIw6jpFUPD1bL0AmCR2P4phNqXqVPPi36A&code_challenge_method=S256 HTTP/1.1
Host: localhost:49088
```
- Exchanging code on Token:

```shell
curl --location 'http://localhost:49088/realms/logosflow/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'client_id=logosflow-frontend-public' \
--data-urlencode 'code=someCodeValue' \
--data-urlencode 'code_verifier= someCodeVerifierValue' \
--data-urlencode 'grant_type=authorization_code' \
--data-urlencode 'redirect_uri=http://localhost:8080/callback'
```

or

```shell
POST /realms/logosflow/protocol/openid-connect/token HTTP/1.1
Host: localhost:49088
Content-Type: application/x-www-form-urlencoded
Content-Length: 378

client_id=logosflow-frontend-public&/
code=someCodeValue&code_verifier=someCodeVerifier&/
grant_type=authorization_code&/
redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fcallback
```

where `code_challenge` and `code_verifier`generated with utils corresponding to [rfc7636 Creates a Code Verifier and Code Challenge](https://www.rfc-editor.org/rfc/rfc7636#page-8).
For develop aims we can generate `code_challenge` and `code_verifier` with [web pkce generator](https://tonyxu-io.github.io/pkce-generator/).
