# Unified DANRE-mod GO load — consolidation findings & open decisions (ZFIN-10025)

Durable reference for the effort to replace the three GO-annotation loads
(**GAF-GOA**, **Noctua GPAD**, **FP-Inference**) — plus the UniProt-Secondary load's
`*2go` GO-mapping stages — with **one** load consuming GO Central's per-species GPAD
file(s). It captures the findings and unresolved decisions that must survive past the
feature branch. The moment-to-moment development log (per-run chronology, superseded counts)
was scratch and is not merged; its conclusions are here and the per-run history is in the Jira
tickets.

**How to actually run any of this: `RUNBOOK-danre-mod-load.md`, next to this file.**

Related code: `DanreModGpadParser`, `DanreModSourceOrganization`, `GafLoadJob`
(`source/org/zfin/datatransfer/go/`). QC tooling in this directory: `mgte_snapshot.sh` +
`snapshot_mgte.sql`, and `mgte_csvdiff.sh` (wrapping `gradle csvDiff`).
Jenkins: `Load-GPAD-GO-Central_m`.

Both scripts take **`--others`**, which adds one extra snapshot/diff pair holding every row whose
organization is *not* among those named. It should always be empty; a non-empty
`mgte_dbdiff_OTHER.xlsx` means rows are being written somewhere nobody is watching. Naming
organizations explicitly is precisely what hid `PAINT` from every artifact until it was added by
hand, so the jobs pass `--others` as standing insurance against the next such gap.

Status: **pre-cutover.** The `load-gpad-danre-mod` Ant target still *defaults* to
`GAF_LOAD_REPORT_ONLY=true` (no DB writes), but it is now resolved from the environment rather
than hardcoded, so it can be overridden.

> **`Load-GPAD-GO-Central_m` is DISABLED until cutover** (decided 2026-08-14), and enabling it
> is one half of a two-part switch — see open decision 2. The other half is turning the
> secondary load's `*2go` GO streams off (`LOAD_INTERPRO2GO_EC2GO=false`, `LOAD_KW2GO=false`)
> and purging the pre-existing `UniProt`-org **interpro2go + ec2go** rows (70,062 — *not*
> kw2go, which has no successor). Do those together: enabled while
> those streams are still on, the same InterPro2GO/EC2GO content exists twice under two
> organizations and neither side prunes the other.
>
> ⚠️ Note the job defaults `GAF_LOAD_REPORT_ONLY` to `false` — **it WRITES** — and is
> registered in `jobs.production.properties`. Disabled is what currently stops a stray
> "Build Now" on production from writing ahead of sign-off. Tick the parameter for a dry run.

**Source file:** `current.geneontology.org/annotations/gpad/DANRE-mod.gpad.gz`, which the
Ant target and Jenkins job already default to. GO regenerates it periodically; the figures in
this document were measured against the build generated 2026-08-04.

---

## How the load owns rows (two org fields — don't conflate them)

Every `marker_go_term_evidence` row carries two organization fields:

- **`gafOrganization`** (`mrkrgoev_annotation_organization` → FK to
  `marker_go_term_evidence_annotation_organization`) = **which load owns the row**
  (`GOA` / `Noctua` / `FP Inferences` / `UniProt` / …). Removal is scoped by this, so a
  source can only prune its own rows.
- **`organizationCreatedBy`** (`mrkrgoev_annotation_organization_created_by`) = the
  source's own **`assigned_by`** (GPAD col 10: `ZFIN` / `UniProt` / `InterPro` / …).

`DanreModSourceOrganization` maps `assigned_by → gafOrganization`: `ZFIN → Noctua`,
everything else → `GOA`. So the unified load only ever writes/removes in the **GOA** and
**Noctua** orgs.

⚠️ `organizationCreatedBy = ZFIN` is **not** a safe discriminator: it tags both the
Noctua curated rows *and* the UniProt-Secondary `*2go` rows (see below). Identify sets by
`gafOrganization`.

---

## What the secondary UniProt load consumes (its GO inputs)

The UniProt-Secondary load does **not** ingest ready-made GO annotations — it *derives*
them in-house by joining two inputs:

**(A) per-protein cross-references** from the primary UniProt load's processed `.dat`
release (`UNIPROT_INPUT_FILE`): each zebrafish protein's UniProt **keywords**, **InterPro
domains**, and **EC numbers**.

**(B) GO's `external2go` translation tables**, downloaded at run time, mapping each
cross-reference to a GO term:

| stream | protein feature (A) | external2go file (B) | pub | evidence |
|---|---|---|---|---|
| interpro2go | InterPro domain | `current.geneontology.org/ontology/external2go/interpro2go` | ZDB-PUB-020724-1 | IEA |
| kw2go ⚠ | UniProt keyword | `…/external2go/uniprotkb_kw2go` | ZDB-PUB-020723-1 | IEA |
| ec2go | EC number | `…/external2go/ec2go` | ZDB-PUB-031118-3 | IEA |

Plus InterPro `entry.list` (`ftp.ebi.ac.uk/pub/databases/interpro/current_release/entry.list`)
— the domain catalog, used for the dblink/domain refresh, **not** GO terms.

So each GO row = *(protein has feature F in the UniProt release)* × *(external2go maps
F → GO term)*, attributed to the stream's pub with evidence IEA. Code:
`UniprotSecondaryTermLoadTask.loadTranslationFiles()` + `SecondaryTerm2GoTermTranslator`.
Consequence for cutover: taking these from the DANRE file instead only works where GO
still produces the mapping **and** ships it in the file — true for interpro2go/ec2go
(though `DANRE-mod` under-covers), false for kw2go (GO retired `GO_REF:0000004`).

