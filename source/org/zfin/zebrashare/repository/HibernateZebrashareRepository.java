package org.zfin.zebrashare.repository;

import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Lab;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.zebrashare.ZebrashareSubmissionMetadata;

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

}
