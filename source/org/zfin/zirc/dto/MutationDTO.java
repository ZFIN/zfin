package org.zfin.zirc.dto;

import jakarta.validation.constraints.NotNull;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.zirc.entity.Gene;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.entity.Lesion;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.entity.Phenotype;

import java.util.Comparator;
import java.util.List;

public record MutationDTO(
        @NotNull Long id,
        @NotNull String lineSubmissionId,
        @NotNull Integer sortOrder,
        // General
        String alleleDesignation,
        // Server-resolved display label when alleleDesignation holds a ZDB-ID
        // (alleleInZfin=true → the field stores the Feature ZDB-ID). Null
        // when alleleInZfin=false (alleleDesignation is just free text and
        // the UI displays it directly) or when the referenced Feature can't
        // be resolved. Read-only — not part of the form schema.
        String alleleName,
        Boolean alleleInZfin,
        String mutationType,
        String mutationDiscoverer,
        String mutationInstitution,
        // Mutagenesis
        String mutagenesisStage,
        String mutagenesisProtocol,
        Boolean molecularlyCharacterized,
        // Lethality
        Boolean homozygousLethal,
        String lethalityStageTypical,
        String lethalitySpecificTimepoint,
        String lethalityWindowStart,
        String lethalityWindowEnd,
        String lethalityAdditionalInfo,
        // Publications
        List<String> publications,
        // Genotyping assays — summary rows only; full per-assay fields are
        // fetched separately when a card is expanded.
        List<AssaySummaryDTO> assays,
        // Per-mutation genes — full records (only ~5 fields, no point in
        // a summary slice). Inline-expand card pattern same as assays.
        List<GeneDTO> genes,
        // Per-mutation lesions — summary rows only; full per-lesion fields
        // come from /api/zirc/lesions/{id} when a card expands.
        List<LesionSummaryDTO> lesions,
        // Per-mutation phenotypes — summary rows only; full per-phenotype
        // fields come from /api/zirc/phenotypes/{id} when a card expands.
        List<PhenotypeSummaryDTO> phenotypes) {

    public static MutationDTO of(Mutation m) {
        List<AssaySummaryDTO> assays = m.getGenotypingAssays() == null ? List.of() :
                m.getGenotypingAssays().stream()
                        .sorted(Comparator.comparing(
                                GenotypingAssay::getSortOrder,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(AssaySummaryDTO::of)
                        .toList();
        List<GeneDTO> genes = m.getGenes() == null ? List.of() :
                m.getGenes().stream()
                        .sorted(Comparator.comparing(
                                Gene::getSortOrder,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(GeneDTO::of)
                        .toList();
        List<LesionSummaryDTO> lesions = m.getLesions() == null ? List.of() :
                m.getLesions().stream()
                        .sorted(Comparator.comparing(
                                Lesion::getSortOrder,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(LesionSummaryDTO::of)
                        .toList();
        List<PhenotypeSummaryDTO> phenotypes = m.getPhenotypes() == null ? List.of() :
                m.getPhenotypes().stream()
                        .sorted(Comparator.comparing(
                                Phenotype::getSortOrder,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(PhenotypeSummaryDTO::of)
                        .toList();
        String alleleName = null;
        // Resolve a display label only when the curator picked an existing
        // ZFIN feature (alleleInZfin=true → alleleDesignation stores the
        // ZDB-ID returned by the /features autocomplete). Free-text designations
        // are rendered as-is.
        if (Boolean.TRUE.equals(m.getAlleleInZfin())
                && m.getAlleleDesignation() != null
                && m.getAlleleDesignation().startsWith("ZDB-")) {
            Feature f = HibernateUtil.currentSession().get(Feature.class, m.getAlleleDesignation());
            if (f != null) {
                alleleName = f.getAbbreviation();
            }
        }
        return new MutationDTO(
                m.getId(),
                m.getLineSubmission().getZdbID(),
                m.getSortOrder(),
                m.getAlleleDesignation(),
                alleleName,
                m.getAlleleInZfin(),
                m.getMutationType(),
                m.getMutationDiscoverer(),
                m.getMutationInstitution(),
                m.getMutagenesisStage(),
                m.getMutagenesisProtocol(),
                m.getMolecularlyCharacterized(),
                m.getHomozygousLethal(),
                m.getLethalityStageTypical(),
                m.getLethalitySpecificTimepoint(),
                m.getLethalityWindowStart(),
                m.getLethalityWindowEnd(),
                m.getLethalityAdditionalInfo(),
                List.copyOf(m.getPublications() == null ? List.of() : m.getPublications()),
                assays,
                genes,
                lesions,
                phenotypes);
    }
}
