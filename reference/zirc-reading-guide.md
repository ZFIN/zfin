# ZIRC Submission — Reading Guide

A curated path through the ZIRC line-submission codebase for someone
coming up to speed. Don't read the code top-down by directory — read it
in the order below, where each step makes the next one easier.

There are three companion docs in `reference/`:

- **`zirc-architecture.md`** — the contract. What conventions hold, where
  things live. Reference material; you'll return to it.
- **`zirc-rearchitect-retrospective.html`** — the story. Why we made the
  bets we did, what we tried that didn't work, what the alternative on
  `zfin-10265-zirc-line-submission-react-re-architect-squash` looks like.
- **`zirc-openapi-approach.md`** — historical: the original decision to
  hand-curate an OpenAPI YAML, plus the reasoning. The actual artifact
  has been removed (a per-feature OpenAPI surface in an
  otherwise-undocumented codebase was a maintenance asymmetry) and
  this file now reads as a "postponed; return-to-this triggers" note.

This file is meant to *order* those docs and the source code into a
reading sequence.

---

## 15-minute tour: the heart of it

If you only have 15 minutes, read these three files in this order. They
contain the whole architectural idea; everything else is plumbing.

1. **`reference/zirc-architecture.md` §1–3** — three short sections that
   cover the mental model, the seven aggregates, and the
   `FieldDescriptor` pattern. ~10 min.

2. **`source/org/zfin/zirc/api/ZircFormSchema.java`** — the single source
   of truth for the submission form. Read it top to bottom. ~600 lines
   but mostly data; the `FIELDS` map at the bottom carries the whole
   contract. Notice:
   - `schema()` produces JSON Schema, built from typed records under
     `api/jsonschema` (`StringSchema.of`, `BooleanSchema.nullable`,
     `NumberSchema`, `ArraySchema`, `ObjectSchema`)
   - `uiSchema()` produces JSON Forms uiSchema, built from typed records
     under `api/uischema` (`VerticalLayout`, `Group`, `Control`,
     `Options`, `Rule`)
   - `FIELDS` is the path → (read, write) dispatch table

3. **`home/javascript/react/zirc/schemaForm/SchemaForm.tsx`** — the
   React side. The whole client form is ~240 lines. Notice:
   - Fetches the schema from the server via React Query
   - The `diffLeaves` function (lines ~95–121) — recursively walks
     two form-data trees and emits one entry per changed leaf
   - The autosave `useEffect` that debounces and fires one PATCH
     per change

That's it. Everything else in this codebase is "the same pattern but for
mutations" or "the same pattern but for assays" or "polish on top."

---

## One-hour deep tour

If you can spend an hour, walk the code in this order. Each step builds
on the prior one.

### Step 1 — Read the retrospective (10 min)

`reference/zirc-rearchitect-retrospective.html`. Focus on:
- "Where we started" (clarifies what was already there at branch creation)
- "TL;DR" (five-bullet summary of the architectural bets)
- The "Head-to-head: this branch vs ZFIN-10265" section to understand
  what's distinctive about our approach

Skip the pitfalls section for now — you'll appreciate it more after
seeing the code.

### Step 2 — Backend, in the order data flows (25 min)

Start at the entity, work outward to the controller. Read these files in
order:

1. **`source/org/zfin/zirc/entity/LineSubmission.java`** — Lombok
   `@Getter @Setter` Hibernate entity. Mostly columns. Notice
   `@DynamicUpdate` and the column-default boilerplate.

2. **`source/org/zfin/zirc/dto/LineSubmissionDTO.java`** — Java
   record. The `of(entity)` static factory is the only thing here;
   read it to see how nested children (`mutations`) map.

3. **`source/org/zfin/zirc/api/ZircFormSchema.java`** — already covered
   above; re-skim if needed. This is the file you'll come back to most
   often as you work.

4. **`source/org/zfin/zirc/api/ZircSubmissionApiController.java`** —
   thin. Just glue from HTTP to `ZircSubmissionService`. Read all of it.

