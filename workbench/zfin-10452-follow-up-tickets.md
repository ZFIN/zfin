# ZFIN-10452 — follow-up tickets

Everything ZFIN-10452 turned up that is out of scope for that ticket.
Background and evidence for all of it is in the sibling document
`zfin-10452-refseq-blast-db.md`. Nothing here has been created in Jira
yet; each entry is written to be pasted more or less as-is into project
`ZFIN`.

Priority is my read, not a decision:

| # | type | summary | priority |
| --- | --- | --- | --- |
| 1 | Bug | `all_refprot_aa` BLAST database has no identifier index | high |
| 2 | Task | Schedule or retire the four cron-less BLAST regeneration jobs | high |
| 3 | Bug | BLAST databases diverge between hosts; no distribution step | high |
| 4 | Bug | `/action/blast/display-sequence` finds no sequence for any accession | medium |
| 5 | Task | Track BLAST database sequence counts run-to-run | medium |
| 6 | Task | `Run-ZFIN-Ensembl-Transcripts-Blast-Data-Dump_d` doesn't rebuild its database | low |
| 7 | Task | Decide what `CuratedNtrRegions` should contain | low |
| 8 | Task | RefSeq load writes ~1.5 GB of build artifacts into the deployed tree | low |
| 9 | Task | BLAST query temp files are cleaned only by an external cron job | low |
| 10 | Bug | The `getBlast*` gradle tasks rsync from a path that no longer exists | medium |

**#1, #2 and #3 are the ones that matter.** #2 and #3 need a decision
before anyone writes code; the rest are self-contained.

---

## 1. `all_refprot_aa` BLAST database has no identifier index

**Type** Bug · **Priority** high

ZFIN-10452 found that `refseq_zf_rna` was built with `xdformat -Tref`
against deflines it could not parse, so no identifier index was written.
`all_refprot_aa` is in the same state:

```
$ /opt/ab-blast/xdget -p /opt/zfin/blastdb/Current/all_refprot_aa NP_571337
FATAL:  Nothing to index!
```

`all_refprot_aa.xpi` is 16,384 bytes — a single empty page — for 46,840
sequences. As with `refseq_zf_rna`, BLAST searches against it still work,
so nothing user-facing looks broken, but anything retrieving a sequence
by accession from it gets nothing and says nothing.

Built by `Regenerate-EnsemblProtein-BlastDBs_w`
(`BLAST/EnsemblP/processEnsembl.sh`), last run 2025-06-26. That job also
has no cron — see #2.

**Scope**

- Find out why `EnsemblP/convertEnsembl.sh` produces no index; expect the
  same defline-vs-`-T` mismatch fixed in `a310f60db7`.
- Check every other physical database the same way. `refseq_zf_aa` and
  `vegaprotein_zf` are the likely candidates — same era, same authors.
  The one-liner is `xdget -{n,p} <db> <any accession>`; `Nothing to
  index!` is the signature.
- Rebuild whatever is affected.

**Acceptance** `xdget` retrieves a known accession from every physical
blast database, and `Validate-Blast-Databases_d` reports no
`no identifier index` findings.

**Note** The rewritten validation job from `b4ba23c73d` already detects
this class automatically, so this ticket is about clearing the existing
backlog, not about detection.

---

## 2. Schedule or retire the four cron-less BLAST regeneration jobs

**Type** Task · **Priority** high · **Needs a decision first**

Four `Regenerate-*-BlastDBs_w` jobs have no entry in
`server_apps/jenkins/trigger.production.properties`. Their `config.xml`
ships an empty `<spec></spec>` and `buildfiles/jenkins.xml` only fills
that in from the trigger file, so despite the `_w` suffix they have never
run on a schedule:

| job | databases | last built |
| --- | --- | --- |
| `Regenerate-GenBank-BlastDBs_w` | the twelve `gbk_*` | **2023-06-24** |
| `Regenerate-SPTrEMBL-BlastDBs_w` | `sptr_zf` `sptr_hs` `sptr_ms` | 2026-05-12 |
| `Regenerate-EnsemblProtein-BlastDBs_w` | `all_refprot_aa` | 2025-06-26 |
| `Regenerate-VegaProtein-BlastDBs_w` | `vegaprotein_zf` | 2025-06-26 |

ZFIN-10452 added the missing cron for `Regenerate-RefSeq-BlastDBs_w` but
deliberately left these alone, because scheduling them is an operational
call rather than a bug fix. GenBank in particular is a large download and
`gbk_est_hs` / `gbk_hs_mrna` are ~9M sequences each; three of the twelve
`gbk_*` databases are human and mouse and may not need refreshing on any
cadence at all.

**The decision:** for each job — schedule it, or retire it and its
databases. A database nobody rebuilds is worse than one nobody has, since
it silently serves 2023 data.

Worth noting that `gbk_zf_mrna` is a live input to `zfin_cdna_seq`, so
the GenBank set is not merely reference data.

**Scope**

- Decide per job.
- For the ones to keep: add a cron to `trigger.production.properties`,
  spread across days, all landing before `Regenerate-ZFIN-BlastDBs_d`
  (`H 0 * * *`) consumes them.
- For the ones to drop: remove the job directory, the `blast_database`
  rows, and the `blastdb_order` parents, so the picker and the validation
  job stop referring to them.
- Set `Validate-Blast-Databases_d.maxAgeDays.<abbrev>` in
  `report_data/report.properties` to match whatever cadence is chosen.

**Acceptance** Every physical blast database either has a scheduled job
that rebuilds it, or is explicitly exempted in `report.properties` with a
comment saying why.

---

## 3. BLAST databases diverge between hosts, with no distribution step

**Type** Bug · **Priority** high · **Needs investigation**

`refseq_zf_rna` is not the same database on every host:

| host | created | sequences | deflines | index |
| --- | --- | --- | --- | --- |
| trunk (= what production serves) | 2025-07-07 | 112,814 | raw NCBI | **none** |
| watson | 2026-02-10 | 87,530 | `>tpe\|ACC\|` | working |

watson's copy postdates both ZFIN-9743 repair commits; production's
predates them. So a fixed build *was* made at some point and never
reached the host that serves queries.

`processRefSeq.sh` has its distribution step commented out, and the
script it names does not exist in the tree:

```sh
#if ($HOSTNAME == genomix.cs.uoregon.edu) then
#    echo "==| distribute refseq to nodes ==|"
#    $TARGET_PATH/RefSeq/distributeToNodesRefSeq.sh
#endif
```

Every other blast load carries the same commented-out block
(`pushzfin_cdna.sh`, `pushzfin_mrph.sh`, `pushzfin_genomicDNA.sh`,
`cpzfin_publishedProtein.sh`, `processzfin_microRNA.sh`,
`GenBank/processGB.sh`, `GenBank/revertGenBank.sh`). Exactly one
`distributeToNodes*` script still exists — `distributeToNodesGenBank.sh` —
and it has one live, uncommented caller in
`GenBank/weeklyGB/weeklyPushGenBank.sh`:

```tcsh
if ($INSTANCE == genomix.cs.uoregon.edu) then
    $TARGET_PATH/GenBank/distributeToNodesGenBank.sh
endif
```

That guard can never be true, and the script re-checks the same dead host
internally, so it is a no-op even if reached.
`revertzfin_publishedProtein.sh` states the position outright: *"comment
out because genomix is no longer with us"*.

So blast database distribution was retired with the machine it ran on, and
nothing replaced it. Whether that matters depends on how many hosts serve
BLAST today — which is the thing to establish first.

**Scope**

- Establish which host is authoritative for `/opt/zfin/blastdb/Current`
  and how a build is meant to reach the others.