### ⚠️ What must SURVIVE in the secondary load (scope limit for ZFIN-10344)

**The secondary load is two jobs in one. Only the GO half may be retired.** An early draft
of the background notes had this backwards — it placed the InterPro/EC/protein/keyword
*data refresh* in the **primary** UniProt load, leaving only the external2go GO mappings in
the secondary load. Acting on that reading would delete the non-GO refresh along with the
GO handlers, and **`DANRE-mod` supplies none of it**. Verified against the code
(`UniprotSecondaryTermLoadTask.calculatePipelineActions()`):

| lines | handlers | cutover disposition |
|---|---|---|
| 333–343 | dblink refresh: `Remove`/`AddNewDBLinksFromUniProts` × `INTERPRO`, `EC`, `PFAM`, `PROSITE` | **KEEP** |
| 346–347 | `MarkerGoTermEvidenceActionCreator(INTERPRO, ipToGoRecords)` / `(EC, ecToGoRecords)` — interpro2go + ec2go GO terms | **DROP** |
| 349–350 | `AddNewSpKeywordTermToGo` / `RemoveSpKeywordTermToGo(UNIPROTKB, upToGoRecords)` — kw2go GO terms | **DROP** (no file successor) |
| 352–355 | `InterproDomain`, `InterproProtein`, `InterproMarkerToProtein`, `ProteinToInterpro` | **KEEP** |
| 356 | `PDBActionCreator` | **KEEP** |

So ZFIN-10344 removes **four handler registrations (lines 346–350) covering the three
`*2go` streams** — nothing else. The **primary** load (`UniProtLoadTask`, handlers at
213–220) does *only* protein→gene matching (UniProtKB dblinks); its pipeline is entirely
match / ignore / delete / obsolete handlers, with no dblink, domain, or GO work to inherit
the refresh. Whichever DANRE file we adopt, the dblink/domain/PDB refresh has no
replacement source and the secondary load must keep running for it.

---

## How an incoming row matches a stored annotation

Worth knowing before reading any add/remove count, because **most "removals" in the early runs
were matching failures, not lost data**. An incoming GPAD row matches a stored
`marker_go_term_evidence` row only when **all** of publication, marker, evidence code, flag and
qualifier-relation agree (`getLikeMarkerGoTermEvidencesButGo`), and then GO-term specificity plus
inferences are compared (`isMoreSpecificAnnotation`). Any one field differing means no match: the
incoming row counts as **added** and the stored row as **removed**.

That is why a representation change looks like loss. The `ECO:0007322` gap was exactly this — the file carried the same gene→GO under the same `GO_REF:0000044`, only tagged
with an ECO we had not mapped, so ~24k stored rows were flagged for removal while the
corresponding incoming rows errored out. One mapping row fixed both sides.

## Ownership rules the `assigned_by → org` map reproduces

The legacy loads partitioned a **shared** `assigned_by` vocabulary through parser filters, so the
map has to encode the same rules or the first cutover diff is churn instead of a near-no-op:

- **`ZFIN` → Noctua.** The GAF/GOA path *rejects* `ZFIN`-created rows (`FpInferenceGafParser`,
  "skip own annotations") and defers them to the Noctua load, which does not filter — which is
  why Noctua owns them.
- **`GOC` → dropped today.** `GoaGafParser` rejects `createdBy == "GOC"`, so DANRE-mod's
  `GO_REF:0000108` rows are **net-new content no current load ingests** — a consolidation gain to
  decide on, not a regression (open decision 3).
- **`GO_Central` / `GO_REF:0000033` (phylo IBA) → GOA.** ~48k rows. The standalone FP-Inference
  file is only ~1,809 rows and is a small overlapping subset, which is what makes FP-Inference
  redundant under consolidation.
- **Everything else** (`UniProt`, `InterPro`, `RHEA`, `IntAct`, …) **→ GOA**, matching legacy GOA
  ownership.

⚠️ **An unmapped `assigned_by` must be rejected and reported, never assigned to a catch-all
org.** A catch-all would put those rows inside some other org's removal scope, which is the one
failure mode that can mass-delete another source's annotations.

---

## Comparison method (QC harness)

`snapshot_mgte.sql` flattens one org's annotations (with child-table dimensions:
inferred_from, annotation_extensions, noctua_model) into a CSV, resolving ids to readable
`gene`/`go_id`/`go_term`/`go_aspect`/`relation_name` columns. `gradle csvDiff` then diffs
two snapshots, keyed on the identity columns and **ignoring** `zdb_id` + the readable
columns. Run per organization. The "legacy vs unified" comparison loads the same DB
snapshot down two paths (legacy 3 loads + dedup vs. the unified load) and diffs the
per-org end states.

Both comparisons below use the **2026.07.05.1** DB snapshot with dedup on both sides (dedup
removed 0 rows from the unified load). Because that snapshot already reflects current production
loads (re-running them was a near no-op), these diffs **are** the real cutover impact, not test
artifacts.

Comparison against the current DANRE-mod file — baseline vs unified, report-only OFF:

| Org | baseline | unified | deleted | added |
|---|--:|--:|--:|--:|
| GOA | 109,656 | 174,773 | 44,967 | 110,084 |
| Noctua | 36,025 | 31,446 | **5,103** | 524 |
| FP Inferences | 1,623 | 1,623 | 0 | 0 |
| UniProt (2° load) | 111,089 | 111,089 | 0 | 0 |

