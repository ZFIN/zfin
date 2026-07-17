# ZIRC line submission editor — architecture (2026-05-13)

## What this is

The ZIRC line submission editor (ticket ZFIN-10265) is a React-based form
for curators submitting a new fish line to ZIRC. A submission has nested
mutations; each mutation has nested genes, lesions, genotyping assays,
phenotypes, and publications; and the submission as a whole has linked-
feature pairs across its mutations. Each subform exposes type-conditional
fields (a Deletion lesion has different inputs than an Insertion, an
RFLP assay has different inputs than a KASP), all of which save
optimistically as the curator types.

The first cut shipped as one hand-rolled React container per page with
JSX branches for every type/visibility/field combination. That worked
but accreted into a single file no one wanted to touch. This branch
rewrites the editor as a *schema-driven* renderer: the form is a tree of
declarative nodes, and a generic `FormRenderer` walks that tree. Adding
a new field or moving one between types is now a single-line edit to
either the schema or the type matrix.

## High-level shape

```
LineSubmissionEdit (container)        MutationEdit (container)
  │                                     │
  ├── useAutosavedForm<DTO>             ├── useAutosavedForm<DTO>
  │     load / save / commit /          │     load / save / commit /
  │     setDto / saveEvent              │     setDto / saveEvent
  │                                     │
  └── <FormRenderer                     └── <FormRenderer
        schema={LINE_SUBMISSION_SCHEMA}        schema={MUTATION_EDIT_SCHEMA}
        value={dto}                            value={dto}
        onCommit={form.commit}                 onCommit={form.commit}
        actions={{ … }} />                     actions={{ … }} />
                                                       ▲
   ┌───────────────────────────────────────────────────┘
   │
   FormRenderer (recursive)
   │
   ├── SectionNode  → <section> + heading + child table
   ├── ArrayNode    → repeating fieldsets w/ type-picker, collapse, summary
   ├── FieldNode    → one labeled input (text/textarea/int/bool/select/…)
   └── CustomNode   → schema escape hatch (file uploader, phenotype timing,
                       publications list, mutations table)
```

Server side mirrors the same shape: one unified `POST /patch` endpoint
per page receives `{path, value}` and dispatches into a small set of
`save*()` methods on `LineSubmissionService`.

## File layout

```
frontend/javascript/react/
  components/form-renderer/
    types.ts              # FormNode union: Section / Array / Field / Custom
    builders.ts           # tiny ergonomic factories (section/array/field/custom)
    FormRenderer.tsx      # the recursive renderer
    useAutosavedForm.ts   # hook: optimistic state + save queue + dtoRef
    Autocomplete.tsx      # async picker reused by autocomplete fields
  components/zirc/
    typeMatrices.ts       # SOURCE OF TRUTH: which fields each type shows
    PhenotypeTimingRow.tsx
    ZircAssayFileUpload.tsx
    MutationsSection.tsx
    SaveToast.tsx
    FormPrimitives.tsx
  containers/zirc/
    lineSubmissionSchema.tsx   # tree of FormNodes for line_submission edit
    mutationEditSchema.tsx     # tree of FormNodes for mutation edit
  containers/
    LineSubmissionEdit.tsx     # slim container wiring schema + autosave
    MutationEdit.tsx           # ditto for the mutation page

source/org/zfin/zirc/
  entity/        # JPA entities (LineSubmission, Mutation, Lesion, …)
  presentation/  # DTOs + the ZircDashboardController
  service/       # LineSubmissionService: save* methods + applyPatch dispatch

source/org/zfin/db/postGmakePostloaddb/1182/migrations/
  0050-ZFIN-10162-zirc-line-submission.sql   # single consolidated CREATE-script

tests/e2e/
  fixtures/auth.ts
  line-submission.spec.ts
  mutation-edit.spec.ts
```

## Schema-driven rendering

`FormNode` is a discriminated union:

- **SectionNode** — `{id, title, children}`, becomes a `<section>` with
  a heading and a `<table>` body. Sections are the JSP nav anchors.
- **ArrayNode** — `{path, newRow, childTemplate, collapseWhen, summarize,
  addRequiresTypePick?, maxItems}`. Renders a list of rows; each row is
  the `childTemplate` re-instantiated against that row's data. Collapsed
  rows show only a one-line summary. The optional type-picker modal
  fires before `newRow` so the first thing the row stores is its type.
- **FieldNode** — one labeled input. `type` is one of `text | textarea |
  int | bool | checkbox | readonly | select | autocomplete |
  select-with-other | multi-checkbox-with-other`. A `visible(dto, row)`
  predicate gates display per row.
- **CustomNode** — `{id, render({dto, row, onChange, onCommit, actions})}`
  is the escape hatch. Used where a generic field doesn't fit: file
  upload widgets, the phenotype hpf/dpf timing row, the publications
  list (a flat string[]; doesn't fit ArrayNode's object-row model yet),
  and the mutations summary table on the line submission page.

