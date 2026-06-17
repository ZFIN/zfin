package org.zfin.zirc.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zfin.zirc.api.jsonschema.BooleanSchema;
import org.zfin.zirc.api.jsonschema.JsonSchema;
import org.zfin.zirc.api.jsonschema.NumberSchema;
import org.zfin.zirc.api.jsonschema.ObjectSchema;
import org.zfin.zirc.api.jsonschema.StringSchema;
import org.zfin.zirc.api.uischema.Control;
import org.zfin.zirc.api.uischema.Group;
import org.zfin.zirc.api.uischema.Options;
import org.zfin.zirc.api.uischema.Rule;
import org.zfin.zirc.api.uischema.UiSchemaElement;
import org.zfin.zirc.api.uischema.VerticalLayout;
import org.zfin.zirc.entity.Phenotype;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Single source of truth for the per-phenotype edit form (M8.1). Same
 * shape as {@link ZircLesionFormSchema} but without a type matrix —
 * the field layout is the same for every phenotype.
 *
 * <p>{@code segregation} and {@code type} are single-valued scalar
 * {@code text} columns; both render with the {@code selectWithOther}
 * dropdown (segregation closed, type with an Other free-text option).
 *
 * <p>The hpf/dpf unit toggle is purely UI state and lives in the
 * {@code phenotypeTiming} renderer — the wire format is always
 * integer hpf, mirroring the alt-branch (ZFIN-10265) decision.
 */
public final class ZircPhenotypeFormSchema {

    private ZircPhenotypeFormSchema() {}

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Read + write for one form-schema path on a Phenotype. */
    public record FieldDescriptor(
            Function<Phenotype, JsonNode> read,
            BiConsumer<Phenotype, JsonNode> write) {
    }

    /**
     * Canonical segregation patterns (ZFIN-10348). Rendered as a closed
     * selectWithOther dropdown (no Other) — one inheritance pattern per
     * phenotype. The {@link #NON_MENDELIAN} value gates the Non-Mendelian
     * %/comment reveal.
     */
    private static final List<String> SEGREGATION_OPTIONS = List.of(
            "Mendelian recessive",
            "Mendelian dominant",
            "Non-Mendelian");

    private static final String NON_MENDELIAN = "Non-Mendelian";

    /**
     * Canonical phenotype types (ZFIN-10349). Rendered as a selectWithOther
     * dropdown with an "Other" free-text entry for values outside the list.
     * One type per phenotype.
     */
    private static final List<String> PHENOTYPE_TYPE_OPTIONS = List.of(
            "Zygotic (Z)",
            "Maternal (M)",
            "Maternal-Zygotic (M-Z)");

    public static JsonSchema schema() {
        Map<String, JsonSchema> properties = new LinkedHashMap<>();
        properties.put("description",             StringSchema.of("Description", 5000));
        // Timing — wire is always hpf integer; unit toggle is UI-only.
        properties.put("hpfStart",                new NumberSchema("Start (hpf)", Boolean.TRUE));
        properties.put("hpfEnd",                  new NumberSchema("End (hpf)", Boolean.TRUE));
        properties.put("stage",                   StringSchema.of("Stage", 255));
        // Image permissions
        properties.put("zfinImagePermission",     BooleanSchema.nullable("ZFIN image permission"));
        properties.put("zircImagePermission",     BooleanSchema.nullable("ZIRC image permission"));
        // Non-Mendelian segregation
        properties.put("nonMendelianPercentage",  new NumberSchema("Non-Mendelian %", Boolean.TRUE));
        properties.put("nonMendelianComment",    StringSchema.of("Non-Mendelian comment", 5000));
        // Single-valued scalar text columns.
        properties.put("segregation",            StringSchema.of("Segregation", 255));
        properties.put("type",                   StringSchema.of("Phenotype type", 255));
        return ObjectSchema.of(null, properties, List.of("description"));
    }