Two caveats. This is *baseline* vs unified rather than *legacy* vs unified — equivalent for
Noctua/FP/UniProt (re-running the legacy loads was a no-op for those) and differing by ~1,813
rows on GOA. And **45,159 of GOA's 110,084 adds are the `*2go` org-mismatch duplication**
(40,723 interpro2go + 4,436 ec2go; finding 2), not new GO content; excluding it GOA grows
+19,958. The
remaining GO_Central/PAINT IBA rise (36,250 → 62,288 distinct) is real and worth confirming
upstream as intended.

---

## Findings

### 1. Noctua: ~2,284 experimental annotations still lost

The unified load drops **5,103** Noctua (ZFIN-curated) annotations that the legacy Noctua GPAD
load carries. Of those, **2,819 are `ND`** — root-term "no biological data available", which the
GOA pipeline does not carry and which arguably should not count as loss. That leaves
**~2,284 genuinely experimental annotations** absent from the DANRE-mod file.

By evidence code and by relation:

| evidence | lost | | relation | lost |
|---|--:|---|---|--:|
| ND | 2,819 | | `acts_upstream_of_or_within` (RO:0002264) | 1,742 |
| IMP | 1,363 | | `enables` | 1,293 |
| IGI | 272 | | `is_active_in` | 1,004 |
| IDA | 209 | | `involved_in` | 944 |
| IPI | 195 | | `located_in` | 41 |
| ISS / NAS / TAS / other | 245 | | ± effect / part_of / other | 79 |

These are annotations GO Central's export does not carry, so the loader has nothing to ingest —
**a GO-pipeline / curation-policy question, not a loader bug.**

Decision (GO / curators): (a) get GO to retain the remaining subject-level Noctua annotations in
the export, (b) keep the Noctua GPAD load as a supplementary source until then, or (c) accept the
loss with a rationale. Also worth confirming with GO that the `ND` omissions are deliberate.
Whichever way it goes, it must not be dropped silently at cutover.

### 2. `*2go` handover: org-mismatch duplication, under-coverage, and a fully-dropped stream
The UniProt-Secondary load produces **three** `*2go` IEA streams, all stored under
`gafOrganization=UniProt` (`organizationCreatedBy=ZFIN`). The unified load resolves the
equivalent content to `GOA` (`assigned_by=InterPro`/`UniProt` → GOA). A per-org `csvDiff`
never compares across orgs, so the old copies read "unchanged in UniProt" (0/0) while the
new copies land as "GOA adds":

| `*2go` stream | pub | legacy `UniProt` | GO_REF | in `DANRE-mod` (→GOA) | in `DANRE-uniprot` (raw rows) |
|---|---|--:|---|--:|--:|
| InterPro2GO | ZDB-PUB-020724-1 | 65,327 | GO_REF:0000002 | 40,723 | 43,882 |
| **UniProtKB-Keyword (kw2go/spkw2go)** | ZDB-PUB-020723-1 | **41,027** | GO_REF:0000004 | **0** | **0** |
| EC2GO | ZDB-PUB-031118-3 | 4,735 | GO_REF:0000003 | 4,436 | 5,400 |

> ⚠️ **The `DANRE-uniprot` column is RAW ROWS and is misleading — see the 2026-08-07
> measurement below. Gene-collapsed, `DANRE-uniprot` and `DANRE-mod` carry the SAME content
> (InterPro2GO 28,501 in both).** The apparent surplus is per-accession multiplicity.

Three problems at cutover:
- **Duplication.** The unified load's removal scope is GOA+Noctua only, so it never
  touches the 111,089 UniProt-org rows — dropping the secondary `*2go` handlers leaves
  those *and* the new GOA copies, storing each mapping twice. Purge by
  `gafOrganization='UniProt'`, **not** `created_by=ZFIN` (which also tags Noctua).
  The UniProt org is *exactly* these three streams (65,327 + 41,027 + 4,735 = 111,089),
  so the purge is cleanly scoped — verified 2026-08-07.
- **Under-coverage.** `DANRE-mod` supplies **40,723** InterPro2GO vs the secondary load's
  **65,327**. Switching source files does *not* close the gap — `DANRE-uniprot` carries the
  same content (§2a).
- **kw2go is dropped entirely.** GO Central retired keyword→GO mapping (`GO_REF:0000004`);
  it is absent from *both* files. So the 41,027 UniProtKB-Keyword annotations have **no
  file successor** — the branch's `GoDefaultPublication` maps GO_REF:0000002/0000003 but
  nothing for keyword2go, because there is nothing to map.

**How much of the would-be-lost `*2go` is a true loss vs. subsumed by a more-specific
retained term?** Measured 2026-08-17 against the **loaded end state** — everything the new load
owns after a full run (`GOA` + `Noctua` + `PAINT`) — rather than against the raw file, because
that is what determines whether a gene actually still has the term. A lost `(gene, GO_lost)` is
"subsumed" if the gene retains a strict `is_a`/`part of` **descendant** of `GO_lost`:

| stream | pairs | would-be-lost | ↳ subsumed | ↳ **true loss** |
|---|--:|--:|--:|--:|
| kw2go | 40,408 | 25,378 | 14,471 (57%) | **10,907** |
| interpro2go | 41,118 | 1,935 | 365 (19%) | **1,570** |
| ec2go | 4,720 | 585 | 292 (50%) | **293** |
| **total** | **86,246** | **27,898** | **15,128 (54%)** | **12,770** |