5. **`source/org/zfin/zirc/service/ZircSubmissionService.java`** — the
   only mutator. Focus on:
   - `updateField` — the central PATCH method. Once you've read this,
     `updateMutationField` and `updateAssayField` are identical
     shapes for the other two aggregates.
   - `writeAudit` — the audit trail helper. Inside-the-transaction
     insert, with backwards-compatible log4j line.
   - `storeAttachment` — the multipart-upload path. The
     `sanitizeFilename` helper sits next to it.

6. **`source/org/zfin/zirc/api/ZircApiExceptionHandler.java`** — small.
   The `@Order(HIGHEST_PRECEDENCE)` is the only subtle bit. Three
   handlers map to 404 / 400 / 422 ProblemDetail responses.

### Step 3 — Frontend, in the order React mounts (15 min)

1. **`home/javascript/react/zirc/api/types.ts`** — TypeScript mirrors
   of the Java DTO records. Hand-typed, not generated. Source of truth
   on the wire.

2. **`home/javascript/react/zirc/api/client.ts`** — ~50 lines. The
   `api` object has `get` / `post` / `patch` / `delete` / `upload`
   methods. Notice the `FormData` detection in `request()` so the
   browser sets the multipart boundary itself.

3. **`home/javascript/react/zirc/api/queries.ts`** — React Query hooks
   (`useLineSubmission`, `useAddMutation`, `useUploadAttachment`,
   etc.). Each is ~10 lines.

4. **`home/javascript/react/zirc/schemaForm/SchemaForm.tsx`** — covered
   above. Re-read with the API client in mind now.

5. **`home/javascript/react/zirc/schemaForm/renderers/`** — fifteen
   custom JSON Forms renderers. Don't read all of them; pick:
   - `RowControlRenderer.tsx` — the workhorse table-row Control for
     string fields. Shows the `options` vocabulary
     (placeholder/helpText/infoHref/suffix).
   - `SectionRenderer.tsx` — the layout renderer for Groups. Note the
     `visible === false` early return; without it, group-level uiSchema
     rules silently don't apply.
   - `MutationsListRenderer.tsx` — the pattern for server-managed list
     widgets with `maxItems` caps.
   - `LesionsListRenderer.tsx` (or `GenesListRenderer` /
     `PhenotypesListRenderer`) — the same pattern applied to
     per-mutation children with inline-expand cards.
   - `AutocompleteRenderer.tsx` — type-ahead ZDB-ID resolver used by
     M5.3 (linked features), M6.1 (gene → marker), M7.1 (lesion).
   - `PhenotypeTimingRenderer.tsx` — escape-hatch for the hpf/dpf
     unit toggle that needs sibling-field access via `useJsonForms`.

6. **`home/javascript/react/zirc/schemaForm/useAutosavedSchemaForm.ts`**
   — the autosave state machine (schema fetch → null-gated seed →
   mirror-sync → debounced PATCH) shared by every aggregate editor. It
   derives its skip-set / mirror-keys / parent-refresh from the
   uiSchema's per-Control flags. Read this to understand the autosave
   model in one place.

7. **`home/javascript/react/zirc/schemaForm/ZircEntityEditor.tsx`** +
   **`aggregateRegistry.ts`** — one component that renders any inline
   aggregate using the hook. The per-aggregate page files (`AssayEdit`,
   `GeneEdit`, `LesionEdit`, `PhenotypeEdit`) are ~25-line wrappers over
   it. `MutationEdit.tsx` is the standalone page that uses the hook
   directly (it hosts the four inline-expand child sections).

### Step 4 — Pitfalls section of the retrospective (10 min)

Now go back to `zirc-rearchitect-retrospective.html` and read the
"Pitfalls and how we resolved them" section. You'll recognize all the
files involved and the descriptions will land.

### Step 5 — The architecture doc end-to-end (10 min)

`reference/zirc-architecture.md` §4–16. Sections 14–16 are the
practical bits:
- §14 — directory layout
- §15 — gotchas (read these; each one comes from a real bug)
- §16 — the "adding a new field" checklist

---

## Contributor path: "I need to make a change"

### "Add a new field to an existing aggregate"

Follow the checklist in `zirc-architecture.md` §16. Seven files, in
order: DB migration → entity → DTO → `FIELDS` map → schema → uiSchema →
TS type. `FormSchemaSnapshotTest` flags drift on the form-schema
output; regenerate via `-Pzirc.snapshot.update=true` and review the
diff before committing.

