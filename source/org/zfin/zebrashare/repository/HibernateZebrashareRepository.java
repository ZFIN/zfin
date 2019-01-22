package org.zfin.zebrashare.repository;

import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Lab;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
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

}
