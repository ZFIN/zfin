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
import org.zfin.zirc.entity.Lesion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Single source of truth for the per-lesion edit form (M7.1). Parallels
 * {@link ZircAssayFormSchema} — the {@code lesionType} dropdown drives
 * conditional reveal of field clusters via JSON Forms {@code rule}
 * blocks with {@code schema.enum} matchers.
 *
 * <p>Lesion-type matrix (which clusters appear for which type):
 * <ul>
 *   <li><b>General</b> — lesionType, additionalInfo (always visible)</li>
 *   <li><b>Sizing</b> — lesionSizeBp (point/deletion/indel), insertionSizeBp (insertion/indel)</li>
 *   <li><b>Nucleotide change</b> — point_mutation</li>
 *   <li><b>Deleted sequence</b> — deletion / indel</li>
 *   <li><b>Inserted sequence</b> — insertion / indel</li>
 *   <li><b>Transgene</b> — transgene (hasLargeVariant flag here too)</li>
 *   <li><b>Location</b> — point/deletion/insertion/indel/transgene (i.e. everything
 *     except other/unknown). 5'/3' flanking sequences live here.</li>
 *   <li><b>Protein-level</b> — point_mutation</li>
 * </ul>
 *
 * <p>Lesion-type list is a starter mirroring the alt-branch (ZFIN-10265)
 * convention; curators should review.
 */
public final class ZircLesionFormSchema {

    private ZircLesionFormSchema() {}

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Read + write for one form-schema path on a Lesion. */
    public record FieldDescriptor(
            Function<Lesion, JsonNode> read,
            BiConsumer<Lesion, JsonNode> write) {
    }

    private static final List<String> LESION_TYPES = List.of(
            "point_mutation", "deletion", "insertion", "indel",
            "transgene", "other", "unknown");

    // Per-cluster visibility lists. Adding a new type or moving a field
    // between types is a single-line edit here, no JSX changes — same
    // win the alt branch's typeMatrices.ts captured, but here it's just
    // data on the server.
    private static final List<String> LESION_SIZE_TYPES =
            List.of("point_mutation", "deletion", "indel");
    private static final List<String> INSERTION_SIZE_TYPES =
            List.of("insertion", "indel");
    private static final List<String> NUCLEOTIDE_CHANGE_TYPES =
            List.of("point_mutation");
    private static final List<String> DELETED_SEQ_TYPES =
            List.of("deletion", "indel");
    private static final List<String> INSERTED_SEQ_TYPES =
            List.of("insertion", "indel");
    private static final List<String> TRANSGENE_TYPES =
            List.of("transgene");
    private static final List<String> LOCATION_TYPES =
            List.of("point_mutation", "deletion", "insertion", "indel", "transgene");
    private static final List<String> PROTEIN_TYPES =
            List.of("point_mutation");

    public static JsonSchema schema() {
        Map<String, JsonSchema> properties = new LinkedHashMap<>();
        // General
        properties.put("lesionType",            StringSchema.of("Lesion Type", 255));
        properties.put("additionalInfo",        StringSchema.of("Additional Info", 5000));
        // Sizing
        properties.put("lesionSizeBp",          new NumberSchema("Lesion size (bp)", Boolean.TRUE));
        properties.put("insertionSizeBp",       new NumberSchema("Insertion size (bp)", Boolean.TRUE));
        // Sequence specifics
        properties.put("nucleotideChange",      StringSchema.of("Nucleotide change", 2000));
        properties.put("deletedSequence",       StringSchema.of("Deleted sequence", 5000));
        properties.put("insertedSequence",      StringSchema.of("Inserted sequence", 5000));
        properties.put("transgeneSequence",     StringSchema.of("Transgene sequence", 5000));
        // Location
        properties.put("locationInline",        StringSchema.of("Location (inline)", 5000));
        properties.put("fivePrimeFlank",        StringSchema.of("5′ flank", 5000));
        properties.put("threePrimeFlank",       StringSchema.of("3′ flank", 5000));
        properties.put("hasLargeVariant",       BooleanSchema.nullable("Has large variant"));
        // Protein-level
        properties.put("mutatedAminoAcids",     StringSchema.of("Mutated amino acids", 2000));
        properties.put("mutatedAminoAcidsHgvs", StringSchema.of("Mutated amino acids (HGVS)", 2000));
        return ObjectSchema.of(properties);
    }

