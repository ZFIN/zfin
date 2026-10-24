# Assembly blast radius

ZFIN-10529 follow-up · discussion brief
Branch `annotation-status-view`, on top of `zfin-10529` · Updated 2026-10-23 · Counts are from the **dev database**, not production

We want the database, not the code, to say which genome assemblies exist and which genes are on them. This page lists what that touches, what has been built for the annotation status, and the steps to load GRCz13ab.

Sections: [What has been built](#what-has-been-built) · [Direction](#what-we-are-changing) · [Cached links](#where-marker-to-assembly-answers-are-cached-today) · [Hard-coded assemblies](#where-an-assembly-is-written-into-code) · [Plain join, GRCv10](#a-plain-join-and-grcv10) · [Genome browsers](#where-a-genome-browser-is-displayed) · [Annotation status](#annotation-status) · [Load publications](#load-publications-as-data) · [Loading GRCz13ab](#loading-grcz13ab) · [Decisions](#decisions)

---

## What has been built

The Genome Resources ordering and the annotation status rework are implemented on branch `annotation-status-view`: 8 commits, 34 files, about 1,000 lines added and 280 removed. Nothing is pushed from the sandbox. Counts below are from the dev database.

| Commit | What it does |
|---|---|
| `085c0aede3` | Genome Resources order: Alliance, NCBI Gene, then links by assembly rank. Adds `assembly.a_is_current` (GRCz13ab and GRCz12tu) and `foreign_db.fdb_assembly_id`. Links on non-current assemblies and Vega collapse behind the site's existing "(all N)" toggle. |
| `350ca56b65` | `load` and `load_publication` tables, seeded for the NCBI Gene load with four publications. Typed columns for precedence, load-managed and confers-Current. |
| `4261e2a8fa` | `external_resource.ncbi_not_in_current_release`, keyed by NCBI Gene ID, refilled by the load. A fetch that is empty or less than half the stored size is refused and the stored list is used. |
| `d6234432ec` | View `marker_annotation_status_derived`. `unload_production.sh` now excludes only `ncbi_danio_rerio_gene_info` data, so the not-in-release rows are dumped. |
| `a219e81692` | The page, `Marker.getAnnotationStatus()`, the load's conflict reports and the snapshots read the view. `NCBIGff3Processor` no longer writes statuses. |
| `30d0e5c3e3` | Drops `marker_annotation_status`, its block in `loadNCBIgeneAccs.sql`, and its entry in `convert_marker_type()`. |
| `f11fb0c449` | The load's managed, precedence and created-by-load publication lists read `load_publication`. The load checks at startup that its strategies' publications are recorded. |
| `4e229f9676` | The load stops if `toLoad.unl` names a publication `load_publication` does not list. |

**The status now.** Not in current release if NCBI lists one of the gene's NCBI Gene IDs. Otherwise Current if the link is attributed to a Current-conferring publication, or the gene has an `NCBILoader` location on a current assembly. Otherwise Unknown. Against the stored table it agrees on 22,194 Current and 2,799 Not in current genes, and adds 55 Current genes (54 genes and 1 ncRNA gene). A single-gene lookup takes 0.4 ms.

**Checked.** The four sample gene pages show Current, Not in current, Current (a new one) and Unknown after the table was dropped. The prepare script gives the same `toDelete.unl` byte for byte and the same `toMap.unl` once sorted. 26 new or changed database and unit tests pass.

**Not run.** The full NCBI load and its integration tests, which empty and refill tables. `MarkerMergeServiceTest` fails here for lack of fixture data, with or without these changes, and the test rewritten to plant a `marker_assembly` row could not be run.

### Still open, and noticed on the way

- **Not started:** making `gff3_ncbi` multi-assembly and moving it to `external_resource`, the generalized loader, retiring `marker_assembly`, the GRCz13ab load.
- **A likely omission:** the GenPept check in `NCBIDirectPort` leaves the supplement publication out of its "load publications" list, with a comment that it matches the original Perl. Left unchanged.
- **Single-publication bindings kept as constants:** the matching strategies, the Ensembl supplement cleanup, the Vega capture, and three RNA-only clauses in `loadNCBIgeneAccs.sql`.
- **The NCBI query** asks for at most 10,000 results and never checks NCBI's reported total. The list is about 2,800 today.

---

## What we are changing

- **Lean on the database for assemblies.** Assembly names, ids, GFF3 locations and "current" belong in the `assembly` table (`a_order`, `a_is_current`, `a_gcf_identifier`). Code should ask the table, not name `GRCz12tu`.
- **Decide links in real time.** A gene's link to an assembly, or to a status, should come from a view over the data that proves it. Stored copies of that answer go stale, as the one-gene gap between `marker_assembly` and the location table already shows.
- **Done so far.** The annotation status follows this direction: it is a view, and its stored table is gone. The assembly side (`marker_assembly`, the GFF3 cache, hard-coded GRCz12tu) is not started.

---

## Where marker-to-assembly answers are cached today

Four tables held a stored answer to "is this gene on this assembly" or "is it current". Only the last is a source of truth. The status table has since been replaced by a view.

| Stored copy | What it holds | Written by | Real-time replacement | Read by |
|---|---|---|---|---|
| `gff3_ncbi_attribute` rows with key `gene_id` | A ZFIN gene id attached to a GFF3 gene line. 22,105 rows. | `markerAssemblyUpdate.sql` | A view joining the GFF3 `GeneID:` accession to the gene's NCBI Gene `db_link`. | `NCBIGff3Processor.getGeneZdbID`, `Gff3Writer` (the outgoing JBrowse file) |
| `marker_assembly` | Gene and assembly. GRCz12tu 22,789 rows, GRCz11 24,632. | `markerAssemblyUpdate.sql`, both GFF3 processors | A plain join of the location table to `assembly`, one row per gene, assembly and source. Filtering happens in each consumer, not in the view. | `Marker.assemblies` (the "Genome Assembly" row, the browser gate), 9 places in `NCBIDirectPort`, snapshot SQL, and the marker-merge table list |
| `marker_annotation_status` | "Current" or "Not in current annotation release" per gene. | `loadNCBIgeneAccs.sql` (wiped and rebuilt every run), and `NCBIGff3Processor` | **Built.** The view `marker_annotation_status_derived`. The table is dropped. | The page, the load reports, `Check-Genes-Missing-External-IDs.sql` and the snapshots now read the view. |
| `sequence_feature_chromosome_location_generated` | Coordinates per gene, assembly and source. | The NCBI load (`NCBILoader` and `ZFIN` rows) | Kept: source of truth | Genome browser links, locations, the replacement views |

**The copies have already drifted.** On GRCz12tu, 22,370 genes have a location row and 22,369 have a `marker_assembly` row. The gap is `ZDB-GENE-080522-3` (bicc1b), which has a `ZFIN`-source location and no other row. No gene has a `marker_assembly` row without a location. The other 420 GRCz12tu `marker_assembly` rows are not `ZDB-GENE-` markers and were not compared.

---

## Where an assembly is written into code

Grouped by what breaks if the assembly changes. Roughly 40 non-test files mention GRCz12tu. Only the ones that matter for this work are listed. The annotation status code and reports no longer name an assembly; the rest below is unchanged.

| Area | Where | What is fixed |
|---|---|---|
| Load | `markerAssemblyUpdate.sql` | Assembly id `1`, the literal `'GRCz12tu'` in the location inserts and guards, and a GRCz11 block with id `3`. |
| Load | `NCBIGff3Processor`, `NCBIGff3PostProcessor` | `GRCZ12TU_ASSEMBLY_ID = 1L`, `find("1")`, and the GRCz12tu `.1` file name and URL. NCBI has since published `.2`. |
| Display | `GeneViewController`, `PseudoGeneViewController` | The genome browser block only shows when `latestAssembly.getName().equals("GRCz12tu")`. |
| Display | `Marker.getLatestAssembly` | Picks the lowest `a_order`. GRCz13ab has order 5 and GRCz12tu 10, so one GRCz13ab row on a gene makes 13ab its "latest". |
| Display | `MappingService`, `MappingDetailController`, `SequenceTargetingReagentViewController`, `GenomicLocationService`, `GenomeLocation.Source` | Switch cases on assembly name, the FASTA file name, and a Map Viewer link with `assm=GCF_049306965.1`. |
| Export | `BasicVariantInfo`, `VariantLinkMLInfo` | `AssemblyEnum.GRCZ12TU` chooses which feature locations go to the Alliance. This is an outside consumer. |
| Export | `Gff3Writer`, `Generate-Alliance-Files_m` | File names `zfin_genes.grcz12.gff3` and `zfin_refseq.grcz12.gff3`, and `setAssembly("GRCz12tu")`. The Alliance job reads the gene file by name. |
| Jobs | `Load-NCBI-GFF3-File`, `Refresh-GBrowse-Tracks_d`, `Check-Markers-Features-Needing-GRCz12tu-Coordinates`, the knockdown-reagent scripts | GRCz12tu in descriptions, snapshots, Bowtie alignments and the liftover report. |
| Curation | `FeatureRPCServiceImpl`, `AbstractFeatureView` | The feature assembly choice allows only GRCz12tu and GRCz11. |
| Features | `HibernateLinkageRepository.getFeatureLocations` | Filters to the list `('GRCz12tu','GRCz11','GRCz10','Zv9')` and orders by `substring(loc.assembly,4)`, which sorts the names as text, not by `a_order`. |
| Generic already | `HibernateFeatureRepository`, `CheckFlankingSequenceDriftTask`, `NcbiAssemblyResolver`, the RefSeq reconcilers | Rank by `a_order` or resolve from the `assembly` table. These are the pattern to copy. |

---

## A plain join, and GRCv10

The replacement for `marker_assembly` should only join. Deciding which locations count belongs to the code that uses them. Two findings come from comparing a plain join with the table as it is today (`ZDB-GENE-` markers, dev database).

| Assembly | Plain join | In `marker_assembly` | Added by a plain join |
|---|---:|---:|---:|
| GRCz12tu | 22,370 | 22,369 | 1 |
| GRCz11 | 28,195 | 24,202 | 3,993 |

- **The "no GRCz12tu row" clause is redundant.** `getLatestAssembly()` already takes the lowest `a_order`, so a gene on both assemblies shows GRCz12tu. Only the root-only list of all assemblies would also show GRCz11.
- **The source filter is real.** 3,827 of the 3,993 added GRCz11 rows come from `other map location`, a genetic map position and not a placement on the genome sequence. 3,817 genes would go from Unknown to GRCz11. `GenomeLocation.Source` already marks which sources are physical placements; that knowledge lives only in Java.
- **Proposed layering.** The join exposes the source. Each consumer filters: the status rule to `NCBILoader` on current assemblies, the "Genome Assembly" row to genome placements, ranked by `a_order`. A small table of location sources with a placement flag would put the Java knowledge in the database.

### GRCv10 is not in the assembly table

| Fact | Detail |
|---|---|
| Size | 62,173 location rows across 61,726 markers. The only assembly name in `sequence_feature_chromosome_location_generated` that is missing from `assembly`. |
| Sources | `VegaStartEndLoader` (62,067 rows) and `NCBIStartEndLoader` (106). |
| Genes | 23,348 genes have a GRCv10 row. 1,524 of them have no location on any assembly in the table. |
| Origin | Not a real assembly. It was the default when `sfclg_assembly` was added to the table. A later migration relabeled the `ZfinGbrowse` and `Ensembl` rows to GRCz11 and left the Vega and NCBI ones. |
| Today | Invisible to assembly-aware code. The feature query filters it out, and nothing in `marker_assembly` points to it. `sfclg_assembly` is free text with no foreign key. |

**Decided and built:** it is now a legacy row in `assembly` (migration `0070`; a test checks that every assembly named in the location table is listed). The text below records the options.

Options: add it to `assembly` as a legacy row (high `a_order`, no GCF identifier, not current, like the existing `GRCz11 or older`), which also allows a foreign key from `sfclg_assembly`; relabel the rows to the assembly the Vega build used, which someone who knows that load must say; or leave it, in which case a plain join drops those rows. If it is added, the 1,524 genes with no other assembly would show "GRCv10" in the page's Genome Assembly row instead of Unknown, unless the display ignores legacy assemblies.

---

## Annotation status

This section was written to compare three depths for the rewrite. Option C was built; the options stay here as the record of what each would have cost.

### How "Current" was set before this branch

It was a stored value, decided when the NCBI Gene load ran. The load deleted every row in `marker_annotation_status` and rebuilt it from each gene's NCBI Gene `db_link`:

| Order | Condition on the gene's NCBI Gene link | Stored status |
|---|---|---|
| 1 | The NCBI Gene ID is on the "not in current release" list, from a live NCBI query (`NCBIEfetch`). | Not in current annotation release (id 13) |
| 2 | Otherwise, the link is attributed to `ZDB-PUB-020723-3` (shared RNA sequence IDs) or `ZDB-PUB-230516-87` (shared Ensembl IDs). | Current (id 12) |
| 3 | Otherwise, or the gene has no NCBI Gene ID. | No row, shown as Unknown |

- **A second writer.** `NCBIGff3Processor`, which runs separately and rarely, also set Current when it created a gene's first GRCz12tu genome location.
- **The page only read the row.** `Marker.getAnnotationStatus()` showed Unknown for no row, otherwise the first status name alphabetically. In dev no gene had more than one row.
- **It did not look at assemblies.** In dev, 101 genes were Current with no GRCz12tu `marker_assembly` row and 28 were Not in current with one.

### Three depths

The ticket asks for "Current" when a gene is on GRCz12tu or GRCz12ab. NCBI now lists only GRCz12tu and GRCz13ab as latest, and we set GRCz12ab not current, so the rule is: **Not in current if NCBI says so; otherwise Current if the link is attributed to a Current-conferring publication, or the gene has an NCBI-loader location on an assembly where `a_is_current`; otherwise Unknown.** On the dev data that changes 55 markers, all from Unknown to Current. No gene loses Current.

**A. Minimal: a view on top of the stored rows.**
- Create the view; point `Marker.getAnnotationStatus()` at it.
- `NCBIGff3Processor` stops writing statuses.
- Reports compare stored against effective status.
- Reword the status description page.
- Left alone: `loadNCBIgeneAccs.sql` keeps writing 12 and 13; hard-coded load publication ids stay.
- Smallest change, still has a status table.

**B. Store only "not in release": stop storing Current.** Adds to A:
- Drop the "Current" step and publication list from `loadNCBIgeneAccs.sql`.
- Delete the stored Current rows (22,194 in dev).
- Derive Current from the NCBI link's load attribution, or from a location row.
- Remove the "Current without GRCz12tu" report; rework the other three.
- Update `Check-Genes-Missing-External-IDs.sql` (it reads id 12), the snapshot SQL, and the load tests.
- Needs first: which publications count as load publications, as data and not code.
- One stored fact, still keyed by marker.

**C. Its own external table, keyed by NCBI Gene ID. (Built.)** Adds to B:
- New `external_resource` table of NCBI Gene ids not in the current release, refilled each run from the same NCBI query that writes `notInCurrentReleaseGeneIDs.unl`.
- Drop `marker_annotation_status`.
- Remove it from the marker-merge table list (`convert_marker_type.sql`) and from `MarkerMergeServiceTest`.
- Point the load's temp table, `NcbiGeneSymbolMatchTask` and the report tagging at the table. (The load's temp table and `NcbiGeneSymbolMatchTask` still read the file.)
- Gains: no per-gene state to merge, delete or leave stale.