So kw2go is essentially the whole problem: interpro2go and ec2go are reproduced almost entirely
by the new load (1,935 and 585 pairs unaccounted for, against 41,118 and 4,720), which is what
makes them safe to hand over. kw2go has no successor at all — see open decision 4.

⚠️ **Granularity matters when comparing to older figures.** This counts distinct `(gene, GO)`,
the question a curator would ask. An earlier pass counted distinct
`(gene, relation, GO, GO_REF, ECO)` and reported 30,751 true losses; on that finer key a
re-sourced annotation always differs (the `GO_REF` changes by definition), so it measures
annotation churn rather than what a gene loses. Use the table above.

⚠️ Some accessions have a gene↔protein `db_link` gap (e.g. ppardb/A9C4A5) — genuinely uncovered
by either file, independent of subsumption.

### 2a. The load consumes `DANRE-mod`, not `DANRE-uniprot`

Gene-collapsed, the two files carry the same content — `DANRE-uniprot` ONLY is on the order of
**9 annotations**, its apparent surplus being raw per-accession rows. Consuming it would add an
accession→gene mapping layer, with its own `db_link` gaps, for nothing. It also has no
gene-product-form field, so it cannot rescue finding 10 either. Settled; do not re-open without
new evidence.

### 5. GOA churn is turnover, not loss
GOA's 42,131 deletes / 50,585 adds are almost all IEA representation change + monthly
UniProt turnover (the 7/5 DB is ~3 weeks newer than the 6/17 file) plus net-new content
(GOC `GO_REF:0000108`, the `*2go` IEAs). Not curation loss.

### 6. Merged/retyped ZFIN IDs aren't remapped on import (small bug, ~23 annotations)
The Noctua GPAD load (and FP / DANRE-mod, which share `GafLoadJob`) throw
`No gene found for ID: …` for genes merged or type-changed in ZFIN *after* they were curated
in Noctua — the file still carries the old id. ~11 genes / ~23 annotations currently; all
resolve in one hop via `zdb_replaced_data`:

| old id (in file) | resolves to | kind |
|---|---|---|
| ZDB-GENE-080305-14 | fbxw12 (ZDB-GENE-090311-15) | merge |
| ZDB-GENE-080723-5 | si:dkey-199f5.7 (ZDB-GENE-100922-57) | merge |
| ZDB-GENE-080723-51 | zgc:194215 (ZDB-GENE-080723-25) | merge |
| ZDB-GENE-131121-474 | ccdc162 (ZDB-GENE-131121-612) | merge |
| ZDB-GENE-150701-2 | cdh16 (ZDB-GENE-140106-140) | merge |
| ZDB-GENE-200316-1 | cd44b (ZDB-GENE-110429-2) | merge |
| ZDB-GENE-080410-2 | nc.terc (ZDB-NCRNAG-080410-1) | type change |
| ZDB-GENE-090929-315 | nc.rny2 (ZDB-NCRNAG-090929-1) | type change |
| ZDB-GENE-111201-3 | nc.rny1 (ZDB-NCRNAG-111201-1) | type change |
| ZDB-GENE-150915-1 | sno.scarna1 (ZDB-SNORNAG-150915-1) | type change |
| ZDB-LINCRNAG-050208-65 | bin1b (ZDB-GENE-030425-1) | type change |

**Root cause — two gaps in the existing `GafService.replaceMergedZDBIds` (the resolver
already exists, it just misses these):**
1. **Prefix mismatch (primary).** It remaps `entryId` at `GafLoadJob:164`, *before* the
   `ZFIN:` prefix is stripped (line 174), but `replaceAttributeOnGafEntry` does
   `containsKey(fullEntryId)` against a map keyed by **bare** `ZDB-…` ids — so a
   `ZFIN:ZDB-GENE-…` entryId never matches. (The with/from remap strips `ZFIN:` correctly;
   the entryId branch doesn't.) Net: entryId-level merge handling has effectively never
   worked for the ZFIN-id GPAD loads — which is why even plain GENE→GENE merges error.
2. **Type coverage.** The map is built only for old-id types `GENE` + `MRPHLNO`
   (`getReplacedDataMapFromEntities(GENE, MRPHLNO)`), so a non-GENE old id like
   `ZDB-LINCRNAG-050208-65` is missed even after fixing #1.

**Fix (IMPLEMENTED, ZFIN-10025 branch):** `replaceMergedZDBIds` now strips a leading
`ZFIN:` before the entryId map lookup (mirroring the with/from handling), broadens the
replaced-data map to all gene/RNA + morpholino marker types, dedupes, and returns the
applied remaps as `{oldId, newId, symbol}`. `GafLoadJob` stashes them on `GafJobData`;
`GafReportBuilder` surfaces them (a summary "IDs corrected (merged/retyped)" count + an INFO
node listing `old id → current marker`). `getGenes()` already routes `*RNAG` ids to
`getGeneByID`, so type-changed targets (NCRNAG/SNORNAG) load once remapped.

**Verified** (report-only Noctua run, 2026-07-10): gene-not-found errors → **0**; the
annotations load under their current markers (nc.terc, fbxw12, nc.rny2, sno.scarna1, …);
the report's new section reports **58** corrected IDs (superset of the 11 above — the
entryId remap was fully broken before, so every merged subject id was erroring).

### 8. Upstream defect: the production file duplicates 53% of its rows (open, GO-side)

