# Phenotype mart refresh: incident, fix, and redesign options

## Context

The phenotype mart is three derived tables, fully recomputed nightly from the
expression/phenotype source tables by
`server_apps/DB_maintenance/warehouse/phenotypeMart/`:

| table | PK | natural key | populated by |
|---|---|---|---|
| `phenotype_source_generated` (psg) | `pg_id` (serial) | `(pg_genox_zdb_id, pg_fig_zdb_id, pg_start_stg_zdb_id, pg_end_stg_zdb_id)` | `SELECT DISTINCT` |
| `phenotype_observation_generated` (pog) | `psg_id` (serial) | `psg_pg_id` + identifying attribute tuple | `SELECT` (no DISTINCT) |
| `phenotype_generated_curated_mapping` (pgcm) | — | `pgcm_pg_id` + source | `SELECT DISTINCT` |

Pipeline: `regenPhenotypeMart.sh` → `runPhenotypeMart.sh` (builds `_temp`
tables via `populateTables.sql`, then runs `refreshPhenotypeMart.sql` to swap
`_temp` into the live tables) → `gradle runPhenotypeIndexer` (Solr).

Consumers: Hibernate entities (`PhenotypeSourceGenerated`,
`PhenotypeStatementWarehouse`, `PhenotypeWarehouse`), the Solr DIH
(`server_apps/solr/site_index/conf/db-data-config.xml`, joining pog + psg +
`mutant_fast_search` + `term`), and data downloads. The tables are **leaf**
tables — nothing outside the mart references them except the internal
`pog → psg` FK.

## The incident (Index-Faceted-Search-Step-1, prod, 2026-06-11)

`refreshPhenotypeMart.sql`'s table swap could not acquire the
`AccessExclusiveLock` on `phenotype_observation_generated`: the `ALTER TABLE …
RENAME` blocked ~7 min behind ~100 live web-app `AccessShareLock` readers and
head-of-line-blocked ~99 more, then was cancelled, rolling the whole
transaction back. `ON_ERROR_STOP=1` + the wrapper's `set -e` aborted before the
diagnostic echo, so the Jenkins console only showed the generic
`PROBLEM ENCOUNTERED` trap; the real Postgres error went to
`regenPhenotypeMartReportPostgres.txt` (via the `&>` redirect).

Root cause: the slow `CREATE new-live + INSERT … SELECT FROM _temp` ran *after*
the rename had already taken the exclusive lock, so the lock was held across
the whole rebuild+copy, and acquisition had no `lock_timeout`.

## Phase 0 — shipped fix (branch `ZFIN-phenotype-mart-lock-fix`)

Two commits, validated on PG 18 against `zfindb`:

1. **Two-phase split.** Do all slow work (back up to `_bkup`, build
   fully-populated/indexed/FK'd `*_new` tables the app never reads) *before*
   any rename; the exclusive-lock window is reduced to the metadata-only
   renames (`live → *_old_<ts>`, `*_new → live`) just before commit. `*_new`
   child FKs reference the `*_new` parent so they point at the new live parent
   after the swap (FKs track by OID).

2. **`lock_timeout` + bounded retry.** Split into two transactions (generator
   `runPhenotypeMart.sh` no longer wraps the file in begin/commit):
   - TXN 1 (build) commits — releasing the `SHARE ROW EXCLUSIVE` locks the
     FK-adds hold on the parent tables (fish_experiment, figure, stage, marker,
     term), so they are not held across the retry back-off.
   - TXN 2 (swap) does the renames under `SET LOCAL lock_timeout='10s'` in a
     sub-transaction retry loop (5 attempts, 20s back-off). On
     `lock_not_available`/`deadlock_detected` the sub-transaction rolls back
     (releasing that attempt's locks) and sleeps, so back-off holds nothing on
     the live or parent tables — readers run freely between attempts. After the
     budget is exhausted it raises a clear error, leaving the live tables
     unchanged and the `*_new` tables for a later/next-run swap.

Validated: happy path reproduces the baseline structure exactly; a held
`AccessShareLock` forces a timeout then the swap recovers on the next attempt;
a lock held through the whole budget fails cleanly with live tables unchanged
and `*_new` retained, and the next run cleans up (`DROP TABLE IF EXISTS`) and
succeeds.

Phase 0 fixes the prod failure but does **not** bound the lock *acquisition*
contention away — it makes it safe (fail fast, retry, never starve), not
absent. Removing the swap entirely is the goal of the later phases.

## The keystone constraint: surrogate keys churn

`pg_id`/`psg_id` are `nextval(...)` serials assigned fresh each rebuild, so the
same logical row gets a **different id every run**. This is what blocks the two
"nice" redesigns:

- **Materialized view + `REFRESH … CONCURRENTLY`** would see every row as
  changed (the unique-index diff is keyed on the churning id) → full rewrite,
  slower than now. Also: matviews can't carry FKs, can't be a serial-PK target,
  and the ~15 sequential enrichment `UPDATE`s would have to collapse into one
  defining `SELECT`.
- **Incremental `MERGE`** has no stable key to match "same logical row" across
  runs.

So **stable identity is the prerequisite** for both. A natural key exists for
psg (the 4 fields). For pog it requires first resolving duplicates (below).

### Options weighed

- **A. Blue-green via a view** — *skip.* Repointing a view still needs
  `AccessExclusiveLock` on the view, which waits behind the same readers — same
  contention as the rename, so it still needs lock_timeout+retry. Only upside is
  dropping the `_old`/`_new`/index-name juggling, at the cost of permanent view
  indirection. Marginal.
- **B. Materialized view + `REFRESH CONCURRENTLY`** — the "right" Postgres tool
  for derived-data blue-green, but here a large/risky rewrite (see constraints
  above). Viable only after identity is stabilized.
- **C. Incremental `MERGE` by natural key** — best steady-state lock profile
  (row locks only, no swap, no DDL, no `_old` cleanup), and naturally enables
  incremental Solr indexing. Highest implementation/correctness risk; needs
  stable identity and careful handling of denormalized name changes + deletes;
  keep a periodic full reconcile against drift. **Recommended end state.**

### Single-column hash key vs composite key

A deterministic hash key (`pg_id = md5(concat_ws('|', natkey...))::uuid`) is a
standard pattern (dbt/Data Vault) and here is *less* disruptive than a composite
PK: it keeps the single-column join/FK shape (composite would widen `pog → psg`,
`pgcm → psg`, and the Solr DIH joins to 4 columns). Trade-offs: an
`integer → uuid`/`text` type change ripples to the Hibernate entities, Solr DIH,
and downloads; hash inputs for pog must be the identifying `*_zdb_id`s +
tag/relations only (NOT the denormalized name columns, so a term/marker rename
is an in-place UPDATE, not a new identity); NULLs must be normalized in the hash
input; don't truncate into bigint (collision risk) — use full md5 as uuid/text.

Alternative that keeps `integer`: **preserve-by-lookup** (reuse the existing
`pg_id` for a matched natural key, new serial only for new keys) — no type
change, but an upsert/lookup against live during build. Both approaches hit the
same pog-uniqueness wall.

### pog duplicate finding (gates the identity work)

`pog` has 659 rows that are **identical on every column** (642 groups, max
multiplicity 4), concentrated in the expression path. Cause: the three pog
inserts use plain `SELECT` (psg and pgcm use `SELECT DISTINCT`); the
expression/labeling/part-of joins fan out and project identical tuples. The
paths are mutually exclusive on `psg_mrkr_relation`, so a per-insert `DISTINCT`
removes them with no information loss. These are a benign artifact + a latent
~0.24% over-count, not meaningful data.

## Plan

Dependencies: `0 → (1 → 2 → 3) → 4`. Phases 0 and 1 are independently
shippable; 2 gates 3; 3 supersedes the Phase-0 swap machinery (which stays
correct in the meantime).

- **Phase 0 — ship the prod fix.** Branch `ZFIN-phenotype-mart-lock-fix` (PR
  open). Deploy `refreshPhenotypeMart.sql` + `runPhenotypeMart.sh` to
  `TARGETROOT`. Resolves the incident.
- **Phase 1 — dedup pog.** Add `DISTINCT` to the three pog inserts in
  `populateTables.sql`. Verify the row count drops by exactly the known dups and
  that Solr facet counts / downloads are unaffected. Independent; also fixes the
  over-count. Makes pog's natural key unique.
- **Phase 2 — stable identity (keystone).** Make `pg_id`/`psg_id` deterministic.
  Decision: hash (`uuid`, accepts type-change ripple) vs preserve-by-lookup
  (keeps `integer`). Prereq: Phase 1.
- **Phase 3 — remove the swap.** Replace rebuild+rename with incremental `MERGE`
  (recompute to `_temp`, `MERGE … ON id` into live): row locks only, no
  `AccessExclusiveLock`, no swap, no `_old` cleanup. Keep a periodic full
  reconcile. (Materialized view + `REFRESH CONCURRENTLY` is the fallback.)
- **Phase 4 — optional.** Drive incremental Solr phenotype indexing from the
  same delta instead of the full reindex that runs after.

### Open decisions

- (a) OK to dedup pog in Phase 1 (≈0.24% fewer rows)?
- (b) Phase 2 identity: accept the `uuid`/`text` type change, or keep `integer`
  via preserve-by-lookup?
- (c) Phase 3: incremental `MERGE` (recommended) vs materialized view?

## Testing notes

All phases verified on the local PG 18.4 `zfindb` via `zrun` (compile container
login shell → `psql` to the `db` container): capture a baseline of counts /
indexes / FK constraints, run the change, diff against baseline, and run twice
to confirm cross-run idempotency. Concurrency/lock behavior tested by holding an
`AccessShareLock` from a second session (`docker compose exec db psql … BEGIN;
SELECT …; pg_sleep(N); COMMIT;`) while running the swap.