**Option C and database dumps.** The dump script excluded all `external_resource` table rows. Left that way, a restored database would show every "not in release" gene as Current until the next NCBI load. At about 2,800 rows the table is small enough to dump, so the script now excludes only the gene info table by name. That makes it the first `external_resource` table the status display depends on.

**A third load publication that the load did not know about.** Of 22,194 stored Current genes, 20,777 traced to the two load publications hard-coded in `loadNCBIgeneAccs.sql` (`ZDB-PUB-020723-3`, `ZDB-PUB-230516-87`). The other **1,417** traced to `ZDB-PUB-261014-17`, "Curation of NCBI Gene Data Via Shared Ensembl IDs from GRCz12 assemblies (Supplemental NCBI Load)", dated 2026-10-14. That id appeared nowhere in the repository. Reading the SQL, the next full load would have deleted every status row and rebuilt from the two listed publications, so those 1,417 genes would have fallen back to Unknown. 1,403 of them have a current-assembly location row. I have not run the load to confirm. This publication is now a row in `load_publication`, and the status no longer depends on a stored copy.

### Where "not in release" comes from

It is a live NCBI query (`NCBIEfetch`), written to `notInCurrentReleaseGeneIDs.unl` and now also stored in `external_resource.ncbi_not_in_current_release`. Beyond the status it also decides which NCBI links the load preserves from deletion, tags report rows, and feeds `NcbiGeneSymbolMatchTask`.

