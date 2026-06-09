# Makefile Audit — `server_apps/data_transfer/` (ZFIN-10327)

## Background

At the start of this audit there were **30 Makefiles** under `server_apps/data_transfer/`.
They are leftovers from the old recursive‑`make`/CVS build system. Two facts make
**all of them non‑functional today**:

1. **Every Makefile does `include $(TOP)/make.include`, and `make.include` no longer
   exists** anywhere in the repo. It was removed during the ZFIN-10113 properties
   cleanup. Because the include fails, `make` cannot even parse these files — `gmake run`
   / `gmake push` error out immediately.
2. **Gradle deliberately excludes them from deployment.** The deploy task
   `server_apps;data_transfer;deployFiles` (in `build.gradle`) copies each directory but
   has `excludes = ['Makefile', 'build.gradle', '.gitignore', '.DS_Store']`. The Makefiles
   are never deployed to the target.

The only remaining references to these Makefiles are **stale `gmake …` instructions in
README/readme/help files** — no script, cron, ant, or gradle target invokes any of them.

**Precedent:** the `Ensembl`, `GO`, `ORTHO`, and `ResourceCenters` directories already have
**no Makefile** yet are still deployed and functional via gradle. Removing a Makefile does
not affect deployment of the directory's other files.

**All 30 were safe to remove with no effect on any load or deployment** — that conclusion is
uniform. They divide into two buckets only by *what each Makefile did*, and therefore by
*what evidence was needed to confirm removal was safe*. (The buckets are not risk tiers:
nothing in Group B turned out to be riskier than Group A — Group B simply required more
evidence to reach the same verdict.)

- **Group A — pure file‑deployment Makefiles (16).** Declared only `GENERICS`/`STATICS`
  file lists for the old make‑deploy; they performed no action. Safety provable by
  inspection alone: gradle's directory copy already deploys those same files, so the
  Makefile is pure redundancy.
- **Group B — Makefiles with operational `run`/`push`/load targets (14).** These actually
  *did* something — they historically let curators trigger loads via `gmake run`. Safety
  therefore required external evidence: confirming (below) that every still‑active load is
  now triggered by **Jenkins calling the load script directly** (e.g. `./DownloadFiles.pl`,
  `perl gbaccession.pl`, `ant load-gene`), never through the Makefile. Group B also needed
  README cleanup and, for three dead manual‑only loads, removal of the orphaned scripts.

Both groups were removed in this PR (ZFIN-10327).

---

## Group A — removed in this PR (16, deploy-only)

| Makefile | What it deployed | Notes |
|---|---|---|
| `Addgene/Makefile` | `LoadAddgene.groovy`, `addgene-email.ftl` | deploy‑only |
| `BLAST/Makefile` | `getEnsemblTscripts.sql`, `runZfinEnsemblTscripts.sh` | deploy‑only (BLAST dir deployed by its own gradle task) |
| `ConstructImages/Makefile` | `loadFigures.sql`, `images.unl`, `constructFigure.unl` | deploy‑only |
| `CZRC/Makefile` | `LoadCZRCzko.groovy`, `zkoCRISPRs.csv` | deploy‑only |
| `DAF/Makefile` | `createDAF.sql`, `createDAF.sh` | deploy‑only |
| `Downloads/GFF3/knockdown_reagents/Makefile` | 12 sql/groovy/sh files | deploy‑only |
| `Downloads/intermineData/Makefile` | `dump.sql`, `dumper.sh` | deploy‑only |
| `Load/Makefile` | `blast_withdrawn.pl` (+ `SUBDIRS = ReNo Vega`) | deploy‑only; recursion is moot without `make.include` |
| `maintainTermDisplay/Makefile` | `checkRNAConsequenceTerm.groovy` | deploy‑only |
| `NCBIGENE/Makefile` | `NCBI_gene_load.pl`*, `prepareNCBIgeneLoad.sql`, `loadNCBIgeneAccs.sql`, `README` | deploy‑only; **`NCBI_gene_load.pl` no longer exists** |
| `NCBIStartEnd/Makefile` | `loadNCBIStartEnd.sql`, `ncbistartend.sh` | deploy‑only |
| `Panther/Makefile` | `LoadPanther.groovy`, `LoadAGR.groovy` | deploy‑only |
| `PUBMED/Journal/Makefile` | `checkAndUpdateJournals.{sql,pl}`, `insertJournalAlias.sql`, `mergeJournals.pl` | deploy‑only |
| `PUBMED/LinkOut/Makefile` | `providerinfo.xml`, `resources.xml`, `upload.groovy` | deploy‑only |
| `RRID/Makefile` | `updateRRIDs.groovy` | deploy‑only |
| `Signafish/Makefile` | `LoadSignafish.groovy`*, `signafish-email.ftl` | deploy‑only; **`LoadSignafish.groovy` no longer exists** (replaced by `signafish_init.groovy`) |

