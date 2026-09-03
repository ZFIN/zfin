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
    // ZFIN-10400. One checklist for both types that can carry an insertion.
    // The separate mutagenesis / construct questions are gone: unifying them
    // into one picker means one option list, shared by insertion and indel.
    private static final List<String> INSERTION_ORIGIN_TYPES =
            List.of("insertion", "indel");

    // Stable tokens, not mdcv term ids: this list has no Sequence Ontology
    // counterpart and is specific to the submission form.
    //
    // ZFIN-10403b narrows it back to the two mutagenesis mechanisms the
    // mockup asked for. "Construct or other species DNA", "Other" and
    // "Unknown" are not wanted: the first two only ever collected a name that
    // nothing downstream reads, and "unknown" duplicates leaving the list
    // blank closely enough that curators do not need both. Anything outside
    // CRISPR/TALEN belongs in Additional Info until a ticket asks for it as a
    // field.
    private static final List<String> INSERTION_ORIGINS = List.of(
            "crispr", "talen");
    private static final List<String> INSERTION_ORIGIN_LABELS = List.of(
            "CRISPR", "TALEN");

    /**
     * Bases accepted in sequence fields. Declared here and emitted onto every
     * nucleotideSequence Control, so the widget and the server-side
     * normalization are driven by one value rather than two defaults that can
     * drift apart.
     *
     * <p>Per-field variation is a matter of passing a different constant to
     * both {@code withAlphabet} and {@code nucleotides} at that field's call
     * sites — they are deliberately adjacent for that reason.
     *
     * <p>N was accepted until curator feedback asked for strict bases only.
     * Existing stored sequences containing N are left as they are; a field is
     * only re-normalized when someone edits it.
     */
    private static final String NUCLEOTIDE_ALPHABET = "ACGT";

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
        properties.put("insertionOrigins", new ArraySchema(
                "The insertion is a consequence of",
                new StringSchema(null, null, null, null, null),
                null, null));
        properties.put("crisprSequence",         StringSchema.of("CRISPR sequence", 5000));
        // A TALEN is a pair — both arms are asked for, always. See
        // Lesion#talenSequence1.
        properties.put("talenSequence1",         StringSchema.of("TALEN sequence 1", 5000));
        properties.put("talenSequence2",         StringSchema.of("TALEN sequence 2", 5000));
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
                // The one surviving autoSize row. A point mutation is 1 bp by
                // definition and has no sequence box to carry the number, so
                // this is the only place it appears — not a duplicate of
                // anything.
                groupRevealedFor(NUCLEOTIDE_CHANGE_TYPES, List.of(
                        new Control("#/properties/lesionSizeBp",
                                Options.of().withWidget("autoSize").withConstantValue(1).withSuffix("bp"),
                                null)
                )),
                // ZFIN-10400 — where the insertion came from. Placed here so it
                // falls after the lesion-type picker and before the inserted
                // sequence box, as the ticket specifies.
                //
                // One "check all that apply" list, each box revealing only its
                // own follow-up. Every follow-up is gated on BOTH the lesion
                // type and the box, because the tokens survive a later change
                // of lesion type — without the type leg a CRISPR box would
                // reappear under a deletion. See Rule#showWhenAll.
                groupRevealedFor(INSERTION_ORIGIN_TYPES, List.of(
                        new Control("#/properties/insertionOrigins",
                                Options.of()
                                        .withWidget("checkboxGroup")
                                        .withStandardValues(INSERTION_ORIGINS)
                                        .withStandardLabels(INSERTION_ORIGIN_LABELS)
                                        .withHelpText("Check all that apply."),
                                null)
                )),
                originFollowUp("crispr", List.of(
                        new Control("#/properties/crisprSequence",
                                Options.of()
                                        .withWidget("nucleotideSequence")
                                        .withAlphabet(NUCLEOTIDE_ALPHABET)
                                        .withMulti(true),
                                null))),
                // Both arms are revealed by the one TALEN box: a TALEN
                // always cuts as a pair, so a single sequence field would be
                // asking for half an answer. Two Controls in one follow-up
                // group rather than a repeatable list — the count is fixed
                // at two and each half is labelled.
                originFollowUp("talen", List.of(
                        new Control("#/properties/talenSequence1",
                                Options.of()
                                        .withWidget("nucleotideSequence")
                                        .withAlphabet(NUCLEOTIDE_ALPHABET)
                                        .withMulti(true),
                                null),
                        new Control("#/properties/talenSequence2",
                                Options.of()
                                        .withWidget("nucleotideSequence")
                                        .withAlphabet(NUCLEOTIDE_ALPHABET)
                                        .withMulti(true),
                                null))),
                // Deletion / indel: the deleted sequence, with its length
                // named inline. There is no separate read-only size row —
                // the box already counts what it holds, and a second field
                // repeating that number was pure redundancy.
                //
                // Split by lesion type only so the count can name itself: on
                // an indel it measures the deleted part and sits opposite an
                // insertion size, where the generic "Lesion size" would be
                // ambiguous. Same column, same server-side derivation.
                groupRevealedFor(List.of("deletion"), List.of(
                        new Control("#/properties/deletedSequence",
                                Options.of().withMulti(true)
                                        .withWidget("nucleotideSequence")
                                        .withAlphabet(NUCLEOTIDE_ALPHABET)
                                        .withSizeLabel("Lesion size"), null)
                )),
                groupRevealedFor(List.of("indel"), List.of(
                        new Control("#/properties/deletedSequence",
                                Options.of().withMulti(true)
                                        .withWidget("nucleotideSequence")
                                        .withAlphabet(NUCLEOTIDE_ALPHABET)
                                        .withSizeLabel("Deletion size"), null)
                )),
                // Insertion / indel: the inserted sequence, likewise carrying
                // its own size. One label for both types — "Insertion size" is
                // unambiguous either way.
                groupRevealedFor(INSERTED_SEQ_TYPES, List.of(
                        new Control("#/properties/insertedSequence",
                                Options.of().withMulti(true)
                                        .withWidget("nucleotideSequence")
                                        .withAlphabet(NUCLEOTIDE_ALPHABET)
                                        .withSizeLabel("Insertion size"), null)
                )),
                groupRevealedFor(TRANSGENE_TYPES, List.of(
                        new Control("#/properties/transgeneSequence",
                                Options.of().withMulti(true)
                                        .withWidget("nucleotideSequence")
                                        .withAlphabet(NUCLEOTIDE_ALPHABET), null),
                        new Control("#/properties/hasLargeVariant",
                                Options.of().withWidget("yesNoRadio"), null)
                )),
                groupRevealedFor(LOCATION_TYPES, List.of(
                        new Control("#/properties/fivePrimeFlank",
                                Options.of()
                                        .withWidget("nucleotideSequence")
                                        .withAlphabet(NUCLEOTIDE_ALPHABET)
                                        .withHelpText("At least 20 nt directly preceding the lesion / transgene.")
                                        .withMulti(true)
                                        .withInfoHref("https://wiki.zfin.org/display/general/Transgene+Insertion+Sequence+Conventions"),
                                null),
                        new Control("#/properties/threePrimeFlank",
                                Options.of()
                                        .withWidget("nucleotideSequence")
                                        .withAlphabet(NUCLEOTIDE_ALPHABET)
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
    /**
     * A follow-up cluster for one ticked origin: visible only when the lesion
     * type can carry an insertion AND that token is in the list.
     */
    private static Group originFollowUp(String token, List<UiSchemaElement> elements) {
        return new Group(null, elements, null,
                Rule.showWhenAll(
                        Rule.in("#/properties/lesionType", INSERTION_ORIGIN_TYPES),
                        Rule.arrayContains("#/properties/insertionOrigins", token)));
    }

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
            field("/insertionOrigins",
                    l -> l.getInsertionOrigins() == null
                            ? new String[0] : l.getInsertionOrigins(),
                    (l, v) -> l.setInsertionOrigins(stringArray(v))),
            field("/crisprSequence",        Lesion::getCrisprSequence,         (l, v) -> l.setCrisprSequence(nucleotides(v))),
            field("/talenSequence1",        Lesion::getTalenSequence1,         (l, v) -> l.setTalenSequence1(nucleotides(v))),
            field("/talenSequence2",        Lesion::getTalenSequence2,         (l, v) -> l.setTalenSequence2(nucleotides(v))),
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
        return nucleotides(v, NUCLEOTIDE_ALPHABET);
    }

    private static String nucleotides(JsonNode v, String alphabet) {
        String s = text(v);
        if (s == null) {return null;}
        StringBuilder out = new StringBuilder(s.length());
        for (char c : s.toUpperCase().toCharArray()) {
            if (alphabet.indexOf(c) >= 0) {out.append(c);}
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
