# Ticket: Generate Solr static resource files in Jenkins instead of committing them

**Type:** Tech-debt / build
**Components:** Solr, Jenkins, search

> Draft ticket description. Background analysis lives in
> [solr-reindex.md](./solr-reindex.md#configset-files-dockersolrsite_indexconf)
> (configset inventory, generator scope) and
> [solr-term-hierarchy-expansion.md](./solr-term-hierarchy-expansion.md)
> (how the synonym files are actually used, and why recall does not depend on them).

## Summary

Several large Solr resource files under `docker/solr/site_index/` are **generated
data committed to git** (no Git LFS) and have gone **years stale** because the
generators are broken or were never automated. Stop committing them; regenerate
them in Jenkins on the appropriate cadence and place them where the image/configset
picks them up. Net effect: ~90+ MB out of the repo, and the files actually stay
current.

This does **not** change search functionality — it changes how these files are
produced and kept fresh.

## Motivation

- **Repo bloat:** `all-term-contains-synonyms.txt` (47 MB) + its `-reversed` twin
  (47 MB) are committed with no LFS; every clone carries ~94 MB forever, plus
  `data/external_popularity.txt` (~11 MB).
- **Staleness:** `external_popularity.txt` was last regenerated **≤ 2021** (~5 yr);
  its boost currently reflects ancient traffic. The synonym files' generator is
  **broken** (see below) and they were last meaningfully touched in 2021.
- **Wrong home:** these are DB- and log-derived artifacts. They belong in a
  generation pipeline, not hand-maintained in version control — the same model
  already used (correctly) for nothing else here yet, but proven viable.

## In scope (files to move to generated)

| File | Source | Notes |
|------|--------|-------|
| `conf/all-term-contains-synonyms.txt` | DB `all_term_contains` (term containment) | 47 MB; analyzer synonyms |
| `conf/all-term-contains-synonyms-reversed.txt` | same query, reversed direction | 47 MB |
| `conf/organism-name.txt` | `organism` table | tiny |
| `conf/reporter-names.txt` | EFG markers (`ZDB-EFG…`) | tiny |
| `data/external_popularity.txt` | prod Apache access logs | ~11 MB; `ExternalFileField` |

## Out of scope

- **Hand-curated small config files stay in git** (stopwords, colors,
  country/organism/superstage synonyms, `wdgff_types.txt`, `protwords.txt`, etc.) —
  these are config, not generated data, and are correctly version-controlled.
- **Already done** (commit `ba292306b5`, do not redo): removed the dead
  `admin-extra.html`, `scripts.conf`, `synonyms.txt`, `zfin_synonyms.txt`, and the
  stale `conf/popularity.txt`.
- Git **history** rewrite to reclaim the ~94 MB already committed (LFS / filter-repo)
  — separate decision, can be a follow-up.

## Two delivery tracks (different source, cadence, and mechanics)

### Track A — DB-derived analyzer files (the 4 synonym/keepword files)

**Cadence/placement:** regenerate as a **pre-step of the Solr reindex** pipeline
(`Index-Faceted-Search-Step-…`), *not* a standalone nightly job — analyzer-resource
changes only take effect on a reindex anyway, so coupling them is correct and avoids
half-applied state.

**The generator must be fixed first — it is currently broken:**
- It emits **only the `zebrafish_anatomy` slice** (~43k of the file's ~562k mappings).
  Running it as-is would **drop ~520k live GO mappings** — a real regression.
  It must cover the file's actual scope: **GO `biological_process` /
  `molecular_function` / `cellular_component`, `zebrafish_anatomy`, `quality`,
  `behavior_ontology`, `mouse_pathology`, and the uberon anatomy ontologies** —
  and must **exclude** the giant ontologies (`chebi`, `cl`, `disease_ontology`),
  which the file deliberately omits. (Confirm the uberon members during impl.)
- It `\copy`s to a **dead path** (`@TARGETROOT@/server_apps/solr/prototype/conf/`);
  repoint to the real configset location.
- **Drop identity (`X => X`) no-op rows.**
- Generate from a DB with a fully-populated `all_term_contains` (it's a derived
  closure; verified coral == prod at 6,478,688 rows, but don't generate from a
  reduced DB).

**Constraint — schema references these files at core load:** `schema.xml` resolves
the synonym/keepword files when the core opens (via the `all_headless_anatomy` type
and `KeepWordFilter`s). A referenced-but-missing file means the **core won't start**.
So they **cannot be fully removed from the image** — ship **empty stub files** (a
valid no-op list) so a fresh container loads, and have the reindex overwrite them
with real content + reload before indexing.

### Track B — popularity file (`external_popularity.txt`)

**Cadence/placement:** a **standalone periodic Jenkins job** (nightly or weekly) —
independent of the reindex. `ExternalFileField` refreshes on any commit/searcher
reopen, so no reindex is needed; the job regenerates the file and triggers a
commit/reload. `generatePopularity.groovy` already exists.

**Constraints:**
- The job **must run where the production Apache access logs are available**
  (`/var/log/httpd/zfin_access`). The reindex/build host may only see its own
  instance's logs — resolve log access before wiring this up, or it will generate
  popularity from the wrong traffic.
- `ExternalFileField` has `defVal=1`, so a **missing file is graceful** (every doc
  gets default popularity). Track B's file can be **fully removed** from the
  image/repo (unlike Track A, no stub needed).

## Acceptance criteria

- [ ] The 5 files are no longer committed as real data (Track A: empty stubs only;
      Track B: removed entirely).
- [ ] Jenkins regenerates each on the correct cadence and places it correctly
      (Track A → reindex pre-step + reload; Track B → periodic job + commit/reload).
- [ ] Track A generator output **covers the full ontology scope** and is verified by
      diffing against the current committed file (differences should be: + current
      GO/etc., − obsolete terms, − identity rows; no unexplained loss of live terms).
- [ ] **Drift guard**: regeneration fails or loudly warns if a file's row count drops
      more than ~30% vs the previous run (catches the silent-shrink failure mode).
- [ ] A **fresh container still loads** (Track A stubs present) and a **full reindex
      still succeeds** end to end.
- [ ] **Search results unchanged**: recall is served by the precomputed
      `*_direct`/`*_parent` fields and is *independent* of the synonym files; the
      synonym files only drive `*_hl` **highlighting** — verify highlighting still
      works after the change.
- [ ] Repo-size reduction confirmed (working tree; history is a separate follow-up).

## Risks / notes

- **Low search-results risk:** the synonym files affect *highlighting only*, not
  matching (see solr-term-hierarchy-expansion.md). The main correctness check is that
  hierarchy-aware highlighting still renders.
- **Obsolete terms:** regeneration naturally drops terms no longer in the ontology
  (~27% of the current file). If preserving "user searches an old term → surface its
  replacement" is desired, do it properly via the ontology's secondary-ID/`replaced_by`
  data as a separate small synonym set — *not* by retaining stale containment rows.
- **Adjacent gap (not this ticket, but related):** the Java `SolrIndexer`s do no
  ancestor expansion, so entities migrated off DIH may lose `*_parent` recall. Flag
  for the DIH→Java migration; see solr-term-hierarchy-expansion.md.

## References

- `reference/solr-reindex.md` — configset inventory, generator/scope analysis
- `reference/solr-term-hierarchy-expansion.md` — mechanism; recall vs highlighting
- `server_apps/solr/scripts/generatePopularity.groovy` — existing popularity generator
- `docker/solr/site_index/conf/generate-*.sql`, `index-solr.groovy` — the legacy
  (broken) analyzer-file generators to fix/replace
