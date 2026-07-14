package org.zfin.zirc.api;

import org.junit.Test;
import org.zfin.zirc.api.jsonschema.JsonSchema;
import org.zfin.zirc.api.jsonschema.ObjectSchema;
import org.zfin.zirc.api.uischema.UiSchemaElement;
import org.zfin.zirc.dto.AssayDTO;
import org.zfin.zirc.dto.GeneDTO;
import org.zfin.zirc.dto.LesionDTO;
import org.zfin.zirc.dto.LineSubmissionDTO;
import org.zfin.zirc.dto.LinkedFeatureDTO;
import org.zfin.zirc.dto.MutationDTO;
import org.zfin.zirc.dto.PhenotypeDTO;

import java.lang.reflect.RecordComponent;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

/**
 * Cross-layer alignment check for the ZIRC form schemas.
 *
 * <p>Each {@code Zirc*FormSchema} declares three things about the same set of
 * fields: the JSON Schema (what the wire looks like), the FIELDS dispatch
 * map (which paths the server will accept on PATCH and how to write them),
 * and an implicit DTO contract (which fields end up in the response).
 * Drift between these is the most common bug pattern when adding or
 * renaming a field — see {@code reference/zirc-architecture.md} §3 for
 * the FieldDescriptor pattern that ties them together.
 *
 * <p>This test asserts two invariants per form schema:
 * <ol>
 *   <li><b>schema().properties (flattened) ↔ FIELDS.keySet():</b>
 *       every JSON Schema leaf-path must appear in FIELDS, modulo a
 *       per-form whitelist of paths that are server-managed via dedicated
 *       endpoints (POST/DELETE on child collections).</li>
 *   <li><b>DTO record components ↔ schema().properties (flattened
 *       leaf segments):</b> every editable DTO component must appear as
 *       a schema leaf, modulo a per-form whitelist of components that are
 *       deliberately read-only (server-set IDs, denormalized display
 *       columns, derived booleans).</li>
 * </ol>
 *
 * <p>The whitelists are <i>intentional</i> divergence, declared explicitly
 * below so accidental divergence still fails the test.
 *
 * <p>JUnit 4 to stay in the test path that actually runs in CI (see
 * memory note on dormant Spock specs).
 */
public class FormSchemaInvariantsTest {

    @Test
    public void submissionSchemaIsConsistent() {
        check(new Spec(
                "Submission",
                ZircFormSchema.schema(),
                ZircFormSchema.uiSchema(),
                ZircFormSchema.FIELDS.keySet(),
                LineSubmissionDTO.class,
                // DTO components not in the form. zdbID is the PK
                // (server-set on create); abbreviation and
                // backgroundChangeConcerns are columns curators don't
                // edit through this form; draft is a server-flipped flag.
                // createdAt / updatedAt / submitterNames / piNames are
                // server-managed metadata read by the detail-page
                // StatusOverviewBar but not rendered in any form section.
                Set.of("zdbID", "abbreviation", "backgroundChangeConcerns", "draft",
                        "createdAt", "updatedAt", "submitterNames", "piNames"),
                // Schema paths managed through dedicated POST/DELETE
                // endpoints rather than the field-path PATCH.
                Set.of("/mutations", "/linkedFeatures")));
    }

    @Test
    public void mutationSchemaIsConsistent() {
        check(new Spec(
                "Mutation",
                ZircMutationFormSchema.schema(),
                ZircMutationFormSchema.uiSchema(),
                ZircMutationFormSchema.FIELDS.keySet(),
                MutationDTO.class,
                // alleleName is server-resolved from Feature.abbreviation when
                // alleleDesignation holds a ZDB-ID; it's display-only, never
                // in the form schema.
                Set.of("id", "lineSubmissionId", "sortOrder", "alleleName"),
                Set.of("/assays", "/genes", "/lesions", "/phenotypes")));
    }

    @Test
    public void assaySchemaIsConsistent() {
        check(new Spec(
                "Assay",
                ZircAssayFormSchema.schema(),
                ZircAssayFormSchema.uiSchema(),
                ZircAssayFormSchema.FIELDS.keySet(),
                AssayDTO.class,
                Set.of("id", "mutationId", "sortOrder"),
                Set.of("/attachments")));
    }

    @Test
    public void geneSchemaIsConsistent() {
        check(new Spec(
                "Gene",
                ZircGeneFormSchema.schema(),
                ZircGeneFormSchema.uiSchema(),
                ZircGeneFormSchema.FIELDS.keySet(),
                GeneDTO.class,
                // mutatedGeneAbbreviation is denormalized for display;
                // the abbreviation surfaces in the autocomplete result,
                // not as an editable field.
                Set.of("id", "mutationId", "sortOrder", "mutatedGeneAbbreviation"),
                Set.of()));
    }

    @Test
    public void lesionSchemaIsConsistent() {
        check(new Spec(
                "Lesion",
                ZircLesionFormSchema.schema(),
                ZircLesionFormSchema.uiSchema(),
                ZircLesionFormSchema.FIELDS.keySet(),
                LesionDTO.class,
                // locationInline is no longer edited through the form (its
                // box was removed) but the column/DTO component remains.
                Set.of("id", "mutationId", "sortOrder", "locationInline"),
                Set.of()));
    }

