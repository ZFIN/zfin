# Testing the NCBI Load Changes (ZFIN-10461)

End-to-end test plan for the branch that adds genome-location reconciliation, fixes the
load report, validates legacy Vega links, and repairs `Load-NCBIStartEndPositions`.

Background — why any of this exists, and the measurements behind the expected numbers — is
in [reference/ncbi-ensembl-gene-mapping.md](../reference/ncbi-ensembl-gene-mapping.md). This file is only the
procedure.

Three things are easy to get wrong and are called out where they arise:

- the GFF3 staging tables are idempotent as of this branch; step 5 is the check that
  re-running the load leaves them unchanged
- the reconciliation needs **pre-existing drift** in the database, or the run proves nothing
- the report fixes are **not visible in any row diff** — they are read off the HTML

## 0. Unit tests first

```bash
gradle test --tests org.zfin.UnitTests
```

`NCBIGenomeLocationReconcileTest` (13 tests) and the extended `LegacyReportAdapterTest` are
registered in that suite, so the pure decision logic (`categorizeDrift`,
`isDuplicateOfTargetRow`, `describeConflict`) and the report parser are covered without a
database. Fast, and it fails first if something is broken.

## 1. Load the database

```bash
gradle loaddb
gradle liquibasePreBuild liquibasePostBuild
```

Use a recent production `.bak`. This matters: the drift the reconciliation corrects is
*pre-existing production data* — 57 rows on the dump this was developed against. A synthetic
or over-cleaned database yields 0 and makes step 8 vacuous.

## 2. Snapshot script

Reused verbatim at steps 2, 4 and 8.

It captures two kinds of thing. **In scope** — the rows the change is meant to touch — is
dumped in full so individual rows can be diffed. **Out of scope** — everything the change
must leave alone — is captured too, as full rows where the set is small and as a per-group
count plus content hash where it is not. An unexpected write shows up as a changed hash even
though the table is far too large to dump.

