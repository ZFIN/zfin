# ZFIN-10025 Unified DANRE-mod GO Load — Progress Summary

_Last updated 2026-06-25. Branch: `ZFIN-10025-danre-mod-unified-load`. Companion to `danre-mod-unified-load-plan.md` (full plan) and `../tmp/go-annotation-loads-status.md` (background)._

Goal: replace the three GO-annotation loads (GAF-GOA, Noctua GPAD, FP-Inference) with **one** load consuming GO Central's single MOD-ID-keyed file `annotations/gpad/DANRE-mod.gpad.gz`, retaining per-source provenance so removal stays scoped per source.

---

## 1. What we've done

**Background / analysis**
- Refreshed the live status of the GOC pipeline refactor: re-probed URLs 2026-06-17 and 2026-06-24 — prod `DANRE-mod` files still **403 (unpublished)**, legacy `/products/` files still 200. Cutoff now reads as **likely slipping** (recorded in the status doc §2/§4).
- Established that the new file is **matchable to legacy loads at row granularity**: every `marker_go_term_evidence` row already stores `organizationCreatedBy` (= the source `assigned_by`), so DANRE-mod's `assigned_by` joins directly to existing DB rows. Confirmed the `assigned_by` distributions of the EBI GOA GAF and DANRE-mod are near-identical.
- File-level diff vs the legacy Noctua source: **5,404 of 31,681 (17%) Noctua annotations are absent from DANRE-mod** — genuine upstream loss (Pascale's "we lose some ZFIN annotations"), concentrated in `RO:0002264`/`RO:0002432` relations.

**Phase 0 scaffold — built, compiles, validated (commit `3fae10140d`)**
- `DanreModGpadParser extends GpadParser` (reuses GPAD 2.0 parsing + ECO mapping).
- `GafOrganization.DANRE_MOD` umbrella enum value (Phase 0 only).
- `GafLoadJob` **report-only mode** (`GAF_LOAD_REPORT_ONLY`): computes the add/update/remove diff and writes reports, but performs **no DB writes**.
- `load-gpad-danre-mod` Ant target — staging skyhook URL (overridable via `DANRE_MOD_GPAD_URL`), report-only by default.

**Report-only run 1 vs staging (commit `f2607fe77e`)** — 142,612 entries processed: 62,704 existing, 25,826 added, 1,807 updated, **52,273 errors**. Root-caused the errors to ~4 decisions, not 600 problems.

**Workbench (commit `91fd29171e`)** — moved the plan into `workbench/`, added `TODO.txt` reminding us to delete `workbench/` before merging.

**interpro2go/ec2go GO_REF mapping — implemented + verified (commit `45338bd282`)**
- Added `GoDefaultPublication` entries: `GO_REF:0000002`→`ZDB-PUB-020724-1` (InterPro2GO), `GO_REF:0000003`→`ZDB-PUB-031118-3` (EC2GO); registered in `getGoRefPubs()`.
- **Report-only run 2 confirmed:** errors **52,273 → 20,603**; added 25,826 → 57,496; `GO_REF not known` 34,044 → 2,180. The 31,864 cleared = 31,670 added + 194 duplicates. Exact.

**ECO investigation (no code yet)** — root-caused the remaining 17k-error bucket: `ECO:0007322` and `ECO:0005547` are **granular GPAD ECO codes not present in GO's flat `gaf-eco-mapping.txt`**; GO expects consumers to resolve them up the `is_a` hierarchy. Our `GpadParser` does a direct lookup only.
- `ECO:0007322` = "curator inference used in automatic assertion" (17,350) → should map to **IEA** (same automatic-assertion family as `ECO:0000501`=IEA).
- `ECO:0005547` = "biological system reconstruction … used in manual assertion" (~56) → manual type, likely `IC`, ambiguous, tiny.

---

## 2. Decisions MADE

| # | Decision | Notes |
|---|---|---|
| D1 | **Consolidate to one load** (vs repoint-and-keep) | Adopt DANRE-mod; retire Noctua + FP-Inference; drop only the secondary load's GO-mapping handlers. |
| D2 | **GPAD variant**, not GAF | We have a GPAD 2.0 parser; carries RO relations + model-ids. |
| D3 | **Approach B — retain per-source org** | Partition rows by `assigned_by` → `GafOrganization` so removal stays scoped per source (no mass-deletes). |
| D4 | **Report-only-first development** | Build the real load, run it report-only against staging; the report *is* the QC harness. Zero prod risk. |
| D5 | **Feature branch + `workbench/`** | Scratch notes isolated; `workbench/` deleted before merge. |
| D6 | **interpro2go + ec2go come from DANRE-mod** | Map `GO_REF:0000002`/`0000003`; drop the corresponding *2go GO-mapping handlers from UniProt-Secondary (keep its dblink/domain/PDB refresh). Implemented + verified. |

---

## 3. Decisions REMAINING

**Curator / GO calls (Phase 2 gating):**
- **ECO mapping** — confirm `ECO:0007322` → `IEA` (17,350 rows; high confidence but affects real evidence assignment) and decide `ECO:0005547` (~56). **Plus an approach choice:** targeted `eco_go_mapping` rows now vs. parser **hierarchy resolution** (walk `is_a` to nearest mapped ancestor; future-proof). _This is the open question we paused on._
- **`GO_REF:0000108` (GOC, ~2,125 rows)** — net-new content the legacy GOA load rejects. Adopt (map to a pub) or keep rejecting?
- **`GO_REF:0000115` (RNAcentral, 55)** — map or leave?
- **`EXP` evidence (105)** — ZFIN-10258 says allow; needs implementing (currently excluded on the GAF path / surfaced as errors here).
- **The 17% Noctua loss** — raise with GO/Pascale regardless of our code.

**Engineering / design choices:**
- **Phylo IBA org** — keep phylo annotations under GOA, or a dedicated org to preserve FP-Inference identity in reporting?
- **First-cutover removal scope** — the initial `assigned_by → org` map must reproduce legacy ownership so the first real (non-report-only) diff is ~no-op, not a mass add+remove churn.

---

## 4. Work REMAINING (by phase)

- **Phase 1 (engineering, can proceed now, stays report-only):**
  1. `assigned_by → GafOrganization` map + **per-source diff/removal loop** (core of approach B; turns the blended report into per-source buckets).
  2. **Port the GAF-path exclusion/ownership rules** (`goRefExcludePubMap`, `EXCLUDED_EVIDENCE_CODES`) into the DANRE-mod path so it stops surfacing rows legacy silently dropped (EXP, etc.).
- **Phase 2:** resolve the ECO/GO_REF/EXP decisions above; verify relation→`qualifier_relation` mapping; build the old-vs-new QC diff (per source); get sign-off on annotation loss + net-new rows.
- **Phase 3 (cutover — prod URL now LIVE as of 2026-06-30; remaining blocker is Phase 2 sign-off):** flip report-only off; retire Noctua + FP-Inference Jenkins jobs; drop the secondary load's *2go GO-mapping handlers. (Old `zfin.gaf.gz` + `/products/` files still served too — coexistence window.)

---

## 5. Current error picture (after run 3, vs prod file — counts ≈ run 2)

| Count | Category | Disposition |
|--:|---|---|
| 17,350 | `ECO:0007322` (SubCell, automatic-assertion) | → IEA (pending confirm) — dominant remaining |
| 2,125 | `GO_REF:0000108` (GOC) | decision: adopt/reject |
| 271 | Duplicate annotation | benign (already present from another source) |
| 105 | `EXP` | ZFIN-10258 (allow) |
| 21 | `ECO:0005547` (manual) | curator call (tiny) |
| 55 | `GO_REF:0000115` (RNAcentral) | map or leave |
| ~80 | gene/pub-not-found, Do-Not-Annotate, root-term tail | expected QC behavior |

---

## 6. Commits on the branch

```
45338bd282  load interpro2go/ec2go IEAs from DANRE-mod via GO_REF mapping
91fd29171e  add workbench plan + QC notes (remove before merge)
f2607fe77e  record first report-only QC run against staging DANRE-mod
3fae10140d  scaffold unified DANRE-mod GPAD load (Phase 0, report-only)
```

## 7. Environment notes (for re-running)
- `git checkout ZFIN-10025-danre-mod-unified-load` to resume.
- Deploy Java: `gradle dirtydeploy -x npmBuild` (the frontend `npmBuild` fails in the compile container — a `node_modules` gap, unrelated to our work). Use `bash -lc` (login shell — sets `SOURCEROOT` and has npm/gradle on PATH).
- Run report-only: `ant -f $SOURCEROOT/server_apps/DB_maintenance/build.xml load-gpad-danre-mod -DjobName=Load-GPAD-DANRE-mod` from `$TARGETROOT` (~16 min; logs heavily).
- Reports land in `$TARGETROOT/server_apps/DB_maintenance/gafLoad/Load-GPAD-DANRE-mod/`; small artifacts are copied to the SOURCEROOT path and committed (TARGETROOT is not committed).
