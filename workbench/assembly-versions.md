# Assembly versions: where the GCF identifier comes from and how to handle new versions of a file

Written 2026-10-29 while building the "mark assemblies as current" developer page. Nothing here is implemented. Related: decision D7 and the "Loading GRCz13ab" section of [`assembly-blast-radius.md`](assembly-blast-radius.md), and Goal 2 in [`jbrowse-issues/handoff-genome-browser-decision.md`](jbrowse-issues/handoff-genome-browser-decision.md).

## Where `assembly.a_gcf_identifier` comes from

It is a free-text column, set by hand in migrations:

| Migration | Rows |
|---|---|
| `1171/assembly-ncbi.sql` | GRCz12tu `GCF_049306965.1_GRCz12tu`, GRCz11 `GCF_000002035.6_GRCz11`, GRCz10 `GCF_000002035.5_GRCz10`, Zv9 `GCF_000002035.4_Zv9`, and `GRCz11 or older` (none) |
| `1186/migrations/ZFIN-10510-refseq-assembly.sql` | GRCz13ab `GCF_052040795.2_GRCz13ab`, GRCz12ab `GCF_052040795.1_GRCz12ab` |
| `1187/migrations/0070-grcv10-legacy-assembly.sql` | GRCv10 (none) |

The value is NCBI's RefSeq assembly accession (with its version) plus the assembly name.

## Nothing reads it

Only the entity getter (`Assembly.getGcfIdentifier`) and the developer page that lists assemblies use the column. The versions that matter are hard-coded elsewhere:

- **Loaders:** `NCBIGff3Processor` and `NCBIGff3PostProcessor` download `GCF_049306965.1_GRCz12tu_genomic.gff.gz` from a fixed `all_assembly_versions/GCF_049306965.1_GRCz12tu/` URL. `NCBIGff3Processor` also names the unzipped file.
- **Display and files:** `GenomicLocationService.FASTA_GENOMIC_Z12_FILE` names `GCF_049306965.1_GRCz12tu_genomic.fna`. `GenomeLocation.Source` has Map Viewer links with `assm=GCF_049306965.1` and `assm=GCF_000002035.6`.
- **Scripts:** `server_apps/data_transfer/NCBIStartEnd/NCBIStartEnd.pl` fetches the GRCz11 feature table by accession, and `prepare_grcz12tu_genome.sh` names the GRCz12tu FASTA.
- **Possibly stale:** the handoff notes say NCBI now lists GRCz12tu `.2` and GRCz13ab `.2` as latest and GRCz12ab as superseded, while the table and the loader still say GRCz12tu `.1`.

`NCBIGff3Processor.downloadNcbiGff3File` already re-downloads when the server's copy has changed (`downloadFileIfChanged`), because the file name stays the same when an assembly is re-released. It does not record which version it got.

## What a version change can mean

The same accession number with a new version has been used in two different ways:

- **A new name.** `GCF_052040795.1` is GRCz12ab and `GCF_052040795.2` is GRCz13ab. The version change came with a new assembly name, so ZFIN has two `assembly` rows.
- **The same name.** GRCz12tu `.1` and `.2` share a name. The handoff notes say `.2` is the same sequence with a re-annotation, so coordinates should hold but gene sets can move.

An `assembly` row is keyed by name, but the file that is loaded is identified by name and version. One column cannot hold both.

## Suggested strategy

1. **Separate the genome from the file version.** `assembly` stays one row per genome, with a stable name. A separate record, either an `assembly_release` table or a registration in `load_file` (the handoff already proposes registering the GFF3 file there), holds the file version: accession with version, annotation release, FTP path, when it was loaded, and whether it is the one in use.
2. **Let NCBI name the file.** The loader reads `assembly_summary.txt`, keeps the Danio rerio rows marked latest, matches the name to `assembly.a_name`, and builds the URL from `ftp_path`. It records the version it loaded. (This is step 2 of "Loading GRCz13ab" in the blast-radius notes.)
3. **Derive the hard-coded values** (Map Viewer `assm=`, the FASTA name, the loader URL, the scripts) from the loaded release instead of from code.
4. **Rule for a new version:**
   - Same name: a new release of the same `assembly` row. Do a dry run first that reports new locations and drift, then write.
   - New name: a new `assembly` row, as GRCz13ab was.

## Decisions to make

- Keep release history (one row per version), or only the version currently loaded?
- Should `a_gcf_identifier` become the accession without a version (`GCF_049306965`), with the version living in the release record?
- Should the developer assemblies page show the loaded version, and eventually flag that a newer one exists?
- Where the loader's `.1` is already out of date against NCBI's `.2` (see above), who owns the first dry run (decision D7)?
