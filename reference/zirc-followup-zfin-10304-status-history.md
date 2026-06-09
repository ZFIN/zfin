# Follow-up: re-port ZFIN-10304 status badges + change-history popups onto the schema-driven editor

## Context

The `zirc-rearchitect` branch replaces the static JSP line-submission
editor introduced by ZFIN-10265 (merged to main as PR #1864,
`5029f76f2c`) with a schema-driven React editor. Between that merge
and the squash of this branch, ZFIN-10304 (`96cdd947d2`, ~2200 LOC
across 18 files) added two curator-facing features **on top of** the
ZFIN-10265 JSP editor:

1. **System-derived completeness status** — a coloured badge
   (Missing / In Progress / Complete / Approved) next to every field,
   every section heading, and every left-nav item on the submission
   detail page.
2. **Change-history popups** — a clock icon next to every field and
   section heading that opens a Bootstrap modal listing prior values
   from the central `Updates` table (`insertUpdatesTable`).

When we squash this branch onto current main, both features regress on
the line-submission edit path because:

- The status computers (`LineSubmissionStatusComputer`, `MutationStatusComputer`,
  `GeneStatusComputer`, `LesionStatusComputer`, `GenotypingAssayStatusComputer`,
  `PhenotypeStatusComputer`) compute against the pre-rearchitect entity
  shape and are unwired in our editor.
- The history popups read from the central `Updates` table, but our
  PATCH path writes to `zirc.audit` instead.
- The two JSP tag files (`zirc-status-badge.tag`, `zirc-field-history.tag`,
  `zirc-section-history.tag`) are still referenced by
  `line-submission-detail.jsp` on main, but our React editor doesn't
  render them.

What survives without porting:

- The **read-only** `line-submission-detail.jsp` page (badges + history
  modals are static EL) — works against the schema we don't change.
- The status-badge in the **dashboard** rollup table — only depends on
  `LineSubmissionStatusComputer.compute(s).overall()`, which is JSP
  EL against the entity shape we kept.
- The **left-nav status badges** on the detail page (same data source).

What regresses on the **edit** page (our React form):

- No per-field status badge in the schema-driven UI.
- No per-section status badge in the schema-driven UI.
- No "view change history" icon — even though our PATCH path writes
  `zirc.audit` rows, there's no UI to surface them, and they aren't in
  the `Updates` table that the existing tags read.

## What to port forward (priority order)

### 1. Per-field + per-section status in the schema-driven editor

The cheapest port: keep the status-computer classes, expose
`Map<String, FieldStatus>` per aggregate as new fields on each DTO
(`MutationDTO.fieldStatus`, `MutationDTO.sectionStatus`, etc.), and
render badges in `RowControlRenderer` / `SectionRenderer` from those
maps. JSON Forms doesn't need a schema change — the status map is
sibling data carried alongside the form payload.

Concretely:

- Add `fieldStatus: Record<string, FieldStatus>` to each `*DTO` TS
  type and the Java records.
- Server-side, populate from `*StatusComputer.compute(...).byField()`
  in the DTO factory methods.
- React side, thread `fieldStatus` through `JsonForms`' `config` prop
  (the same channel we use for `mutationId` / `submissionId`); the
  custom renderers can then lookup `config.fieldStatus[scope-as-key]`
  and render the badge from `FieldStatus.cssClass` /
  `FieldStatus.abbreviation`.

Estimated cost: 1 day. The status computer logic is reusable as-is;
this is purely a presentation port.

### 2. Per-aggregate change-history popup

Decide first whether the source of truth should be the central
`Updates` table or our new `zirc.audit` table. Three options:

- **(a) Dual-write**: `ZircSubmissionService.writeAudit(...)` also
  calls `insertUpdatesTable(...)` so the existing
  `zirc-field-history.tag` keeps working as-is. Simplest, but doubles
  the audit footprint. *Recommended for the first port.*
- **(b) Re-point the tags** at `zirc.audit` via a new
  `Map<String, List<AuditEntry>>` server-side model attribute. Cleaner
  long-term but means rewriting the tag bodies. Defer until we know
  we want to retire `Updates`.
- **(c) Move history into the React UI** — a "history" icon on each
  field row that fetches `GET /api/zirc/audit?entity_kind=...&entity_id=...&path=...`
  on demand. Best UX (lazy load) but the largest port.

Estimated cost: half a day for (a), 1–2 days for (c).

### 3. Computed `overall()` status for the React MutationEdit / nested editors

For parity with the detail page's per-mutation card header. Trivial
once #1 is done — the per-aggregate `overall()` is already computed by
each `*StatusComputer`; expose it as `MutationDTO.overallStatus` and
render in `MutationsListRenderer` / `LesionsListRenderer` /
`PhenotypesListRenderer` / `GenesListRenderer` / `AssaysListRenderer`
card headers.

## Files to revisit when this work picks up

- `source/org/zfin/zirc/service/LineSubmissionStatusComputer.java`
  (and the five sibling computers) — keep as-is; they only need to
  be called from new code.
- `source/org/zfin/zirc/presentation/ZircDashboardController.java` —
  on main, this controller does the heavy lifting of computing
  per-row status maps + bucketing Updates by `recID` prefix. After
  the squash, our slim controller doesn't do that. The squash's
  conflict resolution will need a decision: drop the status logic
  from `viewLineSubmission` (lives only on the detail page) or
  shadow-copy the bits we still need.
- `home/WEB-INF/tags/layout/zirc-status-badge.tag` — keep; it's
  presentation-only and works against any source that produces a
  `FieldStatus`.
- `home/WEB-INF/tags/layout/zirc-field-history.tag` and
  `home/WEB-INF/tags/layout/zirc-section-history.tag` — keep; they
  render from a generic `Updates` collection. If we go with
  option 2a (dual-write), no changes needed.
- `home/WEB-INF/jsp/zirc/line-submission-detail.jsp` — keep as-is on
  main; the squash should not touch it. Detail (view) page remains
  static JSP; only the **edit** page becomes React.

## Open questions

- Does `zirc.audit` need a `field_label` column (for the modal's "Field"
  display) or should the JSP keep using the snake_case `field_name`?
  Today's audit stores JSON Pointers like `/alleleDesignation`; the
  modal would show that string. Probably fine — curators recognize the
  paths from the URL bar in any case.
- ZFIN-10304's status enum has an `APPROVED` value that no part of the
  current code emits; the implementing class comment says "reserved
  for the future curator workflow." Park that question with the
  workflow design rather than blocking this follow-up.
- The `Approved` state and the `IN_PROGRESS` state both depend on a
  future workflow ticket that may add a `status_override` column.
  When that lands, the status computers will need to read that column
  in addition to deriving from emptiness; our DTO additions should
  plan for that.

## How to find the squash commit's regression footprint

After the squash lands, this query enumerates the dead references:

```
git grep -nE 'zirc-status-badge|zirc-field-history|zirc-section-history|fieldStatus|sectionStatus|overallStatus|fieldUpdates|mutationFieldUpdates' \
    home/WEB-INF/jsp/zirc/
```

Anything in `line-submission-detail.jsp` still works (data-bound via
the dashboard controller on main). Anything that referenced these in
the **edit** flow is what this follow-up needs to rebuild.

## Owner

TBD — likely the same person who picks up the workflow ticket that
introduces `Approved` / curator-override status, since the model
needs to extend together.
