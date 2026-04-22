# Load-GAF-GOA Job

## Overview

The Load-GAF-GOA job (`Load-GAF-GOA_m`) runs monthly to sync ZFIN's Gene Ontology annotations with the latest release from the GOA (Gene Ontology Annotation) project at EBI. It downloads zebrafish GO annotation files in GAF (Gene Association Format), maps entries to ZFIN genes, validates them, and loads new annotations while removing stale ones.

**Jenkins job**: `Load-GAF-GOA_m`
**Schedule**: Monthly
**Timeout**: 300 minutes

## Input Files

Three files are downloaded from EBI and concatenated into a single GAF file for processing:

| File | Contents |
|------|----------|
| `goa_zebrafish.gaf.gz` | Main file. GO annotations for zebrafish proteins (UniProt accessions). Sources include UniProt (IEA), GO_Central (IBA), IntAct (IPI), and manual curation. This is the bulk of the data. |
| `goa_zebrafish_isoform.gaf.gz` | Annotations for specific protein isoforms (splice variants). Same format but includes isoform-specific accessions like `UniProtKB:E9QI36-1` in the `geneProductFormID` field. |
| `goa_zebrafish_rna.gaf.gz` | Annotations for non-coding RNA sequences using RNAcentral IDs (`URS*`). Sources are primarily RNAcentral with Rfam evidence. |

Default URLs (overridable via Jenkins environment variables `GOA_GAF_URL1`, `GOA_GAF_URL2`, `GOA_GAF_URL3`):
- `ftp://ftp.ebi.ac.uk/pub/databases/GO/goa/ZEBRAFISH/goa_zebrafish.gaf.gz`
- `ftp://ftp.ebi.ac.uk/pub/databases/GO/goa/ZEBRAFISH/goa_zebrafish_isoform.gaf.gz`
- `ftp://ftp.ebi.ac.uk/pub/databases/GO/goa/ZEBRAFISH/goa_zebrafish_rna.gaf.gz`

## Processing Pipeline

### 1. Download
Downloads all three GAF files. Files 2 and 3 are appended to file 1 to create a combined input. If the file hasn't changed since the last successful run and `skipDownloadIfUnchanged` is true, the job exits early.

### 2. Parse (`FpInferenceGafParser`)
Each line is parsed into a `GafEntry`. Entries are filtered during parsing and rejected if:
- **Evidence code is excluded**: ND, NAS, TAS, or EXP
- **`createdBy` is empty**
- **`createdBy` is "ZFIN"**: We don't reimport our own annotations
- **Taxon is not zebrafish**: Must be `taxon:7955`
- **GO_REF is excluded**: `GO_REF:0000002`, `0000003`, `0000004`, `0000015`, `0000037`, `0000038`

Rejection counts by reason are tracked and included in the error summary report.

### 3. Map to ZFIN Genes (`GafService.getGenes()`)
Each entry's `entryId` (typically a UniProt accession) is looked up in `db_link` to find the associated ZFIN gene:

- **ZDB-GENE / RNAG IDs**: Looked up directly via `markerRepository.getGeneByID()`
- **URS* (RNAcentral) IDs**: Taxon suffix (`_7955`) is stripped, then looked up in `db_link` across all foreign databases. If the link points to a transcript (`ZDB-TSCRIPT-*`), the parent gene is resolved via the "gene produces transcript" marker relationship.
- **All other IDs** (UniProt): Looked up via `db_link` filtered to UniProtKB and GenPept reference databases.

### 4. Validate
Each annotation is validated for:
- GO term not obsolete, not in "Do Not Annotate" subset
- Inference field cardinality and type validity
- Evidence code vs publication consistency
- Protein binding (`GO:0005515`) requires IPI evidence
- No duplicate of an existing or more-specific annotation

### 5. Load
New `MarkerGoTermEvidence` records are batch-inserted. Stale annotations (in the DB but not in the new GAF file) are removed.

### 6. Report
Output files written to `$TARGETROOT/server_apps/DB_maintenance/gafLoad/Load-GAF-GOA_m/`:

| File | Contents |
|------|----------|
| `Load-GAF-GOA_m_summary.txt` | High-level counts (added, removed, errors, existing) |
| `Load-GAF-GOA_m_details.txt` | Full details of every removed, added, updated, errored, and existing entry |
| `Load-GAF-GOA_m_error_summary.txt` | Categorized error analysis with counts, gene-not-found breakdown, and parser rejection stats |
| `Load-GAF-GOA_m_errors.txt` | Stack trace if the job fails with an exception |

