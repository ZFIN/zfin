# Read the Ensembl and NCBI downloads straight from their .gz instead of materialising a copy

**Type:** Improvement
**Found via:** ZFIN-10411 (Jobs write output to $SOURCEROOT instead of $TARGETROOT)
**Affects:** `EnsemblTranscriptBase`, `NCBIGff3Processor`, `NCBIGff3PostProcessor`, `Gff3Reader`

## Summary

Three loads download a `.gz`, gunzip it to a second file on disk, and then read the decompressed
copy. `FileUtil.gunzipFile` keeps the archive, so both files persist. The archive has to stay —
ZFIN-10411 made it the cache key for the freshness check — but the decompressed copy does not need
to exist at all. Both readers can consume the gzip stream directly.

Doing so removes about **470 MB of derived files** from every job workspace and deletes a whole
class of staleness bug: a materialised decompressed file can drift out of sync with the archive it
came from, which is why ZFIN-10411 had to add timestamp-keying to keep them aligned.

## Current shape

```java
// EnsemblTranscriptBase.downloadFile / NCBIGff3*.downloadNcbiGff3File
downloadFileIfChanged(zippedFile, url);   // Danio_rerio.GRCz11.cdna.all.fa.gz
FileUtil.gunzipFile(zippedFileName);      // ...and now also .fa, ~4.35x larger
```

`FileUtil.gunzipFile` never deletes its input (the only `forceDelete` in that class is in
`gzipFile`, the opposite direction), so each download costs archive + decompressed copy.

## What it costs today

Compression ratio measured on the real Ensembl ncrna dump: **4.35x** (1,619,865 -> 7,050,238 bytes).
Applying that to the current archives:

| File | Archive | Decompressed copy |
|------|---------|-------------------|
| `Danio_rerio.GRCz11.cdna.all.fa` | 32.9 MB | ~143 MB |
| `Danio_rerio.GRCz11.ncrna.fa` | 1.6 MB | ~7 MB |
| `Danio_rerio.GRCz11.all.fa` (concatenation of the two above) | — | ~150 MB |
| `GCF_049306965.1_GRCz12tu_genomic.gff` | 39.0 MB | ~170 MB |
| | **73.5 MB kept** | **~470 MB removable** |

## Why it is worth doing beyond disk

ZFIN-10411 had to add logic purely to keep the decompressed copies honest:

- gunzip only when the decompressed file's timestamp differs from its archive's, then copy the
  archive's timestamp onto it — a "was this produced from *this* archive" marker;
- `getCombinedFastaFile` had the same bug one level up, since `.all.fa` is derived from the two
  downloads and its mere existence said nothing about which release it was built from.

Streaming makes the decompressed file stop existing, so none of that bookkeeping is needed. Delete
it along with the change.

## Feasibility — both sides checked

**NCBI: verified working.** `Gff3Reader` calls
`AbstractFeatureReader.getFeatureReader(filePath, new Gff3Codec(), false)`. Tested against the
project's own htsjdk 4.3.0 jar with a small GFF3, plain and gzipped:

```
  sample.gff3    -> OK, iterated 4 features
  sample.gff3.gz -> OK, iterated 4 features
```

Same call, no code change in `Gff3Reader` at all — pass it the `.gz` path and drop the
`FileUtil.gunzipFile` line. (This was the main unknown: Tribble is historically fussy about plain
gzip vs bgzip, but that only affects indexed `query()`, and these loads use full iteration via
`getStream()`.)

**Ensembl: one line.** `EnsemblTranscriptBase.getFastaIterator` is

```java
FileReader fileReader = new FileReader(fileName);
BufferedReader br = new BufferedReader(fileReader);
... RichSequence.IOTools.readFasta(br, ...)
```

Swap the reader for `new InputStreamReader(new GZIPInputStream(new FileInputStream(gzName)))`.
Everything downstream takes a `BufferedReader`.

## Scope

1. `EnsemblTranscriptBase.getFastaIterator` — read from the `.gz`.
2. `EnsemblTranscriptBase.downloadFile` — drop the gunzip step and the timestamp-keying around it.
3. `EnsemblTranscriptBase.getCombinedFastaFile` — either stream both inputs when building
   `.all.fa`, or drop the file entirely (see below).
4. `NCBIGff3Processor` / `NCBIGff3PostProcessor.downloadNcbiGff3File` — drop the gunzip step; pass
   the `.gz` path to `Gff3Reader`.
5. Keep `downloadFileIfChanged` exactly as is — the archive is still the cache key.

## Things to be careful about

- **`.all.fa` may be dead weight.** `getCombinedFastaFile` is called only by
  `EnsemblTranscriptFastaReader` (gradle task `createEnsemblTranscriptFastaFile`), which **no
  Jenkins job runs**. It is the single largest derived file (~150 MB) and duplicates data already
  in the two archives. Worth asking whether it should exist at all rather than porting it.
- **The `.test` override still has to work.** `NCBIGff3Processor.start()` prefers
  `GCF_049306965.1_GRCz12tu_genomic.gff.test`, an uncompressed file, when present. htsjdk picks the
  right decompression by extension, so both paths work through one call — but keep a test for it.
- **Decompression moves from once-per-download to once-per-read.**
  `EnsemblTranscriptUpdateLengthTask.main` calls `init()` twice (once for itself, once for
  `EnsemblTranscriptFastaReadProcess`), so each fasta is read twice per run. That is 2
  decompressions instead of 1 gunzip + 2 plain reads — roughly a second on 33 MB, but it is a real
  shift in where the cost sits. If a load ever grows to many passes, cache the parsed records
  rather than reinstating the decompressed file.
- **Different regression surface from ZFIN-10411.** That ticket moved *where files are written*;
  this changes *how they are parsed*. Verify by record count against a known-good run
  (transcript counts, feature counts), not by checking file locations.

## Related

- **ZFIN-10411** — added `downloadFileIfChanged` and the timestamp-keying this ticket removes.
  Do this after that merges; the two touch the same methods.
- `workbench/ZFIN-10411-testing-plan.md` has the baseline record counts worth diffing against.
