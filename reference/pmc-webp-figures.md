# PMC webp figures (ZFIN-10523)

## Overview

PMC's cloud service began delivering article figures as `.webp` during 2026.
`getPDFandImages.groovy` did not download that format, so publications loaded
with a PDF but no figures and no figure legends. The job reported SUCCESS
throughout.

**Jenkins job**: `Get-PDFsAndImages_d`
**Script**: `server_apps/data_transfer/PUBMED/getPDFandImages.groovy`
**Ticket**: ZFIN-10523 · **Fix**: PR #2029

## What went wrong

The script filters the PMC S3 package before downloading:

```groovy
// getPDFandImages.groovy
@Field final Set<String> DOWNLOADABLE_EXTENSIONS = ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'tif', 'tiff'] as Set
```

`webp` was absent, so those keys were dropped. The article XML was still parsed
afterwards, still found `<graphic xlink:href='fig1.webp'>`, and reported:

```
Skipping figure 'fig1.webp' for ZDB-PUB-… — file not found on disk (not available in S3)
```

That message is wrong and cost the investigation time: the file **is** in S3.
It was never requested. Both the original report and the first replies on the
ticket read this as PMC moving or withholding files.

The two halves of the pipeline had already diverged.
`ImageService.WEB_SAFE_EXTENSIONS` has always included `webp`, and the figure
loop carries a comment reading *"Sampled PMC packages ship figures as
.jpg/.webp"*. The display side knew about webp; the download side did not.

### Evidence

Build 2556 of `Get-PDFsAndImages_d`: five publications processed, all five
received their PDF, all five lost every figure. Thirty-six `Skipping figure`
lines, all thirty-six `.webp`, none any other extension.

Listing the same bucket the job itself reads (`PubmedUtils.listS3Files`, i.e.
`pmc-oa-opendata`) for `PMC13624371`:

```
PMC13624371.1/BDR2-118-e70105-e001.jpg     <- downloaded (jpg passes the filter)
PMC13624371.1/BDR2-118-e70105-g001.webp    <- "not available in S3"
PMC13624371.1/BDR2-118-e70105-g002.webp    <- "not available in S3"
PMC13624371.1/BDR2-118-e70105-g003.webp    <- "not available in S3"
PMC13624371.1/BDR2-118-e70105-g004.webp    <- "not available in S3"
PMC13624371.1/PMC13624371.1.pdf            <- downloaded
```

The `2 downloadable files` figure in the log is the filter's output: the PDF and
the one JPEG. Note PMC mixes formats — `webp` for figures, `jpg` for inline
graphics and equations — which is why affected publications end up with a PDF
and some image rows rather than nothing at all.

## Why the one-line fix was not enough

Adding `webp` to `DOWNLOADABLE_EXTENSIONS` alone would have traded one failure
for a quieter one.

The JDK has **no webp ImageIO reader**. On Java 21,
`ImageIO.getImageReadersByFormatName("webp")` is empty. Because `webp` counts as
web-safe, `ImageService` does not re-encode it, so the file would store
correctly while `convertImageToThumbnail` and `convertImageToMedium` failed on
every figure. Both calls are wrapped in `try`/`catch` that only prints, so rows
would still be written pointing at `_thumb.webp` and `_medium.webp` files that
were never created — figures present, thumbnails broken, nothing marked failed.

PR #2029 therefore also adds `com.twelvemonkeys.imageio:imageio-webp`, a
reader-only plugin that registers itself through the ImageIO service loader. No
code calls it directly.

## Scale

Counting publications that have a PDF but no figures, cohorted by the month
ZFIN created the record (production snapshot, 2026-09-30):

```
month      missing   loadable   blocked by copyright
2026-09      124       123         1
2026-08       74        66         8
2026-07       20         8        12
2026-06       34        17        17
2026-05       17         5        12
2026-04       11         3         8
2026-03        8         3         5
                        ---
                        225
2026-02       12         6         6   <- baseline resumes
2026-01        9         8         1
2025-12        8         6         2
2025-11        8         3         5
```

**Roughly 225 loadable publications, and 189 of them are August and September
alone.** "Loadable" means `pub_can_show_images = 't'`; the rest cannot have
figures loaded whatever the format, and are a standing background of eight to
twelve a month.

### A correction worth keeping

An earlier pass at this estimated ~440 affected publications from a *shortfall
model* — comparing the percentage of publications with figures each month
against the 83% that held steady for the fourteen months to 2026-02. That model
was misleading in a way worth recording, because it is the obvious way to
measure this.

It read the March–July period as a gradual PMC rollout, each month slightly
worse than the last. Counting actual rows shows most of that elevation is
copyright-blocked publications, which a percentage model cannot distinguish
from format failures: July has 20 missing but only 8 loadable. The real damage
is far more concentrated — it begins in earnest in August and is total by
September.

Both numbers describe something true (440 is the gap against baseline, 225 is
the actionable list) but only the second is a thing you can act on, and the
first implied a six-month gradual decline that the data does not support.

### What the numbers cannot tell you

Skipped figures were never recorded, so nothing in the database knows *why* a
given publication lacks figures. A publication with a PDF and no figures might
have had webp figures skipped, might have no figures at PMC at all, or might
simply have been fetched before PMC published them.

