# QC — Pascale's replacement DANRE-mod file (`goex`), 2026-08-06

_ZFIN-10345 / ZFIN-10025. Responds to Doug's 2026-08-05 comment: "UniProt made changes that
messed up the GO release… we have a fix (possibly)… does the DANRE file look correct?"_

**Source under test:** `https://ftp.ebi.ac.uk/pub/contrib/goa/goex/current/gpad/DANRE-mod.gpa.gz`
(6.99 MB, `date-generated: 2026-08-04 15:14`, go-version 2026-08-02)

**Baseline:** `https://current.geneontology.org/annotations/gpad/DANRE-mod.gpad.gz`
(3.07 MB, `date-generated: 2026-06-17 14:07`) — the file the branch has been testing against.

**Verdict: the fix works for the problem we reported.** The experimental/Noctua annotations that
vanished are back. But the new file introduces a *separate, new* defect — large-scale row
duplication — that GO should fix before we cut over.

---

## 1. Format — unchanged, parses fine

Both files are GPAD 2.0, 12 tab-separated columns, same column semantics, same `!` header block.
`DanreModGpadParser` read the new file end-to-end with **no new error categories**. The only
difference in the header is the `.gpa.gz` vs `.gpad.gz` extension and `generated-by: UniProt`
(same as the prod file).

## 2. The regression is fixed

