# ZFIN-10529: what we built, and why

A record of the work on branch `annotation-status-view` and the architectural choices made along the way, for a reviewer who wants the reasoning and not only the diff. For the open decision about genome browsers, see [`jbrowse-issues/handoff-genome-browser-decision.md`](jbrowse-issues/handoff-genome-browser-decision.md). For the wider brief and decision list (D1 to D10), see [`assembly-blast-radius.md`](assembly-blast-radius.md).

## How it grew

The ticket asked for one thing: reorder the Genome Resources on the gene page (Alliance, NCBI Gene, Ensembl GRCz12ab, Ensembl GRCz12tu, with Vega and Ensembl GRCz11 behind "Show more"). Doing that properly showed that ZFIN decides "which assembly" in many places, some by name in code, some by a cached copy in the database. So the work moved step by step from "reorder a list" toward "the database says which assemblies exist and what they mean, and code asks the database".

The common thread of every decision below: **put a fact in one place, as data, and derive everything else from it.**

## Work, in order

1. **Gene page order.** Genome Resources are ordered by assembly, using the existing `list-collapse.js` widget for "Show more" (a first custom toggle was dropped as redundant).
2. **Assembly facts in the database.** `assembly.a_is_current` and `foreign_db.fdb_assembly_id`, replacing hard-coded GRCz12 checks and anticipating GRCz13ab.
3. **Load metadata.** `load`, `load_publication` and `load_file` tables; `load_file_log` points at its file.
4. **Annotation status derived.** The stored per-gene table `marker_annotation_status` is gone; a view `marker_annotation_status_derived` replaces it, and NCBI's "not in current release" list is stored by NCBI Gene id in `external_resource.ncbi_not_in_current_release`.
5. **NCBI load reads its rules from data.** The hard-coded publication lists in the load's SQL and Java now come from `load_publication`.
6. **Dump policy from data.** `unload_production.sh` asks `load_file.lf_exclude_from_dump` which tables to leave out of dumps.
7. **GRCv10** is listed in `assembly` as a legacy row.
8. **Feature locations and the mapping detail page** take assembly order from the `assembly` table (`AssemblyOrder`) instead of fixed lists.
9. **Jenkins `Load-Database` job** gained a "Load the newest dump file" checkbox. Separately, `jenkins-cli-pass-stdin` fixes the CLI wrapper.
10. **Evidence for the browser decision** (production screenshots of every place a browser appears) and a hand-off for whoever decides.

## Decisions and the reasoning

### Assemblies are rows, not code

- **`a_is_current` is a flag, and more than one assembly can be current.** GRCz12tu and GRCz13ab are both current at once during a transition, so "current" cannot be "the one with the lowest order". Ordering (`a_order`, lowest is newest) and currency are separate facts.
- **`foreign_db.fdb_assembly_id` is nullable and sits on the foreign database.** Anything from "Ensembl (GRCz12ab)" is on that assembly by definition, so the link to an assembly belongs with the database. It is not redundant with `db_link_assembly`, which records an assembly for an individual link; the column states the default for the whole source. Null means the source is not assembly-specific.
- **GRCv10 is a row in `assembly`, marked legacy**, rather than a special case in code. Relabeling its rows was left for someone who knows the Vega load.
- **Rejected: a second hard-coded list.** The first idea (a constant of assembly names in Java) just moved the problem. `AssemblyOrder.fromDatabase()` reads `a_order`; an unknown assembly ranks last.

### One source of truth for "Current"

Today's rules were stored in two places that could disagree. Options considered: keep the stored table (A), derive part of it (B), derive all of it (C). We chose **C**.

- **The per-gene status table is removed.** Status is a view over facts that already exist: the gene's NCBI Gene link, its attribution to a publication flagged `lp_confers_current_gene_status_via_ncbi`, and an `NCBILoader` location on a current assembly.
- **A view, not a materialized copy.** A copy can drift, and drifting was the problem. The cost is a query at read time, which is small for one gene.
- **The "not in release" list is the one thing that must be stored**, because it comes from a live NCBI query and cannot be derived from ZFIN data. It is keyed by NCBI Gene id, not ZFIN id, so it does not depend on ZFIN links and needs no second writer.
- **The refresh has a guard.** NCBI's query quietly returns an empty list when it fails, and an empty list would make every gene look current. A fetched list replaces the stored one only if it is non-empty and at least half the stored size. Otherwise the stored list stays, and the load report says so. (The half threshold was chosen by the team, not measured.)
- **Its rows are dumped.** Status depends on them, and a restored database without them would show 2,799 genes wrongly as Current. A real restore found this, and migration `0055` seeds the table from the old status table before that table is dropped.

