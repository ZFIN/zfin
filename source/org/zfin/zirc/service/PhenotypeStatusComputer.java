package org.zfin.zirc.service;

import org.zfin.zirc.entity.Phenotype;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-field status for one {@link Phenotype} row under a {@link org.zfin.zirc.entity.Mutation}.
 * Only {@code description} is required — every phenotype needs a textual
 * description. Everything else is optional and stays COMPLETE whether empty
 * or filled. {@code stage} is server-derived from {@code hpfStart} on save,
 * so it's treated like any other optional display field.
 */
public final class PhenotypeStatusComputer {

    public enum Field {
        DESCRIPTION              ("description",              true),
        HPF_START                ("hpfStart",                 false),
        HPF_END                  ("hpfEnd",                   false),
        STAGE                    ("stage",                    false),
        ZFIN_IMAGE_PERMISSION    ("zfinImagePermission",      false),
        ZIRC_IMAGE_PERMISSION    ("zircImagePermission",      false),
        NON_MENDELIAN_PERCENTAGE ("nonMendelianPercentage",   false),
        NON_MENDELIAN_COMMENT    ("nonMendelianComment",      false),
        SEGREGATION              ("segregation",              false),
        TYPE                     ("type",                     false);

        private final String path;
        private final boolean required;

        Field(String path, boolean required) {
            this.path = path;
            this.required = required;
        }

        public String  getPath()    { return path; }
        public boolean isRequired() { return required; }

        public FieldStatus statusFor(Phenotype p) {
            Object value = readProperty(p, path);
            boolean empty = isEmpty(value);
            if (empty && required) return FieldStatus.MISSING;
            return FieldStatus.COMPLETE;
        }
    }

    private PhenotypeStatusComputer() {}

    public static FieldStatusResult compute(Phenotype p) {
        Map<String, FieldStatus> byField = new LinkedHashMap<>();
        for (Field f : Field.values()) {
            byField.put(f.getPath(), f.statusFor(p));
        }

        FieldStatus overall = FieldStatus.COMPLETE;
        for (FieldStatus st : byField.values()) overall = overall.worse(st);

        Map<String, FieldStatus> bySection = new LinkedHashMap<>();
        bySection.put("Phenotype", overall);

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
