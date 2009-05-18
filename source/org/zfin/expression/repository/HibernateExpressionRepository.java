package org.zfin.expression.repository;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionAssay;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.framework.HibernateUtil;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.repository.RepositoryFactory;

/**
 * Repository that is used for curation actions, such as dealing with expression experiments.
 */
public class HibernateExpressionRepository implements ExpressionRepository {

    @SuppressWarnings("unchecked")
    public ExpressionExperiment getExpressionExperiment(String experimentID) {
        Session session = HibernateUtil.currentSession();
        return (ExpressionExperiment) session.get(ExpressionExperiment.class, experimentID);
    }

    /**
     * Retrieve an assay by name.
     *
     * @param assay assay name
     * @return expression Assay
     */
    @SuppressWarnings("unchecked")
    public ExpressionAssay getAssayByName(String assay) {
        Session session = HibernateUtil.currentSession();
        return (ExpressionAssay) session.get(ExpressionAssay.class, assay);
    }

    /**
     * Retrieve db link by id.
     *
     * @param genbankID genbank id
     * @return MarkerDBLink
     */
    @SuppressWarnings("unchecked")
    public MarkerDBLink getMarkDBLink(String genbankID) {
        Session session = HibernateUtil.currentSession();
        return (MarkerDBLink) session.get(MarkerDBLink.class, genbankID);
    }

    /**
     * Retrieve GenotypeExperiment by Experiment ID
     *
     * @param experimentID id
     * @return GenotypeExperiment
     */
    public GenotypeExperiment getGenotypeExperimentByExperimentIDAndGenotype(String experimentID, String genotypeID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GenotypeExperiment.class);
        criteria.add(Restrictions.eq("experiment.zdbID", experimentID));
        criteria.add(Restrictions.eq("genotype.zdbID", genotypeID));
        return (GenotypeExperiment) criteria.uniqueResult();
    }

    /**
     * Create a new genotype experiment for given experiment and genotype.
     *
     * @param experiment genotype experiment
     */
    public void createGenoteypExperiment(GenotypeExperiment experiment) {
        Session session = HibernateUtil.currentSession();
        session.save(experiment);
    }

    /**
     * Retrieve experiment by id.
     *
     * @param experimentID id
     * @return Experiment
     */
    public Experiment getExperimentByID(String experimentID) {
        Session session = HibernateUtil.currentSession();
        return (Experiment) session.get(Experiment.class, experimentID);
    }

    /**
     * Retrieve Genotype by PK.
     *
     * @param genotypeID id
     * @return genotype
     */
    public Genotype getGenotypeByID(String genotypeID) {
        Session session = HibernateUtil.currentSession();
        return (Genotype) session.get(Genotype.class, genotypeID);
    }

    /**
     * Convenience method to create a genotype experiment from
     * experiment ID and genotype ID
     *
     * @param experimentID id
     * @param genotypeID   id
     */
    public GenotypeExperiment createGenoteypExperiment(String experimentID, String genotypeID) {
        Experiment experiment = getExperimentByID(experimentID);
        Genotype geno = getGenotypeByID(genotypeID);
        GenotypeExperiment genox = new GenotypeExperiment();
        genox.setExperiment(experiment);
        genox.setGenotype(geno);
        createGenoteypExperiment(genox);
        return genox;
    }

    /**
     * Create a new expression Experiment.
     *
     * @param expressionExperiment expression experiment
     */
    public void createExpressionExperiment(ExpressionExperiment expressionExperiment) {
        Session session = HibernateUtil.currentSession();
        session.save(expressionExperiment);
    }

    /**
     * Remove an existing expression experiment and all objects that it is composed of.
     * Note: It delegates the call to removing the ActiveData record (OCD). 
     * @param experiment expression experiment
     */
    public void deleteExpressionExperiment(ExpressionExperiment experiment) {
        InfrastructureRepository infraRep = RepositoryFactory.getInfrastructureRepository();
        infraRep.deleteActiveDataByZdbID(experiment.getZdbID());
    }
}