```bash
#!/bin/bash
# snap.sh <label>
set -euo pipefail
d=~/ncbi-test/$1; mkdir -p "$d"
q() { psql -X -q -A -F$'\t' -v ON_ERROR_STOP=1 "$DBNAME" -c "$1"; }

# ---------- in scope: full rows ----------

q "select sfclg_pk_id, sfclg_data_zdb_id, sfclg_acc_num, sfclg_chromosome, sfclg_start, sfclg_end
     from sequence_feature_chromosome_location_generated
    where sfclg_location_source = 'NCBILoader' order by sfclg_pk_id" > "$d/sfclg_ncbiloader.tsv"

q "select dblink_linked_recid, dblink_acc_num, recattrib_source_zdb_id
     from db_link join record_attribution on recattrib_data_zdb_id = dblink_zdb_id
    where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
    order by 1,2,3" > "$d/dblink_ncbigene.tsv"

q "select mas_mrkr_zdb_id, mas_vt_pk_id from marker_annotation_status
    order by 1,2" > "$d/marker_annotation_status.tsv"

q "select ma_mrkr_zdb_id, ma_a_pk_id from marker_assembly order by 1,2" > "$d/marker_assembly.tsv"

# ---------- out of scope: must not change ----------

# Every other genome-location source, in full. NCBIStartEndLoader especially:
# reconcileNcbiGenomeLocations() filters on sfclg_location_source = 'NCBILoader' and
# deliberately excludes the rest, so any movement here is a bug in that filter.
# Other sources in use: DirectSubmission, ZFIN, ZfinGbrowseStartEndLoader,
# EnsemblStartEndLoader, NCBIStartEndLoader, VegaStartEndLoader, UCSCStartEndLoader,
# ZfinGbrowseZv9StartEndLoader, AGP Load, General Load, other map location.
q "select sfclg_location_source, sfclg_pk_id, sfclg_data_zdb_id, sfclg_acc_num,
          sfclg_chromosome, sfclg_start, sfclg_end
     from sequence_feature_chromosome_location_generated
    where sfclg_location_source is distinct from 'NCBILoader'
    order by sfclg_location_source, sfclg_pk_id" > "$d/sfclg_other_sources.tsv"

# Every foreign-db container in db_link, fingerprinted. The load writes eight of them;
# the report's displayNameForForeignDB change makes every other container it has ever
# seen render by name, so this is where an accidental write to InterPro, Pfam, PROSITE,
# UniProtKB, Ensembl or PANTHER would surface.
q "select dblink_fdbcont_zdb_id, count(*),
          md5(string_agg(dblink_zdb_id||'|'||coalesce(dblink_acc_num,'')||'|'||
                         coalesce(dblink_linked_recid,'')||'|'||coalesce(dblink_info,''),
                         ',' order by dblink_zdb_id))
     from db_link group by 1 order by 1" > "$d/dblink_by_container.tsv"

# Every attribution pub, fingerprinted - the Vega validation only ever drops
# ZDB-PUB-130725-2 rows, so the other pubs' hashes must hold.
q "select recattrib_source_zdb_id, count(*)
     from db_link join record_attribution on recattrib_data_zdb_id = dblink_zdb_id
    group by 1 order by 1" > "$d/recattrib_by_pub.tsv"

# All attribute keys, not just gene_id; and the record table behind them.
q "select gna_key, count(*) from gff3_ncbi_attribute group by 1 order by 1" > "$d/gff3_attr_by_key.tsv"
q "select gff_feature, count(*) from gff3_ncbi group by 1 order by 1"       > "$d/gff3_by_feature.tsv"

# ---------- derived checks ----------

q "select count(*) from gff3_ncbi_attribute where gna_key = 'gene_id'"    > "$d/gene_id_pairs.txt"
q "select count(*) from sequence_feature_chromosome_location_generated
    where sfclg_location_source='NCBILoader' and sfclg_start = sfclg_end" > "$d/degenerate_ends.txt"

# Drift, categorised exactly as NCBIDirectPort.categorizeDrift does.
q "select case when cg is null then 'ORPHANED' when cg like '%,%' then 'AMBIGUOUS'
               else 'REMAPPED' end as category, count(*)
     from (select (select string_agg(distinct d.dblink_linked_recid, ',' order by d.dblink_linked_recid)
                     from db_link d
                    where d.dblink_acc_num = l.sfclg_acc_num
                      and d.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1') as cg
             from sequence_feature_chromosome_location_generated l
            where l.sfclg_location_source = 'NCBILoader'
              and not exists (select 1 from db_link d
                               where d.dblink_acc_num = l.sfclg_acc_num
                                 and d.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
                                 and d.dblink_linked_recid = l.sfclg_data_zdb_id)) t
    group by 1 order by 1" > "$d/drift_by_category.tsv"
```

```bash
./snap.sh 02-baseline
```

### Comparing two snapshots

```bash
diff -r ~/ncbi-test/02-baseline ~/ncbi-test/08-after-gene-load
```

Expected-to-move files are named in each comparison step below. **Anything else that differs
is a finding** — in particular `sfclg_other_sources.tsv` and every row of
`dblink_by_container.tsv` except the NCBI Gene container `ZDB-FDBCONT-040412-1` must be
identical across the whole run.

## 3. Run Load-NCBI-GFF3-File

```bash
cd $SOURCEROOT && gradle loadGff3NcbiFile
```

`gff3_ncbi` is the job's **staging** table, not its target: `processNcbiGff3()` mirrors every
feature in the downloaded GFF3 into it, and `Gff3Writer` later reads it back to emit
`zfin_genes.grcz12.gff3`. The job's actual target is
`sequence_feature_chromosome_location_generated`, and the write there *is* idempotent —
`upsertSequenceFeatureChromosomeRecords()` keys on the accession and only touches
start/end/chromosome when they differ. The staging table had no such guard until this branch;
see step 5.

`downloadNcbiGff3File()` returns early if `GCF_049306965.1_GRCz12tu_genomic.gff.gz` already
exists in the working directory. Drop a cached copy there or every run re-downloads it from
NCBI.