---

## Load publications as data

The NCBI load tells its own links apart by the publication they are attributed to. Those publication ids were written into code and SQL in four places, each for a different purpose.

| Where | What the ids decide |
|---|---|
| `prepareNCBIgeneLoad.sql` | Which attributions mean the load owns the link, so it can delete and replace it. |
| `loadNCBIgeneAccs.sql`, priority block | An explicit ranking when several methods match: `ZDB-PUB-020723-3` first, `ZDB-PUB-230516-87` second. |
| `loadNCBIgeneAccs.sql`, status insert | Which methods make a gene Current. |
| `NCBIDirectPort` | Three constants (`PUB_MAPPED_BASED_ON_RNA`, `_VEGA`, `_NCBI_SUPPLEMENT`) for report labels and counts. |

| Publication | Method | NCBI Gene links in dev | In the load SQL before |
|---|---|---:|---|
| `ZDB-PUB-020723-3` | Shared RNA sequence IDs | 23,501 | Yes |
| `ZDB-PUB-261014-17` | Shared Ensembl IDs from GRCz12 assemblies (2026) | 1,417 | **No** |
| `ZDB-PUB-130725-2` | Shared Vega gene IDs (legacy matches: the load removes them and re-asserts the ones no stronger method replaced, and never matches new genes this way) | 124 | Constant only |
| `ZDB-PUB-230516-87` | Shared Ensembl IDs (2023 supplement) | 0 | Yes |

