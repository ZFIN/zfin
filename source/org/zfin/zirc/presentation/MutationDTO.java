package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zfin.zirc.entity.Gene;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.entity.Lesion;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.entity.Phenotype;

import java.util.Comparator;
import java.util.List;

/**
 * Wire format for one mutation. Used both for the per-mutation editor
 * sub-page (full set of scalar fields) and for the parent line submission's
 * mutations list (where most fields are unused but the payload is small
 * enough — submissions cap at five mutations per the form spec).
 *
 * <p>Nested children (genes, lesions, genotyping assays, phenotypes)
 * are deferred to a follow-up — they'll get their own DTOs and endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
public class MutationDTO {

    private Long id;
    private String lineSubmissionId;
    private Integer sortOrder;

    private String alleleDesignation;
    private Boolean alleleInZfin;
    private String mutagenesisStage;
    private String mutagenesisProtocol;
    private String mutagenesisProtocolOther;
    private Boolean molecularlyCharacterized;
    private String mutationType;

    private Boolean homozygousLethal;
    private String lethalityStageTypical;
    private String lethalitySpecificTimepoint;
    private String lethalityWindowStart;
    private String lethalityWindowEnd;
    private String lethalityAdditionalInfo;

    private Boolean zfinRecordEstablished;
    private String cellGenomicFeature;
    private String mutationDiscoverer;
    private String mutationInstitution;

    private List<GeneDTO> genes;
    private List<LesionDTO> lesions;
    private List<GenotypingAssayDTO> genotypingAssays;
    private List<PhenotypeDTO> phenotypes;
    private List<String> publications;

    public static MutationDTO from(Mutation m) {
        MutationDTO dto = new MutationDTO();
        dto.setId(m.getId());
        dto.setLineSubmissionId(m.getLineSubmission() != null ? m.getLineSubmission().getZdbID() : null);
        dto.setSortOrder(m.getSortOrder());
        dto.setAlleleDesignation(m.getAlleleDesignation());
        dto.setAlleleInZfin(m.getAlleleInZfin());
        dto.setMutagenesisStage(m.getMutagenesisStage());
        dto.setMutagenesisProtocol(m.getMutagenesisProtocol());
        dto.setMutagenesisProtocolOther(m.getMutagenesisProtocolOther());
        dto.setMolecularlyCharacterized(m.getMolecularlyCharacterized());
        dto.setMutationType(m.getMutationType());
        dto.setHomozygousLethal(m.getHomozygousLethal());
        dto.setLethalityStageTypical(m.getLethalityStageTypical());
        dto.setLethalitySpecificTimepoint(m.getLethalitySpecificTimepoint());
        dto.setLethalityWindowStart(m.getLethalityWindowStart());
        dto.setLethalityWindowEnd(m.getLethalityWindowEnd());
        dto.setLethalityAdditionalInfo(m.getLethalityAdditionalInfo());
        dto.setZfinRecordEstablished(m.getZfinRecordEstablished());
        dto.setCellGenomicFeature(m.getCellGenomicFeature());
        dto.setMutationDiscoverer(m.getMutationDiscoverer());
        dto.setMutationInstitution(m.getMutationInstitution());
        dto.setGenes(
            m.getGenes().stream()
                .sorted(Comparator.comparing(Gene::getSortOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(GeneDTO::from)
                .toList());
        dto.setLesions(
            m.getLesions().stream()
                .sorted(Comparator.comparing(Lesion::getSortOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(LesionDTO::from)
                .toList());
        dto.setGenotypingAssays(
            m.getGenotypingAssays().stream()
                .sorted(Comparator.comparing(GenotypingAssay::getSortOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(GenotypingAssayDTO::from)
                .toList());
        dto.setPhenotypes(
            m.getPhenotypes().stream()
                .sorted(Comparator.comparing(Phenotype::getSortOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(PhenotypeDTO::from)
                .toList());
        dto.setPublications(List.copyOf(m.getPublications()));
        return dto;
    }
}
