package org.zfin.zirc.repository;

import org.zfin.profile.Person;
import org.zfin.zirc.entity.GenotypingAssay;
import org.zfin.zirc.entity.LineSubmission;
import org.zfin.zirc.entity.Mutation;

import java.util.List;

public interface ZircSubmissionRepository {

    List<LineSubmission> getLineSubmissions();

    LineSubmission getLineSubmission(String zdbID);

    Mutation getMutation(Long mutationId);

    GenotypingAssay getAssay(Long assayId);

    org.zfin.zirc.entity.GenotypingAssayFile getAssayFile(Long fileId);

    org.zfin.zirc.entity.LinkedFeature getLinkedFeature(String submissionId, Long mutationAId, Long mutationBId);

    org.zfin.zirc.entity.Gene getGene(Long geneId);

    org.zfin.zirc.entity.Lesion getLesion(Long lesionId);

    org.zfin.zirc.entity.Phenotype getPhenotype(Long phenotypeId);

    Person getPerson(String personZdbID);

    Person getPersonReference(String personZdbID);

    void save(Object entity);

    void delete(Object entity);

}