Doug's traced example — flt1 (`ZDB-GENE-050407-1`), `RO:0002264 / GO:0001525 / PMID:28087639 /
ECO:0000316` — plus Pascale's bbs1 example:

| gene | classic `noctua_zfin.gpad.gz` | prod DANRE-mod (06-17) | goex DANRE-mod (fix) |
|---|--:|--:|--:|
| flt1 — all rows | present | **0 rows — gene absent entirely** | restored (incl. the exact traced row) |
| bbs1 — experimental annotations | 23 | **0 rows — gene absent entirely** | 25 |

Both genes were *completely missing* from the prod file, not merely thinned.

### Noctua loss, measured against the classic Noctua source

Key = subject + relation + GO + reference + ECO, against `noctua_zfin.gpad.gz` (37,847 rows /
31,681 distinct annotations, generated 2026-05-21):

| DANRE-mod file | in both | **lost** | new-only |
|---|--:|--:|--:|
| prod 06-17 | 17,844 | **13,837** | 50 |
| goex (fix) | 27,426 | **4,255** | 182 |

**9,582 of the 13,837 lost Noctua annotations are recovered** — and the residue is *below* the
~5,404 file-level loss we measured back in June, i.e. the file is now slightly better than its
pre-breakage state.

### What loss remains (4,255) — mostly legitimately out of scope

| bucket | count | note |
|---|--:|---|
| `ECO:0000307` (ND) | 2,656 | "no biological data found" — GOA does not carry ND; expected |
| real evidence | **1,599** | across **861 genes**: IMP 924, IDA 194, IGI 182, ISS 129, IPI 77, NAS/TAS 69, other 24 |

So the genuinely concerning residual is **1,599 experimental annotations**, down from ~13.8k.
That is the number worth taking back to GO.

## 3. NEW DEFECT — 53% of the file is duplicate rows

**459,621 data rows collapse to 214,064 distinct annotations.** The redundant 245,557 rows are
byte-identical in **all 12 GPAD columns except the `id=GOA:` value** in col 12.

Example — flt1 / GO:0001525 / PMID:28087639 / ECO:0000316 appears **10×**: 5 distinct `with/from`
sets, each emitted twice under two different GOA ids.

Multiplicity runs from 1 to **52** copies, and is strongly source-dependent:

| assigned_by | raw rows | distinct | ratio |
|---|--:|--:|--:|
| UniProt | 188,502 | 72,270 | 2.61 |
| InterPro | 112,862 | 40,799 | 2.77 |
| ZFIN | 85,948 | 33,285 | 2.58 |
| GOC | 7,766 | 3,634 | 2.14 |
| **GO_Central** | 62,688 | 62,288 | **1.01 — clean** |
| RHEA / IntAct / most others | — | — | ~1.0 |

GO_Central (PAINT/IBA) is essentially unaffected while the UniProt-derived streams are ~2.6×,
which points at the GOA-side accession→gene collapse emitting one row per source accession
without de-duplicating. This is consistent with "alarming changes at UniProt".

**It is pure noise, not signal** — proven by running the load twice (§4): the deduplicated file
produces *identical* added/updated/removed counts. Only the error count and the "existing" tally
inflate.

## 4. Load results — all three runs, same DB, same code, report-only

Dev stack `zfin-10345`, preloaded `zfindb` (baseline: GOA 123,146 / UniProt 111,089 /
Noctua 35,877 / FP-Inferences 1,622), `ECO:0007322`→IEA migration applied.

| metric | prod 06-17 | goex (raw) | goex (deduped) |
|---|--:|--:|--:|
| processed | 142,612 | 459,621 | 214,064 |
| existing | 71,214 | 154,040 | 101,744 |
| added | 66,282 | 104,665 | 104,665 |
| updated | 1,807 | 2,794 | 2,794 |
| **removed** | **88,014** | **56,714** | **56,714** |
| errors | 3,307 | 198,120 | 4,859 |
| runtime | 20 min | 52 min | 30 min |

The 198,120 errors in the raw run are **187,923 "Duplicate annotation entry"** plus the usual
tail. Deduplicating removes that bucket entirely and leaves 4,859 — all previously-known open
decisions (`GO_REF:0000108` GOC 3,675, `EXP` 105, `ECO:0005547` 24, gene/pub-not-found, root-term).

### Removals by source organization — the headline

| `organizationCreatedBy` | prod 06-17 | goex | change |
|---|--:|--:|--:|
| **ZFIN (Noctua)** | **15,656** | **4,808** | **−10,848 (−69%)** |
| GO_Central | 26,865 | 13,321 | −13,544 (−50%) |
| UniProt | 44,897 | 38,294 | −6,603 (−15%) |
| all others | 596 | 291 | −305 |
| **total** | **88,014** | **56,714** | **−31,300** |

Cutover churn drops by a third, and the Noctua deletions — the ones that represent real ZFIN
curation loss — drop by more than two thirds.

## 5. Feedback for GO / Pascale

1. **The fix is correct on the axis that mattered.** The missing experimental/Noctua annotations
   are back (flt1 and bbs1 both verified); measured Noctua loss falls 13,837 → 4,255, and Noctua
   deletions in our load fall 15,656 → 4,808.
2. **New bug — please de-duplicate.** 53% of rows are redundant, identical except for the
   `id=GOA:` value, at multiplicities up to 52×. GO_Central rows are clean; the UniProt-derived
   streams (UniProt/InterPro/GOC ~2.1–2.8×) are not — suggesting the accession→gene collapse.
3. **Still missing: 1,599 experimental annotations across 861 genes** (IMP 924, IDA 194, IGI 182,
   ISS 129, IPI 77, …) that are in `noctua_zfin.gpad.gz` but absent from the new file. A further
   2,656 ND (`ECO:0000307`) are also absent, which we assume is intentional — worth confirming.
4. **No new format or vocabulary problems.** Same GPAD 2.0 shape, no new ECO codes, no new error
   categories on our side.

## 6. Reproducing

```bash
source .zenv/activate
z run -c 'cd $TARGETROOT && DANRE_MOD_GPAD_URL=https://ftp.ebi.ac.uk/pub/contrib/goa/goex/current/gpad/DANRE-mod.gpa.gz \
  ant -f $SOURCEROOT/server_apps/DB_maintenance/build.xml load-gpad-danre-mod -DjobName=Load-GPAD-DANRE-mod-goex'
```

Report artifacts (`_summary.txt`, `_error_summary.txt`) copied to
`server_apps/DB_maintenance/gafLoad/Load-GPAD-DANRE-mod-{goex,goexdedup,prod0617}/`.
The `_details.txt` (354 MB) and `.html` are left in TARGETROOT only.

Note: the ant target exits 2 ("completed with errors") whenever the run has any errors, so
`BUILD FAILED` here is expected and does not mean the run aborted — all three runs completed and
wrote full reports.

Caveat: this preloaded DB is a different vintage than the one used for Run 5 in the plan doc
(GOA 123,146 here vs 109,656 there), so these numbers are internally comparable across the three
runs above but not directly against Run 5.
