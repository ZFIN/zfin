package org.zfin.zebrashare.repository;

import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Lab;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.zebrashare.FeatureCommunityContribution;
import org.zfin.zebrashare.ZebrashareEditor;
import org.zfin.zebrashare.ZebrashareSubmissionMetadata;

import java.util.List;

@Repository
public class HibernateZebrashareRepository implements ZebrashareRepository {

    @Override
    public ZebrashareSubmissionMetadata addZebrashareSubmissionMetadata(Publication publication,
                                                                        Person submitterUser,
                                                                        Lab labOfOrigin,
                                                                        String submitterName,
                                                                        String submitterEmail) {
        Transaction tx = HibernateUtil.createTransaction();

        ZebrashareSubmissionMetadata metadata = new ZebrashareSubmissionMetadata();
        metadata.setPublication(publication);
        metadata.setSubmitter(submitterUser);
        metadata.setLabOfOrigin(labOfOrigin);
        metadata.setSubmitterName(submitterName);
        metadata.setSubmitterEmail(submitterEmail);
        HibernateUtil.currentSession().save(metadata);

        tx.commit();
        return metadata;
    }

    @Override
    public ZebrashareSubmissionMetadata getZebraShareSubmissionMetadataForPublication(Publication publication) {
        return (ZebrashareSubmissionMetadata) HibernateUtil.currentSession()
                .createCriteria(ZebrashareSubmissionMetadata.class)
                .add(Restrictions.eq("publication", publication))
                .uniqueResult();
    }

    @Override
    public List<ZebrashareEditor> getZebraShareEditorsForPublication(Publication publication) {
        return (List<ZebrashareEditor>) HibernateUtil.currentSession()
                .createCriteria(ZebrashareEditor.class)
                .add(Restrictions.eq("publication", publication))
                .list();
    }

    @Override
    public Publication getZebraSharePublicationForFeature(Feature feature) {
        String hql = "" +
                "select pub " +
                "from Feature as feature " +
                "inner join feature.publications as pubattrib " +
                "inner join pubattrib.publication as pub " +
                "inner join pub.journal as journal " +
                "where feature = :feature " +
                "and journal.zdbID = 'ZDB-JRNL-181119-2' ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("feature", feature);
        return (Publication) query.uniqueResult();
    }

    public List<Feature> getZebraShareFeatureForPub(String jrnlZDB) {
        String hql = "" +
                "select feature " +
                "from Feature as feature " +
                "inner join feature.publications as pubattrib " +
                "inner join pubattrib.publication as pub " +
                "inner join pub.journal as journal " +
                "journal.zdbID = :jrnlZDB ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("jrnlZDB", "ZDB-JRNL-181119-2");
        return  query.list();
    }

    @Override
    public FeatureCommunityContribution getLatestCommunityContribution(Feature feature) {
        return (FeatureCommunityContribution) HibernateUtil.currentSession()
                .createCriteria(FeatureCommunityContribution.class)
                .add(Restrictions.eq("feature", feature))
                .addOrder(Order.desc("date"))
                .setMaxResults(1)
                .uniqueResult();
    }

    @Override
    public boolean isAuthorizedSubmitter(Feature feature, Person person) {
        String hql = "" +
                "select 1 " +
                "from Feature as feature, ZebrashareEditor editor " +
                "inner join feature.publications as pubattrib " +
                "inner join pubattrib.publication as pub " +
                "where feature = :feature " +
                "and editor.person = :person " +
                "and editor.publication = pub ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("person", person);
        query.setParameter("feature", feature);
        return query.uniqueResult() != null;
    }

}