### The table, as built

| Column | Replaces |
|---|---|
| `lp_ld_id` (foreign key to `load`) | Which load the row belongs to. |
| `lp_pub_zdb_id` (foreign key to `publication`) | The magic string. |
| `lp_precedence`, unique within the load | The ranking in the priority block. Null if the load does not create records with the publication. |
| `lp_label` | The report constants. |
| `lp_load_managed` | The ownership lists in `prepareNCBIgeneLoad.sql` (the `pre_delete` set). False for the Vega method, which the Java load handles separately. |
| `lp_confers_current_gene_status_via_ncbi` | The publication list in the status insert. NCBI Gene load only; other loads leave it false. |

- **What it fixed.** The SQL joins to the table where it had an `IN (...)` list, a new matching method is a data row, and the load stops if its load file names a publication the table does not list. `ZDB-PUB-261014-17` is now a row.
- **Kept narrow.** The Current column is named for the NCBI load because only that load uses it. A key/value table was considered and rejected: the status view would still hard-code the key name. Add another column when a second load needs one.
- **The `load` table, in part.** `load` and `load_publication` exist. The planned `load_file` child and the restructured `load_file_log` are not built, and nothing reads them yet.

---

## Where a genome browser is displayed

Evidence for D4. The same material with production screenshots is in [`jbrowse-issues/genome-browser-issues.md`](jbrowse-issues/genome-browser-issues.md). Each place below chooses its assembly on its own, so there is no single rule to replace. Counts and IDs are from the dev database.

