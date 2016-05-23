package org.zfin.feature.repository;

import java.util.List;

public interface ControlledVocabularyRepository<T> {

    T getControlledVocabularyTerm(String oboID);

    List<T> getControlledVocabularyTermList();

}
