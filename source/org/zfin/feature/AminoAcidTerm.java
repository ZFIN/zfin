package org.zfin.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("amino_acid_term")
public class AminoAcidTerm extends MutationDetailControlledVocabularyTerm {
}