| # | Where | Example URL | How the assembly is chosen today |
|---|---|---|---|
| 1 | **Gene page, Sequences** | `/ZDB-GENE-990415-72#sequences` | `GeneViewController`. The gene's best assembly in `marker_assembly` (lowest `a_order`) must be named `GRCz12tu`, and the gene must have exactly one `ZFIN` or `ZfinGbrowseStartEndLoader` location on it (the landmark). The image uses `GenomeBrowserBuild.CURRENT`, which is GRCz12tu. The "Genome Browsers" links row uses `MappingService.getJBrowserBrowserLinks`, which only names links for GRCz12tu and GRCz11. |
| 2 | **Gene page, Transcripts** | `/ZDB-GENE-990415-72#transcripts` | `TranscriptService` builds its images with `GRCZ11` fixed, whatever the gene's assemblies. |
| 3 | **Pseudogene page** | `/ZDB-GENEP-041008-30` | Reads the first `ZFIN` or `NCBILoader` location. The image is set before any assembly check and uses `CURRENT`. The name check on the best assembly only guards the links row, and it calls `getName()` with no null check. |
| 4 | **Clone page, Genome Browser** | `/ZDB-BAC-030616-1` | `CloneViewController` fixes `GRCZ11`. All 26,513 clones with locations are GRCz11 only. |
| 5 | **STR page, target location** and the "all target locations" page | `/ZDB-CRISPR-131024-1` | `SequenceTargetingReagentViewController`. The only page that chooses per object: prefers GRCz12tu, falls back to GRCz11 for an STR with no GRCz12tu placement. |
| 6 | **Feature page, Genome Browser** | `/ZDB-ALT-030922-2` | `FeatureService.getGbrowseImage` takes the feature's first location and switches on its assembly: Zv9, GRCz10, GRCz12tu, otherwise GRCz11. |
| 7 | **Mapping detail** | `/action/mapping/detail/ZDB-GENE-990415-72` | `MappingDetailController` takes the first `ZFIN` (GRCz12tu) or `ZfinGbrowseStartEndLoader` (GRCz11) location. GRCz12tu wins, otherwise GRCz11. It does not look at `marker_assembly`. |
| 8 | **BLAST results** (transcript hits) | BLAST result pages | `BlastResultMapper` builds the image with the default build, which is `CURRENT`. |
| 9 | **Search "related data" links** | search result pages | `RelatedDataService` links to `/action/jbrowse/byName?name=<id>`. |
| 10 | **Standalone JBrowse v1** | `/action/jbrowse`, `/action/jbrowse/byName?name=...` | `JBrowseController`. The build is chosen from the location's source. |
| 11 | **Standalone JBrowse2** | `/action/jbrowse2` | `JBrowse2Controller` serves a React app configured with `GRCz11_assembly.json` and `GRCz11_tracks.json`. GRCz11 only, whatever the data. |
| 12 | **Navigation, Genomics, Genome Browsers, ZFIN** | every page | An external JBrowse on AWS Amplify, with `assembly=GRCz12tu` and the tracks `zfin-gene12,refseq12` written into the URL in `pageHeader.tag`. |
| 13 | **Footer link to `/action/jbrowse2`** | every page | Not in this repository (searched templates, tags and configuration). It may be in a CMS page or another repository. If it exists, it is item 11. |
| 14 | `markerGbrowse.tag` | not used | A legacy `Zv8` GBrowse image tag. Nothing references it. |

