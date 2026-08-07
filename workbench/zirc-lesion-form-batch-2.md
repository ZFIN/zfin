# ZIRC Lesion Form — Batch 2 (ZFIN-10379, 10380, 10399, 10400, 10403)

Implementation plan for the second round of ZIRC submission-form lesion
work, following ZFIN-10374–10378 (`3c43591d44`, "refine ZIRC lesion edit
form"). All five tickets touch the Lesions section of the ZIRC line
submission form; three of them hinge on shared pick-list components, so
the components come first and the tickets are mostly schema data after
that.

Companion reading: `reference/zirc-architecture.md` §3 (the
`FieldDescriptor` pattern) and §5 (the `options` vocabulary).

---

## 1. Where things stand

The lesion form is server-driven. `ZircLesionFormSchema.java` emits both
the JSON Schema and the JSON Forms uiSchema; React renderers under
`frontend/javascript/react/zirc/schemaForm/renderers/` dispatch on
`options.widget`. Three existing facts shape this batch:

- **`AutoSizeRenderer.tsx`** already derives a live bp count from a
  sibling sequence field (`options.sourceField`), mirrored server-side by
  `ZircSubmissionService#recalcLesionSizes`. Auto-calculated sizes are
  therefore already solved; what's missing is a *constrained* sequence
  input to feed them.
- **`Rule.showWhenIn`** drives every conditional reveal off `lesionType`.
  Rules are single-scope, so AND-ing two conditions (e.g. `lesionType ==
  insertion` **and** `insertionFromMutagenesis == true`) means nesting a
  ruled `Group` inside a ruled `Group`. The mechanism already supports
  this — `groupRevealedFor` is just a `Group` with a `Rule` — so no new
  machinery is needed.
- **Two tests guard every schema change.** `FormSchemaInvariantsTest`
  asserts schema leaves ↔ `FIELDS` keys ↔ DTO record components line up
  in both directions. `FormSchemaSnapshotTest` byte-diffs against
  `test/resources/zirc/snapshot/lesion.form-schema.json`.

### The vocabularies already exist in the database

All four pick lists these tickets need are rows in
`mutation_detail_controlled_vocabulary` (`mdcv`), ordered by
`mdcv_term_order` — the same table the GWT curation interface reads.

| `mdcv_used_in`                | rows today | this batch wants          |
| ----------------------------- | ---------- | ------------------------- |
| `amino_acid_term`             | Stop + 22  | unchanged — reusable as-is |
| `protein_consequence_term`    | 7          | 9 (2 new) + reorder       |
| `transcript_consequence_term` | 16         | 17 (1 new) + reorder      |
| `dna_mutation_term`           | 12         | unchanged                 |

The `amino_acid_term` rows match the ZFIN-10379 screenshot exactly
(`Stop`, then `Ala [A]` … `Val [V]`, including `Pyl [O]` and `Sec [U]`).

The reorder work in ZFIN-10399 and ZFIN-10380 is a genuine data fix, not
cosmetics: `mdcv_term_order` currently contains **ties**. Transcript
consequences have four rows at order 3, three at order 6, and three at
order 7, which is why the rendered list looks arbitrary today.

### Blocker: two new terms have no ontology backing

`mdcv_term_zdb_id` is a hard FK to `term(term_zdb_id)`:

```
"mdcv_term_zdb_id_fk" FOREIGN KEY (mdcv_term_zdb_id)
    REFERENCES term(term_zdb_id) ON UPDATE RESTRICT ON DELETE RESTRICT
```

- `inframe insertion` (ZFIN-10399) — **fine**. SO:0001821 already exists
  as `ZDB-TERM-130401-1809`. A plain `INSERT` covers it.
- `c-terminal peptide truncation` / `n-terminal peptide truncation`
  (ZFIN-10380) — **no matching SO term exists** in the `term` table. The
  nearest candidates are `polypeptide_truncation` (SO:0001617, already
  used by the existing row), `feature_truncation` (SO:0001906), and
  `sequence_variant_causing_polypeptide_truncation` (SO:1000098). None
  mean "N/C-terminal." Since `term` is populated by the OBO loads,
  hand-inserting a ZFIN-local row would be wiped by the next ontology
  load.

**Decision:** build ZFIN-10380 without the two new terms. Ship the
deletion-type reveal, the multi-select, and the reorder of the existing
seven. The two rows land in a follow-up once curators choose the
ontology terms.

---

## 2. Phase 0 — Foundation ✅ done

Commits: `789fa40c94`, `8017241ee6`, `092e1effeb`.

### 0a. Refactor `Options` ✅

Done by cherry-picking `398a908195` from the `zirc-options-lombok-with`
branch, which annotates the record with Lombok `@With` and deletes the
hand-written wither block, plus a private `@Builder` behind
`Options.of()`.

That is better than the builder-behind-the-existing-API approach this
plan originally called for: Lombok generates the withers, so the "don't
transpose two of the nine String components" invariant is enforced by the
compiler rather than by hand. The cost is that call sites read
`.withWidget(…)` instead of `.widget(…)` — 135 calls, already converted
in that commit.

Adding an option is now one line in the record header. The four this
batch needed (`vocabulary`, `alphabet`, `toField`, `positionField`) went
in as part of 0c.

### 0b. Vocabulary endpoint ✅

New `ZircVocabularyApiController` at `/api/zirc/vocabulary/{name}`,
backed by a service that reads `mdcv` through the existing
`HibernateControlledVocabularyRepository` (which already sorts by
`mdcv_term_order`).

Response shape:

```json
[{"id": "ZDB-TERM-130401-1609", "label": "polypeptide truncation", "abbreviation": null}]
```

Whitelist `{name}` against the four known `mdcv_used_in` values so the
path parameter can't be used as an arbitrary probe.

Client side: `useVocabulary` in `api/queries.ts`, caching for the life of
the page (`staleTime: Infinity`, no refetch on mount or focus) — this data
changes roughly once a year, and every lesion card on a submission would
otherwise refetch the same list.

**Ordering ties matter more than expected.** `mdcv_term_order` has
duplicates, so sorting is done through
`MutationDetailControlledVocabularyTerm.compareTo` (order, then display
name) rather than the repository's `order by order` alone. This already
pays off before any migration: amino acids have `His` and `Ile` both at
order 10, and the raw query returns `Ile` first, so the tie-break is what
makes the served list match the ZFIN-10379 screenshot exactly. The
curation interface has the same latent nondeterminism in its own lists.

**The name→`Class` map is load-bearing.**
`HibernateControlledVocabularyRepository` builds HQL by concatenating
`clazz.getSimpleName()`, so an unmapped name must never reach it. Mapping
to `Class` rather than to a string makes that structural;
`ZircVocabularyServiceTest` asserts an unknown name throws first.

**Why an endpoint rather than baking the lists into `schema()`:** it
keeps `schema()` free of database access. `FormSchemaSnapshotTest`
serializes `schema()` directly, and `LesionStatusComputer` derives
`REQUIRED_PATHS` from it in a static initializer; both would need a live
database if the vocabulary were read at schema-build time. It also keeps
ZIRC and the curation interface reading the same rows, which is what
ZFIN-10399 explicitly asks for.

### 0c. Four widgets ✅

Each lives in `schemaForm/renderers/` and is registered in
`aggregateRenderers.ts`. Nothing references them yet — the schema changes
that use them are per-ticket work in Phase 1.

**`NucleotideSequenceRenderer`** — `options.alphabet` (default `ACGTN`).
Normalizes on every change and shows a live `n bp` count. Ranks at 30, so
a Control can keep `options.multi` for its textarea shape and still land
here rather than in `TextareaRowRenderer` (20).

Two details in `nucleotides.ts` are load-bearing rather than polish, and
are unit-tested as pure functions:

- **FASTA headers are dropped as a line**, before the per-character
  filter. A description is prose and prose contains bases: `">seq1
  description here"` leaves `C`, `T`, `N` behind once everything else is
  stripped, silently prepending three junk bases to the sequence.
- **The caret is repositioned** by counting surviving characters before
  the old position. The value is controlled, so a browser puts the caret
  at the end of a programmatically-changed value, and typing a *lowercase
  base mid-sequence* changes the string. Without this the field only
  supports appending.

The existing read-only `autoSize` rows stay as they are — they display
the server's authoritative value from `recalcLesionSizes`; the inline
count is only typing feedback.

Still to do in Phase 1: apply the same normalization server-side in
`ZircLesionFormSchema` (a `nucleotides()` sibling to `text()`) so a direct
PATCH can't store what the UI would have stripped.

Fields that switch to this widget: `deletedSequence`, `insertedSequence`,
`transgeneSequence`, `fivePrimeFlank`, `threePrimeFlank`, plus the new
`crisprSequence` and `talenSequence`.

**`VocabularySelectRenderer`** — single select on `options.vocabulary`.
View mode resolves the stored id back through the vocabulary; a value the
vocabulary no longer offers still gets an `<option>` so the select can't
show a placeholder while the field holds a value.

**`VocabularyMultiSelectRenderer`** — adds one term at a time with
removable chips, rather than `<select multiple>` or a checkbox column:
the lists run to seventeen entries, a lesion normally carries one or two,
and a multiple-select loses selections to a stray click with no undo.
`diffLeaves` treats arrays as atomic leaves, so the whole array is one
PATCH — same as the `previousNames` stringList.

**`AminoAcidChangeRenderer`** — **revised from the original plan.** Binds
to the "from" field and writes two named siblings (the
`PhenotypeTimingRenderer` arrangement) rather than claiming a nested
object scope. The lesion schema is flat, and nesting the three would push
`from` / `to` / `position` into `LesionDTO` as component names —
`FormSchemaInvariantsTest` matches on leaf segments — where they would
read as belonging to nothing. Sibling names come from `options.toField` /
`options.positionField` so field names stay declared only in the schema.

**Label rule, shared by all three vocabulary widgets:** append the
abbreviation when a term has one. Yields `Ala [A]` for amino acids,
matching the curation interface, and plain labels for the consequence
vocabularies, which have no abbreviations.

### Phase 0 verification

Verified in the running app, with all four widgets temporarily wired into
the lesion schema (since reverted):

- `vocabularySelect` and both amino-acid selects populate from
  `/api/zirc/vocabulary`, with `Ala [A]` formatting.
- `vocabularyMultiSelect` adds a chip, resets its picker, and offers a
  remove link.
- `nucleotideSequence` normalizes `acgt 123 nnxx acgt` to `ACGTNNACGT`
  and reads `10 bp`, with the server's recomputed **Lesion size (bp)**
  agreeing at 10.
- Typing `gg` mid-sequence yields `ACGTGG|NNACGT` with the caret after
  the insertion — unpatched, the second `g` would be stranded at the end.

Plus: 30 unit tests on the normalization and caret helpers, typecheck,
lint, and 43 zirc Java tests with the schema snapshot unchanged.

**The screenshot check earned its keep.** All four widgets were initially
dead. They had been registered only in `aggregateRenderers.ts` — the
*view-mode* registry — while `ZircEntityEditor` kept its own
near-identical list for edit mode. A Control declaring
`widget: "vocabularySelect"` therefore fell back to `RowControlRenderer`
and rendered as a plain text input.

That failure mode is silent by construction: nothing logged, correct
payload on the wire, and a field that looks ordinary rather than broken.
Typecheck, lint and the whole Java suite passed with every widget
unreachable. Fixed in `867c9cb454` by extracting a shared `fieldRenderers`
list, so the duplication that caused it is gone rather than patched.

Note `SchemaForm.tsx` keeps a *third* renderer list for the
submission-level form. It has a genuinely different field set, so it was
left alone — but it is the same trap if a submission-level widget is ever
added.

Minor, not fixed: `options.label` is ignored by the new renderers (they
use the JSON Forms `label` prop, which comes from the schema title). The
codebase is already inconsistent here — `MultipleChoiceWithOtherRenderer`
honours it, `SelectWithOtherRenderer` doesn't. Phase 1 fields carry
proper schema titles, so it doesn't bite.

### Storage convention for multi-valued fields

Postgres `text[]` columns holding **mdcv term ZDB IDs**, not display
strings.

Precedent: `LineSubmission.previousNames` is a `String[]` over a `text[]`
column (migration `0090-zirc-previous-names-array.sql`), rendered by the
`stringList` widget.

IDs rather than labels because a display-name edit then can't orphan
stored data, and the eventual ZIRC→curation load already speaks in term
IDs. The vocabulary endpoint supplies labels at render time.

---

## 3. Phase 1 — Tickets

Migrations go in `source/org/zfin/db/postGmakePostloaddb/1199/migrations/`
(currently empty, already wired into the master changelog).

Suggested order below: broadest first, and the most likely to stall last.

### ZFIN-10399 — transcript consequence on all mutation types ✅

Done in `6e05b59a1a`. Two migrations in `1199/migrations/`: the vocabulary
reorder plus `inframe insertion`, and the `l_transcript_consequences
text[]` column. Verified end to end — endpoint order, PATCH round-trip,
and labels rendering in the form with adds persisting.

The curation-side check is still worth doing by eye: the reorder moves the
GWT feature editor's list too.

- **SQL:** renumber `transcript_consequence_term` orders into the
  ticket's 17-item sequence, which also resolves the existing ties at
  3/6/7. Plus an `INSERT` for `inframe insertion` →
  `ZDB-TERM-130401-1809` (SO:0001821).
- **Schema:** `transcriptConsequences` array, always visible, placed
  after `threePrimeFlank`. No exon/intron fields — the curation interface
  has them, this form deliberately does not.
- **Curation-side check:** this edits shared `mdcv` rows, so the GWT
  feature editor's list order changes too. That is the intended blast
  radius (the ticket confirms Holly approved it), but smoke-check it
  deliberately rather than discovering it later.

Target order:

```
premature stop, missense, frameshift, inframe deletion,
inframe insertion (new), stop loss, start loss,
3' UTR variant, 5' UTR variant, splicing variant, splice site,
cryptic splice site, cryptic donor splice site,
cryptic acceptor splice site, intron gain, exon loss, nonsynonymous
```

### ZFIN-10400 — insertion-type questions ✅

Done in `bbe78a4817`. Two notes that change the plan for ZFIN-10403:

- **Nested ruled Groups do not work.** `SectionRenderer` wraps each Group's
  children in a `<table><tbody>`, so a Group inside a Group puts a
  `<section>` inside a `<tbody>`. AND-ing two conditions uses JSON Forms'
  `{type: "AND", conditions: [...]}` instead, via `Rule.showWhenAll`. The
  plan's "AND-ing by nesting" line was wrong.
- The mutagenesis and construct questions have **separate type lists**
  (`MUTAGENESIS_ORIGIN_TYPES`, `CONSTRUCT_ORIGIN_TYPES`), so ZFIN-10403 adds
  `"indel"` to the first one only.

The server-side `nucleotides()` normalization that Phase 0 left open also
landed here, applied to the two new sequence fields; ZFIN-10403 extends it
to the existing ones.


- **Columns:** `l_insertion_from_mutagenesis`, `l_insertion_from_construct`
  (both nullable boolean), `l_crispr_sequence`, `l_talen_sequence`,
  `l_construct_name`.
- **UI:** two `yesNoRadio` controls inserted between `lesionType` and
  `insertedSequence`:
  - *Is the insertion a consequence of mutagenesis (CRISPR or TALEN)?*
  - *Is the insertion due to insertion of construct or other species DNA?*
- **Conditional reveals:** nest a `Rule.showWhenTrue` Group inside the
  existing `groupRevealedFor(INSERTED_SEQ_TYPES, …)` Group — AND-ing by
  nesting. CRISPR yes → CRISPR + TALEN sequence boxes
  (`nucleotideSequence` widget). Construct yes → construct name (plain
  text).
- **"One is required to be answered":** status badge, see §4.

### ZFIN-10403 — indel format and input boxes ✅

Done in `7993c9299c`. Reuses ZFIN-10400's CRISPR/TALEN block.

Two readings resolved in favour of the mockup:

- **"Remove Lesion size box"** is a rename, not a deletion. The mockup keeps
  the value and calls it "Deletion size (bp)", opposite "Insertion size
  (bp)". Implemented as an `options.label` override on the same
  `lesionSizeBp` property — nothing changes about what is stored or derived.
  Needed `AutoSizeRenderer` to start honouring `options.label`.
- **Deletion keeps "Lesion size"**, so the two types now differ in naming.
  Scoped that way because the ticket is indel-only, but it is worth a
  curator's eye.

The consequences field needed no work — ZFIN-10399 already put it on every
lesion type.

- Drop `indel` from the `lesionSizeBp` reveal list (removes the "Lesion
  size" box). Deletion and insertion sizes already auto-calculate for
  indel in `recalcLesionSizes`, so that half is free.
- Add the CRISPR/TALEN question and its two sequence boxes.
- Add `transcriptConsequences` from ZFIN-10399.
- Sequence inputs move to the `nucleotideSequence` widget, which covers
  the ticket's "validate the sequence input is DNA." The Controls are
  shared across lesion types, so deletion / insertion / transgene get the
  constraint too — intended, not spillover.

**Layout note:** the mockup shows sequence and size side by side in two
columns. The current renderers are a label/value `<tr>` table with the
size stacked directly under the sequence it measures — which is what
ZFIN-10374–10378 deliberately shipped. Treating the mockup's columns as
illustrative and keeping stacked rows, unless the layout rework is
wanted as real scope.

### ZFIN-10379 — mutated amino acids pick list

- **Columns:** `l_aa_change_from`, `l_aa_change_to`,
  `l_aa_position_start`.
- **UI:** the `aminoAcidChange` composite replaces the
  `mutatedAminoAcids` free-text control. `mutatedAminoAcidsHgvs` becomes
  derived and read-only.
- **Legacy data:** the old free-text `l_mutated_amino_acids` column stays
  in place, unread by the form — the same treatment `locationInline`
  already has in the invariants-test whitelist.
- **"Either nucleotide or amino acid info":** status badge, see §4.

### ZFIN-10380 — mutation consequence for deletion

- Add `deletion` to `PROTEIN_TYPES`.
- `proteinConsequences` multi-select (multiple selections enabled).
- **SQL:** renumber the existing seven `protein_consequence_term` rows
  into the ticket's relative order.
- **Deferred:** `c-terminal peptide truncation` and `n-terminal peptide
  truncation`. Leave a comment in the migration naming ZFIN-10380 and the
  missing SO terms so the follow-up is discoverable.

Target order (bracketed entries deferred):

```
polypeptide truncation
[c-terminal peptide truncation]   ← deferred, no SO term
[n-terminal peptide truncation]   ← deferred, no SO term
amino acid substitution
amino acid deletion
amino acid insertion
non conservative amino acid substitution
elongated polypeptide
polypeptide fusion
```

---

## 4. The "visual indicator" for either/or requirements

Both ZFIN-10379 ("nucleotide info **or** amino acid info") and
ZFIN-10400 ("one of the two questions must be answered") ask for a
placeholder indicator, not working validation — the tickets say so
explicitly, so that real validation has something to test against later.

`LesionStatusComputer` already renders `MISSING` badges through
`StatusBadge`. Its `REQUIRED_PATHS` set is collected from the schema's
`required` lists, which can only express per-field requirements. Add a
small cross-field hook in `statusFor` for these two group-requirements.

This reuses the shipped badge UI, and gives the eventual validation work
a defined thing to replace rather than a parallel mechanism to reconcile.

---

## 5. Test obligations for every schema change

All three, or CI fails:

1. Add the new path to `ZircLesionFormSchema.FIELDS` **and** to
   `LesionDTO`. `FormSchemaInvariantsTest` asserts both directions —
   a schema leaf with no DTO component 500s on PATCH; a DTO component
   with no schema entry silently round-trips past the form.
2. Add the field to `LesionStatusComputer.Field`.
3. Refresh the snapshot and read the diff before committing:

```
zrun -c "gradle test -Dzirc.snapshot.update=true \
    --tests org.zfin.zirc.api.FormSchemaSnapshotTest"
```

Server-computed fields (`lesionSizeBp`, `insertionSizeBp`) stay out of
`FIELDS` and are listed in the invariants test's `schemaExempt`
whitelist. Any new derived field (e.g. `mutatedAminoAcidsHgvs`, once it
becomes derived) belongs there too.

---

## 6. Decisions taken

| Question | Decision |
| --- | --- |
| Vocabulary source | New `/api/zirc/vocabulary/{name}` endpoint reading `mdcv`. Keeps `schema()` DB-free and keeps ZIRC in sync with curation. |
| ZFIN-10380's two new terms | Build the ticket without them; follow up once curators supply SO terms. |
| Nucleotide alphabet | `ACGT` + `N`. |
| Invalid characters | Strip silently, count what remains. Matches how `AutoSizeRenderer` already counts (tallies `[A-Za-z]` only). |
| Multi-value storage | `text[]` of mdcv term ZDB IDs. |
| Amino acid change wiring | Bound to the "from" field writing named siblings, not a nested object scope (revised during Phase 0 — nesting would put `from`/`to`/`position` into `LesionDTO` as bare component names). |
| Position input | **Start–end range** (revised). The ticket text says "position input box" singular but its screenshot shows a start–end pair, and the ticket asks for something "similar to the curation interface". End is nullable, so a single-residue change is a start with an empty end — which satisfies the literal reading too. A single value could not express an in-frame deletion spanning residues without falling back to free text. Settled before ZFIN-10379 so `l_aa_position_end` lands in the same migration. |
| Amino acid change cardinality | One per lesion, not repeatable. Only the consequence lists are explicitly multi-valued (ZFIN-10380, ZFIN-10399). |
| Construct question scope | Insertion only. The ZFIN-10403 mockup shows only the CRISPR/TALEN question for indel. |
| Two-column layout | Not adopted; keeping the stacked rows from ZFIN-10374–10378. |

## 7. Open items

- SO terms for `c-terminal peptide truncation` and `n-terminal peptide
  truncation` (blocks the remainder of ZFIN-10380).
- The curation screenshot also shows two small boxes labelled `aa` (counts
  of residues removed / added). The ticket does not mention them and they
  are not being reproduced.
- Decide whether the ZFIN-10403 two-column layout is wanted as real
  scope.