The published DANRE-mod file contains **459,621 data rows that collapse to 214,064 distinct
annotations**. The redundant 245,557 are byte-identical in all 12 GPAD
columns *except* the `id=GOA:` value, at multiplicities up to 52×. Because that field is unique
per row a plain `sort -u` finds nothing; it has to be stripped first, which may be why it
cleared GO's QC:

```bash
zcat DANRE-mod.gpad.gz | grep -v '^!' | sed -E 's/id=GOA:[0-9]+\|?//' | sort -u | wc -l
```

Source-correlated, which points at where to look: GO_Central is clean at 1.01×, while UniProt
(2.61×), InterPro (2.77×) and GOC (2.14×) are not — consistent with the accession→gene collapse
emitting one row per source accession without de-duplicating.

**It does not corrupt what we load.** A control run against a de-duplicated copy produced
identical added/updated/removed counts, `cleanMarkerGoTermEvidenceDuplicatesTask` removes 0
rows, and a post-load duplicate check on the GOA org finds 1,727 redundant rows (0.99%) — normal
load behaviour, not 245k. The costs are operational: **~57 min instead of ~20**, and an error log
in which ~186k of ~195k entries are "Duplicate annotation entry", burying the ~7.7k real ones.

Not a blocker; worth reporting upstream and worth knowing before anyone reads a load report.

### 10. Gene product form IDs are not carried by GPAD — 42,279 → 0 (open, accept or not)

`marker_go_term_evidence.mrkrgoev_protein_accession` holds the gene product form — which isoform
an annotation is about. The 2026.07.05.1 baseline has **42,279** populated (13,648 distinct, all
`UniProtKB:`, only **16** carrying an isoform suffix). After a load it is **0**, in every
organization the load owns. Not a decay or a cleanup bug: `FpInferenceGafParser` reads it from
**GAF column 17** (`entries[16]`), which is how the legacy GOA GAF load populated it, and
`GpadParser` overrides parsing without setting it — **0 occurrences**.

It cannot be recovered from either file. **GPAD 2.0 has no gene-product-form column at all**
(verified: all 459,621 rows have exactly 12 fields; col 1 is always a gene, `ZFIN:*` ×459,598 and
`ComplexPortal:*` ×23; `annotation_properties` carries only `id=GOA:…`; zero lines match
`gene_product_form` or `isoform`). `DANRE-uniprot` uses UniProt accessions as its *subject*, so
the accession vocabulary is present, but it has no gene-product-form field either and **no
isoform-level subjects**, so even consuming it — which would reopen decision 2a — could not
reproduce the 16 rows where the field says something a plain accession does not.

⚠️ **Do not confuse this with the with/from data, which IS carried.** UniProtKB accessions appear
in GPAD col 7 on 438,562 rows (e.g. `PANTHER:PTN000637985|UniProtKB:P37231`), and ZFIN stores
those as `inference_group_member` / `inferred_from`. For an IBA that records what the inference
was drawn *from*, not which isoform the annotation is *about* — different claims, so mapping one
into the other would be wrong.

**Consumers:** none user-facing. It is absent from `gpad2.0.sql` and `gpad.pl`, so it reaches no
download file (`gene_association.zfin`, `gpad2.0.zfin`, `gene_association2.2.zfin`), and nothing
in the UI or any DTO reads it. Its only real consumer is the cleanup's near-duplicate rule, which
prefers the row *with* an accession; that becomes inert once everything is NULL. It is also the
sole non-key, non-ignored column in the csvDiff, which is why it dominates the update sheets.

Related dead code, worth a separate ticket: `GafService.getDBLink()` is defined and never called,
and `mrkrgoev_protein_dblink_zdb_id` — the FK the cleanup's backfill derives the text from — has
never been assigned by any Java in the repository's history (`git log -S … --all` returns
nothing), so the backfill can only ever fill rows that predate the column falling out of use.

---

## Open decisions before cutover

1. **Noctua loss** (finding 1) — GO/curator conversation. **Re-measure before deciding: an
   upstream GO change has landed since these figures were taken and recaptured most of the loss.**
   The counts recorded here — 5,103 annotations, 2,819 of them ND, leaving ~2,284 experimental —
   predate it. Doug's working figure afterwards is on the order of 4,000 still missing, and the
   open question is how many of those are true losses versus rows GOA filters into its own error
   reports.
2. **`*2go` ownership org** — route the `*2go` GO_REFs to the same org the secondary load
   uses (`UniProt`) or add an explicit `UniProt`-org purge/migration at cutover, so old and
   new copies don't coexist. **Sequencing decided 2026-08-14:** `Load-GPAD-GO-Central_m` stays
   **disabled** and the secondary load's flags stay **on** until one coordinated switch —
   enable the job, set `LOAD_INTERPRO2GO_EC2GO=false` and `LOAD_KW2GO=false`, and purge the
   `UniProt`-org **interpro2go + ec2go** rows, together — kw2go is excluded, it is decision 4.
   Either half alone is a defect: job-only duplicates the
   content across two orgs, flags-only drops it with nothing supplying it. The purge is drafted
   as **`cutover-purge-uniprot-2go.sql`** in this directory — deliberately not a liquibase
   migration, so a routine `liquibasePostBuild` cannot fire it. It refuses to run unless the
   GOA-org replacement is already present, and it covers interpro2go + ec2go **only**; kw2go is
   excluded pending decision 3. Note it is a **net reduction**: −24,604 interpro2go and −299
   ec2go, because the GPAD file under-covers both (finding 2). Not yet run anywhere.
