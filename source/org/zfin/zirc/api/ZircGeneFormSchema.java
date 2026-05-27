package org.zfin.zirc.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.zirc.api.jsonschema.JsonSchema;
import org.zfin.zirc.api.jsonschema.ObjectSchema;
import org.zfin.zirc.api.jsonschema.StringSchema;
import org.zfin.zirc.api.uischema.Control;
import org.zfin.zirc.api.uischema.Group;
import org.zfin.zirc.api.uischema.Options;
import org.zfin.zirc.api.uischema.UiSchemaElement;
import org.zfin.zirc.api.uischema.VerticalLayout;
import org.zfin.zirc.entity.Gene;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Single source of truth for the per-gene edit form (M6.1). Parallel to
 * {@link ZircAssayFormSchema} but smaller — Gene only has four
 * editable fields beyond the parent FK and sort order.
 *
 * <p>{@code mutatedGeneZdbID} uses the {@code autocomplete} widget
 * targeting {@code /api/zirc/autocomplete/markers}; the value stored
 * back is the marker's ZDB-ID and the FK lookup happens server-side
 * in the FIELDS write closure.
 */
public final class ZircGeneFormSchema {

    private ZircGeneFormSchema() {}

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Read + write for one form-schema path on a Gene. */
    public record FieldDescriptor(
            Function<Gene, JsonNode> read,
            BiConsumer<Gene, JsonNode> write) {
    }

    public static JsonSchema schema() {
        Map<String, JsonSchema> properties = new LinkedHashMap<>();
        properties.put("mutatedGeneZdbID",  StringSchema.of("Gene (ZDB-ID)", 50));
        properties.put("linkageGroup",      StringSchema.of("Linkage Group", 255));
        properties.put("genbankGenomicDna", StringSchema.of("GenBank Genomic DNA", 255));
        properties.put("genbankCdna",       StringSchema.of("GenBank cDNA", 255));
        return ObjectSchema.of(null, properties, java.util.List.of("mutatedGeneZdbID", "genbankGenomicDna"));
    }

    public static UiSchemaElement uiSchema() {
        return new VerticalLayout(java.util.List.of(
                Group.of("Gene", java.util.List.of(
                        new Control("#/properties/mutatedGeneZdbID",
                                Options.of()
                                        .widget("autocomplete")
                                        // Searches the markers endpoint
                                        // narrowed to GENEDOM (gene/pseudogene/
                                        // miRNA etc.) so non-gene markers like
                                        // SSLPs or BACs don't pollute the
                                        // dropdown. The selected item's value
                                        // (ZDB-ID) is what we PATCH back.
                                        .searchEndpoint("markers")
                                        .typeGroup("GENEDOM")
                                        .placeholder("Start typing a gene name…")
                                        .helpText("Resolves to the ZFIN marker ZDB-ID. Leave blank if unknown.")
                                        // The parent gene card shows the denormalized marker
                                        // abbreviation derived from this id — refresh it on change.
                                        .refreshesParent(true),
                                null),
                        new Control("#/properties/linkageGroup",
                                Options.of().placeholder("e.g. 5"),
                                null),
                        new Control("#/properties/genbankGenomicDna",
                                Options.of().placeholder("Accession, e.g. NC_007112.7"),
                                null),
                        new Control("#/properties/genbankCdna",
                                Options.of().placeholder("Accession, e.g. NM_001077291"),
                                null)
                ))
        ));
    }

    /** Resolve a marker ZDB-ID to its entity; returns null for null/blank. */
    private static Marker resolveMarker(String zdbID) {
        if (zdbID == null || zdbID.isBlank()) {return null;}
        Marker m = HibernateUtil.currentSession().get(Marker.class, zdbID.trim());
        if (m == null) {
            throw new IllegalArgumentException("Marker " + zdbID + " not found");
        }
        return m;
    }

    public static final Map<String, FieldDescriptor> FIELDS = Map.ofEntries(
            field("/mutatedGeneZdbID",
                    g -> g.getMutatedGene() == null ? null : g.getMutatedGene().getZdbID(),
                    (g, v) -> g.setMutatedGene(resolveMarker(text(v)))),
            field("/linkageGroup",
                    Gene::getLinkageGroup,
                    (g, v) -> g.setLinkageGroup(text(v))),
            field("/genbankGenomicDna",
                    Gene::getGenbankGenomicDna,
                    (g, v) -> g.setGenbankGenomicDna(text(v))),
            field("/genbankCdna",
                    Gene::getGenbankCdna,
                    (g, v) -> g.setGenbankCdna(text(v)))
    );

    private static Map.Entry<String, FieldDescriptor> field(
            String path,
            Function<Gene, ?> getter,
            BiConsumer<Gene, JsonNode> setter) {
        return Map.entry(path, new FieldDescriptor(
                g -> MAPPER.valueToTree(getter.apply(g)),
                setter));
    }

    private static String text(JsonNode v) {
        if (v == null || v.isNull()) {return null;}
        String s = v.asText();
        return s.isBlank() ? null : s.trim();
    }
}
