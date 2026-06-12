# Solr Term-Hierarchy Expansion (and the `all-term-contains` synonym files)

Findings note on how ontology ancestor/descendant matching is implemented in the
site index, why the giant `all-term-contains-synonyms*.txt` files exist, and how
they're produced/maintained. Companion to [solr-reindex.md](./solr-reindex.md);
the raw file inventory lives in its [Configset files](./solr-reindex.md#configset-files-dockersolrsite_indexconf) section.

## TL;DR

Two separate things use the term hierarchy, and they are **not** interchangeable:

1. **Matching / recall — precomputed `*_direct` + `*_parent` fields.** DIH joins
   `all_term_contains` at index time and stores each doc's own terms (`*_direct`)
   and its ancestors (`*_parent`); both sit in the query handler's `qf`. This is
   what makes a broad query find specific-term docs. **It does not use the synonym
   files at all.**
2. **Highlighting — synonym-FST `*_hl` fields.** The `all_headless_anatomy` type
   runs the `all-term-contains-synonyms*.txt` files through the analyzer; these
   fields appear only in `hl.fl`, never in `qf`/`pf`. Their sole job is
   hierarchy-aware highlighting of result snippets.

So **search results do not depend on the synonym files** — only highlighting does.
Nobody is proposing to remove the functionality; the only open question is how the
files are *produced and maintained* (see "Changing the mechanism").

## Mechanism 1 — index-time `*_parent` fields (DIH) → matching/recall

`docker/solr/site_index/conf/db-data-config.xml` has dedicated entities that
**join `all_term_contains`** to pull ancestor terms into stored, multivalued
fields:

- Entities: `gene_expressed_in_parent`, `gene_efg_expressed_in_parent`,
  `gene_affected_anatomy_parent`, `gene_affected_biological_process_parent`, …
  (each joins `all_term_contains on alltermcon_contained_zdb_id = <term>` to walk
  up to containers/ancestors).
- Target fields (schema): `anatomy_parent`, `expressed_in_parent`,
  `expression_anatomy_parent`, `affected_anatomy_parent`,
  `biological_process_parent`, `molecular_function_parent`,
  `cellular_component_parent`, `phenotype_quality_parent`,
  `affected_molecular_function_parent`, …

This is index-time ancestor expansion done right: the closure in
`all_term_contains` is consumed during indexing and written per-doc; **no synonym
file involved.** These are the fields that are actually **searched** — `qf` lists
`<concept>_direct^1.1`, `<concept>_parent^1.0`, `<concept>_t` for each concept.
Note the boost: a direct term hit (`^1.1`) outranks an ancestor-expansion hit
(`^1.0`), relevance control you'd *lose* if direct and ancestor terms were flattened
into a single synonym-expanded field.

## Mechanism 2 — synonym-FST `*_hl` fields → highlighting only

```xml
<dynamicField name="*_hl" type="all_headless_anatomy" .../>
```

The `all_headless_anatomy` field type applies the synonym files in opposite
directions on the two analyzers:

| analyzer | file | direction | effect |
|----------|------|-----------|--------|
| index | `all-term-contains-synonyms.txt` | `child => parent` | a doc's specific term also indexes its ancestors |
| query | `all-term-contains-synonyms-reversed.txt` | `parent => child` | a query for a broad term expands to its descendants |

Every `*_hl` field gets this (the `hl.fl` list in `solrconfig.xml` is full of
them: `anatomy_hl`, `affected_anatomy_hl`, `biological_process_hl`,
`expression_anatomy_hl`, …). Crucially, **`*_hl` fields appear only in `hl.fl`,
never in any `qf`/`pf`** (verified across all request handlers). So the synonym
files are the only consumer of the FST and their sole job is *highlighting* —
deleting them would degrade hierarchy-aware highlight snippets but **would not
change which documents match.**

## The synonym files themselves

See [solr-reindex.md](./solr-reindex.md#configset-files-dockersolrsite_indexconf)
for sizes/provenance. Key facts relevant here:

- `all-term-contains-synonyms.txt` + `…-reversed.txt`: **~47 MB / ~607k lines each**,
  loaded into a synonym FST in heap at core open.
- They're a **serialization of the `all_term_contains` closure** — the same
  hierarchy already materialized in the DB table and (for `*_parent`) already
  expanded per-doc. Significant redundancy.
- **Scope is GO-heavy and multi-ontology**, not anatomy-only: GO
  `biological_process`/`molecular_function`/`cellular_component` dominate, plus
  `zebrafish_anatomy`, `quality`, `behavior_ontology`, `mouse_pathology`, uberon —
  and they deliberately **exclude** the giant ontologies (`chebi`, `cl`,
  `disease_ontology`).
- **~27% of mappings reference terms no longer in `term`** (obsoleted GO) — stale.
- The surviving generator (`generate-all-term-contains-synonyms-file.sql` via
  `index-solr.groovy`) is **`zebrafish_anatomy`-only** and writes to a dead
  `@TARGETROOT@/server_apps/solr/prototype/conf/` path — i.e. it reproduces only
  ~43k of the file's ~562k mappings. Last regenerated **≤ 2021** (git shows the
  2026-06-08 touch was a pure rename during the Solr 9 move).

## Quick win

`term_observation` is a defined field type that also references the synonym files
but **no field or dynamicField uses it** — dead. Safe to delete (does not free the
synonym files, since `all_headless_anatomy` / `*_hl` still uses them).

## Migration gap (flag)

The Java indexers under `source/org/zfin/solr/indexer/` do **no** ancestor
expansion (no `all_term_contains` usage). Since recall depends on the precomputed
`*_parent` fields (mechanism 1), any entity migrated off DIH that doesn't replicate
that expansion would **lose hierarchy-aware recall** — not merely highlighting.
That's a real search-quality regression risk, worth auditing as part of the
DIH→Java migration.

## Changing the mechanism (separate ticket)

To be clear: **the hierarchy functionality stays.** Recall is already fully handled
by the precomputed `*_parent`/`*_direct` fields and is independent of the synonym
files, and the highlighting the files drive is wanted. The only thing worth changing
is **how the files are produced and maintained.**

Simplest, lowest-risk option (worth doing if we can get the generator logic right):
- **Keep the files functionally as-is**, but stop committing them to the source tree
  (~94 MB of generated data in git). Regenerate them **nightly via a Jenkins job**
  from the DB and place them where the image/configset picks them up — the same
  model already used for `external_popularity.txt`.
- The generator must be fixed first: it currently emits only the `zebrafish_anatomy`
  slice and `\copy`s to a dead path. It needs to cover the file's real ontology scope
  (GO + anatomy + quality + behavior + mouse_pathology + uberon; see the scope
  analysis in solr-reindex.md) and write to the right location.

**Out of scope for the Solr 9 upgrade ticket**, whose goal was to ensure the upgrade
didn't change search *results*. Because recall doesn't depend on these files, the
upgrade is unaffected either way; the production-and-maintenance change belongs in
its own ticket.

Larger, optional follow-up (not required): drive highlighting off the precomputed
`*_parent` content and drop the synonym FST entirely. That's a **highlighting**
refactor, not a recall change — worth it only if the heap cost of the ~47 MB × 2 FST
at core open becomes a concern. (Earlier framing of this as a "matching" refactor was
incorrect — the query-side reversed expansion feeds highlighting, not `qf`.)

## Aside — obsolete terms and search recall

Keeping obsolete-term mappings has been raised as a possible recall aid (user
searches an old term, we surface what replaced it). Caveats:

- The containment file maps `obsolete-term => its old **ancestor**`, not
  `=> its **replacement**`. Expanding an obsolete term to a near-root ancestor
  (e.g. `molecular_function`) is close to useless.
- They **can't be regenerated** (gone from `term`/`all_term_contains`), so keeping
  them means freezing the 2021 set forever.
- The principled mechanism for "old term → current term" is the ontology's own
  secondary-ID / `replaced_by` data, generated fresh — a separate, small synonym
  set, not stale containment rows.