3. **kw2go (UniProtKB-Keyword, 41,027 rows)** — no file successor (GO retired
   `GO_REF:0000004`), and **the `uniprotkb_kw2go` mapping file itself is slated for
   retirement** (still served as of 2026-08-14: HTTP 200, 70 KB, modified 2026-08-08). So
   "keep loading them" is not on the table — the choice is **freeze or delete**:
   **(a)** `LOAD_KW2GO=false`, leaving the 41,027 existing rows in place, unrefreshed and
   progressively stale; or **(b)** the same plus deleting them.

   Scale (finding 2, measured against the loaded end state): of **40,408** distinct
   `(gene, GO)` pairs, 15,030 are reproduced by the new load and a further **14,471** are
   subsumed by a more-specific term the gene keeps, leaving **10,907** that genuinely disappear
   under (b).

   Subsumption is computed on a strict `is_a` + `part of` closure, deliberately *not*
   `all_term_contains` — that table also encodes `regulates` and `positively regulates`
   (verified), which would overstate it. The closure is implemented in **`mgte_subsumption.sh` /
   `.sql`**; re-derive these figures with it rather than quoting them, as they move with the
   input file. ⚠️ **Do not
   simply leave the flag on.** When the file stops being served the secondary load *fails on
   the download*, taking the dblink/domain/PDB half with it: `createTempFile` leaves a 0-byte
   destination, so `downloadFileViaWget` size-checks against the server, a missing file returns
   no `Content-Length` (`-1`), and `-1` is neither `==` nor `>` 0 → `IOException("Server file
   is smaller than local file")`, rethrown as a `RuntimeException`. The message points at the
   wrong thing and the timing is GO's, not ours. ⚠️ **No longer entangled with #2** —
   `DANRE-uniprot` carries the same content (§2a), so no source-file change rescues these.
4. **`GO_REF:0000115` (RNAcentral, 45)** — map or leave. Still open.

5. **Relation → `qualifier_relation`** — ✅ **VERIFIED, current file.** `getRelQualifier`
   (`GafService`) already fails loudly rather than silently on an unresolved relation — an
   unmatched col-3 value throws `GafValidationError("RO term ... does not exist")`, which
   rejects that row rather than dropping the qualifier — so this was a check to run, not a gap
   to close.

   Checked 2026-09-28 against the then-current production file (build 2026-08-04,
   `current.geneontology.org/annotations/gpad/DANRE-mod.gpad.gz`, 459,621 rows):

   ```bash
   zcat DANRE-mod.gpad.gz | grep -v '^!' | awk -F'\t' '{print $3}' | sort | uniq -c
   # every value matches ^(RO|BFO):[0-9]+$ — no bare relation names, no pipe-joined values
   ```

   All **12** distinct values (`RO:0002327` enables ×146,310 down to `RO:0004034` ×4) resolve to
   a non-obsolete `term` row via `getTermByOboID` — `lookupRelationTerm` takes the RO/BFO-prefixed
   branch unconditionally for these, so the ontology allowlist (`ZFIN_RO` / `GO_QUALIFIER`) never
   comes into play. Counts summed to 459,621, so every row was covered, not just a sample.

   ⚠️ **Re-check against whatever file is actually loaded at cutover**, not this one — GO
   periodically adds relations, and a newly-introduced RO/BFO term would surface as parser
   errors in `_error_summary.txt` rather than as a silent qualifier loss, but that only helps if
   someone reads the summary. The one-liner above is cheap enough to re-run on the release
   candidate as part of the pre-cutover report-only check (decision 8).
6. **Phylo IBA org** — `GO_REF:0000033` → **`PAINT`**, keyed on the reference rather than
   `assigned_by` (the latter is only nearly a proxy, and the few rows that differ would be
   mis-homed). Implemented.

   ⚠️ **The code change alone does not re-home existing rows.** The matcher deliberately does not
   key on organization, so an incoming phylo row matches the stored GOA copy and that row stays
   in GOA; only unmatched rows are inserted with `PAINT`. Cutover must therefore run
   `cutover-rehome-phylo-to-paint.sql` — see RUNBOOK §13, which sequences it. Left undone, phylo
   splits across GOA and PAINT and survives only because matching is org-agnostic.

   **Separate: the FP-Inference rows are not rescued by this.** Of their 1,623 distinct
   (gene, GO) pairs only ~480 are reproduced by the new load's phylo content; the rest are
   FP-only. Giving phylo its own org gives those rows a natural home but does not supply their
   content.

   **Agreed approach: purge the rows and retire `Load-GAF-FP-Inference_m`**, following the `*2go`
   pattern. `cutover-purge-fp-inference.sql` implements it.

   ⚠️ **Not yet wired into `RUN_CUTOVER_SCRIPTS`, and it should not be until the premise is
   re-confirmed.** The agreement was reached on the understanding that the FP annotations arrive
   in the new GO input file. They mostly do not: ~1,143 of the 1,623 pairs have no successor
   there. Purging on that basis drops rows nobody intended to drop. The case below argues for
   purging anyway, for a different reason — confirm on that basis before enabling it.

   **Where those rows come from.** `Load-GAF-FP-Inference_m` — enabled but with an empty cron
   `spec`, so it runs only when triggered — calls Ant `load-gaf-fpinference`, i.e. `GafLoadJob`
   with the organization fixed to `FP Inferences` and `FpInferenceGafParser`, against
   `https://current.geneontology.org/products/upstream_and_raw_data/zfin-prediction.gaf`.
   Note `upstream_and_raw_data`: these are GO's **raw PANTHER predictions**, a different pipeline
   stage from the released `DANRE-mod` product. The file is uniform — 100% IBA /
   `GO_REF:0000033` / `assigned_by=GOC`, subjects keyed on UniProtKB accessions — and so are the
   stored rows, all on `ZDB-PUB-110330-1`.

   **Why the FP-only pairs have no successor is not a gene-mapping problem.** Of the ~1,000 genes
   carrying them, the overwhelming majority are present in `DANRE-mod` and most already carry
   phylo annotations there; only a couple of dozen are absent entirely. GO knows these genes and
   is making phylo calls for them — just not these ones, and the orphaned terms are generic
   parents (signal transduction, GPCR signaling, transmembrane transport, synapse). They read as
   stale predictions GO has since refined or dropped.

   That shifts the decision: freezing them in the dead org does not preserve unique knowledge, it
   preserves superseded predictions sitting beside fresh GO phylo content on the same genes, with
   nothing marking them stale and no load to refresh or prune them. ⚠️ Not yet proven — the
   subsumption closure (`mgte_subsumption.sh`) has not been run against them.
