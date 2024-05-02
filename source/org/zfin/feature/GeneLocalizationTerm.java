package org.zfin.feature;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("gene_localization_term")
public class GeneLocalizationTerm extends MutationDetailControlledVocabularyTerm {
}
