package org.zfin.zirc.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zfin.gwt.root.dto.Mutagen;
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
import org.zfin.zirc.entity.Mutation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Single source of truth for the per-mutation edit form. Parallel to
 * {@link ZircFormSchema} but operates on {@link Mutation} entities.
 *
 * <p>M3.3 scope: General, Mutagenesis, Lethality, and Publications sections.
 * Per-mutation children (Genes, Lesions, Genotyping Assays, Phenotypes) are
 * later milestones; the assay-type field matrix in M4 is where the deferred
 * path-resolver question finally surfaces.
 *
 * <p>Canonical enum values for mutagenesis stage and lethality
 * stage are starter lists; curators should review before production.
 */
public final class ZircMutationFormSchema {

    private ZircMutationFormSchema() {}

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Read + write for a single form-schema path on a Mutation entity. */
    public record FieldDescriptor(
            Function<Mutation, JsonNode> read,
            BiConsumer<Mutation, JsonNode> write) {
    }

    /**
     * Canonical mutation-type list (ZFIN-10347). Rendered as a
     * selectWithOther dropdown — the widget appends an "Other" option
     * that reveals a free-text input, so curators aren't limited to the
     * standard values. Stored verbatim as the mutationType string.
     */
    private static final List<String> MUTATION_TYPES = List.of(
            "Null (complete loss of function)",
            "Hypomorphic (partial loss of function)",
            "Hypermorphic (gain of function, increased activity)",
            "Neomorphic (gain of function, new function)",
            "Unknown");

    private static final List<String> MUTAGENESIS_STAGES = List.of(
            "oocyte", "sperm", "embryo", "larva", "adult", "unknown");

    /**
     * Submitter-facing mutagenesis-protocol picklist (ZFIN-10402): the whole
     * {@link Mutagen} vocabulary, in the order curators asked for, minus
     * {@link Mutagen#NOT_SPECIFIED} — a submitter always knows which protocol
     * they used, so leaving it out keeps that non-answer out of the data.
     *
     * <p>Ordered here rather than by reordering the enum, because the curation
     * interface builds its own mutagen list per feature type and must keep the
     * ordering it has. Drawing the values from the enum is what makes "the full
     * list" true, and keeps submissions on the same strings curation stores.
     * {@code FormSchemaInvariantsTest} fails if a new enum value is ever added
     * without being placed here.
     */
    static final List<Mutagen> MUTAGENESIS_PROTOCOL_ORDER = List.of(
            Mutagen.CRISPR,
            Mutagen.DNA_AND_CRISPR,
            Mutagen.TALEN,
            Mutagen.DNA_AND_TALEN,
            Mutagen.DNA,
            Mutagen.ENU,
            Mutagen.ZINC_FINGER_NUCLEASE,
            Mutagen.TMP,
            Mutagen.G_RAYS,
            Mutagen.SPONTANEOUS,
            Mutagen.EMS);

    private static final List<String> MUTAGENESIS_PROTOCOLS =
            MUTAGENESIS_PROTOCOL_ORDER.stream().map(Mutagen::toString).toList();

    private static final List<String> LETHALITY_STAGES = List.of(
            "embryonic", "larval", "juvenile", "adult", "unknown");

