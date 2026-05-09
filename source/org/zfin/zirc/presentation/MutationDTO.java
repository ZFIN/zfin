package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zfin.zirc.entity.Gene;
import org.zfin.zirc.entity.Mutation;

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
    private String mutagenesisProtocol;
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

    public static MutationDTO from(Mutation m) {
        MutationDTO dto = new MutationDTO();
        dto.setId(m.getId());
        dto.setLineSubmissionId(m.getLineSubmission() != null ? m.getLineSubmission().getZdbID() : null);
        dto.setSortOrder(m.getSortOrder());
        dto.setAlleleDesignation(m.getAlleleDesignation());
        dto.setMutagenesisProtocol(m.getMutagenesisProtocol());
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
        return dto;
    }
}
