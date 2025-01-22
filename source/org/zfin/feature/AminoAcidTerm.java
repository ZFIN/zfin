package org.zfin.feature;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("amino_acid_term")
public class AminoAcidTerm extends MutationDetailControlledVocabularyTerm {
}
