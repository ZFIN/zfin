# Marker-merge equivalence testing

This directory holds the **differential equivalence harness** that proves the Java port
(`org.zfin.marker.MergeMarkersCommandLine`, run as `zfin-util merge-markers`) produces the *same
database effect* as the legacy Perl `cgi-bin/merge_markers.pl`, across many marker pairs of many
types.

- `compare_merge_markers.sh` — the host-side orchestrator (run this).
- `derive_headless_merge_markers.sh` — turns the CGI Perl into a headless, batch-runnable form.

It is a **characterization / golden-master test**: rather than asserting hand-written expectations,
it runs *both* implementations against identical real data and asserts the resulting databases are
identical. That is the only way to catch the kind of subtle porting bugs a generic
foreign-key-graph merge is prone to (see "What it caught" at the bottom).

```
server_apps/DB_maintenance/merge/compare_merge_markers.sh 100      # 100 pairs across types
COMPOSE=~/zfin/docker/docker-compose.yml OUT=/tmp/merge_cmp \
  server_apps/DB_maintenance/merge/compare_merge_markers.sh 100    # with overrides
```

It is **destructive to throwaway clone databases only** (`merge_cmp_seed`, `merge_cmp_test`) and
never touches `zfindb`. It runs on the host and drives the containers (`db` for psql/clone, `compile`
for perl + `zfin-util`). Expect ~15–20 min for 100 pairs (the DB clones dominate). Drop the clones
when done: `psql -U postgres -c 'drop database if exists merge_cmp_seed'` (and `merge_cmp_test`).

---

## The core idea: make the comparison deterministic

Two separate runs of *anything* against a database differ in incidental ways — wall-clock
timestamps, auto-generated ids, physical row order. The harness is built so the **only** legitimate
difference between a Perl run and a Java run is wall-clock timestamps, which we then exclude:

1. **Identical starting state.** Both implementations start from a byte-identical clone of the same
   seed database. So every `get_id('DALIAS')`/`get_id('NOMEN')` call hands out the *same* new id on
   both sides (the id sequence/regen table starts in the same state and is advanced the same number
   of times by the same operations). Generated ZDB ids therefore **match**, not just "look similar".
2. **Same pairs, same order.** Both runs process the identical list of pairs (`pairs.txt`) in the
   same order, so cumulative state evolves identically.
3. **Atomic per pair.** A pair that errors must be a clean no-op on *both* sides, or one partial
   failure would desync everything after it. Java already gets this from `ToolBootstrap`'s
   transaction; the headless Perl is derived with `RaiseError=1, AutoCommit=0` + an explicit commit
   (see below). So a failed pair rolls back fully on both sides.
4. **Exclude timestamps.** The remaining nondeterminism — `now()` in `mhist_date`, audit columns —
   is removed by excluding all temporal column types from the fingerprint.

With those four in place, "did the Perl and Java do the same thing?" reduces to "are the two
databases identical (ignoring timestamp columns)?"

---

## Efficient DB cloning: `CREATE DATABASE ... TEMPLATE`

`zfindb` is ~15 GB. A `pg_dump | psql` round-trip would take a long time and a lot of disk. Instead
the harness uses PostgreSQL's template mechanism:

```sql
CREATE DATABASE merge_cmp_seed  TEMPLATE zfindb;          -- once, the frozen baseline
CREATE DATABASE merge_cmp_test  TEMPLATE merge_cmp_seed;  -- reset before each implementation's batch
```

Why this is fast (~3 min for 15 GB here, disk-copy bound rather than logical):

- `CREATE DATABASE ... TEMPLATE x` performs a **physical, file-level copy** of the template's data
  directory. It does not parse, re-plan, re-index, or re-validate anything — it just copies the
  files and registers a new database. That is dramatically cheaper than dump/restore, which
  serializes every row to SQL/COPY text and rebuilds indexes/constraints on load.

Operational requirements and gotchas (all handled by the script):

