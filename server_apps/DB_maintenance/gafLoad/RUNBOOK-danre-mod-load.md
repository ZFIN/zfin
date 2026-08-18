# Runbook — running the unified DANRE-mod GO load locally

_ZFIN-10025 / ZFIN-10345. How to reset the DB to a baseline, run the unified load (report-only
or with real writes), and produce the per-org before/after xlsx diffs. Companion to
`README-danre-mod-consolidation.md`, which covers what the load does and what is still open;
this file is only how to drive it._

**Every command here runs inside the compile container**, from `$SOURCEROOT` (its default
working directory). How you get a shell there is up to you and deliberately not covered.
Times are from a 10-core / 64 GB stack: baseline load ~6 min, deploy ~35 s, the load itself
20–57 min depending on the input file.

---

## The happy path

Everything below is detail. On a stack that is already provisioned and deployed, a full
baseline-to-comparison run is three steps:

```bash
gradle loaddb -DB=/opt/zfin/unloads/db/2026.07.05.1/2026.07.05.1.bak
gradle liquibasePostBuild
```

then trigger **`Load-GPAD-GO-Central_m`** in Jenkins. The job takes the BEFORE snapshot itself,
runs the load, runs the dedup cleanup, takes the AFTER snapshot, and produces the per-org
workbooks — so "before" is the baseline you just restored, with nothing to sequence by hand.

Spend 30 seconds on §2 in between anyway: the entire comparison is read against those four
org counts, and it is the cheapest guard against measuring the wrong thing.

**Add these when they apply:**

| when | step |
|---|---|
| Java changed since the last deploy | `gradle dirtydeploy` (§3) |
| a job `config.xml` changed | `ant deploy-jobs` then `$CLI reload-configuration` (§12) |
| `$TARGETROOT` is fresh, or a Postgres function/trigger changed | add `gradle make` to §1 |
| restoring a dump older than the current one | add `gradle liquibasePreBuild` to §1 |

Both deploy steps are independent of the database reset and can run any time before you
trigger — with one rule: **never `ant deploy-jobs` while a build is running.** Its `remove-jobs`
step deletes any job directory in `$JENKINS_HOME` with no counterpart in the source tree, taking
the running build and its history with it.

To drive the load by hand instead of through Jenkins, follow §4 → §5 → §6 in order.

---

## 0. Prereqs

The environment the rest of this file assumes:

| var | value | note |
|---|---|---|
| `SOURCEROOT` | `/opt/zfin/source_roots/zfin.org` | **your worktree**, bind-mounted — edits are live, and anything written here is visible outside the container |
| `TARGETROOT` | `/opt/zfin/www_homes/zfin.org` | a **Docker volume**, not a host path (see §7) |
| `PGHOST` / `DBNAME` | `db` / `zfindb` | so `psql -h $PGHOST -d $DBNAME` just works |
| `CATALINA_BASE` | `/opt/zfin/catalina_bases/zfin.org` | where deployed classes live (see §3) |
| `JENKINS_HOME` | `$TARGETROOT/server_apps/jenkins/jenkins-home` | shared with the Jenkins container (see §12) |

`$TARGETROOT` being a volume is the one that catches people: reports and workbooks written
there are invisible from outside the container (§7).

---

## 1. Reset the DB to a baseline

```bash
gradle loaddb -DB=/opt/zfin/unloads/db/2026.07.05.1/2026.07.05.1.bak \
  && gradle make && gradle liquibasePreBuild && gradle liquibasePostBuild
```

Available unloads are whatever is under `~/development/unloads/db/` on the host, mounted at
`/opt/zfin/unloads/db/` in the container.

> ⚠️ **Always name the dump when you care which snapshot loads.** Left to itself the task picks
> the unload directory with the newest **mtime**, not the newest name — so a freshly-downloaded
> *older* snapshot wins, and you silently get the wrong baseline. `-DB=<path>` (or the equivalent
> `-Dunload=<path>`) removes the ambiguity. Both are read by `loadDatabase`, which `loaddb`
> delegates to; system properties propagate through the wrapper, so either entry point is fine.

