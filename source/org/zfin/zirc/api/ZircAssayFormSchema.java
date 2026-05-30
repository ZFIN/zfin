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
import org.zfin.zirc.entity.GenotypingAssay;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Single source of truth for the per-assay edit form (M4.2). Parallels
 * {@link ZircFormSchema} / {@link ZircMutationFormSchema} but for the
 * {@link GenotypingAssay} aggregate.
 *
 * <p>The {@code assayType} dropdown at the top of the form drives conditional
 * reveal of field clusters via JSON Forms {@code rule} blocks with
 * {@code schema.enum} matchers. Each cluster lives in its own Group so the
 * rule applies to the whole block, not per-field.
 *
 * <p>Field/cluster matrix:
 * <ul>
 *   <li><b>General</b> — assayType, additionalInfo (always visible)</li>
 *   <li><b>PCR primers</b> — fwd/rev primers + expected PCR sizes; PCR, RFLP, dCAPS, sequencing, KASP</li>
 *   <li><b>Restriction digest</b> — RFLP, dCAPS</li>
 *   <li><b>Sequencing primer</b> — sequencing</li>
 *   <li><b>dCAPS mismatch primer</b> — dCAPS</li>
 *   <li><b>Allele-specific PCR</b> — AS-PCR</li>
 *   <li><b>KASP genomic sequence</b> — KASP</li>
 *   <li><b>SSLP</b> — SSLP</li>
 * </ul>
 *
 * <p>Canonical assay-type list is a starter — curators should review.
 */
public final class ZircAssayFormSchema {

    private ZircAssayFormSchema() {}

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Read + write for one form-schema path on a GenotypingAssay. */
    public record FieldDescriptor(
            Function<GenotypingAssay, JsonNode> read,
            BiConsumer<GenotypingAssay, JsonNode> write) {
    }

    // Stable enum tokens stored on the wire (and in ga_assay_type). Display
    // labels for the dropdown live in ASSAY_TYPE_LABELS, parallel by index.
    private static final List<String> ASSAY_TYPES = List.of(
            "pcr_gel", "pcr_sequencing", "rflp", "dcaps", "asa", "kasp", "hrma", "sslp");
    private static final List<String> ASSAY_TYPE_LABELS = List.of(
            "PCR + gel electrophoresis",
            "PCR + sequencing",
            "RFLP",
            "dCAPS",
            "ASA",
            "KASP",
            "HRMA",
            "SSLP");

    // Per-cluster reveal sets — each lists the assay types that should
    // show this Group's fields. Matches the old per-type field layouts.
    private static final List<String> FWD_REV_PRIMER_TYPES =
            List.of("pcr_gel", "pcr_sequencing", "rflp", "dcaps", "hrma", "sslp");
    private static final List<String> EXPECTED_PCR_TYPES =
            List.of("pcr_gel", "pcr_sequencing", "rflp", "dcaps", "asa", "kasp", "hrma");
    private static final List<String> SEQUENCING_TYPES =
            List.of("pcr_sequencing");
    private static final List<String> DCAPS_TYPES =
            List.of("dcaps");
    private static final List<String> ALLELE_SPECIFIC_TYPES =
            List.of("asa", "kasp");
    private static final List<String> KASP_TYPES =
            List.of("kasp");
    private static final List<String> DIGEST_TYPES =
            List.of("rflp", "dcaps");
    private static final List<String> SSLP_TYPES =
            List.of("sslp");
    // Per-type attachment-bucket labels — each visibility rule shows the
    // attachments Control with the matching heading for that workflow.
    private static final List<String> GEL_IMAGE_TYPES =
            List.of("pcr_gel", "rflp", "dcaps", "sslp");
    private static final List<String> CHROMATOGRAM_TYPES =
            List.of("pcr_sequencing");
    private static final List<String> RESULT_IMAGE_TYPES =
            List.of("asa", "kasp");
    private static final List<String> MELT_CURVE_TYPES =
            List.of("hrma");

