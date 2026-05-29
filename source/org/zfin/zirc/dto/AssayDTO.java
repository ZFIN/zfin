package org.zfin.zirc.dto;

import jakarta.validation.constraints.NotNull;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.entity.GenotypingAssayFile;

import java.util.Comparator;
import java.util.List;

/**
 * Full per-assay payload returned by GET /api/zirc/assays/{id}. Every column
 * in {@code zirc.genotyping_assay} is reachable here so the per-assay schema
 * editor (M4.2) can read & write any field; the uiSchema's conditional-show
 * rules decide what's <em>visible</em>, the persistence layer doesn't.
 */
public record AssayDTO(
        @NotNull Long id,
        @NotNull Long mutationId,
        @NotNull Integer sortOrder,
        String assayType,
        // PCR core
        String forwardPrimer,
        String reversePrimer,
        String expectedWtPcr,
        String expectedMutPcr,
        // Sequencing
        String sequencingPrimer,
        // dCAPS
        String dcapsMismatchPrimer,
        // Allele-specific PCR
        String wtSpecificPrimer,
        String mutSpecificPrimer,
        String commonPrimer,
        // KASP
        String kaspGenomicSequence,
        // RFLP / dCAPS
        String restrictionEnzymeName,
        String restrictionEnzymeCatalog,
        Boolean enzymeCleavesWt,
        Boolean enzymeCleavesMut,
        String expectedWtDigest,
        String expectedMutDigest,
        // SSLP
        String sslpMarkerName,
        String sslpDistance,
        String sslpGenomicLocation,
        String sslpInducedBackground,
        String sslpOutcrossedBackground,
        String sslpInducedPcr,
        String sslpOutcrossedPcr,
        // Catch-all
        String additionalInfo,
        // Attachments (M4.3) — summary rows; full content is fetched via
        // GET /api/zirc/assays/attachments/{id}/content
        List<AssayFileDTO> attachments) {

    public static AssayDTO of(GenotypingAssay a) {
        List<AssayFileDTO> files = a.getFiles() == null ? List.of() :
                a.getFiles().stream()
                        .sorted(Comparator.comparing(
                                GenotypingAssayFile::getUploadedAt,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(AssayFileDTO::of)
                        .toList();
        return new AssayDTO(
                a.getId(),
                a.getMutation() == null ? null : a.getMutation().getId(),
                a.getSortOrder(),
                a.getAssayType(),
                a.getForwardPrimer(),
                a.getReversePrimer(),
                a.getExpectedWtPcr(),
                a.getExpectedMutPcr(),
                a.getSequencingPrimer(),
                a.getDcapsMismatchPrimer(),
                a.getWtSpecificPrimer(),
                a.getMutSpecificPrimer(),
                a.getCommonPrimer(),
                a.getKaspGenomicSequence(),
                a.getRestrictionEnzymeName(),
                a.getRestrictionEnzymeCatalog(),
                a.getEnzymeCleavesWt(),
                a.getEnzymeCleavesMut(),
                a.getExpectedWtDigest(),
                a.getExpectedMutDigest(),
                a.getSslpMarkerName(),
                a.getSslpDistance(),
                a.getSslpGenomicLocation(),
                a.getSslpInducedBackground(),
                a.getSslpOutcrossedBackground(),
                a.getSslpInducedPcr(),
                a.getSslpOutcrossedPcr(),
                a.getAdditionalInfo(),
                files);
    }
}