> ⚠️ **`loaddb` alone is NOT a usable baseline for this load.** `liquibasePostBuild` is what
> applies the three ZFIN-10025 migrations under
> `source/org/zfin/db/postGmakePostloaddb/1184/migrations/`, and the load's numbers depend on
> all of them:
>
> | migration | without it |
> |---|---|
> | `0010-…-eco-0007322-subcell-iea-mapping` | `GpadParser.postProcessing` rejects the ~17k `ECO:0007322` UniProtKB-SubCell rows and the matching stored rows get flagged for removal — errors +~17k, removals up to +~24k |
> | `0030-…-add-exp-go-evidence-code` | the 105 `ECO:0000269`/`EXP` rows fail with `invalid evidence code: EXP` |
> | `0040-…-dedupe-annotation-extension-groups` | the ~328k accumulated duplicate annotation-extension groups stay, and `_details.txt` is unreadable |
>
> Preloaded `zfin-db-preloaded` images generally predate these, so check (§2) rather than assume.

`gradle make` and `gradle liquibasePreBuild` are omitted above because neither does anything
useful here: `make` deploys Postgres functions/triggers and files into `$TARGETROOT`, and this
branch changes no functions or triggers, while every changeset in the preBuild changelog is
already inside a dump this recent. Add them back in the cases listed in the happy-path table.

---

## 2. Verify the baseline

```bash
psql -h $PGHOST -d $DBNAME -c "
select o.mrkrgoevas_annotation_organization as org, count(*)
from marker_go_term_evidence e
join marker_go_term_evidence_annotation_organization o
  on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
group by 1 order by 2 desc;"
```

For **2026.07.05.1** — the snapshot the 2026-07-07 and 2026-08-07 reports both use — this must
be exactly:

| org | rows |
|---|--:|
| UniProt | 111,089 |
| GOA | 109,656 |
| Noctua | 36,025 |
| FP Inferences | 1,623 |

Then confirm the ECO migration applied:

```bash
psql -h $PGHOST -d $DBNAME -c "
select t.term_ont_id, m.egm_go_evidence_code from eco_go_mapping m
join term t on t.term_zdb_id = m.egm_term_zdb_id where t.term_ont_id = 'ECO:0007322';"
```

Expect one row → `IEA`. If it's missing, re-run `gradle liquibasePostBuild`, or apply the
migration file directly with `psql -f`.

**Also worth checking on any preloaded image:** whether the MGTE dedup cleanup ever ran. A DB
carrying thousands of redundant GOA rows will produce removal counts that can't be compared to
the reports.

```bash
psql -h $PGHOST -d $DBNAME -c "
with d as (
  select e.mrkrgoev_mrkr_zdb_id, e.mrkrgoev_term_zdb_id, e.mrkrgoev_source_zdb_id,
         e.mrkrgoev_evidence_code, e.mrkrgoev_relation_term_zdb_id,
         e.mrkrgoev_annotation_organization_created_by, count(*) c
  from marker_go_term_evidence e
  join marker_go_term_evidence_annotation_organization o
    on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
  where o.mrkrgoevas_annotation_organization = 'GOA'
  group by 1,2,3,4,5,6 having count(*) > 1)
select count(*) dup_groups, sum(c-1) redundant_rows from d;"
```

A clean 2026.07.05.1 baseline is fine here; the `dev` preloaded image was not (13,550 redundant
GOA rows, ≈ its whole 13,490-row excess over this snapshot).

Finally, check the annotation-extension groups have been deduped (migration `0040`):

```bash
psql -h $PGHOST -d $DBNAME -c "
select count(*) groups from marker_go_term_annotation_extension_group;"
```

