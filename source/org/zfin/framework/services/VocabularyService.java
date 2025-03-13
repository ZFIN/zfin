package org.zfin.framework.services;

import org.zfin.framework.Vocabulary;
import org.zfin.framework.VocabularyTerm;
import org.zfin.framework.api.SearchResponse;
import org.zfin.framework.dao.VocabularyDAO;
import org.zfin.framework.dao.VocabularyTermDAO;

import java.util.HashMap;
import java.util.Map;

public class VocabularyService {

    VocabularyDAO vocabularyDao = new VocabularyDAO();
    VocabularyTermDAO vocabularyTermDao = new VocabularyTermDAO();

    public VocabularyTerm getVocabularyTerm(VocabularyEnum vocabularyEnum, String vocabularyTermName) {
        VocabularyTerm vocabularyTerm = vocabularyTermDao.findByField("name", vocabularyTermName).getSingleResult();
        return vocabularyTerm;
    }

    // <vocabularyName, Map<vocabularyTermName, term>>
    private Map<String, Map<String, VocabularyTerm>> vocabCacheMap = new HashMap<>();
    public VocabularyTerm getVocabularyTerm(String vocabulary, String vocabularyTerm) {

        Map<String, VocabularyTerm> vocabMap = vocabCacheMap.computeIfAbsent(vocabulary, k -> new HashMap<>());
        if(vocabMap.entrySet().isEmpty() || vocabMap.get(vocabularyTerm) == null) {
            Vocabulary vocab = vocabularyDao.findByField("name", vocabulary).getSingleResult();
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", vocabularyTerm);
            map.put("vocabulary.id", vocab.getId());
            SearchResponse<VocabularyTerm> term = vocabularyTermDao.findByParams(null, map);
            vocabMap.put(vocabularyTerm,term.getSingleResult() );
            vocabCacheMap.put(vocabulary, vocabMap);
        }
        return vocabMap.get(vocabularyTerm);
    }

}