    public static JsonSchema schema() {
        Map<String, JsonSchema> properties = new LinkedHashMap<>();
        // General
        properties.put("assayType",                  StringSchema.of("Assay type", 255));
        // PCR primers
        properties.put("forwardPrimer",              StringSchema.of("Forward primer", 2000));
        properties.put("reversePrimer",              StringSchema.of("Reverse primer", 2000));
        properties.put("expectedWtPcr",              StringSchema.of("Expected wild-type PCR product", 2000));
        properties.put("expectedMutPcr",             StringSchema.of("Expected mutant PCR product", 2000));
        // Sequencing
        properties.put("sequencingPrimer",           StringSchema.of("Sequencing primer", 2000));
        // dCAPS
        properties.put("dcapsMismatchPrimer",        StringSchema.of("Primer with introduced mismatch", 2000));
        // Allele-specific (ASA + KASP)
        properties.put("wtSpecificPrimer",           StringSchema.of("WT-specific primer", 2000));
        properties.put("mutSpecificPrimer",          StringSchema.of("Mutant-specific primer", 2000));
        properties.put("commonPrimer",               StringSchema.of("Common primer", 2000));
        // KASP
        properties.put("kaspGenomicSequence",        StringSchema.of("Genomic DNA sequence (KASP design)", 5000));
        // RFLP / dCAPS
        properties.put("restrictionEnzymeName",      StringSchema.of("Restriction enzyme name", 255));
        properties.put("restrictionEnzymeCatalog",   StringSchema.of("Restriction enzyme catalog #", 255));
        properties.put("enzymeCleavesWt",            BooleanSchema.nullable("Enzyme cleaves WT template"));
        properties.put("enzymeCleavesMut",           BooleanSchema.nullable("Enzyme cleaves MUT template"));
        properties.put("expectedWtDigest",           StringSchema.of("Expected WT product after digest", 2000));
        properties.put("expectedMutDigest",          StringSchema.of("Expected MUT product after digest", 2000));
        // SSLP
        properties.put("sslpMarkerName",             StringSchema.of("SSLP marker name", 255));
        properties.put("sslpDistance",               StringSchema.of("Distance marker → mutation", 255));
        properties.put("sslpGenomicLocation",        StringSchema.of("Genomic location of marker", 255));
        properties.put("sslpInducedBackground",      StringSchema.of("Background mutation was induced on", 255));
        properties.put("sslpOutcrossedBackground",   StringSchema.of("Recommended outcrossing background", 255));
        properties.put("sslpInducedPcr",             StringSchema.of("PCR product on induced background", 2000));
        properties.put("sslpOutcrossedPcr",          StringSchema.of("PCR product on outcrossing background", 2000));
        // Catch-all
        properties.put("additionalInfo",             StringSchema.of("Additional info", 5000));
        // Attachments — summary rows; uploads happen through a dedicated
        // multipart endpoint, not the field-path PATCH (AssayEdit's diff
        // filter must skip /attachments).
        properties.put("attachments",                attachmentsArrayProp());
        return ObjectSchema.of(null, properties, List.of("assayType"));
    }

