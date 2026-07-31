# Failure emails render a report template whose inputs nothing writes

**Type:** Bug
**Found via:** ZFIN-10411 (Jobs write output to $SOURCEROOT instead of $TARGETROOT)
**Affects:** 12 Jenkins jobs, all failure-notification emails

## Summary

`server_apps/jenkins/email-templates/reportBody.template` is used as the failure-email
body by 12 Jenkins jobs. The template reads three fixed files out of
`$TARGETROOT/server_apps/DB_maintenance/`, but **nothing in the codebase writes any of
them**, and **none of the 12 jobs produces them**. Every one of these jobs therefore
sends a failure email whose body is a template error rather than a report — and because
the body only renders when a job has *already failed*, the breakage is invisible during
normal operation.

## The template and its inputs

`reportBody.template` is a Groovy-scripted email body (`${SCRIPT, template="reportBody.template"}`)
that inlines three files:

| Line | Path | Purpose in template |
|------|------|---------------------|
| ~30 | `$TARGETROOT/server_apps/DB_maintenance/description.txt` | `print new File(description).text` — free-text preamble |
| ~45 | `$TARGETROOT/server_apps/DB_maintenance/column.header` | pipe-delimited `<th>` row |
| ~57 | `$TARGETROOT/server_apps/DB_maintenance/reportRecords.txt` | pipe-delimited `<td>` rows via `f.eachLine` |

The shape is a generic data-validation report — the kind the `Check-*` validation jobs
produce. It is not specific to any of the jobs currently using it.

## Evidence

1. **No writer exists.** An exhaustive repo grep for `reportRecords`, `column.header`, and
   `description.txt` returns only the template's own three references (plus unrelated
   `//print column headers` comments in `GeneViewController.java` and
   `AnnualStatsController.java`). No `.java`, `.sql`, `.sh`, `.pl`, `.groovy`, or `.xml`
   file writes them.
2. **Never tracked or deleted.** `git log --all --diff-filter=D` finds no history of these
   files ever having been in the repo, so this is not a regression from a removal.
3. **Not present on disk.** `$TARGETROOT/server_apps/DB_maintenance/` does not exist at all —
   not the three files, not the containing directory. `TARGETROOT` is set to a single literal
   (`/opt/zfin/www_homes/zfin.org`) and does not differ per instance in practice, so this is
   not a dev-host-only observation.
4. **No job produces them.** Of the 12 jobs, only `Update-Orthology_w` references
   `server_apps/DB_maintenance` at all, and it points at a different path
   (`report_data/Update-Orthology_w/Update-Orthology_w.html`), not the template's inputs.

## Affected jobs

All 12 use the template in the **FailureTrigger** body only (no SuccessTrigger uses it):

- Check-And-Update-Journals_w
- Check-NCBI-Links-In-Without-ZFIN-Links-Out
- Fetch-Pubs-From-Pubmed-By-Accession
- Get-PDFsAndImages_d
- Load-NCBI-GFF3-File
- OMIM-Update_w
- Pull-FPBase-Proteins_m
- Run-Meow_w
- Run-Priority-Pipeline-Alliance_w
- Update-Length-For-Ensembl-Transcript
- Update-Orthology_w
- Update-Transcript_Sequences_w

## Impact

Low urgency, but it degrades exactly the signal you most want working: when one of these
12 jobs fails, the notification email that should explain the failure instead carries a
broken template body. Whatever diagnostic value was intended has never been delivered.

The failure trigger still fires, so recipients do learn the job failed — the subject line
and the attachment (where one is configured) are unaffected. This is about the body only.

## Proposed fix

Options, roughly in increasing effort:

1. **Replace the body with `$PROJECT_DEFAULT_CONTENT`** on all 12 jobs, matching what
   their SuccessTriggers already use. Gives recipients the standard build-log link.
   Smallest change; loses nothing, since nothing renders today.
2. **Make the template degrade gracefully** — guard each `new File(...)` with an
   `.exists()` check and emit a short "no report data available" line instead of throwing.
   Keeps the template usable for any job that later does produce the three files, and fixes
   all 12 in one file.
3. **Give the template per-job inputs** — parameterise the directory (e.g. by `$JOB_NAME`)
   so a job can drop its own `description.txt` / `column.header` / `reportRecords.txt` in
   and get a real table. Most work; only worth it if there is appetite for a genuine
   standard report format across these jobs.

Recommendation: **(2) plus (1)** — harden the template so it can never produce a broken
body again, and repoint the jobs that have no report data at
`$PROJECT_DEFAULT_CONTENT` so their emails say something useful.

Whichever option is taken, keep the paths derived from `System.getenv('TARGETROOT')` as the
template does today. `TARGETROOT` is a single literal value in practice, so substituting the
current path would work — but doing so would foreclose ever switching to a per-instance
`TARGETROOT`, which we want to keep available.

## Open questions

- Capture one of these failure emails (or the `email-ext` log) to record what recipients
  actually see when the template throws — worth pasting into this ticket.
- Was this template ever wired to a working producer historically, or has it been
  aspirational since the initial GitHub import? (`reportBody.template` has a single commit,
  `488ef2157b`, the initial import.)

## Related / out of scope

- **ZFIN-10411** — this was found while fixing that ticket's wrong-output-directory
  findings. Same family (a job reading a fixed path under `$TARGETROOT` that nothing
  populates), but too broad to fold into that diff.
- Three jobs in that ticket also carried a phantom `attachmentsPattern`
  (`ensembl-transcript-report.txt`, a filename nothing writes). Already fixed on the
  ZFIN-10411 branch — the same copy-paste habit that produced this template problem.
- `generateGff3.sh` writes its output to a hardcoded
  `/opt/zfin/www_homes/zfin.org/home/data_transfer/Downloads/` rather than
  `$TARGETROOT/home/data_transfer/Downloads/`. Those two paths are byte-identical today, so
  nothing is currently misrouted — but hardcoding it forecloses ever pointing `TARGETROOT`
  at a per-instance path (the `${TARGETROOT_PREFIX}/www_homes/${INSTANCE}` form that sits
  commented out in `commons/env/test-unittest.properties`). Worth its own ticket as
  future-proofing, not as a live bug.