- Either restore a distribution step or document that each host builds
  independently — and if the latter, make sure each host actually runs
  the regeneration jobs.
- Delete the `genomix.cs.uoregon.edu` guards and the commented-out
  `distributeToNodes*` blocks either way; they currently read as though
  distribution is handled.

**Acceptance** A documented answer to "I rebuilt a blast database; what
do I do so production serves it?", and the same
`xdformat -i` creation date on every host for a given database.

---

## 4. `/action/blast/display-sequence` finds no sequence for any accession

**Type** Bug · **Priority** medium

While investigating ZFIN-10452 this endpoint returned
`<span class="error">Sequence not found.</span>` for every accession
tried, including ones that certainly exist in a healthy database:

```
/action/blast/display-sequence?accession=NM_131020      -> Sequence not found.
/action/blast/display-sequence?accession=XM_001331747   -> Sequence not found.
/action/blast/display-sequence?accession=XM_073908090   -> Sequence not found.
```

`/action/blast/external-blast?accession=…&refDB=…&blastDB=…` likewise
returned its redirect form with no hidden sequence fields, i.e. an empty
result, for the same accessions.

Because *every* accession failed — not just RefSeq ones — this is
probably not the ZFIN-10452 index bug. Both controllers go through
`MountedWublastBlastService`, which shells out to `xdget` against a
locally mounted `/opt/zfin/blastdb`, so the likely explanation is that
the host serving the webapp has no such mount. That would make it
pre-existing and unrelated, but it is still a user-facing feature
returning nothing.

