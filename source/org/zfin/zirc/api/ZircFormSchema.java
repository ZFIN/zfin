package org.zfin.zirc.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zfin.zirc.api.jsonschema.ArraySchema;
import org.zfin.zirc.api.jsonschema.BooleanSchema;
import org.zfin.zirc.api.jsonschema.ConstSchema;
import org.zfin.zirc.api.jsonschema.JsonSchema;
import org.zfin.zirc.api.jsonschema.NumberSchema;
import org.zfin.zirc.api.jsonschema.ObjectSchema;
import org.zfin.zirc.api.jsonschema.StringSchema;
import org.zfin.zirc.api.uischema.Control;
import org.zfin.zirc.api.uischema.Group;
import org.zfin.zirc.api.uischema.Options;
import org.zfin.zirc.api.uischema.UiSchemaElement;
import org.zfin.zirc.api.uischema.VerticalLayout;
import org.zfin.zirc.entity.LineSubmission;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Single source of truth for the ZIRC submission form. Defines:
 *   - the JSON Schema describing data shape + constraints
 *   - the JSON Forms uiSchema describing layout + per-field widget choice
 *   - the path → entity-field descriptor table used by the field-path PATCH
 *     endpoint (read for audit log capture, write for persistence)
 *
 * <p>Hand-built for now. The descriptor table grew to a manageable ~11 entries
 * covering all four root-level sections; for nested entities (mutations[i],
 * genes[j]) a path resolver will be needed — that's M3+ scope.
 */
public final class ZircFormSchema {

    private ZircFormSchema() {}

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Read + write for a single form-schema path. Audit log uses read; PATCH uses write. */
    public record FieldDescriptor(
            Function<LineSubmission, JsonNode> read,
            BiConsumer<LineSubmission, JsonNode> write) {
    }

    /**
     * Canonical acceptance-reasons list. Renaming a label is a code-only
     * change; renaming a value requires a data migration because existing
     * rows in line_submission.ls_reasons store the old value.
     */
    private static final List<ConstSchema> CANONICAL_REASONS = List.of(
            new ConstSchema("frequently_requested",      "Currently frequently requested"),
            new ConstSchema("expect_high_demand",        "Expect high demand"),
            new ConstSchema("interesting_gene",          "Interesting gene"),
            new ConstSchema("community_resource",        "Community resource/tool"),
            new ConstSchema("mutant_gene_cloned",        "Mutant gene cloned"),
            new ConstSchema("danger_of_losing",          "Danger of losing line"),
            new ConstSchema("lack_of_space_or_funding",  "Lack of space or funding to maintain line"),
            new ConstSchema("other",                     "Other")
    );

    public static JsonSchema schema() {
        Map<String, JsonSchema> acceptance = new LinkedHashMap<>();
        acceptance.put("reasons", reasonsArrayProp());
        acceptance.put("reasonsOther", StringSchema.of("Other reason", 2000));
        // Acceptance: at least one reason must be selected. reasonsOther is
        // gated by the "other" canonical value being included, so it's not
        // listed as universally required here.
        ObjectSchema acceptanceSchema =
                ObjectSchema.of("Acceptance Reasons", acceptance, List.of("reasons"));

        Map<String, JsonSchema> background = new LinkedHashMap<>();
        background.put("singleAllelic",        BooleanSchema.nullable("Single-allelic submission"));
        background.put("maternalBackground",   StringSchema.of("Maternal", 255));
        background.put("paternalBackground",   StringSchema.of("Paternal", 255));
        background.put("backgroundChangeable", BooleanSchema.nullable("Background Changeable"));
        ObjectSchema backgroundSchema = ObjectSchema.of("Background", background,
                List.of("maternalBackground", "paternalBackground", "backgroundChangeable"));

        Map<String, JsonSchema> additionalInfo = new LinkedHashMap<>();
        additionalInfo.put("unreportedFeaturesDetails", StringSchema.of("Unreported Features Details", 5000));
        additionalInfo.put("husbandryInfo",             StringSchema.of("Husbandry Info", 5000));
        additionalInfo.put("additionalInfo",            StringSchema.of("Additional Info", 5000));

        Map<String, JsonSchema> properties = new LinkedHashMap<>();
        properties.put("name",            StringSchema.of("Line Name", 255));
        properties.put("previousNames",   StringSchema.of("Previous Names", 2000));
        properties.put("submitterNames",  StringSchema.readOnly("Submitter"));
        properties.put("createdAt",       StringSchema.readOnly("Date Started"));
        properties.put("updatedAt",       StringSchema.readOnly("Last Updated"));
        properties.put("acceptance",      acceptanceSchema);
        properties.put("mutations",       mutationsSummaryArrayProp());
        properties.put("linkedFeatures",  linkedFeaturesArrayProp());
        properties.put("background",      backgroundSchema);
        properties.put("additionalInfo",  ObjectSchema.of("Additional Info", additionalInfo));
        // Top-level required: name and mutations. The acceptance and background
        // nested objects carry their own required lists.
        return ObjectSchema.of(null, properties, List.of("name", "mutations"));
    }