That last case is real and not rare: in a ten-publication dry run on a dev
instance, **five loaded figures successfully with no webp involved** — their
figures were in an accepted format and had simply not been available when the
job last looked. The selection SQL never revisits them, so they accumulate
silently alongside the webp ones. See *Open items* on retry behaviour.

## Re-fetching the backlog

The normal run cannot reach these publications. Its selection excludes anything
that already has a PDF:

```sql
WHERE pub_pmc_id IS NOT NULL AND pub_pmc_id != ''
  AND NOT EXISTS (SELECT 'x' FROM publication_file
                   WHERE pf_pub_zdb_id = zdb_id AND pf_file_type_id = 1)
  AND NOT EXISTS (SELECT 'x' FROM figure WHERE fig_source_zdb_id = zdb_id)
```

They have a PDF, so they are filtered out regardless of whether they have
figures. Deploying the fix does not heal them.

The script does accept an explicit list, and that path bypasses the filters
entirely (`WHERE zdb_id in (…)`, no `NOT EXISTS`):

```
cd $TARGETROOT/server_apps/data_transfer/PUBMED
PUB_ZDB_IDs="ZDB-PUB-…,ZDB-PUB-…" ./getPDFandImages.groovy
```

IDs may be comma- or whitespace-separated; command-line arguments work too, but
the environment variable suits a list of this size.

### Build the list carefully

**Restrict to publications with zero figures.** PDFs are safe to re-run —
`add_basic_pdfs.sql` guards with `not exists`. Figures are **not**:
`load_figs_and_images.sql` mints fresh ids with `get_id('FIG')` /
`get_id('IMAGE')` and inserts unconditionally, with no `not exists` and no
conflict clause. Re-running a publication that already has figures creates a
second set, duplicated on the page.

```sql
SELECT p.zdb_id FROM publication p
 WHERE p.pub_pmc_id IS NOT NULL AND p.pub_pmc_id != ''
   AND NOT EXISTS (SELECT 1 FROM figure f WHERE f.fig_source_zdb_id = p.zdb_id)
   AND EXISTS (SELECT 1 FROM publication_file pf
                WHERE pf.pf_pub_zdb_id = p.zdb_id AND pf.pf_file_type_id = 1)
   AND p.zdb_id ~ '^ZDB-PUB-26(0[3-9]|1[0-2])';
```

Publications that loaded *some* figures are deliberately excluded and need
handling separately.

Run against a production copy on 2026-10-03, restricted to records created from
2026-03 onward, that query returns:

```
288   candidates (PMC id, PDF loaded, zero figures)
225     of which pub_can_show_images = 't'   <- actually loadable
 63     excluded by pub_can_show_images      <- will stay without figures
```

Expect to load **225**, with 189 of those from August and September. Widening
the date range adds roughly 40 more loadable publications from before the webp
period — long-standing absences for reasons that predate this bug. Including
them is defensible but they are not this regression, and they will not all
succeed.

The 63 excluded here are the same standing background described under *Scale*;
they stay without figures however often the job runs.

### Order of operations

1. Merge and deploy PR #2029. Running the backlog before that skips the webp
   figures exactly as before and wastes the pass — each publication would end
   up with a PDF and still no figures, with nothing recording the attempt.
2. Dry-run ten ids. The log should read `Downloaded N files` with N matching the
   figure count, not `Skipping figure`.
3. Check `figure` / `image` rows appeared **and** that the thumbnails exist on
   disk. The thumbnail path is new ground for webp and is where the decoder gets
   its first real exercise.
4. Run the rest.

### Known exclusion

`load_figs_and_images.sql` silently drops rows for publications where
`pub_can_show_images` is not `'t'`:

```sql
delete from tmp_figs_to_load_with_ids
  where not exists (Select 'x' from publication
                    where pub_can_show_images = 't' and zdb_id = pub_zdb_id);
```

That is where the 63 in the counts above come from. They are excluded for
copyright reasons rather than webp, and will stay without figures however often
the job is run — which is why the list query filters on the same flag rather
than letting them fail silently inside the load.

## Open items

**No failure signal.** The job reports SUCCESS and emails success (to
`informix@` and `cvanslyk@`) after discarding 36 figures. There is no failure
status anywhere in it, which is the real reason a six-month regression went
unnoticed. Worth addressing on its own — a run that resolves zero of N
referenced figures is a reportable condition.

**Retry behaviour is unexamined.** Ceri asked on the ticket whether the job
re-checks for files; it is unanswered and independent of format. Curation works
close to PMC's processing window, so some misses may be timing rather than
webp. The selection SQL above means a publication that got a PDF before its
figures were published is never revisited.

**The misleading message.** `file not found on disk (not available in S3)`
conflates "we did not download it" with "PMC does not have it". Those are
different conditions and the second is the one worth alerting on.

## Note on the example publications

None of the three publications in the original report demonstrates this bug.
Ceri established that `ZDB-PUB-260826-4` is embargoed until 2027-02-25,
`ZDB-PUB-260530-15` has no figures at PMC, and `ZDB-PUB-260923-10` had not been
loaded by ABC. The webp cause came separately, from the blue team. Worth
remembering when validating the fix: those three will still show no figures
afterwards, for reasons unrelated to it.