### "Add a new aggregate (new child entity under Mutation)"

Mirror the existing inline-expand pattern. The skeleton is:

1. DB migration: new table with FK to `zirc.mutation`
2. New entity in `source/org/zfin/zirc/entity/` (with `@DynamicUpdate`)
3. New full-record DTO + summary DTO in `source/org/zfin/zirc/dto/`
   (summary if the aggregate has many fields and the list card only
   shows a header; full record is fine for small aggregates like
   `GeneDTO`)
4. Add the entity to `hibernate.cfg.xml`
5. Add `phenotypes`/`lesions`/etc. summary list to `MutationDTO`
6. Service methods: `getRequired*ById`, `add*`, `delete*`, `update*Field`,
   `next*SortOrder` (mirror the methods in `ZircSubmissionService`
   under "Lesions (M7.1)" or "Phenotypes (M8.1)")
7. Repository: `get*` interface + Hibernate impl
8. New `Zirc*FormSchema` class with schema/uiSchema/FIELDS — schemas
   are typed records under `org.zfin.zirc.api.jsonschema` and
   `org.zfin.zirc.api.uischema`, **not** `Map<String, Object>`
9. New `Zirc*ApiController` with `/form-schema`, `/{id}`, PATCH, POST, DELETE
10. Wire summary list into the parent's `ZircMutationFormSchema`
    (`*SummaryArrayProp()` helper + a uiSchema Group with
    `widget: "<aggregate>List"`)
11. New React Query hooks + cache key in `api/queries.ts`
    (`use<Kind>ById`, `<kind>Key`)
12. For an inline-expand child aggregate: add an entry to
    `schemaForm/aggregateRegistry.ts` (endpoints, query hook, cache key,
    parent topology) and write a ~25-line page wrapper that renders
    `<ZircEntityEditor kind="<kind>" …>`. Flag the parent's summary-list
    Control `managesOwnPersistence` and the parent-card-label field
    `refreshesParent` in the form schema — the hook reads both from the
    uiSchema, so there's no per-page `EXTERNALLY_MANAGED_PATHS` or
    mirror-sync effect to wire. If the aggregate gets its own *route*
    instead, follow the `MutationEdit.tsx` shape (uses the hook directly).
13. If the aggregate's form needs a widget `ZircEntityEditor` doesn't
    already register, add the renderer to its union.
14. Add a `*SchemaMatchesSnapshot` test in `FormSchemaSnapshotTest` and a
    `Spec` row in `FormSchemaInvariantsTest`
15. Regenerate snapshots: `gradle test --tests org.zfin.zirc.api.FormSchemaSnapshotTest -Pzirc.snapshot.update=true`

The M6.1 → M7.1 → M8.1 commit chain on `zirc-rearchitect` is the
canonical worked example — three nearly-identical aggregate adds in a
row, each ~1000 lines, mostly copy-paste:

```
b47e34441  M7.1: Lesions per mutation (inline cards with lesion-type matrix)
517c7a136  M8.1: Phenotypes per mutation (inline cards with hpf/dpf timing widget)
76550adbc  M6.1: Genes per mutation (inline cards with marker autocomplete)
```

(M4.1–M4.3 — `af40bb58c6` / `f16ef7ed89` / `0b77dcdf5f` — are the
original assay-aggregate pattern, predating typed records.)

### "Change a conditional reveal rule"

uiSchema `rule` blocks are in the `*FormSchema.java` files. Two
patterns to copy:

- Boolean: `Map.of("effect", "SHOW", "condition", Map.of("scope",
  "#/properties/X", "schema", Map.of("const", true)))`
- Enum membership: same shape but
  `"schema", Map.of("enum", List.of("foo", "bar"))`

For Group-level rules, remember `SectionRenderer` honors `props.visible`.
For Control-level rules, JSON Forms handles it automatically.

### "Add a new widget type"

1. Write the renderer under `home/javascript/react/zirc/schemaForm/renderers/`
2. Register it with `rankWith(20, and(isControl, optionIs("widget",
   "yourWidgetName")))` higher than the default rank of 10
3. Export it as `*RendererEntry` and add it to the renderers list where
   the form mounts: `ZircEntityEditor`'s union (inline aggregates) or the
   `renderers` array in `MutationEdit` / `SchemaForm`