    public static JsonSchema schema() {
        Map<String, JsonSchema> properties = new LinkedHashMap<>();
        // General
        properties.put("alleleDesignation",          StringSchema.of("Allele Designation", 255));
        properties.put("alleleInZfin",               BooleanSchema.nullable("ZFIN Record Established"));
        properties.put("mutationType",               StringSchema.of("Mutation Type", 255));
        properties.put("mutationDiscoverer",         StringSchema.of("Discoverer", 255));
        properties.put("mutationInstitution",        StringSchema.of("Institution", 255));
        // Mutagenesis
        properties.put("mutagenesisStage",           StringSchema.of("Mutagenesis Stage", 255));
        properties.put("mutagenesisProtocol",        StringSchema.of("Mutagenesis Protocol", 255));
        properties.put("molecularlyCharacterized",   BooleanSchema.nullable("Molecularly Characterized"));
        // Lethality
        properties.put("homozygousLethal",           BooleanSchema.nullable("Homozygous Lethal"));
        properties.put("lethalityStageTypical",      StringSchema.of("Typical Lethality Stage", 255));
        properties.put("lethalitySpecificTimepoint", StringSchema.of("Specific Timepoint", 255));
        properties.put("lethalityWindowStart",       StringSchema.of("Lethality Window Start", 255));
        properties.put("lethalityWindowEnd",         StringSchema.of("Lethality Window End", 255));
        properties.put("lethalityAdditionalInfo",    StringSchema.of("Lethality Additional Info", 5000));
        // Publications
        properties.put("publications",               new ArraySchema("Publications",
                                                            new StringSchema(null, null, null, null, null),
                                                            null, null));
        // Genotyping assays — summary rows that the AssaysListRenderer
        // turns into expandable cards. Add/Delete go through dedicated
        // endpoints, so MutationEdit's diff filter must skip /assays.
        properties.put("assays",                     assaysSummaryArrayProp());
        // Genes — per-mutation gene records. Same external-managed
        // pattern as assays; MutationEdit must skip /genes in its diff.
        properties.put("genes",                      genesArrayProp());
        // Lesions — same shape as assays/genes, with a lesion-type
        // matrix that drives conditional reveal on the per-lesion form.
        properties.put("lesions",                    lesionsSummaryArrayProp());
        // Phenotypes — same inline-expand pattern; no type matrix.
        properties.put("phenotypes",                 phenotypesSummaryArrayProp());
        // Required fields per the General + Mutagenesis sections.
        return ObjectSchema.of(null, properties, List.of(
                "alleleDesignation",
                "mutagenesisStage",
                "mutagenesisProtocol",
                "mutationType"));
    }

    public static UiSchemaElement uiSchema() {
        // Conditional reveal: lethality detail fields only render when
        // homozygousLethal is exactly true.
        Rule showWhenLethal = Rule.showWhenTrue("#/properties/homozygousLethal");
        // Twin rules on the alleleDesignation pair: plain input renders when
        // the curator doesn't claim the allele already exists in ZFIN;
        // marker autocomplete renders when they do.
        Rule hideIfInZfin = Rule.hideWhenTrue("#/properties/alleleInZfin");
        Rule showIfInZfin = Rule.showWhenTrue("#/properties/alleleInZfin");

        return new VerticalLayout(List.of(
                Group.of("General", List.of(
                        // alleleInZfin gates which alleleDesignation widget
                        // renders, so it comes first in the group.
                        new Control("#/properties/alleleInZfin",
                                Options.of()
                                        .withWidget("yesNoRadio")
                                        .withHelpText("When \"Yes\", the field below searches existing ZFIN markers."),
                                null),
                        // Plain text input for new alleles.
                        new Control("#/properties/alleleDesignation",
                                Options.of()
                                        .withPlaceholder("e.g. zf123")
                                        .withHelpText("ZFIN allele designation; leave blank if not yet assigned."),
                                hideIfInZfin),
                        // Feature autocomplete when the allele is already in
                        // ZFIN — alleles live in the Feature table, not
                        // Marker (per the reference's /features/search).
                        // The selected ZDB-ID is what we PATCH back.
                        new Control("#/properties/alleleDesignation",
                                Options.of()
                                        .withWidget("autocomplete")
                                        .withSearchEndpoint("features")
                                        .withPlaceholder("Start typing an allele or ZDB-ID…")
                                        .withHelpText("Resolves to the ZFIN feature ZDB-ID."),
                                showIfInZfin),
                        new Control("#/properties/mutationType",
                                Options.of().withWidget("selectWithOther").withStandardValues(MUTATION_TYPES),
                                null),
                        new Control("#/properties/mutationDiscoverer",
                                Options.of().withPlaceholder("Person who first identified the mutation"),
                                null),
                        new Control("#/properties/mutationInstitution",
                                Options.of().withPlaceholder("Lab / institution"), null)
                )),
                Group.of("Mutagenesis", List.of(
                        new Control("#/properties/mutagenesisStage",
                                Options.of().withWidget("selectWithOther").withStandardValues(MUTAGENESIS_STAGES),
                                null),
                        new Control("#/properties/mutagenesisProtocol",
                                Options.of().withWidget("selectWithOther").withStandardValues(MUTAGENESIS_PROTOCOLS),
                                null),
                        new Control("#/properties/molecularlyCharacterized",
                                Options.of().withWidget("yesNoRadio"), null)
                )),
                // Genes: same inline-expand pattern as assays.
                new Group("Genes",
                        List.of(new Control("#/properties/genes",
                                Options.of().withWidget("genesList").withManagesOwnPersistence(true), null)),
                        Options.of().withLayout("plain"),
                        null),
                // Lesions: same inline-expand pattern; the per-lesion
                // form has the lesion-type matrix.
                new Group("Lesions",
                        List.of(new Control("#/properties/lesions",
                                Options.of().withWidget("lesionsList").withManagesOwnPersistence(true), null)),
                        Options.of().withLayout("plain"),
                        null),
                // Genotyping Assays is a list of child rows like the
                // submission's Mutations section — drop the table wrapper.
                new Group("Genotyping Assays",
                        List.of(new Control("#/properties/assays",
                                Options.of().withWidget("assaysList").withManagesOwnPersistence(true), null)),
                        Options.of().withLayout("plain"),
                        null),
                // Phenotypes: same inline-expand pattern; no type matrix.
                new Group("Phenotypes",
                        List.of(new Control("#/properties/phenotypes",
                                Options.of().withWidget("phenotypesList").withManagesOwnPersistence(true), null)),
                        Options.of().withLayout("plain"),
                        null),
                Group.of("Lethality", List.of(
                        new Control("#/properties/homozygousLethal",
                                Options.of().withWidget("yesNoRadio"), null),
                        new Control("#/properties/lethalityStageTypical",
                                Options.of().withWidget("selectWithOther").withStandardValues(LETHALITY_STAGES),
                                showWhenLethal),
                        new Control("#/properties/lethalitySpecificTimepoint",
                                Options.of()
                                        .withPlaceholder("e.g. 48 hpf")
                                        .withHelpText("Single timepoint when most homozygotes die. Use the window fields below for a range."),
                                showWhenLethal),
                        new Control("#/properties/lethalityWindowStart",
                                Options.of().withPlaceholder("e.g. 24 hpf"), showWhenLethal),
                        new Control("#/properties/lethalityWindowEnd",
                                Options.of().withPlaceholder("e.g. 72 hpf"), showWhenLethal),
                        new Control("#/properties/lethalityAdditionalInfo",
                                Options.of().withMulti(true), showWhenLethal)
                )),
                Group.of("Publications", List.of(
                        new Control("#/properties/publications",
                                Options.of()
                                        .withWidget("stringList")
                                        .withPlaceholder("Citation, PMID, DOI, or ZDB Pub ID")
                                        .withAddLabel("+ Add publication"),
                                null)
                ))
        ));
    }