Expect a count in the **low thousands**; a **six-figure** one means `0040` has not run. (The
exact number depends on how many annotations the DB holds — a post-load DB measured 1,053 — so
read the order of magnitude, not the digits.) The pre-fix load doubled every annotation's
extension groups on each pass (README finding 9), reaching 328,727 on this baseline, and a DB
carrying that backlog produces a `_details.txt` in which a single annotation's extensions run to
thousands of lines.

---

## 3. Deploy the Java

```bash
gradle dirtydeploy
```

Only needed after **Java** changes, and easy to forget after a `git pull`. `build.xml`, the SQL
scripts and the shell helpers are all read live from `$SOURCEROOT`, so edits to those need no
redeploy; `.java` changes are invisible to the load until deployed.

The load's classpath is `$CATALINA_BASE/webapps/ROOT/WEB-INF/classes`, **not** `$TARGETROOT`
(which has no `WEB-INF/classes` at all — an easy wrong place to look). Compare a class you just
changed against its source:

```bash
stat -c %y $CATALINA_BASE/webapps/ROOT/WEB-INF/classes/org/zfin/datatransfer/go/service/GafLoadJob.class
```

Running the load against stale classes is silent — you get a clean run with the old behaviour,
and the numbers quietly fail to match this file's reference run.

---

## 4. Run the load

**Report-only (no DB writes) — the default:**

```bash
cd $TARGETROOT && \
  ant -f $SOURCEROOT/server_apps/DB_maintenance/build.xml load-gpad-danre-mod \
      -DjobName=my-run
```

**With real writes:**

```bash
cd $TARGETROOT && \
  GAF_LOAD_REPORT_ONLY=false \
  ant -f $SOURCEROOT/server_apps/DB_maintenance/build.xml load-gpad-danre-mod \
      -DjobName=my-run
```

**Against a different input file** — add `DANRE_MOD_GPAD_URL`:

```bash
cd $TARGETROOT && \
  DANRE_MOD_GPAD_URL=https://ftp.ebi.ac.uk/pub/contrib/goa/goex/current/gpad/DANRE-mod.gpa.gz \
  GAF_LOAD_REPORT_ONLY=false \
  ant -f $SOURCEROOT/server_apps/DB_maintenance/build.xml load-gpad-danre-mod \
      -DjobName=my-run
```

Default URL is `https://current.geneontology.org/annotations/gpad/DANRE-mod.gpad.gz`.

### Through Jenkins instead

`Load-GPAD-GO-Central_m` wraps everything above **plus** the before/after snapshots and diff (§6)
and the cleanup (§5) — which is what makes it the better route for a comparison run: one trigger,
and the workbooks come out archived with the build. It is parameterized, so a one-off needs no
config edit:

| parameter | default | |
|---|---|---|
| `DANRE_MOD_GPAD_URL` | the published prod file | point at a candidate release to QC it |
| `GAF_LOAD_REPORT_ONLY` | `false` — **it writes** | tick for a dry run |
| `RUN_MGTE_CLEANUP` | `true` | run the dedupe before the AFTER snapshot |
| `MGTE_CLEANUP_CSVS` | `true` | keep the cleanup's CSVs in `<jobName>-dbdiff` |

⚠️ **The job ships DISABLED** (it is half of the cutover switch — see README open decision 3).
For a QC run, enable it, run it, and disable it again; do not leave it enabled, and do not treat
enabling it as the cutover.

From the UI: *Build with Parameters*. From a shell, set up `$CLI` as in §12, then:

```bash
$CLI build Load-GPAD-GO-Central_m -f -v \
    -p GAF_LOAD_REPORT_ONLY=true \
    -p MGTE_CLEANUP_CSVS=false
```

`-f` follows the build so the exit code reflects its outcome; `-v` streams the console output.
Drop both to fire and forget. Omitted parameters take the defaults above — which means
**omitting `GAF_LOAD_REPORT_ONLY` writes to the database**.

