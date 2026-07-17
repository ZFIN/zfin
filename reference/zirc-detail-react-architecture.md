# ZIRC Line-Submission Schema-Driven Stack — Architecture Diagram

Snapshot of the post-refactor schema-driven detail/edit stack as it stands on
the `zirc-detail-readonly-cutover` branch. Pairs with
[`reference/zirc-architecture.md`](zirc-architecture.md) (the upstream
contract from PR #1875) and
[`reference/zirc-followup-zfin-10304-status-history.md`](zirc-followup-zfin-10304-status-history.md)
(the original ZFIN-10304 follow-up plan).

## One-page picture

```
╔══════════════════════════════════════════════════════════════════════════════════════════════════════╗
║                                ZIRC LINE-SUBMISSION SCHEMA-DRIVEN STACK                              ║
║                                  (post-refactor, current branch)                                     ║
╚══════════════════════════════════════════════════════════════════════════════════════════════════════╝

┌─────────────────────────────────── PostgreSQL (zfindb · schema=zirc) ─────────────────────────────────┐
│                                                                                                       │
│   line_submission   mutation   gene   lesion   genotyping_assay   phenotype   linked_feature          │
│   line_submission_person   genotyping_assay_file                                                      │
│   audit            (one row per field-path PATCH / add / delete / upload)                             │
│   line_submission_comment   (per (recId, scope, fieldName|sectionName, author))                       │
│                                                                                                       │
└──────────────────────────────────────────┬────────────────────────────────────────────────────────────┘
                                           │ Hibernate (entities + @DynamicUpdate for safe concurrent PATCH)
                                           ▼
┌──────────── source/org/zfin/zirc/entity ──────────┐  ┌─────────── source/org/zfin/zirc/service ────────┐
│ LineSubmission   Mutation   Gene   Lesion         │  │ ZircSubmissionService                            │
│   GenotypingAssay   Phenotype   LinkedFeature     │  │   • createDraftForCurrentUser                    │
│   GenotypingAssayFile   LineSubmissionPerson      │  │   • updateField(zdbID, FieldUpdate)              │
│   AuditEntry   LineSubmissionComment              │◀─│       └─ FIELDS.read → audit.write              │
│   (Gene.@Transient getMutatedGeneZdbID for the    │  │       └─ entity setter (typed coercion)         │
│    schema/computer alignment)                     │  │       └─ audit insert  ─── all one txn          │
│                                                   │  │ ZircCommentService    (INSERT-only)              │
│                                                   │  │ Line/Mutation/Gene/Lesion/Genotyping/Phenotype   │
│                                                   │  │   StatusComputer   (read schema.required once,   │
│                                                   │  │   produce {byField, bySection, overall})         │
└─────────────────────────┬─────────────────────────┘  └────────────────────────┬─────────────────────────┘
                          │                                                     │
                          │     ┌───── source/org/zfin/zirc/api ─────────┐      │
                          └─────│ ZircSubmissionApiController            │──────┘
                                │   GET  /api/zirc/form-schema           │
                                │   GET  /api/zirc/line-submissions/{id} │
                                │   POST /api/zirc/line-submissions      │
                                │   PATCH/api/zirc/line-submissions/{id} │ ◀── edit-mode write surface
                                │ ZircMutationApiController              │
                                │ ZircGeneApiController                  │
                                │ ZircLesionApiController                │
                                │ ZircAssayApiController                 │
                                │ ZircPhenotypeApiController             │
                                │   GET  /api/zirc/{kind}/form-schema    │ ◀── one cached fetch per aggregate
                                │   GET  /api/zirc/{kind}/{id}           │
                                │ ZircAutocompleteApiController          │
                                │ ZircCommentApiController               │
                                │   GET  /api/zirc/comments              │
                                │   POST /api/zirc/comments              │
                                └────────────────┬───────────────────────┘
                                                 │
        ┌────────── source/org/zfin/zirc/api ────┴──── (schema as data) ─────────┐
        │ ZircFormSchema · ZircMutationFormSchema · ZircGeneFormSchema           │
        │ ZircLesionFormSchema · ZircAssayFormSchema · ZircPhenotypeFormSchema   │
        │ ZircLinkedFeatureFormSchema                                            │
        │                                                                       │
        │   .schema()    → ObjectSchema { properties, required, … }             │
        │   .uiSchema()  → VerticalLayout { Groups + Controls + Rules + Options }│
        │   .FIELDS      → Map<JSONPointer, FieldDescriptor(read, write)>       │
        │                                                                       │
        │ Single source of truth for:                                           │
        │   • shape, type, max-length, enum                                     │
        │   • required-ness   (← LineSubmissionStatusComputer reads from here)  │
        │   • layout (Group order, "plain" layout opt for list groups)          │
        │   • widget dispatch (yesNoRadio, selectWithOther, …)                  │
        │   • conditional reveal (Rule.SHOW/HIDE)                               │
        │   • comments opt-out (Options.comments=false)                         │
        │                                                                       │
        │ Snapshot-locked by FormSchemaSnapshotTest.                            │
        │ FIELDS.keySet() ≡ schema leaves ≡ DTO components (FormSchemaInvariants)│
        └───────────────────────────────────────────────────────────────────────┘
                                                 │
                                                 │ Jackson →  JSON over the wire
        =================================  WIRE BOUNDARY  ===================================
                                                 │
        ┌──────────────── frontend/javascript/react/zirc ─────────────── (TS / React)
        │
        │  ┌─ containers/ (loader shims for react/index.js) ─┐
        │  │ LineSubmissionEdit.tsx    LineSubmissionDetail.tsx │
        │  └────────────────────────────┬────────────────────┘
        │                               │
        │  ┌─ pages/ ─────────────────  │ ─────────────────────────────────────────────────────┐
        │  │ LineSubmissionEdit.tsx ◀──┘                                                       │
        │  │   useLineSubmission(id) → useCreateLineSubmission                                 │
        │  │   <SchemaForm submission={…} onCreated={…}/>           (default mode='edit')      │
        │  │                                                                                   │
        │  │ LineSubmissionDetail.tsx                                                          │
        │  │   payload = JSON.parse(<script id="ls-detail-status-payload">…</script>)         │
        │  │   useLineSubmission(id)                                                          │
        │  │   <SchemaForm mode='view'                                                        │
        │  │       fieldStatus / fieldUpdates / sectionStatus / sectionUpdates                │
        │  │       mutationFieldStatus / mutationSectionStatus />                             │
        │  │                                                                                   │
        │  │ MutationEdit / GeneEdit / LesionEdit / AssayEdit / PhenotypeEdit                 │
        │  │   per-aggregate edit pages (mount their own JsonForms + autosave)                │
        │  └───────────────────────────────────────────┬───────────────────────────────────────┘
        │                                              │
        │  ┌─ schemaForm/ ───────────────────────────── │ ───────────────────────────────────────┐
        │  │ SchemaForm.tsx                                                                       │
        │  │   • fetch /form-schema (staleTime Infinity)                                          │
        │  │   • seed formData + lastSavedRef from DTO                                            │
        │  │   • EDIT mode  : on change → 800ms debounce → diffLeaves → PATCH per leaf            │
        │  │                  (EXTERNALLY_MANAGED_PATHS bypass: /mutations, /linkedFeatures)      │
        │  │   • VIEW mode  : skip autosave + onChange; thread payload into config               │
        │  │                                                                                       │
        │  │   config = { submissionId, mode, readonly, recId,                                    │
        │  │              fieldStatus, fieldUpdates, sectionStatus, sectionUpdates,               │
        │  │              mutationFieldStatus, mutationSectionStatus, … }                         │
        │  │                                                                                       │
        │  │ aggregateRenderers.ts   (shared registry for nested per-aggregate forms)             │
        │  │ useViewConfig.ts        → viewConfigFrom(config), leafOf(path), commentsEnabled()    │
        │  └──────────────────────┬───────────────────────────────────────────────────────────────┘
        │                         │
        │  ┌─ schemaForm/renderers/ ─────── (JSON Forms widget dispatch) ───────────────────────┐
        │  │ VerticalLayoutRenderer    SectionRenderer (h2 + indent + section-status + history  │
        │  │                                            + comments;  idPrefix for nested cards) │
        │  │ RowControlRenderer        TextareaRowRenderer      YesNoRadioRenderer              │
        │  │ SelectWithOtherRenderer   MultipleChoiceWithOtherRenderer    AutocompleteRenderer  │
        │  │ PublicationsListRenderer (stringList)             PhenotypeTimingRenderer          │
        │  │ MutationsListRenderer    LinkedFeaturesListRenderer                                │
        │  │ GenesListRenderer (view-mode: HTML table with per-cell badge+comment)              │
        │  │ LesionsListRenderer / AssaysListRenderer / PhenotypesListRenderer                  │
        │  │   ↳ view-mode: per-row component (LesionDetailCard / AssayDetailCard / Phenotype-) │
        │  │                  • useLesionById / useAssayById / usePhenotypeById                 │
        │  │                  • nested <JsonForms> with aggregateRenderers                      │
        │  │                  • recId = "ZIRC-LESION-{id}" / "ZIRC-GA-{id}" / "ZIRC-PHEN-{id}"  │
        │  │ AttachmentsRenderer  (file list; multipart upload bypasses PATCH)                  │
        │  └──────────────────────┬───────────────────────────────────────────────────────────────┘
        │                         │
        │  ┌─ components/ ────────┴── (renderer-agnostic UI primitives) ───────────────────────┐
        │  │ StatusBadge (M/IP/C/A; reserves slot when null for alignment)                     │
        │  │ ValueDisplay (read-only formatter: empty→— · bool→Yes/No · enum oneOf→title)      │
        │  │ FieldHistory (clock icon → portaled popup; updates fed from sectionUpdates /      │
        │  │               fieldUpdates payload maps)                                           │
        │  │ FieldComments (chat icon → portaled popup; useQuery GET, useMutation POST;        │
        │  │                z-index 2000 above the nav)                                         │
        │  │ SaveStatusBadge (edit-mode toast for the autosave state)                          │
        │  └────────────────────────────────────────────────────────────────────────────────────┘
        │
        └─ Apache (httpd) serves /dist/ from the webpack bundle ────────────────────────────────
           Tomcat answers /action/** + /api/** through ProxyPass on the httpd container.


┌─────────────── EDIT MODE (one keystroke → one PATCH) ──────────────────────────────────────────┐
│                                                                                                │
│   <input> ─onChange─► JsonForms ─onChange─► setFormData ─effect+800ms─► diffLeaves              │
│                                                                              │                 │
│        ◀────────── lastSavedRef = formData ◀── 200 OK ◀── PATCH(path,value) ─┘                 │
│                                              │                                                  │
│                                              ▼                                                  │
│             ZircSubmissionApiController.patch → ZircSubmissionService.updateField              │
│                ↳ FIELDS.get(path)  ◀─── unknown path: 400                                       │
│                ↳ read old / write new (typed coercion)                                          │
│                ↳ audit row INSERT (same Hibernate txn)                                          │
│                ↳ commit  ─►  @DynamicUpdate emits only the changed column                       │
│                                                                                                 │
└────────────────────────────────────────────────────────────────────────────────────────────────┘

┌─────────────── VIEW MODE (one bootstrap render; comments are the only live channel) ─────────┐
│                                                                                                │
│   Browser ─GET /action/zirc/line-submission/{id}/detail-react─► viewLineSubmissionReact       │
│     │  status payload JSON ─► <script type="application/json"> ─► JSON.parse on mount        │
│     │  dataPage tag ─► subSections / subSectionStatus / subSubSections … left-nav HTML       │
│     ▼                                                                                          │
│   <LineSubmissionDetail> ─► fetch /line-submissions/{id} + /form-schema                       │
│                              + /mutations/form-schema  (cached)                               │
│                              + /lesions/{id}, /assays/{id}, /phenotypes/{id}  (lazy per row)  │
│   <SchemaForm mode='view'>                                                                    │
│      └─► JsonForms readonly  ──► renderers branch on view.readonly                            │
│              └─► StatusBadge (from payload) · ValueDisplay · FieldHistory · FieldComments     │
│                                                                                                │
│   Comment popup ─► GET /api/zirc/comments  /  POST /api/zirc/comments (optimistic cache add)  │
│                                                                                                │
└────────────────────────────────────────────────────────────────────────────────────────────────┘
```

## Key invariants the diagram is encoding

- **The schema is data.** Every Java `Zirc*FormSchema` builds `ObjectSchema` /
  `VerticalLayout` records and Jackson serializes them. The same JSON drives
  both the edit autosave loop and the view-mode read-only rendering.
- **`schema.required` is the single source of truth for required-ness.** Each
  `*StatusComputer` reads it once at class init via reflective walk of the
  schema. The Java `Field` enum is just a path catalogue now.
- **`FIELDS` is the authorization gate.** The PATCH endpoint rejects any
  JSON-Pointer not in the map. Audit rows mirror exactly what got written.
- **Three-way consistency is test-locked.** `FormSchemaInvariantsTest` asserts
  `FIELDS.keySet() ≡ schema leaves ≡ DTO record components` (minus per-form
  whitelists for things like `/mutations` that move through dedicated
  POST/DELETE endpoints, or `/createdAt`/`/updatedAt` which are read-only
  display fields).
- **Edit is two-way, view is one-way.** View mode never touches `FIELDS` or
  the audit table; its only live channel is the comments API. Status, history,
  and comments are three independent side-channels keyed to the same JSON-
  Pointer field names.
- **One `recId` convention threads through nested aggregates.** Submission
  level uses the ZDB-ID directly; nested cards set `ZIRC-MUT-{id}`,
  `ZIRC-GENE-{id}`, `ZIRC-LESION-{id}`, `ZIRC-GA-{id}`, `ZIRC-PHEN-{id}` on
  their inner config so renderers can address audit / comments to the right
  entity without re-deriving anything.