    public static UiSchemaElement uiSchema() {
        // All groups are headless — the inline assay editor card already
        // carries the "Assay #N — <type>" header. Group structure is kept
        // so per-assayType visibility rules continue to work.
        //
        // Field ordering inside each per-type group matches the old form,
        // including the placement of the attachment buckets (e.g. SSLP
        // shows the gel-image upload right after the primers, before the
        // SSLP-specific fields).
        Options actgnHelp = Options.of().helpText("ACTGN only.");
        return new VerticalLayout(List.of(
                Group.of(null, List.of(
                        new Control("#/properties/assayType",
                                Options.of()
                                        .widget("selectWithOther")
                                        .standardValues(ASSAY_TYPES)
                                        .standardLabels(ASSAY_TYPE_LABELS)
                                        .noOther(true)
                                        .refreshesParent(true),
                                null)
                )),
                // Forward / Reverse primer — shown for the six types that
                // use a plain PCR-style primer pair. ASA and KASP use the
                // WT/mut/common trio instead and are excluded here.
                groupRevealedFor(FWD_REV_PRIMER_TYPES, List.of(
                        new Control("#/properties/forwardPrimer", actgnHelp, null),
                        new Control("#/properties/reversePrimer", actgnHelp, null)
                )),
                // Expected WT/MUT PCR product — shown for every type that
                // produces a PCR amplicon (everything except sslp).
                groupRevealedFor(EXPECTED_PCR_TYPES, List.of(
                        Control.of("#/properties/expectedWtPcr"),
                        Control.of("#/properties/expectedMutPcr")
                )),
                // PCR + sequencing — sits between the PCR products and the
                // chromatogram-attachments block.
                groupRevealedFor(SEQUENCING_TYPES, List.of(
                        new Control("#/properties/sequencingPrimer", actgnHelp, null)
                )),
                // dCAPS — one extra primer field, before the digest block.
                groupRevealedFor(DCAPS_TYPES, List.of(
                        new Control("#/properties/dcapsMismatchPrimer", actgnHelp, null)
                )),
                // ASA + KASP — WT/mut/common primer trio. KASP adds the
                // genomic-sequence textarea in its own group below.
                groupRevealedFor(ALLELE_SPECIFIC_TYPES, List.of(
                        new Control("#/properties/wtSpecificPrimer",  actgnHelp, null),
                        new Control("#/properties/mutSpecificPrimer", actgnHelp, null),
                        new Control("#/properties/commonPrimer",      actgnHelp, null)
                )),
                groupRevealedFor(KASP_TYPES, List.of(
                        new Control("#/properties/kaspGenomicSequence",
                                Options.of().multi(true), null)
                )),
                // RFLP + dCAPS digest block — same fields for both types.
                groupRevealedFor(DIGEST_TYPES, List.of(
                        new Control("#/properties/restrictionEnzymeName",
                                Options.of().placeholder("e.g. BsmBI"), null),
                        new Control("#/properties/restrictionEnzymeCatalog",
                                Options.of()
                                        .placeholder("vendor + cat #")
                                        .infoHref("https://international.neb.com/"),
                                null),
                        new Control("#/properties/enzymeCleavesWt",
                                Options.of().widget("checkbox"), null),
                        new Control("#/properties/enzymeCleavesMut",
                                Options.of().widget("checkbox"), null),
                        Control.of("#/properties/expectedWtDigest"),
                        Control.of("#/properties/expectedMutDigest")
                )),
                // Attachment buckets — same underlying `attachments` field,
                // labeled per-workflow via four parallel Controls with
                // visibility rules.
                attachmentBucketFor(GEL_IMAGE_TYPES,    "Annotated gel images"),
                attachmentBucketFor(CHROMATOGRAM_TYPES, "Chromatograms"),
                attachmentBucketFor(RESULT_IMAGE_TYPES, "Annotated result images"),
                attachmentBucketFor(MELT_CURVE_TYPES,   "Annotated melt curve files"),
                // SSLP — its primers + gel-image bucket render above (in
                // FWD_REV_PRIMER_TYPES / GEL_IMAGE_TYPES); the type-specific
                // metadata fields all live here.
                groupRevealedFor(SSLP_TYPES, List.of(
                        new Control("#/properties/sslpMarkerName",
                                Options.of().placeholder("Search ZFIN SSLP markers…"), null),
                        Control.of("#/properties/sslpDistance"),
                        Control.of("#/properties/sslpGenomicLocation"),
                        Control.of("#/properties/sslpInducedBackground"),
                        Control.of("#/properties/sslpOutcrossedBackground"),
                        Control.of("#/properties/sslpInducedPcr"),
                        Control.of("#/properties/sslpOutcrossedPcr")
                )),
                // Additional info — last row in every variant.
                Group.of(null, List.of(
                        new Control("#/properties/additionalInfo",
                                Options.of().multi(true), null)
                ))
        ));
    }

