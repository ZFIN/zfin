package org.zfin.feature;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("transcript_consequence_term")
public class TranscriptConsequence extends MutationDetailControlledVocabularyTerm{
}