- **No active connections to the template.** `CREATE DATABASE ... TEMPLATE x` requires that `x` has
  zero other sessions (Postgres briefly treats the template as read-only). The script checks
  `pg_stat_activity` for `zfindb` and aborts with a clear message if anything is connected — so run
  it when the app/Tomcat is idle, or stop Tomcat first. (This is also why we clone `zfindb` into a
  *frozen* `merge_cmp_seed` once, then re-template from the seed between runs: the seed has no
  clients, so the per-implementation resets never contend with the live app.)
- **Privilege.** Creating databases needs `CREATEDB`; we connect to the `db` container as the
  `postgres` superuser over trust auth (`psql -h db -U postgres`).
- **Disk.** Each clone is a full ~15 GB copy. The harness keeps at most the seed + one test clone
  (~30 GB) and drops the test clone between phases.

Reset between the two implementation runs is then just:

```sql
DROP DATABASE IF EXISTS merge_cmp_test;
CREATE DATABASE merge_cmp_test TEMPLATE merge_cmp_seed;
```

Each implementation points at `merge_cmp_test`:
- **Java:** a copy of `zfin.properties` with `DB_NAME=merge_cmp_test`, passed via
  `ZFIN_PROPERTIES_PATH`.
- **Perl:** the headless script's `<!--|DB_NAME|-->` placeholder is substituted to `merge_cmp_test`.

---

## Fingerprinting: order-independent, timestamp-free per-table content hashes

After each implementation's batch, the harness computes a **content fingerprint per table** and
compares the two sets of fingerprints. Comparing fingerprints (not full dumps) keeps it fast and
makes a mismatch trivially localizable to a specific table.

The fingerprint is computed entirely server-side by a temporary function:

```sql
create or replace function pg_temp.fp(tabs text[]) returns table(tbl text, fp text)
language plpgsql as $f$
declare r text; cols text; q text;
begin
  foreach r in array tabs loop
    -- column list for this table, EXCLUDING temporal types (the only legit nondeterminism)
    select string_agg(quote_ident(column_name), ',' order by ordinal_position) into cols
      from information_schema.columns
     where table_schema = 'public' and table_name = r
       and data_type not in ('timestamp without time zone','timestamp with time zone',
                             'date','time without time zone','time with time zone');
    if cols is null then tbl := r; fp := '(missing-or-only-temporal-cols)'; return next; continue; end if;
    -- per-row md5, then md5 of the row-hashes aggregated in SORTED order => order-independent
    q := format(
      'select coalesce(md5(string_agg(h, '''' order by h)), ''empty'')
         from (select md5(row(%s)::text) h from public.%I) s', cols, r);
    begin execute q into fp; exception when undefined_table then fp := '(missing)'; end;
    tbl := r; return next;
  end loop;
end $f$;

select tbl || '|' || fp from pg_temp.fp(string_to_array('<tables>', ',')) order by tbl;
```

Key properties:

- **Order-independent.** Each row is hashed (`md5(row(...)::text)`), then the row-hashes are
  aggregated `ORDER BY h` before hashing again. So two tables with identical rows in different
  physical order (different clustering, vacuum, insertion order) fingerprint the same — we're
  comparing *sets of rows*, not a byte dump.
- **Timestamp-free.** Temporal columns (`timestamp`/`date`/`time`) are dropped from each table's
  column list, discovered per-table from `information_schema.columns`. This is what lets two
  independent runs match despite `mhist_date = now()` and audit timestamps. (It is a deliberate
  blind spot: a real difference confined entirely to a timestamp column would not be flagged.
  Acceptable here because timestamps are exactly the expected nondeterminism.)
- **Whole-table.** It hashes all non-temporal columns of every row, so any data difference in any
  compared table is caught.

### Which tables get fingerprinted

