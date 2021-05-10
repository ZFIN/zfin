package org.zfin.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
@DiscriminatorValue("dna_mutation_term")
public class DnaMutationTerm extends MutationDetailControlledVocabularyTerm implements Serializable {
}
