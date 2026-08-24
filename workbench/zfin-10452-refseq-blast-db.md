# ZFIN-10452 — RefSeq missing from the ZFIN cDNA BLAST database

Investigation notes for ZFIN-10452, "Possible regression? RefSeq
associated with a ZFIN gene is not in the ZFIN cDNA sequences BLAST
database". Fixed in `f87cea527e` (the load) and `b4ba23c73d` (the
validation job). Follow-up work is written up in the sibling document
`zfin-10452-follow-up-tickets.md`.

**Short version.** Not a regression in the ZFIN-9743 SQL — that works.
`refseq_zf_rna` on production was built on 2025-07-07 with `xdformat -I
-Tref`, which wrote **no identifier index**. BLAST *searches* against it
still work, but `xdget` cannot retrieve a single sequence, so the nightly
`zfin_cdna_seq` rebuild silently contributed zero RefSeqs for thirteen
months. The two commits that fix the formatting were both written *after*
that build and never reached production, because
`Regenerate-RefSeq-BlastDBs_w` has no cron entry and has only ever been
run by hand.

---

## 1. The report, and what turned out to be true

The curator observed that `XM_073908090` (tmc3):

- is **not** returned when blasting against "ZFIN cDNA Sequences"
- **is** returned when blasting against "GenBank Zebrafish RNA"

That asymmetry is the whole diagnosis in miniature. "GenBank Zebrafish
RNA" (`GenBankZebrafishRNA`) is a parent node whose children include
`refseq_zf_rna` — non-public, so it never appears in the database picker,
but it is searched when the parent is selected:

| parent | child | order |
| --- | --- | --- |
| `GenBankZebrafishRNA` | `gbk_est_zf` | 120 |
| `GenBankZebrafishRNA` | `gbk_zf_mrna` | 130 |
| `GenBankZebrafishRNA` | `gbk_zf_rna` | 140 |
| `GenBankZebrafishRNA` | **`refseq_zf_rna`** | 150 |

A BLAST *search* reads the sequence and defline files. Retrieval by
accession (`xdget`) needs the identifier index. One was intact and the
other was not, which is exactly what the curator saw.

## 2. The ZFIN-9743 query is fine

`getGenbankCdnaDBLinks()` in `HibernateSequenceRepository` does include
RefSeq RNA dblinks hanging off genes, as ZFIN-9743 intended. Verified
against the live production list rather than a local snapshot:

```
curl 'https://zfin.org/action/blast/blast-files?action=GENBANK_CDNA'
```

- 209,362 lines, **184,468 unique** accessions
- 92,996 RefSeq-style (`XM_`/`XR_`/`NM_`/`NR_`), 91,472 other
- `XM_073908090` **is present**

The dblink itself is `ZDB-DBLINK-260612-226087` — RefSeq / RNA, fdbcont
`ZDB-FDBCONT-040412-38`, attached to gene `ZDB-GENE-141216-304`. That
fdbcont's primary blast database is `refseq_zf_rna`
(`ZDB-BLASTDB-071128-19`).

## 3. How `zfin_cdna_seq` is assembled

`ZFIN/zfin_cdna/assemblezfin_cdna.sh` builds the fasta by pulling
sequences *out of other local blast databases* with `xdget`, keyed on the
accession list from §2:

```
xdget -n -f ... -o new_zfin_gb_seq_mrna.fa  $BLAST_PATH/Current/gbk_zf_mrna    zfin_genbank_cdna_acc.unl
xdget -n -f ... -o new_zfin_refseq_rna.fa   $BLAST_PATH/Current/refseq_zf_rna  zfin_genbank_cdna_acc.unl
```

`convertzfin_cdna.sh` then appends a dump of `vega_zfin` and formats the
lot as `zfin_cdna_seq`. So a RefSeq reaches "ZFIN cDNA Sequences" only if
`xdget` can retrieve it from `refseq_zf_rna`.

The nightly job itself is healthy — `zfin_cdna_seq` reported
`Date Created 2026/08/21` when this was investigated. The input was the
problem.

### The sequence counts already said RefSeq contributed nothing

From `/action/api/blast/info/<db>` on production, on 2026-08-21:

| | sequences |
| --- | --- |
| `zfin_cdna_seq` | 133,094 |
| less `vega_zfin` (dumped in whole) | −40,278 |
| leaves, for GenBank + RefSeq combined | **92,816** |
| unique *non*-RefSeq accessions in the list | 91,472 |