A merge touches an a-priori-unknown set of tables (that's the whole point of the generic FK walk),
and fingerprinting all of a 15 GB database would be slow. So the harness derives the **merge
footprint** automatically: it greps the Java tool's own SQL log for `update`/`delete`/`insert into
<table>` and unions that with a small set of always-touched bookkeeping tables (`marker`,
`marker_history`, `data_alias`, `zdb_active_data`, `zdb_replaced_data`, `record_attribution`,
`db_link`). For the 100-pair multi-type run this was 32 tables.

### Comparison and drill-down

- Per-pair `OK`/`ERR` is compared (`results.diff`) — catches "succeeds on one, fails on the other".
- Per-table fingerprints are compared (`fingerprint.diff`) — `IDENTICAL across N tables` is the pass
  condition.
- On a fingerprint mismatch the harness dumps the offending table(s) for inspection. To pinpoint
  *why*, two finer tools were used during development:
  - **Java `--dry-run`** logs every SQL statement it would execute and rolls back, so you can diff
    the generated SQL without mutating anything.
  - **Single-pair isolation:** clone once, run Java `--dry-run` (rolls back, leaving the clone
    clean), then run Perl on the same clone, and diff just the suspect tables — far faster than
    re-running the whole batch.

---

## Deriving a headless Perl (`derive_headless_merge_markers.sh`)

The canonical `cgi-bin/merge_markers.pl` can't be run as-is in a batch: it's a CGI script with
deploy-time placeholders. The derive step transforms it, changing **only** plumbing — never the
merge logic:

- fills the `<!--|DB_NAME|-->` / `<!--|PGHOST|-->` deploy placeholders;
- swaps the two `CGI->param("OID"/"merge_oid")` reads for `$ARGV[0]`/`$ARGV[1]` and drops
  `use CGI`/`CGI::Carp` (absent in the build container);
- sets the DB user to `postgres` (trust auth) instead of the empty deploy default;
- opens the DBI handle with `RaiseError => 1, AutoCommit => 0` and commits before disconnect, so each
  invocation is **atomic** (matches Java's transaction; failed pairs roll back instead of leaving
  partial state). Production runs the CGI under autocommit — making the *test* atomic is what keeps a
  failed pair from desyncing the batch.
- neutralizes the `regen_genox_marker(...)` call (see below).

### Why `--skip-regen` / neutralizing regen

`regen_genox_marker` is a denormalization recompute that rebuilds derived fast-search tables. It
names a backup table with **minute granularity** (e.g. `mutant_fast_search_old_2606041640_...`), so
when many merges run inside the same minute (the Perl is fast; Java's per-invocation JVM/Hibernate
startup spreads them out) the second call collides with `relation ... already exists`. That is a
batch-speed artifact, not a Perl-vs-Java logic difference, and the rebuilt tables are derived data
that nightly jobs regenerate anyway. So both sides skip it: Java via the `--skip-regen` flag (a
genuinely useful operational flag for bulk merges too), the Perl via the neutralized call. With
regen skipped, the fast-search tables are only changed by the merge's direct FK reassignments, which
are deterministic and identical on both sides.

---

## What it caught

Running both implementations over 100 pairs surfaced four porting discrepancies that code review and
unit tests would not have — see the memory note and `MergeMarkersCommandLine.java` comments:

1. `mrkr_comments` `"\n\n"` quirk (Perl's loose `undef ne 'none'`).
2. `operator does not exist: bigint = character varying` — `setString` types a param as `varchar`;
   against a `bigint` key column reached during recursion Postgres refuses the comparison. The Perl
   interpolates `unknown`-typed literals; the Java now does too.
3. NULL collision keys — Perl concatenates `undef` as `""`; Java rendered `NULL` as `"null"`.
4. The big one: the Perl's unique/primary-key conflict resolution is hand-unrolled for **exactly
   2, 3, or 4 columns**; a generalized loop did conflict-resolution on wide keys (e.g.
   `expression_experiment2`, 6–7 column unique constraints) that the Perl never performs. Capped to
   `[2,4]`.

Final result: identical per-pair `OK`/`ERR` and identical content fingerprints across 32 tables for
100 pairs spanning genes, antibodies, morpholinos, CRISPRs, TALENs and pseudogenes.
