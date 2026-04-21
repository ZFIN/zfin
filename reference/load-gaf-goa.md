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
