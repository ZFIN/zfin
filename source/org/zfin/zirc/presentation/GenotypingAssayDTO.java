package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zfin.zirc.entity.GenotypingAssay;

/**
 * Wire format for one per-mutation genotyping-assay row.
 */
@Getter
@Setter
@NoArgsConstructor
public class GenotypingAssayDTO {

    private Long id;
    private Integer sortOrder;
    private String assayType;
    private String forwardPrimer;
    private String reversePrimer;
    private String expectedWtPcr;
    private String expectedMutPcr;
    private String restrictionEnzyme;
    private String enzymeCleaves;
    private String expectedWtDigest;
    private String expectedMutDigest;
    private String additionalInfo;

    public static GenotypingAssayDTO from(GenotypingAssay g) {
        GenotypingAssayDTO dto = new GenotypingAssayDTO();
        dto.setId(g.getId());
        dto.setSortOrder(g.getSortOrder());
        dto.setAssayType(g.getAssayType());
        dto.setForwardPrimer(g.getForwardPrimer());
        dto.setReversePrimer(g.getReversePrimer());
        dto.setExpectedWtPcr(g.getExpectedWtPcr());
        dto.setExpectedMutPcr(g.getExpectedMutPcr());
        dto.setRestrictionEnzyme(g.getRestrictionEnzyme());
        dto.setEnzymeCleaves(g.getEnzymeCleaves());
        dto.setExpectedWtDigest(g.getExpectedWtDigest());
        dto.setExpectedMutDigest(g.getExpectedMutDigest());
        dto.setAdditionalInfo(g.getAdditionalInfo());
        return dto;
    }
}
