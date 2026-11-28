package org.zfin.gwt.curation.ui.feature;

/**
 * The four mutation detail widgets of one variant: the feature form's own set (a single-variant
 * feature) or one variant block of a Complex feature (VariantView, ZFIN-10623).
 */
public interface MutationDetailWidgets {

    GenomicMutationDetailView getGenomicMutationDetailView();

    MutationDetailDNAView getMutationDetailDnaView();

    MutationDetailTranscriptView getMutationDetailTranscriptView();

    MutationDetailProteinView getMutationDetailProteinView();
}
