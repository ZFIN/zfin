package org.zfin.feature;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.io.Serializable;

@Entity
@DiscriminatorValue("dna_mutation_term")
public class DnaMutationTerm extends MutationDetailControlledVocabularyTerm implements Serializable {
}