```bash
./snap.sh 04-after-gff3-run1
```

## 4. Compare after the first GFF3 run

Three checks, only one of which is a row diff.

### a. The end-coordinate fix

`degenerate_ends.txt` must go **down or stay flat, never up**. The old
`genomeLocation.setEnd(gff3Ncbi.getStart())` fired whenever an end coordinate changed,
leaving `sfclg_start = sfclg_end`; those rows are its fingerprint, and this run should write
real ends over them.

```bash
diff <(cut -f1,2,3 ~/ncbi-test/02-baseline/sfclg_ncbiloader.tsv) \
     <(cut -f1,2,3 ~/ncbi-test/04-after-gff3-run1/sfclg_ncbiloader.tsv)
```

Identity columns should be near-identical; movement belongs in the coordinate columns. Spot
check a few corrected rows against the GFF3 file itself.

### b. Drift is unchanged

The GFF3 load resolves new rows from current `db_link`, so it neither creates nor clears
drift. `drift_by_category.tsv` should match the baseline. If it moved, something outside this
change is at work.

### c. `marker_annotation_status` and `marker_assembly` move only for new locations

The GFF3 load does write both, but only on the branch that creates a *new* genome location:
`gene.getAssemblies().add(grcz12tu)` and `gene.setAnnotationStatusTerms(Set.of(annotationStatusTerm))`
in `upsertSequenceFeatureChromosomeRecords()`. Rows added to `marker_annotation_status.tsv`
and `marker_assembly.tsv` should therefore correspond one-for-one to genes that gained an
`sfclg` row in this run. Existing rows must not change.

### d. Nothing out of scope moved

```bash
diff -r ~/ncbi-test/02-baseline ~/ncbi-test/04-after-gff3-run1
```

`sfclg_other_sources.tsv`, `dblink_by_container.tsv` and `recattrib_by_pub.tsv` must all be
**identical** — the GFF3 load reads `db_link` and writes neither it nor any other genome
location source. `gff3_by_feature.tsv` and `gff3_attr_by_key.tsv` are expected to move (see
step 5).

## 5. Idempotency of the staging tables

`gradle loadGff3NcbiFile` (`NCBIGff3Processor`) writes six things. All six are now safe to
re-run.

| Table | Written by | Re-runnable? |
|---|---|---|
| `sequence_feature_chromosome_location_generated` | `upsertSequenceFeatureChromosomeRecords()` | **Yes** — keyed on accession, writes start/end/chromosome only when they differ |
| `marker_assembly` | `gene.getAssemblies().add(grcz12tu)` | **Yes** — `@ManyToMany` `Set`, backed by `UNIQUE (ma_a_pk_id, ma_mrkr_zdb_id)` |
| `marker_annotation_status` | `gene.setAnnotationStatusTerms(Set.of(term))` | **Yes** in outcome — see the caveat below |
| `gff3_ncbi_attribute`, key `gene_id` | `addGeneIDToAttributes()` | **Yes** — delete-all-then-re-derive |
| `gff3_ncbi` | `processNcbiGff3()` | **Yes** — `deletePreviousGeneration()` |
| `gff3_ncbi_attribute`, keys `gene`, `gene_name`, `Parent`, `ID`, `Dbxref` | cascade from the parent record | **Yes** — dropped with their parent |

`gff3_ncbi` had been insert-only: nothing anywhere deleted from it, so every run appended a
second full copy of the GFF3 file, and `Gff3Ncbi.getGeneID()` / `getGeneZdbID()` pick from a
record's attributes with `findAny()` — duplicated *records* made those answers arbitrary in
exactly the way duplicated `gene_id` *pairs* did. `processNcbiGff3()` now takes the highest
`gff_pk_id` before it starts and deletes everything at or below that watermark once a
complete new copy is in place.

