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
| 8 | Task | The blast loads write ~15 GB of build artifacts into the deployed tree | medium |
| 9 | Task | BLAST query temp files are cleaned only by an external cron job | low |
| 10 | Task | Decide the authoritative source for a blast database fetch | medium |
| 11 | Task | 222 of 231 Jenkins jobs keep every build forever | medium |

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

Confirmed at the filesystem level on 2026-09-02. `/mnt/netapp/zfin/blastdb`
is a different directory on different hosts, not one shared volume:

| seen from | `Current/gbk_zf_mrna.fa` | `dump*.fa` in the parent |
| --- | --- | --- |
| production tomcat / `is-zfin-dkr-p06` | absent | 731 |
| `is-zfin-dkr-p07` (crick) | present, 1.08 GiB, 2024-06-23 | 652 |

The `.x*` database files are byte-identical across both, so the copies
were seeded alike — but each host accumulates its own query temp files
and only production's gets cleaned, so they are visibly drifting apart
now. Any host-specific database rebuild would drift the same way, with
nothing to detect it.

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

## 8. The blast loads write ~15 GB of build artifacts into the deployed tree

**Type** Task · **Priority** medium

`Regenerate-RefSeq-BlastDBs_w` runs with `customWorkspace $TARGETROOT`
and does `cd server_apps/data_transfer/BLAST/RefSeq/ && ./processRefSeq.sh`.
Every script in that directory works in `.`, so the downloaded `.gz`
chunks, the assembled `.fa` files and the formatted `.x*` databases all
land inside the deployed checkout — about 1.5 GB per run, now that all
three chunks are fetched. `pushRefSeq.sh` then copies out of that
directory and leaves everything behind, plus a `refseq.ftp` marker and a
`refseq_process.log`.

`Regenerate-ZFIN-BlastDBs_d` does the same thing at a much larger scale,
and it is not theoretical: a run on cell on 2026-09-02 filled the disk
partway through `zfin_genomicDNA` —

```
/bin/cat: zfin_genomic_dna_all_dna.fa: No space left on device
...
  Failed write at sequence #7
FATAL:  xdf_rec_write failed, xdf_errno 10
```

The blast databases are 2-bit packed (`NCBI2na.1`), so the fasta each
load dumps out of them is roughly four times the database size:

| intermediate | derived from | approx |
| --- | --- | --- |
| `zfin_cdna/zfin_gb_seq.fa` | `.xns` 1.58 GB packed → 6.3 Gbp | ~6.4 GB |
| `zfin_genomicDNA/zfin_genomic_dna_all*.fa` | 465 MB packed, written once per source then concatenated | ~3.8 GB |
| `zfin_cdna/zfin_cdna_seq.fa`, `vega_zfin.fa` | | ~0.2 GB |
| `RefSeq/*.{fna,faa}.gz` and `.fa` | | ~1.5 GB |

Measured on cell after the failed run, `$TARGETROOT/server_apps/data_transfer`:

| | |
| --- | --- |
| `BLAST/ZFIN` | 16.2 GiB |
| `BLAST/SPTrEMBL` | 1.1 GiB |
| `BLAST/RefSeq` | 648 MiB |
| `BLAST/Ensembl` | 357 MiB |
| **`BLAST/` total** | **18.3 GiB** |

Note *where* that is. `$TARGETROOT` is the www home — Jenkins builds in
`/opt/zfin/www_homes/zfin.org` — which on a containerised instance is a
Docker named volume, `cell_www_data`, so the artifacts sit under
`/var/lib/docker/volumes/` and not in the source tree at all. Looking for
them in `/opt/zfin/source_roots/` finds nothing. They are the single
largest thing in that volume (24.3 GiB of 31 GiB) and they count against
the Docker filesystem, which is what actually ran out.