The error summary and the regular summary are both included in the success email.

## Example: Loading a Single Annotation

Worked example for a real entry loaded after the IEA cardinality fix:

**GAF line**
```
UniProtKB    A0A0G2KKC2    cnnm1    located_in    GO:0016020    GO_REF:0000104    IEA    UniRule:UR000732112|UniRule:UR001725762    C    Metal transporter    cnnm1    protein    taxon:7955    20260407    UniProt
```

**Console log**
```
Loaded IEA annotation with 2 inferences for A0A0G2KKC2: UniRule:UR001725762, UniRule:UR000732112 | GafEntry{entryId='A0A0G2KKC2', qualifier='located_in', goid='GO:0016020', pmid='GO_REF:0000104', evidenceCode='IEA', inferences='UniRule:UR000732112|UniRule:UR001725762', taxonID='taxon:7955', createdDate='20260407', createdBy='UniProt', ...}
```

### 1. Parse (`FpInferenceGafParser`)

The tab-delimited line is split into a `GafEntry`:
- `entryId` = `A0A0G2KKC2` (col 2)
- `qualifier` = `located_in` (col 4)
- `goid` = `GO:0016020` (col 5)
- `pmid` = `GO_REF:0000104` (col 6)
- `evidenceCode` = `IEA` (col 7)
- `inferences` = `UniRule:UR000732112|UniRule:UR001725762` (col 8, pipe-delimited)
- `taxonID` = `taxon:7955` (col 13)
- `createdBy` = `UniProt` (col 15)

`GoaGafParser.isValidGafEntry()` passes: IEA is not excluded, `GO_REF:0000104` is not excluded, taxon is zebrafish, `createdBy=UniProt` is accepted.

### 2. Map to gene (`GafService.getGenes`)

`A0A0G2KKC2` is not a `ZDB-GENE` / `RNAG` and not a `URS*`, so it falls through to:

```java
sequenceRepository.getMarkerDBLinksForAccession(
    "A0A0G2KKC2",
    ReferenceDatabase.UNIPROTKB, ReferenceDatabase.GENPEPT)
```

This queries `db_link` filtered by the `@DiscriminatorValue("MARK")` (rows where `get_obj_type(dblink_linked_recid) = 'MARK'`) and the two ref-db FKs. Returns one `MarkerDBLink` whose `marker` is the `cnnm1` gene.

### 3. Build the annotation (`generateAnnotation`)

A `MarkerGoTermEvidence` is constructed:
- `marker` = `cnnm1`
- `goTerm` = `GO:0016020` (`membrane`)
- `qualifierRelation` = `LOCATED_IN`
- `evidenceCode` = `IEA`
- `source` = `ZDB-PUB-170525-1` (from `GoDefaultPublication.GOREF_UNIRULE`, because `GO_REF:0000104` is the UniRule default pub)
- `organizationCreatedBy` = `UniProt`
- `createdWhen` = `20260407`

### 4. Validate inferences (`handleInferences`)

The `inferences` field is split on `|` → `["UniRule:UR000732112", "UniRule:UR001725762"]`. For each:
- `isValidCardinality(IEA, {...})`: IEA's cardinality is `CARDINALITY_ONE_OR_MORE`, size 2 → pass (this is the fix that unlocks this annotation)
- `InferenceCategory.getInferenceCategoryByValue(...)` matches `UNIRULE`'s regex `UniRule:.*` → pass
- `validateEvidenceVsPub(IEA, GOREF_UNIRULE, inference)`: the GOREF_UNIRULE pub requires `InferenceCategory.UNIRULE`, which matches → pass

Both pass, two `InferenceGroupMember` objects are created, and the multi-inference log line fires.

### 5. Duplicate check

`getLikeMarkerGoTermEvidencesButGo` checks whether `cnnm1` already has a more specific annotation to a descendant of `GO:0016020` from the same pub + evidence + org. No such annotation exists, so the load proceeds.

### 6. The resulting INSERTs

The `MarkerGoTermEvidence` is `session.save()`d, cascading to its `inferenceGroupMembers` set.

