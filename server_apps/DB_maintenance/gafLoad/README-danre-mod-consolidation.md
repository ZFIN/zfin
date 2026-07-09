# Unified DANRE-mod GO load — consolidation findings & open decisions (ZFIN-10025)

Durable reference for the effort to replace the three GO-annotation loads
(**GAF-GOA**, **Noctua GPAD**, **FP-Inference**) — plus the UniProt-Secondary load's
`*2go` GO-mapping stages — with **one** load consuming GO Central's per-species GPAD
file(s). It captures the findings and unresolved decisions that must survive past the
feature branch; the moment-to-moment development log lived in `workbench/` (scratch, not
merged).

Related code: `DanreModGpadParser`, `DanreModSourceOrganization`, `GafLoadJob`
(`source/org/zfin/datatransfer/go/`). QC tooling: `snapshot_mgte.sql`, `diff_mgte.sh`,
`gradle csvDiff` (this directory). Jenkins: `Load-GPAD-DANRE-mod_m`.

Status: **report-only / pre-cutover.** The load runs the full pipeline but the Ant target
ships `GAF_LOAD_REPORT_ONLY=true` (no DB writes) until the decisions below are resolved.

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

---

## Comparison method (QC harness)

`snapshot_mgte.sql` flattens one org's annotations (with child-table dimensions:
inferred_from, annotation_extensions, noctua_model) into a CSV, resolving ids to readable
`gene`/`go_id`/`go_term`/`go_aspect`/`relation_name` columns. `gradle csvDiff` then diffs
two snapshots, keyed on the identity columns and **ignoring** `zdb_id` + the readable
columns. Run per organization. The "legacy vs unified" comparison loads the same DB
snapshot down two paths (legacy 3 loads + dedup vs. the unified load) and diffs the
per-org end states.

Latest comparison — 2026.07.05.1 DB snapshot vs the 2026-06-17 `DANRE-mod.gpad.gz`,
report-only OFF, dedup on both sides (dedup removed 0 rows from the unified load):

| Org | legacy | unified | deleted (legacy-only) | added (unified-only) |
|---|--:|--:|--:|--:|
| GOA | 107,843 | 116,297 | 42,131 | 50,585 |
| Noctua | 36,025 | 20,425 | **15,858** | 258 |
| FP Inferences | 1,623 | 1,623 | 0 | 0 |
| UniProt (2° load) | 111,089 | 111,089 | 0 | 0 |

Because the 7/5 snapshot already reflects current production loads (re-running them was
a near no-op), this diff **is** the real cutover impact, not a test artifact.

---

## Findings

### 1. Noctua: ~15,858 curated annotations genuinely lost (the #1 blocker)
The unified load drops 44% of Noctua (ZFIN-curated) annotations. **97.6% (11,751 of
12,045 distinct gene→GO pairs) are absent as a subject from the DANRE-mod file entirely** —
GO Central's export drops them; the loader has nothing to ingest. This holds across every
relation, and is heaviest for experimental evidence (IMP 7,648, ND 3,605, IGI 2,098,
IDA 1,562) and the causal relation `acts_upstream_of_or_within` (RO:0002264, ~10k;
file-wide 26,746 → 16,621).

Worked example — `flt1` (ZDB-GENE-050407-1), a VEGFR1/angiogenesis gene:

| `flt1` as subject | `noctua_zfin.gpad.gz` | `DANRE-mod.gpad.gz` |
|---|--:|--:|
| of GO:0001525 (angiogenesis) | 11 | 0 |
| of any GO term | 22 (all RO:0002264) | 0 |

Even the ~294 gene→GO pairs that *do* survive are usually a *different* annotation (an
InterPro IEA prediction), not the experimental curation — e.g. `robo1` → axon guidance is
present only as `involved_in`/`ECO:0000256`/`GO_REF:0000002`, while the experimental
IMP/IGI `acts_upstream_of_or_within` rows are gone.

**This is a GO-pipeline / curation-policy issue, not a loader bug.** Decision (GO / Pascale):
(a) get GO to retain subject-level Noctua annotations in the export, (b) keep the Noctua
GPAD load as a supplementary source until then, or (c) accept the loss with a rationale.
It must not be dropped silently at cutover.

