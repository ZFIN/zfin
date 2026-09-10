# NCBI / Ensembl Gene ID Mapping

## Overview

ZFIN links its gene records to two external gene identifier systems: **NCBI Gene IDs** and
**Ensembl gene IDs (ENSDARG)**. Two independent loads maintain them, and a third strategy inside
the NCBI load uses one to corroborate the other. In 2026 Ensembl reissued every zebrafish gene
stable ID for the GRCz12 assemblies, which broke the link between the two systems and is the
reason this document exists.

Counts and file states below are marked with the date they were measured. The NCBI input file is
rebuilt daily, so re-measure before acting on a number.

## Where each identifier comes from

| ZFIN link | Container | Maintained by |
|---|---|---|
| NCBI Gene ID | `ZDB-FDBCONT-040412-1` | Jenkins `NCBI-Gene-Load-Java` (`NCBIDirectPort`) |
| Ensembl gene ID (GRCz11) | `ZDB-FDBCONT-061018-1` | `server_apps/data_transfer/Ensembl/fetch_ensdarg.sh` |
| Ensembl gene ID (second copy) | `ZDB-FDBCONT-200123-1` | same; named `ExpressionAtlas` in `foreign_db` despite holding ENSDARGs |

### ZFIN does not compute Ensembl matches — it imports them

This is the single most important thing to know before changing anything here.

`fetchEnsdarg.groovy` asks BioMart for two attributes and nothing else:

```xml
<Dataset name="drerio_gene_ensembl">
  <Filter name="chromosome_name" value="1,2,…,25,MT"/>
  <Filter name="with_zfin_id" excluded="0"/>
  <Attribute name="ensembl_gene_id"/>
  <Attribute name="zfin_id_id"/>
</Dataset>
```

`zfin_id_id` **is Ensembl's own ZFIN cross-reference**. There is no sequence comparison, no
coordinate overlap, no symbol matching on the ZFIN side. Ensembl's genebuild decides which ZFIN
gene an ENSDARG corresponds to; this load reads that decision out and caches it in `db_link`.

Everything ZFIN adds is ambiguity rejection — a 1:1 filter in the groovy, then merge repointing
via `zdb_replaced_data` and further 1:1 pruning in `load_ensdarG.sql`.

The vestigial `fetch_ensdarG.mysql` did the same thing one layer down, reading Ensembl's `xref`
table directly. Note it filtered `external_db_id in (2510,2530)` — **two** ZFIN external DBs —
whereas the modern flat file exposes a single `db_name = 'ZFIN'`. That difference is the leading
hypothesis for the coverage gap described below and has not been confirmed.

**Consequence**: the NCBI load's Ensembl-based strategy was never comparing ZFIN's own opinion
against NCBI's. ZFIN's `db_link` was a cached copy of Ensembl's opinion, so the comparison was
always Ensembl-vs-NCBI with ZFIN as an intermediary.

## The GRCz12 stable ID renumbering

Ensembl minted an entirely new ENSDARG block for the GRCz12 assemblies. The blocks are disjoint —
not a version suffix, not a renumbering within the old range.

| Assembly | GCA | ID block | Notes |
|---|---|---|---|
| GRCz11 | GCA_000002035.4 | `ENSDARG00000…` (≤ 117822) | what ZFIN holds |
| **GRCz12tu** | GCA_049306965.1 | `ENSDARG00160…` | what NCBI's `gene_info` uses |
| GRCz12ab | GCA_052040795.1 | `ENSDARG00180…` | **different block** |

The same gene gets a new ID, e.g. `ZDB-GENE-030131-5654` is `ENSDARG00000074384` on GRCz11 and
`ENSDARG00160027920` on GRCz12tu.

Two traps:

- **NCBI's reference assembly is GRCz12ab, but its `gene_info` Ensembl xrefs are GRCz12tu.**
  Loading GRCz12ab IDs would leave ZFIN disjoint from NCBI all over again.