**marker_go_term_evidence** (1 row):
```sql
INSERT INTO marker_go_term_evidence (
    mrkrgoev_zdb_id,                   -- ZDB-MRKRGOEV-<date>-<n>
    mrkrgoev_mrkr_zdb_id,              -- ZDB-GENE-... (cnnm1)
    mrkrgoev_term_zdb_id,              -- ZDB-TERM-... (GO:0016020)
    mrkrgoev_source_zdb_id,            -- ZDB-PUB-170525-1
    mrkrgoev_evidence_code,            -- 'IEA'
    mrkrgoev_qualifier_relation,       -- 'located_in'
    mrkrgoev_flag,                     -- null
    mrkrgoev_organization_created_by,  -- 'UniProt'
    mrkrgoev_date_entered,             -- now()
    mrkrgoev_created_when              -- 2026-04-07
) VALUES (...);
```

**inference_group_member** (2 rows — composite PK = `mrkrgoev_zdb_id` + `inferred_from`):
```sql
INSERT INTO inference_group_member (mrkrgoev_zdb_id, inferred_from)
VALUES ('ZDB-MRKRGOEV-...', 'UniRule:UR000732112');

INSERT INTO inference_group_member (mrkrgoev_zdb_id, inferred_from)
VALUES ('ZDB-MRKRGOEV-...', 'UniRule:UR001725762');
```

Before the IEA cardinality fix (1 → 1+), step 4 would have rejected this annotation outright and no rows would have been inserted.

## Key Code Locations

| File | Purpose |
|------|---------|
| `source/org/zfin/datatransfer/go/service/GafLoadJob.java` | Main job orchestration |
| `source/org/zfin/datatransfer/go/service/GafService.java` | Gene mapping, annotation generation, validation |
| `source/org/zfin/datatransfer/go/FpInferenceGafParser.java` | GAF file parsing and entry filtering |
| `source/org/zfin/datatransfer/go/GoaGafParser.java` | GOA-specific parser extensions |
| `source/org/zfin/datatransfer/go/GafErrorSummary.java` | Error summary report generation |
| `source/org/zfin/gwt/root/dto/GoEvidenceCodeEnum.java` | Evidence code definitions and cardinality rules |
| `source/org/zfin/gwt/root/dto/InferenceCategory.java` | Inference type definitions and regex matching |
| `source/org/zfin/gwt/root/dto/GoDefaultPublication.java` | GO_REF to ZFIN publication mappings |
| `source/org/zfin/gwt/root/ui/GoEvidenceValidator.java` | Evidence and inference validation logic |
| `server_apps/jenkins/jobs/Load-GAF-GOA_m/config.xml` | Jenkins job configuration |

## Error Analysis

### Typical Error Distribution

Based on analysis of 12 builds (Feb 2025 — Apr 2026):

| Category | Typical Count | Notes |
|----------|--------------|-------|
| Gene not found for ID | ~17,000 | Dominant category. See breakdown below. |
| Obsolete GO term referenced | 0–968 | Spikes when GO ontology obsoletes terms, drops to 0 after cleanup |
| IEA inference field validation | ~300 | UniRule, RHEA, Ensembl, SubCell — entries with multiple inference refs |
| Term in "Do Not Annotate" subset | ~38 | |
| Duplicate annotation entry | ~14 | |
| Publication not found for PMID | ~8 | |

### Gene Not Found — Breakdown

The ~17,000 "Gene not found" errors come from ~9,000 unique IDs:

| ID Type | % of unique IDs | Description |
|---------|----------------|-------------|
| RNAcentral (`URS*`) | ~63% | Non-coding RNA IDs. Most are not in ZFIN's `db_link`. 30 unique IDs (55 annotation matches) now load after the TranscriptDBLink fix. |
| TrEMBL (`A0A*`) | ~36% | Unreviewed UniProt entries. Computationally predicted proteins not yet loaded into ZFIN. |
| Swiss-Prot / other | ~1% | Older accessions not present in ZFIN. |

By annotation source: RNAcentral (~51%), UniProt (~36%), GO_Central (~13%).

## Recent Changes and Motivations

### IEA Inference Cardinality: 1 → 1+ (GoEvidenceCodeEnum)

**Problem**: GOA began providing multiple inference references per IEA annotation (e.g., `UniRule:UR000376739|UniRule:UR000414636`). The old cardinality=1 rule rejected these, losing ~322 valid annotations per load.

**Fix**: Changed IEA cardinality from exactly-1 to `CARDINALITY_ONE_OR_MORE`. The individual inference validation (`isInferenceValid`) still filters bad types, and the database schema (`inference_group_member` table) supports multiple members per annotation.

**Monitoring**: IEA annotations with >1 inference are logged with `"IEA annotation with N inferences"`.

### RNAcentral ID Mapping (GafService.getGenes)