The renderer's `RenderCtx` carries `inTableRow` so a CustomNode inside
an ArrayNode renders into `<tr><td colSpan={2}>` instead of plain divs
— necessary because the row layout is a `<table>`.

### Schema example

```ts
section('overview', 'Overview', [
    field({path: 'zdbID',         label: 'ID',          type: 'readonly'}),
    field({path: 'name',          label: 'Name',        type: 'text'}),
    field({path: 'previousNames', label: 'Previous Names', type: 'text'}),
]),

section('lesions', 'Lesions', [
    array({
        path: 'lesions',
        newRow: emptyLesionRow,
        addRequiresTypePick: { options: LESION_TYPE_OPTIONS, targetPath: 'lesionType' },
        collapseWhen: row => !!row.lesionType,
        summarize: summarizeLesion,
        childTemplate: [
            field({path: 'lesionType', label: 'Lesion type', type: 'select', options: …}),
            lesionField('lesionSizeBp', {exceptForTypes: ['indel']}),
            lesionField('lesionSizeBp', {onlyForTypes: ['indel'], labelOverride: 'Deletion size'}),
            lesionField('insertionSizeBp'),
            lesionField('deletedSequence'),
            // …
        ],
    }),
]),
```

## Type matrices

`frontend/javascript/react/components/zirc/typeMatrices.ts` is the single
source of truth for "which fields does this type show?". Each lesion
type and each assay type names its field set:

```ts
BASE_LESION_FIELDS_BY_TYPE = {
    point_mutation: ['lesionSizeBp', 'nucleotideChange', 'locationInline', …],
    deletion:       ['lesionSizeBp', 'deletedSequence',  'locationInline',
                     'hasLargeVariant', …],
    indel:          ['lesionSizeBp', 'insertionSizeBp',
                     'deletedSequence', 'insertedSequence',
                     'locationInline', 'hasLargeVariant', …],
    transgene:      ['lesionSizeBp', 'transgeneSequence', '5primeFlank',
                     '3primeFlank', …],
    other:          ['mutatedAminoAcids', 'mutatedAminoAcidsHgvs',
                     'additionalInfo'],
    unknown:        ['additionalInfo'],
}
```

`visibleLesionFields(type, hasLargeVariant)` returns the field list,
splicing the `5primeFlank/3primeFlank` rows in when `hasLargeVariant`
is true (the two-axis visibility case). The schema's
`lesionFieldVisible(key)` predicate calls this for each rendered
FieldNode.

Same shape exists for genotyping assays (`ASSAY_FIELDS_BY_TYPE`,
`visibleAssayFields(type)`), but the assay axis is one-dimensional —
the type alone determines the fields.

Field metadata (label, type, placeholder, helpText, info link, suffix
like `bp`) lives in `LESION_FIELD_DEFS` / `ASSAY_FIELD_DEFS`. Adding a
new field is: add the key to the union, add a row to `*_FIELD_DEFS`,
add it to one or more type matrices, add the DB column and entity/DTO
field — and it shows up in the renderer with zero JSX changes.

## Autosave

`useAutosavedForm<TDto>` provides the optimistic-state plumbing:

- `dto` — current local state (drives the UI).
- `setDto(next)` — replaces local state (used by CustomNodes after a
  server-roundtrip response, e.g. file upload).
- `commit(path, value)` — sets the field locally, then queues a save
  call (`save(id, dto, path, value)`). The hook serializes saves through
  a queue so a fast-typing curator doesn't fan out parallel writes.
- `saveEvent` — exposed for `<SaveToast>` ("saved Name", "saving…").
- `loadError`, `loading` — load-side status.

The hook keeps a `dtoRef` so the save callback always sees the latest
state (avoids stale closures when a curator triggers two commits back-
to-back). It also accepts an `idPath` option so the first save of a new
row writes back the assigned id, and subsequent saves go to the right
endpoint.

The save callback is the per-container plumbing — both containers
funnel everything through one `POST /patch` endpoint per page. The
service-side dispatch (`applyPatch` / `applyMutationPatch`) inspects
`path` to decide whether to update a scalar, replace a child list, or
recompute a server-derived field.

## DB design

All ZIRC tables live in their own `zirc` schema. Per-column prefix
(`ls_`, `m_`, `l_`, `g_`, …) makes joins readable.

Internal FKs (within the submission tree) are `ON DELETE CASCADE`:
deleting a submission removes its mutations, lesions, assays,
phenotypes, publications, and linked-feature pairs in one shot. FKs out
to `person` and `marker` are `RESTRICT`.

A couple of patterns worth calling out:

- **ZDB-ID generation**. `ls_zdb_id` is a ZFIN-style ZDB ID, generated
  via the standard `org.zfin.database.ZdbIdGenerator`. Wiring needs a
  row in `zdb_object_type` *and* a `linesubmission_seq` sequence — both
  set up at the bottom of the consolidated migration. Without either,
  `get_id('LINESUBMISSION')` fails at the SQL level and the controller
  returns an empty 200 (the global exception handler swallows the
  details).
- **Soft-delete + draft state**. `ls_is_draft`, `ls_submitted_at`, and
  `ls_deleted_at` capture the submission lifecycle without a separate
  status table. New submissions start `is_draft = TRUE`.
- **Array columns instead of junction tables**. `ls_reasons TEXT[]`,
  `ga_enzyme_cleaves TEXT[]`, `p_segregation TEXT[]`, `p_type TEXT[]`
  — small, read-mostly multi-select sets get stored as Postgres arrays
  rather than a join table. Matches the form's "multi-checkbox-with-
  other" wire shape.
- **Auto-updated timestamp**. `ls_updated_at` is maintained by a
  BEFORE-UPDATE trigger so the application doesn't have to remember to
  set it.
- **Linked-feature normalized pairs**. `lslf_normalized_pair CHECK
  (lslf_mutation_a_id < lslf_mutation_b_id)` enforces that
  `(mutation_5, mutation_3)` and `(mutation_3, mutation_5)` can't both
  exist as separate edges. The service sorts on insert.
- **File uploads**. `zirc.genotyping_assay_file` is the only "owned by
  one parent type" file table — phenotype images / chromosome diagrams
  will get their own siblings when needed, *not* a polymorphic
  owner-type+id pair.

### Migration consolidation

The branch landed across 19 migrations (0050..0230) as the spec evolved.
Before merge those were collapsed into a single
`0050-ZFIN-10162-zirc-line-submission.sql` that creates the final shape
directly — none of the intermediate states ever existed outside dev, so
the ALTER trail was pure noise. New work after merge starts at the next
free four-digit token.

## Tricky areas

These are the spots that bit us during this build and will probably bite
the next contributor too.

### Per-type field labels

`lesionSizeBp` reads as "Lesion size" by default, but on Indel rows the
form needs to show "Deletion size" alongside the new "Insertion size"
field — otherwise the two integer rows are indistinguishable.

Resolution: `lesionField()` takes optional `onlyForTypes` /
`exceptForTypes` / `labelOverride` so the schema emits two FieldNodes
pointing at the same path with different labels and complementary
visibility predicates. Cheap to do per-field, but doesn't generalize —
see *Future improvements* below.

### CustomNode inside a table

The default ArrayNode child template renders into `<tr><th><td>` rows.
A CustomNode emitting raw `<div>` content used to produce a
`<p> in <tbody>` DOM-nesting warning. Fixed by threading `inTableRow`
through `RenderCtx`: in a table context, `CustomView` wraps its render
output in `<tr><td colSpan={2}>` automatically.

### Stale DTO closures

`useAutosavedForm`'s save callback receives the DTO at the moment of
commit. If two commits fire back-to-back (e.g. blur fires after another
input takes focus), the second commit's save callback would see the
*pre-first-commit* DTO unless we kept a ref. The hook stores a `dtoRef`
and reads it inside the save queue — straightforward, easy to forget if
you reimplement the hook.

### First-save id refresh

A new submission has no `zdbID` until the first save returns one.
`useAutosavedForm`'s `idPath` option (default `'zdbID'`) tells the hook
to copy the assigned id from the server response into local state, so
the URL stays stable, the Overview "ID" cell updates, and the next save
goes to the correct endpoint.

### Publications: onChange vs onCommit

The publications CustomNode lets the curator add a new empty row and
type into it. The natural impulse is to send the new `[…pubs, '']`
state to the server immediately. But the server's `savePublications`
filters blanks (to drop accidentally-left-behind empty rows), so the
empty new row was getting stripped from the response and the local
state visibly snapped back to "No publications recorded."

Fix: add-row uses `onChange` (local only); the actual save fires via
`onCommit` only on blur (with `trim().filter(Boolean)` applied
client-side too). Counterintuitive but consistent — the same pattern
will surface for any "type-into-a-blank-row" list.

### Gene picker autocomplete fallback

The gene autocomplete writes `mutatedGeneZdbId` *and*
`mutatedGeneAbbreviation` into the row when the curator picks a match.
Saving via PATCH sends both; the server's `saveGenes` then resolves the
Marker from `zdbId`, falling back to `abbreviation` when `zdbId` is
blank (which happens during the brief autocomplete state where text has
been typed but no selection committed yet).

### New rows stay expanded