`xdformat` did clean up after itself here — "XDF database removed" — so a
disk-full run does not leave a corrupt database. But it fails late, after
the expensive `xdget` work, and the ~1 GB `Current/gbk_zf_mrna.fa` found
on crick (#9) looks like exactly this kind of artifact left somewhere it
does not belong.

Two smaller things in the same script set:

- `pushRefSeq.sh` opens with
  `mv $BLAST_PATH/Current/refseq_zf_*.x* $BLAST_PATH/Backup` under
  `set -euo pipefail`, so it aborts on a host where `Current` has no
  RefSeq files yet.
- `convertRefSeq.sh` titles the protein database `"ReqSeq Zebrafish
  protein"` — a typo carried into the database header.

**Scope** Give the blast loads a scratch directory outside the deployed
www home, along the lines of the `BLASTSERVER_FASTA_FILE_PATH` the GenBank
scripts use; clean up intermediates on success; make the `Backup` move
tolerant of a first run. Consider a free-space check before the first
`xdget`, so a short disk fails in a second with a number rather than
twenty minutes in with `xdf_errno 10`.

**Acceptance** A blast regeneration leaves no build artifacts in the
source tree, succeeds on a host with an empty `Current`, and says up
front if there is not enough room.

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

**The cleanup job works.** It was worth checking, because the listing that
turned this up showed a 1.08 GiB `Current/gbk_zf_mrna.fa` dated
2024-06-23 that a recursive `find -name "*.fa" -mtime +1` should have
removed long ago. The explanation is not the job:

| `/mnt/netapp/zfin/blastdb` as seen from | `gbk_zf_mrna.fa` | `dump*.fa` |
| --- | --- | --- |
| the production tomcat container (host p06) | absent | 731 |
| `is-zfin-dkr-p06` | absent | 731 |
| `is-zfin-dkr-p07` (crick) | present, 1.08 GiB | 652 |

The job cleans production's copy, where the stale `.fa` is gone. The
listing had been taken on crick, which holds a separate copy with no
equivalent cleanup — see #3.

Note the pattern misses `.fasta`, `.out` and `.ctx` regardless, which is
why the rest of the cruft survives on both: 26 non-database files
totalling 1.18 GiB, including `protein.fasta` and
`sebu1_033116.{ctx,out}` from 2016, an empty extensionless
`vega_transcript`, and `.txt` files from 2009–2015.

**Scope**

- Delete the temp file in a `finally` at the three call sites, so the
  application cleans up after itself. `deleteOnExit()` is the wrong tool
  under a long-running Tomcat — it fires only at JVM shutdown and holds a
  reference to every path until then.
- Write query temp files somewhere that is not the blast database
  directory.
- Sweep the stray non-`.fa` files, on every host that holds a copy.
- If the cron job is kept as a backstop, say so in a comment where the
  temp file is created.

**Acceptance** A BLAST search leaves no file behind without the cron job
running, and `Current/` holds only blast databases.

---

## 10. Decide the authoritative source for a blast database fetch

**Type** Task · **Priority** medium

`getBlastAllDatabases` and `getBlastSmallDatabases` fetched from
`$SSH_HOST:/research/zfin.org/blastdb/Current`. That directory exists on
no host any more — checked 2026-09-02 on `crick.zfin.org`, which is the
`SSH_HOST` in `docker/.env`, and on the production hosts. Both tasks also
set `ignoreExitValue = true`, so rsync failed, nothing transferred, and
the build reported success. That is why nobody noticed: a dev checkout
with no blast databases at all is indistinguishable from one that fetched
cleanly.

ZFIN-10452 fixed the mechanics. The source is now
`BLAST_SOURCE_HOST`/`BLAST_SOURCE_PATH`, defaulting to
`/mnt/netapp/zfin/blastdb/Current`, and a non-zero rsync throws with the
exit code and the source it tried, so a wrong path fails in four seconds
instead of silently. crick has that path, so the default resolves against
the `SSH_HOST` already in `docker/.env`.

What is left is the decision the mechanics cannot make: **which host a
developer should fetch from.** crick works, but crick's copy is one of
several that are drifting apart (#3) — it still holds a 1.08 GiB build
artifact production has cleaned — so "it resolves" is not the same as
"it is authoritative". Three host/path pairs are named in the tree and no
two agree.

| named in | host | path |
| --- | --- | --- |
| `build.gradle` (before this ticket) | `$SSH_HOST` = crick | `/research/zfin.org/blastdb/Current` — gone |
| `build.gradle` (now) | `$SSH_HOST` = crick | `/mnt/netapp/zfin/blastdb/Current` — resolves |
| `docker/setup_blast.sh` | `watson.zfin.org` | `/opt/zfin/blastdb/Current` |
| production containers | is-zfin-dkr-p0*x* | `/mnt/netapp/zfin/blastdb/Current` |

There is also `/research/zblastfiles/zmore/dev_blastdb/Current`, which is
where the complete 2023-06-24 `gbk_*` set was found when cell needed it,
and per-instance directories beside it (`/research/zblastfiles/zmore/cell`
is what cell mounts at `/opt/zfin/blastdb`).

This overlaps #3 — "which host is authoritative for
`/opt/zfin/blastdb/Current`" is the same question asked from the
production side.

**Scope**

- Name one host and path as the fetch source, and make it the default in
  `build.gradle`.
- Point `docker/setup_blast.sh` at the same place, or delete it if the
  gradle tasks supersede it.
- Provision ssh for the fetch, or document that it is a manual step.
- Settle `DOCKER_ABBLAST_PATH`, which has three values in the tree.

### The transport, not just the path

`getBlastBinaries` fetches `$SSH_HOST:/opt/ab-blast`, and that source is
live: crick has it, populated, x86-64 ELF binaries dated 2025-02-21. (They
will not exec in an aarch64 `compile` container on Apple Silicon — only
the `blast` container runs them.) It now goes through the same helper as
the other three, so it fails loudly.

But no fetch can authenticate as things stand. From cell's compile
container:

```
$ echo $SSH_USER
ryanm
$ rsync -avn $SSH_USER@$SSH_HOST:/opt/ab-blast /tmp/abblast-test/
The authenticity of host 'crick.zfin.org (184.171.92.17)' can't be established.
Failed to add the host to the list of known hosts (/home/gradle/.ssh/known_hosts).
ryanm@crick.zfin.org's password:
```

Two problems. The `gradle` user has no key and no writable
`known_hosts`, so ssh falls through to a password prompt — which in a
gradle run means the build hangs or dies at host-key verification. And
`SSH_USER` is `ryanm`, from the committed `docker/environment_linux:2`
template, not the `rtaylor4` in `docker/.env:2`; cell's container was
built from the template.

So these tasks had *two* independent reasons to do nothing, and
`ignoreExitValue = true` concealed both. ZFIN-10452 added
`-e "ssh -o BatchMode=yes"` so the failure is immediate and legible
rather than a hang, but provisioning the key is a decision for whoever
owns the dev images.

### `DOCKER_ABBLAST_PATH` has three values

Even a working fetch may leave the containers reading elsewhere. The
compose files mount `${DOCKER_ABBLAST_PATH}:/opt/ab-blast`, and:

| | value |
| --- | --- |
| `docker/.env:7` | `~/development/blast/ab-blast` |
| `docker/environment_linux:7` | `/opt/ab-blast` |
| where `getBlastBinaries` writes | `/opt/zfin/blastdb/ab-blast` |

Only `docker/setup_blast.sh` uses the third, which it prints as setup
instructions. Same silent-mismatch shape as the blastdb path.

**Acceptance** `gradle getBlast` on a clean checkout populates
`/opt/zfin/blastdb/Current` with working databases, on a documented
source, or names what is wrong.

---

## 11. 222 of 231 Jenkins jobs keep every build forever

**Type** Task · **Priority** medium

Found while working out what had filled cell's disk. `cell_jenkins_data`
is 23.0 GiB, and 22.7 GiB of that is `jobs/`:

| job | build data |
| --- | --- |
| `NCBI-Gene-Load-Java` | 7.4 GiB |
| `Load-GAF-GOA_m` | 5.3 GiB |
| `Check-Foreign-Species-In-Constructs_w` | 2.5 GiB |
| `Check-Get_Object_Type-Function_d` | 2.5 GiB |
| `Load-GPAD-GO-Central_m` | 1.1 GiB |
| `Check-Expressed-Marker-Gene-Or-EFG_w` | 1.0 GiB |

Twenty-four of the 231 job configs in `server_apps/jenkins/jobs/` contain a
`BuildDiscarderProperty` — but counting the element flatters the situation.
The values matter:

| `daysToKeep,numToKeep,artifactDaysToKeep,artifactNumToKeep` | jobs | effect |
| --- | --- | --- |
| `-1,-1,-1,-1` | 16 | none — present in the file, inert |
| `365,-1,-1,-1` | 7 | age cap only, unlimited count, artifacts uncapped |
| `-1,30,-1,-1` | 1 | last 30 builds (`Delete-Old-Blast-Results_d`) |
| `30,10,30,10` | 1 | 30 days / 10 builds, artifacts too (`Run-Priority-Pipeline-Alliance_w`) |

So **9 of 231 have any effect, 2 bound the build count, and exactly 1 caps
archived artifacts** — and `artifactNumToKeep`/`artifactDaysToKeep` are the
only fields that bound disk rather than log volume. None of the six largest
consumers above carries the property at all.

The sixteen inert copies are the part to watch when fixing this: they look
like a policy in a diff and in the Jenkins UI, so a reviewer can reasonably
believe a job is covered when nothing is being discarded.

Alongside the 18.3 GiB of blast build artifacts in `cell_www_data` (#8),
this is most of what a dev instance's Docker filesystem holds — and it is
why `Regenerate-ZFIN-BlastDBs_d` could not finish: the job needs about
15 GiB of scratch and there was no room for it.

`Run-Priority-Pipeline-Alliance_w` is the only job with a policy that
bounds disk, so that is the pattern to copy — not the `365,-1,-1,-1` used by
the seven blast jobs, which never discards an artifact.

**Scope**

- Set a default discard policy across the job configs — the loads that
  archive large artifacts matter most.
- Reclaim what has accumulated on the dev instances.

**Acceptance** A long-lived instance's `jenkins_data` volume stops growing
without bound.

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