Deleted **after** the load, not before. `gff3_ncbi` is not private to this job — `Gff3Writer`
reads it back to emit `zfin_genes.grcz12.gff3` and `markerAssemblyUpdate.sql` joins against
it — so clearing up front would leave those readers with an empty or half-filled table for
the length of the run, and a run that died midway would leave nothing at all. This way the
previous copy stays intact until a complete replacement exists, at the cost of one run's
worth of rows transiently.

### What to check

Run `gradle loadGff3NcbiFile` a second time. It is now a real idempotency test rather than
something to avoid:

- `gff3_by_feature.tsv` and `gff3_attr_by_key.tsv` must be **identical** to the first run's.
  Before this change both roughly doubled.
- The log must carry `Cleared the previous run's staging data: N gff3_ncbi records and M
  attribute pairs.`, with N equal to the record count the first run reported.
- The report gains a `Previous run's records cleared` row; on a first-ever run against an
  empty table it logs `No previous gff3_ncbi records to clear.` instead.
- No record may carry more than one `gene_id` pair:

```sql
select gna_gff_pk_id, count(*)
  from gff3_ncbi_attribute
 where gna_key = 'gene_id'
 group by 1 having count(*) > 1;   -- expect 0 rows
```

- `addGeneIDToAttributes()` will now normally log `Cleared 0 stale gene_id attribute pairs`,
  because `deletePreviousGeneration()` has already taken them. That delete is kept
  deliberately: the GFF3 file may itself carry a `gene_id` attribute (it is in
  `persistKeySet`), and `markerAssemblyUpdate.sql` writes `gene_id` pairs of its own between
  runs.

### Caveat on `marker_annotation_status`

`setAnnotationStatusTerms(Set.of(term))` *replaces* the `@ManyToMany` collection rather than
adding to it, so it discards whatever else the gene held. The vocabulary has only two terms —
`Current` (12) and `Not in current annotation release` (13) — so the only thing it can destroy
is a 13, and only for genes on the new-location branch.

This self-repairs. `loadNCBIgeneAccs.sql` does `DELETE FROM marker_annotation_status;` and
rebuilds the whole table from `db_link` plus `not_in_current_release` (loaded from
`notInCurrentReleaseGeneIDs.unl`), so the next NCBI-Gene-Load-Java run recomputes every row
from authoritative inputs the GFF3 load never touches. The exposure is the window between the
two jobs, during which an affected gene page reads "Current" when it should read "Not in
current annotation release". Left alone deliberately.

### Two related gaps, not fixed here

- **`NCBIGff3PostProcessor.markZfinGeneRecords()` still has the unfixed `gene_id` bug** — it
  persists a pair per gene record with no preceding delete, as `addGeneIDToAttributes()` did
  before this branch. Deliberately left alone: it is a manual `gradle postLoadGff3NcbiFile`
  with no Jenkins job behind it, so nothing runs it on a schedule and it cannot drift on its
  own. Fix it if it ever gets automated.
- **`markerAssemblyUpdate.sql` line 114's `ON CONFLICT (gna_pk_id) DO NOTHING` is inert** —
  `gna_pk_id` is a fresh serial, so it never conflicts. What actually stops that statement
  duplicating `gene_id` pairs is `temp_new_gene` emptying out on repeat runs.

## 6. Pre-flight: confirm there is drift to reconcile

Read `~/ncbi-test/04-after-gff3-run1/drift_by_category.tsv`.

**If it is empty, stop and seed drift.** Otherwise the reconciliation runs over nothing and
step 8 proves nothing.

Seed on the `sfclg` side, not `db_link`: the gene load rewrites `db_link` and would wipe
anything planted there, whereas `sfclg_data_zdb_id` survives until reconciliation touches it.
That is also the exact shape real drift takes.

```sql
begin;
-- ORPHANED           point a row at a gene holding no NCBI Gene link at all
-- REMAPPED           point a row at another gene that does hold links
-- duplicate collision  as REMAPPED, plus insert a row on the target gene at the same
--                      chromosome/start/end so the re-point violates the unique constraint
-- real conflict        as above but at different coordinates; must survive as an ERROR row
-- AMBIGUOUS          use an accession listed in existing_many_to_many_report.csv
commit;
```