- **Classic Ensembl infrastructure is GRCz11-only and will stay that way.** Release 116
  (Aug 2026) still ships `Danio_rerio.GRCz11.116.*`; `release-116/mysql/` contains only
  `danio_rerio_*_116_11` (the `_11` suffix is the assembly version); BioMart is
  `ensembl_mart_116` = GRCz11 and now lives on an archive host. The GRCz12 genesets exist only
  on the newer organism-centric FTP.

## Input files

```
# NCBI — rebuilt daily
https://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz

# Ensembl GRCz12tu geneset (genebuild 2025_12)
https://ftp.ebi.ac.uk/pub/ensemblorganisms/GCA/049/306/965/1/ensembl/2025_12/geneset/xref.tsv.gz
https://ftp.ebi.ac.uk/pub/ensemblorganisms/GCA/049/306/965/1/ensembl/2025_12/geneset/genes.gtf.gz

# Ensembl GRCz11 geneset (genebuild 2018_04) — for old→new comparison
https://ftp.ebi.ac.uk/pub/ensemblorganisms/Danio_rerio/GCA_000002035.4/ensembl/geneset/2018_04/xref.tsv.gz
```

The species-name path form (`…/ensemblorganisms/Danio_rerio/GCA_049306965.1/ensembl/geneset/2025_12/`)
resolves to the same directory. Note the segment order differs between the two forms.

`Danio_rerio.gene_info.gz` is tab-delimited with a `#` header. Column 6 is `dbXrefs`,
pipe-separated, carrying `ZFIN:ZDB-GENE-…` and `Ensembl:ENSDARG…`. The load downloads it as
`zf_gene_info.gz`. `gene2ensembl.gz` on the same FTP tree carries the NCBI↔Ensembl mapping as
proper columns and would be a cleaner feed, but is not currently used.

`xref.tsv.gz` has a real header row: `gene_stable_id · transcript_stable_id · protein_stable_id ·
xref_id · xref_label · description · db_name · info_type · source · ensembl_identity ·
xref_identity`. Filter `db_name = 'ZFIN'`; `gene_stable_id` is the ENSDARG and `xref_id` is the
ZDB gene ID. Only `db_name = 'ZFIN'` carries ZDB-shaped values — the file also has 31k
"ZFIN transcript name" rows, none of which are gene IDs.

## NCBI load matching strategies

Three strategies produce NCBI Gene ID links, each stamped with its own attribution pub. See
`getZdbGeneIdAndAttributionByNCBIgeneId` for the priority order: RNA > supplement > Vega.

### 1. GenBank RNA reciprocal 1:1 — `ZDB-PUB-020723-3`

The primary strategy. Matches on **GenBank/INSDC RNA accessions only**.

- ZFIN side: `prepareNCBIgeneLoad.sql` dumps `toMap.unl` from `ZDB-FDBCONT-040412-37` (GenBank
  RNA) db_links, excluding accessions attributed to the load pubs themselves and `WITHDRAWN%`
  genes, with a union pulling RNAG-segment links up to the parent gene.
- NCBI side: `gene2accession.gz`, taxid 7955, `SUPPRESSED` skipped, and **only rows where status
  is `-`** (the non-RefSeq tier) feed matching. RefSeq rows populate separate maps used to attach
  accessions to already-matched genes.
- `RefSeqCatalog.gz` supplies sequence lengths only; it plays no part in matching.

The match is strictly reciprocal 1:1. Any accession supporting more than one gene on either side
disqualifies the whole gene; leftovers become the 1:N / N:1 / N:N reports.

### 2. Shared Ensembl ID, "NCBI supplement" — `ZDB-PUB-230516-87` (ZFIN-8517)