The embedded images are rendered by React components. `Jbrowse2Image` picks its assembly from `config-jbrowse2.json`, which already lists four assemblies (**GRCz12tu, GRCz11, GRCz10, Zv9**) and 39 tracks that each name the assemblies they belong to. It uses the `data-build` the page gives it, and falls back to the first assembly. GRCz13ab is not in that file, so the front end holds another hard-coded assembly list.

### Corner cases, with IDs to try

| Case | What it shows now | Example | Genes in dev |
|---|---|---|---|
| Gene on GRCz12tu with exactly one landmark | Browser on Sequences. The normal case. | `ZDB-GENE-990415-72`, `ZDB-GENE-000112-47` | 22,362 |
| Gene on GRCz12tu with **two** landmarks | No browser block on Sequences. | `ZDB-GENE-041210-304` | 7 |
| Gene whose best assembly is **GRCz11** (not in GRCz12tu), one GRCz11 landmark | No browser on Sequences. The mapping detail page does show a GRCz11 browser. | `ZDB-GENE-010112-3` | 2,062 |
| Same, with **no** landmark | No browser anywhere on the gene page. | `ZDB-GENE-030131-3108` | 116 |
| Gene with a GRCz12tu location but `marker_assembly` lists only GRCz11 (the stored copy drifted) | No browser on Sequences, although the mapping detail page shows a GRCz12tu one. | `ZDB-GENE-080522-3` (bicc1b) | 1 |
| Gene with GRCz11 locations but **no** `marker_assembly` row at all | "Genome Assembly: Unknown", no browser on Sequences. | `ZDB-GENE-000607-14` | 3,497 |
| Gene with only a **GRCv10** location | Unknown assembly, no browser. | `ZDB-GENE-030131-8765` | 1,515 |
| Gene with no location | Nothing. | `ZDB-GENE-000112-32` | 6,164 |
| Gene with a GRCz12tu **and** a GRCz11 landmark | GRCz12tu in Sequences, GRCz11 in Transcripts. Two assemblies to toggle between. | `ZDB-GENE-000112-47` | 18,971 |
| Pseudogene on GRCz12tu | Image and links. | `ZDB-GENEP-041001-14` | 4 |
| Pseudogene on GRCz12tu and GRCz11 | Image and links. | `ZDB-GENEP-041008-30` | 15 |
| Pseudogene on GRCz11 only | No image, no links. | `ZDB-GENEP-050420-8` | 4 |
| STR on GRCz12tu and GRCz11 | GRCz12tu. | `ZDB-CRISPR-131024-1` | 17,982 CRISPRs |
| STR on GRCz11 only (fallback) | GRCz11. | `ZDB-CRISPR-160128-223` | 19 CRISPRs |
| STR on GRCz12tu only | GRCz12tu. | `ZDB-CRISPR-160128-304` | 15 CRISPRs |
| STR with no location | No browser. | `ZDB-CRISPR-140710-4` | 207 CRISPRs |
| Feature on four assemblies | One image, the highest-ranked assembly. | `ZDB-ALT-030922-2` | 35,591 |
| Feature on Zv9 only | The old Zv9 GBrowse. | `ZDB-ALT-120130-1` | 9,949 |
| Feature on GRCz11 only, with coordinates | GRCz11. Most have only a chromosome, and show "No data available". | `ZDB-ALT-000712-2` (with coordinates), `ZDB-ALT-000215-12` (chromosome only) | 574 with coordinates, 7,139 without |
| Feature on GRCz11 and GRCz12tu | GRCz12tu. | `ZDB-ALT-000412-8` | 5,876 |