7. **Gene product form IDs (finding 10)** — accept the loss, or not? **42,279 → 0** on every
   load. GAF col 17 carried it; GPAD 2.0 has no equivalent column and neither DANRE file supplies
   one, so this is upstream and not a parser gap we can close. Reaches no download file and no UI.
   Recommend accepting explicitly rather than letting a 42,279-row field empty silently.

   **Update (2026-09-28):** GO has indicated they are working on adding this to a future GPAD
   release, so the loss may be temporary rather than permanent — re-check the file's columns
   before assuming it is still absent on a future cutover attempt. Until then the recommendation
   stands: accept explicitly, sourced from GO's own stated intent, not from silence.
8. **First-cutover removal scope** — the initial map must reproduce legacy ownership closely
   enough that the first real diff is ~no-op rather than a mass add+remove. Findings 1 and 5
   quantify what is left after the `ECO:0007322` fix; this is the go/no-go check, run
   report-only, immediately before flipping the flag.
9. **ND filtering** — ✅ **IMPLEMENTED (2026-09-28)**, pending Doug/Pascale sign-off on scope.
   ZFIN-10464 comment 12 (2026-09-19). Doug, relaying Pascale: GO/GOA know that `ND` annotations
   coexist with real ones on the same GO aspect and **plan** an upstream filter, but it is not in
   place. He asks ZFIN to implement one, *"either a filter on the incoming file or after the
   load."*

   Worked example (comment 10), all on `ZDB-GENE-060918-2` (crygm2d3): `GO:0008150`
   (`biological_process`, **ND**,
   ZFIN / `GO_REF:0000015`, Noctua) alongside `GO:0002088` (`lens development`) and `GO:0007601`
   (`visual perception`), both **IBA**, GO_Central / `GO_REF:0000033`, PAINT. ZFIN's own database
   constraints already disallow root terms with descendants, which argues this is a schema rule
   rather than load policy.

   **That schema rule already exists and already fires on every load** (`p_marker_has_goterm` /
   `p_check_drop_go_root_term` in `lib/DB_functions/`, unconditional triggers on
   `marker_go_term_evidence`, not new to this branch) — confirmed live: in the current sidecar DB,
   two of the worked example's three root ND rows (MF, BP) are already gone, superseded by the
   PAINT terms once those were inserted; only CC's ND row remains, because CC has no real
   annotation yet. The gap wasn't "does ND get superseded" — it already did, silently. Three
   pieces now close it, in `GafService`:

   | mechanism | catches |
   |---|---|
   | `dropRedundantRootTermEntries` (new) | the incoming file's own ND row when the SAME file also supplies a real annotation for that gene+aspect — dropped before validation, so it does not become a rejected-row error, repeated on every future run once superseded |
   | `dropRootTermsSupersededInThisRun` (existing) | a brand-new ND row and its brand-new real counterpart landing in the same insert batch — avoids the Postgres batch-abort bug (ZFIN-10358) |
   | `addAnnotation` (extended) | a real annotation superseding an already-*stored* ND row — the trigger already deletes it silently; this makes it visible (`WARN` log + a normal `removed`-entries report line) |

   ⚠️ **`addAnnotation` detects and reports the supersession, it does not also delete the row
   itself — found the hard way, via a real rehearsal, not in review.** An earlier version issued
   an explicit `removeEvidence()` here, reasoning that a Java-level delete was more auditable than
   trusting the trigger. Both `removeEvidence()` and `addEvidence()` are Hibernate-queued, not
   executed immediately -- they only hit the DB at end-of-batch flush -- and Hibernate's default
   flush order runs every queued INSERT before every queued DELETE regardless of Java call order.
   So `addEvidence()`'s INSERT ran first, `p_check_drop_go_root_term` deleted the stale row as its
   own side effect, and the explicit DELETE queued moments earlier then hit a row that no longer
   existed: `OptimisticLockException: actual row count: 0; expected: 1`. That's a **batch-level**
   failure, not a row-level one — it rolled back the entire ~100-row batch, discarding every other
   annotation batched alongside it. One rehearsal run measured 210 of 212 "Failed to add batch"
   errors as exactly this. Removing the explicit delete (the trigger already does it, atomically,
   in the same INSERT) removes the race entirely.

   Together these cover every case where the incoming file adds a new annotation this run. What
   they do **not** cover: a (gene, aspect) pair where an ND row and a real annotation were
   **both already sitting in the database, unchanged, before any of this ran** — nothing in this
   run's file is new, so nothing triggers the check. Measured against the sidecar DB: **zero**
   such pairs exist there today, consistent with the trigger having already been cleaning this up
   continuously for as long as it's existed — but that DB is a smaller/older snapshot, so this is
   worth re-checking against the pre-cutover production baseline before treating "no backlog" as
   settled; if one turns up, it needs a one-time cleanup query, not new load code.

   Detection is by the term being **root**, not by evidence code `ND` — matching the DB triggers'
   own logic. True today that ND is only ever assigned to a root term, but root-vs-non-root is the
   invariant that actually matters, so checking that directly is what stays correct if that
   assumption ever breaks.

   Do not conflate this with the descendant filtering removed under ZFIN-10518. That filter went because ZFIN consumes what
   GO asserts; this one is **added** policy covering a case GO itself calls a defect, and it is
   narrower — same aspect, root vs non-root, not general ancestry. Applied globally (every load
   through `GafLoadJob`/`GafService`), matching how the ZFIN-10518 removal was scoped, since there
   is no clean way to gate it to one parser without a conditional.

   ⚠️ **Doug's follow-up (comment 13, *"I'll check with Pascale on this…"*) is still unanswered.**
   This was built on the most defensible reading of the ambiguous points rather than waiting —
   confirm with Doug/Pascale before or shortly after cutover, since it changes load behavior. Note
   the example row also carries a `noctua-model-id` belonging to the neighbouring gene
   `ZDB-GENE-060918-3` (crygm2d4), which is a separate question for GO, untouched by this.
