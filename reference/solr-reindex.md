# Solr Reindex Pipeline

## Overview

The nightly Solr reindex rebuilds the `site_index` core from the database. It runs as `SolrReindexOrchestrator`, a Java job that interleaves two kinds of indexing steps:

- **DIH steps** — invoked via Solr's data-import handler, using the entity definitions in `docker/solr/site_index/conf/db-data-config.xml`.
- **Java steps** — invoked directly against the `SolrIndexer` implementations registered in `org.zfin.solr.indexer.IndexerRegistry`.

The orchestrator is the cutover vehicle for migrating entities off DIH. Each entity is either DIH or Java, never both. As entities migrate, the matching `<entity name="…">` block in `db-data-config.xml` comes out and a `SolrIndexer` goes in — see [Migrating an entity off DIH](#migrating-an-entity-off-dih) below.

**Jenkins job**: `Index-Faceted-Search-Step-2-Solr_d`
**Schedule**: Nightly
**Gradle entry point**: `gradle solrReindex`

## Why a single orchestrator (not "Java does Java, DIH does DIH")

Mixing the two engines requires shared state:

- One initial wipe (DIH's `clean=true` only wipes once on its first caller; not safe to rely on when Java steps mix in).
- One ordering pass — heavy entities first so memory-pressure failures surface fast.
- One core RELOAD cadence between batches to release Lucene's `IndexWriter` buffers (the dominant heap-growth source on a multi-GB rebuild; see ZFIN-10171 for the trajectory).
- One commit at the end.

Running DIH and Java as independent jobs would either duplicate that state or fight over it. The orchestrator owns it once.

## Migration model

Migrated entities are tracked in two places, kept in lockstep:

1. The `BATCHES` table in `SolrReindexOrchestrator.java`. Every entity appears here exactly once, tagged with `Source.DIH` or `Source.JAVA`.
2. The `IndexerRegistry` static initializer. Every `Source.JAVA` entry must have a matching `SolrIndexer` registered.

The batches mirror the legacy `solr-reindex-pipeline.sh` ordering (heavy → medium → light), so behavior under memory pressure stays predictable.

| Batch    | Entities                                                                                                                              |
|----------|----------------------------------------------------------------------------------------------------------------------------------------|
| `heavy`  | figure, phenotype, phenotype_misexpressed_gene, expression, feature, expression_result, phenotype_observation                          |
| `medium` | fish, construct, gene, marker, str, antibody, term, publication                                                                       |
| `light`  | person, **lab** *(Java)*, company, journal, go_annotation, str_relationship                                                            |

## Migrating an entity off DIH

Three coordinated changes:

1. **Implement** `org.zfin.solr.indexer.SolrIndexer` for the entity. Match the doc shape DIH produced — confirm by diffing `gradle solrDiag -PdiagArgs="dump …"` output against the baseline snapshot from a Solr 8 nightly. See `LabIndexer.java` for the working example.
2. **Register** the indexer in `IndexerRegistry`:
   ```java
   static {
       register(new LabIndexer());
       register(new YourEntityIndexer());   // ← add this line
   }
   ```
3. **Flip** the matching `Step` in `SolrReindexOrchestrator.BATCHES` from `Source.DIH` to `Source.JAVA`, and remove the `<entity name="…">` block from `db-data-config.xml` so DIH stops emitting it.

Then verify locally:
```bash
# Run a full reindex
docker compose run --rm compile bash -lc "gradle solrReindex"

# Or, faster: just the JAVA indexer in isolation
docker compose run --rm compile bash -lc "gradle solrIndex -PsolrEntities=your_entity"

# Diff doc shape against the baseline (see ./baseline-solr8/docs.ndjson)
gradle solrDiag -PdiagArgs="dump ./out/current.ndjson"
# Then: python3 diff script over baseline vs ./out/current.ndjson, filtered
# to the entity's type — fields and counts should match modulo intentional fixes.
```

> **Why `solrDiag` instead of a script?** The previous bash pipeline
> (`solr-dump.sh` + jq) occasionally produced ~0.1% malformed lines on
> a full 1.34M-doc dump — a concurrent-write artifact between curl
> pagination and jq's appends. The Java port writes sequentially from
> a single process via SolrJ, so that class of corruption can't happen.
> For a single-entity diff, querying Solr directly is still fast:
>
> ```bash
> curl -fsS "$SOLR/$CORE/select" \
>   --data-urlencode "q=*:*" \
>   --data-urlencode "fq=type:Lab" \
>   --data-urlencode "rows=2000" \
>   --data-urlencode "sort=id asc" \
>   --data-urlencode "wt=json" > /tmp/labs.json
> ```

## Invocation

```bash
# Nightly full reindex (initial wipe → heavy → reload → medium → reload → light → commit)
gradle solrReindex

# Skip the initial wipe — additive run, useful for repairing a partial rebuild
gradle solrReindex -PsolrNoClean=true

# Resume from a specific entity after a failure. Skips all earlier batches and
# entities; the batch containing the resume target runs from that entity forward,
# preserving inter-batch RELOAD semantics.
gradle solrReindex -PsolrResumeFrom=construct

# Ad-hoc run of just the migrated Java indexers (no DIH, no wipe).
gradle solrIndex                                  # all registered indexers
gradle solrIndex -PsolrEntities=lab,company       # a subset

# Verify the DIH side of the most recent reindex (mirrors the ant
# check-indexer target the Jenkins job runs after build-solr-index,
# but reachable without a deployed classpath).
gradle checkIndexer
```

## Failure recovery

The orchestrator stops at the first failed step and logs the failed entity. Restart with `-PsolrResumeFrom=<entity>`:

```bash
# Example: the run died on the 'gene' DIH step. Pick up from there.
gradle solrReindex -PsolrResumeFrom=gene
```

Resume semantics:

- Entities before the resume target are skipped.
- The initial wipe is skipped (the prior partial index is what we're appending to).
- The batch containing the resume target starts at that entity. Earlier entities in the same batch are not re-run.
- The RELOAD between batches still runs after the (now-shorter) resume batch completes.

If the failure is DIH-side, `dataimport?command=status` on the running Solr will show the failure message. If the failure is Java-side, the orchestrator's log holds the stack trace.

## Snapshot / restore

The orchestrator does **not** snapshot. The ant target `build-solr-index-jenkins` still calls `backup-solr-index` after `solrReindex`, which runs `gradle solrBackup` (the `org.zfin.solr.admin.SolrSnapshotTool` tool) to call `/replication?command=backup&name=YYYY.MM.DD-HH.mm`. That produces `snapshot.YYYY.MM.DD-HH.mm/` under `/research/zunloads/solr/v9/${INSTANCE}/`. The `v9/` segment segregates Solr 9 snapshots from the legacy pre-9 full-SOLR_HOME dumps (which can't be restored via `/replication` — see the cutover guards in `getLatestSolrIndex`/`getLatestSolrUnload`).

### GoCD deployment pipeline

The GoCD deploy step runs:

```bash
docker compose run --rm compile bash -lc "gradle getLatestSolrIndex"
```

`getLatestSolrIndex` is in the top-level `build.gradle`. It picks the newest `snapshot.*` dir, then shells `ant restore-solr-index -DsolrIndex=<name>`, which calls `/replication?command=restore`.

**The orchestrator cutover does not change this contract.** The snapshot is a Lucene file dump; it doesn't carry information about whether a given doc was emitted by DIH or by a `SolrIndexer`. As long as Jenkins keeps producing `snapshot.*` directories at the same path, the GoCD step works unchanged. We confirmed this against the lab cutover — the snapshot taken after a mixed DIH+Java orchestrator run restores cleanly via the existing flow.

## Memory pressure and core RELOAD

A full reindex of `site_index` grows Lucene's `IndexWriter` buffers monotonically across entities. On a 16 GB cgroup we observed kernel `SIGKILL` around the 1.9M-doc mark (pre-RELOAD). The orchestrator releases that state between batches by hitting Solr's documented admin endpoint:

```
GET /solr/admin/cores?action=RELOAD&core=site_index
```

RELOAD closes and reopens the `SolrCore` (IndexWriter, caches, request handlers) inside the same JVM — no container restart, no warm-up loss of the rest of the index. After RELOAD the orchestrator polls `/solr/site_index/admin/ping` until the core responds, then continues with the next batch.

The RELOAD call is **synchronous**: Solr holds the HTTP response open until the core has finished reopening its `IndexWriter`, which on the full ~1.34M-doc index takes longer than the `getJson` helper's default 30s request timeout. The orchestrator therefore issues the RELOAD with the full `RELOAD_TIMEOUT` (5 min) budget. A 30s budget passes the first (post-`heavy`) reload but times out on the second (post-`medium`) reload once the index is large — `HttpTimeoutException` in `reloadCore`, before `waitForCorePing` is ever reached (ZFIN-10171).

**Observed (2026-06-08, `coral`):** a full ant chain (`build-solr-index-jenkins` → reindex + check-indexer + backup) completed in **36m 35s** at 1,345,826 docs (heap 12g / `mem_limit: 16g`), with both inter-batch reloads succeeding under the 5-min budget.

**Observed (2026-06-04):** a full reindex via the orchestrator completed in **1h 41m 12s** at 1,345,829 docs with `mem_limit: 16g` / heap 12g. Solr container memory hovered around 13.15 GiB / 16 GiB throughout — well under the cgroup ceiling. Heavy batch took ~62 min, medium ~32 min, light ~3 min, with two RELOADs between batches. If the index grows past ~2M docs or new heavy entities land and the ceiling is hit again, the fallbacks (in order of increasing impact) are: split the offending entity into its own batch in `BATCHES` so it reloads sooner; bump `mem_limit` to 24g (host has the headroom — postgres now caps at 4 GB shared_buffers).

A historical note: a legacy bash pipeline (`solr-reindex-pipeline.sh`, removed alongside the orchestrator) offered a `--release=restart` mode (full container restart for native-memory release). The Java orchestrator dropped that mode — it can't shell `docker compose restart` from inside the JVM cleanly, and RELOAD proved sufficient for the cases we hit. If a regression appears, the recipe is restored by shelling `docker compose restart solr` between batches from an outer wrapper.

## Files

| File | Purpose |
|------|---------|
| `source/org/zfin/solr/indexer/SolrReindexOrchestrator.java` | Main driver (this doc's subject) |
| `source/org/zfin/solr/indexer/IndexerRegistry.java` | Central registry of Java `SolrIndexer` implementations |
| `source/org/zfin/solr/indexer/SolrIndexer.java` | Interface every Java entity indexer implements |
| `source/org/zfin/solr/indexer/SolrIndexerJob.java` | Ad-hoc runner — just the Java indexers, no DIH |
| `source/org/zfin/solr/indexer/LabIndexer.java` | First migrated entity (reference implementation) |
| `docker/solr/site_index/conf/db-data-config.xml` | DIH entity definitions for still-DIH entities |
| `buildfiles/solr-ant.xml` | Ant target `build-solr-index-jenkins` that the Jenkins job calls |
| `server_apps/DB_maintenance/build.gradle` | The `solrReindex`, `solrIndex`, `checkIndexer`, `solrCharacterize`, and `solrDiag` gradle tasks |
| `source/org/zfin/datatransfer/CheckIndexerJob.java` | Validator the ant `check-indexer` and `gradle checkIndexer` targets both invoke |
| `source/org/zfin/solr/diagnostics/` | Characterization toolkit (DumpTool, FieldsTool, TermsTool, QueriesTool, AnalyzeTool, LogQueryExtractor + the SolrCharacterizer orchestrator) — Java port of the `solr-*.sh` scripts |
| `server_apps/solr/diagnostics/` | Input data for the diagnostic tools (field lists, analyzer inputs, query lists) |
| `source/org/zfin/solr/admin/SolrAdminClient.java` | Shared Solr-admin plumbing: DIH import, core RELOAD, wipe, backup, restore |
| `source/org/zfin/solr/admin/SolrSnapshotTool.java` | CLI for `backup` / `restore` subcommands (`gradle solrBackup` / `solrRestore`) |
| `server_apps/solr/scripts/generatePopularity.groovy` | Regenerate Solr's `external_popularity.txt` from apache log hits |
