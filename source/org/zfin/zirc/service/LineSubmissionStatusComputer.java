package org.zfin.zirc.service;

import org.zfin.zirc.api.ZircFormSchema;
import org.zfin.zirc.api.jsonschema.JsonSchema;
import org.zfin.zirc.api.jsonschema.ObjectSchema;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.Mutation;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * System-derived per-field and per-section status for a {@link LineSubmission}.
 * No new DB state — the result is recomputed from the entity on each render.
 * A follow-up phase will persist this so curators can override the derived
 * value and the dashboard can filter without recomputing.
 */
public final class LineSubmissionStatusComputer {

    public enum FieldStatus {
        MISSING     ("M",  "Missing",     "badge-danger"),
        IN_PROGRESS ("IP", "In Progress", "badge-warning"),
        COMPLETE    ("C",  "Complete",    "badge-success"),
        APPROVED    ("A",  "Approved",    "badge-primary");

        private final String abbreviation;
        private final String displayName;
        private final String cssClass;

        FieldStatus(String abbreviation, String displayName, String cssClass) {
            this.abbreviation = abbreviation;
            this.displayName  = displayName;
            this.cssClass     = cssClass;
        }

        public String getAbbreviation() { return abbreviation; }
        public String getDisplayName()  { return displayName; }
        public String getCssClass()     { return cssClass; }

        /** Severity-aware merge: returns the worse (lower-ordinal) of the two. */
        public FieldStatus worse(FieldStatus other) {
            if (other == null) return this;
            return this.ordinal() < other.ordinal() ? this : other;
        }
    }

    /**
     * Catalogue of LineSubmission fields that participate in status computation.
     * Each constant carries the JavaBean property path (used both as the
     * JSP map key and to look up the getter via reflection).
     *
     * Required-ness is no longer carried here — it lives on the schema's
     * {@code required} arrays (see {@link ZircFormSchema#schema()}), which
     * {@link #REQUIRED_PATHS} unions at class init.
     *
     * Optional fields are always COMPLETE — empty is a valid terminal state.
     * Required fields are MISSING when empty, COMPLETE otherwise.
     * IN_PROGRESS is reserved for the future curator workflow and is not
     * derived here.
     */
    public enum Field {
        NAME                        ("name"),
        PREVIOUS_NAMES              ("previousNames"),
        ABBREVIATION                ("abbreviation"),
        REASONS                     ("reasons"),
        MUTATIONS                   ("mutations"),
        LINKED_FEATURES             ("linkedFeatures"),
        MATERNAL_BACKGROUND         ("maternalBackground"),
        PATERNAL_BACKGROUND         ("paternalBackground"),
        BACKGROUND_CHANGEABLE       ("backgroundChangeable"),
        // Conditional applicability — the compute loop skips it unless
        // backgroundChangeable is explicitly No. Optional even when applicable.
        BACKGROUND_CHANGE_CONCERNS  ("backgroundChangeConcerns"),
        SINGLE_ALLELIC              ("singleAllelic"),
        HUSBANDRY_INFO              ("husbandryInfo"),
        UNREPORTED_FEATURES_DETAILS ("unreportedFeaturesDetails"),
        ADDITIONAL_INFO             ("additionalInfo");

        private final String path;

        Field(String path) { this.path = path; }

        public String getPath() { return path; }
    }

    /**
     * Union of every {@code required} list across the LineSubmission schema
     * (root + nested object schemas). Computed once at class load — the schema
     * is static.
     */
    private static final Set<String> REQUIRED_PATHS = collectRequiredPaths(ZircFormSchema.schema());

    private static Set<String> collectRequiredPaths(JsonSchema node) {
        Set<String> out = new LinkedHashSet<>();
        if (node instanceof ObjectSchema obj) {
            if (obj.required() != null) out.addAll(obj.required());
            if (obj.properties() != null) {
                for (JsonSchema child : obj.properties().values()) {
                    out.addAll(collectRequiredPaths(child));
                }
            }
        }
        return out;
    }

    private static FieldStatus statusFor(LineSubmission s, String path) {
        Object value = readProperty(s, path);
        if (isEmpty(value) && REQUIRED_PATHS.contains(path)) return FieldStatus.MISSING;
        return FieldStatus.COMPLETE;
    }

    public record FieldStatusResult(
            Map<String, FieldStatus> byField,
            Map<String, FieldStatus> bySection,
            FieldStatus overall) {}

    private LineSubmissionStatusComputer() {}

    public static FieldStatusResult compute(LineSubmission s) {
        Map<String, FieldStatus> byField = new LinkedHashMap<>();
        for (Field f : Field.values()) {
            // backgroundChangeConcerns only applies when changeable == FALSE.
            if (f == Field.BACKGROUND_CHANGE_CONCERNS
                    && !Boolean.FALSE.equals(s.getBackgroundChangeable())) {
                continue;
            }
            byField.put(f.getPath(), statusFor(s, f.getPath()));
        }

        // Section rollups. Section keys match the JSP labels in
        // line-submission-detail.jsp. People is excluded — submitter is
        // auto-added on first save, so it can't really go missing.
        Map<String, FieldStatus> bySection = new LinkedHashMap<>();
        bySection.put("Overview",        rollup(byField, Field.NAME, Field.PREVIOUS_NAMES, Field.REASONS));
        // Mutations section is worst-of (presence-check, each mutation's own overall),
        // so a present-but-incomplete mutation bubbles up Missing — not just absence.
        FieldStatus mutationsSection = rollup(byField, Field.MUTATIONS);
        if (s.getMutations() != null) {
            for (Mutation m : s.getMutations()) {
                mutationsSection = mutationsSection.worse(MutationStatusComputer.compute(m).overall());
            }
        }
        bySection.put("Mutations",       mutationsSection);
        bySection.put("Linked Features", rollup(byField, Field.LINKED_FEATURES));
        bySection.put("Background",      rollup(byField,
                Field.MATERNAL_BACKGROUND, Field.PATERNAL_BACKGROUND,
                Field.BACKGROUND_CHANGEABLE, Field.BACKGROUND_CHANGE_CONCERNS));
        bySection.put("Additional Info", rollup(byField,
                Field.ADDITIONAL_INFO, Field.UNREPORTED_FEATURES_DETAILS));

        FieldStatus overall = FieldStatus.COMPLETE;
        for (FieldStatus st : bySection.values()) overall = overall.worse(st);

        return new FieldStatusResult(byField, bySection, overall);
    }

    private static FieldStatus rollup(Map<String, FieldStatus> byField, Field... fields) {
        FieldStatus worst = FieldStatus.COMPLETE;
        for (Field f : fields) {
            FieldStatus st = byField.get(f.getPath());
            if (st != null) worst = worst.worse(st);
        }
        return worst;
    }

    // ─── Reflective property access ─────────────────────────────────────────

    private static Object readProperty(Object bean, String propertyName) {
        String method = "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        try {
            Method m = bean.getClass().getMethod(method);
            return m.invoke(bean);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Cannot read property '" + propertyName + "' (no public " + method
                            + "() on " + bean.getClass().getName() + ")", e);
        }
    }

    private static boolean isEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String s) return s.isBlank();
        if (value instanceof Object[] arr) return arr.length == 0;
        if (value instanceof Collection<?> c) return c.isEmpty();
        return false; // Booleans (or any other reference value) are non-empty when non-null.
    }
}