10. **The NOT qualifier is invisible to every diff** — ✅ **FIXED.** `snapshot_mgte.sql` did not
   select `mrkrgoev_gflag_name`, so it was absent from the before/after snapshots and therefore
   from every workbook, per-org and `ALL` alike. Two consequences: a `NOT` annotation and its
   positive twin collapsed onto the same key, and a `not` → null flip was not visible anywhere.
   The column is small — on the order of a hundred `not` rows and a hundred `contributes to` —
   but these are negated statements, and silently turning one positive is the worst failure a
   loss report can have.

   `snapshot_mgte.sql` now selects `mrkrgoev_gflag_name` as `qualifier_flag`, given the same
   treatment as `protein_acc` — compared but in **neither** `KEY` nor `IGNORE`. That means a
   flip now surfaces as an `UPDATE` on the matched pair rather than as a silent collapse, without
   redefining what counts as a delete/add — so existing delete/add figures stay comparable to
   earlier runs. `mgte_subsumption.sql`'s snapshot table shape was updated to match (the `\copy`
   maps CSV columns positionally). Re-run `mgte_snapshot.sh`/`mgte_csvdiff.sh` to pick it up —
   any snapshot CSV taken before this change lacks the column and cannot be diffed against one
   taken after.

## Why a falling row count is not the same as loss

Two upstream behaviours shrink the row count without ZFIN losing a statement, and both will show
up in the `ALL` workbook's totals:

- **GO consolidates annotations.** Two rows differing only in their with/from contents — say, two
  different InterPro domains — are merged upstream into one row carrying both. Two rows become
  one; the statement is unchanged.
- **A representation change re-keys a row**, so the incoming row counts as added and the stored
  one as removed even though the same gene→GO assertion survives.

Count `(gene, GO)` pairs or statements, not rows, and run `mgte_subsumption.sh` over the result —
it is immune to both, since a merged pair is one pair before and after. The raw `deletes` sheet
is not.

## The diff key, and why `protein_acc` is in neither list

`mgte_csvdiff.sh` holds both lists, once. **KEY** is every identity column a row is matched on;
**IGNORE** is `zdb_id` (recycled every load, so ignoring it puts a re-inserted row into
`updates_ignored` rather than delete+add) plus the five derived readable columns.

`protein_acc` is deliberately in **neither** — compared but not matched on, so a difference
surfaces as an *update* instead of a delete+add pair. Measured on the 2026-08-13 run, moving it
out of KEY turned **26,755** delete+add pairs into updates and cut the deletes column by 60%
(44,967 → 18,212) with both sides still balancing exactly. Putting it in IGNORE instead would
hide the change entirely.

⚠️ Taking it out of KEY makes keys non-unique (3,175 colliding groups over 14,037 rows on the
2026.07.05.1 GOA snapshot). That is only safe because `CSVDiff` became multiplicity-aware on
2026-08-12; the earlier implementation silently dropped all but one member of a key group.

## Reproduce

Operational detail — resetting to a baseline, verifying it, running the load, and producing the
workbooks — is in **`RUNBOOK-danre-mod-load.md`**, next to this file. The essentials:

Prefer the wrapper scripts — they carry the key and ignore lists, so a hand-written `csvDiff`
invocation is how numbers stop being comparable:

```bash
# per-org snapshot + diff (inside a container with $PGHOST/$DBNAME/$SOURCEROOT)
G=$SOURCEROOT/server_apps/DB_maintenance/gafLoad
$G/mgte_snapshot.sh before "$OUT" --others GOA Noctua PAINT "FP Inferences" UniProt
# ... run the load ...
$G/mgte_snapshot.sh after  "$OUT" --others GOA Noctua PAINT "FP Inferences" UniProt
$G/mgte_csvdiff.sh         "$OUT" --others GOA Noctua PAINT "FP Inferences" UniProt

# is <gene> a SUBJECT of <GO> in a source file? (upstream-loss check)
zcat DANRE-mod.gpad.gz | grep -P '^ZFIN:<gene-zdb-id>\t' | awk -F'\t' '$4=="<GO id>"'
```