Reports land in `$TARGETROOT/server_apps/DB_maintenance/gafLoad/Load-GPAD-GO-Central_m/`, and the
workbooks and cleanup CSVs in the sibling `-dbdiff` directory (§7).

> Exit code 2 means "completed with errors"; the job maps that to UNSTABLE rather than FAILURE,
> for the reason in the note below. A run with errors is normal.

See §12 for deploying a changed `config.xml`.

The target echoes its settings at startup — **check `reportOnly: false` really is set** before
walking away from a write run.

> ⚠️ **`BUILD FAILED` / `Java returned: 2` is EXPECTED** whenever the run produced any errors —
> `GafLoadJob` exits 2 in that case. It does *not* mean the run aborted. Confirm completion by
> reading `_summary.txt` (§7); a finished run always writes one.

Observed runtimes: 20 min (142,612-row 06-17 file), 30 min (214,064-row deduped), 57 min
(459,621-row raw goex file, real writes).

---

## 5. Dedup cleanup (after a write run only)

```bash
gradle cleanMarkerGoTermEvidenceDuplicatesTask
```

> ⚠️ **The `cd $SOURCEROOT` is mandatory.** Chaining this after the Ant command leaves you in
> `$TARGETROOT`, and gradle fails with *"Directory '/opt/zfin/www_homes/zfin.org' does not
> contain a Gradle build."*

It also writes four record-keeping CSVs. The SQL ends in `\copy` statements with **relative**
filenames, and `\copy` cannot interpolate psql variables, so they land in the process's working
directory. One flag decides both where they go and whether they exist at all:

**Producing a diff for comparison — say nothing and get nothing.** The CSVs are byproducts nobody
reads, and each can be large:

```bash
gradle cleanMarkerGoTermEvidenceDuplicatesTask
```

**Auditing what the cleanup removed — name a directory:**

```bash
gradle cleanMarkerGoTermEvidenceDuplicatesTask \
  -DcleanupCsvDir=$TARGETROOT/server_apps/DB_maintenance/gafLoad/mydiff
```

There is deliberately no "write them to the current directory" setting. That was the old default,
and it is what put four CSVs in the source tree on every run.

`tmp_mgte_duplicates.csv` is the useful one: **0 rows means the load produced no duplicate
annotations.**

In Jenkins this is the `MGTE_CLEANUP_CSVS` parameter on `Load-GPAD-GO-Central_m` and
`Load-GAF-GOA_m` — checked (default) passes `<jobName>-dbdiff` so the CSVs are archived with the
build, unchecked omits the flag. `Gene-Association-File-Export_w` never passes one: it runs the
dedupe only so its export sees clean data.

If the directory cannot be created the task warns and writes **no** CSVs rather than falling
back to the current directory — that fallback is the litter the flag exists to prevent. The
cleanup itself still runs either way.

That has held on every run so far, including against the 53%-duplicated goex file.

---

## 6. Before/after per-org diff (the xlsx workbooks)

Snapshot **before** §4, snapshot again after §5, then `csvDiff`. Mirrors the
`Load-GPAD-GO-Central_m` Jenkins job.

Both steps are the same two scripts the Jenkins jobs call, so a manual run and a job run produce
directly comparable workbooks:

```bash
G=$SOURCEROOT/server_apps/DB_maintenance/gafLoad
OUT=$TARGETROOT/server_apps/DB_maintenance/gafLoad/mydiff
$G/mgte_snapshot.sh before $OUT --others GOA Noctua PAINT "FP Inferences" UniProt
```

**AFTER** — identical with `after` in place of `before`.

`FP Inferences` contains a space; the scripts turn it into the filename tag `FP_Inferences`.
Include `UniProt` and `FP Inferences` even though the load never touches them: proving they are
0-diff is the evidence for the two known cutover gaps.

