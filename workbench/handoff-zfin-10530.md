# ZFIN-10530 hand-off: where the branch stands

For whoever picks up branch `zfin-10530` next. It says what was done on 2026-10-26 and 27, what was checked, and what is still open. For the reasoning behind the design, see [`work-and-decisions.md`](work-and-decisions.md). For the wider assembly brief and decisions D1 to D10, see [`assembly-blast-radius.md`](assembly-blast-radius.md).

## The ticket

ZFIN-10530, "Modify rule for displaying Annotation Status in Gene Page summary": show **Current** if the gene is on GRCz12tu, GRCz12ab or both. Still show **Not in current annotation** for genes NCBI lists that way, and **Unknown** if the gene has only GRCz11, Ensembl, Vega or nothing.

The branch does this with the view `marker_annotation_status_derived`. A gene is Current if it has an NCBILoader location on an assembly with `assembly.a_is_current`. GRCz12tu, GRCz12ab and GRCz13ab are all current, as set by main's migration. It is also Current if its NCBI Gene link is attributed to a publication that `load_publication` says confers Current.

## How the branch came to be

The work began on `annotation-status-view` with every commit titled `ZFIN-10529`. The first commit (Genome Resources order, the actual ZFIN-10529 change) was squash-merged to main as `eff701b62e` (#2054). The rest belonged to ZFIN-10530, so it was moved:

1. `zfin-10530` was cut from the old branch and rebased onto `origin/main` (`eff701b62e`), dropping the commit main already had.
2. The 17 remaining commits were combined into 10 and retitled `ZFIN-10530:`.
3. The old history is kept on `zfin-10530-before-squash`, and `annotation-status-view` was not touched. Both can be deleted once this branch is accepted.

Nothing has been pushed.

### Commits on `origin/main`

| Commit | What | Notes |
|---|---|---|
| `6a22e61b97` | `load` and `load_publication` tables | Separate because the view in `59a8e58154` joins `load_publication` |
| `b89fbd2433` | Store NCBI's not-in-current-release list, with a guard against a bad fetch | Includes the "no colons in the report message" fix |
| `59a8e58154` | The view, and everything reads the status from it | The ticket's rule change. Also renames the assembly migration to `0020-` |
| `dadfd69c3a` | Drop `marker_annotation_status`, seeding Not in current genes first (`0055`) | Separate because it is destructive |
| `edf03db03c` | The NCBI load reads its publication lists from `load_publication` | Includes the stop on an unlisted publication in `toLoad.unl` |
| `ac2bee03d6` | GRCv10 as a legacy assembly | |
| `e9b6df9621` | `load_file` and the dump setting driven by it | Outside the ticket. Depends on the commits above, so it cannot move to another PR without rework |
| `3b103011e4` | Load-Database job: "Load the newest dump file" checkbox | Outside the ticket. Has no dependencies and could become its own PR |
| `fb1e7ba65a` | Feature locations and the mapping detail page order assemblies by the `assembly` table | |
| `2fd33f7bd8` | Workbench notes, with the check that blocks merging them | Delete `workbench/` before merging |

## What the rebase changed, and why

Main had also merged ZFIN-10528 (`f8d5335fbc`), which added `ZDB-PUB-261014-17` ("Shared Ensembl IDs from GRCz12 assemblies") to the NCBI load's hard-coded publication lists. This branch replaces those lists with `load_publication`. Its commit message says the new publication should be treated like `ZDB-PUB-230516-87` everywhere, so:

- **The `load_publication` seed (`0030-load-and-load-publication.sql`)** now gives `ZDB-PUB-261014-17` precedence 3, load-managed, confers Current. Vega (`ZDB-PUB-130725-2`) moves from 3 to 4.
- **There is one change in behavior.** Main ranked the two Ensembl supplement publications equal (2). The table does not allow ties, so the GRCz11 one now wins. A gene is matched through only one of the two, so this should not matter in practice.
- **`NCBIDirectPort`'s startup check** (`requireCreatedByLoad`) also requires `PUB_MAPPED_BASED_ON_NCBI_SUPPLEMENT_GRCZ12`, and `NcbiLoadPublicationsDatabaseTest` expects the new row.
- **Main marks GRCz12ab current; the branch had not.** `AssemblyLegacyTest` now expects three current assemblies. With that, the view matches the ticket's wording with no further change.
- **The `0020-` migration is now safe to run again.** Main merged it as `ZFIN-10529-assembly-current-and-foreign-db-assembly.sql`. The branch renames it to `0020-…` so that it runs before `0050`, which needs its column. Liquibase sees a renamed file as a new changeset, so a database migrated from main runs it again. Both `add column` statements now use `if not exists`, and its updates set the same values a second time.

## What was checked

- `./gradlew compileJava compileTestJava` passes.
- `NcbiLoadPublicationsTest` passes (6 tests).
- The final tree of the 10 commits is the same as the 17-commit history, apart from the `0020` change made afterwards.
- **`0020` run twice on minimal stand-in tables** (rolled back). Both runs succeed, GRCz13ab, GRCz12ab and GRCz12tu end up current, and `foreign_db` has one foreign key to `assembly`. Main's version fails the second time with `column "a_is_current" … already exists`.
- **`0020` run against this stack's real database**, where the columns already existed (rolled back). Both `add column` statements were skipped, GRCz12ab changed to current, and there was still one foreign key.

**Not run:** the database tests (`NcbiLoadPublicationsDatabaseTest`, `AssemblyLegacyTest`, `MarkerAnnotationStatusDerivedViewTest`, …), the NCBI load integration tests (`-PncbiLoadTests`, which empty tables), a full NCBI load on the rebased code, and the restore-and-Liquibase check.

## The database: unknown state

The stack's database (`z run` → host `db`, `zfindb`) had run all nine 1187 migrations on 2026-10-23, before the rebase. So it had GRCz12ab not current, the old `load_publication` seed and the old checksums. It was patched in place, in one committed transaction:

```sql
update assembly set a_is_current = true where a_name = 'GRCz12ab';
update load_publication set lp_precedence = 4 where lp_pub_zdb_id = 'ZDB-PUB-130725-2';
update load_publication set lp_precedence = 3, lp_load_managed = true,
       lp_confers_current_gene_status_via_ncbi = true
 where lp_pub_zdb_id = 'ZDB-PUB-261014-17';   -- matched no row, see below
update databasechangelog set md5sum = null
 where filename like '%1187/migrations/0020-ZFIN-10529-assembly-current-and-foreign-db-assembly.sql'
    or filename like '%1187/migrations/0030-load-and-load-publication.sql';
```

Seconds after the commit, `publication` and `record_attribution` were missing. Soon after, `zfindb` had **no tables at all** and no other sessions. The patch contains no drop statements, so something else emptied or replaced the database. Possible causes are a reload, the container being reinitialized, or `db` pointing at a new instance. The patch is gone with it. A plain `docker exec zfin-10529-db-1 psql -d zfindb` also reached an empty `zfindb`, which may or may not be the same server that `z run` reaches.

**Before anything else:** find out what happened to the database, and load a dump if it is still empty. On this branch's code, the migrations give the right values without any patching.

## Open items

1. **`ZDB-PUB-261014-17` was missing from `load_publication`** in that database, so the seed did not find the publication. If the next dump lacks it too, the branch's NCBI load stops at the startup check, because `requireCreatedByLoad` requires it. Check `select zdb_id from publication where zdb_id = 'ZDB-PUB-261014-17'` after the reload. If it is missing, decide whether the seed should create it, the check should be relaxed, or dumps will have it once ZFIN-10528 has run in production.
2. **Run the database tests and the restore-and-Liquibase check** on the reloaded database.
3. **Re-measure the view against the data.** The figures in `59a8e58154` (22,194 Current, 2,799 Not in current, 55 added) were taken while GRCz12ab was not current. Genes placed only on GRCz12ab will now be Current too.
4. **The ticket's examples.** Check the gene page for genes on GRCz12tu only, GRCz12ab only, both, GRCz11 only, and one NCBI lists as not in the current release.
5. **Out-of-scope commits.** Decide whether `3b103011e4` (Jenkins checkbox) moves to its own PR. `e9b6df9621` (`load_file`) is also out of scope, but it depends on commits before it.
6. **Migration file names.** Main has `0030-ZFIN-10570-ro-gorel-relation-ids.sql`, and this branch has `0030-load-and-load-publication.sql`. They are independent and sort in a fixed order, so this is harmless, but renumbering ours avoids confusion. Only do it before any shared database has run it, since a rename makes Liquibase run it again.
7. **Out-of-date workbench notes.** `work-and-decisions.md` and `assembly-blast-radius.md` still name branch `annotation-status-view`, say GRCz12ab is not current, use `ZFIN-10529:` commit titles and cite pre-rebase hashes. They are deleted before merging anyway, but a reviewer reading them now may be misled.
8. **Not started, and not part of this ticket.** Replacing `marker_assembly` with a view (Goal 1 in [`jbrowse-issues/handoff-genome-browser-decision.md`](jbrowse-issues/handoff-genome-browser-decision.md), waiting on D6 and D8), and moving `gff3_ncbi*` to `external_resource` (Goal 2).
9. **Before merging:** delete `workbench/`. The "No workbench notes" workflow fails until it is gone.
