package org.zfin.zirc.service;

import org.zfin.zirc.entity.Gene;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.entity.Lesion;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.entity.Phenotype;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatus;
import org.zfin.zirc.service.LineSubmissionStatusComputer.FieldStatusResult;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * System-derived per-field / per-section / overall status for a {@link Mutation},
 * mirroring {@link LineSubmissionStatusComputer}. Currently only the General
 * section is wired; additional sections (Genes, Lesions, Genotyping Assays,
 * Phenotypes, Lethality, Publications) can be added the same way.
 */
public final class MutationStatusComputer {

    public enum Field {
        ALLELE_IN_ZFIN              ("alleleInZfin",              false),
        ALLELE_DESIGNATION          ("alleleDesignation",         true),
        MUTAGENESIS_STAGE           ("mutagenesisStage",          true),
        MUTAGENESIS_PROTOCOL        ("mutagenesisProtocol",       true),
        MOLECULARLY_CHARACTERIZED   ("molecularlyCharacterized",  false),
        MUTATION_TYPE               ("mutationType",              true),
        ZFIN_RECORD_ESTABLISHED     ("zfinRecordEstablished",     false),
        // Conditional applicability — the compute loop skips it unless
        // zfinRecordEstablished is explicitly Yes. Optional when applicable.
        CELL_GENOMIC_FEATURE        ("cellGenomicFeature",        false),
        MUTATION_DISCOVERER         ("mutationDiscoverer",        false),
        MUTATION_INSTITUTION        ("mutationInstitution",       false);

        private final String path;
        private final boolean required;

        Field(String path, boolean required) {
            this.path = path;
            this.required = required;
        }

        public String  getPath()    { return path; }
        public boolean isRequired() { return required; }

        public FieldStatus statusFor(Mutation m) {
            Object value = readProperty(m, path);
            boolean empty = isEmpty(value);
            if (empty && required) return FieldStatus.MISSING;
            return FieldStatus.COMPLETE;
        }
    }

    private MutationStatusComputer() {}

    public static FieldStatusResult compute(Mutation m) {
        Map<String, FieldStatus> byField = new LinkedHashMap<>();
        for (Field f : Field.values()) {
            if (f == Field.CELL_GENOMIC_FEATURE
                    && !Boolean.TRUE.equals(m.getZfinRecordEstablished())) {
                continue;
            }
            byField.put(f.getPath(), f.statusFor(m));
        }

        // Section keys match the JSP labels in line-submission-detail.jsp.
        // Only Overview has real field-driven rollup logic right now;
        // the others default to COMPLETE until per-section rules land.
        FieldStatus overview = FieldStatus.COMPLETE;
        for (FieldStatus st : byField.values()) overview = overview.worse(st);

        // Genes section is required at the Mutation level: a mutation must
        // declare at least one Gene; if present, each Gene rolls up its own
        // overall (mutatedGene is required per-row).
        FieldStatus genesSection;
        if (m.getGenes() == null || m.getGenes().isEmpty()) {
            genesSection = FieldStatus.MISSING;
        } else {
            genesSection = FieldStatus.COMPLETE;
            for (Gene g : m.getGenes()) {
                genesSection = genesSection.worse(GeneStatusComputer.compute(g).overall());
            }
        }

        // Lesions section: worst-of each Lesion's overall (lesionType is required
        // per-row). Empty collection is allowed → COMPLETE.
        FieldStatus lesionsSection = FieldStatus.COMPLETE;
        if (m.getLesions() != null) {
            for (Lesion lz : m.getLesions()) {
                lesionsSection = lesionsSection.worse(LesionStatusComputer.compute(lz).overall());
            }
        }

        // Genotyping Assays section: worst-of each assay's overall (assayType
        // is required per-row). Empty collection is allowed → COMPLETE.
        FieldStatus assaysSection = FieldStatus.COMPLETE;
        if (m.getGenotypingAssays() != null) {
            for (GenotypingAssay ga : m.getGenotypingAssays()) {
                assaysSection = assaysSection.worse(GenotypingAssayStatusComputer.compute(ga).overall());
            }
        }

        // Phenotypes section: worst-of each phenotype's overall (description
        // is required per-row). Empty collection is allowed → COMPLETE.
        FieldStatus phenotypesSection = FieldStatus.COMPLETE;
        if (m.getPhenotypes() != null) {
            for (Phenotype p : m.getPhenotypes()) {
                phenotypesSection = phenotypesSection.worse(PhenotypeStatusComputer.compute(p).overall());
            }
        }

        Map<String, FieldStatus> bySection = new LinkedHashMap<>();
        bySection.put("Overview",          overview);
        bySection.put("Genes",             genesSection);
        bySection.put("Lesions",           lesionsSection);
        bySection.put("Genotyping Assays", assaysSection);
        bySection.put("Phenotypes",        phenotypesSection);
        bySection.put("Lethality",         FieldStatus.COMPLETE);
        bySection.put("Publications",      FieldStatus.COMPLETE);

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