`--others` adds one more pair, `mgte_*_OTHER.csv`, holding every row whose organization is **not**
named. It should always be empty. If it is not, some organization is being written that nobody is
watching — and its rows would otherwise be missing from the diff entirely, which is exactly how
`PAINT` went unnoticed until it was added by hand.

> ⚠️ **Never write snapshots into `.../gafLoad/<jobName>/` — the load deletes that directory.**
> `GafLoadJob` calls `clearReportDirectory()` as its first action, which is a full
> `FileUtils.deleteDirectory()` on `<baseDir>/<jobName>`. A BEFORE snapshot written there is gone
> before the load even downloads, and the diff step then dies with
> `mgte_before_GOA.csv (No such file or directory)`. Use any other directory — `mydiff` above, or
> the `<jobName>-dbdiff` convention the Jenkins jobs now use. This bit all three Jenkins jobs
> (fixed 2026-08-10); the manual runs in the reports escaped it only because they happened to use
> a separate directory.

**Diff:**

```bash
$SOURCEROOT/server_apps/DB_maintenance/gafLoad/mgte_csvdiff.sh \
  $TARGETROOT/server_apps/DB_maintenance/gafLoad/mydiff --others GOA Noctua PAINT "FP Inferences" UniProt
```

Yields one `mgte_dbdiff_<org>.xlsx` per org (sheets: deletes / adds / updated_1 / updated_2).

> **The key and ignore lists live in `mgte_csvdiff.sh`, once** — they used to be duplicated
> verbatim in all three GO job configs and could drift apart silently. Key = every identity
> column; ignore = `zdb_id` (so a recycled id counts as unchanged rather than delete+add) plus
> the five derived readable columns, which ride along for eyeballing the sheets. Changing either
> list makes the numbers incomparable to the 2026-07-07 and 2026-08-07 reports, so change it
> knowingly. Note `protein_acc` is in neither list: it is compared but not matched on, so a
> UniProt isoform reassignment surfaces as an update rather than as a delete+add pair.

---

## 7. Where the output lands, and getting it out

Reports: `$TARGETROOT/server_apps/DB_maintenance/gafLoad/<jobName>/`

| file | size | commit? |
|---|--:|---|
| `<jobName>_summary.txt` | ~270 B | yes |
| `<jobName>_error_summary.txt` | 0.4–1 MB | yes |
| `<jobName>_details.txt` | up to ~350 MB | no |
| `<jobName>.html` | ~7 MB | no |

> ⚠️ **`$TARGETROOT` is a Docker volume, not a host directory**, so nothing written there is
> visible outside the container. `$SOURCEROOT` is the bind-mounted worktree, so copy anything you
> want to keep into it and collect it from the worktree afterwards:

```bash
mkdir -p $SOURCEROOT/.out
cp $TARGETROOT/server_apps/DB_maintenance/gafLoad/mydiff/*.xlsx $SOURCEROOT/.out/
cp $TARGETROOT/server_apps/DB_maintenance/gafLoad/my-run/my-run_*summary.txt $SOURCEROOT/.out/
```

`.out/` is gitignored-by-convention scratch — empty it once you have the files.

`_details.txt` is the only place the per-row add/remove lists live, and it is not committed —
so extract what you need from it *before* the next run overwrites it. Section markers are
`== REMOVED ==`, `== ADDED ==`, `== UPDATED ==`, `== ERRORS ==`, `== EXISTING ==`, and each
`MarkerGoTermEvidence{…}` line carries `organizationCreatedBy=`, which is how the per-source
removal breakdowns in the reports were produced:

```bash
f=$TARGETROOT/server_apps/DB_maintenance/gafLoad/my-run/my-run_details.txt
a=$(grep -n "^== ADDED ==" $f | head -1 | cut -d: -f1)
sed -n "3,$((a-1))p" $f | grep -o "organizationCreatedBy=[^,]*" | sort | uniq -c | sort -rn
```

---

## 8. Testing a locally modified input file