### What this means for D4

- **A single "has a browser" flag on `assembly` would not be enough.** Items 2, 4 and 11 deliberately use GRCz11 on pages whose genes are also on GRCz12tu, and the STR page chooses per object. The choice depends on the page type as well as the assembly.
- **The first GRCz13ab row hurts items 1, 3 and 7.** On item 1 the best-assembly name check fails and the browser disappears. On item 7 the build comes from the location's source (`ZFIN` means `CURRENT`), not from its assembly, and the loop reads every assembly's rows. A `ZFIN`-source GRCz13ab row could be drawn on the GRCz12tu genome with GRCz13ab coordinates.
- **Your idea fits the code.** Admins would say, for each page type, which assemblies may be shown and in what order. The page shows the first of those on which the object has a landmark. The front end already carries a configuration with several assemblies, so a page with more than one available could offer a toggle between them (for example a GRCz12tu and a GRCz11 landmark on 18,971 genes).

Sketch of the setting, for discussion:

| Column | Meaning |
|---|---|
| page type | `GENE_SEQUENCE`, `GENE_TRANSCRIPTS`, `PSEUDOGENE`, `CLONE`, `STR_TARGET`, `FEATURE`, `MAPPING_DETAIL`, `BLAST` |
| assembly (foreign key) | Which assembly |
| rank | Order to try them. A page offers the ones the object has a landmark on. |
| enabled | Lets an admin switch GRCz13ab on for one page type at a time. |

What still has to exist outside the table: a JBrowse genome and tracks for the assembly in `config-jbrowse2.json`, a `GenomeBrowserBuild` entry and its property for the legacy paths, a landmark source the loader writes for the assembly, and the link-name rule in `MappingService`. The table decides what to show; those make it possible.

Open questions: which of the eight page types need their own row; whether the toggle replaces the separate Sequences and Transcripts images; and whether the external Amplify browser in the navigation (item 12) and `/action/jbrowse2` (item 11) should follow the same setting.

---

## Loading GRCz13ab

GRCz13ab is the revised release of the GRCz12ab genome (same strain and BioSample, GenBank `.1` to `.2`). NCBI lists it as latest, with a GFF3 posted on 2026-09-24 from annotation release RS_2026_08. None of this has been built.

| Piece | State in dev now |
|---|---|
| `assembly` row | id 2, `a_order` 5, current, `GCF_052040795.2` |
| RefSeq links on the assembly (`db_link_assembly`) | 22,097 genes, from the RefSeq reconciler |
| `Ensembl(GRCz13ab)` database | Exists and is in the summary display group; no links yet |
| `gff3_ncbi` for GRCz13ab | Not loaded. The table has no assembly column. |
| Location rows and `marker_assembly` | None |

### Steps, in dependency order

