package org.zfin.feature;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("transcript_consequence_term")
public class TranscriptConsequence extends MutationDetailControlledVocabularyTerm{
}
