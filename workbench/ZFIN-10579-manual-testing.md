# ZFIN-10579 manual testing: publication file issues, provenance, and Files tab

Covers the `Find-Publication-File-Issues_w` job, the PMC loader scripts, the Files tab on the
publication edit page, and the updates page.

Only two pages show the new data: the Files tab (timestamp columns and "Added by") and the updates
page (audit rows). `pf_source` and `pf_person_zdb_id` are also checked in SQL.

## 0. Setup
- Load a recent DB.
- Confirm the Liquibase changeset `ZFIN-10579-publication-file-source-and-person` applied:
  ```
  psql -h db -U postgres -d zfindb -c "\d publication_file"
  ```
  You should see `pf_source` and `pf_person_zdb_id`, plus the `pf_source_check` constraint.
- Use a test publication with no files. Call its id `PUB`.

## 1. Files tab (`/action/publication/PUB/edit`, Files tab)
1. The table has **Entered in DB**, **Modified on disk** and **Added by** columns. The "No files yet"
   row spans all 6 columns.
2. Upload an Original Article. Both timestamps are about now, and **Added by** shows your full name.
3. Upload a Supplemental Material file. It gets its own row, timestamps and **Added by**.
4. Delete the stored file on disk and reload. **Modified on disk** shows red "missing"; **Entered in
   DB** still shows a date.
5. Older rows show real dates, not "—".
6. **Added by** for rows with no person:
   - loaded by the PMC loader: `PMC loader`
   - attached through a ZebraShare submission by someone without a person record: `ZebraShare submission`
   - rows from before provenance was recorded: blank
7. Upload, change a type, and delete a file. After each, the table reloads with **Added by** still
   filled in.

## 2. Updates page (`/action/updates/PUB`)
Each action adds a `publication file` row (comment in brackets):
- Add a file: `Add file`, new value `<path> (<type>)`.
- Upload a second Original Article: `Replace Original Article` for the old file, then `Add file`.
- Change a file's type: `Edit file type`, old and new values.
- Delete a file: `Delete file`.

The submitter is you for all of these.

## 3. Provenance columns (SQL)
```
select pf_file_name, pf_source, pf_person_zdb_id, pf_date_entered
from publication_file where pf_pub_zdb_id = 'PUB' order by pf_pk_id;
```
- Curator UI upload: `CURATOR_UPLOAD` and your person id.
- `/action/zebrashare/new` attachment: `ZEBRASHARE_SUBMISSION` and the submitter's id.
- Older rows: NULL in both.
- Deleting a person must not delete files: `ON DELETE SET NULL` clears `pf_person_zdb_id`.

## 4. PMC loader scripts
Run `getPDFandImages.groovy` for one publication with a PMC id and no Original Article, if S3 access
allows. Otherwise load a small `pdfsToLoad.txt` and the `add_basic_pdfs` input by hand.
- New supplement rows have `pf_source = 'PMC_LOADER'` and no person.
- Each addition has a matching `Add file (PMC loader)` row on the updates page.
- **Run the loader a second time.** No new rows should appear (regression for the 565-row case,
  ZDB-PUB-180822-2).
- Where a stale `PMC<id>.<v>.pdf` Original Article row exists, `add_basic_pdfs.sql` removes it, logs
  the removal, and leaves a single Original Article.

## 5. Find-Publication-File-Issues_w
Run `gradle findPublicationFileIssuesTask -PtaskArgs="..."` (or the Jenkins job) against a DB and
files directory with seeded problems.

| Seed | Expected report line |
|---|---|
| zero-byte or missing file | `EMPTY_FILE` |
| two Original Articles, different content | `MULTIPLE_ORIGINAL_ARTICLES`; `N distinct md5`; `md5=not hashed (unique size)` where sizes differ |
| two Original Articles, identical content | `MULTIPLE_ORIGINAL_ARTICLES_DUPLICATED` |
| same file and same type recorded twice | `DUPLICATE_FILE`, summarized as `N x Type [ids=...]` |
| same file recorded as two different types | `DUPLICATE_FILE_MIXED_TYPES`, per-row type, id and date |
| different names, same content | with `--check-identical-content`, an `IDENTICAL_CONTENT` line with the md5 |
| same file content on two different publications | with `--check-identical-content`, an `IDENTICAL_CONTENT_ACROSS_PUBLICATIONS` line listing each publication, file, type and id |
| uploaded name or stored path contains another publication's ZDB ID (e.g. `ZDB-PUB-190118-10.pdf` on ZDB-PUB-190118-1) | `FILE_NAMED_FOR_OTHER_PUBLICATION`; Details say which name matched (uploaded name, stored path or both) and whether the other publication has an identical file |

