# Legacy-vs-new DB comparison with Pascale's file — same method as the 2026-07-07 report

_ZFIN-10345 / ZFIN-10025. Companion to `goex-danre-mod-qc-2026-08-06.md` (file-level + report-only QC)._

Reproduces the **2026-07-07** `report-goa-noctua-unified-load` methodology exactly, swapping only the
input file: **`ftp.ebi.ac.uk/pub/contrib/goa/goex/current/gpad/DANRE-mod.gpa.gz`** (2026-08-04)
in place of `current.geneontology.org/.../DANRE-mod.gpad.gz` (2026-06-17).

## Method (identical to July)

- Baseline: `gradle loadDatabase` of **2026.07.05.1** — the same snapshot July used. Verified
  identical: **GOA 109,656 / Noctua 36,025 / FP Inferences 1,623 / UniProt 111,089**.
- `liquibasePreBuild` + `liquibasePostBuild` → applies the `ECO:0007322`→IEA mapping (as July did).
- `snapshot_mgte.sql` BEFORE, per org.
- `load-gpad-danre-mod` with **`GAF_LOAD_REPORT_ONLY=false` (real writes)** against the new file.
  (build.xml changed to make report-only env-overridable; still defaults to `true`.)
- `gradle cleanMarkerGoTermEvidenceDuplicatesTask`.
- `snapshot_mgte.sql` AFTER, per org → `gradle csvDiff` with July's key
  (`marker,term,source,evidence,relation,created_by,contributed_by,protein_acc,inferred_from,annotation_extensions,noctua_model`;
  ignoring `zdb_id,gene,go_id,go_term,go_aspect,relation_name`).

Artifacts: `mgoe_dbdiff_{GOA,Noctua,FP_Inferences,UniProt}.xlsx` (sheets deletes/adds/updated),
copied to `~/Downloads/report-goex-danre-mod/`.

**One methodology difference from July:** July compared *legacy path vs new path* (it re-ran the
three legacy loads on the same baseline). This run compares *baseline vs new path*. July showed the
legacy re-run was a near no-op — Noctua and FP exactly unchanged, GOA 109,656 → 107,843 — so the two
framings coincide for Noctua/FP/UniProt and differ by ~1,813 rows on GOA.

## Rows per organization

| Organization | Baseline (07-05) | July new path (06-17 file) | **New path (Pascale's file)** |
|---|--:|--:|--:|
| GOA | 109,656 | 116,297 | **174,773** |
| Noctua | 36,025 | 20,425 | **31,446** |
| FP Inferences | 1,623 | 1,623 | **1,623** |
| UniProt (2° load) | 111,089 | 111,089 | **111,089** |

## Diff

| Organization | July deletes | July adds | **New deletes** | **New adds** |
|---|--:|--:|--:|--:|
| GOA | 42,131 | 50,585 | **44,967** | **110,084** |
| Noctua | **15,858** | 258 | **5,103** | **524** |
| FP Inferences | 0 | 0 | 0 | 0 |
| UniProt | 0 | 0 | 0 | 0 |

## 1. Noctua — the headline regression is largely fixed

**15,858 → 5,103 deletes (−68%).** Noctua ends at 31,446 rows instead of 20,425 — ~11,000 curated
annotations retained that the 06-17 file dropped.

| evidence | July | new | | relation | July | new |
|---|--:|--:|---|---|--:|--:|
| IMP | 7,648 | **1,363** | | `acts_upstream_of_or_within` | 10,169 | **1,742** |
| ND | 3,605 | 2,819 | | `enables` | 2,625 | 1,293 |
| IGI | 2,098 | **272** | | `is_active_in` | 1,217 | 1,004 |
| IDA | 1,562 | **209** | | `involved_in` | 1,253 | 944 |
| IPI | 441 | 195 | | `located_in` | 401 | **41** |
| ISS/NAS/TAS/other | 504 | 245 | | other | 193 | 79 |

`RO:0002264` — 64% of July's loss — falls **83%**. Of the 5,103 remaining, **2,819 are ND**
(GOA doesn't carry ND), leaving **~2,284 genuinely experimental annotations** still lost,
down from ~12,250.

## 2. GOA — the +65,117 is mostly a known ZFIN-side issue, not new GO content

| created_by | before | after | delta |
|---|--:|--:|--:|
| **InterPro** | 0 | 40,731 | **+40,731** |
| **GO_Central** | 39,962 | 62,234 | **+22,272** |
| UniProt | 68,601 | 70,179 | +1,578 |
| RHEA / IntAct / AgBase / others | 1,093 | 1,629 | +536 |
| **total** | **109,656** | **174,773** | **+65,117** |

- **InterPro +40,731 is the `*2go` org-mismatch duplication (plan §4c), not new annotation
  content.** The secondary UniProt load still owns InterPro2GO/EC2GO under the `UniProt` org
  (111,089 rows, 0 diff — untouched), while the unified load writes the same content under `GOA`.
  July saw this as +28,509; it is larger now only because Pascale's file carries more InterPro.
  **This is our cutover gap to fix, not a GO problem.**
- **GO_Central +22,272** is real PAINT/IBA growth: the file carries 62,288 distinct GO_Central
  rows vs 36,250 in the 06-17 file. Note the 06-17 file would have *reduced* IBA below baseline
  (39,962 → ~36,250); Pascale's file takes it well above. Worth confirming with GO that this
  scale of IBA growth is intended.

Excluding the InterPro double-storage, GOA grows +24,386 rather than +65,117.

**The breakage was not Noctua-only.** Pascale's file carries substantially more content in *every*
UniProt-derived stream than the 06-17 file (distinct annotations: UniProt 72,270 vs 52,562,
InterPro 40,799 vs 28,509, GO_Central 62,288 vs 36,250, GOC 3,634 vs 2,117). The 06-17 release was
under-populated across the board.

## 3. The file's 53% row duplication does NOT reach the database

The input has 459,621 rows / 214,064 distinct (see the companion doc). Despite that:

- `cleanMarkerGoTermEvidenceDuplicatesTask` removed **0 rows** — July's "the unified load is
  already duplicate-clean" still holds.
- Post-load duplicate check on the GOA org: 1,595 groups / **1,727 redundant rows** (0.99%),
  in line with normal load behaviour, not 245k.
- Load summary: processed 459,621, added 111,213, updated 32, removed 50,184, existing 152,743,
  errors 195,633 — of which **187,923 are "Duplicate annotation entry"**.

So the duplication costs runtime (57 min) and makes the error log unreadable, but does not corrupt
the result.

## 4. Unchanged cutover gaps (both ours, not GO's)

- **FP Inferences** — 0 diff; 1,623 rows still orphaned, not migrated.
- **UniProt org** — 0 diff; 111,089 rows still outside the removal scope → the InterPro2GO/EC2GO
  duplication above. Scope any purge by **`gafOrganization='UniProt'`**, not `created_by='ZFIN'`.

## 5. Bottom line

Against the identical baseline and method, swapping in Pascale's file:

- **Noctua loss 15,858 → 5,103 (−68%)**; experimental-only residue ~12,250 → ~2,284.
- **GOA churn is not worse** (deletes 42,131 → 44,967); the large add count is dominated by our own
  `*2go` org-mismatch, plus genuine IBA growth.
- **No new failure modes.** Same error categories, no new ECO codes, dedup still a no-op.
- **One new upstream defect to report:** the 53% row duplication.
