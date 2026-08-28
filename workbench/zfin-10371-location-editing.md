# ZFIN-10371 — feature location editing: four 500s and a silent removal

Follow-up work on `zf-10371b`, after Ceri reported that the original
ZFIN-10371 fix (`ea61a3db09`, "Override auto-calculated fields in feature
edit") errored out under test. Six commits, `33583e0b8b` … `2e03f2a02e`.

What began as one reported 500 turned into four distinct crashes plus a
data-loss-shaped behaviour in the location-removal workflow. They are all
in the Add/Edit Feature section of the curation tab.

---

## 1. Where this started

Ceri's comment carried a screenshot and "This is erroring out when I try to
test." The form showed every location field populated — Chromosome `24`,
Assembly `GRCz11`, Location `45578005` — with `08/26/26` in "Assembly
information not known as of".

A tomcat log captured separately showed a foreign-key violation writing
`sfcl_chromosome = ''`. That log turned out to be from a *different*
reproduction attempt, not Ceri's: its values match neither her form nor the
database row. Diagnosing from it first sent this work off in the wrong
direction for a while — worth remembering that a log volunteered alongside a
bug report is not necessarily a log *of* that bug.

Her actual failure: chr24 in GRCz11 is 42,172,926 bp, and she had entered
45,578,005 — 3.4 Mb past the end of the contig.

---

## 2. The four crashes

### 2.1 Out-of-range coordinate (`e982ed6580`, `eb9c0296b4`, `ff243ce9b8`)

The save reads the assembly FASTA for the feature's exact span, so a
position past the end of the chromosome threw from inside htsjdk:

    htsjdk.samtools.SAMException: Query asks for data past end of contig
      at GenomicLocationService.getReferenceSequence(GenomicLocationService.java:58)

`validateLocationWithinChromosome` now checks start/end against the
assembly's `.fai` index before anything is written, and reports
"Location 45578005-45578005 is outside chromosome 24 on GRCz11, which is
42172926 bp long".

Three separate entry points needed it — see §3.

Two related holes closed at the same time:

- The flanking-sequence windows run ±500 bases around the feature. Only the
  upstream side was clamped (to 1), so any feature within 500 bp of a contig
  *end* hit the same exception. Now clamped both ways.
- `validateReferenceSequence` got the same check as a backstop, because a
  save that does not touch the location never reaches the edit-path check —
  so a row already stored out of range would still have thrown.

### 2.2 Partial location clear (`33583e0b8b`)

Removal only deleted the `sequence_feature_chromosome_location` row when
*every* location field was cleared — chromosome, assembly, evidence code and
both positions. Clearing the chromosome and positions while leaving the
assembly selected fell through to the update path and wrote an empty
chromosome:

    update sequence_feature_chromosome_location
       set sfcl_assembly='GRCz12tu', sfcl_chromosome='', ...
    ERROR: violates foreign key constraint "sfcl_chromosome_fk_odc"

`sfcl_chromosome` is `NOT NULL` with a foreign key into `chromosome`, so a
location without a chromosome cannot be stored at all. Delete-detection now
keys on the chromosome alone, matching what the add-feature path and the
no-existing-location path already did.

`sfcl_evidence_code` is also `NOT NULL` (and `CHECK`-constrained to three
terms), so clearing the evidence code while keeping a chromosome was a
second latent 500 in the same form; it now reports a validation error.

### 2.3 Blank assembly (`33583e0b8b`, `2e03f2a02e`)

Blanking the Assembly dropdown gave "Assembly can only be changed to
GRCz12tu or GRCz11" — a guard meant to stop *retargeting* to an obsolete
assembly, misfiring on a removal.

The constraint that settles the behaviour is not in the schema:
`sfcl_assembly` is nullable with no foreign key, so the database would
accept a blank assembly — but `getLocationByFeature` only matches
assemblies `LIKE '%z1%'` or `'%9%'`, so such a row becomes permanently
invisible to the form, and the next save inserts a *second* row alongside
the orphan.

Resolved per Ryan: once "Assembly information not known as of" is filled in,
the form asks "Remove the location information for this feature?"; without
that date it explains what to do instead of reporting the assembly-change
error.

The Sequence-of-Variant check also had to be exempted from requiring a full
location once that date is set — otherwise confirming the prompt just hit
the next validation wall, which contradicts what ZFIN-10371 made those
fields manually editable for.

### 2.4 On-blur fetch went silent (`21525ef431`)

`fetchReferenceSequenceIfReady()` returned outright once the "not known as
of" date was filled in, so blurring the Location field on such a feature
issued no request at all: no reference sequence, but also no coordinate
check. The curator learned an impossible position was impossible only when
they tried to save.

The reason for the early return still holds — the reference sequence is
hand-entered in that state and must not be overwritten — so the call now
always goes out and only what it *writes* is gated.

The validation message also had to move to `Window.alert`: the inline error
label is wiped by `handleChanges()` → `clearErrors()` on the next field
event, which lands before the curator can read it.

---

## 3. The shape of the bug

One check, reached by three paths, found one user report at a time:

| Path | Reaches validation via |
|---|---|
| Save | `updateFeatureLocation` (changed location) |
| Save, location untouched | `validateReferenceSequence` backstop |
| Field blur | `getReferenceSequence` RPC |
| Field blur, "not known as of" set | same RPC, previously skipped entirely |

The check itself lives in one place. The paths that reach it kept
multiplying, and each new one surfaced from a user report rather than from
tracing callers up front. If a fifth appears, that is where to look.

---

## 4. Location removal only removes one assembly

Not a crash, and after discussion **not** changed — but the most
consequential finding.

`getLocationByFeature` (`HibernateFeatureRepository.java:511`) does
`order by fs.assembly desc` with `setMaxResults(1)`, so the form only ever
shows one assembly's location, and removal deletes only that row. Verified
against a four-assembly feature:

    FORM SHOWS:                assembly=GRCz12tu chr=17 start=20711636
    AFTER REMOVAL, FORM SHOWS: assembly=GRCz11   chr=17 start=19431607

So "remove the location" silently promotes the next assembly down. The
curator clears the fields, saves, and the form comes back populated with a
*different* location — which reads as the save having failed, or as the
application inventing data.

Multiple locations per feature are the norm: 36,733 features carry more than
one, many holding all of GRCz10, GRCz11, GRCz12tu and Zv9.

Deleting only the row the curator is looking at is the intended behaviour —
they are looking at the GRCz12tu location and asked for that one to go. What
was missing was telling them what survives. `FeatureDTO` now carries the
other assemblies, and a confirm before saving a removal says:

> This feature also has location data for Zv9, GRCz11, GRCz10. Removing the
> GRCz12tu location leaves those unchanged, and one of them will be shown
> here instead. Continue?

Note the ordering is a *string* sort, not a recency sort. It picks GRCz12tu
today only because the digit after `GRCz1` happens to increase; a
hypothetical `GRCz2` would sort above `GRCz12tu`.

---

## 5. How this was verified, and what that cost

`editFeatureDTO` commits its own transaction, so the usual
`AbstractDatabaseTest` rollback does not apply. Every scenario was driven
through the real service method against the dev-stack database as a
*throwaway* test, with the affected rows backed up and restored afterwards.
Nothing from this work is committed as a test.

Scenarios exercised: Ceri's exact form state; an in-range edit still saving;
a feature at the literal last base of chr24; a legacy row already stored out
of range; removal on a four-assembly feature.

Two process notes worth carrying forward:

- Verifying through `editFeatureDTO` only covers the save path. The on-blur
  RPC (§2.4) was missed because of this, and was reported by a user rather
  than found here.
- The dev-stack snapshot runs to **2026-06-16**. `max(feature_date_entered)`
  is *not* a proxy for snapshot age — it reflects original record creation,
  and reading it that way produced a wrong "February" claim earlier in this
  work. Anything found in the snapshot about *data* rather than *code* needs
  re-checking against production.

---

## 6. Data notes

A report of features that simultaneously have "Assembly information not
known as of" set *and* a location row (`ftr_chr_info_date is not null` joined
to `sequence_feature_chromosome_location`) returns **29 features / 50 rows**
on production. 21 of the 29 have two rows: a legacy GRCz11 one plus a
GRCz12tu one from a bulk load on 2026-07-31 (`ZDB-SFCL-260731-NNNN`).