    @Test
    public void phenotypeSchemaIsConsistent() {
        check(new Spec(
                "Phenotype",
                ZircPhenotypeFormSchema.schema(),
                ZircPhenotypeFormSchema.uiSchema(),
                ZircPhenotypeFormSchema.FIELDS.keySet(),
                PhenotypeDTO.class,
                Set.of("id", "mutationId", "sortOrder"),
                Set.of()));
    }

    @Test
    public void linkedFeatureSchemaIsConsistent() {
        // LinkedFeature has no per-row /form-schema endpoint — the
        // submission-scope LinkedFeaturesListRenderer drives editing
        // inline against the composite-PK URL, so there's no
        // schema() / uiSchema() to align. We still want the FIELDS-to-DTO
        // half of the invariant: the FIELDS keys must match the DTO's
        // editable components.
        check(new Spec(
                "LinkedFeature",
                /* schema */ null,
                /* uiSchema */ null,
                ZircLinkedFeatureFormSchema.FIELDS.keySet(),
                LinkedFeatureDTO.class,
                // The composite PK is in the URL, not patched as fields.
                Set.of("mutationAId", "mutationBId"),
                Set.of()));
    }

    // ─── plumbing ──────────────────────────────────────────────────────

    private record Spec(
            String name,
            JsonSchema schema,
            UiSchemaElement uiSchema,
            Set<String> fieldsKeys,
            Class<?> dtoClass,
            Set<String> dtoExempt,
            Set<String> schemaExempt) {}

    private static void check(Spec spec) {
        Set<String> dtoComponentNames = recordComponentNames(spec.dtoClass);
        Set<String> dtoEditable = new TreeSet<>(dtoComponentNames);
        dtoEditable.removeAll(spec.dtoExempt);

        if (spec.schema == null) {
            // No /form-schema endpoint for this aggregate (LinkedFeature
            // is inline-edited). The remaining invariant is FIELDS ↔ DTO.
            Set<String> fieldSegments = new TreeSet<>();
            for (String key : spec.fieldsKeys) {
                int slash = key.lastIndexOf('/');
                fieldSegments.add(slash >= 0 ? key.substring(slash + 1) : key);
            }
            assertEquals(
                    "FIELDS keys (minus dtoExempt) must match the DTO components for "
                            + spec.name + ".",
                    dtoEditable,
                    fieldSegments);
            return;
        }

        Set<String> schemaLeaves = flattenLeafPaths(spec.schema);
        Set<String> patchableLeaves = new TreeSet<>(schemaLeaves);
        patchableLeaves.removeAll(spec.schemaExempt);

        assertEquals(
                "FIELDS.keySet() must match the schema's leaf paths for "
                        + spec.name + " (minus the schemaExempt whitelist). "
                        + "If a path was added on one side without the other, this assertion fails — "
                        + "see reference/zirc-architecture.md §3.",
                patchableLeaves,
                new TreeSet<>(spec.fieldsKeys));

        Set<String> schemaLeafSegments = new TreeSet<>();
        for (String leafPath : schemaLeaves) {
            int slash = leafPath.lastIndexOf('/');
            schemaLeafSegments.add(slash >= 0 ? leafPath.substring(slash + 1) : leafPath);
        }

        assertEquals(
                "DTO components (minus dtoExempt) must match the set of schema leaf segments for "
                        + spec.name + ". An added DTO field with no schema entry would silently "
                        + "round-trip past the form; a schema leaf with no DTO component would 500 on PATCH.",
                dtoEditable,
                schemaLeafSegments);
    }

    /**
     * Walk an ObjectSchema's properties recursively and produce the set
     * of leaf JSON Pointer paths. Non-object subtrees (strings, numbers,
     * booleans, arrays) terminate the walk and contribute their own path
     * as a leaf. The output paths begin with {@code "/"} so they line up
     * with FIELDS keys directly.
     */
    private static Set<String> flattenLeafPaths(JsonSchema root) {
        Set<String> out = new LinkedHashSet<>();
        if (root instanceof ObjectSchema obj) {
            walk(obj.properties(), "", out);
        } else {
            throw new IllegalStateException("Root schema must be an ObjectSchema; got " + root);
        }
        return out;
    }

    private static void walk(Map<String, JsonSchema> properties, String prefix, Set<String> out) {
        for (Map.Entry<String, JsonSchema> e : properties.entrySet()) {
            String path = prefix + "/" + e.getKey();
            if (e.getValue() instanceof ObjectSchema nested) {
                walk(nested.properties(), path, out);
            } else {
                out.add(path);
            }
        }
    }

    private static Set<String> recordComponentNames(Class<?> dtoClass) {
        if (!dtoClass.isRecord()) {
            throw new IllegalArgumentException(dtoClass + " must be a record");
        }
        Set<String> names = new LinkedHashSet<>();
        for (RecordComponent c : dtoClass.getRecordComponents()) {
            names.add(c.getName());
        }
        return names;
    }
}