The loader takes a URL and calls `DownloadService.getLastModifiedOnServer`, so `file://` is
unreliable — the file has to be served over HTTP. The one step here that is **not** in the
container: start a server next to the file on the Docker host, e.g.
`python3 -m http.server 8899`. From the container it is then reachable as
`host.docker.internal`:

```bash
curl -sI http://host.docker.internal:8899/myfile.gpad.gz | head -3    # verify it resolves
# then pass DANRE_MOD_GPAD_URL=http://host.docker.internal:8899/myfile.gpad.gz
```

> Both `DANRE-mod.gpad.gz` and `DANRE-uniprot.gpad.gz` live under
> `https://current.geneontology.org/annotations/gpad/`. Fetch over **`https`** (or `curl -L`) —
> `http://` 301-redirects, and a client that doesn't follow it gets a 167-byte redirect page that
> looks like an empty file.

Useful for de-duplicated variants. To strip GOA's duplicate rows (identical in all 12 GPAD
columns except `id=GOA:`), note that a plain `sort -u` finds nothing — the id must go first:

```bash
{ zcat in.gpa.gz | grep '^!
  zcat in.gpa.gz | grep -v '^!' \
    | awk -F'\t' '{k=$0; gsub(/id=GOA:[0-9]+\|?/,"",k); if(!(k in s)){s[k]=1; print}}'
} | gzip > out.gpad.gz
```

---

## 9. Gotchas, condensed

| symptom | cause |
|---|---|
| `BUILD FAILED`, `Java returned: 2` | normal when the run had errors; check `_summary.txt` |
| ~17k `invalid eco code: ECO:0007322` errors | `liquibasePostBuild` never ran (§1) |
| gradle: *"does not contain a Gradle build"* | still in `$TARGETROOT`; gradle needs `cd $SOURCEROOT` (§5) |
| loaded the wrong baseline | with no `-DB=`, unloads are picked by mtime, not name (§1) |
| `$TARGETROOT` looks empty from the host | it's a Docker volume; stage via `$SOURCEROOT` (§7) |
| removal/error counts don't match the reports | wrong baseline vintage, or a DB that never had the MGTE dedup cleanup (§2) |
| stray `*.csv` in the repo root | psql run directly on `clean_marker_go_term_evidence.sql` without `-v write_csvs=false` (§5) |
| a `-D` flag on a gradle task does nothing | `console.gradle` has to forward it with `systemProperty`; `JavaExec` inherits nothing (§11) |
| Jenkins keeps running the old job config | `ant deploy-jobs` only writes disk; reload it (§12) |
| one annotation's extensions run to thousands of lines in `_details.txt` | baseline predates migration `0040` (§1, §2) |
| `invalid evidence code: EXP` (105) | baseline predates migration `0030` (§1) |
| `csvDiff`: `mgte_before_*.csv (No such file or directory)` | snapshots were written into `.../gafLoad/<jobName>/`, which `clearReportDirectory()` wipes at load start (§6) |

---

## 10. Reference: what a good run looks like

2026.07.05.1 baseline, real writes, the published `DANRE-mod.gpad.gz` (build generated
2026-08-04, 459,621 rows).

```
processed: 459621   added: 111318   updated: 32
removed: 50184      existing: 152743   errors: 195528
```

Per-org: GOA 109,656 → **174,878**, Noctua 36,025 → **31,446**, FP Inferences and UniProt
unchanged. Annotation-extension groups should stay in the low thousands rather than doubling.

> `added` and `errors` are the measured run adjusted by the 105 `EXP` rows that migration `0030`
> turned from errors into adds (README finding 7a); the per-org GOA figure is confirmed directly
> against a post-load database.

Errors are dominated by 187,923 `Duplicate annotation entry` — an artifact of the file's own row
duplication, not of the load (README finding 8). `README-danre-mod-consolidation.md` interprets
these numbers; findings 1 and 2 account for the Noctua drop and most of the GOA rise.

