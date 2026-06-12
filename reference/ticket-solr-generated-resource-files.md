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
| `conf/organism-name.txt` | `organism.organism_common_name` | tiny; mildly stale (40 in file vs 44 in DB) |
| `conf/reporter-names.txt` | EFG marker abbrevs (`ZDB-EFG…`) | tiny; **~3× stale** (76 in file vs 227 in DB — KeepWordFilter is dropping ~150 current reporters) |
| `data/external_popularity.txt` | prod Apache access logs | ~11 MB; `ExternalFileField` |

## Out of scope

These stay in git — but they fall into two groups (classified by **whether a DB
source exists**, not by whether a `generate-*.sql` happens to exist):

- **Truly static config** — `stopwords.txt`, `term_stopwords.txt`,
  `publication-keyword-stopwords.txt`, `protwords.txt`, `wdgff_types.txt`,
  `colors.txt`, `country-synonyms.txt`, `chromosome_keepwords.txt`,
  `superstage-keepwords.txt`, `superstage-synonyms.txt`. No DB source; rarely change.
- **Hand-curated, *no* DB source, but can drift** — `organism-abbrev.txt` (`Hsa`,
  `Dme`), `organism-synonyms.txt` (`Hsa => Human`…), `reporter-color-synonyms.txt`
  (`GFP => Green`…). Verified there is **nothing to generate them from** (the
  `organism` table has only `organism_common_name`; fluorophore→color is domain
  knowledge, not a column). A job **cannot** produce these — but they *can* fall
  behind as species/fluorophores are added, so they want **occasional human review**,
  not automation. Flagging so they aren't mistaken for "set and forget."
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

## Appendix: prototyped generator fix + scope verification

The corrected Track-A query for the two `all-term-contains` files has already been
worked out and verified against prod-scale data (coral `all_term_contains` =
6,478,688 rows, == prod). Numbers below are a starting point, not gospel — re-verify
on the chosen DB at implementation time.

**Verified scope (same-ontology, in-scope set, identity rows dropped):**

| ontology | mappings | ontology | mappings |
|---|---:|---|---:|
| biological_process | 452,268 | quality | 10,546 |
| molecular_function | 65,270 | behavior_ontology | 3,808 |
| zebrafish_anatomy | 43,222 | mouse_pathology.ontology | 3,410 |
| cellular_component | 42,283 | uberon/phenoscape-anatomy | 2,675 |
| uberon | 20,827 | **total distinct pairs** | **643,383** |

Sanity check vs the current committed file (562,300 distinct non-identity mappings):
the regenerated set is *larger* (643 k) because it gains current GO growth and drops
the ~155 k obsolete mappings the stale file still carries. Expected and healthy.

**Uberon decision (the one open scope question):** the committed file contained
uberon only at collision-level (~3.6 k by-child, i.e. names shared with other
ontologies), so it likely was **not** intentionally in scope. Including `uberon` +
`uberon/phenoscape-anatomy` adds ~23.5 k mappings that weren't really there before
(total 643 k with, **619,881 without**). Decide deliberately; the draft includes them
flagged.

**Drafted SQL** (replaces the anatomy-only `generate-all-term-contains-synonyms-file.sql`;
parameterize `@OUTDIR@` to the configset location; `delimiter '|'` matches the legacy
single-column dump format that `SynonymGraphFilter` consumes):

```sql
-- Forward (child => parent): index-analyzer expansion, specific -> ancestor
\copy (
  select distinct child.term_name || ' => ' || parent.term_name
  from term parent
  join all_term_contains on alltermcon_container_zdb_id = parent.term_zdb_id
  join term child         on alltermcon_contained_zdb_id = child.term_zdb_id
  where parent.term_ontology = child.term_ontology
    and child.term_ontology in (
      'biological_process','molecular_function','cellular_component',
      'zebrafish_anatomy','quality','behavior_ontology','mouse_pathology.ontology',
      'uberon','uberon/phenoscape-anatomy')      -- CONFIRM uberon (see note above)
    and child.term_zdb_id <> parent.term_zdb_id  -- drop identity / reflexive no-ops
) to '@OUTDIR@/all-term-contains-synonyms.txt' delimiter '|';

-- Reversed (parent => child): query-analyzer expansion, broad -> descendants
\copy (
  select distinct parent.term_name || ' => ' || child.term_name
  from term parent
  join all_term_contains on alltermcon_container_zdb_id = parent.term_zdb_id
  join term child         on alltermcon_contained_zdb_id = child.term_zdb_id
  where parent.term_ontology = child.term_ontology
    and child.term_ontology in (
      'biological_process','molecular_function','cellular_component',
      'zebrafish_anatomy','quality','behavior_ontology','mouse_pathology.ontology',
      'uberon','uberon/phenoscape-anatomy')
    and child.term_zdb_id <> parent.term_zdb_id
) to '@OUTDIR@/all-term-contains-synonyms-reversed.txt' delimiter '|';
```

Key changes vs the legacy SQL: (1) covers the full ontology set instead of
`zebrafish_anatomy` only; (2) requires both endpoints in the **same** ontology
(avoids cross-ontology name-collision noise); (3) `distinct` (the old file had dup
lines); (4) drops identity/reflexive rows via `term_zdb_id` inequality; (5) writes to
`@OUTDIR@` instead of the dead `@TARGETROOT@/server_apps/solr/prototype/conf/`.

**The other two analyzer files are simpler** — `generate-organism-name-file.sql`
(`select organism_common_name from organism`) and `generate-reporter-name-file.sql`
(EFG marker abbrevs) are *not* scope-broken, only path-broken: they just need the
output repointed to `@OUTDIR@` (and a `distinct` is reasonable).

**Still to do in impl (not yet prototyped):** the Jenkins wiring, the empty-stub
files in the image, the drift guard, and confirming the generated output format
loads cleanly into `SynonymGraphFilter` (no header/quoting surprises from `\copy`).
