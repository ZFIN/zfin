package org.zfin.feature.presentation;

import lombok.Getter;
import lombok.Setter;
import org.zfin.feature.FeatureGenomicMutationDetail;
import org.zfin.feature.Variant;
import org.zfin.feature.VariantNote;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.mapping.FeatureLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One variant of a multi-variant (Complex) feature, shown on the feature page as its own block
 * with everything a single-variant feature shows in its Variants section (ZFIN-10622): type,
 * location, nucleotide change, notes, and the DNA/cDNA, transcript and protein effects.
 */
@Getter
@Setter
public class VariantDisplay {

    private final int variantNumber;
    private final String variantType;
    private final String nucleotideChange;
    private final List<VariantNote> variantNotes;
    private FeatureLocation location;
    private MutationDetailsPresentation mutationDetails;
    private List<PublicationAttribution> dnaChangeAttributions;
    private List<PublicationAttribution> transcriptConsequenceAttributions;
    private List<PublicationAttribution> proteinConsequenceAttributions;

    public VariantDisplay(Variant variant) {
        this.variantNumber = variant.getVariantNumber();
        this.variantType = variant.getVariantType() == null ? null : variant.getVariantType().getDisplay();
        FeatureGenomicMutationDetail genomicDetail = variant.getFeatureGenomicMutationDetail();
        this.nucleotideChange = nucleotideChange(genomicDetail);
        this.variantNotes = genomicDetail == null || genomicDetail.getExternalNotes() == null
            ? Collections.emptyList()
            : new ArrayList<>(genomicDetail.getExternalNotes());
    }

    /**
     * Reference/variation, as the flanking-sequence variation is shown for single-variant
     * features (e.g. C/T, ATGCGCC/-): a missing side is a deletion or insertion, shown as '-'.
     */
    private static String nucleotideChange(FeatureGenomicMutationDetail detail) {
        if (detail == null || (detail.getFgmdSeqRef() == null && detail.getFgmdSeqVar() == null)) {
            return null;
        }
        return orDash(detail.getFgmdSeqRef()) + "/" + orDash(detail.getFgmdSeqVar());
    }

    private static String orDash(String sequence) {
        return sequence == null || sequence.isBlank() ? "-" : sequence;
    }
}
