package org.zfin.zirc.dto;

import org.zfin.zirc.entity.Gene;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.entity.Lesion;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.entity.Phenotype;

import java.util.Comparator;
import java.util.List;

public record MutationDTO(
        Long id,
        String lineSubmissionId,
        Integer sortOrder,
        // General
        String alleleDesignation,
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
        return new MutationDTO(
                m.getId(),
                m.getLineSubmission().getZdbID(),
                m.getSortOrder(),
                m.getAlleleDesignation(),
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