`gbk_zf_mrna` alone accounts for essentially all of it. The RefSeq
contribution is inside the rounding error — not "reduced", **zero**.

## 4. Root cause: no identifier index

Confirmed directly on trunk:

```
$ ls -al /opt/zfin/blastdb/Current/refseq_zf_rna*
-rw-rw-r-- 1 gradle zfishweb  11947802 Jul  7  2025 refseq_zf_rna.xnd
-rw-rw-r-- 1 gradle zfishweb     16384 Jul  7  2025 refseq_zf_rna.xni
-rw-rw-r-- 1 gradle zfishweb 105024553 Jul  7  2025 refseq_zf_rna.xns
-rw-rw-r-- 1 gradle zfishweb    903272 Jul  7  2025 refseq_zf_rna.xnt

$ xdget -n /opt/zfin/blastdb/Current/refseq_zf_rna XM_073908090
FATAL:  Nothing to index!
```

`xdget` **refused** rather than reporting "not found" — it never looked.
So the failure is not "this accession is missing"; it is "no accession
can be retrieved from this database, ever."

The file sizes agree. `.xnt` is 903,272 bytes = 112,814 sequences × 8, so
the offset table is complete; `.xns` and `.xnd` are full size. Only
`.xni`, the identifier index, is a single 16 KB page. For comparison, a
correctly built database of 268,576 sequences produces a **14.7 MB**
`.xni` (§9).

### Why: the `-Tref` timeline

The live database predates its own fix.

| date | event |
| --- | --- |
| 2025-05-12 | NCBI creates `XM_073908090.1` |
| 2025-06-30 | `594c111e92` — last change before the build |
| **2025-07-07** | **`refseq_zf_rna` built** — raw NCBI deflines, `xdformat -I -Tref` |
| 2025-08-25 | `a310f60db7` — *"remove -Tref … as it produces no index as the fasta file has changed"* |
| 2025-09-03 | `ddd964b86a` — adds the `tpe\|` prefix and version stripping |

`-Tref` tells `xdformat` to parse `ref|accession|locus` deflines. The
deflines were raw NCBI (`>XM_073908090.1 PREDICTED: …`), so nothing
parsed and no index was written. ZFIN's own commit message says as much.
Both repair commits land *after* the build, so neither has ever been
applied to a live database.

## 5. Why the fix never shipped: the job has no cron

`server_apps/jenkins/trigger.production.properties` has **no entry** for
`Regenerate-RefSeq-BlastDBs_w`. The job's `config.xml` ships an empty
`<spec></spec>`, and `buildfiles/jenkins.xml`'s `addTriggerPerJob` only
fills that in from the trigger properties file:

```
<replaceregexp match="<spec></spec>" replace="<spec>${schedule}</spec>" ... />
```

No property, no schedule. Despite the `_w` suffix the job is manual-only,
and had not run since 2025-07-07. The reporter's hunch about a Jenkins
job was right in spirit — the job is not *failing*, it was never wired up.

Only two blast jobs are actually scheduled: `Regenerate-Ensembl-BlastDBs_w`
and `Regenerate-ZFIN-BlastDBs_d`. See §10.

## 6. Second, independent bug: only chunk 1 was downloaded

`downloadRefSeq.sh` fetched exactly `zebrafish.1.protein.faa.gz` and
`zebrafish.1.rna.fna.gz`. NCBI now publishes **three** chunks of each:

| chunk | RNA records | protein records |
| --- | --- | --- |
| 1 | 58,266 | 43,262 |
| 2 | **88,910** | 52,796 |
| 3 | 121,400 | 73,623 |
| **total** | **268,576** | **169,681** |

`XM_073908090.1` is in **chunk 2**. So fixing the index alone would not
have closed the ticket.

This was confirmed independently, because watson turned out to hold a
*newer* build than production — one whose index works:

| | trunk (= production) | watson |
| --- | --- | --- |
| created | 2025-07-07 | **2026-02-10** |
| sequences | 112,814 | 87,530 |
| deflines | raw NCBI | `>tpe\|ACC\|` |
| index | none | working |
| `xdget XM_073905898` (chunk 1) | `Nothing to index!` | **retrieved** |
| `xdget XM_073908090` (chunk 2) | `Nothing to index!` | **`Not found`** |

watson's build was made after both repair commits, so it demonstrates the
chunk bug in isolation: healthy index, chunk-1 accession retrievable,
chunk-2 accession absent. `NM_131020` (hbba1) is missing from it too.

