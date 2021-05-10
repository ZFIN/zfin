package org.zfin.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("gene_localization_term")
public class GeneLocalizationTerm extends MutationDetailControlledVocabularyTerm {
}
