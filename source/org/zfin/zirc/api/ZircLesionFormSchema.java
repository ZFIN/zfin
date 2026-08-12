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

    // Stable enum tokens stored on the wire (and in the lesion row). Display
    // labels for the dropdown live in LESION_TYPE_LABELS, parallel by index.
    // "other" is intentionally absent: the lesionType select widget already
    // offers a free-text "Other" option, so an explicit token would double it.
    private static final List<String> LESION_TYPES = List.of(
            "point_mutation", "deletion", "insertion", "indel",
            "transgene", "unknown");
    private static final List<String> LESION_TYPE_LABELS = List.of(
            "Point Mutation", "Deletion", "Insertion", "Indel",
            "Transgene", "Unknown");

    // Per-cluster visibility lists. Adding a new type or moving a field
    // between types is a single-line edit here, no JSX changes — same
    // win the alt branch's typeMatrices.ts captured, but here it's just
    // data on the server.
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

    // The twelve possible single-nucleotide substitutions, offered as a
    // closed dropdown for point mutations (was free text). Token == label.
    private static final List<String> NUCLEOTIDE_CHANGES = List.of(
            "A>T", "A>C", "A>G",
            "T>A", "T>C", "T>G",
            "C>A", "C>T", "C>G",
            "G>A", "G>T", "G>C");

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
        properties.put("fivePrimeFlank",        StringSchema.of("5′ flanking sequence", 5000));
        properties.put("threePrimeFlank",       StringSchema.of("3′ flanking sequence", 5000));
        properties.put("hasLargeVariant",       BooleanSchema.nullable("Has large variant"));
        // Protein-level
        properties.put("mutatedAminoAcids",     StringSchema.of("Mutated amino acids", 2000));
        properties.put("mutatedAminoAcidsHgvs", StringSchema.of("Mutated amino acids (HGVS)", 2000));
        return ObjectSchema.of(null, properties, List.of("lesionType"));
    }

    public static UiSchemaElement uiSchema() {
        // All groups are headless — the inline lesion editor card already
        // carries the "Lesion #N — <type>" header, and inner sub-section
        // headings (General/Sizing/Nucleotide Change/Location/…) just
        // cluttered the card. Group structure is retained so the per-type
        // visibility rules still work.
        return new VerticalLayout(List.of(
                Group.of(null, List.of(
                        new Control("#/properties/lesionType",
                                Options.of().widget("selectWithOther")
                                        .standardValues(LESION_TYPES)
                                        .standardLabels(LESION_TYPE_LABELS)
                                        .refreshesParent(true),
                                null)
                )),
                // Sizes are always derived (never user-entered) and rendered
                // read-only via the autoSize widget, placed directly under the
                // sequence they measure. Point mutation: nucleotide change,
                // then the fixed 1 bp size.
                groupRevealedFor(NUCLEOTIDE_CHANGE_TYPES, List.of(
                        new Control("#/properties/nucleotideChange",
                                Options.of()
                                        .widget("selectWithOther")
                                        .standardValues(NUCLEOTIDE_CHANGES)
                                        .noOther(true),
                                null)
                )),
                groupRevealedFor(NUCLEOTIDE_CHANGE_TYPES, List.of(
                        new Control("#/properties/lesionSizeBp",
                                Options.of().widget("autoSize").constantValue(1).suffix("bp"),
                                null)
                )),
                // Deletion / indel: deleted sequence, then its length as the
                // (auto, read-only) lesion size.
                groupRevealedFor(DELETED_SEQ_TYPES, List.of(
                        new Control("#/properties/deletedSequence",
                                Options.of().multi(true), null)
                )),
                groupRevealedFor(DELETED_SEQ_TYPES, List.of(
                        new Control("#/properties/lesionSizeBp",
                                Options.of().widget("autoSize")
                                        .sourceField("deletedSequence").suffix("bp"),
                                null)
                )),
                // Insertion / indel: inserted sequence, then its length as the
                // (auto, read-only) insertion size.
                groupRevealedFor(INSERTED_SEQ_TYPES, List.of(
                        new Control("#/properties/insertedSequence",
                                Options.of().multi(true), null)
                )),
                groupRevealedFor(INSERTED_SEQ_TYPES, List.of(
                        new Control("#/properties/insertionSizeBp",
                                Options.of().widget("autoSize")
                                        .sourceField("insertedSequence").suffix("bp"),
                                null)
                )),
                groupRevealedFor(TRANSGENE_TYPES, List.of(
                        new Control("#/properties/transgeneSequence",
                                Options.of().multi(true), null),
                        new Control("#/properties/hasLargeVariant",
                                Options.of().widget("yesNoRadio"), null)
                )),
                groupRevealedFor(LOCATION_TYPES, List.of(
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
                groupRevealedFor(PROTEIN_TYPES, List.of(
                        new Control("#/properties/mutatedAminoAcids",
                                Options.of().placeholder("e.g. p.Gly12Val"), null),
                        new Control("#/properties/mutatedAminoAcidsHgvs",
                                Options.of().placeholder("HGVS protein notation"), null)
                )),
                // Always-visible, and kept last so it sits below every
                // per-type field cluster regardless of the lesion type.
                Group.of(null, List.of(
                        new Control("#/properties/additionalInfo",
                                Options.of().multi(true), null)
                ))
        ));
    }

    /**
     * Helper for the lesion-type matrix's repeated "SHOW when lesionType
     * in [...]" pattern. Mirrors the assay-form's groupRevealedFor. Groups
     * are headless so children render as bare rows under the editor card.
     */
    private static Group groupRevealedFor(
            List<String> lesionTypes, List<UiSchemaElement> elements) {
        return new Group(null, elements, null,
                Rule.showWhenIn("#/properties/lesionType", lesionTypes));
    }

    public static final Map<String, FieldDescriptor> FIELDS = Map.ofEntries(
            // lesionSizeBp / insertionSizeBp are intentionally NOT here: they
            // are server-computed (see ZircSubmissionService#recalcLesionSizes)
            // and rendered read-only, never patched by the client.
            field("/lesionType",            Lesion::getLesionType,             (l, v) -> l.setLesionType(text(v))),
            field("/additionalInfo",        Lesion::getAdditionalInfo,         (l, v) -> l.setAdditionalInfo(text(v))),
            field("/nucleotideChange",      Lesion::getNucleotideChange,       (l, v) -> l.setNucleotideChange(text(v))),
            field("/deletedSequence",       Lesion::getDeletedSequence,        (l, v) -> l.setDeletedSequence(text(v))),
            field("/insertedSequence",      Lesion::getInsertedSequence,       (l, v) -> l.setInsertedSequence(text(v))),
            field("/transgeneSequence",     Lesion::getTransgeneSequence,      (l, v) -> l.setTransgeneSequence(text(v))),
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
}
