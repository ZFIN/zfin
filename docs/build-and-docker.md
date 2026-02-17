# Build Process and Docker Architecture

## Docker Services

| Service | Image | Purpose | Ports |
|---------|-------|---------|-------|
| **base** | `ghcr.io/zfin/zfin-base` | Foundation image — Gradle 8, JDK 21, Node 18, Ant, Perl, Groovy, Bowtie | — |
| **compile** | `ghcr.io/zfin/zfin-compile` | Build container — inherits base, adds Docker CLI | — |
| **db** | `ghcr.io/zfin/zfin-db` | PostgreSQL 18 | 5432 |
| **solr** | — | Solr search engine | 8983 |
| **tomcat** | — | Tomcat 10 + JDK 21 (app server) | 8080 |
| **tomcatdebug** | — | Debug variant of Tomcat (JPDA) | 5000 |
| **httpd** | — | Apache (reverse proxy + static files) | 80/443 |
| **jenkins** | — | CI/CD server | 9499 |
| **ncbiload** | — | NCBI data loading | — |
| **blast** | — | BLAST sequence search | — |
| **mailhog** | — | Email testing | — |

## Container Volumes

Named volumes are managed by Docker. Bind mounts (prefixed with `$DOCKER_*`) are configured in `.env`.

### Shared Named Volumes

| Volume | Container Path | Used By |
|--------|---------------|---------|
| `www_data` | `/opt/zfin/www_homes/zfin.org` (TARGETROOT) | compile, db, httpd, tomcat, tomcatdebug, jenkins, certbot |
| `catalina_base` | `/opt/zfin/catalina_bases/zfin.org` (CATALINA_BASE) | compile, tomcat, tomcatdebug, jenkins, filebeat |
| `solr_data` | `/var/solr` | compile, solr, jenkins |
| `jenkins_data` | `.../server_apps/jenkins/jenkins-home` | compile, jenkins |
| `tls_certs` | `/opt/zfin/tls` | compile, httpd |
| `keystore` | `/opt/apache/apache-tomcat/conf` | compile, tomcat, tomcatdebug |
| `certbot_data` | `/etc/letsencrypt` | certbot, httpd |
| `downloads_data` | `/opt/zfin/download-files` | compile, jenkins |
| `httpd_log` | `/var/log/httpd` or `/opt/zfin/log/httpd` | httpd, compile, fail2ban, filebeat |
| `maven_cache` | `/home/gradle/.m2` | compile, jenkins, ncbiload |
| `gradle_cache` | `/home/gradle/.gradle` | compile, jenkins, ncbiload |
| `pg_data` | `/var/lib/postgresql/` | db |
| `elasticsearch_data` | `/usr/share/elasticsearch/data` | elasticsearch |
| `kibana_data` | `/usr/share/kibana/data` | kibana |
| `jbrowse_data` | `/data` | jbrowse, processgff |
| `fail2ban_config` | `/config` | fail2ban |

### Bind Mounts (from `.env`)

| Variable | Container Path | Used By |
|----------|---------------|---------|
| `$DOCKER_SOURCE_ROOTS_PATH` | `/opt/zfin/source_roots/zfin.org` (SOURCEROOT) | compile, jenkins, ncbiload |
| `$DOCKER_DB_UNLOADS_PATH` | `/opt/zfin/unloads/db` | compile |
| `$DOCKER_SOLR_UNLOADS_PATH` | `/opt/zfin/unloads/solr` | compile |
| `$DOCKER_RESEARCH_PATH` | `/mnt/research` | compile, jenkins, ncbiload |
| `$DOCKER_LOADUP_PATH` | `/opt/zfin/loadUp` | compile, httpd, tomcat, tomcatdebug, jenkins |
| `$DOCKER_DOWNLOADS_PATH` | `/opt/zfin/download-files` | compile, httpd, tomcat, tomcatdebug, jenkins |
| `$DOCKER_GFF3_PATH` | `/opt/zfin/gff3` | compile, tomcat, tomcatdebug, jenkins |
| `$DOCKER_BLASTSERVER_BLAST_DATABASE_PATH` | `/opt/zfin/blastdb` | compile, tomcat, tomcatdebug, blast, jenkins |
| `$DOCKER_ABBLAST_PATH` | `/opt/ab-blast` | compile, tomcat, tomcatdebug, blast, jenkins |
| `$DOCKER_HHATLAS_PATH` | `/opt/zfin/hh_atlas` | httpd |
| `$DOCKER_SSH_AUTH_SOCK` | `/run/host-services/ssh-auth.sock` | compile |
| `/var/run/docker.sock` | `/var/run/docker.sock` | compile |
| `~/.ssh/known_hosts` | `/home/gradle/.ssh/known_hosts` | compile |