    /**
     * Path → read+write dispatch for mutation fields. Same gatekeeper
     * behavior as {@link ZircFormSchema#FIELDS}: unknown paths are
     * rejected at the controller.
     */
    public static final Map<String, FieldDescriptor> FIELDS = Map.ofEntries(
            // General
            field("/alleleDesignation",
                    Mutation::getAlleleDesignation,         (m, v) -> m.setAlleleDesignation(text(v))),
            field("/alleleInZfin",
                    Mutation::getAlleleInZfin,              (m, v) -> m.setAlleleInZfin(boolNullable(v))),
            field("/mutationType",
                    Mutation::getMutationType,              (m, v) -> m.setMutationType(text(v))),
            field("/mutationDiscoverer",
                    Mutation::getMutationDiscoverer,        (m, v) -> m.setMutationDiscoverer(text(v))),
            field("/mutationInstitution",
                    Mutation::getMutationInstitution,       (m, v) -> m.setMutationInstitution(text(v))),
            // Mutagenesis
            field("/mutagenesisStage",
                    Mutation::getMutagenesisStage,          (m, v) -> m.setMutagenesisStage(text(v))),
            field("/mutagenesisProtocol",
                    Mutation::getMutagenesisProtocol,       (m, v) -> m.setMutagenesisProtocol(text(v))),
            field("/molecularlyCharacterized",
                    Mutation::getMolecularlyCharacterized,  (m, v) -> m.setMolecularlyCharacterized(boolNullable(v))),
            // Lethality
            field("/homozygousLethal",
                    Mutation::getHomozygousLethal,          (m, v) -> m.setHomozygousLethal(boolNullable(v))),
            field("/lethalityStageTypical",
                    Mutation::getLethalityStageTypical,     (m, v) -> m.setLethalityStageTypical(text(v))),
            field("/lethalitySpecificTimepoint",
                    Mutation::getLethalitySpecificTimepoint, (m, v) -> m.setLethalitySpecificTimepoint(text(v))),
            field("/lethalityWindowStart",
                    Mutation::getLethalityWindowStart,      (m, v) -> m.setLethalityWindowStart(text(v))),
            field("/lethalityWindowEnd",
                    Mutation::getLethalityWindowEnd,        (m, v) -> m.setLethalityWindowEnd(text(v))),
            field("/lethalityAdditionalInfo",
                    Mutation::getLethalityAdditionalInfo,   (m, v) -> m.setLethalityAdditionalInfo(text(v))),
            // Publications — clear+addAll keeps Hibernate's persistent collection reference intact
            field("/publications",
                    Mutation::getPublications,
                    (m, v) -> {
                        m.getPublications().clear();
                        if (v != null && v.isArray()) {
                            for (int i = 0; i < v.size(); i++) {
                                String s = v.get(i).asText();
                                if (s != null && !s.isBlank()) {
                                    m.getPublications().add(s.trim());
                                }
                            }
                        }
                    })
    );