### What the truncation costs ZFIN

Of the 92,996 RefSeq cDNA accessions ZFIN wants, 72,805 exist in NCBI's
current files:

| build | servable | |
| --- | --- | --- |
| watson, Feb 2026 | 60,975 | 84% |
| unfixed script run *today* | 36,482 | 50% |
| fixed script | 72,805 | 100% |

The loss drifts with wherever NCBI last drew the chunk boundary, which is
why the fix probes upwards instead of hard-coding a count. Note the
often-quoted "78% of RefSeq dropped" describes the whole RefSeq file
(58,266 of 268,576 today); the ZFIN-relevant loss is the table above.

## 7. Third bug: version stripping missed multi-digit versions

`ddd964b86a` rewrote deflines with:

```
sed -i -e 's/>/>tpe|/g' -e 's/\.[0-9] Danio/| Danio/g' -e 's/\.[0-9] PREDICTED/| PREDICTED/g'
```

`\.[0-9]` requires a space after a single digit, so `XM_685006.11` and
`XM_001345945.10` never matched — they keep the version in the indexed
id, while ZFIN stores accessions unversioned, so `xdget` could not find
them. **966** of the 268,576 RNA deflines carry versions of two digits or
more.

## 8. Why nothing caught it

Three separate safety nets were absent or blind.

1. **`assemblezfin_cdna.sh` has no `set -e`** and never checked its
   output. `xdget` exits non-zero on `FATAL`, and the script walked
   straight past it every night for thirteen months.
2. **`downloadzfin_cdna.sh` guarded with `[ -f ]`, not `[ -s ]`.**
   `wget -O` creates the file even when the request fails, so an empty
   accession list would have passed too. Its log line also counted
   `zfin_genomic_genbank_acc.unl`, a file it never downloads.
3. **`Validate-Blast-Databases_d` ran daily and passed throughout.**
   `validateAllPhysicalDatabasesReadable()` only failed when
   `numSequences < 0`. A database with zero sequences produced a warning
   — and, via a copy/paste slip, logged `failures.get(failures.size()-1)`,
   an unrelated earlier failure rather than the database in hand. Age and
   retrievability were never examined at all.

## 9. What changed, and how it was verified

`f87cea527e` — the load:

- `downloadRefSeq.sh` downloads every chunk, probing upwards so a future
  fourth chunk is picked up, and rewrites deflines with one anchored rule
  that handles any version length. Applied to the protein fasta too,
  which previously got neither a closing pipe nor version stripping.
- `trigger.production.properties` gains
  `Regenerate-RefSeq-BlastDBs_w=30 09 * * 2` — Tuesday, off the Ensembl
  rebuild's day, well ahead of the nightly job that consumes it.
- `assemblezfin_cdna.sh` errors out if any `xdget` yields an empty fasta.
- `downloadzfin_cdna.sh` guards with `-s` and logs the right file.

`b4ba23c73d` — the validation job; see §10 and the follow-up document.

### End-to-end proof

Download in the `compile` container, format and retrieve in `blast`
(which is the only service with `platform: linux/amd64`, because the
ab-blast binaries are x86_64):

```
Downloaded 3 chunk(s) of rna.fna      (268576 sequences)
Downloaded 3 chunk(s) of protein.faa  (169681 sequences)
Index entries written (in database):  268,576  (268,576)
refseq_zf_rna.xni                     14,696,448 bytes

$ xdget -n refseq_zf_rna XM_073908090
>tpe|XM_073908090| PREDICTED: Danio rerio transmembrane channel like 3 (tmc3), transcript variant X11, mRNA
GCCGTTCTGATGAGAATCCTGTAGTAATCCGTTCCAGTGTTTCGTCTGTCCTCGTGAAAA…
```

`XM_685006` (version `.11`) and `XM_001345945` (`.10`) retrieve too, so
§7 is closed as well. Also verified: the rewritten sed against all
268,576 real deflines produces well-formed `>tpe|ACC|` with no residual
versions, and 8 new unit tests cover the staleness-policy parsing.

### Running the blast tools locally

`compile` is aarch64 on Apple Silicon, so the x86_64 binaries will not
exec there. Use `z run blast` with **full paths** — the bare fedora image
sets no `PATH`:

```
z run blast -c "/opt/ab-blast/xdget -n /opt/zfin/blastdb/Current/refseq_zf_rna XM_073908090"
z run blast -c "/opt/ab-blast/xdformat -n -i /opt/zfin/blastdb/Current/refseq_zf_rna"
```

`docker/.env` needs `DOCKER_BLASTSERVER_BLAST_DATABASE_PATH` pointing at
the directory that *contains* `Current/`. Note `docker/setup_blast.sh`
prints `${BLASTDIR}/blast` for that variable, which appends a spurious
path segment — its own rsync puts `Current` at `$BLASTDIR/Current`.

## 10. BLAST database inventory

All 42 physical (non-external) databases, freshness from
`/action/api/blast/info/<db>` on 2026-08-24.

### Frozen because the job has no cron

| databases | last built | job | cron |
| --- | --- | --- | --- |
| `gbk_est_zf` `gbk_zf_mrna` `gbk_zf_rna` `gbk_zf_dna` `gbk_gss_zf` `gbk_htg_zf` `gbk_est_hs` `gbk_est_ms` `gbk_hs_dna` `gbk_hs_mrna` `gbk_ms_dna` `gbk_ms_mrna` | 2023-06-24 | `Regenerate-GenBank-BlastDBs_w` | ✗ |
| `refseq_zf_rna` `refseq_zf_aa` | 2025-07-07 | `Regenerate-RefSeq-BlastDBs_w` | ✗ → added |
| `all_refprot_aa` | 2025-06-26 | `Regenerate-EnsemblProtein-BlastDBs_w` | ✗ |
| `vegaprotein_zf` | 2025-06-26 | `Regenerate-VegaProtein-BlastDBs_w` | ✗ |
| `sptr_zf` `sptr_hs` `sptr_ms` | 2026-05-12 | `Regenerate-SPTrEMBL-BlastDBs_w` | ✗ |

### Current

`ensembl_zf` `ensembl_zf_only` (2026-08-20, weekly) ·  `zfin_cdna_seq`
`zfin_crispr` `zfin_mrph` `zfin_talen` `GenomicDNA`
`ZFINGenesWithExpression` (2026-08-24, nightly)

### Frozen because upstream is retired — not a problem

`repbase_zf` 2006-10-27 (RepBase went subscription-only) · `vega_zfin`
2016-01-12, `vega_transcript` 2016-09-19, `vega_withdrawn` 2016-03-22
(Vega retired by Ensembl) · `zfinEnsemblTscript` 2015-09-10 ·
`LoadedMicroRNAMature` / `LoadedMicroRNAStemLoop` 2009-09-29,
`LoadedFishMicroRNAStemLoop` 2022-03-23 (miRBase / FishmiRNA snapshots)

These are the staleness exemption list in `report.properties`. The seven
CURATED databases (`publishedRNA`, `publishedProtein`, `unreleasedRNA`,
`wz_est`, `CuratedNtrRegions`, `CuratedMicroRNA*`) are skipped by
origination rather than listed — they change only when a curator edits
them, so age says nothing.

### What the rewritten validation job will report

Simulated against the state above: **20 findings, 22 databases clean.**
The twelve `gbk_*`, both `refseq_*`, `sptr_*` ×3, `all_refprot_aa`,
`vegaprotein_zf`, and `CuratedNtrRegions` (zero sequences since
2022-03-23). No noise — every one is real. Worth a decision on the
GenBank set before the first email lands.

## 11. Loose ends

- **`all_refprot_aa` has the same no-index defect.** `xdget -p … NP_571337`
  → `FATAL: Nothing to index!`; `.xpi` is 16,384 bytes for 46,840
  sequences. Confirmed on watson. Covered by a follow-up ticket.
- **Production and watson diverge** (2025-07-07 vs 2026-02-10).
  `processRefSeq.sh` has its `distributeToNodesRefSeq.sh` call commented
  out and no such script exists in the tree, so how builds are meant to
  propagate is unclear. Follow-up ticket.
- **`/action/blast/display-sequence` returned "Sequence not found." for
  every accession tried**, including `NM_131020`, so it is not specific
  to this bug. Cause not established. Follow-up ticket.
- **Not verified:** that `XM_073908090` is physically present in
  production's `refseq_zf_rna`. `xdget` refuses before looking, so this
  rests on the curator's BLAST hit. It does not affect the fix — the
  database is being rebuilt from scratch either way.
- A working test database is left at `~/development/blastdb/refseq-test`
  (1.5 GB, safe to delete).
