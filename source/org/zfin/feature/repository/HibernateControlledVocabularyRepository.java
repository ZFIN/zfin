package org.zfin.feature.repository;

import org.hibernate.query.Query;
import org.zfin.framework.HibernateUtil;

import java.util.List;

public class HibernateControlledVocabularyRepository<T> implements ControlledVocabularyRepository<T> {

    private final Class<T> clazz;

    public HibernateControlledVocabularyRepository(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T getControlledVocabularyTerm(String oboID) {

        Query<T> query = HibernateUtil.currentSession().createQuery("from " + clazz.getSimpleName() + " where term.oboID = :oboID", clazz);
        return query.setParameter("oboID", oboID).uniqueResult();
    }

    @Override
    public List<T> getControlledVocabularyTermList() {
        Query<T> query = HibernateUtil.currentSession().createQuery("from " + clazz.getSimpleName() + " order by order", clazz);
        return query.list();
    }
}
