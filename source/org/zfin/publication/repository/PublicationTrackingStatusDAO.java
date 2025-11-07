package org.zfin.publication.repository;

import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import org.zfin.framework.dao.BaseSQLDAO;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.sequence.gff.Assembly;

public class PublicationTrackingStatusDAO extends BaseSQLDAO<PublicationTrackingStatus> {

    public PublicationTrackingStatusDAO(Session entityManager) {
        super(PublicationTrackingStatus.class);
        this.entityManager = entityManager;
    }

    public PublicationTrackingStatusDAO() {
        super(PublicationTrackingStatus.class);
    }

    public PublicationTrackingStatus findByStatus(PublicationTrackingStatus.Type type) {
        String hql = """
            from PublicationTrackingStatus
            where type = :type
            """;
        TypedQuery<PublicationTrackingStatus> query = entityManager.createQuery(hql, PublicationTrackingStatus.class);
        query.setParameter("type", type);
        return query.getResultList().get(0);
    }
}