### 2. `*2go` handover: org-mismatch duplication, under-coverage, and a fully-dropped stream
The UniProt-Secondary load produces **three** `*2go` IEA streams, all stored under
`gafOrganization=UniProt` (`organizationCreatedBy=ZFIN`). The unified load resolves the
equivalent content to `GOA` (`assigned_by=InterPro`/`UniProt` → GOA). A per-org `csvDiff`
never compares across orgs, so the old copies read "unchanged in UniProt" (0/0) while the
new copies land as "GOA adds":

| `*2go` stream | pub | legacy `UniProt` | GO_REF | in `DANRE-mod` (→GOA) | in `DANRE-uniprot` |
|---|---|--:|---|--:|--:|
| InterPro2GO | ZDB-PUB-020724-1 | 65,327 | GO_REF:0000002 | 28,509 | 43,882 |
| **UniProtKB-Keyword (kw2go/spkw2go)** | ZDB-PUB-020723-1 | **41,027** | GO_REF:0000004 | **0** | **0** |
| EC2GO | ZDB-PUB-031118-3 | 4,735 | GO_REF:0000003 | 3,161 | 5,400 |

Three problems at cutover:
- **Duplication.** The unified load's removal scope is GOA+Noctua only, so it never
  touches the 111,089 UniProt-org rows — dropping the secondary `*2go` handlers leaves
  those *and* the new GOA copies, storing each mapping twice. Purge by
  `gafOrganization='UniProt'`, **not** `created_by=ZFIN` (which also tags Noctua).
- **Under-coverage.** `DANRE-mod` supplies only **28,509** InterPro2GO vs the secondary
  load's **65,327** (< half); `DANRE-uniprot` has **43,882** (closer, still short).
- **kw2go is dropped entirely.** GO Central retired keyword→GO mapping (`GO_REF:0000004`);
  it is absent from *both* files. So the 41,027 UniProtKB-Keyword annotations have **no
  file successor** — the branch's `GoDefaultPublication` maps GO_REF:0000002/0000003 but
  nothing for keyword2go, because there is nothing to map.

**How much of the would-be-lost `*2go` is a true loss vs. subsumed by a more-specific
retained term?** Quantified across all three streams (built the GO `is_a`+`part_of`
ancestor closure, 357,043 pairs; a lost `(gene, GO_lost)` is "subsumed" if the gene retains
a descendant of `GO_lost` in `DANRE-mod`):

| | distinct (gene, GO) |
|---|--:|
| all `*2go` pairs | 74,763 |
| would-be-lost (not reproduced by DANRE-mod) | 40,477 |
| ↳ subsumed by a retained more-specific term (**not** true loss) | 9,726 (**24%**) |
| ↳ **not** subsumed (true loss vs DANRE-mod) | 30,751 (**76%**) |

