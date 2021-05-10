package org.zfin.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("protein_consequence_term")
public class ProteinConsequence extends MutationDetailControlledVocabularyTerm {
}
