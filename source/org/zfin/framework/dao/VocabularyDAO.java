package org.zfin.framework.dao;

import org.hibernate.Session;
import org.zfin.framework.Vocabulary;

public class VocabularyDAO extends BaseSQLDAO<Vocabulary> {

    protected Session entityManager;

    public VocabularyDAO(Session entityManager) {
        super(Vocabulary.class);
        this.entityManager = entityManager;
    }
    public VocabularyDAO() {
        super(Vocabulary.class);
    }

}