### Load rules are data, with typed columns

- **A first-class table named `load`.** Candidates were `load`, `dataload`, `load_job` and `data_load`. We kept `load`: shortest, and `load_file_log` already used the word. (`data_load` would have meant renaming existing tables.)
- **`load_publication`, with a typed column**, `lp_confers_current_gene_status_via_ncbi`, rather than a key/value metadata table. A key/value table was considered and rejected: it gives up types, constraints and foreign keys for flexibility nobody asked for, and makes queries like the view above harder to read. Add a column when a rule is needed.
- **The publication lists in the load come from the table, and the load stops on an unlisted publication** rather than quietly ranking it last. Someone attributing a gene to a publication by hand should not change the load's behavior unnoticed; the load fails loudly and an admin adds a row.
- **A file is separate from a run.** `load_file` holds what does not change from run to run (source URL, target table, dump setting); `load_file_log` keeps one row per run. `load_file_log` was extended, not rebuilt (expand then contract): the old name, source and table columns are still written and can be dropped once nothing reads them.
- **The dump setting is `lf_exclude_from_dump`.** Names considered: reloadable, disposable, recoverable. Named for what it does. **Dump by default, exclude by mark**: forgetting to mark a table costs disk space, while forgetting the other way loses data. Excluded tables still keep their structure and comments in the dump.
- **No table for the GOA load.** Considered and skipped; its log rows suffice.

### Marker-to-assembly: derive from locations

- **`sequence_feature_chromosome_location_generated` is the truth about where a gene is.** `marker_assembly` is a cached copy, and it drifts. The goal is a plain join instead (decision B over C, "use `a_is_current`", and not relying on Ensembl, since NCBI is the primary source). It is not done yet; D6 and the D8 follow-up come first. See the hand-off for the goals.
- **The `gff3_ncbi*` tables are a mirror of an upstream file** and belong in `external_resource`, without ZFIN gene ids mixed in. Also not done yet.

### Pages ask the database

- **The feature locations query joins `assembly` and orders by `a_order`**, so a new assembly sorts correctly the day it is inserted.
- **`MappingDetailController`'s rules became static methods that take an `AssemblyOrder`**, so they are plain-class unit tests (14) with no database, instead of logic hidden inside a controller.
- **The genome browser choice was deliberately not decided in this ticket.** It needs a table, there are three reasonable shapes, and the answer depends on what the team wants to control. We gathered evidence (production screenshots, the hard-coded places, the track differences between assemblies) and wrote the options up for a decision.

### Process choices

- **Migrations in `1187/migrations/`, numbered so they run in order.** A real restore plus Liquibase run caught an ordering fault (the view needed a column added later), so the assembly migration is `0020-…`. Preconditions use `onFail:MARK_RAN`, with `--precondition-sql-check` for table checks.
- **Verified against a real restore**, not only unit tests: `gradle loaddb`, `gradle liquibasePostBuild`, and a full NCBI load (it added 5 genes and the status matched; Not in current stayed 2,799).
- **Validation report dropped.** A "check external resource registry" job was judged overkill and must not be re-added.
- **Commit style.** Messages begin `ZFIN-10529:`, one logical change per commit, the SQL rewrite as a follow-up to the table commit.
- **The Jenkins API token** typed into chat during setup is held only by the CLI's own login store. Rotate it if the session is shared.

## What was not done

- The NCBI load integration tests (`-PncbiLoadTests`) were not run, because they empty tables.
- `MarkerMergeServiceTest` fails on the dev database for missing fixture data, with or without these changes; its rewritten case could not be run.
- The remaining items (D4 to D7, removing `marker_assembly`, moving `gff3_ncbi*`, the generalized loader, GRCz13ab) are listed in the hand-off.