Initially, `ArrayNode.collapseWhen` collapsed a row as soon as it had
content, which meant the curator typed one character into a fresh
phenotype description and watched the whole row collapse. Fix:
`appendRow` flags the newly-added row as `explicitlyExpanded` until
the user actively clicks "Done." The collapse predicate is consulted
*only* for rows the user hasn't touched in this session.

### Schema/JSP drift

The sidebar navigation is rendered by the JSP from a hand-written
`sections` list, while the schema controls the actual section order
inside the form. They have to match. The e2e spec
`mutation-edit.spec.ts` asserts the nav order (`Lethality` directly
before `Publications`, etc.) as a drift backstop — if you reorder
sections in the schema, you also have to update the JSP and that test.

### Two-axis visibility (`hasLargeVariant`)

Lesion field visibility is *almost* purely a function of `lesionType`,
but `hasLargeVariant` further splices in the structured 5'/3' flank
rows for Deletion/Insertion/Indel large variants. Handled in
`visibleLesionFields(type, hasLargeVariant)` rather than a flag on each
field def — the splice is more local to read.

### Name-uniqueness autosave hostility

The original `ls_name UNIQUE` constraint surfaced as a 500 mid-typing
whenever a curator typed a name that collided with an existing one.
Constraint dropped. The form's autosave still races on the *PK*
(zdb_id), which is fine because it's server-generated, but any future
attempt to constrain a user-typed scalar needs to think hard about how
mid-typing collisions surface — there's no debouncing on commit.

### Hibernate + Postgres array columns

`ls_reasons TEXT[]` etc. are mapped via the standard
`UserType<String[]>`/`@JdbcTypeCode(Types.ARRAY)` patterns; works fine
but the type wiring is non-obvious and Hibernate 6's array support is
still maturing. If we add more array columns, factor a common base.

## Future improvements

- **First-class per-type labels.** The `onlyForTypes` / `labelOverride`
  trick keeps the FieldNode API as a plain string but forces the schema
  to emit duplicate nodes for any field whose label varies by type. A
  cleaner API would let `label` be `string | ((row) => string)` and
  resolve it in the renderer, eliminating the gated duplicates. The
  current pattern is fine for two-or-three cases but would clutter the
  schema if many fields needed it.

- **Make ArrayNode handle primitive arrays.** Publications (`string[]`)
  doesn't fit the object-row model and is implemented as a CustomNode.
  An `ArrayNode<T extends string | number>` variant — same controls
  (Add / Remove / reorder) but a single primitive value per row —
  would let the publications list drop out of CustomNode and gain
  consistent UX with the object-row arrays.

- **Server-side schema reflection / validation.** The type matrix
  enumerates which fields belong to which lesion / assay type, but the
  server doesn't consult it. A POSTed PATCH for `lesionSizeBp` against
  a row whose `lesionType = 'other'` is silently accepted today. Either
  share the matrix between client and server (export to JSON consumed
  by both), or have the service validate against the matrix on save.

- **Generalize the "Other" pattern.** `select-with-other` and
  `multi-checkbox-with-other` are duplicated wire patterns
  (`x` + `xOther`). A standardized `Pair<canonical, otherText?>` shape
  would let `select-with-other` and `multi-checkbox-with-other` share
  one wire type and one renderer.

- **More e2e coverage.** Current specs cover the sidebar nav order, the
  type-picker modal, type-driven field visibility (Deletion has these
  fields, RFLP has these checkboxes), and the hasLargeVariant flank
  reveal. Saves are exercised implicitly via the autosave round-trip
  but not directly asserted; a "type into Name, reload, confirm Name
  persisted" path would catch full-stack PATCH regressions cheaply.

- **`runOnChange` strategy for the consolidated migration.** The
  consolidated 0050 migration creates the final schema; if it ever
  ships and then needs a structural change, the existing changeset
  signature in `databasechangelog` will conflict. Decide ahead of time
  whether new schema work after merge lands as fresh migrations (the
  obvious choice) or whether we ever re-collapse, and document the
  policy. The 18-step consolidation we did here was a pre-merge
  one-shot — never repeat it on a deployed schema.

- **Linked-feature pair ordering in the service.** The `a < b` CHECK
  is enforced by Postgres; the service silently swaps the two IDs
  before insert so the curator never sees a constraint error. That
  swap currently lives inline in `saveLinkedFeatures`; a tiny helper
  or test would make the invariant easier to spot. Same applies to the
  client-side backstop in `LineSubmissionEdit.tsx` that drops rows
  with `a == b`.

- **Phenotype hpf/dpf timing widget reuse.** `PhenotypeTimingRow` is a
  bespoke CustomNode because the dpf↔hpf conversion + stage lookup is
  too specific for a generic field type. If another section ever
  surfaces a similar "value + unit + derived display" pattern, factor
  a shared `UnitConvertedNumberRow` rather than duplicating the
  custom-node code.