**Problem**: The RNA GAF file uses IDs with taxon suffixes (`URS0000005DE0_7955`) but ZFIN stores them without (`URS0000005DE0`). Additionally, RNAcentral `db_link` records point to transcripts (`ZDB-TSCRIPT-*`), which use the Hibernate discriminator `'TSCR'`. The original code queried `MarkerDBLink` (discriminator `'MARK'`), which silently excluded all transcript-linked records.

**Fix**: Added a `URS*` branch in `getGenes()` that strips the `_7955` suffix and queries `TranscriptDBLink` directly. The transcript's parent gene is resolved via the "gene produces transcript" relationship. Confirmed: 55 RNACentral matches from 30 unique URS IDs now load, mapping to miRNA genes (mir196c, mir214a, etc.), lincRNA genes, and lncRNA genes (gas5).

**Monitoring**: All matches are logged with `"RNACentral Match"` prefix.

### Ensembl Inference Case Sensitivity (InferenceCategory)

**Problem**: GOA files use lowercase `ensembl:` but the `ENSEMBL` inference category matched `Ensembl:` (case-sensitive regex). This caused valid Ensembl inferences to fail validation.

**Fix**: Changed the ENSEMBL match pattern to `(?i)ensembl:.*` for case-insensitive matching.

### Evidence vs Pub Validation (GoEvidenceValidator)

**Problem**: `validateEvidenceVsPub` was called with a random inference from a `HashSet` iterator. With multiple inferences now allowed, this was nondeterministic — the validation result could differ between runs depending on which inference the iterator returned first.

**Fix**: Now iterates over all accepted inferences (those that passed `isInferenceValid`) and validates each individually.

### Unrecognized Inference Categories (GoEvidenceValidator)

**Problem**: `InferenceCategory.getInferenceCategoryByValue()` threw a `RuntimeException` for unrecognized values, crashing the entire load.

**Fix**: Caught in `validateEvidenceVsPub` and converted to a `ValidationException` so the entry is rejected gracefully and reported in the error summary.

### Unknown Evidence Codes (FpInferenceGafParser)

**Problem**: Evidence codes not in `GoEvidenceCodeEnum` (like `EXP`, newly introduced by GO) caused a `RuntimeException` that aborted the entire load during parsing.

**Fix**: Added `EXP` to the explicit exclusion set alongside `ND`, `NAS`, and `TAS`. Unknown codes that aren't in the exclusion set still throw (so new codes are surfaced as errors rather than silently ignored).

### Parser Rejection Tracking (FpInferenceGafParser)

**Problem**: Entries filtered during parsing were logged individually at WARN level (noisy, ~100k+ messages) with no summary of why they were rejected.

**Fix**: Changed to DEBUG level. Added `rejectionCounts` map that tracks counts by reason (e.g., "Excluded GO_REF: GO_REF:0000002", "Excluded evidence code: EXP"). These counts are included in the error summary report.

### Error Summary Report (GafErrorSummary)

**Problem**: The details file contained ~19,000 error lines with no categorization or analysis. Understanding the error landscape required manual parsing.

**Fix**: Added `GafErrorSummary.java` that processes the error list directly from `GafJobData` after the load completes. Generates a categorized report with:
- Error counts by category
- Parser rejection counts by reason
- Gene-not-found deep dive (by ID type and annotation source)
- Top 20 unmapped IDs
- Examples for each error category

Output: `Load-GAF-GOA_m_error_summary.txt`, also included in the success email.

## Grepping the Console Output

```bash
# RNAcentral matches (new gene mappings)
grep "RNACentral Match"

# IEA annotations with multiple inferences
grep "IEA annotation with"

# Error summary file location
grep "Error summary written to"

# Unrecognized inference categories (caught, not crashing)
grep "Unrecognized inference category"

# Invalid NCBI API key warning (from NCBIRequest, used by UniProt load)
grep "NCBI API token appears invalid"
```

## Historical Trend Analysis

A one-off analysis of 12 months of historical details files was performed to produce trend graphs. The results are in https://zfin.atlassian.net/browse/ZFIN-10231 (gaf-error-report.html). Going forward, the Java `GafErrorSummary` generates the error summary automatically as part of each load run.

Key findings:
- RNAcentral errors are constant (~5,730 unique IDs / ~8,969 occurrences) — structural, won't change unless ZFIN maps more RNA IDs
- TrEMBL errors fluctuate with UniProt releases
- Obsolete GO term errors spike when the ontology updates, then resolve
- IEA inference validation errors have been growing as GOA provides richer multi-reference evidence