    /**
     * Build one Attachments Control wrapped in a headless visibility-gated
     * group, labeled with the per-workflow bucket heading. All four buckets
     * share the same backing {@code attachments} array on the entity; the
     * label changes are UI-only.
     */
    private static Group attachmentBucketFor(List<String> assayTypes, String label) {
        return new Group(null,
                List.of(new Control("#/properties/attachments",
                        Options.of()
                                .widget("attachmentsList")
                                .managesOwnPersistence(true)
                                .label(label),
                        null)),
                Options.of().layout("plain"),
                Rule.showWhenIn("#/properties/assayType", assayTypes));
    }

    /**
     * Tiny helper specific to the assay-type matrix: a headless Group
     * revealed when {@code assayType} is one of the listed values.
     */
    private static Group groupRevealedFor(
            List<String> assayTypes, List<UiSchemaElement> elements) {
        return new Group(null, elements, null,
                Rule.showWhenIn("#/properties/assayType", assayTypes));
    }

    /**
     * Path → read+write dispatch for assay fields. Same gatekeeper behavior
     * as the other two FIELDS maps: unknown paths are rejected at the
     * controller. enzymeCleaves uses a list-rewrite write so Hibernate sees
     * a new array instance per save.
     */
    public static final Map<String, FieldDescriptor> FIELDS = Map.ofEntries(
            field("/assayType",                GenotypingAssay::getAssayType,                (a, v) -> a.setAssayType(text(v))),
            field("/additionalInfo",           GenotypingAssay::getAdditionalInfo,           (a, v) -> a.setAdditionalInfo(text(v))),
            field("/forwardPrimer",            GenotypingAssay::getForwardPrimer,            (a, v) -> a.setForwardPrimer(text(v))),
            field("/reversePrimer",            GenotypingAssay::getReversePrimer,            (a, v) -> a.setReversePrimer(text(v))),
            field("/expectedWtPcr",            GenotypingAssay::getExpectedWtPcr,            (a, v) -> a.setExpectedWtPcr(text(v))),
            field("/expectedMutPcr",           GenotypingAssay::getExpectedMutPcr,           (a, v) -> a.setExpectedMutPcr(text(v))),
            field("/sequencingPrimer",         GenotypingAssay::getSequencingPrimer,         (a, v) -> a.setSequencingPrimer(text(v))),
            field("/dcapsMismatchPrimer",      GenotypingAssay::getDcapsMismatchPrimer,      (a, v) -> a.setDcapsMismatchPrimer(text(v))),
            field("/wtSpecificPrimer",         GenotypingAssay::getWtSpecificPrimer,         (a, v) -> a.setWtSpecificPrimer(text(v))),
            field("/mutSpecificPrimer",        GenotypingAssay::getMutSpecificPrimer,        (a, v) -> a.setMutSpecificPrimer(text(v))),
            field("/commonPrimer",             GenotypingAssay::getCommonPrimer,             (a, v) -> a.setCommonPrimer(text(v))),
            field("/kaspGenomicSequence",      GenotypingAssay::getKaspGenomicSequence,      (a, v) -> a.setKaspGenomicSequence(text(v))),
            field("/restrictionEnzymeName",    GenotypingAssay::getRestrictionEnzymeName,    (a, v) -> a.setRestrictionEnzymeName(text(v))),
            field("/restrictionEnzymeCatalog", GenotypingAssay::getRestrictionEnzymeCatalog, (a, v) -> a.setRestrictionEnzymeCatalog(text(v))),
            field("/enzymeCleavesWt",          GenotypingAssay::getEnzymeCleavesWt,          (a, v) -> a.setEnzymeCleavesWt(boolNullable(v))),
            field("/enzymeCleavesMut",         GenotypingAssay::getEnzymeCleavesMut,         (a, v) -> a.setEnzymeCleavesMut(boolNullable(v))),
            field("/expectedWtDigest",         GenotypingAssay::getExpectedWtDigest,         (a, v) -> a.setExpectedWtDigest(text(v))),
            field("/expectedMutDigest",        GenotypingAssay::getExpectedMutDigest,        (a, v) -> a.setExpectedMutDigest(text(v))),
            field("/sslpMarkerName",           GenotypingAssay::getSslpMarkerName,           (a, v) -> a.setSslpMarkerName(text(v))),
            field("/sslpDistance",             GenotypingAssay::getSslpDistance,             (a, v) -> a.setSslpDistance(text(v))),
            field("/sslpGenomicLocation",      GenotypingAssay::getSslpGenomicLocation,      (a, v) -> a.setSslpGenomicLocation(text(v))),
            field("/sslpInducedBackground",    GenotypingAssay::getSslpInducedBackground,    (a, v) -> a.setSslpInducedBackground(text(v))),
            field("/sslpOutcrossedBackground", GenotypingAssay::getSslpOutcrossedBackground, (a, v) -> a.setSslpOutcrossedBackground(text(v))),
            field("/sslpInducedPcr",           GenotypingAssay::getSslpInducedPcr,           (a, v) -> a.setSslpInducedPcr(text(v))),
            field("/sslpOutcrossedPcr",        GenotypingAssay::getSslpOutcrossedPcr,        (a, v) -> a.setSslpOutcrossedPcr(text(v)))
    );

