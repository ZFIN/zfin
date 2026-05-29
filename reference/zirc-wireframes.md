# ZIRC submission app — current state at a glance

ASCII diagrams of the line-submission editor as it stands on the
`pr1883` branch (May 2026). Three architectural cuts followed by
page-level wireframes.

- [§1 Page / UX hierarchy](#1-page--ux-hierarchy)
- [§2 Data model](#2-data-model)
- [§3 Architecture stack (request lifecycle)](#3-architecture-stack-request-lifecycle)
- [§4 Dashboard wireframe](#4-dashboard-wireframe)
- [§5 Submission edit wireframe](#5-submission-edit-wireframe)
- [§6 Mutation edit wireframe (with inline-expand cards)](#6-mutation-edit-wireframe-with-inline-expand-cards)
- [§7 dCAPS assay form (per-type field reveal)](#7-dcaps-assay-form-per-type-field-reveal)

Companion docs: [`zirc-architecture.md`](zirc-architecture.md) for the
full prose architecture, [`zirc-reading-guide.md`](zirc-reading-guide.md)
for a 15-minute tour of the code.

---

## 1. Page / UX hierarchy

```
                       Dashboard  /action/zirc/dashboard
                       ├─ Active line submissions (table)
                       └─ Closed line submissions (table)
                                       │
                              click on a row
                                       │
            ┌──────────────────────────┴──────────────────────────┐
            ▼                                                       ▼
  Submission detail-react                              Submission edit
  /line-submission/{id}/detail-react                   /line-submission/{id}/edit
  ── view-only, status badges,                         ── per-field autosave
     change-history, comments,                         ── 5 sections (side nav):
     status overview band                                Overview · Mutations
                                                         Linked Features · Background
                                                         Additional Info
                                                                  │
                                                       click [Edit] on a row
                                                                  ▼
                                                       Mutation edit
                                                       /mutation/{id}/edit
                                                       ── per-field autosave
                                                       ── 8 sections (side nav)
                                                       ── 4 inline-expand cards
                                                          (Genes, Lesions, Assays,
                                                          Phenotypes) — each
                                                          mounts ZircEntityEditor
                                                          on [Edit]; no route nav
```

---

## 2. Data model

```
zirc.line_submission                            zirc.line_submission_person
─────────────────────                           ─────────────────────────
ls_zdb_id  PK                                   lsp_submission_id  FK
ls_name (unique)                                lsp_person_id      FK → person
ls_previous_names    TEXT[]                     lsp_sort_order
ls_reasons           TEXT[]
ls_maternal_background / paternal_background
ls_background_changeable / change_concerns
ls_husbandry_info / additional_info
ls_is_draft  BOOL                               zirc.line_submission_linked_feature
ls_created_at / updated_at / submitted_at       ──────────────────────────────────
        │                                       lf_submission_id   FK
        │ 1..N                                  lf_mutation_a_id   FK
        ▼                                       lf_mutation_b_id   FK
zirc.mutation                                    (CHECK: same submission;
─────────────                                    a_id < b_id)
m_id  PK                  (BIGSERIAL)
m_submission_id  FK
m_sort_order
m_allele_designation / mutation_type
m_mutagenesis_stage / protocol
m_homozygous_lethal / lethality_*
m_publications  TEXT[]
        │
        ├── 0..N → zirc.gene             (g_mutated_gene_zdb_id FK → marker,
        │                                  g_linkage_group, g_genbank_*)
        ├── 0..N → zirc.lesion           (l_lesion_type IN {point_mutation,
        │                                  deletion, insertion, indel,
        │                                  transgene, …}, type-specific fields)
        ├── 0..N → zirc.genotyping_assay (ga_assay_type IN {pcr_gel,
        │              │                   pcr_sequencing, rflp, dcaps,
        │              │                   asa, kasp, hrma, sslp},
        │              │                  ga_enzyme_cleaves_wt  BOOL,
        │              │                  ga_enzyme_cleaves_mut BOOL,
        │              │                  ga_*_primer, ga_sslp_*, …)
        │              └── 0..N → zirc.genotyping_assay_file (uploads)
        └── 0..N → zirc.phenotype        (p_description, hpf/dpf timing,
                                          segregation, image-permission)

zirc.line_submission_comment            zirc.line_submission_section_approval
────────────────────────────            ─────────────────────────────────────
lsc_rec_id   (e.g. ZDB-LINESUB-…,       lssa_rec_id
              ZIRC-MUT-{id},            lssa_section_name
              ZIRC-GENE-{id},           lssa_approved_by  FK → person
              ZIRC-LF-{a}-{b}, …)       lssa_approved_at
lsc_scope IN ('field', 'section')
lsc_field_name | lsc_section_name
lsc_author_zdb_id  FK → person
lsc_comment
lsc_closed  BOOL
lsc_created_at

zirc.audit  (immutable event log)
─────────────────────────────────
audit_entity_kind     (LineSubmission, Mutation, Gene, …)
audit_entity_id
audit_path            JSON Pointer (e.g. /alleleDesignation)
audit_old_value       JSONB
audit_new_value       JSONB
audit_actor           FK → person
audit_when            TIMESTAMP
```

---

## 3. Architecture stack (request lifecycle)

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│  Browser                                                                          │
│                                                                                   │
│  Apache (static) ───────────►  Tomcat (/action/* reverse-proxied)                 │
│   /dist/*.js                                                                      │
└─────────────┬─────────────────────────────────┬─────────────────────────────────┘
              │ GET .../edit                    │ fetch() once mounted
              ▼                                  ▼
  ┌──────────────────────────────┐    ┌────────────────────────────────────────┐
  │  JSP shell                    │    │  Spring MVC REST controllers          │
  │  ─────────                    │    │  ───────────────────────────          │
  │  line-submission-edit.jsp     │    │  ZircSubmissionApiController          │
  │  mutation-edit.jsp            │    │  ZircMutationApiController            │
  │  line-submission-detail-      │    │  ZircAssayApiController, …            │
  │     react.jsp                 │    │   GET    .../form-schema              │
  │  ── side-nav scaffold         │    │   GET    .../{id}                     │
  │  ── breadcrumb                │    │   PATCH  .../{id}   body {path,value} │
  │  ── <__react-root> mount      │    │     ↳ dispatch via FIELDS map         │
  │     point + data-* props      │    │   POST / DELETE for managed lists     │
  └──────────────┬───────────────┘    └─────────────────┬─────────────────────┘
                 │                                       │
                 ▼ React/JsonForms takes over            │ reads ZircFormSchema /
   ┌────────────────────────────────────┐                │       Zirc*FormSchema:
   │  React (TypeScript)                │                ▼
   │  ─────────────────                  │      ┌───────────────────────────────┐
   │  LineSubmissionEdit /              │ ◄────│  ZircFormSchema (Java)        │
   │  MutationEdit /                    │ schema│  ──────────────              │
   │  ZircEntityEditor                  │ fetch │  schema()    JSON Schema     │
   │    └── mounts <JsonForms>          │       │  uiSchema()  JSON Forms UI   │
   │         with the fetched           │       │  FIELDS      path→(read,     │
   │         schema + uiSchema          │       │              write) dispatch │
   │                                    │       │              (one entry per  │
   │  useAutosavedSchemaForm ──── PATCH ┼──────►│              leaf path)      │
   │    debounced diff →                │       └─────────────────┬─────────────┘
   │    one PATCH per leaf              │                          │
   │                                    │                          ▼
   │  Renderers union:                  │             ┌─────────────────────────┐
   │    SectionRenderer                 │             │  Service layer          │
   │    RowControlRenderer              │             │  ─────────────          │
   │    TextareaRowRenderer             │             │  ZircSubmissionService  │
   │    SelectWithOtherRenderer         │             │  LineSubmissionService  │
   │    CheckboxRenderer                │             │  ── per-leaf merge      │
   │    YesNoRadioRenderer              │             │  ── @DynamicUpdate so   │
   │    MultipleChoiceWithOtherRenderer │             │     concurrent edits    │
   │    PublicationsListRenderer (stringList)│        │     to sibling columns  │
   │    AttachmentsRenderer             │             │     don't clobber       │
   │    AutocompleteRenderer            │             │  ── writes audit row    │
   │    PhenotypeTimingRenderer         │             │     per leaf change     │
   │    MutationsListRenderer           │             └─────────────┬───────────┘
   │    GenesListRenderer               │                            │ Hibernate
   │    LesionsListRenderer             │                            ▼
   │    AssaysListRenderer              │             ┌─────────────────────────┐
   │    PhenotypesListRenderer          │             │  Postgres               │
   │    LinkedFeaturesListRenderer      │             │  ────────               │
   │    VerticalLayoutRenderer          │             │  zirc.* tables          │
   └────────────────────────────────────┘             └─────────────────────────┘
```

---

## 4. Dashboard wireframe

```
+--------------------------------------------------------------------------------------------------------+
|  ZFIN  Research | Genomics | Resources | Community | Support | Curation             Version: zfin-10298|
+--------------------------------------------------------------------------------------------------------+
|                                                                                                        |
|  ZIRC                                                                                                  |
|  Line Submission Dashboard                                                                             |
|  ==========================                                                                            |
|                                                                                                        |
|  + Start a new line submission                                                                         |
|                                                                                                        |
|  +----------------------------------------------------------------------------------------------+      |
|  | Active Line Submissions                                                                      |      |
|  +-----------------------------+--------------+---------+--------------------------------------+      |
|  | Line Submission Name        | Date Started | Status  | Submitter(s)                         |      |
|  +-----------------------------+--------------+---------+--------------------------------------+      |
|  | bla                         |  2026-05-27  |   M     |  R. Taylor                           |      |
|  | (untitled)                  |  2026-05-27  |   M     |  S. Moxon                            |      |
|  | m3.1 verify autosave        |  2026-05-15  |   M     |  —                                   |      |
|  | jsonforms spike works       |  2026-05-15  |   M     |  —                                   |      |
|  | test4                       |  2026-05-13  |   M     |  R. Taylor                           |      |
|  +-----------------------------+--------------+---------+--------------------------------------+      |
|                                                                                                        |
|  +----------------------------------------------------------------------------------------------+      |
|  | Closed Line Submissions                                                                      |      |
|  +----------------------------------------------------------------------------------------------+      |
|  | No closed submissions.                                                                       |      |
|  +----------------------------------------------------------------------------------------------+      |
+--------------------------------------------------------------------------------------------------------+
```

---

## 5. Submission edit wireframe

```
+----------------------+---------------------------------------------------------------------------------------+
|                      |   Dashboard                  Detail                            Edit                    |
| My Submission Name   +---------------------------------------------------------------------------------------+
|                      |  EDIT LINE SUBMISSION                                                                  |
|                      |  My Submission Name                                                                    |
+----------------------+  +----------------------------------------------------------------------------------+ |
| Overview             |  | Overview         ⟲  💬                                                            | |
|                      |  | ================================================================================ | |
| Mutations            |  | Line Name *         +----------------------------------------------------+        | |
|                      |  |                     | My Submission Name                                 |        | |
| Linked Features      |  |                     +----------------------------------------------------+        | |
|                      |  |                       Line name as it should appear in publications.              | |
| Background           |  |                                                                                  | |
|                      |  | Previous Names      +-----------------------------+ [ Remove ]                   | |
| Additional Info      |  |                     | foo-zf1                     |                              | |
|                      |  |                     +-----------------------------+                              | |
|                      |  |                     +-----------------------------+ [ Remove ]                   | |
|                      |  |                     | bar-zf2                     |                              | |
|                      |  |                     +-----------------------------+                              | |
|                      |  |                     [ + Add previous name ]                                      | |
|                      |  |                                                                                  | |
|                      |  | Acceptance Reasons  [X] Currently frequently requested                           | |
|                      |  |                     [ ] Expect high demand                                       | |
|                      |  |                     [X] Interesting gene                                         | |
|                      |  |                     [ ] Community resource/tool                                  | |
|                      |  |                     [ ] Mutant gene cloned                                       | |
|                      |  |                     [ ] Danger of losing line                                    | |
|                      |  |                     [ ] Lack of space or funding to maintain line                | |
|                      |  |                     [ ] Other                                                    | |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |                                                                                       |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |  | Mutations         ⟲  💬                                                           | |
|                      |  | ================================================================================ | |
|                      |  |  # | Allele Designation | Mutagenesis Protocol | Mutation Type | Discoverer | Actions| |
|                      |  | ---+--------------------+----------------------+---------------+------------+--------| |
|                      |  |  1 | smoke-test-zf999   |  —                   |  —            |   —        |[Edit]  | |
|                      |  |    |                    |                      |               |            |[Remove]| |
|                      |  | [ + Add mutation ]                                                                | |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |                                                                                       |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |  | Linked Features  ⟲  💬                                                            | |
|                      |  | No linked features recorded for this submission.                                 | |
|                      |  | Add at least two mutations before linking them.                                  | |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |                                                                                       |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |  | Background       ⟲  💬                                                            | |
|                      |  | Single-allelic submission   ( ) Yes ( ) No                                       | |
|                      |  | Maternal                    [  (select)                              ▾ ]         | |
|                      |  | Paternal                    [  (select)                              ▾ ]         | |
|                      |  | Background Changeable       ( ) Yes ( ) No                                       | |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |                                                                                       |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |  | Additional Info  ⟲  💬                                                            | |
|                      |  | Unreported Features Details  +------------------------------+                    | |
|                      |  |                              |                              |                    | |
|                      |  |                              +------------------------------+                    | |
|                      |  | Husbandry Info               +------------------------------+                    | |
|                      |  | Additional Info              +------------------------------+                    | |
|                      |  +----------------------------------------------------------------------------------+ |
+----------------------+---------------------------------------------------------------------------------------+
```

---

## 6. Mutation edit wireframe (with inline-expand cards)

```
+----------------------+---------------------------------------------------------------------------------------+
|                      |   Dashboard         «  Submission                                  Mutation            |
| smoke-test-zf999     +---------------------------------------------------------------------------------------+
|                      |  Submission > My Submission Name  ·  EDIT MUTATION                                    |
|                      |  smoke-test-zf999                                                                     |
+----------------------+  +----------------------------------------------------------------------------------+ |
| General              |  | General                                                                          | |
|                      |  | -------                                                                          | |
| Mutagenesis          |  | Allele already in ZFIN     ( ) Yes  (•) No                                       | |
|                      |  | Allele Designation *       [  smoke-test-zf999                            ]      | |
| Genes                |  | Mutation Type *            [  (select)                              ▾ ]          | |
|                      |  | ZFIN Record Established    ( ) Yes ( ) No                                        | |
| Lesions              |  | Discoverer                 [  Person who first identified the mutation   ]      | |
|                      |  | Institution                [  Lab / institution                          ]      | |
| Genotyping Assays    |  +----------------------------------------------------------------------------------+ |
|                      |                                                                                       |
| Phenotypes           |  +----------------------------------------------------------------------------------+ |
|                      |  | Mutagenesis                                                                      | |
| Lethality            |  | Mutagenesis Stage / Protocol / Molecularly Characterized  …                      | |
|                      |  +----------------------------------------------------------------------------------+ |
| Publications         |                                                                                       |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |  | Genes                                                                            | |
|                      |  |   ┌──────────────────────────────────────────────────────────────────────────┐   | |
|                      |  |   │ Gene 1   shha                                            [Edit] [Remove]│   | |
|                      |  |   └──────────────────────────────────────────────────────────────────────────┘   | |
|                      |  |   ┌──────────────────────────────────────────────────────────────────────────┐   | |
|                      |  |   │ Gene 2   pax6a                                          [Done] [Remove]│   | |
|                      |  |   │   Gene (ZDB-ID) *   [ ZDB-GENE-980526-561                ]              │   | |
|                      |  |   │   Linkage Group     [ 7                                  ]              │   | |
|                      |  |   │   GenBank Genomic   [ NC_007112.7                        ]              │   | |
|                      |  |   │   GenBank cDNA      [                                    ]              │   | |
|                      |  |   └──────────────────────────────────────────────────────────────────────────┘   | |
|                      |  |   [ + Add gene ]                                                                  | |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |                                                                                       |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |  | Lesions                                                                          | |
|                      |  |   ┌──────────────────────────────────────────────────────────────────────────┐   | |
|                      |  |   │ Lesion 1   Point mutation                              [Edit]  [Remove] │   | |
|                      |  |   └──────────────────────────────────────────────────────────────────────────┘   | |
|                      |  |   [ + Add lesion ]                                                                | |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |                                                                                       |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |  | Genotyping Assays                                                                | |
|                      |  |   ┌──────────────────────────────────────────────────────────────────────────┐   | |
|                      |  |   │ Assay 1   dCAPS                                        [Done]  [Remove] │   | |
|                      |  |   │   ←  see "dCAPS assay form" wireframe below for the full revealed body   │   | |
|                      |  |   └──────────────────────────────────────────────────────────────────────────┘   | |
|                      |  |   [ + Add assay ]                                                                 | |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |                                                                                       |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |  | Phenotypes                                                                       | |
|                      |  |   ┌──────────────────────────────────────────────────────────────────────────┐   | |
|                      |  |   │ Phenotype 1   "small head, curved tail at 48 hpf"     [Edit]  [Remove]  │   | |
|                      |  |   └──────────────────────────────────────────────────────────────────────────┘   | |
|                      |  |   [ + Add phenotype ]                                                             | |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |                                                                                       |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |  | Lethality                                                                        | |
|                      |  | Homozygous Lethal       ( ) Yes  (•) No                                          | |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |                                                                                       |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |  | Publications                                                                     | |
|                      |  | Publications  [ ZDB-PUB-111111-1                              ] [ Remove ]       | |
|                      |  |               [ + Add publication ]                                              | |
|                      |  +----------------------------------------------------------------------------------+ |
|                      |                                                                                       |
|                      |  [ «  Back to Submission ]                                                            |
+----------------------+---------------------------------------------------------------------------------------+
```

---

## 7. dCAPS assay form (per-type field reveal)

```
  ┌────────────────────────────────────────────────────────────────────────────────────┐
  │ Assay 1   dCAPS                                                  [Done]   [Remove] │
  ├────────────────────────────────────────────────────────────────────────────────────┤
  │                                                                                    │
  │   Assay type             [ dCAPS                                          ▾ ]      │
  │                                                                                    │
  │   Forward primer         [ ACTGN sequence                                    ]     │
  │                            ACTGN only.                                              │
  │   Reverse primer         [ ACTGN sequence                                    ]     │
  │                            ACTGN only.                                              │
  │                                                                                    │
  │   Expected wild-type     [                                                   ]     │
  │     PCR product                                                                    │
  │   Expected mutant        [                                                   ]     │
  │     PCR product                                                                    │
  │                                                                                    │
  │   Primer with introduced [ ACTGN sequence                                    ]     │
  │     mismatch               ACTGN only.                                              │
  │                                                                                    │
  │   Restriction enzyme     [ e.g. BsmBI                                        ]     │
  │     name                                                                           │
  │   Restriction enzyme     [ vendor + cat #                                    ]     │
  │     catalog # (info)                                                               │
  │                                                                                    │
  │   Enzyme cleaves WT      [ ]                                                       │
  │     template                                                                       │
  │   Enzyme cleaves MUT     [X]                                                       │
  │     template                                                                       │
  │                                                                                    │
  │   Expected WT product    [                                                   ]     │
  │     after digest                                                                   │
  │   Expected MUT product   [                                                   ]     │
  │     after digest                                                                   │
  │                                                                                    │
  │   Annotated gel images                                                             │
  │   ─────────────────                                                                 │
  │   No attachments yet.                                                              │
  │   [ Choose File ]  No file chosen                                                  │
  │                                                                                    │
  │   Additional info        +------------------------------------+                    │
  │                          |                                    |                    │
  │                          +------------------------------------+                    │
  └────────────────────────────────────────────────────────────────────────────────────┘
```

The other seven assay types follow the same shape with different field
reveals — see [`ZircAssayFormSchema.java`](../source/org/zfin/zirc/api/ZircAssayFormSchema.java)'s
per-type cluster lists (`FWD_REV_PRIMER_TYPES`, `EXPECTED_PCR_TYPES`,
`SEQUENCING_TYPES`, `DCAPS_TYPES`, `ALLELE_SPECIFIC_TYPES`, `KASP_TYPES`,
`DIGEST_TYPES`, `SSLP_TYPES`) and the attachment-bucket reveal lists
(`GEL_IMAGE_TYPES`, `CHROMATOGRAM_TYPES`, `RESULT_IMAGE_TYPES`,
`MELT_CURVE_TYPES`).
