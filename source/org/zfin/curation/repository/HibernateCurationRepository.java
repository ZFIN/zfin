package org.zfin.curation.repository;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.zfin.curation.Curation;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.Date;
import java.util.List;

@Repository
public class HibernateCurationRepository implements CurationRepository {

    public List<Curation> getCurationForPub(Publication pub) {
        String hql = "from Curation c " +
                "where c.publication = :pub " +
                "and c.topic != :linkedAuthors";

        return HibernateUtil.currentSession()
                .createQuery(hql)
                .setParameter("pub", pub)
                .setParameter("linkedAuthors", Curation.Topic.LINKED_AUTHORS)
                .list();
    }

    public List<Curation> getOpenCurationTopics(String pubZdbID) {
        String hql = "from Curation c" +
                " where c.publication.zdbID = :pubID" +
                " and c.openedDate is not null " +
                " and c.closedDate is null " +
                " and c.topic != :linkedAuthors";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("pubID", pubZdbID);
        query.setParameter("linkedAuthors", Curation.Topic.LINKED_AUTHORS);
        return query.list();
    }

    public void closeCurationTopics(Publication pub, Person curator) {
        for (Curation.Topic topic : Curation.Topic.values()) {
            if (topic == Curation.Topic.LINKED_AUTHORS) {
                continue;
            }

            Date now = new Date();
            Session session = HibernateUtil.currentSession();
            List<Curation> curationList = (List<Curation>) session
                    .createCriteria(Curation.class)
                    .add(Restrictions.eq("publication", pub))
                    .add(Restrictions.eq("topic", topic))
                    .list();
            if (CollectionUtils.isNotEmpty(curationList)) {
                for (Curation curation : curationList) {
                    if (curation.getClosedDate() == null) {
                        // existing curation topics which haven't been closed yet need to be closed
                        if (curation.getOpenedDate() == null) {
                            // this is a topic in the "new" state -- not opened or closed. Need to set the
                            // curator, opened and closed date.
                            curation.setCurator(curator);
                            curation.setOpenedDate(now);
                        }
                        curation.setClosedDate(now);
                    }
                }
            } else {
                // curation topic hasn't been created, so make one with no data found and closed
                Curation curation = new Curation();
                curation.setTopic(topic);
                curation.setPublication(pub);
                curation.setCurator(curator);
                curation.setEntryDate(new Date());
                curation.setDataFound(false);
                curation.setEntryDate(now);
                curation.setOpenedDate(now);
                curation.setClosedDate(now);
                session.save(curation);
            }
        }
    }

    public void resetCurationTopics(Publication publication) {
        Session session = HibernateUtil.currentSession();
        for (Curation curation : getCurationForPub(publication)) {
            curation.setOpenedDate(null);
            curation.setClosedDate(null);
            curation.setDataFound(false);
            session.save(curation);
        }
    }
}