Subsumption rate by stream: kw2go **32%** (9,298/28,972 — broad keyword terms, the most
rescuable), ec2go **14%**, interpro2go **2%** (InterPro maps to specific terms that mostly
aren't retained). So the true-path rule rescues only ~a quarter — the bulk is genuine loss
relative to `DANRE-mod`.

**Two caveats pull in opposite directions.** (i) This is measured against the sparse,
gene-keyed `DANRE-mod`. The specific terms that *would* subsume many lost broad terms exist
in `DANRE-uniprot` (protein-keyed) — e.g. igfbp2a's lost `GO:0019838 growth factor binding`
is subsumed by `GO:0005520 insulin-like growth factor binding`, urod's lost `lyase`/
`carboxy-lyase` by `uroporphyrinogen decarboxylase activity`, dlc's `cell differentiation`
by `epithelial cell differentiation` — but those aren't in `DANRE-mod`, so they count as
losses here. A `DANRE-uniprot`-based load would rescue far more, so **76% is the pessimistic
`DANRE-mod` bound**. (ii) Some accessions have a gene↔protein `db_link` gap (e.g. ppardb/
A9C4A5) — genuinely uncovered in either file.

### 3. `ECO:0007322 → IEA` mapping (done)
`DANRE-mod` tags ~17,350 UniProtKB-SubCell annotations with `ECO:0007322` ("curator
inference used in automatic assertion"), a granular code not in GO's flat
`gaf-eco-mapping.txt`, so it was never in `eco_go_mapping`. `GpadParser.postProcessing`
rejects unmapped ECO codes. Migration
`source/org/zfin/db/postGmakePostloaddb/1184/migrations/0010-ZFIN-10025-eco-0007322-subcell-iea-mapping.sql`
maps it to **IEA** (the automatic-assertion sibling of `ECO:0000501`/`ECO:0000256`, and how
these SubCell rows are already stored under `ZDB-PUB-120306-4`/`GO_REF:0000044`). This lets
them load and match instead of erroring + being flagged for removal.

### 4. `GafLoadJob` parser resolved by bean name (done)
`DanreModGpadParser extends GpadParser`, which made `GafLoadJob`'s by-type
`getBean(GpadParser.class)` ambiguous and crashed the legacy `Load-GPAD-Noctua_w` job on
startup — during the coexistence window it must keep running. Fixed by resolving the
parser by conventional bean name (commit on branch `ZFIN-10025-danre-mod-unified-load`).

### 5. GOA churn is turnover, not loss
GOA's 42,131 deletes / 50,585 adds are almost all IEA representation change + monthly
UniProt turnover (the 7/5 DB is ~3 weeks newer than the 6/17 file) plus net-new content
(GOC `GO_REF:0000108`, the `*2go` IEAs). Not curation loss.

---

## Open decisions before cutover

1. **Noctua loss** (finding 1) — GO/curator conversation; the largest, highest-impact item.
2. **Source file** — `DANRE-mod` vs `DANRE-uniprot` vs both. Both live at
   `https://current.geneontology.org/annotations/gpad/` (⚠️ `http://` 301-redirects —
   fetch via `https`/`curl -L`, or you get a 167-byte redirect page that looks empty).
   `DANRE-uniprot` carries far more of the UniProt-derived IEA the secondary load replaces
   (InterPro2GO 43,882 vs 28,691; UniProt 71,243 vs 52,732; RNAcentral 8,969 vs 55; GOC
   6,477 vs 2,125) — **but it is keyed by UniProtKB accession, not ZFIN gene id**, so
   consuming it needs a UniProt→gene `db_link` mapping layer (like the legacy GAF-GOA path,
   with its own protein↔gene gaps). `DANRE-mod` (gene-keyed) is a drop-in but under-covers
   mapping2go. Neither file recovers the Noctua loss or carries keyword2go.
3. **`*2go` ownership org** — route the `*2go` GO_REFs to the same org the secondary load
   uses (`UniProt`) or add an explicit `UniProt`-org purge/migration at cutover, so old and
   new copies don't coexist.
4. **kw2go (UniProtKB-Keyword, 41,027 rows)** — no file successor (GO retired
   `GO_REF:0000004`). Keep the secondary load's `spkw2go` handler, or accept the loss?
   Quantified: only ~32% of would-be-lost kw2go pairs are subsumed by a more-specific
   retained term in `DANRE-mod` — the rest are genuine losses there (though a
   `DANRE-uniprot`-based load would rescue more). Decision is entangled with the source-file
   choice (#2).
5. **`GO_REF:0000108` (GOC, ~2,125)** — adopt (net-new content) or keep rejecting?
6. **`GO_REF:0000115` (RNAcentral, 55)** and **`ECO:0005547` (manual, ~21)** — map or leave.
7. **`EXP` evidence (~105)** — ZFIN-10258 says allow; currently surfaced as errors.
8. **Relation → `qualifier_relation`** — confirm every col-3 RO/BFO relation resolves.

## Reproduce

```bash
# per-org snapshot + diff (inside a container with $PGHOST/$DBNAME/$SOURCEROOT)
psql -h $PGHOST -d $DBNAME -v org=Noctua -v stage=t -f snapshot_mgte.sql
psql -h $PGHOST -d $DBNAME -c "\copy (select * from t order by zdb_id) to stdout csv header" > noctua.csv
gradle csvDiff --args="out before.csv after.csv \
  marker,term,source,evidence,relation,created_by,contributed_by,protein_acc,inferred_from,annotation_extensions,noctua_model \
  zdb_id,gene,go_id,go_term,go_aspect,relation_name"

# is <gene> a SUBJECT of <GO> in a source file? (upstream-loss check)
zcat DANRE-mod.gpad.gz | grep -P '^ZFIN:<gene-zdb-id>\t' | awk -F'\t' '$4=="<GO id>"'
```
