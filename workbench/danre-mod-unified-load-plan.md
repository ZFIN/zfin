# Plan — Unified DANRE-mod GPAD Load (ZFIN-10025)

_Branch: `ZFIN-10025-danre-mod-unified-load`. Drafted 2026-06-17. Companion to `tmp/go-annotation-loads-status.md`._

Goal: replace the three GO-annotation loads (GAF-GOA, Noctua GPAD, FP-Inference) with **one** load that consumes GO Central's unified MOD-ID-keyed file `annotations/gpad/DANRE-mod.gpad.gz`, **while retaining per-source organization information** so removal stays scoped per source (approach **B** — no mass-deletes) and so we can prove every row maps back to the legacy load that used to own it.

Staging source (live now): `https://skyhook.geneontology.io/pipeline-from-goa/main/annotations/gpad/DANRE-mod.gpad.gz`
Prod target (still 403 as of 2026-06-17): `https://current.geneontology.org/annotations/gpad/DANRE-mod.gpad.gz`

---

## 1. Is the new file's organization matchable to the old loads? — YES

**This is the key enabler for approach B, and it works at row granularity.** Two independent facts make it work:

### 1a. The DB already records the source `assigned_by` on every row
`MarkerGoTermEvidence` has **two** org fields, both populated on every load:
- `gafOrganization` (FK → `marker_go_term_evidence_annotation_organization`) = **which load** brought the row in (`GOA` / `Noctua` / `FP Inferences` / `UniProt`).
- `organizationCreatedBy` (`mrkrgoev_annotation_organization_created_by`, NOT NULL) = the source's own **`assigned_by`**, set from `gafEntry.getCreatedBy()` in `GafService.generateAnnotation:379`.

DANRE-mod's `assigned_by` (GPAD col 10) is exactly the same vocabulary as `organizationCreatedBy`. So we can join **new-file row ↔ existing DB row on `assigned_by = organizationCreatedBy`** (plus gene/GO/evidence/ref) — a direct, per-row provenance comparison. No reverse-engineering needed.

### 1b. The assigned_by distributions already line up (probed 2026-06-17)

| assigned_by | EBI GOA GAF (legacy source) | DANRE-mod (new) | Legacy load that owns it today |
|---|--:|--:|---|
| UniProt | 74,217 | 65,878 | **GOA** |
| GO_Central (≈ all `GO_REF:0000033` IBA/phylo) | 50,146 | 48,129 | **GOA** (+ small FP-Inference overlap) |
| InterPro | 42,857 | 38,497 | **GOA** (+ UniProt-Secondary interpro2go overlap) |
| ZFIN (our Noctua curation) | 31,690 | 31,784 | **Noctua** |
| GOC (`GO_REF:0000108`) | 3,191 | 2,786 | **none — GOA rejects GOC today** ⚠ |
| RHEA / IntAct / AgBase / MGI / BHF-UCL / … | ~1,500 | ~1,400 | **GOA** |

The EBI GOA GAF and DANRE-mod are essentially the same GOA aggregation (DANRE-mod is gene-collapsed and slightly smaller). The same `assigned_by` vocabulary appears in both.