    /**
     * JSON Forms uiSchema. VerticalLayout of Groups, with widget hints on
     * Controls that need custom rendering (radios, selects, multi-field
     * composites, multi-line textareas).
     */
    public static UiSchemaElement uiSchema() {
        List<String> backgroundValues = List.of("AB", "TU", "WIK", "AB/TU", "unknown");
        return new VerticalLayout(List.of(
                Group.of("Overview", List.of(
                        new Control("#/properties/name",
                                Options.of()
                                        .placeholder("e.g. nasl1<sup>zf123</sup>")
                                        .helpText("Line name as it should appear in publications."),
                                null),
                        new Control("#/properties/previousNames",
                                Options.of()
                                        .placeholder("Comma-separated former names")
                                        .helpText("Useful when this line was previously known by a different designation."),
                                null),
                        new Control("#/properties/submitterNames",
                                Options.of().comments(false), null),
                        new Control("#/properties/createdAt",
                                Options.of().comments(false), null),
                        new Control("#/properties/updatedAt",
                                Options.of().comments(false), null),
                        new Control("#/properties/acceptance",
                                Options.of()
                                        .widget("multipleChoiceWithOther")
                                        .label("Acceptance Reasons"),
                                null)
                )),
                // Mutations is structurally different from the field sections —
                // a list of child rows, edited on their own pages. The "plain"
                // layout option tells SectionRenderer to drop the table wrapper.
                new Group("Mutations",
                        List.of(new Control("#/properties/mutations",
                                Options.of().widget("mutationsList"),
                                null)),
                        Options.of().layout("plain"),
                        null),
                // Linked features: pairwise links between mutations on the
                // same submission. List-of-cards layout like Mutations.
                new Group("Linked Features",
                        List.of(new Control("#/properties/linkedFeatures",
                                Options.of().widget("linkedFeaturesList"),
                                null)),
                        Options.of().layout("plain"),
                        null),
                Group.of("Background", List.of(
                        new Control("#/properties/background/properties/singleAllelic",
                                Options.of().widget("yesNoRadio"), null),
                        new Control("#/properties/background/properties/maternalBackground",
                                Options.of().widget("selectWithOther").standardValues(backgroundValues),
                                null),
                        new Control("#/properties/background/properties/paternalBackground",
                                Options.of().widget("selectWithOther").standardValues(backgroundValues),
                                null),
                        new Control("#/properties/background/properties/backgroundChangeable",
                                Options.of().widget("yesNoRadio"), null)
                )),
                Group.of("Additional Info", List.of(
                        new Control("#/properties/additionalInfo/properties/unreportedFeaturesDetails",
                                Options.of().multi(true), null),
                        new Control("#/properties/additionalInfo/properties/husbandryInfo",
                                Options.of().multi(true)
                                        .placeholder("Husbandry-specific information, e.g. special feeding regime"),
                                null),
                        new Control("#/properties/additionalInfo/properties/additionalInfo",
                                Options.of().multi(true), null)
                ))
        ));
    }

    /**
     * Path → read+write dispatch. The PATCH endpoint rejects any path not in
     * this map (the form schema is the gatekeeper, no untyped string dispatch).
     * Audit-log old-value capture uses the same descriptor's read.
     */
    public static final Map<String, FieldDescriptor> FIELDS = Map.ofEntries(
            field("/name",                       LineSubmission::getName,                       (s, v) -> s.setName(text(v))),
            field("/previousNames",              LineSubmission::getPreviousNames,              (s, v) -> s.setPreviousNames(text(v))),
            // Acceptance: stored flat on the entity (reasons/reasonsOther) but nested in the form schema
            field("/acceptance/reasons",         s -> s.getReasons() == null ? new String[0] : s.getReasons(),
                                                                                                (s, v) -> s.setReasons(stringArray(v))),
            field("/acceptance/reasonsOther",    LineSubmission::getReasonsOther,               (s, v) -> s.setReasonsOther(text(v))),
            // Background
            field("/background/singleAllelic",        LineSubmission::getSingleAllelic,         (s, v) -> s.setSingleAllelic(boolNullable(v))),
            field("/background/maternalBackground",   LineSubmission::getMaternalBackground,    (s, v) -> s.setMaternalBackground(text(v))),
            field("/background/paternalBackground",   LineSubmission::getPaternalBackground,    (s, v) -> s.setPaternalBackground(text(v))),
            field("/background/backgroundChangeable", LineSubmission::getBackgroundChangeable,  (s, v) -> s.setBackgroundChangeable(boolNullable(v))),
            // Additional Info
            field("/additionalInfo/unreportedFeaturesDetails", LineSubmission::getUnreportedFeaturesDetails,
                                                                                                (s, v) -> s.setUnreportedFeaturesDetails(text(v))),
            field("/additionalInfo/husbandryInfo",  LineSubmission::getHusbandryInfo,           (s, v) -> s.setHusbandryInfo(text(v))),
            field("/additionalInfo/additionalInfo", LineSubmission::getAdditionalInfo,          (s, v) -> s.setAdditionalInfo(text(v)))
    );

