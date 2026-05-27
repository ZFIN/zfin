package org.zfin.zirc.service;

import org.zfin.zirc.api.ZircPhenotypeFormSchema;
import org.zfin.zirc.api.jsonschema.JsonSchema;
import org.zfin.zirc.api.jsonschema.ObjectSchema;
import org.zfin.zirc.entity.Phenotype;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Per-field status for one {@link Phenotype} row under a {@link org.zfin.zirc.entity.Mutation}.
 * Only {@code description} is required — every phenotype needs a textual
 * description. Everything else is optional and stays COMPLETE whether empty
 * or filled. {@code stage} is server-derived from {@code hpfStart} on save,
 * so it's treated like any other optional display field.
 */
public final class PhenotypeStatusComputer {

    public enum Field {
        DESCRIPTION              ("description"),
        HPF_START                ("hpfStart"),
        HPF_END                  ("hpfEnd"),
        STAGE                    ("stage"),
        ZFIN_IMAGE_PERMISSION    ("zfinImagePermission"),
        ZIRC_IMAGE_PERMISSION    ("zircImagePermission"),
        NON_MENDELIAN_PERCENTAGE ("nonMendelianPercentage"),
        NON_MENDELIAN_COMMENT    ("nonMendelianComment"),
        SEGREGATION              ("segregation"),
        TYPE                     ("type");

        private final String path;

        Field(String path) { this.path = path; }

        public String getPath() { return path; }
    }

    private static final Set<String> REQUIRED_PATHS = collectRequiredPaths(ZircPhenotypeFormSchema.schema());

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

    private static FieldStatus statusFor(Phenotype p, String path) {
        Object value = readProperty(p, path);
        if (isEmpty(value) && REQUIRED_PATHS.contains(path)) return FieldStatus.MISSING;
        return FieldStatus.COMPLETE;
    }

    private PhenotypeStatusComputer() {}

    private static final Map<String, List<String>> SECTIONS =
            SchemaSections.groupsToFields(ZircPhenotypeFormSchema.uiSchema());

    public static FieldStatusResult compute(Phenotype p) {
        Map<String, FieldStatus> byField = new LinkedHashMap<>();
        for (Field f : Field.values()) {
            byField.put(f.getPath(), statusFor(p, f.getPath()));
        }

        Map<String, FieldStatus> bySection = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> e : SECTIONS.entrySet()) {
            FieldStatus worst = FieldStatus.COMPLETE;
            for (String f : e.getValue()) {
                FieldStatus st = byField.get(f);
                if (st != null) worst = worst.worse(st);
            }
            bySection.put(e.getKey(), worst);
        }

        FieldStatus overall = FieldStatus.COMPLETE;
        for (FieldStatus st : bySection.values()) overall = overall.worse(st);

        return new FieldStatusResult(byField, bySection, overall);
    }

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
        return false;
    }
}