**Scope** Establish whether these endpoints are still reachable from
anything user-facing (a gene or transcript page's sequence display), then
either fix the retrieval path or retire the controllers.

**Acceptance** Either the endpoint returns a sequence for a known
accession, or it is gone along with whatever links to it.

**Caveat** Diagnosis not completed. Also, submitting the public BLAST
form by `curl` produced "There was a problem with your BLAST request" —
quite possibly a malformed request on my side rather than a real defect,
so treat it as unconfirmed and worth one manual check through a browser.

---

## 5. Track BLAST database sequence counts run-to-run

**Type** Task · **Priority** medium

`b4ba23c73d` added checks for unreadable, empty, index-less and stale
databases. What it still cannot see is a database that rebuilt
*successfully* but much smaller than last time — which is exactly what
the chunk-1-only download in ZFIN-10452 produced for years.

`blast_database` already carries the columns for this and they are all
NULL in the database snapshot I inspected:

- `blastdb_num_seqs`
- `blastdb_old_num_seqs`
- `blastdb_num_accessions`
- `blastdb_last_regenerated`

**Scope**

- Have `Validate-Blast-Databases_d` write the observed count and
  timestamp on each run, rolling the previous value into
  `blastdb_old_num_seqs`.
- Add a finding when a count drops by more than a configurable
  percentage, with the threshold alongside the existing
  `maxAgeDays` keys in `report_data/report.properties`.
- Backfill on first run rather than reporting 42 spurious findings.

**Acceptance** A database that loses a meaningful fraction of its
sequences between runs shows up in the report.

**Note** The staleness check added in `b4ba23c73d` would *not* have
caught ZFIN-10452's chunk bug, because the truncated builds were recent.
Only a count comparison catches that.

---

## 6. `Run-ZFIN-Ensembl-Transcripts-Blast-Data-Dump_d` doesn't rebuild its database

**Type** Task · **Priority** low

The job is scheduled (`00 02 * * 0-5`) and presumably passing, yet
`zfinEnsemblTscript` reports `Date Created 2015-09-10`. The job runs
`BLAST/runZfinEnsemblTscripts.sh`, which dumps data without formatting a
blast database, so the name promises more than it does.

**Scope** Decide whether `zfinEnsemblTscript` is still wanted. If yes,
have something rebuild it and give it a `maxAgeDays`. If no, drop the
database row and rename the job to describe what it actually does.

**Acceptance** No scheduled job whose name implies it maintains a blast
database it does not maintain.

---

## 7. Decide what `CuratedNtrRegions` should contain

**Type** Task · **Priority** low

`CuratedNtrRegions` is public and selectable in the BLAST picker ("Curated
NTR / Regions") and holds **zero sequences**, created 2022-03-23. The
rewritten validation job reports it as `empty`, so it will appear in the
first report email.

**Scope** If it is meant to hold curated NTR regions, find out why it is
empty. If it has been superseded, set `blastdb_public = false` and give it
origination `CURATED_IGNORE`, which the validation job skips by design.

**Acceptance** Either the database has sequences, or it no longer appears
in the picker or the validation report.

---

## 8. RefSeq load writes ~1.5 GB of build artifacts into the deployed tree

**Type** Task · **Priority** low

`Regenerate-RefSeq-BlastDBs_w` runs with `customWorkspace $TARGETROOT`
and does `cd server_apps/data_transfer/BLAST/RefSeq/ && ./processRefSeq.sh`.
Every script in that directory works in `.`, so the downloaded `.gz`
chunks, the assembled `.fa` files and the formatted `.x*` databases all
land inside the deployed checkout — about 1.5 GB per run, now that all
three chunks are fetched. `pushRefSeq.sh` then copies out of that
directory and leaves everything behind, plus a `refseq.ftp` marker and a
`refseq_process.log`.

Two smaller things in the same script set:

- `pushRefSeq.sh` opens with
  `mv $BLAST_PATH/Current/refseq_zf_*.x* $BLAST_PATH/Backup` under
  `set -euo pipefail`, so it aborts on a host where `Current` has no
  RefSeq files yet.
- `convertRefSeq.sh` titles the protein database `"ReqSeq Zebrafish
  protein"` — a typo carried into the database header.

**Scope** Give the RefSeq load a scratch directory outside the deployed
tree, along the lines of the `BLASTSERVER_FASTA_FILE_PATH` the GenBank
scripts use; clean up intermediates on success; make the `Backup` move
tolerant of a first run.

**Acceptance** A RefSeq regeneration leaves no build artifacts in the
source tree, and succeeds on a host with an empty `Current`.

---

## 9. BLAST query temp files are cleaned only by an external cron job

**Type** Task · **Priority** low

`AbstractWublastBlastService.dumpFastaSequence()` writes each query
sequence to a temp file in the blast database directory and returns it:

```java
File tempFile = File.createTempFile("dump", ".fa",
        new File(ZfinPropertiesEnum.WEBHOST_BLAST_DATABASE_PATH.value()));
```

None of the three callers — `SMPWublastService:88`,
`MountedWublastBlastService:88`, `SMPNCBIBlastService:100` — delete it.
Nothing in the application ever does.

What actually cleans up is a separate Jenkins job,
`Delete-Old-Blast-Results_d` (`H 23 * * *`):

```sh
find /opt/zfin/blastdb/ -name "*.fa" -mtime +1 -exec rm -f {} \;
```

So this is **not** an unbounded leak. Production held 720 `dump*.fa` on
2026-09-02, and that is the steady state: `-mtime +1` truncates to whole
days, so the Sep 1 23:00 run removed everything older than Aug 30 23:00,
which is why the oldest survivor was Aug 31 07:43 with only a quiet
Sunday night before it. Roughly 350 a weekday, two days retained.

The problem is the coupling, not the volume. A servlet writes files it
never cleans up, into the directory the blast databases live in, and
correctness depends on an unrelated cron job that nothing in the Java
code mentions. If that job is ever disabled — and four BLAST
regeneration jobs in this same area already have no cron at all, see #2 —
the directory grows until the volume fills, with nothing to point at.

**A loose end in the cleanup job.** The dump files sit directly in
`/opt/zfin/blastdb/`, which is what `WEBHOST_BLAST_DATABASE_PATH` points
at, and that is where the `find` cleans them — the two-day steady state
above is that job working. But the `find` is recursive and matches
`*.fa`, so it should also reach `Current/gbk_zf_mrna.fa` (1.08 GiB,
2024-06-23), and that file is still there.

The most recent build log rules out the obvious causes: the job runs from
`/opt/zfin/www_homes/zfin.org`, both `find`s execute, `-name "*.fa"`
reaches `find` unexpanded, and it exits 0 under `/bin/sh -xe`, so there
were no permission errors during traversal either. Something stops it at
the top level instead — a `Current` that is a symlink would do it, since
`find` without `-L` will not descend into one. One command in the same
context the job runs, without the `-exec`, settles it:

```sh
find /opt/zfin/blastdb/ -name "*.fa" -mtime +1
ls -ld /opt/zfin/blastdb /opt/zfin/blastdb/Current
```

Note also that the pattern misses `.fasta`, `.out` and `.ctx`, which is
why the rest of the cruft in `Current/` would survive regardless: 26
non-database files totalling 1.18 GiB, including `protein.fasta` and
`sebu1_033116.{ctx,out}` from 2016, an empty extensionless
`vega_transcript`, and `.txt` files from 2009–2015.

**Scope**

- Delete the temp file in a `finally` at the three call sites, so the
  application cleans up after itself. `deleteOnExit()` is the wrong tool
  under a long-running Tomcat — it fires only at JVM shutdown and holds a
  reference to every path until then.
- Write query temp files somewhere that is not the blast database
  directory.
- Find out why `Delete-Old-Blast-Results_d` does not descend into
  `Current/`, and sweep the stray files there while in the area.
- If the cron job is kept as a backstop, say so in a comment where the
  temp file is created.

**Acceptance** A BLAST search leaves no file behind without the cron job
running, and `Current/` holds only blast databases.

---

## 10. The `getBlast*` gradle tasks rsync from a path that no longer exists

**Type** Bug · **Priority** medium · **Needs investigation**

`getBlastAllDatabases`, `getBlastSmallDatabases` and the new
`getBlastZfinLoadDatabases` all fetch from
`$SSH_HOST:/research/zfin.org/blastdb/Current`, with
`SSH_HOST=crick.zfin.org` (`docker/.env:3`). Production's blast directory
is `/mnt/netapp/zfin/blastdb`, and on the production host
`/research/zfin.org/blastdb/Current` does not exist. A third path is in
play too: `docker/setup_blast.sh` rsyncs from
`watson.zfin.org:/opt/zfin/blastdb/Current`.

Every one of those tasks sets `ignoreExitValue = true`, so a source path
that does not resolve produces no error and no files — indistinguishable
from a successful no-op.

Whether the gradle tasks are actually broken depends on what
`crick.zfin.org` exposes, which has not been checked. `/research/...` and
`/mnt/netapp/...` may be two mounts of the same NetApp volume.

**Scope**

- Establish the one path that is authoritative for a blast database fetch
  and use it in all three gradle tasks and `docker/setup_blast.sh`.
- Drop `ignoreExitValue = true`, or at least fail when nothing transfers,
  so a bad path is visible.

**Acceptance** `gradle getBlast` on a clean checkout either populates
`/opt/zfin/blastdb/Current` or fails loudly.

---

## Deliberately not filed

- **The 2016-era Vega databases and `repbase_zf`.** Upstream is gone;
  they are in the validation exemption list with a comment. Retiring them
  is tidying, not a fix.
- **The `getGenbankXpatCdnaDBLinks()` 30-second query.** Noted in a code
  comment as known and unrelated to this ticket.
- **`docker/setup_blast.sh` printing the wrong `DOCKER_BLASTSERVER_BLAST_DATABASE_PATH`.**
  Real (it appends a spurious path segment its own rsync does not create)
  but a two-line fix; fold it into whichever ticket touches the script
  next rather than filing it.