Individually notable:

- **`u912`** (`ZDB-ALT-251231-3`) — location row with chr 24 / GRCz11 but
  **null start and end**, and a "not known as of" date of **2026-12-31**,
  in the future.
- **`fgi2`**, **`umo313`** — recent (Aug/Jul 2026), so likely live curation
  rather than legacy.

Query kept at `workbench/sql/assembly-unknown-with-location.sql`.

---

## 7. Open

- **Double dialog.** Where the curator blanks the assembly *and* the feature
  has other assembly rows, two prompts fire back to back. They say different
  things, so both were kept; easy to merge if it grates in practice.
- **`getLocationByFeature` fallback.** The `'%9%'` query calls
  `uniqueResult()` without `setMaxResults(1)`
  (`HibernateFeatureRepository.java:522`), so a feature with two
  `Zv9`-matching rows would throw `NonUniqueResultException` rather than
  picking one. Every feature currently has exactly one `Zv9` row, so it does
  not fire. Not touched.
- **a69 test data.** `ZDB-ALT-061106-2` on the `zf-10371b` stack no longer
  holds its original location; `ZDB-SFCL-200626-3` (chr 24 / GRCz11 /
  41700184) was replaced during testing by `ZDB-SFCL-260827-1` (chr 12 /
  GRCz12tu / 123456). Original values recorded here in case it needs
  restoring.
- **Jira + PR.** Neither posted; branch unpushed.
