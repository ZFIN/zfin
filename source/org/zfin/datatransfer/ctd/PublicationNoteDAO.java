package org.zfin.datatransfer.ctd;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.zfin.curation.PublicationNote;

import java.util.List;

public class PublicationNoteDAO {

    protected Session entityManager;

    public PublicationNoteDAO(Session entityManager) {
        this.entityManager = entityManager;
    }

    public PublicationNote find(String pubID) {
        String hql = """
            from PublicationNote where
            publication.zdbID = :pubID
            """;
        Query<PublicationNote> query = entityManager.createQuery(hql, PublicationNote.class);
        query.setParameter("pubID", pubID);
        return query.uniqueResult();

    }

    public PublicationNote persist(PublicationNote publicationNote) {
        entityManager.save(publicationNote);
        return publicationNote;
    }

    public void delete(PublicationNote publicationCtd) {
        entityManager.delete(publicationCtd);
    }

    public List<PublicationNote> findAll() {
        return forceRetrieveAll();
    }

    public List<PublicationNote> forceRetrieveAll() {
        return entityManager.createQuery("from PublicationNote ", PublicationNote.class).getResultList();
    }

}
