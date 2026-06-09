package org.zfin.zirc.repository;

import org.springframework.stereotype.Repository;
import org.zfin.framework.HibernateUtil;
import org.zfin.profile.Person;
import org.zfin.zirc.entity.Gene;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.entity.GenotypingAssayFile;
import org.zfin.zirc.entity.Lesion;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.LinkedFeature;
import org.zfin.zirc.entity.LinkedFeatureId;
import org.zfin.zirc.entity.Mutation;
import org.zfin.zirc.entity.Phenotype;

import java.util.List;

@Repository
public class HibernateZircSubmissionRepository implements ZircSubmissionRepository {

    @Override
    public List<LineSubmission> getLineSubmissions() {
        return HibernateUtil.currentSession()
                .createQuery("from ZircLineSubmission order by createdAt desc", LineSubmission.class)
                .list();
    }

    @Override
    public LineSubmission getLineSubmission(String zdbID) {
        return HibernateUtil.currentSession().get(LineSubmission.class, zdbID);
    }

    @Override
    public Mutation getMutation(Long mutationId) {
        return HibernateUtil.currentSession().get(Mutation.class, mutationId);
    }

    @Override
    public GenotypingAssay getAssay(Long assayId) {
        return HibernateUtil.currentSession().get(GenotypingAssay.class, assayId);
    }

    @Override
    public GenotypingAssayFile getAssayFile(Long fileId) {
        return HibernateUtil.currentSession().get(GenotypingAssayFile.class, fileId);
    }

    @Override
    public LinkedFeature getLinkedFeature(String submissionId, Long mutationAId, Long mutationBId) {
        return HibernateUtil.currentSession().get(LinkedFeature.class,
                new LinkedFeatureId(submissionId, mutationAId, mutationBId));
    }

    @Override
    public Gene getGene(Long geneId) {
        return HibernateUtil.currentSession().get(Gene.class, geneId);
    }

    @Override
    public Lesion getLesion(Long lesionId) {
        return HibernateUtil.currentSession().get(Lesion.class, lesionId);
    }

    @Override
    public Phenotype getPhenotype(Long phenotypeId) {
        return HibernateUtil.currentSession().get(Phenotype.class, phenotypeId);
    }

    @Override
    public Person getPerson(String personZdbID) {
        return HibernateUtil.currentSession().get(Person.class, personZdbID);
    }

    @Override
    public Person getPersonReference(String personZdbID) {
        return HibernateUtil.currentSession().getReference(Person.class, personZdbID);
    }

    @Override
    public void save(Object entity) {
        HibernateUtil.currentSession().persist(entity);
    }

    @Override
    public void delete(Object entity) {
        HibernateUtil.currentSession().remove(entity);
    }

}