### Key Observations

- The **compile** container has the most volume mounts — it needs access to source code, all output directories, build caches, Docker socket, and SSH for the full build pipeline.
- **TARGETROOT** (`www_data`) is shared across compile, httpd, tomcat, and jenkins so they all see the same deployed files.
- **CATALINA_BASE** is shared between compile (which writes the Tomcat config) and tomcat (which runs from it).
- **SOURCEROOT** is a bind mount (not a named volume) so it maps directly to the host filesystem for live editing.

## GoCD Deployment Pipeline

GoCD uses the following steps to build and deploy a full instance.

### 1. Build Docker Images

```bash
docker compose build base
docker compose kill compile
docker compose build compile
docker compose run --rm compile bash -lc '# init volumes for certs and zfin.properties'
docker compose run --rm --detach --name trunk-compile-run-1 compile #kick off the compile container to run in the background and be available for `docker exec...`
docker compose run --rm compile bash -lc "ant do"
docker compose build db
docker compose build solr
docker compose build tomcat
docker compose build jenkins
docker compose build httpd
```

### 2. Load Database

```bash
docker compose up --detach db
docker compose run --rm compile bash -lc "gradle loaddb && gradle make && gradle liquibasePreBuild && gradle liquibasePostBuild"
```

### 3. Load Solr

```bash
docker compose down solr
docker compose run --rm compile bash -lc "gradle getLatestSolrIndex"
docker compose up --detach solr
```

### 4. Deploy Jenkins

```bash
docker compose down jenkins
docker compose run --rm compile bash -lc "ant deploy-jobs"
docker compose run --rm compile bash -lc "ant deploy-plugins"
docker compose up --detach jenkins
```

### 5. Deploy Web App

```bash
docker compose down httpd
docker compose down tomcat
docker compose run --rm compile bash -lc "gradle make"
docker compose run --rm compile bash -lc "ant deploy-catalina-base"
docker compose run --rm compile bash -lc "ant deploy-without-tests"
docker compose up --detach httpd
docker compose up --detach tomcat
docker compose run --rm compile bash -lc "gradle test -PnonSmokeTests"
docker compose run --rm compile bash -lc "gradle test -PsmokeTests"
```

## Key Deployment Steps (Simplified)

Ignoring data loads, Docker image builds, and test runs, the essential deployment steps are:

```bash
# 1. Initialize compile container (cert setup via generate_base.sh)
docker compose run --rm --detach --name trunk-compile-run-1 compile

# 2. Compile Java source
ant do

# 3. Copy scripts, static files, and DB functions to TARGETROOT
gradle make

# 4. Create Tomcat instance directory structure
ant deploy-catalina-base

# 5. Full build + deploy to Tomcat (compile, GWT, npm, copy, restart)
ant deploy-without-tests
```

## Ant Task Dependency Trees

### `ant do` — Compile Only

```
do
├── cleanClasses          (remove old .class files)
├── compile               (javac: source/**/*.java → WEB-INF/classes/)
└── prepare-properties    (copy properties files to classes dir)
    ├── hibernate-available   (confirm hibernate.properties exists)
    └── prepare               (copy xml + properties to classes dir)

Note: loadProperties and exportProperties are custom taskdefs that
run on every ant invocation, not as target dependencies.
```

Output: compiled classes in `SOURCEROOT/home/WEB-INF/classes/`. Does **not** deploy to Tomcat.

### `ant deploy-catalina-base` — Create Tomcat Instance

Defined in `buildfiles/tomcat.xml`.

```
deploy-catalina-base
└── create-mutant-instance
    ├── create-tomcat-dirs    (create conf/, logs/, webapps/, work/, temp/, bin/, lib/)
    └── set-catalina-base-perms
```

Creates the Tomcat directory structure under `$CATALINA_BASE` (typically `/opt/zfin/catalina_bases/zfin.org/`), copies config templates (server.xml, web.xml) with token substitution for ports, database host, domain name, etc.


See [gradle-deployment.md](gradle-deployment.md) for detailed file flow schematics of the Gradle tasks.