1. **Make the GFF3 tables multi-assembly.** Add an assembly column to `gff3_ncbi`, move both GFF3 tables to `external_resource`, and replace the `gene_id` cache with the view. Add the `load_file` rows and the dump setting in the same change.
2. **Resolve the file from NCBI, not from code.** Read `assembly_summary.txt`, keep Danio rerio rows marked latest, match the assembly name to `assembly.a_name`, and build the GFF3 URL from its `ftp_path`. Record the version actually loaded.
3. **Parameterize the loader.** Both processors and `markerAssemblyUpdate.sql` take an assembly and run once per current, latest assembly. A current assembly NCBI no longer lists is skipped with a warning.
4. **Dry-run GRCz13ab, and GRCz12tu `.2`.** Report new location counts and drift before writing. GRCz12tu `.2` is the same sequence with a re-annotation, so coordinates should hold but gene sets can move.
5. **Settle the display hazards first.** The browser gate and `getLatestAssembly` both break the moment a gene has a GRCz13ab row (see below). Decide the rule before the first row is written.
6. **Write the rows, then make reports and the reconciler assembly-aware.** `NCBILoader` and `ZFIN` location rows with `sfclg_assembly = 'GRCz13ab'`; coverage and conflict reports name each assembly.
7. **Exports.** `Gff3Writer` output names and the Alliance job and variant exports. Agree with the Alliance side which assembly they expect.
8. **Separate efforts, not blockers.** Ensembl GRCz13ab links, a GRCz13ab genome browser and FASTA, feature coordinates by liftover, and the knockdown-reagent alignments.

**What breaks on the first GRCz13ab row**
- **Browser block hidden.** `getLatestAssembly` returns the lowest `a_order`, so the gene's latest assembly becomes GRCz13ab, and the controllers only show the browser when it equals GRCz12tu.
- **"Genome Assembly" shows GRCz13ab** for any gene that has a row, though no GRCz13ab browser exists yet.
- **Possible fix:** gate the browser on whether the gene is on any assembly that has a browser, stored as data on `assembly`, not by name.

---

## Decisions

| | Decision | Status |
|---|---|---|
| D1 | **How deep to take the status rewrite.** Option C: the per-gene table is gone, and the not-in-release list is stored by NCBI Gene ID. The second writer is removed. | Built: C |
| D2 | **Where load publications live.** A `load_publication` table under a `load` table, seeded with the four publications, including `ZDB-PUB-261014-17`. The load's SQL and Java read it. | Built |
| D3 | **Keep the dumped "not in release" table.** Its rows are dumped. The dump script excludes only the gene info table by name until the `load_file` work replaces that list. | Built |
| D4 | **Browser gating by data.** A column on `assembly` that says it has a browser, replacing the name check, before any GRCz13ab row is written. | Open |
| D5 | **Which assembly the Alliance gets.** The variant and gene exports are fixed on GRCz12tu. Moving them needs agreement outside ZFIN. | Open |
| D6 | **Where the GRCz11 filtering lives.** Plain join in the view; each consumer filters. Decide whether to add a table of location sources with a placement flag, or keep that knowledge in `GenomeLocation.Source`. | Open |
| D7 | **First run against GRCz12tu `.2`.** Dry run with a drift report before the loader is switched from `.1`. | Open |
| D8 | **What to do with GRCv10.** Decided: added to `assembly` as a legacy row (id 8, `a_order` 50, not current, no GCF identifier; migration `0070`). Relabeling the rows is left for someone who knows the Vega load. The display rule for the 1,524 genes with no other assembly is still needed before `marker_assembly` becomes a plain join. | Built; display rule open |
| D9 | **The feature assembly list.** `HibernateLinkageRepository.getFeatureLocations` joins the `assembly` table and orders by `a_order`, and `MappingDetailController` compares assemblies through `AssemblyOrder` (read from the table) for the browser location and for both superseded-location rules. Which assemblies have a browser is still the `GenomeBrowserBuild` enum (D4). | Built |
| D10 | **A first-class `load` table.** `load`, `load_publication` and `load_file` exist, and `load_file_log` points at its file. The older load name, source and table name columns on the log are still written and can be dropped later. `uniprot_release` is not folded in. | Built |

---

How this was checked: counts come from queries against the dev database; code references from searching the repository; NCBI facts from reading its public directory listings and assembly reports. The GFF3 files were not downloaded and the NCBI load was not run. The annotation status work, the load tables and the Genome Resources change are implemented on the branch; nothing else on this page is. Production numbers will differ from the dev counts.