    // ─── schema builders ────────────────────────────────────────────────────
    // (Schema records live in org.zfin.zirc.api.jsonschema; helpers below
    //  return the typed records directly.)

    /** Hard cap mirroring the alt-branch (ZFIN-10265) MAX_CHILD_ROWS_PER_MUTATION. */
    public static final int MAX_ASSAYS_PER_MUTATION = 10;

    /**
     * Mirror of {@link org.zfin.zirc.dto.AssaySummaryDTO}; the per-card
     * header reads from this. Full assay fields come from a dedicated
     * /api/zirc/assays/{id} endpoint when a card is expanded (M4.2).
     */
    private static ArraySchema assaysSummaryArrayProp() {
        Map<String, JsonSchema> itemProps = new LinkedHashMap<>();
        itemProps.put("id",        NumberSchema.of());
        itemProps.put("sortOrder", NumberSchema.of());
        itemProps.put("assayType", StringSchema.nullable());
        return new ArraySchema("Genotyping Assays", ObjectSchema.of(itemProps),
                MAX_ASSAYS_PER_MUTATION, null);
    }

    /**
     * Mirror of {@link org.zfin.zirc.dto.GeneDTO}; the GenesListRenderer
     * shows a card per row with the mutatedGeneAbbreviation as the
     * header. The full per-gene fields are PATCHed via
     * {@code /api/zirc/genes/{id}} when a card is expanded.
     */
    private static ArraySchema genesArrayProp() {
        Map<String, JsonSchema> itemProps = new LinkedHashMap<>();
        itemProps.put("id",                      NumberSchema.of());
        itemProps.put("sortOrder",               NumberSchema.of());
        itemProps.put("mutatedGeneZdbID",        StringSchema.nullable());
        itemProps.put("mutatedGeneAbbreviation", StringSchema.nullable());
        return new ArraySchema("Genes", ObjectSchema.of(itemProps),
                10, null);
    }

    /**
     * Mirror of {@link org.zfin.zirc.dto.LesionSummaryDTO}; the
     * LesionsListRenderer card header reads {@code lesionType} (which
     * doubles as the discriminator on the inline-expanded form).
     */
    private static ArraySchema lesionsSummaryArrayProp() {
        Map<String, JsonSchema> itemProps = new LinkedHashMap<>();
        itemProps.put("id",         NumberSchema.of());
        itemProps.put("sortOrder",  NumberSchema.of());
        itemProps.put("lesionType", StringSchema.nullable());
        return new ArraySchema("Lesions", ObjectSchema.of(itemProps),
                10, null);
    }

    /**
     * Mirror of {@link org.zfin.zirc.dto.PhenotypeSummaryDTO}; the
     * PhenotypesListRenderer card header reads the truncated
     * {@code description}. Full per-phenotype fields come back from
     * {@code /api/zirc/phenotypes/{id}} on expand.
     */
    private static ArraySchema phenotypesSummaryArrayProp() {
        Map<String, JsonSchema> itemProps = new LinkedHashMap<>();
        itemProps.put("id",          NumberSchema.of());
        itemProps.put("sortOrder",   NumberSchema.of());
        itemProps.put("description", StringSchema.nullable());
        return new ArraySchema("Phenotypes", ObjectSchema.of(itemProps),
                10, null);
    }

    // ─── uiSchema builders ──────────────────────────────────────────────────
    // (now in org.zfin.zirc.api.uischema; construct VerticalLayout/Group/Control
    //  records directly above, using Group.of / Control.of for the shorthand cases.)

    // ─── descriptor builders ────────────────────────────────────────────────

    private static Map.Entry<String, FieldDescriptor> field(
            String path,
            Function<Mutation, ?> getter,
            BiConsumer<Mutation, JsonNode> setter) {
        return Map.entry(path, new FieldDescriptor(
                m -> MAPPER.valueToTree(getter.apply(m)),
                setter));
    }

    // ─── value coercers ────────────────────────────────────────────────────

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
