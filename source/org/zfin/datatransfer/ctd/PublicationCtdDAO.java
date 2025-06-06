package org.zfin.datatransfer.ctd;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class PublicationCtdDAO {

    protected Session entityManager;

    public PublicationCtdDAO(Session entityManager) {
        this.entityManager = entityManager;
    }

    public PublicationCtd find(long id) {
        return entityManager.load(PublicationCtd.class, id);

    }

    public PublicationCtd find(String pubID) {
        String hql = """
            from PublicationCtd where
            publication.zdbID = :pubID
            """;
        Query<PublicationCtd> query = entityManager.createQuery(hql, PublicationCtd.class);
        query.setParameter("pubID", pubID);
        return query.uniqueResult();

    }

    public PublicationCtd persist(PublicationCtd publicationCtd) {
        entityManager.save(publicationCtd);
        return publicationCtd;
    }

    public void delete(PublicationCtd publicationCtd) {
        entityManager.delete(publicationCtd);
    }

    public List<PublicationCtd> findAll() {
        return forceRetrieveAll();
    }

    public List<PublicationCtd> forceRetrieveAll() {
        return entityManager.createQuery("from PublicationCtd ", PublicationCtd.class).getResultList();
    }

}