### 1c. The legacy ownership rules we must reproduce (or deliberately change)
The legacy loads already partition this shared vocabulary via parser filters — to keep the new load's ownership matching the old, the `assigned_by → organization` map must encode the same rules:
- **`ZFIN` → Noctua.** The GAF/GOA path *rejects* `ZFIN`-created rows (`FpInferenceGafParser:145`, "skip own annotations"), deferring them to the Noctua load. (The GPAD path doesn't filter, which is why Noctua loads them.)
- **`GOC` → currently dropped.** `GoaGafParser:19` rejects `createdBy == "GOC"`. The 2,786 `GOC`/`GO_REF:0000108` rows in DANRE-mod are therefore **net-new content** no current load ingests — a consolidation gain to flag, not a regression.
- **`GO_Central`/`GO_REF:0000033` (phylo IBA) → GOA.** ~48k rows. The standalone FP-Inference file (`zfin-prediction.gaf`) is only **1,809 rows** (assigned_by `GOC` in that file) — a small overlapping subset. Consolidation makes FP-Inference essentially redundant (matches the doc's "consolidation candidate").
- **Everything else (UniProt/InterPro/RHEA/IntAct/…) → GOA.**

**Bottom line:** yes — a row the new load brings in under `assigned_by=ZFIN` is provably the same class the Noctua load owned; `assigned_by=UniProt/InterPro/etc` maps to GOA; and we can verify it per-row against `organizationCreatedBy`. The two honest caveats are (i) the new `GOC` rows that were previously dropped, and (ii) FP-Inference provenance shifting from `GOC` (own file) to `GO_Central` (merged file).

---

## 2. Architecture (approach B — retain per-source organization)

Instead of one umbrella org, the unified load **partitions the file by `assigned_by`** and runs the existing add/update/**remove** diff **once per source organization**. Removal stays scoped by `getEvidencesForGafOrganization(org)` exactly as today, so no source can delete another's annotations.

```
DANRE-mod.gpad.gz ─► DanreModGpadParser (GPAD 2.0, reuses GpadParser)
                         │  each GafEntry.createdBy = assigned_by
                         ▼
        group entries by  assigned_by → GafOrganization   (new mapping)
                         │
        ┌────────────────┼───────────────────────────┐
        ▼                ▼                            ▼
   org = Noctua      org = GOA                  org = GO_Central(phylo) …
   (ZFIN rows)       (UniProt/InterPro/…)       (GO_REF:0000033)
        │                │                            │
   existing diff     existing diff                existing diff
   add/update/remove add/update/remove            add/update/remove
   scoped to Noctua  scoped to GOA                scoped to its org
```

### Mapping table (`assigned_by → GafOrganization`)
Reproduce legacy ownership initially, so the first cutover is a no-op diff against existing rows:
| assigned_by value(s) | → GafOrganization | Rationale |
|---|---|---|
| `ZFIN` | `Noctua` | matches legacy Noctua ownership |
| `UniProt`, `InterPro`, `RHEA`, `IntAct`, `AgBase`, `MGI`, `BHF-UCL`, `HGNC*`, `Ensembl`, `Reactome`, `SynGO`, `ComplexPortal`, `RNAcentral`, `CACAO`, `DisProt`, `FlyBase`, `*-UCL` | `GOA` | matches legacy GOA ownership |
| `GO_Central` (`GO_REF:0000033`) | `GOA` (or a dedicated phylo org) | legacy GOA already owns these |
| `GOC` (`GO_REF:0000108`) | **decision needed** — new org, or fold into GOA | net-new; legacy dropped them |
| anything unmapped | reject + report | fail safe — never silently mass-delete |

> Implementation note: keep the map data-driven (a `Map<String,OrganizationEnum>` or a small DB table), not hard-coded `if`s, so curators can re-map a source without a code change. Unmapped `assigned_by` → row is rejected and counted in the report, **never** assigned to a catch-all org (which would put it at risk of removal under the wrong scope).

---

## 3. Components to build

### 3.1 `DanreModGpadParser extends GpadParser`  (new, non-destructive)
- `@Component` in `org.zfin.datatransfer.go` (auto-registered by `gaf-load.xml` component-scan).
- Reuses GPAD 2.0 parsing + ECO→evidence `postProcessing` unchanged. `getGafEntry` already puts `assigned_by` into `createdBy`.
- Likely needs to handle non-`ZFIN:` col-1 prefixes (23 `ComplexPortal:` rows) — reject-and-report rather than crash.

### 3.2 Organization model
- Add `OrganizationEnum` values for any new orgs (e.g. a phylo/`GO_Central` org and/or a `GOC` org if we don't fold them into GOA).
- Add corresponding rows to `marker_go_term_evidence_annotation_organization` (SQL migration) so `getGafOrganization` resolves them.
- Add the `assigned_by → OrganizationEnum` map.

### 3.3 `GafLoadJob` changes
- **Report-only flag** (`GAF_LOAD_REPORT_ONLY` via existing `envTrue`): compute add/update/remove diffs and write the HTML/txt reports, but skip the three mutation methods and the final `logValidationReport` save. Safe QC against staging with zero DB writes. *(This is the immediate, low-risk deliverable.)*
- **Per-source loop**: when the parser is `DanreModGpadParser`, group `gafJobData` by resolved org and run `processEntries` / `generateRemovedEntries` / add / update / remove per org. (Today the job assumes one org for the whole file.)

### 3.4 Ant target `load-gpad-danre-mod` (build.xml)
- Modeled on `load-noctua-gpad`. Default URL = staging skyhook, overridable via `DANRE_MOD_GPAD_URL`.
- Ships with `GAF_LOAD_REPORT_ONLY=true` initially (report-only by default until we trust the diff).
- Parser arg = `org.zfin.datatransfer.go.DanreModGpadParser`.

---

## 4. QC / comparison harness (runs through the real load path)
Because report-only mode runs the actual parse → gene-map → pub-map → ECO-map pipeline, its report **is** the QC artifact. For the old-vs-new diff (Pascale's "we lose some ZFIN annotations"):
1. Run report-only against staging → get resolved (gene, GO, evidence, ref, **resolved org**) tuples.
2. Compare against current `marker_go_term_evidence`, joining on `assigned_by = organizationCreatedBy` (§1a) per organization.
3. Report three buckets **per source org**: in-both, legacy-only (would be lost), new-only (would be added — e.g. the GOC rows).

Already-known numbers from raw-file diffing (file-level, pre-pipeline):
- **Noctua/ZFIN: 5,404 of 31,681 (17%) Noctua annotations are absent from DANRE-mod** — genuine upstream loss (only 3 recover when ignoring relation), concentrated in `RO:0002264`/`RO:0002432` causal relations. Quantify through the pipeline and raise with GO/Pascale.

---

## 4a. First report-only run — RESULTS (2026-06-24, staging skyhook file)

Ran `load-gpad-danre-mod` (report-only) end-to-end through the real pipeline (gene-map → pub-map → ECO-map → annotation-extension recovery). **Completed in ~16 min (971 s), zero DB writes**, full report set produced. Phase 0 is now validated, not just built.

**Diff buckets (142,612 of 188,529 file rows reached processing; the ~46k gap is dropped earlier in parse/postProcessing — mostly invalid-ECO rows, still TBD to fully account):**
| Bucket | Count | Meaning |
|---|--:|---|
| Existing | 62,704 | already in our DB (match what legacy loads brought in) |
| Added | 25,826 | in DANRE-mod, not in our DB (would be new) |
| Updated | 1,807 | exist but differ |
| Errors | 52,273 | could not be processed |
| Removed | 0 | expected (umbrella org empty + report-only) |

**The 52,273 errors collapse to ~4 decisions, not 600 problems:**

1. **GO_REF not mapped — 34,044 (exact).** Root-caused: `GO_REF:0000002` (28,691, InterPro2GO IEA) + `0000003` (3,173) + `0000108` (2,125, the net-new GOC rows) + `0000115` (55) = **34,044**. Cause: `GpadParser` **bypasses `isValidGafEntry`**, so refs the GAF path *silently excludes* via `goRefExcludePubMap` (`FpInferenceGafParser:50` lists `GO_REF:0000002`) flow through and fail at pub-mapping instead. This is an **ownership decision**: `GO_REF:0000002` is the InterPro2GO IEA stream UniProt-Secondary loads in-house today — under consolidation, either map it to a pub (take from DANRE-mod) or keep excluding it (keep from secondary).
2. **Unmapped ECO — 17,406.** `ECO:0007322` (SubCell IEA, 17,350) + `ECO:0005547` (56) — the `eco_go_mapping` gap (file counts now 17,350 / 56, vs the 22,274 / 62 estimated earlier). Fix = add `eco_go_mapping` rows (curator/GO call).
3. **EXP — 105.** Also a filter-bypass artifact (`EXP` is in the GAF path's `EXCLUDED_EVIDENCE_CODES`); ties to ZFIN-10258.
4. **Long tail — ~700.** Root-term QC rejections (`Cannot add root-term … already has non-root terms`), 17 Do-Not-Annotate, ~45 gene/pub-not-found, 77 duplicates.

**Mapped GO_REFs that loaded cleanly** (cross-checked vs `GoDefaultPublication`): `0000033` PAINT (36,227), `0000044` SubCell2 (16,211), `0000104` UniRule (11,116), `0000117` ARBA (10,480), `0000024` UniProt (10,268), `0000015` (2,124), `0000116` RHEA (701), `0000041` (438), `0000107` (11).

**Phase 1 consequence (important):** because the parser inherits `GpadParser`'s filter-bypass, the unified load **surfaces annotations the legacy GAF load silently dropped** (GO_REF:0000002, EXP). Phase 1 must therefore port **explicit exclusion/ownership rules** from the GAF path (`goRefExcludePubMap`, `EXCLUDED_EVIDENCE_CODES`) and apply them per source — it is not only about per-source removal scoping.

Report artifacts from this run are committed under `server_apps/DB_maintenance/gafLoad/Load-GPAD-DANRE-mod/` (SOURCEROOT; the live run writes to TARGETROOT which is not committed): `*_summary.txt` and `*_error_summary.txt`. The 75 MB `_details.txt` and 2.3 MB `.html` are not committed (size).

**Run 2 (2026-06-25) — after mapping GO_REF:0000002 + 0000003 (§7 decision):** errors **52,273 → 20,603** (−31,670); added **25,826 → 57,496**; `GO_REF not known` **34,044 → 2,180**. The 31,864 cleared split into 31,670 added + 194 newly-detected duplicates (`Duplicate annotation entry` 77 → 271) — accounts exactly. Remaining errors collapse to one big item — **`ECO:0007322` SubCell IEA (17,350)**, the `eco_go_mapping` gap — plus small open decisions: `GO_REF:0000108` GOC (2,125), `EXP` (105), `ECO:0005547` (21), `GO_REF:0000115` RNAcentral (55).

---

## 5. Pre-adoption blockers (from status doc §2, still gating)
1. **ECO gap:** 4 of 22 ECO codes unmapped — **confirmed live as 17,406 erroring rows** (`ECO:0007322` SubCell 17,350 + `ECO:0005547` 56); needs `eco_go_mapping` rows (curator/GO call).
1b. **GO_REF mapping/exclusion gap (NEW, from §4a):** 34,044 rows error on unmapped GO_REFs, dominated by `GO_REF:0000002` (28,691). Decide map-vs-exclude per source (ties to the secondary-load InterPro2GO ownership).
2. **Relation→qualifier mapping:** confirm every col-3 RO/BFO relation resolves to a ZFIN `qualifier_relation`.
3. **ZFIN round-trip QC** (§4 above).
4. **Removal-scope safety:** the per-source partition + reject-unmapped rule (§2) is the mitigation for the mass-delete risk.

---

## 6. Phased rollout
1. **Phase 0 (now, safe):** parser class + enum + Ant target + report-only flag. Run report-only vs staging. *No DB writes, no removal logic yet.*
2. **Phase 1:** add the `assigned_by → org` map + per-source diff loop; **port the GAF-path exclusion/ownership rules** (`goRefExcludePubMap`, `EXCLUDED_EVIDENCE_CODES`) into the DANRE-mod path so it stops surfacing rows legacy silently dropped (see §4a); keep report-only. Validate per-org add/remove counts vs legacy DB.
3. **Phase 2:** resolve ECO/relation/curator decisions; build the old-vs-new QC diff; get sign-off on annotation loss + the new GOC rows.
4. **Phase 3 (cutover, prod URL live):** flip report-only off; retire Noctua + FP-Inference Jenkins jobs; drop only the GO-mapping handlers from UniProt-Secondary (keep its dblink/domain/PDB refresh — status doc §1 Correction).

---

## 7. Open decisions
- **✅ DECIDED (2026-06-25) — *2go GO_REFs come from DANRE-mod.** The UniProt-Secondary load will **drop all the *2go GO-mapping handlers** (interpro2go / ec2go / kw2go), so their annotations must now come from the DANRE-mod file. **Map** these GO_REFs to ZFIN pubs (do NOT exclude):
    - `GO_REF:0000002` → InterPro2GO (InterPro / ECO:0000256, **28,691 rows**) — reuse existing InterPro2GO pub `ZDB-PUB-020724-1`.
    - `GO_REF:0000003` → EC2GO (UniProt / ECO:0000501, **3,173 rows**) — reuse existing EC2GO pub `ZDB-PUB-031118-3`.
    - Clears **31,864 of the 34,044** GO_REF errors. kw2go (`GO_REF:0000004`) is absent from the file (retired, ZFIN-10135) — nothing to take over.
    - Implementation: add `GoDefaultPublication` entries whose `title()` is the GO_REF string (so they land in `goRefPubMap`) → the pub ZDB IDs above; ensure `getGoRefPubs()` returns them. Reconfirms §1 Correction: drop ONLY the secondary load's GO-mapping handlers, keep its dblink/domain/PDB refresh.
- **`GO_REF:0000115` RNAcentral InterPro-family (55 rows):** map alongside `0000002` or leave? Tiny; likely map for consistency.
- **GOC / `GO_REF:0000108` (~2,125 rows):** adopt (new org or fold into GOA) or keep rejecting? Currently net-new.
- **Phylo IBA org:** keep under GOA, or a dedicated org to preserve the FP-Inference identity in reporting?
- **GPAD vs GAF:** plan assumes GPAD (RO relations + model-ids + existing parser). Confirm.
- **Removal scope on first cutover:** initial map must reproduce legacy ownership so the first real diff is ~no-op, not a churn of mass add+remove.