    // ─── schema builders ────────────────────────────────────────────────────
    // (Schema records live in org.zfin.zirc.api.jsonschema; helpers below
    //  return the typed records directly.)

    /** Hard cap on attachments per assay; same MAX_CHILD_ROWS_PER_MUTATION shape from the alt branch. */
    public static final int MAX_ATTACHMENTS_PER_ASSAY = 10;

    /**
     * Mirror of {@link org.zfin.zirc.dto.AssayFileDTO}; the renderer reads
     * the summary fields. File content is fetched via the streaming
     * endpoint, not as part of the form data.
     */
    private static ArraySchema attachmentsArrayProp() {
        Map<String, JsonSchema> itemProps = new LinkedHashMap<>();
        itemProps.put("id",               NumberSchema.of());
        itemProps.put("originalFilename", new StringSchema(null, null, null, null, null));
        itemProps.put("contentType",      StringSchema.nullable());
        itemProps.put("fileSize",         new NumberSchema(null, Boolean.TRUE));
        itemProps.put("uploadedAt",       StringSchema.nullable());
        return new ArraySchema("Attachments", ObjectSchema.of(itemProps),
                MAX_ATTACHMENTS_PER_ASSAY, null);
    }

    // ─── uiSchema builders ──────────────────────────────────────────────────
    // (now in org.zfin.zirc.api.uischema; construct VerticalLayout/Group/Control
    //  records directly above. groupRevealedFor is the local helper for the
    //  assay-type matrix's repeated "SHOW when assayType in […]" pattern.)

    // ─── descriptor builders ────────────────────────────────────────────────

    private static Map.Entry<String, FieldDescriptor> field(
            String path,
            Function<GenotypingAssay, ?> getter,
            BiConsumer<GenotypingAssay, JsonNode> setter) {
        return Map.entry(path, new FieldDescriptor(
                a -> MAPPER.valueToTree(getter.apply(a)),
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

    private static String[] stringArray(JsonNode v) {
        if (v == null || v.isNull() || !v.isArray()) {return new String[0];}
        List<String> kept = new java.util.ArrayList<>(v.size());
        for (int i = 0; i < v.size(); i++) {
            String s = v.get(i).asText();
            if (s != null && !s.isBlank()) {
                kept.add(s.trim());
            }
        }
        return kept.toArray(new String[0]);
    }
}
