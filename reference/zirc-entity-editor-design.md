# ZircEntityEditor — locked design (not yet implemented)

**Status**: design decided in a May 2026 conversation; no code written yet.
This note records the locked decisions and the implementation sequence so
they survive into the session that builds it. See `zirc-architecture.md`
for the surrounding form-pipeline conventions this builds on.

## Why

Today there are five near-identical per-aggregate edit components
(`MutationEdit`, `AssayEdit`, `GeneEdit`, `LesionEdit`, `PhenotypeEdit`)
plus the submission `SchemaForm`. The four inline editors are ~90% the
same boilerplate (schema fetch → null-gated seed → autosave diff →
PATCH → JsonForms mount), differing only in configuration. `diffLeaves`
alone is copy-pasted six times in three variants.

The goal: one component, **`ZircEntityEditor`**, that renders any ZIRC
aggregate from its schema + uiSchema, driven by a small amount of
per-aggregate config.

## Locked decisions

### Component
- **One** component named `ZircEntityEditor`, in `home/javascript/react/zirc/`.
  Replaces the per-aggregate inline editors; each becomes a one-liner
  (or the list renderer mounts `ZircEntityEditor` directly).

### What it reads from the uiSchema (per-Control)
- `widget` — renderer dispatch (existing).
- **`managesOwnPersistence`** — the widget owns its own write path
  (uploads/deletes via dedicated endpoints), so the autosave loop must
  skip this path. Set by default by the self-managed-list group builder.
  The skip-set is *derived* by walking Controls and converting
  `scope` → JSON Pointer; this replaces the three hardcoded
  `EXTERNALLY_MANAGED_PATHS` sets. Name chosen over `skipAutosave` to
  state intent at the declaration site; bridge the
  persistence→autosave gap with a one-line comment at the filter.
- **`refreshesParent`** — editing this field changes what the parent's
  collapsed card displays, so after a successful PATCH the parent query
  must be invalidated. Per-Control because the trigger field varies and
  is *not* always a type discriminator:
  - Assay → `/assayType` (discriminator)
  - Lesion → `/lesionType` (discriminator)
  - Gene → `/mutatedGeneZdbID` (feeds the denormalized abbreviation the card shows; NOT a discriminator)
  - Phenotype → `/description` (card shows the snippet; currently UNWIRED — latent staleness the flag fixes by construction)

### What it reads from a frontend registry (per entityKind)
- Endpoints (GET `/{kind}/{id}`, GET `/{kind}/form-schema`, PATCH `/{kind}/{id}`)
- React Query hook + cache key
- Parent topology `{ kind, cacheKey, paramName }`

This registry is the one fact the schema doesn't carry today —
**see "Flagged for future iteration" below.**

### Standing groundwork (already shipped)
- `FormFor<T>` form-data derivation (`formHelpers.ts`)
- `gradle generateZircTypes` (types.ts generated from Java DTOs)
- Three drift tests: `FormSchemaSnapshotTest`, `FormSchemaInvariantsTest`,
  `GenerateTypeScriptDriftTest`

## Implementation sequence (smallest-risk-first)

1. Extract `diffLeaves` → `formHelpers.ts`; add a `useAutosavedSchemaForm`
   hook; port `MutationEdit` first (the hardest caller — mirror-sync,
   self-managed paths, a refresh trigger) to prove the hook.
2. Add `managesOwnPersistence` + `refreshesParent` to
   `org.zfin.zirc.api.uischema.Options`; default the former in the
   self-managed-list group builder; flag the trigger fields
   (`assayType`, `lesionType`, `mutatedGeneZdbID`, `description`).
3. Build `ZircEntityEditor` + the entityKind registry; collapse the four
   inline editors; delete the duplicated seed/autosave code.
4. Snapshot regen + browser smoke.

## Flagged for future iteration: the frontend registry

The per-entityKind registry is hand-maintained frontend data, and parts
of it **restate things the server already knows** — which is the same
duplication smell this whole effort set out to remove. Specifically:

- **Endpoints** follow a strict convention (`/api/zirc/{plural}/{id}`,
  `/api/zirc/{plural}/form-schema`, PATCH same). They're derivable from
  `entityKind` + a pluralization, not genuinely hand-data — except for
  any irregular routes.
- **Parent topology** (`assay`'s parent is `mutation`, etc.) restates
  the server's entity graph (`Mutation.genotypingAssays`, etc.). Two
  sources that can fork silently.
- **Cache key + query hook** are genuinely React Query plumbing — the
  one part that *can't* move server-side — but they too follow a naming
  convention (`{kind}Key`, `use{Kind}ById`).

Improvement options to weigh when this is picked up:

- **(A) Aggregate-metadata endpoint** — `GET /api/zirc/aggregates`
  returns `[{ kind, endpoints, parent }]` reflected from the controllers
  + entity graph. The registry's server-derived parts become fetched,
  not hand-maintained; the fork risk disappears. Most thorough; most
  up-front work.
- **(B) Convention-derive the endpoints** — if routes are uniform,
  compute them from `entityKind` + a small pluralization map. Shrinks
  the registry to the irregular routes + the React Query plumbing.
- **(C) Embed parent in the /form-schema response** — add a small
  `meta: { parent: { kind, paramName } }` block to each form-schema
  payload. The editor learns its parent from the same fetch that gives
  it the schema; cache-key/hook stays frontend.

Honest bound: even the most aggressive option leaves the React Query
cache-key + hook on the frontend (they're React Query-specific). The
irreducible frontend fact is small; the *fork-prone* parts (endpoints,
parent topology) are the ones worth eliminating. (A) or (C) removes the
fork; (B) is the cheap partial win.

No decision made — flagged only. Revisit when `ZircEntityEditor` is
built or when a sixth aggregate makes the registry maintenance bite.
