package org.zfin.feature;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("protein_consequence_term")
public class ProteinConsequence extends MutationDetailControlledVocabularyTerm {
}
