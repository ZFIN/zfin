package org.zfin.framework.services;

import org.zfin.framework.Vocabulary;
import org.zfin.framework.VocabularyTerm;
import org.zfin.framework.dao.VocabularyDAO;
import org.zfin.framework.dao.VocabularyTermDAO;

public class VocabularyService {

    VocabularyDAO dao = new VocabularyDAO();
    VocabularyTermDAO termDao = new VocabularyTermDAO();

    public VocabularyTerm getVocabularyTerm(VocabularyEnum vocabularyEnum, String vocabularyTermName) {
        Vocabulary vocabulary = dao.findByField("name", vocabularyEnum.getName()).getSingleResult();
        VocabularyTerm vocabularyTerm = termDao.findByField("name", vocabularyTermName).getSingleResult();
        return vocabularyTerm;
    }

}