Record the `sfclg_pk_id` values you seed — each is asserted on by hand at step 8.

## 7. Run NCBI-Gene-Load-Java

Reproducible route, inputs pinned so two runs see identical files (see
`docker/ncbiload-inputs/README.md` for assembling `set1`):

```bash
docker compose run --build --rm -it ncbiload bash -lc \
  'export WORKING_DIR=$SOURCEROOT/server_apps/data_transfer/NCBIGENE; cp /tmp/inputs/set1/* $WORKING_DIR; \
   EMAIL_TO_FILE=true NO_SLEEP=1 SKIP_DOWNLOADS=1 LOAD_NCBI_ONE_WAY_GENES=true \
   DB_NAME=zfindb TARGETROOT=$SOURCEROOT ROOT_PATH=$SOURCEROOT gradle ncbiLoadPort'
```

Do **not** set `EARLY_EXIT=1`. It quits before the SQL runs, and `reconcileNcbiGenomeLocations()`
executes after the marker-assembly update so it sees final `db_link` state.

The Jenkins job runs `gradle ncbiPort`; that resolves to `ncbiLoadPort` through Gradle's
camel-case task abbreviation.

```bash
./snap.sh 08-after-gene-load
```

## 8. Compare after the gene load

### Drift must be 0, except rows reported as ERROR

```bash
cat ~/ncbi-test/08-after-gene-load/drift_by_category.tsv
```

Reconcile against the log line the load prints:

```
Genome location reconciliation: N re-pointed, N deleted, N could not be reconciled.
```

re-pointed + deleted + failed must equal the pre-load drift total, and failed must equal the
residual drift.

### Each seeded row, by hand

| Seeded case | Expected outcome |
|---|---|
| ORPHANED | row deleted |
| REMAPPED | `sfclg_data_zdb_id` now the new gene |
| duplicate collision | row deleted, **and** the target's correct row still present |
| real conflict | row byte-identical to what it was, reported as ERROR |
| AMBIGUOUS | row unchanged, reported as ERROR — never re-pointed at whichever gene sorted first |

### No coordinates lost

```sql
-- expect 0 rows
select l.sfclg_acc_num
  from <baseline copy> l
 where not exists (select 1 from sequence_feature_chromosome_location_generated n
                    where n.sfclg_location_source = 'NCBILoader'
                      and n.sfclg_acc_num = l.sfclg_acc_num)
   and exists (select 1 from db_link d
                where d.dblink_acc_num = l.sfclg_acc_num
                  and d.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1');
```

### Nothing out of scope moved

```bash
diff -r ~/ncbi-test/04-after-gff3-run1 ~/ncbi-test/08-after-gene-load
```

Expected to move: `sfclg_ncbiloader.tsv`, `dblink_ncbigene.tsv`, `drift_by_category.tsv`,
`marker_annotation_status.tsv`, `marker_assembly.tsv`, and the `ZDB-FDBCONT-040412-1` row of
`dblink_by_container.tsv` plus the seven other containers the load writes (GenBank RNA/DNA,
GenPept, RefSeq RNA/Peptide, Vega).

Also expected to move, because `markerAssemblyUpdate.sql` runs as part of this load and is
easy to forget — it writes three things outside the obvious target:

- **`sfclg_other_sources.tsv`, the `ZFIN` source only.** Lines 73-99 insert
  `sfclg_location_source = 'ZFIN'` rows at GRCz12tu coordinates for genes in `temp_new_gene`.
  Every *other* source in that file must still be identical — `NCBIStartEndLoader` above all,
  since `reconcileNcbiGenomeLocations()` filters on `NCBILoader` and its deliberate exclusion
  of the start/end rows is exactly what this check proves.
- **`gff3_attr_by_key.tsv`, the `gene_id` key only.** Line 103 inserts `gene_id` pairs into
  `gff3_ncbi_attribute` so the outgoing `zfin_genes.grcz12.gff3` carries the attribute jBrowse
  needs. Every other key must be unchanged, and `gff3_by_feature.tsv` must be unchanged — the
  gene load writes the attribute table but never `gff3_ncbi` itself.
