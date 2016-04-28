package org.zfin.feature.repository;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.zfin.framework.HibernateUtil;

import java.util.List;

public class HibernateControlledVocabularyRepository<T> implements ControlledVocabularyRepository<T> {

    private Class<T> clazz;

    public HibernateControlledVocabularyRepository(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T getControlledVocabularyTerm(String oboID) {

        Criteria criteria = HibernateUtil.currentSession().createCriteria(clazz);
        Criteria termCriteria = criteria.createCriteria("term");
        return (T) termCriteria.add(Restrictions.eq("oboID", oboID)).uniqueResult();
    }

    @Override
    public List<T> getControlledVocabularyTermList() {
        Criteria criteria = HibernateUtil.currentSession().createCriteria(clazz);
        criteria.addOrder(Order.asc("order"));
        return (List<T>) criteria.list();
    }
}
