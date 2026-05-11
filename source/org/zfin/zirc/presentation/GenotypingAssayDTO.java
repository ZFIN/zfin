package org.zfin.zirc.presentation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.zfin.zirc.entity.GenotypingAssay;

/**
 * Wire format for one per-mutation genotyping-assay row. The assayType
 * selects which subset of fields is meaningful — see typeMatrices.ts on
 * the client for the per-type field map. Inapplicable fields are null.
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
    private String restrictionEnzymeName;
    private String restrictionEnzymeCatalog;
    private String[] enzymeCleaves;
    private String expectedWtDigest;
    private String expectedMutDigest;
    private String additionalInfo;
    private String sequencingPrimer;
    private String dcapsMismatchPrimer;
    private String wtSpecificPrimer;
    private String mutSpecificPrimer;
    private String commonPrimer;
    private String kaspGenomicSequence;
    private String sslpMarkerName;
    private String sslpDistance;
    private String sslpGenomicLocation;
    private String sslpInducedBackground;
    private String sslpOutcrossedBackground;
    private String sslpInducedPcr;
    private String sslpOutcrossedPcr;
    private Boolean chromatogramFilesAvailable;
    private Boolean gelImagesAvailable;
    private Boolean resultImagesAvailable;
    private Boolean meltCurveFilesAvailable;

    public static GenotypingAssayDTO from(GenotypingAssay g) {
        GenotypingAssayDTO dto = new GenotypingAssayDTO();
        dto.setId(g.getId());
        dto.setSortOrder(g.getSortOrder());
        dto.setAssayType(g.getAssayType());
        dto.setForwardPrimer(g.getForwardPrimer());
        dto.setReversePrimer(g.getReversePrimer());
        dto.setExpectedWtPcr(g.getExpectedWtPcr());
        dto.setExpectedMutPcr(g.getExpectedMutPcr());
        dto.setRestrictionEnzymeName(g.getRestrictionEnzymeName());
        dto.setRestrictionEnzymeCatalog(g.getRestrictionEnzymeCatalog());
        dto.setEnzymeCleaves(g.getEnzymeCleaves());
        dto.setExpectedWtDigest(g.getExpectedWtDigest());
        dto.setExpectedMutDigest(g.getExpectedMutDigest());
        dto.setAdditionalInfo(g.getAdditionalInfo());
        dto.setSequencingPrimer(g.getSequencingPrimer());
        dto.setDcapsMismatchPrimer(g.getDcapsMismatchPrimer());
        dto.setWtSpecificPrimer(g.getWtSpecificPrimer());
        dto.setMutSpecificPrimer(g.getMutSpecificPrimer());
        dto.setCommonPrimer(g.getCommonPrimer());
        dto.setKaspGenomicSequence(g.getKaspGenomicSequence());
        dto.setSslpMarkerName(g.getSslpMarkerName());
        dto.setSslpDistance(g.getSslpDistance());
        dto.setSslpGenomicLocation(g.getSslpGenomicLocation());
        dto.setSslpInducedBackground(g.getSslpInducedBackground());
        dto.setSslpOutcrossedBackground(g.getSslpOutcrossedBackground());
        dto.setSslpInducedPcr(g.getSslpInducedPcr());
        dto.setSslpOutcrossedPcr(g.getSslpOutcrossedPcr());
        dto.setChromatogramFilesAvailable(g.getChromatogramFilesAvailable());
        dto.setGelImagesAvailable(g.getGelImagesAvailable());
        dto.setResultImagesAvailable(g.getResultImagesAvailable());
        dto.setMeltCurveFilesAvailable(g.getMeltCurveFilesAvailable());
        return dto;
    }
}