---

## 11. Turning off the secondary load's GO streams

At cutover the unified load takes over the `*2go` content the UniProt-secondary load produces
today. Rather than deleting that code, `UniprotSecondaryTermLoadTask` gates it behind two flags,
each **defaulting to on** so existing behaviour is unchanged until deliberately switched:

| flag | gates |
|---|---|
| `LOAD_INTERPRO2GO_EC2GO` | the InterPro2GO + EC2GO `MarkerGoTermEvidence` handlers |
| `LOAD_KW2GO` | the `spkw2go` add/remove handlers |

Set either to `false` to skip it. They are read as a system property first, then the
environment, so both of these work:

```bash
gradle <secondaryLoadTask> -DLOAD_INTERPRO2GO_EC2GO=false
LOAD_INTERPRO2GO_EC2GO=false <the Jenkins job's normal invocation>
```

> ⚠️ **Prefer `-D` when driving it through gradle**, and know what makes it work: `JavaExec`
> forks a fresh JVM that inherits neither your shell's environment nor the build's system
> properties, so a `-D` reaches the task **only because `console.gradle` forwards that exact
> property** with a `systemProperty` line. Adding a new `-D` flag to a task means adding it there
> too, or it is silently ignored.

The two are separate flags because they are separate decisions. InterPro2GO/EC2GO have a
successor in `DANRE-mod` (README finding 2); kw2go does not — GO retired `GO_REF:0000004`, so
turning `LOAD_KW2GO` off loses ~28k annotations outright. That one is still an open decision
(README open decision 4); do not flip it as a pair with the other.

---

## 12. Deploying a changed Jenkins job config

Two steps, and **the second is not optional**: `deploy-jobs` only writes to disk, while Jenkins
serves job configs from an in-memory model, so skipping the reload leaves it running the old
config indefinitely.

```bash
# 1. write the configs into JENKINS_HOME
ant deploy-jobs

# 2. make Jenkins re-read them
$CLI reload-configuration
```

Set `$CLI` up once per shell. Jenkins serves the CLI jar itself, and the admin password sits on
the `JENKINS_HOME` volume, which this container shares:

```bash
curl -sf -o /tmp/jenkins-cli.jar http://jenkins:9499/jobs/jnlpJars/jenkins-cli.jar
CLI="java -jar /tmp/jenkins-cli.jar -s http://jenkins:9499/jobs -http \
     -auth admin:$(cat $JENKINS_HOME/secrets/initialAdminPassword)"
```

⚠️ **The `-http` is required.** Without it the CLI uses its WebSocket transport, which Jenkins
refuses from another container: `CLI handshake failed with status code 403`. That reads like an
auth failure and is not — the same credentials work over `-http`.

⚠️ **It must be bare `ant deploy-jobs` from `$SOURCEROOT`.** `ant -f buildfiles/jenkins.xml
deploy-jobs` fails with *"failed to create task or type if"*: those are ant-contrib tasks, and the
`taskdef` registering them lives in the root `build.xml`, which imports `jenkins.xml`.

Expected in the output, in a git worktree: two `fatal: not a git repository … .git/worktrees/…`
lines with `Result: 128`, from an `<exec>` that reports the instance name. The build still
succeeds.

### Verifying the reload took

Use `get-job`. It renders Jenkins' **in-memory** model, so unlike reading
`$JENKINS_HOME/jobs/<job>/config.xml` it proves Jenkins re-read the file rather than merely that
`deploy-jobs` wrote it:

```bash
$CLI get-job Load-GPAD-GO-Central_m | grep cleanupCsvDir
```

Do **not** try to reload over plain HTTP. `curl -X POST .../jobs/reload` returns
**403 "No valid crumb"**, and fetching a crumb first returns 403 as well, because `useSecurity`
is on and `/jobs/crumbIssuer` itself requires authentication. The CLI handles both.