    public static UiSchemaElement uiSchema() {
        // All groups are headless — the inline phenotype editor card
        // already carries the "Phenotype #N — <description-snippet>"
        // header. Group structure is retained as a layout container.
        // Non-Mendelian %/comment only render once segregation is set to
        // "Non-Mendelian" (ZFIN-10348).
        Rule showWhenNonMendelian =
                Rule.showWhenIn("#/properties/segregation", NON_MENDELIAN);

        return new VerticalLayout(List.of(
                Group.of(null, List.of(
                        // The parent phenotype card shows the description snippet —
                        // refresh it on change.
                        new Control("#/properties/description",
                                Options.of().multi(true).refreshesParent(true), null)
                )),
                // Custom timing widget — hpf/dpf unit toggle + read-only
                // stage echo.
                Group.of(null, List.of(
                        new Control("#/properties/hpfStart",
                                Options.of().widget("phenotypeTiming"), null)
                )),
                Group.of(null, List.of(
                        new Control("#/properties/zfinImagePermission",
                                Options.of().widget("yesNoRadio"), null),
                        new Control("#/properties/zircImagePermission",
                                Options.of().widget("yesNoRadio"), null)
                )),
                // Segregation sits directly below image permissions
                // (ZFIN-10348). The %/comment fields follow it and only
                // reveal when Non-Mendelian is selected.
                Group.of(null, List.of(
                        new Control("#/properties/segregation",
                                Options.of()
                                        .widget("selectWithOther")
                                        .standardValues(SEGREGATION_OPTIONS)
                                        .noOther(true),
                                null),
                        new Control("#/properties/nonMendelianPercentage",
                                Options.of().suffix("%"), showWhenNonMendelian),
                        new Control("#/properties/nonMendelianComment",
                                Options.of().multi(true), showWhenNonMendelian)
                )),
                Group.of(null, List.of(
                        new Control("#/properties/type",
                                Options.of()
                                        .widget("selectWithOther")
                                        .standardValues(PHENOTYPE_TYPE_OPTIONS),
                                null)
                ))
        ));
    }

    public static final Map<String, FieldDescriptor> FIELDS = Map.ofEntries(
            field("/description",             Phenotype::getDescription,             (p, v) -> p.setDescription(text(v))),
            field("/hpfStart",                Phenotype::getHpfStart,                (p, v) -> p.setHpfStart(intNullable(v))),
            field("/hpfEnd",                  Phenotype::getHpfEnd,                  (p, v) -> p.setHpfEnd(intNullable(v))),
            field("/stage",                   Phenotype::getStage,                   (p, v) -> p.setStage(text(v))),
            field("/zfinImagePermission",     Phenotype::getZfinImagePermission,     (p, v) -> p.setZfinImagePermission(boolNullable(v))),
            field("/zircImagePermission",     Phenotype::getZircImagePermission,     (p, v) -> p.setZircImagePermission(boolNullable(v))),
            field("/nonMendelianPercentage",  Phenotype::getNonMendelianPercentage,  (p, v) -> p.setNonMendelianPercentage(doubleNullable(v))),
            field("/nonMendelianComment",     Phenotype::getNonMendelianComment,     (p, v) -> p.setNonMendelianComment(text(v))),
            field("/segregation",             Phenotype::getSegregation,             (p, v) -> p.setSegregation(text(v))),
            field("/type",                    Phenotype::getType,                    (p, v) -> p.setType(text(v)))
    );

    private static Map.Entry<String, FieldDescriptor> field(
            String path,
            Function<Phenotype, ?> getter,
            BiConsumer<Phenotype, JsonNode> setter) {
        return Map.entry(path, new FieldDescriptor(
                p -> MAPPER.valueToTree(getter.apply(p)),
                setter));
    }

    private static String text(JsonNode v) {
        if (v == null || v.isNull()) {return null;}
        String s = v.asText();
        return s.isBlank() ? null : s.trim();
    }

    private static Boolean boolNullable(JsonNode v) {
        if (v == null || v.isNull()) {return null;}
        return v.asBoolean();
    }

    private static Integer intNullable(JsonNode v) {
        if (v == null || v.isNull()) {return null;}
        return v.isNumber() ? v.asInt() : null;
    }

    private static Double doubleNullable(JsonNode v) {
        if (v == null || v.isNull()) {return null;}
        return v.isNumber() ? v.asDouble() : null;
    }
}
