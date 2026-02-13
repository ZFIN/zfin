# Performance Monitoring for ZFIN

## What This Is

This adds tools to answer questions like:

- **Which pages are slow?** See a ranked list of every URL endpoint with its average, median, and 95th-percentile response time.
- **Why is a page slow?** Drill into a single request and see a waterfall timeline: how long was spent in the Java controller, in Hibernate queries, in PostgreSQL, etc.
- **Is the server healthy?** View CPU usage, memory consumption, database connection pool saturation, and Tomcat thread pool utilization over time.
- **What errors are happening?** See grouped exceptions with stack traces, frequency, and which endpoints they affect.

Everything runs inside the existing Docker environment. No code changes are needed to the ZFIN application itself beyond what's included here — the monitoring agents attach to the running Java process automatically.

## The Components

### Elasticsearch (already existed)

Elasticsearch is a search and analytics database. Think of it as a specialized database optimized for storing and querying time-series data like logs and metrics. It was already part of our Docker stack, used by Filebeat to store Apache HTTPD access logs. Now it also stores APM traces and Metricbeat metrics.

### Kibana (already existed)

Kibana is a web UI for exploring data stored in Elasticsearch. It provides dashboards, charts, and — most importantly for us — a built-in **APM UI** that visualizes application performance data without any dashboard setup required. Access it at `http://<host>:5601`.

### APM Server (new)

APM stands for **Application Performance Monitoring**. The APM Server is a lightweight service that receives performance data from the APM agent running inside Tomcat and forwards it to Elasticsearch. It's a pass-through — it doesn't do heavy processing, just validates and routes data.

### Elastic APM Java Agent (new, runs inside Tomcat)

This is the key piece. The APM agent is a `.jar` file that attaches to the Tomcat Java process at startup (via `-javaagent`). It automatically instruments:

- **Spring MVC controllers** — records every HTTP request with the controller method name, URL, and response time
- **Hibernate/JPA queries** — records every database query with its SQL and execution time
- **JDBC connections** — tracks connection acquisition time
- **HTTP client calls** — tracks outgoing HTTP requests (e.g., to Solr)

It does this by bytecode instrumentation (modifying Java classes at load time), so **no application code changes are needed**. It sends all this data to the APM Server.

### Jolokia (new, runs inside Tomcat)

Jolokia is a tiny agent that exposes JMX (Java Management Extensions) data over HTTP. JMX is a standard Java mechanism for exposing internal metrics like thread counts and memory usage. Normally JMX uses a binary protocol that's hard to work with, but Jolokia makes it available as simple HTTP/JSON. Metricbeat reads from Jolokia to collect:

- **Tomcat thread pool** — how many threads are active vs. available
- **C3P0 connection pool** — how many database connections are in use vs. idle

### Metricbeat (new)

Metricbeat is a lightweight agent that collects system and service metrics on a schedule (every 30 seconds) and sends them to Elasticsearch. It collects three categories of data:

| Category | What It Measures |
|----------|-----------------|
| **System** | Host CPU, memory, disk, network, top processes |
| **Jolokia/JMX** | Tomcat thread pool utilization, C3P0 DB connection pool |
| **PostgreSQL** | Active connections, transactions/sec, buffer cache hit ratio |

## How It Fits Together

```
┌──────────────────────────────────────────────────────────┐
│  Tomcat Container                                        │
│  ┌────────────────┐  ┌─────────────┐  ┌──────────────┐  │
│  │  ZFIN Web App  │  │  APM Agent  │  │   Jolokia    │  │
│  │  (Spring MVC,  │  │  (auto-     │  │  (JMX over   │  │
│  │   Hibernate)   │  │  instruments│  │   HTTP)      │  │
│  └────────────────┘  └──────┬──────┘  └──────┬───────┘  │
│                             │                │           │
└─────────────────────────────┼────────────────┼───────────┘
                              │                │
                    traces &  │                │ JMX metrics
                    spans     │                │ (every 30s)
                              ▼                │
                     ┌────────────────┐        │
                     │   APM Server   │        │
                     └───────┬────────┘        │
                             │                 │
                             ▼                 ▼
                     ┌────────────────────────────────┐
        ┌───────────▶│         Elasticsearch           │◀──── Metricbeat
        │            └───────────────┬────────────────┘      (system, PG,
        │                            │                        Jolokia)
   Filebeat                          │
   (Apache logs,                     ▼
    already existed)        ┌─────────────────┐
                            │     Kibana       │
                            │  (APM UI, logs,  │
                            │   dashboards)    │
                            └─────────────────┘
```

## How to Enable It

All monitoring services run under the Docker Compose `logging` profile and are **off by default**.

### 1. Start the logging stack

```bash
docker compose --profile logging up -d
```

This starts Elasticsearch, Kibana, Filebeat, APM Server, and Metricbeat. Tomcat does **not** need to be restarted for these.

### 2. Enable the APM agent in Tomcat

Set `ENABLE_APM=true` in your `.env` file, then rebuild and restart Tomcat:

```bash
# In your .env file, set:
# ENABLE_APM=true

docker compose up -d --build tomcat
```

When `ENABLE_APM` is `false` (the default), the APM agent and Jolokia are **not loaded at all** — there is zero overhead.

### 3. Open Kibana

Navigate to:
```
http://<your-host>:5601
```

Log in with the `elastic` user and the password from `ELASTIC_PASSWORD` in your `.env` file.

## Finding Slow Endpoints in Kibana

1. Open Kibana and go to **Observability > APM** (left sidebar)
2. Click on the **zfin-web** service
3. The **Overview** tab shows:
   - A table of endpoints ranked by latency (avg, p95)
   - Throughput (requests/min) per endpoint
   - Error rate over time
4. Click any endpoint name to see:
   - A **latency distribution** histogram
   - A list of individual transaction **traces**
5. Click a trace to see a **waterfall view**: a timeline showing exactly where time was spent (controller logic, Hibernate queries, PostgreSQL execution)

### Example: Finding Why a Page Is Slow

1. Go to APM > zfin-web > Transactions tab
2. Sort by **Avg. duration** (descending)
3. See that `PublicationViewController#showAllFigures` averages 13 seconds
4. Click it, then click a sample trace
5. The waterfall shows: 200ms in the controller, then 12.8 seconds across 47 Hibernate SELECT queries
6. Now you know: the page is slow because of N+1 query problem, not slow Java code

## Configuration Reference

### Environment Variables

These go in your `.env` file:

| Variable | Default | Description |
|----------|---------|-------------|
| `ENABLE_APM` | `false` | Load APM + Jolokia agents in Tomcat |
| `ELASTIC_PASSWORD` | (required) | Password for the `elastic` Elasticsearch user |
| `KIBANA_SYSTEM_PASSWORD` | (required) | Password for Kibana's internal ES user |
| `XPACK_ENCRYPTEDSAVEDOBJECTS_ENCRYPTIONKEY` | (required) | Encryption key for Kibana saved objects (min 32 chars) |
| `DOCKER_APM_SERVER_PORT` | `127.0.0.1:8200-8209` | Host port for APM Server |
| `DOCKER_ELASTICSEARCH_PORT` | `127.0.0.1:9200-9209` | Host port for Elasticsearch |
| `DOCKER_KIBANA_PORT` | `127.0.0.1:5601-5610` | Host port for Kibana |

### Port Mappings

All monitoring services bind to `127.0.0.1` by default (localhost only). To bind to a specific interface, override in `.env`:

```bash
# Bind to a specific local IP (useful for multi-instance setups)
DOCKER_ELASTICSEARCH_PORT=127.0.0.2:9200
DOCKER_KIBANA_PORT=127.0.0.2:5601
DOCKER_APM_SERVER_PORT=127.0.0.2:8200
```

## Files Added/Modified

### New Files

| File | Purpose |
|------|---------|
| `docker/apm-server/Dockerfile` | Builds the APM Server container; loads index template before starting |
| `docker/apm-server/apm-server.yml` | APM Server configuration |
| `docker/apm-server/traces-apm-template.json` | Elasticsearch index template that maps APM trace fields as `keyword` for proper aggregation support |
| `docker/apm-server/docker-entrypoint-wrapper.sh` | Startup script that loads the index template into Elasticsearch before starting the APM Server |
| `docker/metricbeat/Dockerfile` | Builds the Metricbeat container |
| `docker/metricbeat/metricbeat.yml` | Metricbeat output configuration |
| `docker/metricbeat/modules.d/system.yml` | System metrics collection (CPU, memory, etc.) |
| `docker/metricbeat/modules.d/jolokia.yml` | JMX metrics via Jolokia (thread pool, connection pool) |
| `docker/metricbeat/modules.d/postgresql.yml` | PostgreSQL metrics collection |

### Modified Files

| File | What Changed |
|------|-------------|
| `docker/docker-compose.yml` | Added `apm-server` and `metricbeat` services; added `ENABLE_APM` env var to Tomcat; standardized port mappings |
| `docker/tomcat/Dockerfile` | Downloads APM agent + Jolokia JARs; conditionally loads them via `ENABLE_APM` |
| `docker/environment_linux` | Added example port override variables for new services |
| `source/.../AddRequestInfoToLog4j.java` | Added response time and status code tracking to request logs |
| `home/WEB-INF/log4j2.xml` | Added `responseTimeMs`, `statusCode`, `trace.id`, `transaction.id` to JSON log output |

## Log Correlation

When APM is enabled, the APM agent injects `trace.id` and `transaction.id` into the Log4j2 thread context. These IDs appear in the JSON log file (`catalina.json`), which Filebeat ships to Elasticsearch. In Kibana, you can click a log line and jump directly to the corresponding APM transaction trace — or vice versa.

The `responseTimeMs` and `statusCode` fields are always present (even without APM) and provide a lightweight way to track response times in logs.
