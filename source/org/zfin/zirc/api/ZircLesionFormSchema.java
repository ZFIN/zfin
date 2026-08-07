package org.zfin.zirc.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zfin.zirc.api.jsonschema.ArraySchema;
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

import java.util.ArrayList;
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
    // ZFIN-10380 adds deletion: a deletion can remove residues, so it wants
    // the amino-acid section and the protein-consequence list too.
    private static final List<String> PROTEIN_TYPES =
            List.of("point_mutation", "deletion");
    // ZFIN-10400. Separate lists because the two questions do not travel
    // together: ZFIN-10403's mockup shows indel asking about mutagenesis but
    // not about a construct.
    private static final List<String> MUTAGENESIS_ORIGIN_TYPES =
            List.of("insertion", "indel");
    private static final List<String> CONSTRUCT_ORIGIN_TYPES =
            List.of("insertion");

    /** Bases accepted in sequence fields; matches DEFAULT_ALPHABET client-side. */
    private static final String NUCLEOTIDE_ALPHABET = "ACGTN";

    // The twelve possible single-nucleotide substitutions, offered as a
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
        // Insertion origin (ZFIN-10400)
        properties.put("insertionFromMutagenesis", BooleanSchema.nullable(
                "Is the insertion a consequence of mutagenesis (CRISPR or TALEN)?"));
        properties.put("insertionFromConstruct",   BooleanSchema.nullable(
                "Is the insertion due to insertion of construct or other species DNA?"));
        properties.put("crisprSequence",         StringSchema.of("CRISPR sequence", 5000));
        properties.put("talenSequence",          StringSchema.of("TALEN sequence", 5000));
        properties.put("constructName",          StringSchema.of("Construct name", 255));
        // Protein-level
        properties.put("mutatedAminoAcidsHgvs", StringSchema.of("Mutated amino acids (HGVS)", 2000));
        // Structured amino-acid change (ZFIN-10379). from/to are
        // amino_acid_term ZDB IDs; the end of the range is optional.
        properties.put("aaChangeFrom",          StringSchema.of("Amino Acid Change", 255));
        properties.put("aaChangeTo",            StringSchema.of("Amino acid change (to)", 255));
        properties.put("aaPositionStart",       new NumberSchema("Position", Boolean.TRUE));
        properties.put("aaPositionEnd",         new NumberSchema("Position (end)", Boolean.TRUE));
        // Protein-level consequences (ZFIN-10380): mdcv term ZDB IDs.
        properties.put("proteinConsequences", new ArraySchema(
                "Protein consequences",
                new StringSchema(null, null, null, null, null),
                null, null));
        // Transcript-level (ZFIN-10399): mdcv term ZDB IDs, any lesion type.
        properties.put("transcriptConsequences", new ArraySchema(
                "Transcript consequences",
                new StringSchema(null, null, null, null, null),
                null, null));
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
                                Options.of().withWidget("selectWithOther")
                                        .withStandardValues(LESION_TYPES)
                                        .withStandardLabels(LESION_TYPE_LABELS)
                                        .withRefreshesParent(true),
                                null)
                )),
                // Sizes are always derived (never user-entered) and rendered
                // read-only via the autoSize widget, placed directly under the
                // sequence they measure. Point mutation: nucleotide change,
                // then the fixed 1 bp size.
                groupRevealedFor(NUCLEOTIDE_CHANGE_TYPES, List.of(
                        new Control("#/properties/nucleotideChange",
                                Options.of()
                                        .withWidget("selectWithOther")
                                        .withStandardValues(NUCLEOTIDE_CHANGES)
                                        .withNoOther(true),
                                null)
                )),
                groupRevealedFor(NUCLEOTIDE_CHANGE_TYPES, List.of(
                        new Control("#/properties/lesionSizeBp",
                                Options.of().withWidget("autoSize").withConstantValue(1).withSuffix("bp"),
                                null)
                )),
                // ZFIN-10400 — where the insertion came from. Placed here so it
                // falls after the lesion-type picker and before the inserted
                // sequence box, as the ticket specifies.
                //
                // The follow-up fields are gated on BOTH the lesion type and
                // the answer. A rule on the boolean alone would leak: the flag
                // survives a later change of lesion type, so a CRISPR box
                // would reappear under, say, deletion. See Rule#showWhenAll
                // for why this is an AND condition rather than nested Groups.
                groupRevealedFor(MUTAGENESIS_ORIGIN_TYPES, List.of(
                        new Control("#/properties/insertionFromMutagenesis",
                                Options.of().withWidget("yesNoRadio"), null)
                )),
                new Group(null, List.of(
                        new Control("#/properties/crisprSequence",
                                Options.of()
                                        .withWidget("nucleotideSequence")
                                        .withMulti(true),
                                null),
                        new Control("#/properties/talenSequence",
                                Options.of()
                                        .withWidget("nucleotideSequence")
                                        .withMulti(true),
                                null)
                        ), null,
                        Rule.showWhenAll(
                                Rule.in("#/properties/lesionType", MUTAGENESIS_ORIGIN_TYPES),
                                Rule.isTrue("#/properties/insertionFromMutagenesis"))),
                groupRevealedFor(CONSTRUCT_ORIGIN_TYPES, List.of(
                        new Control("#/properties/insertionFromConstruct",
                                Options.of().withWidget("yesNoRadio"), null)
                )),
                new Group(null, List.of(
                        new Control("#/properties/constructName",
                                Options.of().withPlaceholder("e.g. Tg(fli1a:EGFP)"), null)
                        ), null,
                        Rule.showWhenAll(
                                Rule.in("#/properties/lesionType", CONSTRUCT_ORIGIN_TYPES),
                                Rule.isTrue("#/properties/insertionFromConstruct"))),
                // Deletion / indel: deleted sequence, then its length as the
                // (auto, read-only) lesion size.
                groupRevealedFor(DELETED_SEQ_TYPES, List.of(
                        new Control("#/properties/deletedSequence",
                                Options.of().withMulti(true)
                                        .withWidget("nucleotideSequence"), null)
                )),
                groupRevealedFor(List.of("deletion"), List.of(
                        new Control("#/properties/lesionSizeBp",
                                Options.of().withWidget("autoSize")
                                        .withSourceField("deletedSequence").withSuffix("bp"),
                                null)
                )),
                // ZFIN-10403: on an indel this same column measures only the
                // deleted part and sits opposite an insertion size, so the
                // generic "Lesion size" name is ambiguous. Same property and
                // same server-side derivation — a label override, not a new
                // field.
                groupRevealedFor(List.of("indel"), List.of(
                        new Control("#/properties/lesionSizeBp",
                                Options.of().withWidget("autoSize")
                                        .withSourceField("deletedSequence").withSuffix("bp")
                                        .withLabel("Deletion size (bp)"),
                                null)
                )),
                // Insertion / indel: inserted sequence, then its length as the
                // (auto, read-only) insertion size.
                groupRevealedFor(INSERTED_SEQ_TYPES, List.of(
                        new Control("#/properties/insertedSequence",
                                Options.of().withMulti(true)
                                        .withWidget("nucleotideSequence"), null)
                )),
                groupRevealedFor(INSERTED_SEQ_TYPES, List.of(
                        new Control("#/properties/insertionSizeBp",
                                Options.of().withWidget("autoSize")
                                        .withSourceField("insertedSequence").withSuffix("bp"),
                                null)
                )),
                groupRevealedFor(TRANSGENE_TYPES, List.of(
                        new Control("#/properties/transgeneSequence",
                                Options.of().withMulti(true)
                                        .withWidget("nucleotideSequence"), null),
                        new Control("#/properties/hasLargeVariant",
                                Options.of().withWidget("yesNoRadio"), null)
                )),
                groupRevealedFor(LOCATION_TYPES, List.of(
                        new Control("#/properties/fivePrimeFlank",
                                Options.of()
                                        .withWidget("nucleotideSequence")
                                        .withHelpText("At least 20 nt directly preceding the lesion / transgene.")
                                        .withMulti(true)
                                        .withInfoHref("https://wiki.zfin.org/display/general/Transgene+Insertion+Sequence+Conventions"),
                                null),
                        new Control("#/properties/threePrimeFlank",
                                Options.of()
                                        .withWidget("nucleotideSequence")
                                        .withHelpText("At least 20 nt directly following the lesion / transgene.")
                                        .withMulti(true)
                                        .withInfoHref("https://wiki.zfin.org/display/general/Transgene+Insertion+Sequence+Conventions"),
                                null)
                )),
                // ZFIN-10379 — the curation interface's from > to + position
                // control, replacing the free-text box that asked curators to
                // hand-write HGVS. The old mutatedAminoAcids column is left in
                // place but no longer surfaced; see the migration for why.
                groupRevealedFor(PROTEIN_TYPES, List.of(
                        new Control("#/properties/aaChangeFrom",
                                Options.of()
                                        .withWidget("aminoAcidChange")
                                        .withVocabulary("amino_acid_term")
                                        .withToField("aaChangeTo")
                                        .withPositionField("aaPositionStart")
                                        .withPositionEndField("aaPositionEnd"),
                                null),
                        new Control("#/properties/mutatedAminoAcidsHgvs",
                                Options.of().withPlaceholder("HGVS protein notation"), null),
                        // ZFIN-10380. Sits inside the protein cluster rather
                        // than in its own group because it belongs with the
                        // amino-acid change, the way the curation interface
                        // pairs them.
                        new Control("#/properties/proteinConsequences",
                                Options.of()
                                        .withWidget("vocabularyMultiSelect")
                                        .withVocabulary("protein_consequence_term")
                                        .withAddLabel("+ Add consequence")
                                        .withHelpText("Add one entry per consequence."),
                                null)
                )),
                // Transcript consequences (ZFIN-10399) apply to every lesion
                // type, so this group carries no reveal rule. It is placed
                // after the location group, which is where the 3' flanking
                // sequence lives — the ticket asks for the pick list to follow
                // that box. Unlike the curation interface's version there are
                // no exon / intron inputs.
                Group.of(null, List.of(
                        new Control("#/properties/transcriptConsequences",
                                Options.of()
                                        .withWidget("vocabularyMultiSelect")
                                        .withVocabulary("transcript_consequence_term")
                                        .withAddLabel("+ Add consequence")
                                        .withHelpText("Add one entry per consequence."),
                                null)
                )),
                // Always-visible, and kept last so it sits below every
                // per-type field cluster regardless of the lesion type.
                Group.of(null, List.of(
                        new Control("#/properties/additionalInfo",
                                Options.of().withMulti(true), null)
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
            field("/deletedSequence",       Lesion::getDeletedSequence,        (l, v) -> l.setDeletedSequence(nucleotides(v))),
            field("/insertedSequence",      Lesion::getInsertedSequence,       (l, v) -> l.setInsertedSequence(nucleotides(v))),
            field("/transgeneSequence",     Lesion::getTransgeneSequence,      (l, v) -> l.setTransgeneSequence(nucleotides(v))),
            field("/fivePrimeFlank",        Lesion::getFivePrimeFlank,         (l, v) -> l.setFivePrimeFlank(nucleotides(v))),
            field("/threePrimeFlank",       Lesion::getThreePrimeFlank,        (l, v) -> l.setThreePrimeFlank(nucleotides(v))),
            field("/hasLargeVariant",       Lesion::getHasLargeVariant,        (l, v) -> l.setHasLargeVariant(boolNullable(v))),
            field("/insertionFromMutagenesis", Lesion::getInsertionFromMutagenesis, (l, v) -> l.setInsertionFromMutagenesis(boolNullable(v))),
            field("/insertionFromConstruct", Lesion::getInsertionFromConstruct, (l, v) -> l.setInsertionFromConstruct(boolNullable(v))),
            field("/crisprSequence",        Lesion::getCrisprSequence,         (l, v) -> l.setCrisprSequence(nucleotides(v))),
            field("/talenSequence",         Lesion::getTalenSequence,          (l, v) -> l.setTalenSequence(nucleotides(v))),
            field("/constructName",         Lesion::getConstructName,          (l, v) -> l.setConstructName(text(v))),
            field("/mutatedAminoAcidsHgvs", Lesion::getMutatedAminoAcidsHgvs,  (l, v) -> l.setMutatedAminoAcidsHgvs(text(v))),
            field("/aaChangeFrom",          Lesion::getAaChangeFrom,           (l, v) -> l.setAaChangeFrom(text(v))),
            field("/aaChangeTo",            Lesion::getAaChangeTo,             (l, v) -> l.setAaChangeTo(text(v))),
            field("/aaPositionStart",       Lesion::getAaPositionStart,        (l, v) -> l.setAaPositionStart(intNullable(v))),
            field("/aaPositionEnd",         Lesion::getAaPositionEnd,          (l, v) -> l.setAaPositionEnd(intNullable(v))),
            field("/transcriptConsequences",
                    l -> l.getTranscriptConsequences() == null
                            ? new String[0] : l.getTranscriptConsequences(),
                    (l, v) -> l.setTranscriptConsequences(stringArray(v))),
            field("/proteinConsequences",
                    l -> l.getProteinConsequences() == null
                            ? new String[0] : l.getProteinConsequences(),
                    (l, v) -> l.setProteinConsequences(stringArray(v)))
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

    /**
     * JSON array to a trimmed String[] with blanks dropped. Same contract as
     * {@code ZircFormSchema.stringArray} — the array widgets can pass empty
     * strings through mid-edit and those should not reach the column.
     */
    private static String[] stringArray(JsonNode v) {
        if (v == null || v.isNull() || !v.isArray()) {return new String[0];}
        List<String> out = new ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            String s = v.get(i).asText();
            if (s != null && !s.isBlank()) {
                out.add(s.trim());
            }
        }
        return out.toArray(new String[0]);
    }

    /**
     * Sequence fields: uppercase and drop anything that is not a base.
     *
     * The nucleotideSequence widget applies the same rule as the curator
     * types, but a PATCH can be made directly against the field path, so the
     * constraint has to live here too or it is decorative. Kept deliberately
     * simple — it mirrors normalizeSequence in nucleotides.ts, minus the
     * FASTA-header handling, which is a paste affordance rather than part of
     * what the column is allowed to hold.
     */
    private static String nucleotides(JsonNode v) {
        String s = text(v);
        if (s == null) {return null;}
        StringBuilder out = new StringBuilder(s.length());
        for (char c : s.toUpperCase().toCharArray()) {
            if (NUCLEOTIDE_ALPHABET.indexOf(c) >= 0) {out.append(c);}
        }
        return out.isEmpty() ? null : out.toString();
    }

    private static Integer intNullable(JsonNode v) {
        if (v == null || v.isNull()) {return null;}
        // The number input clears to null, but an empty string can arrive via
        // a hand-built PATCH; treat it the same rather than storing 0.
        if (v.isTextual() && v.asText().isBlank()) {return null;}
        return v.asInt();
    }

    private static Boolean boolNullable(JsonNode v) {
        if (v == null || v.isNull()) {return null;}
        return v.asBoolean();
    }
}
