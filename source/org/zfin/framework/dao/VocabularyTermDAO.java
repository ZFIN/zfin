package org.zfin.framework.dao;

import org.hibernate.Session;
import org.zfin.framework.VocabularyTerm;

public class VocabularyTermDAO extends BaseSQLDAO<VocabularyTerm> {

    protected Session entityManager;

    public VocabularyTermDAO(Session entityManager) {
        super(VocabularyTerm.class);
        this.entityManager = entityManager;
    }

    public VocabularyTermDAO() {
        super(VocabularyTerm.class);
    }

}