Then check:
- With no flags, the job changes nothing. The task itself exits 0 even when issues are found.
- Report columns are `Issue, Publication ZDB ID, Filename, Details, More Details` (the xlsx has "Compare publications"
  between Details and More Details). **Details** is short: `2 x Supplemental Material`, `Other + Original Article`,
  `3 x Original Article`, `Identical content: 'a.pdf', 'b.pdf'`, `Also on: ZDB-PUB-... (a.pdf, b.pdf)` (the file names in the matching group), or
  `Contains files named: 'ZDB-PUB-190118-10.pdf'` (one `FILE_NAMED_FOR_OTHER_PUBLICATION` line per publication). It has
  no row ids, timestamps or md5s. **More Details** has the full text. File names in the reports have no directories
  (`FEB2-597-262-s002.pdf`, not `2022/ZDB-PUB-221018-25/FEB2-597-262-s002.pdf`); the `updates` table keeps full paths.
- `--fix-empty-superseded-original` and `--fix-empty-promote-zdbid-pdf` each fix only their own case.
- `--fix-identical-originals` keeps the lowest-id row of identical Original Articles and ignores the
  differing pair.
- `--fix-duplicate-files` collapses only same-type duplicates; the mixed-type one stays in the report.
- Every fix writes an `updates` row with no submitter, a line in `fixed_publication_files.csv`, and
  leaves files on disk untouched.
- The duplicate fixes batch their logging: `--fix-duplicate-files` writes one `updates` row and one CSV
  line per duplicated file and type, not per deleted row (e.g. `564 rows (ids 36762..38211) duplicating
  the same file and type, kept row id 36761`). `--fix-identical-originals` writes one per stored file.
  The two empty-file fixes still write one per deleted row.
- Fixes run before the report, so a re-run shows only what is left. A second run with the same flags
  fixes nothing.
- Without `--check-identical-content`, no `IDENTICAL_CONTENT` or `IDENTICAL_CONTENT_ACROSS_PUBLICATIONS` lines appear and the run is faster.
- An unknown flag exits with code 2 and prints the valid flags.
- Build status: a run that finds issues ends **UNSTABLE** (yellow; the shell step exits 3 when the CSV has more than its
  header line) and sends the "Publication file issues found" email with the xlsx attached. A run with no issues is
  **SUCCESS**. A run where the scan itself errors (for example, stop the DB, or break a flag) is **FAILED** and sends the
  "Publication file scan failed" email with the build log. The job removes the previous run's report files first, so an
  old report can't mark a new run unstable.
- Each CSV has an `.xlsx` twin (`publication_file_issues.xlsx`, and `fixed_publication_files.xlsx` when a fix flag is
  on) with the same rows. In Excel, the publication ZDB ID cells are links to `<instance URL>/ZDB-PUB-...`
  (`INSTANCE_URL` if set in the environment, otherwise `https://<DOMAIN_NAME>`), and clicking one opens the
  publication. The header row is frozen with filters on, and a very long Details cell is cut at 32,000 characters
  (Excel's limit; the CSV has the full text).
- In `publication_file_issues.xlsx`, `IDENTICAL_CONTENT_ACROSS_PUBLICATIONS` and
  `FILE_NAMED_FOR_OTHER_PUBLICATION` rows have a fifth column, **Compare publications**, with a "View publications"
  link to `<instance URL>/action/infrastructure/data-citation-list/<ZDB-PUB of the row>/<pub>,<pub>...`, which lists
  the publications involved. For `FILE_NAMED_FOR_OTHER_PUBLICATION`, a publication that no longer exists is left out
  of the link, and the link is omitted if no other publication remains. Other rows leave the column blank. The CSV
  has no such column.

## 6. Jenkins
- The job directory is `Find-Publication-File-Issues_w`. Rename the old
  `Find-Empty-Publication-Files_w` directory by hand before deploying, or the job's build history is
  deleted.
- The parameters page shows `FIX_EMPTY_SUPERSEDED_ORIGINAL`, `FIX_EMPTY_PROMOTE_ZDBID_PDF`,
  `FIX_IDENTICAL_ORIGINALS`, `FIX_DUPLICATE_FILES` and `CHECK_IDENTICAL_CONTENT`, all off by default.
- Tick each parameter once and confirm the matching flag appears in the console's `TASK_ARGS`.
- `publication_file_issues.csv`/`.xlsx` and `fixed_publication_files.csv`/`.xlsx` are archived as artifacts, and the
  failure email still goes out.
- Both emails ("Publication file scan failed" and "Publication file issues found") contain a `Build:` link to the run's
  page (`${BUILD_URL}`); the failure email also links the console log.
