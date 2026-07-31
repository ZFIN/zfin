# ZFIN-10411 testing plan

Covers the 11 commits on `zfin-10411-broader-scope` (PR #1945) plus the CTD work in PR #1943,
which is part of the same ticket and shares one of the risk classes below.

Nearly every change moves where a file is written. The failure mode is not a stack trace — it is
a job that still goes green while its artifact is missing, stale, or in the wrong place. So the
checks below are mostly "look at where the bytes landed", not "did it exit 0".

---

## The one assertion that covers most of the ticket

After **every** job run below, the checkout must be clean:

```bash
cd $SOURCEROOT && git status --porcelain
```

Empty output is the pass condition. None of the leftovers this ticket removes were gitignored, so
any file this ticket missed shows up here. Capture the output *before* the first run too, and
clear any pre-existing untracked junk from earlier runs, or you will be chasing ghosts.

Second global check — the artifact actually reached Jenkins:

```bash
ls $TARGETROOT/<expected path>          # on the build host
```
plus the build's **Artifacts** list in the Jenkins UI, which is what the archiver actually
captured.

---

## Highest-risk change first: `allowEmptyArchive` is now `false`

Four jobs previously archived with `allowEmptyArchive=true`, which is exactly why nobody noticed
they were archiving nothing. They now **fail the build** when the pattern matches no files:

| Job | Pattern now required to match |
|-----|-------------------------------|
| Load-Reference-Proteome | `server_apps/DB_maintenance/Load-Reference-Proteome/*.txt` |
| Load-NCBI-GFF3-File | `gff3_ncbi_report.*` |
| Update-Length-For-Ensembl-Transcript | `ensembl-transcript-load-report.html`, `ensembl-error-report.txt`, `duplicated-ensembl-transcript-name-report.txt`, `report-transcript-*` |
| Load-CTD-Data_m *(PR #1943)* | `Load-CTD-Data_m/*` minus `CTD_chemicals.csv` |

**If a path is wrong, these four jobs go from silently-passing to loudly-failing.** That is the
intent, but it means these four must each be run green before merge. Do not merge on the
assumption that "it only fails if something else is broken."

---

## Per-job plan

### 1. Load-Eco-Ontology_d — ECO→GO mapping
Changes: `getECOGOMapping.groovy` writes into TARGETROOT; zero-mapping bail-out; psql exit check;
`failonerror="true"` on the ant `<exec>`; `on conflict do nothing` in `insert_eco_go_map.sql`.

- Run the job. Expect green.
- `$TARGETROOT/server_apps/data_transfer/eco_go_mapping/` contains fresh `gaf-eco-mapping.txt`,
  `gafeco.txt`, `loadSQLOutput.log`, `loadSQLError.log`.
- `$SOURCEROOT/server_apps/data_transfer/eco_go_mapping/` has **no** `gafeco.txt` /
  `gaf-eco-mapping.txt`.
- `select count(*) from eco_go_mapping;` — record before and after.
- **Re-runnability (the `on conflict` change):** run the job a *second* time back to back. It must
  stay green and the count must not change. Previously a second run aborted on
  `egm_alternate_key_index`.
- **Curated rows survive:** confirm the mappings added by postGmakePostloaddb (DLOAD-672,
  ZFIN-9426) that are *not* in ECO's file are still present after both runs. This is the one
  change that can silently lose curated data if `on conflict` were ever turned into a delete.
- **Failure path:** point `DOWNLOAD_URL` at a 404 in a scratch copy of the script and confirm the
  job now goes red rather than green-with-stale-data.

### 2. Load-Reference-Proteome
Changes: ant `<java>` forks with `dir=$TARGETROOT/.../Load-Reference-Proteome`; `-loadDir` made
absolute; archiver tightened.

- Run the job. Expect green.
- `UP000000437_7955.fasta(.gz)` and `accessions-not-found.txt` land in
  `$TARGETROOT/server_apps/DB_maintenance/Load-Reference-Proteome/`, not `$SOURCEROOT`.
- Jenkins **Artifacts** lists the `.txt`.
- The load still finds `report.properties` — this is what the absolute `-loadDir` protects. A
  wrong path here shows up as a load-time failure, not a missing file.

### 3. Load-NCBI-GFF3-File
Changes: `workingDir` on `loadGff3NcbiFile`/`postLoadGff3NcbiFile`; conditional download;
archiver tightened; failure-email attachment repointed.

- **Run twice.** First run downloads; second must log `Remote ... unchanged, reusing ...` and skip
  the transfer. Second run should be markedly faster.
- `gff3_ncbi_report.html`, `GCF_049306965.1_GRCz12tu_genomic.gff{,.gz}` all under `$TARGETROOT`.
- Nothing new in `$SOURCEROOT` — this job previously dropped the report *and* a 39 MB archive there.
- Success email carries `gff3_ncbi_report.*`.
- **Force a refresh:** `touch -d '2020-01-01' $TARGETROOT/GCF_*.gff.gz` then re-run — it must
  re-download (timestamp older than remote) and then re-gunzip.
- Note `postLoadGff3NcbiFile` has no Jenkins job; run it manually to confirm it shares the same
  directory and does not re-download what the processor just fetched.

### 4. Update-Length-For-Ensembl-Transcript
Changes: `workingDir` on all four Ensembl tasks; `cp` dropped from the job; five reports archived;
both phantom attachments repointed; conditional download; combined-fasta staleness fix.

- **Run twice.** Second run must reuse both fasta downloads *and* skip rebuilding
  `Danio_rerio.GRCz11.all.fa`.
- All six outputs in `$TARGETROOT`: the html summary plus `ensembl-error-report.txt`,
  `duplicated-ensembl-transcript-name-report.txt`, `report-transcript-ensembl.txt`,
  `report-transcript-no-ensembl.txt`, `report-transcript-ensembl-genbank-no-vega`.
  Two of those have **no `.txt` extension** — confirm the `report-transcript-*` glob caught them.
- Jenkins Artifacts lists all of them.
- Success email attaches the html; failure email attaches `ensembl-error-report.txt`. Both
  previously attached a filename nothing writes, so **this is the first time either has carried an
  attachment** — verify by actually receiving one.
- **Combined-fasta staleness:** `touch -d '2020-01-01' $TARGETROOT/Danio_rerio.GRCz11.cdna.all.fa`
  and re-run; `.all.fa` must be rebuilt. This is the bug where a refreshed download would have been
  ignored because the derived file already existed.
- Confirm the transcript length updates still apply (`numberOfNullLengthAdded` /
  `numberOfNonNullLengthAltered` in the console).

### 5. Gene-Association-File-Export_w — GO cleanup csvs
Changes: `workingDir` on `cleanMarkerGoTermEvidenceDuplicatesTask`; console message now names the
directory and the fourth csv.

- Run the job. The four csvs (`clean_marker_go_term_evidence.csv`,
  `to_delete_marker_go_term_evidence.csv`, `tmp_inference_group_member_updates.csv`,
  `tmp_mgte_duplicates.csv`) appear under `$TARGETROOT`, not `$SOURCEROOT`.
- Console prints the directory they landed in.
- The job's real output (`server_apps/data_transfer/GO/*.gz`) is unaffected and still archived —
  builder 2 was not touched.
- The `psql -f` still resolves: its path comes from the SOURCEROOT *property* and is absolute, so
  moving the working directory must not break it. A failure here looks like "SQL file not found".

### 6. Run-Priority-Pipeline-Alliance_w and 7. Journal-Abbreviation-Sync
Change: `cp` → `mv`.

- Run each. Report still archived and still attached to the emails.
- `$SOURCEROOT/priority-pipeline-report.html` and `$SOURCEROOT/JournalAbbreviationSync.sql` are
  **gone** after the run.
- Trivial changes, but worth one green run each because `mv` fails where `cp` would have
  succeeded if the destination is not writable.

### 8. Download-Files_d — mesh-chebi + GFF3 leftovers
Changes: `DownloadFiles.pl` uses `mv` and removes the preprocess intermediate; `generateGff3.sh`
uses `mv`.

⚠️ **This job publishes to the public downloads directory.** Run it on a non-production instance
first, or at minimum diff `$DOWNLOAD_DIRECTORY/current` before and after.

- `mesh-chebi-mapping.tsv` reaches `downloadsStaging/`; neither it nor
  `mesh-chebi-mapping-preprocess.tsv` remains in `$SOURCEROOT`.
- `createMeshChebiMappingFile` still finds `conf/mesh-chebi-header.txt` — it is read relative to
  the working directory, which is why this task deliberately still runs from the checkout. A
  failure here means the header is missing from the final tsv, so **check the tsv has its header
  rows**, not just that it exists.
- `zfin_genes.grcz12.gff3.gz` / `zfin_refseq.grcz12.gff3.gz` reach the Downloads dir and are gone
  from `$SOURCEROOT`.

### 9. Generate-Alliance-Files_m
Changes: `zfin_genes.gff3` added to the pre-run `rm`; staging cleared after submission;
`zfin_wt_expression.json` now published.

⚠️ **Run with `submit=false` and no `ALLIANCE_RELEASE_VERSION` first.** With a release version the
script POSTs to `fms.alliancegenome.org`; with `submit=true` it hits the *submit* endpoint rather
than *validate*. Leaving the version blank skips the upload loop entirely — do that for the first
pass.

- After a green run, `$SOURCEROOT/server_apps/DB_maintenance/Alliance/` contains **only**
  `validateAllianceFiles.sh`. No `ZFIN*.json.gz`, no `zfin_genes.gff3`.
- `$SOURCEROOT` root has no `ZFIN*.json` and no `zfin_wt_expression.json`.
- `$DOWNLOAD_DIRECTORY/current` now contains `zfin_wt_expression.json` **for the first time** —
  this is a deliberate new public file. Confirm with whoever owns the downloads page that its
  appearance is expected.
- **The new `mv` is strict:** it is a plain `mv`, so if `createZfinExpressionInfoFile` ever stops
  producing the file the builder fails. Confirm the file is actually produced before merging, or
  the next monthly run breaks.
- Then do one `submit=false` run *with* a release version to exercise the validate endpoint and
  confirm the staging cleanup still happens after a real upload round-trip.

### 10. Pull-FPBase-Proteins_m
Change: both phantom attachment patterns blanked.

- Run the job; trigger a failure too if cheap. Emails send with no attachment and no email-ext
  error. Nothing else about this job changed.

### 11. Load-CTD-Data_m — PR #1943
- Run green. Everything under `$TARGETROOT/Load-CTD-Data_m/`; report dir spelling fixed; email
  inlines `Load-CTD-Data_m/statistics.html`; `CTD_chemicals.csv` excluded from the archive.
- The CAS fix: confirm `getChebiToCasMapping` no longer NPEs and that `mesh_chebi_mapping` is not
  emptied — the fail-fast guard should abort rather than delete everything if no CAS xrefs are found.

---

## Cross-cutting: the conditional download

Applies to Ensembl (jobs 3, 4) and NCBI GFF3 (job 3).

Already verified against the live endpoints in isolation — HEAD returns `Last-Modified` and
`Content-Length` from both, and `setLastModified` round-trips exactly (0 ms delta), so a fresh
file will not be needlessly re-fetched. What still needs testing in situ:

- **First run after deploy.** Existing cached archives have download-time timestamps, which are
  *newer* than the remote `Last-Modified`, so they pass the timestamp check. They are only
  re-fetched if the size differs. A genuinely stale release will differ in size and be caught —
  but if you want certainty that the loads are on the current release, delete the cached
  `Danio_rerio.GRCz11.*` and `GCF_*.gff*` files once before the first run.
- **Network sad paths.** Confirm the fall-through is safe: if the HEAD fails or returns non-200,
  the code downloads rather than reusing. Simulate by pointing at a bad host in a scratch run.
- **Watch for a re-download on every run.** If run 2 re-downloads, the timestamp round-trip is
  failing on that filesystem — the log line will say which check tripped.

---

## Out of scope for this round

- `reportBody.template` — all 12 failure emails render a broken body. Separate ticket
  (`workbench/reportBody-template-followup-ticket.md`); not caused by, and not fixed by, this branch.
- `generateGff3.sh`'s hardcoded `/opt/zfin/www_homes/zfin.org/...` destination. Identical to
  `$TARGETROOT` today, so untestable as a defect; future-proofing only.
- The "universal fix" for the five copies of the `TARGETROOT ?: projectDir` idiom in
  `console.gradle`. Deferred until #1943 merges.