Gated by `LOAD_NCBI_ONE_WAY_GENES`. The entry point is **NCBI's** assertion, not ZFIN's: NCBI's
`gene_info` carries a `ZFIN:` xref for a gene ZFIN has no reciprocal link to, and a shared Ensembl
gene ID corroborates it. Implemented in `NcbiMatchThroughEnsemblTask`.

### 3. Legacy Vega — `ZDB-PUB-130725-2`

Preserve-only; `buildVegaIDMappings()` is commented out. `captureLegacyVegaMatches()` snapshots
existing Vega-attributed links before the load deletes them and `reintroduceLegacyVegaLinks()`
re-asserts the ones NCBI still cross-references.

### Not a strategy: symbol matching

`NcbiGeneSymbolMatchTask` produces `ncbi_gene_symbol_matches.csv` and lands in the report as
curator triage. **Do not promote it to load evidence.** It looks compelling — 99.9% of one
at-risk set matched — but for zebrafish the nomenclature authority *is* ZFIN
(`nomenclature_status = 'O'`, `symbol_from_nomenclature_authority` populated), so NCBI's symbol
came from ZFIN. A wrong xref carries the wrong symbol just as consistently, making the match
circular and unable to detect the error it appears to check.

## What broke

### Strategy 2 lost its corroboration

The join is `db_link.dblink_acc_num = <NCBI's Ensembl xref>`. NCBI migrated to the GRCz12tu ID
block while ZFIN still holds GRCz11 IDs, so the overlap went to zero.

Measured from the load's own archives: in the run of 2026-08-21 NCBI's file still carried 1,468
legacy GRCz11 IDs alongside 22,920 new-block ones, and the strategy produced 423 matches against
1,973 the run before — deleting 1,550 links. By 2026-08-28 NCBI had dropped **all** legacy IDs
(31,602 Ensembl xrefs, 0 old-block), so the strategy now derives **nothing**.

### The delete is unconditional, the re-derivation is not

`removeEnsemblMatchesFromDB()` deletes every supplement-attributed NCBI Gene link and its
RefSeq/GenBank children at the start of each run, with **no** `LOAD_NCBI_ONE_WAY_GENES` guard —
the flag gates only re-derivation. `prepareNCBIgeneLoad.sql` independently puts those links into
`pre_delete`. **Setting the flag false therefore deletes the links and re-derives none.**

### The Ensembl load cannot run

`fetchEnsdarg.groovy` requests `http://www.ensembl.org/biomart/martservice`, which now returns
**HTTP 308** to an archive host. Java's `HttpURLConnection` follows neither 308 nor an
http→https protocol change, so `ensdarg.csv` comes back **empty**.

`load_ensdarG.sql` has **no empty-input guard**. With `ens_zdb` empty its `NOT EXISTS` delete
matches every ENSDARG link in `ZDB-FDBCONT-061018-1` on a `ZDB-GENE%`:

```
candidate ENSDARG links     25,924
protected by Sanger pubs     5,167
WOULD BE DELETED            20,757
```

The script is manual-only (`fetch_ensdarg.sh`, no Jenkins job) and commits unconditionally, so
this is a loaded footgun rather than a live failure — but it is aimed precisely at whoever next
tries to refresh the Ensembl IDs. **Add the guard before running it.** Fixing the redirect alone
would not help: the archive BioMart serves GRCz11.

## Coverage arithmetic

As of 2026-09-02.

ZFIN markers holding a GRCz11 ENSDARG, by type:

| Type | ZFIN markers | With ENSDARG | Coverage |
|---|---|---|---|
| GENE | 36,025 | 25,469 | 70.7% |
| LINCRNAG | 915 | 846 | 92.5% |
| MIRNAG | 426 | 325 | 76.3% |
| GENEP | 418 | 257 | 61.5% |
| **all GENEDOM_AND_NTR** | **38,439** | **26,998** | **70.2%** |

What can be mapped to a new-block ID:

| Route | Markers covered | Of 26,998 |
|---|---|---|
| Ensembl `xref.tsv.gz` (GRCz12tu) | 7,849 | 29.1% |
| NCBI `gene_info` | 20,797 | 77.1% |
| union | 21,023 | 78.0% |

So roughly **5,975 markers** lose their Ensembl gene ID under any currently available route.

The ceiling is Ensembl's xref propagation, not the method. GRCz12tu's geneset has 60,968 genes
(26,698 protein-coding) but ZFIN cross-references for only 7,888 — 15.4%. The mature GRCz11
genebuild reaches 14,481, so waiting should roughly double coverage, but even that is far short of
ZFIN's 26,998. In-place refresh of a genebuild directory does happen (GRCz11's `2018_04` was
re-dumped in 2025), but on no published schedule.

**Do not use NCBI-derived mappings as strategy 2's evidence.** Deriving the Ensembl ID from NCBI's
own ZFIN assertion makes the corroboration vacuous. It is sound as a translation table for the
Ensembl load, and its disagreements with Ensembl are a useful curation list.

## Open decisions

1. **Protect the supplement links before the next run.** 1,971 links are attributed to
   `ZDB-PUB-230516-87` and **100%** are still asserted by NCBI's `gene_info` — the mappings are
   sound, only the corroboration mechanism is gone. The next run deletes all of them and
   re-derives none. The Vega capture/validated-re-assert pattern is the template. Note that
   preserving the NCBI Gene link alone is not enough: the gene must also land in
   `ncbiSupplementMap` so `getZdbGeneIdAndAttributionByNCBIgeneId` resolves it and its
   RefSeq/GenBank accessions are reloaded.

2. **A fresh strategy 2 is worth having but is not a recovery plan.** Joining NCBI's `gene_info`
   to Ensembl's `xref.tsv.gz` directly on the Ensembl gene ID — never consulting ZFIN's `db_link`,
   so no ID migration is needed — proposes 7,506 links, of which 7,057 are already RNA-pub links.
   Its unique contribution is **428 of the 1,971** recovered plus 16 new. 184 are contradicted
   (Ensembl names a different ZFIN gene) and 1,359 have no evidence either way. Preservation must
   come first.

3. **Migrating ZFIN's Ensembl IDs** means rewriting the Ensembl load to parse a TSV rather than
   query BioMart, and accepting the coverage gap — or loading GRCz12tu additively alongside GRCz11
   rather than replacing it.

4. **Confirm the `external_db_id in (2510,2530)` question.** If the file's single `ZFIN` xref type
   corresponds to only one of the two, no amount of waiting closes the gap and the additive
   approach is the only route.

5. **Retire or repoint the GRCz11 coordinate load.** `Load-NCBIStartEndPositions` is now pointed
   at `all_assembly_versions/suppressed/`, where the feature table is frozen at 2024-09-12, so it
   re-loads identical coordinates weekly. If GRCz11 start/end positions are being retired, retire
   the job and delete its rows instead.

## Related tables

`sequence_feature_chromosome_location_generated` holds gene coordinates from two NCBI-derived
sources with very different staleness behaviour:

| Source | Assembly | Write strategy | Self-corrects? |
|---|---|---|---|
| `NCBILoader` | GRCz12tu | incremental upsert **keyed on accession only** | no |
| `NCBIStartEndLoader` | GRCz11 | full delete + rebuild from current `db_link` | yes |

`NCBILoader`'s upsert never revisits the gene, so a pairing invalidated by the NCBI load is never
corrected — that is ZFIN-10461, addressed by `reconcileNcbiGenomeLocations()`. `NCBIStartEndLoader`
cannot go stale by construction, which is why the reconciliation deliberately excludes it.

`marker_annotation_status` is rebuilt wholesale by `loadNCBIgeneAccs.sql` purely from NCBI Gene
db_links, so it follows them exactly. It is marker-keyed, not db_link-keyed — joining it onto every
db_link row of a gene turns one gene-level change into ~17 reported changes.