4. In the relevant `*FormSchema.uiSchema()`, set the widget via the typed
   `Options` builder: `new Control("#/properties/foo", Options.of().widget("yourWidgetName"), null)`

### "I hit a bug related to autosave"

The autosave state machine lives in one place —
`home/javascript/react/zirc/schemaForm/useAutosavedSchemaForm.ts` — used
by `ZircEntityEditor` (the four inline aggregates) and by `MutationEdit`
directly. The submission page (`schemaForm/SchemaForm.tsx`) is the lone
exception: it has a create-on-first-save flow and still carries its own
copy of the loop.

The shared idioms (all in the hook):
- `formData: T | null` starts null; the caller doesn't render
  `<JsonForms>` until the seed effect has populated it (prevents the
  seed-vs-autosave race)
- `lastSavedRef` tracks what's on the server; `diffLeaves`
  (in `api/formHelpers.ts`) produces the PATCH list
- The autosave diff skips paths whose Control is flagged
  `managesOwnPersistence` in the uiSchema (dedicated POST/DELETE
  endpoints handle those), derived from the schema — no hardcoded path
  list
- Those same server-managed keys are mirrored back into local state
  from the refetched entity via one `JSON.stringify(...)`-keyed effect
- A change to a `refreshesParent`-flagged path invalidates the parent
  query after the save

If you see "field clears unexpectedly on reload," read
`zirc-rearchitect-retrospective.html` "Spurious field clear during slow
page reload" — that bug is fixed but the pattern that causes it is
worth recognizing.

---

## What to skip on a first read

- The entity classes beyond `LineSubmission`. They're all the same
  Lombok-decorated column-list shape.
- The renderers other than the three named above. They follow the same
  pattern; one is enough to understand the shape.

---

## When you're stuck

The most-likely-first answers to common confusions:

| You see... | Read |
|---|---|
| "Method undefined" Java LSP errors that look impossible | `zirc-rearchitect-retrospective.html` Dev-practice section — Lombok-LSP false positives are pervasive; trust `gradle compileJava` |
| Spock test you wrote doesn't run | Same section — Spock specs are silently dormant; write JUnit 4 Java until that's resolved |
| `@DeleteMapping` returns 500 to the client even though the delete worked | Add `@ResponseStatus(HttpStatus.NO_CONTENT)`. The client expects 204 |
| New uiSchema `rule` is silently ignored | Check whether it's on a Group; `SectionRenderer` needs the explicit `visible === false` gate (already in place — the bug only re-appears if you make a new layout renderer) |
| JSP fails to compile with "Must use jsp:body" | Comments between `<jsp:attribute>` blocks break the parser; move the comment inside an attribute body |
| `FormSchemaSnapshotTest` fails | A schema/uiSchema change drifted the wire output; rerun with `-Pzirc.snapshot.update=true`, review the diff under `test/resources/zirc/snapshot/`, then commit if intended |
| `FormSchemaInvariantsTest` fails | The three-way alignment between `schema().properties`, `FIELDS.keySet()`, and the DTO drifted. The failure message names which form and which side is out of sync. Either add the missing entry on the other side, or whitelist the divergence as intentional in the test's `Spec` (e.g. a new server-managed child list) |
| `GenerateTypeScriptDriftTest` fails | The committed `home/javascript/react/zirc/api/types.ts` is out of sync with the Java DTOs. Run `gradle generateZircTypes` and commit the regenerated file. If you added a new DTO, also list it in `GenerateTypeScript.DTO_CLASSES` so it's emitted. |
| Liquibase says a changeset already ran but it didn't | The dev DB's tracker is out of sync; apply the SQL directly with `psql` for local work — CI runs cleanly |

---

## Recommended reading sequence at a glance

```
30 sec:  this guide
15 min:  zirc-architecture.md §1–3 + ZircFormSchema.java + SchemaForm.tsx
60 min:  + retrospective + service + DTO + queries + renderers
deep:    + read the architecture doc end-to-end + skim the form-schema snapshots
```

If you've read all of the above and are still missing context, the
commit log on the `zirc-rearchitect` branch is well-annotated and
chronological — each commit message explains the *why* of one
self-contained change.