### Dead‑file check (Group A)

The Makefiles only *listed files for deployment*; gradle now deploys those same files via
the directory copy, so **removing the Makefiles orphans nothing**. All listed files still
exist except two, which are additional proof of staleness:

- `NCBIGENE/Makefile` → `NCBI_gene_load.pl` — **gone** (dir now has `LoadNcbiEnsemblMapping.groovy`).
- `Signafish/Makefile` → `LoadSignafish.groovy` — **gone** (dir now has `signafish_init.groovy`).

No load scripts were deleted — determining whether the *loads themselves* are dead is the
job of the orphaned‑files follow‑up (see the end of this doc), not the Makefile removal.

---

## Group B — removed in this PR (14, after Jenkins trigger confirmation)

Each had an operational target (`run`, `run_commit`, `push`, …) that historically drove a
data load. All were already broken (missing `make.include`). We confirmed how each load is
triggered today by searching the checked‑in Jenkins jobs under `server_apps/jenkins/jobs/`:
the still‑active loads run via Jenkins shell builders that invoke the **script directly**
(the builder commands are `cd …/<dir> && ./<script>` or `ant <target>`, never `gmake …`).
Nothing in the repo invokes `make`/`gmake` against any `data_transfer` directory, so the
Makefiles were non‑load‑bearing and safe to delete with no effect on any load.

Resolution applied:

- **Active load, README kept + `gmake` line updated** to point at the real trigger:
  `Genbank`, `OMIM`, `zfishbook`.
- **Active load, no `gmake` README to fix — Makefile removed only:** `Downloads`, `MEOW`,
  `LoadOntology`, and the top‑level `data_transfer/Makefile`.
- **No Jenkins job (load appears dead) — Makefile removed only:** `SNP`, `Dorsky`,
  `ExternalSearch`, `SangerMutants`. Their scripts were left in place (still in the gradle
  deploy whitelist; judging whether the loads are truly dead is out of scope here).
- **No Jenkins job, manual `gmake`‑only loads — directory contents removed, replaced with a
  `README.txt` tombstone pointing to git history (tag `v1181` and earlier):** `Load/ReNo/`
  (incl. its `Nomenclature/` subdir) and `Load/Vega/` (its 237‑line `README.help` survives
  in git history). The two now‑dead entries for these scripts were also dropped from
  `buildSrc/.../SimpleDirectoryCopyTask.groovy`'s template whitelist.

| Makefile | Invokes | Jenkins trigger today | Action |
|---|---|---|---|
| `Makefile` (top‑level) | recurses into subdirs | none (dead recursive build) | removed |
| `Downloads/Makefile` | `DownloadFiles.pl` | `Download-Files_d` → `./DownloadFiles.pl` | removed |
| `Genbank/Makefile` | `gbaccession.pl` | `GenBank-Accession-Update_d` → `perl gbaccession.pl` | removed + README |
| `OMIM/Makefile` | `OMIM.pl` | `OMIM-Update_w` → `./OMIM.pl` | removed + README |
| `MEOW/Makefile` | `meow.pl` | `Run-Meow_w` → `./meow.pl` | removed |
| `SNP/Makefile` | `dbSNP.pl` | none | removed |
| `zfishbook/Makefile` | `zfishbook.pl` | `Zfishbook-Data-Load` → `./zfishbook.sh` | removed + README |
| `Dorsky/Makefile` | `LoadFeatures.groovy` | none | removed |
| `ExternalSearch/Makefile` | `CreateMarkerSearchPage.pl` | none | removed |
| `SangerMutants/Makefile` | `loadSanger131203.pl` (missing) | none | removed |
| `LoadOntology/Makefile` | `$(TARGETDIR)` (malformed) | 11 ontology jobs → `ant` on `build.xml` | removed |
| `Load/ReNo/Makefile` | `*.r`, `load_run_report_hit.sh` | none | dir gutted → tombstone |
| `Load/ReNo/Nomenclature/Makefile` | `generate_*_run.sh` | none | dir gutted → tombstone |
| `Load/Vega/Makefile` | ~40 targets, many `.sh`/`.r`/`.sql` | none | dir gutted → tombstone |

