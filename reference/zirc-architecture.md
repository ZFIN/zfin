# ZIRC Line Submission — Architecture

This document is the **contract**, not the story. For the story
(decisions, alternatives considered, mistakes made), see
`reference/zirc-rearchitect-retrospective.html`.

This file is meant to be the first thing a new contributor to the ZIRC
submission stack reads. It explains the moving parts, where they live,
and the conventions that hold them together.

---

## 1. Mental model in one paragraph

The ZIRC submission form is **schema-driven**: the server publishes a
JSON Schema + JSON Forms uiSchema; the React client fetches both and
renders the form. Writes are **per-field**: the client emits one PATCH
per leaf change with a JSON Pointer path and value. The server looks
the path up in a `FieldDescriptor` map, captures the old value, applies
the new value, writes one audit row, and commits — all inside the same
Hibernate transaction. Each aggregate gets its own flat-path namespace
(submission, mutation, assay, gene, lesion, phenotype, linked-feature),
which sidesteps the need for a JSON Pointer array-index resolver.

---

## 2. Aggregates

An **aggregate** here is a self-contained editing unit — a top-level
entity that owns its own form, its own PATCH endpoint, its own
flat-path namespace for autosave, and its own audit identity. The term
is borrowed loosely from DDD's "aggregate root." Each aggregate has
its own JSON Pointer root, which is why a PATCH to `/alleleDesignation`
on `/api/zirc/mutations/{id}` updates *that* mutation — not "the third
mutation under that submission." Expanded child cards therefore run as
their own form scope, and there is no array-index path resolver to
build or maintain ([§11](#11-inline-expansion-as-the-ux-pattern)).

There are seven aggregates in the ZIRC submission stack:

| Aggregate          | DB table                 | Form schema                                   | PATCH endpoint                                                          |
|--------------------|--------------------------|-----------------------------------------------|-------------------------------------------------------------------------|
| `LineSubmission`   | `zirc.line_submission`   | `GET /api/zirc/form-schema`                   | `PATCH /api/zirc/line-submissions/{zdbID}`                              |
| `Mutation`         | `zirc.mutation`          | `GET /api/zirc/mutations/form-schema`         | `PATCH /api/zirc/mutations/{mutationId}`                                |
| `GenotypingAssay`  | `zirc.genotyping_assay`  | `GET /api/zirc/assays/form-schema`            | `PATCH /api/zirc/assays/{assayId}`                                      |
| `Gene`             | `zirc.gene`              | `GET /api/zirc/genes/form-schema`             | `PATCH /api/zirc/genes/{geneId}`                                        |
| `Lesion`           | `zirc.lesion`            | `GET /api/zirc/lesions/form-schema`           | `PATCH /api/zirc/lesions/{lesionId}`                                    |
| `Phenotype`        | `zirc.phenotype`         | `GET /api/zirc/phenotypes/form-schema`        | `PATCH /api/zirc/phenotypes/{phenotypeId}`                              |
| `LinkedFeature`    | `zirc.linked_feature`    | (no per-row form-schema — inline edit)        | `PATCH /api/zirc/line-submissions/{zdbID}/linked-features/{aId}/{bId}`  |

Each aggregate has:

- An **entity class** in `source/org/zfin/zirc/entity/`
- A **DTO record** in `source/org/zfin/zirc/dto/` (e.g. `MutationDTO.of(entity)`)
- A **form schema class** in `source/org/zfin/zirc/api/` (`ZircFormSchema`,
  `ZircMutationFormSchema`, `ZircAssayFormSchema`, `ZircGeneFormSchema`,
  `ZircLesionFormSchema`, `ZircPhenotypeFormSchema`, `ZircLinkedFeatureFormSchema`)
  holding `schema()`, `uiSchema()`, and `FIELDS`
- A **controller** with GET / GET form-schema / PATCH / (optionally) child-row endpoints

Child aggregates appear as **summary lists** inside the parent's
response (`MutationDTO.assays`, `MutationDTO.genes`, `MutationDTO.lesions`,
`MutationDTO.phenotypes`, `LineSubmissionDTO.mutations`,
`LineSubmissionDTO.linkedFeatures`, `AssayDTO.attachments`). The
summary carries enough for the parent UI to render a card; the full
record is fetched separately when that child is being edited.

`LinkedFeature` is the outlier — it sits on the submission (not a
mutation) and uses a composite PK `(submissionId, mutationAId,
mutationBId)`. The URL pins the pair as path segments; audit
`entity_id` flattens them as `"{submissionId}:{aId}:{bId}"`.

---

## 3. The FieldDescriptor pattern

The single most important pattern in this codebase. Every path that
the client can PATCH appears in a `Map<String, FieldDescriptor>` called
`FIELDS` on the corresponding form-schema class.

```java
public record FieldDescriptor(
        Function<Mutation, JsonNode> read,
        BiConsumer<Mutation, JsonNode> write) {}

public static final Map<String, FieldDescriptor> FIELDS = Map.ofEntries(
    field("/alleleDesignation",
        Mutation::getAlleleDesignation,
        (m, v) -> m.setAlleleDesignation(text(v))),
    field("/publications",
        Mutation::getPublications,
        (m, v) -> {
            m.getPublications().clear();
            if (v != null && v.isArray()) {
                for (int i = 0; i < v.size(); i++) {
                    String s = v.get(i).asText();
                    if (s != null && !s.isBlank()) {
                        m.getPublications().add(s.trim());
                    }
                }
            }
        }),
    ...
);
```

This one map gives us four things at once:

1. **Authorization gate** — unknown paths are rejected at the controller. Clients can't write columns the form doesn't expose.
2. **Audit log capture** — the same descriptor's `read()` produces the pre-update value, so `old`/`new` round-trip through Jackson consistently.
3. **Persistence** — `write()` applies typed coercion (trim, null-on-blank, etc.) before Hibernate sees the entity.
4. **Schema generation reference** — the `FIELDS` keys are the legal paths; if a uiSchema Control references a scope that isn't in `FIELDS`, the client's PATCH will 400 at runtime. Three tests guard against drift before it reaches the client: `FormSchemaSnapshotTest` locks down the serialized wire shape per form; `FormSchemaInvariantsTest` asserts the three-way alignment between `schema().properties`, `FIELDS.keySet()`, and the corresponding DTO record components (with per-form whitelists for intentional divergence like server-managed child lists and read-only denormalized columns); and `GenerateTypeScriptDriftTest` asserts the committed `home/javascript/react/zirc/api/types.ts` matches the output of `GenerateTypeScript.render()` from the Java DTOs.

   The TypeScript mirror is generated from the Java DTOs via `gradle generateZircTypes`. Jakarta `@NotNull` on a DTO record component is the signal that the field is server-set and always populated on the wire — the generator emits it as a non-nullable TS field. Per-page `FormDataShape` types derive from the generated DTO type via `FormFor<T>` (see `home/javascript/react/zirc/api/formHelpers.ts`), so an added field shows up in the React form without a per-page edit.

**Rule**: if you add a column to an entity that should be editable, you add a `FIELDS` entry, a `schema()` property, and a uiSchema Control. Forgetting any one is the most common bug pattern.

---

## 4. Schema and uiSchema construction

JSON Schema (`schema()`) describes **shape and constraints**. JSON
Forms uiSchema (`uiSchema()`) describes **layout and per-control
rendering hints**. Both are emitted as **typed records** from Java and
serialized to JSON by Jackson. JSON Schema records live under
`org.zfin.zirc.api.jsonschema` (`ObjectSchema`, `StringSchema`,
`BooleanSchema`, `NumberSchema`, `ArraySchema`, `ConstSchema`); uiSchema
records live under `org.zfin.zirc.api.uischema` (`VerticalLayout`,
`Group`, `Control`, `Options`, `Rule`). The `type` discriminator is
emitted via a `@JsonProperty("type")` accessor on each record so
Jackson's `SORT_PROPERTIES_ALPHABETICALLY` keeps the snapshot stable.

```java
public static JsonSchema schema() {
    Map<String, JsonSchema> properties = new LinkedHashMap<>();
    properties.put("alleleDesignation", StringSchema.of("Allele Designation", 255));
    properties.put("homozygousLethal",  BooleanSchema.nullable("Homozygous Lethal"));
    ...
    return ObjectSchema.of(properties);
}

public static UiSchemaElement uiSchema() {
    Rule showWhenLethal = Rule.showWhenTrue("#/properties/homozygousLethal");
    return new VerticalLayout(List.of(
        Group.of("General", List.of(
            Control.of("#/properties/alleleDesignation"),
            new Control("#/properties/homozygousLethal",
                    Options.of().widget("yesNoRadio"), null)
        )),
        Group.of("Lethality", List.of(
            new Control("#/properties/lethalityStageTypical",
                    Options.of().widget("selectWithOther").standardValues(LETHALITY_STAGES),
                    showWhenLethal)
        ))
    ));
}
```

Use the static factories (`StringSchema.of`, `BooleanSchema.nullable`,
`NumberSchema.of`, `ArraySchema.of`, `ObjectSchema.of`, `Group.of`,
`Control.of`, `Options.of`, `Rule.showWhenTrue`, `Rule.showWhenIn`)
for the common shapes; reach for the canonical constructors only when
you need to set fields the factories don't cover. Records that
combine multiple fluent options (`Options.of().widget(...).placeholder(...).helpText(...)`)
read top-to-bottom and serialize to the same JSON shape the older
Map-based builders did.

---

## 5. The `options` vocabulary (render metadata)

JSON Forms supports arbitrary keys under `options` on each Control,
and we use this as our render-metadata channel. The standard
vocabulary:

| Key             | Type                     | Meaning                                                                 |
|-----------------|--------------------------|-------------------------------------------------------------------------|
| `widget`        | string                   | Renderer dispatch — `yesNoRadio`, `selectWithOther`, `stringList`, etc. |
| `multi`         | boolean                  | Multi-line textarea instead of single-line input.                       |
| `standardValues`| string[]                 | For `selectWithOther` — the canonical enum values.                      |
| `layout`        | `"plain"`                | On a Group; drops the table-row wrapper for list-of-cards content.      |
| `placeholder`   | string                   | HTML placeholder attribute.                                             |
| `helpText`      | string                   | Renders as `<small class="text-muted">` under the input.                |
| `infoHref`      | URL string               | Renders as an "i" link next to the label, opens in a new tab.           |
| `suffix`        | string                   | Bootstrap-style input-group append text (e.g., `bp`).                   |
| `label`         | string                   | Override the default label-from-property-title.                         |

**Convention**: render-metadata that's curator-facing belongs in
`options`, not in the JSON Schema. The schema is for constraints
(`maxLength`, `enum`, `type`, `maxItems`); the uiSchema's `options`
are for how the form looks. Keep them on the right side of the line.

---

## 6. Conditional reveal

JSON Forms' `rule` blocks on uiSchema elements gate visibility on the
form's current data. The typed `Rule` record exposes two factories:

**Boolean reveal** — show fields only when a flag is true:

```java
Rule showWhenLethal = Rule.showWhenTrue("#/properties/homozygousLethal");
```

**Enum-membership reveal** — show a group when a discriminator is one of N values:

```java
Rule showForPcrTypes = Rule.showWhenIn(
        "#/properties/assayType",
        List.of("PCR", "RFLP", "dCAPS"));
```

The assay-type and lesion-type field matrices are just N
enum-membership rules — one per field cluster. The local
`groupRevealedFor(label, types, elements)` helper on
`ZircAssayFormSchema` / `ZircLesionFormSchema` wraps the pattern;
authoring the inverse (type → [field-list]) is fine in Java if it
reads better, and the helper transposes it to per-field rules at
uiSchema build time.

**Important**: Controls inherit rule-driven `visible` automatically
through `withJsonFormsControlProps`. Layout renderers
(`SectionRenderer`, etc.) do NOT — they receive
`withJsonFormsLayoutProps`, which exposes `props.visible` but doesn't
gate rendering. Custom layout renderers must check `if (visible ===
false) return null;` explicitly. Forgetting this silently ignores
group-level rules.

---

## 7. Row caps via `maxItems`

Per-aggregate row caps (max N mutations per submission, max M
attachments per assay) ride on standard JSON Schema `maxItems` in the
array property:

```java
private static ArraySchema mutationsSummaryArrayProp() {
    Map<String, JsonSchema> itemProps = new LinkedHashMap<>();
    itemProps.put("id",        NumberSchema.of());
    itemProps.put("sortOrder", NumberSchema.of());
    itemProps.put("alleleDesignation", StringSchema.nullable());
    return new ArraySchema("Mutations", ObjectSchema.of(itemProps),
            MAX_MUTATIONS_PER_SUBMISSION, null);
}
```

The list renderers read `props.schema.maxItems` and disable their "+
Add" button at capacity:

```tsx
const max = (schema as { maxItems?: number }).maxItems;
const atCap = max != null && (data ?? []).length >= max;
<button disabled={atCap || addMutation.isPending}
        title={atCap ? `Maximum ${max} per submission` : undefined}>
    + Add mutation
</button>
```

Server-side enforcement throws `IllegalArgumentException` from the
add method, which the global advice maps to a 400 ProblemDetail.

**Convention**: caps live in the schema, not in a separate `/caps`
endpoint or a TypeScript constants file. One fetch carries everything.

---

## 8. Per-field PATCH

The wire shape is always:

```http
PATCH /api/zirc/mutations/3
Content-Type: application/json

{ "path": "/forwardPrimer", "value": "GATCGATCGATC" }
```

Paths are **JSON Pointer**, rooted at the aggregate. We don't use
JSON Patch (the `op`-keyed array form) because we never need replace +
add + remove in one call — every change is a replace at a leaf.

Server flow:

```java
public Mutation updateMutationField(Long id, FieldUpdate u) {
    Mutation m = repository.getMutation(id);
    FieldDescriptor d = ZircMutationFormSchema.FIELDS.get(u.path());
    if (d == null) throw new IllegalArgumentException("Unknown path: " + u.path());

    JsonNode oldValue = d.read().apply(m);
    HibernateUtil.createTransaction();
    d.write().accept(m, u.value());
    writeAudit("mutation", String.valueOf(id), "update",
               u.path(), oldValue, u.value());
    HibernateUtil.flushAndCommitCurrentSession();
    return m;
}
```

The audit insert is **inside the same transaction** — if the main
commit fails, the audit row rolls back with it.

---

## 9. Concurrency

**At the persistence layer**: every entity carrying field-path PATCH
support has `@DynamicUpdate`. Hibernate's default is to UPDATE every
column on every commit; with `@DynamicUpdate` it UPDATEs only the
modified columns. Without this, two near-simultaneous PATCHes against
different fields of the same row would clobber each other.

**Required on**: `LineSubmission`, `Mutation`, `GenotypingAssay`,
`Gene`, `Lesion`, `Phenotype`, `LinkedFeature`. Forgetting it on a new
entity silently re-introduces lost updates.

**At the client**: no save queue today. The autosave fires PATCHes
serially within one debounce window. Across debounce windows there's
no ordering guarantee — an in-tab save queue is a reasonable addition
when we have test coverage to make it safe.

---

## 10. Audit trail

Every user-driven change writes one row to `zirc.audit`:

```sql
CREATE TABLE zirc.audit (
    ae_id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    ae_actor       TEXT NOT NULL,
    ae_entity_kind TEXT NOT NULL,    -- submission|mutation|assay|gene|lesion|phenotype|linked-feature
    ae_entity_id   TEXT NOT NULL,    -- entity id; "{sub}:{aId}:{bId}" for linked-feature
    ae_action      TEXT NOT NULL,    -- update|create-*|delete-*|upload|delete-file
    ae_path        TEXT,             -- JSON pointer for update; NULL for whole-row events
    ae_old_value   JSONB,
    ae_new_value   JSONB,
    ae_at          TIMESTAMP NOT NULL DEFAULT now()
);
```

Indexed on `(entity_kind, entity_id, at)` for "history for this row"
and `(actor, at)` for "what did this curator do."

Service-level helper `writeAudit(...)` is the only path that writes
to the table — and it also emits the legacy `ZIRC_AUDIT` log4j line
so existing log-grepping tooling still works.

---

## 11. Inline expansion as the UX pattern

Children are edited **inline**, not by navigating to a separate page:

- The submission edit page has a "Mutations" section. Each mutation is a card with an Edit button that opens a separate `/zirc/mutation/{id}/edit` page (the one exception — the mutation deserved its own route given the volume of fields).
- The submission edit page also has a "Linked Features" section — pairwise links between mutations, edited inline via a row-level renderer that PATCHes the composite-PK URL.
- The mutation edit page has four child sections: **Genotyping Assays**, **Genes**, **Lesions**, **Phenotypes**. Each row is a card that **expands inline** to mount a per-aggregate schema-driven editor (`AssayEdit`, `GeneEdit`, `LesionEdit`, `PhenotypeEdit`).
- Each expanded card's editor PATCHes its own aggregate's endpoint with flat paths. No array-index path resolver is needed because each card runs its own form scope.

The parent form's autosave diff filters out child-list paths
(`/mutations`, `/linkedFeatures`, `/assays`, `/genes`, `/lesions`,
`/phenotypes`, `/attachments`) via `EXTERNALLY_MANAGED_PATHS` so
Add/Delete (which use dedicated POST/DELETE endpoints) don't get
spuriously PATCHed. React Query refetches are then mirrored back
into local form state via per-list `JSON.stringify(...)`-keyed
effects, so the list renderers see the post-Add/Delete updates
without triggering autosave.

---

## 12. Error responses (RFC 7807)

`ZircApiExceptionHandler` is a `@RestControllerAdvice` scoped to
`org.zfin.zirc.api`. It produces `application/problem+json`:

| Exception                              | Status | Type URI                                          |
|----------------------------------------|--------|---------------------------------------------------|
| `ZircEntityNotFoundException`          | 404    | `https://zfin.org/problems/not-found`             |
| `IllegalArgumentException`             | 400    | `https://zfin.org/problems/bad-request`           |
| `MethodArgumentNotValidException`      | 422    | `https://zfin.org/problems/validation-failed`     |

`@Order(Ordered.HIGHEST_PRECEDENCE)` is required because the global
`ZfinGlobalExceptionHandler` has a catch-all `@ExceptionHandler(Exception.class)` that would otherwise render HTML error pages for our REST endpoints.

**Rule**: throw `IllegalArgumentException` (or a subclass of
`ZircEntityNotFoundException`) for known-bad inputs. Don't return
`ResponseEntity`s with custom statuses — the advice keeps the
response shape consistent.

---

## 13. OpenAPI contract — postponed

There is no published OpenAPI 3 spec for the ZIRC API right now. The
hand-curated YAML + drift test + vendored Swagger UI shell that
shipped during M1–M8 was removed because a per-feature OpenAPI surface
in an otherwise-undocumented codebase was a maintenance asymmetry —
new ZIRC endpoints had to mirror into the YAML or CI failed, while
the rest of the codebase had no such expectation, and curators of
other features were the ones paying the discovery cost.

See `reference/zirc-openapi-approach.md` for the original
decision-evaluation (springdoc / hand-curated / swagger-core
annotations) and for the "return-to-this" triggers — chief among them
a codebase-wide migration to Spring Boot, or a second feature area
independently growing an OpenAPI surface.

`FormSchemaSnapshotTest` continues to lock down the wire shape of the
form-schema responses. That's a narrower contract than OpenAPI but
it's the contract that actually constrains the React client today.

---

## 14. Where things live

```
source/org/zfin/zirc/
├── entity/             Hibernate entities (Lombok @Getter @Setter, @DynamicUpdate)
├── dto/                DTO records: AssayDTO, MutationDTO, GeneDTO, LesionDTO, PhenotypeDTO, … + GenerateTypeScript (main class for the TS-mirror task)
├── repository/         ZircSubmissionRepository + Hibernate impl
├── service/            ZircSubmissionService (the only mutator)
└── api/                Form schemas + controllers + exception advice
    ├── jsonschema/                   — typed JSON Schema records (ObjectSchema, StringSchema, …)
    ├── uischema/                     — typed uiSchema records (VerticalLayout, Group, Control, Options, Rule)
    ├── ZircFormSchema.java           — submission form: schema + uiSchema + FIELDS
    ├── ZircMutationFormSchema.java   — mutation form
    ├── ZircAssayFormSchema.java      — per-assay form (with assay-type matrix rules)
    ├── ZircGeneFormSchema.java       — per-gene form (marker autocomplete)
    ├── ZircLesionFormSchema.java     — per-lesion form (lesion-type matrix rules)
    ├── ZircPhenotypeFormSchema.java  — per-phenotype form (hpf/dpf timing widget)
    ├── ZircLinkedFeatureFormSchema.java — per-linkage form (composite PK)
    ├── ZircAutocompleteApiController.java — markers / features / persons type-ahead
    ├── Zirc*ApiController.java       — Spring MVC controllers
    └── ZircApiExceptionHandler.java  — RFC 7807 advice

home/javascript/react/zirc/
├── api/                              client + (generated) types + React Query hooks + formHelpers
├── pages/                            top-level page components
│   ├── MutationEdit.tsx              — mutation page (mounts 4 inline-expand child sections)
│   ├── AssayEdit.tsx                 — inline per-assay editor
│   ├── GeneEdit.tsx                  — inline per-gene editor
│   ├── LesionEdit.tsx                — inline per-lesion editor
│   └── PhenotypeEdit.tsx             — inline per-phenotype editor
├── components/                       small reusable bits
│   └── SaveStatusBadge.tsx           floating bottom-right toast
└── schemaForm/                       JSON Forms wrapper + renderers
    ├── SchemaForm.tsx                submission form mount
    └── renderers/                    custom Control + Layout renderers
        ├── SectionRenderer.tsx       Group → <section class="section">
        ├── RowControlRenderer.tsx    Control → table row in default layout
        ├── AssaysListRenderer.tsx    — server-managed inline-expand list (assays)
        ├── GenesListRenderer.tsx     — server-managed inline-expand list (genes)
        ├── LesionsListRenderer.tsx   — server-managed inline-expand list (lesions)
        ├── PhenotypesListRenderer.tsx — server-managed inline-expand list (phenotypes)
        ├── LinkedFeaturesListRenderer.tsx — submission-scope pairwise links
        ├── AutocompleteRenderer.tsx  — type-ahead ZDB-ID resolver
        ├── PhenotypeTimingRenderer.tsx — hpf/dpf unit toggle (UI-only)
        ├── ...

test/org/zfin/zirc/api/
├── FormSchemaSnapshotTest.java       JUnit 4 byte-level snapshot test
├── FormSchemaInvariantsTest.java     JUnit 4 schema ↔ FIELDS ↔ DTO drift check
└── ...
test/org/zfin/zirc/dto/
└── GenerateTypeScriptDriftTest.java  JUnit 4 — committed types.ts ↔ generator output
test/resources/zirc/snapshot/
├── submission.form-schema.json
├── mutation.form-schema.json
├── assay.form-schema.json
├── gene.form-schema.json
├── lesion.form-schema.json
└── phenotype.form-schema.json

source/org/zfin/db/postGmakePostloaddb/
├── 1182/migrations/zirc-line-submission.sql               original schema (PR #1845)
└── 1183/migrations/
    ├── zirc-relax-genotyping-assay-file-kind.xml          M4.3
    └── zirc-audit-table.xml                               audit
```

### Footprint by category

The schema-driven stack lands at ~79 files / ~8.0k LOC, split close to
50/50 between front end and back end. The bulk concentrates in three
places: `ZircSubmissionService` (the PATCH/audit dispatcher, ~814 LOC),
the seven `*FormSchema` classes (~1.5k LOC) that are the single source
of truth for shape, and the JSON Forms custom renderers (~1.9k LOC)
that cover shapes JSON Forms doesn't ship out of the box (inline-expand
list cards, multi-choice-with-Other, autocomplete-against-our-API,
hpf/dpf timing).

A useful read of the numbers is **per-aggregate fan-out**: each
aggregate carries a fixed bundle (entity + DTO + FormSchema +
ApiController + page component, plus a list renderer if it's a child).
Six aggregates × ~5 files = ~30 files of inherent multiplicity before
any shared scaffolding gets counted.

**Front end — 31 files / ~3,881 LOC**

| Group                                                          | Files | LOC   |
|----------------------------------------------------------------|-------|-------|
| `containers/` mount shims (3-line bootstraps)                  |     2 |     6 |
| `zirc/pages/` — one page per aggregate                         |     6 | 1,009 |
| `zirc/schemaForm/` shell (`SchemaForm.tsx` + `queryClient.ts`) |     2 |   271 |
| `zirc/schemaForm/renderers/` — JSON Forms custom renderers     |    17 | 1,929 |
| `zirc/api/` — `client.ts`, `queries.ts`, `types.ts`            |     3 |   576 |
| `zirc/components/SaveStatusBadge.tsx`                          |     1 |    90 |

**Back end — 48 files / ~4,131 LOC**

| Group                                                                                | Files | LOC   |
|--------------------------------------------------------------------------------------|-------|-------|
| REST controllers + `ZircApiExceptionHandler`                                         |     8 |   564 |
| Per-aggregate `*FormSchema.java` (`schema()` + `uiSchema()` + `FIELDS` dispatch map) |     7 | 1,484 |
| `api/jsonschema/` typed JSON Schema records                                          |     7 |   227 |
| `api/uischema/` typed UI Schema records                                              |     6 |   241 |
| DTOs (per-aggregate full + summary, plus `FieldUpdate`, `FormSchemaDTO`)             |    14 |   572 |
| Service layer (`ZircSubmissionService` + autocomplete + exception)                   |     3 |   902 |
| Repository (`ZircSubmissionRepository` + Hibernate impl)                             |     2 |   127 |
| `AuditEntry` entity                                                                  |     1 |    64 |

Counts taken at the commit immediately after the M1–M8 squash plus the
ZFIN-10265 obsolete-tree cleanup. They will drift; re-derive before
citing.

---

## 15. Gotchas (file these in your head)

1. **Lombok-unaware LSP false positives.** The Java LSP routinely reports `getX()` as undefined on Lombok-generated accessors. Trust `gradle compileJava`, not the editor.
2. **Spock specs in `test/` are dormant.** `useJUnitPlatform()` is commented out in `build.gradle`. Default to JUnit 4 Java for new tests until the Spock revival is done deliberately.
3. **JSP `<jsp:attribute>` blocks must be siblings only.** Comments between them break the parser (`Must use jsp:body to specify tag body for [<z:dataPage>] if jsp:attribute is used`). Put comments inside the attribute body.
4. **`@DeleteMapping` returning void defaults to 200 with empty body.** The client's `response.json()` then throws on the empty body. Add `@ResponseStatus(HttpStatus.NO_CONTENT)` to every void delete.
5. **`SectionRenderer` had to opt in to rule-driven visibility.** Custom layout renderers must `if (visible === false) return null;` — layouts don't inherit the gate that Controls get for free.
6. **`@DynamicUpdate` is per-entity.** Forgetting it on a new entity that carries field-path PATCH silently re-introduces lost updates.
7. **No JSON Pointer array-index resolver yet.** Don't author paths like `/mutations/0/alleleDesignation`. Each aggregate gets its own flat-path namespace.
8. **Liquibase tracker drift on local dev DBs.** The dev DB's `DATABASECHANGELOG` thinks several already-applied changesets are pending; `gradle liquibasePostBuild` fails. We've been applying new migrations via `psql` directly for the local session. The committed XML is correct for CI.

---

## 16. Adding a new field — the checklist

You almost always touch six places. In this order:

1. **DB**: Liquibase changeset under `source/org/zfin/db/postGmakePostloaddb/{version}/migrations/`. Run via `gradle liquibasePostBuild` (or `psql` for local).
2. **Entity**: column in the relevant `@Entity` class.
3. **DTO**: field in the relevant DTO record + the `of(entity)` mapping.
4. **FIELDS map**: an entry in the relevant `Zirc*FormSchema.FIELDS` with `(getter, setter)`.
5. **JSON Schema**: a property in `Zirc*FormSchema.schema()` (typed record under `org.zfin.zirc.api.jsonschema`).
6. **uiSchema**: a Control (with options) in the appropriate Group in `Zirc*FormSchema.uiSchema()`.
7. **TS type**: regenerate `home/javascript/react/zirc/api/types.ts` via `gradle generateZircTypes`. The file is auto-generated from the DTO records; never hand-edit. `GenerateTypeScriptDriftTest` fails CI if you forget. Per-page `FormDataShape` derives from the generated DTO type via `FormFor<T>` (see `home/javascript/react/zirc/api/formHelpers.ts`), so no per-page edit is needed unless you add a new page.
8. **Snapshot**: rerun `gradle test --tests org.zfin.zirc.api.FormSchemaSnapshotTest -Pzirc.snapshot.update=true`, then review the diff under `test/resources/zirc/snapshot/` before committing.

That's it. If the field is editable, no React change is needed —
JSON Forms picks it up from the schema. If it needs a custom renderer
(e.g., a new widget type), register it in the page's `renderers` array.

**Adding a new child aggregate** is the same checklist times two: do
it for the new aggregate (one row in §2), and add a summary-list
property + uiSchema Group to the parent's form schema with
`widget: "<aggregate>List"`. The recent M6.1 (genes) / M7.1
(lesions) / M8.1 (phenotypes) commits are worked examples on this
branch.