- **`marker_assembly.tsv`** gains GRCz12tu (`ma_a_pk_id = 1`) and GRCz11 (`ma_a_pk_id = 3`)
  rows.

Note while you are in that file: the `ON CONFLICT (gna_pk_id) DO NOTHING` on line 114 does
nothing — `gna_pk_id` is a fresh serial on every insert, so it can never conflict. What
actually stops that statement duplicating `gene_id` pairs run over run is `temp_new_gene`
emptying out, since its `not exists` guard drops any gene that already has the GRCz12tu
`marker_assembly` row. That is the same class of bug as the one fixed in
`addGeneIDToAttributes()`, just currently masked. Worth confirming the count only rises by
the size of `temp_new_gene` (the SQL prints it: `select * from temp_new_gene`).

Must be **identical**:

- every other container hash in `dblink_by_container.tsv` — InterPro, UniProtKB, Pfam,
  PROSITE, Ensembl, PANTHER and the rest. These now render by their real name in the report,
  which makes it much easier to notice if the load ever starts writing them; the hashes are
  how you notice.
- `gff3_by_feature.tsv`.
- every pub in `recattrib_by_pub.tsv` except `ZDB-PUB-130725-2` (legacy Vega, expected to
  fall by the dropped count), `ZDB-PUB-020723-3` and `ZDB-PUB-230516-87` (the two live
  strategies, rebuilt every run).

## 9. Read the report — `ncbi_report.html`

None of the reporting fixes show up in a database diff.

- **"Number of db_link records updated"** should be small, and every entry should show a real
  change. The run-55 symptom was 27,066 updates whose After column was empty. Open several
  "Updated …" actions: each field renders as `Field | Value`, unchanged fields collapsed to a
  single value, changed fields as `before -> after`, blanks as `(none)`.
- **No "Unknown Foreign DB" group.** InterPro, UniProtKB, Pfam, PROSITE, Ensembl, PANTHER and
  the rest must each appear under their real `foreign_db` name.
- **"NCBI Gene ID matches by strategy"** table present, with all three strategies and the
  Ensembl-supplement funnel. Cross-check the numbers against `logNCBIgeneLoad.txt`.
- **"Annotation Status without NCBI Gene ID"** should report 0.
- **"Dropped Legacy Vega NCBI GeneID"** — expect roughly 22 of 149, split between "NCBI no
  longer cross-references any ZFIN gene for this accession" and "NCBI now cross-references a
  different ZFIN gene". Log line: `Legacy Vega links: N re-asserted, N dropped`. Confirm the
  surviving `ZDB-PUB-130725-2` link count fell by exactly the dropped number.
- **Genome location actions** — UPDATE / DELETE / ERROR tables, ERROR rows naming what the
  target gene already holds so a curator can act on them.

## 10. Load-NCBIStartEndPositions

Separate job, separate `sfclg_location_source`, untouched by reconciliation. Not part of the
sequence above.

```bash
perl -c server_apps/data_transfer/NCBIStartEnd/NCBIStartEnd.pl
```

Then force a failure (point the wget at a bad path) and confirm an **email is produced**
rather than:

```
Can't locate object method "sendMailWithAttachedReport" via package "ZFINPerlModules"
```

That is the whole point of the fix — the job had been failing silently since 2025-05-23
because its error handler died before it could report. Separately, confirm the real download
now resolves under `all_assembly_versions/suppressed/`.

## 11. Regression surface

`LegacyReportAdapter` is shared. `UniProtLoadTask`, `UniprotSecondaryTermLoadTask`,
`ReportBuilder` (the GFF3 report) and `LoadActionReportAdapter` all render through it, so the
`tryParseKeyValueTable` and `displayNameForForeignDB` changes reach every one of them.

Eyeball a UniProt report and `gff3_ncbi_report.html` for mangled tables before merging.