    public static UiSchemaElement uiSchema() {
        return new VerticalLayout(List.of(
                Group.of("General", List.of(
                        new Control("#/properties/lesionType",
                                Options.of().widget("selectWithOther").standardValues(LESION_TYPES),
                                null),
                        new Control("#/properties/additionalInfo",
                                Options.of().multi(true), null)
                )),
                groupRevealedFor("Sizing", LESION_SIZE_TYPES, List.of(
                        new Control("#/properties/lesionSizeBp",
                                Options.of().suffix("bp"), null)
                )),
                groupRevealedFor("Insertion Sizing", INSERTION_SIZE_TYPES, List.of(
                        new Control("#/properties/insertionSizeBp",
                                Options.of().suffix("bp"), null)
                )),
                groupRevealedFor("Nucleotide Change", NUCLEOTIDE_CHANGE_TYPES, List.of(
                        new Control("#/properties/nucleotideChange",
                                Options.of()
                                        .placeholder("WT → mutant, e.g. A → T")
                                        .multi(true),
                                null)
                )),
                groupRevealedFor("Deleted Sequence", DELETED_SEQ_TYPES, List.of(
                        new Control("#/properties/deletedSequence",
                                Options.of().multi(true), null)
                )),
                groupRevealedFor("Inserted Sequence", INSERTED_SEQ_TYPES, List.of(
                        new Control("#/properties/insertedSequence",
                                Options.of().multi(true), null)
                )),
                groupRevealedFor("Transgene", TRANSGENE_TYPES, List.of(
                        new Control("#/properties/transgeneSequence",
                                Options.of().multi(true), null),
                        new Control("#/properties/hasLargeVariant",
                                Options.of().widget("yesNoRadio"), null)
                )),
                groupRevealedFor("Location", LOCATION_TYPES, List.of(
                        new Control("#/properties/locationInline",
                                Options.of()
                                        .helpText("Annotated inline; list at least 5 nt before and after.")
                                        .multi(true),
                                null),
                        new Control("#/properties/fivePrimeFlank",
                                Options.of()
                                        .helpText("At least 20 nt directly preceding the lesion / transgene.")
                                        .multi(true)
                                        .infoHref("https://wiki.zfin.org/display/general/Transgene+Insertion+Sequence+Conventions"),
                                null),
                        new Control("#/properties/threePrimeFlank",
                                Options.of()
                                        .helpText("At least 20 nt directly following the lesion / transgene.")
                                        .multi(true)
                                        .infoHref("https://wiki.zfin.org/display/general/Transgene+Insertion+Sequence+Conventions"),
                                null)
                )),
                groupRevealedFor("Protein-level", PROTEIN_TYPES, List.of(
                        new Control("#/properties/mutatedAminoAcids",
                                Options.of().placeholder("e.g. p.Gly12Val"), null),
                        new Control("#/properties/mutatedAminoAcidsHgvs",
                                Options.of().placeholder("HGVS protein notation"), null)
                ))
        ));
    }

    /**
     * Helper for the lesion-type matrix's repeated "SHOW when lesionType
     * in [...]" pattern. Mirrors the assay-form's groupRevealedFor.
     */
    private static Group groupRevealedFor(
            String label, List<String> lesionTypes, List<UiSchemaElement> elements) {
        return new Group(label, elements, null,
                Rule.showWhenIn("#/properties/lesionType", lesionTypes));
    }

    public static final Map<String, FieldDescriptor> FIELDS = Map.ofEntries(
            field("/lesionType",            Lesion::getLesionType,             (l, v) -> l.setLesionType(text(v))),
            field("/additionalInfo",        Lesion::getAdditionalInfo,         (l, v) -> l.setAdditionalInfo(text(v))),
            field("/lesionSizeBp",          Lesion::getLesionSizeBp,           (l, v) -> l.setLesionSizeBp(intNullable(v))),
            field("/insertionSizeBp",       Lesion::getInsertionSizeBp,        (l, v) -> l.setInsertionSizeBp(intNullable(v))),
            field("/nucleotideChange",      Lesion::getNucleotideChange,       (l, v) -> l.setNucleotideChange(text(v))),
            field("/deletedSequence",       Lesion::getDeletedSequence,        (l, v) -> l.setDeletedSequence(text(v))),
            field("/insertedSequence",      Lesion::getInsertedSequence,       (l, v) -> l.setInsertedSequence(text(v))),
            field("/transgeneSequence",     Lesion::getTransgeneSequence,      (l, v) -> l.setTransgeneSequence(text(v))),
            field("/locationInline",        Lesion::getLocationInline,         (l, v) -> l.setLocationInline(text(v))),
            field("/fivePrimeFlank",        Lesion::getFivePrimeFlank,         (l, v) -> l.setFivePrimeFlank(text(v))),
            field("/threePrimeFlank",       Lesion::getThreePrimeFlank,        (l, v) -> l.setThreePrimeFlank(text(v))),
            field("/hasLargeVariant",       Lesion::getHasLargeVariant,        (l, v) -> l.setHasLargeVariant(boolNullable(v))),
            field("/mutatedAminoAcids",     Lesion::getMutatedAminoAcids,      (l, v) -> l.setMutatedAminoAcids(text(v))),
            field("/mutatedAminoAcidsHgvs", Lesion::getMutatedAminoAcidsHgvs,  (l, v) -> l.setMutatedAminoAcidsHgvs(text(v)))
    );

    private static Map.Entry<String, FieldDescriptor> field(
            String path,
            Function<Lesion, ?> getter,
            BiConsumer<Lesion, JsonNode> setter) {
        return Map.entry(path, new FieldDescriptor(
                l -> MAPPER.valueToTree(getter.apply(l)),
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
}