    // ─── schema builders ────────────────────────────────────────────────────
    // (Schema records live in org.zfin.zirc.api.jsonschema; helpers below
    //  return the typed records directly. StringSchema.of / BooleanSchema.nullable
    //  / ObjectSchema.of are the everyday factories.)

    /** Hard cap mirroring the alt-branch (ZFIN-10265) form spec. */
    public static final int MAX_MUTATIONS_PER_SUBMISSION = 5;

    /**
     * Schema for the mutations summary list shown on the submission page.
     * Items are read-only summaries — editing happens on the per-mutation
     * page. The shape mirrors {@link org.zfin.zirc.dto.MutationDTO}.
     */
    private static ArraySchema mutationsSummaryArrayProp() {
        Map<String, JsonSchema> itemProps = new LinkedHashMap<>();
        itemProps.put("id",                NumberSchema.of());
        itemProps.put("lineSubmissionId",  new StringSchema(null, null, null, null, null));
        itemProps.put("sortOrder",         NumberSchema.of());
        itemProps.put("alleleDesignation", StringSchema.nullable());
        itemProps.put("alleleInZfin",      new BooleanSchema(null, Boolean.TRUE));
        itemProps.put("mutationType",      StringSchema.nullable());
        return new ArraySchema("Mutations", ObjectSchema.of(itemProps),
                MAX_MUTATIONS_PER_SUBMISSION, null);
    }

    private static ArraySchema reasonsArrayProp() {
        return new ArraySchema(null, StringSchema.withOneOf(CANONICAL_REASONS),
                null, Boolean.TRUE);
    }

    /**
     * Schema for the linked-features list on the submission page. Items
     * mirror {@link org.zfin.zirc.dto.LinkedFeatureDTO}. Editing happens
     * inline via the LinkedFeaturesListRenderer; Add/Delete + per-field
     * PATCH go through dedicated endpoints under
     * {@code /line-submissions/{zdbID}/linked-features/...}.
     */
    private static ArraySchema linkedFeaturesArrayProp() {
        Map<String, JsonSchema> itemProps = new LinkedHashMap<>();
        itemProps.put("mutationAId",          NumberSchema.of());
        itemProps.put("mutationBId",          NumberSchema.of());
        itemProps.put("distanceKnown",        new BooleanSchema(null, Boolean.TRUE));
        itemProps.put("distanceCentimorgans", new NumberSchema(null, Boolean.TRUE));
        itemProps.put("distanceMegabases",    new NumberSchema(null, Boolean.TRUE));
        itemProps.put("additionalInfo",       StringSchema.nullable());
        return new ArraySchema("Linked Features", ObjectSchema.of(itemProps), null, null);
    }

    // ─── uiSchema builders ──────────────────────────────────────────────────
    // (now in org.zfin.zirc.api.uischema; construct VerticalLayout/Group/Control
    //  records directly above, using Group.of / Control.of for the shorthand cases.)

    private static Map<String, String> entry(String value, String label) {
        Map<String, String> e = new LinkedHashMap<>();
        e.put("const", value);
        e.put("title", label);
        return e;
    }

    // ─── descriptor builders ────────────────────────────────────────────────

    private static Map.Entry<String, FieldDescriptor> field(
            String path,
            Function<LineSubmission, ?> getter,
            BiConsumer<LineSubmission, JsonNode> setter) {
        return Map.entry(path, new FieldDescriptor(
                s -> MAPPER.valueToTree(getter.apply(s)),
                setter));
    }

    // ─── value coercers (JsonNode → Java) ──────────────────────────────────

    private static String text(JsonNode v) {
        if (v == null || v.isNull()) {return null;}
        String s = v.asText();
        return s.isBlank() ? null : s.trim();
    }

    private static Boolean boolNullable(JsonNode v) {
        if (v == null || v.isNull()) {return null;}
        return v.asBoolean();
    }

    private static String[] stringArray(JsonNode v) {
        if (v == null || v.isNull() || !v.isArray()) {return new String[0];}
        String[] result = new String[v.size()];
        for (int i = 0; i < v.size(); i++) {
            result[i] = v.get(i).asText();
        }
        return result;
    }
}