---

## Other dead/orphaned files surfaced by this audit

While checking the removed Makefiles' file references, a search for files in those 16
directories with **no reference anywhere in the tree** turned up the following. None were
deleted (out of scope for the Makefile cleanup, and some could be curator/cron entry points
invoked by path) — they are listed here as candidates for the follow‑up.

**High confidence — superseded:**
- `Downloads/GFF3/knockdown_reagents/get_crispr_seq.sql`
- `Downloads/GFF3/knockdown_reagents/get_mo_seq.sql`
  → the active `load_knockdown_reagents.sh` uses the combined `get_mo_and_crispr_seq.sql`
  instead; both files were last touched in the 2021 initial import.

**Medium confidence — orphaned (no refs, ~2021‑era):**
- `BLAST/assembleZfinEnsemblTscripts.sh` — not called by its sibling
  `runZfinEnsemblTscripts.sh` or anything else.
- `NCBIGENE/ncbi-ensembl.sql` — NCBI/Ensembl mapping now goes through
  `LoadNcbiEnsemblMapping.groovy`.
- `Downloads/GFF3/knockdown_reagents/E_zfin_knockdown_reagents.gff3.example` — sample file,
  referenced by nothing.

**Likely one‑off (self‑referential — confirm before removing):**
- `Downloads/GFF3/knockdown_reagents/prepare_grcz12tu_genome.sh`
- `Downloads/GFF3/knockdown_reagents/load_knockdown_reagents_grcz12tu.sh`
- `Downloads/GFF3/knockdown_reagents/load_knockdown_reagents_grcz12tu.sql`
  → a self‑contained "one‑time GRCz12tu genome" set; they reference each other but nothing
  external invokes them.

**Whole deploy‑only (Group A) directories that are themselves dead loads:**
- `ConstructImages/` — `loadFigures.sql`, `images.unl`, `constructFigure.unl`: no job, zero
  references anywhere. **Gutted to a README.txt tombstone in this PR** (the clean analogue
  of the ReNo/Vega manual loads).
- `DAF/` — `createDAF.sh`, `createDAF.sql`, `dafHeader.txt`: no job, no by‑name references.
  Probable dead load, **but left in place pending load‑owner confirmation** — disease‑
  association‑file generation for the Alliance is an ongoing deliverable and DAF could be
  run by path.
- `CZRC/` — `LoadCZRCzko.groovy`, `zkoCRISPRs.csv`: no job; `LoadCZRCzko.groovy` is
  unreferenced and the CRISPR data was a one‑time load migrated into a release‑1091
  liquibase changeset (which uses its own copy under `source/...`). Probable completed
  one‑time load; **left in place pending confirmation.**

**Checked and NOT dead:**
- `NCBIGENE/clear-artifacts.sh` — no by‑name references but modified 2025‑07‑10 (ZFIN-9745),
  so it is a live entry point invoked by path. Excluded.
- `Downloads/intermineData/` (`dump.sql`, `dumper.sh`) — no job *names* the directory, but
  `DownloadFiles.pl:522` runs `intermineData/dumper.sh` on every `Download-Files_d` (daily)
  run. Live. Excluded.
- `RRID/` (`updateRRIDs.groovy`) — its `Update-RRIDs_d` job is **disabled** (dormant), not
  deleted; like `zfishbook`, kept in case it is re‑enabled.

> Caveat: "no reference in the repo" is not proof of death for shell scripts that cron or
> curators run by absolute path. Confirm with the load owner before deleting any of the
> above.

## Remaining follow‑up

Group A and Group B Makefiles are all removed as of this PR, so **no `data_transfer`
Makefiles remain**. The only open items are the dead/orphaned non‑Makefile files catalogued
above — left in place here because confirming their death requires a load owner, not a
Makefile audit. Suggested follow‑up ticket: **"Remove orphaned data_transfer load files
surfaced by the Makefile audit."**
