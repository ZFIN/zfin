package org.zfin.expression.repository;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.curation.dto.ExpressedTermDTO;
import org.zfin.expression.*;
import org.zfin.framework.HibernateUtil;
import static org.zfin.framework.HibernateUtil.currentSession;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.Phenotype;
import org.zfin.ontology.GoTerm;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Repository that is used for curation actions, such as dealing with expression experiments.
 */
public class HibernateExpressionRepository implements ExpressionRepository {

    private static Logger LOG = Logger.getLogger(HibernateExpressionRepository.class);

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
     *
     * @param experiment expression experiment
     */
    public void deleteExpressionExperiment(ExpressionExperiment experiment) {
        InfrastructureRepository infraRep = RepositoryFactory.getInfrastructureRepository();
        infraRep.deleteActiveDataByZdbID(experiment.getZdbID());
    }

    @SuppressWarnings("unchecked")
    public List<ExpressionExperiment> getExperimentsByGeneAndFish(String publicationID, String geneZdbID, String fishID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select experiment from ExpressionExperiment experiment";
        hql += "       left join experiment.marker as gene ";
        if (fishID != null) {
            hql += "       join experiment.genotypeExperiment.genotype geno";
        }
        hql += "     where experiment.publication.zdbID = :pubID ";
        if (geneZdbID != null)
            hql += "           and experiment.marker.zdbID = :geneID ";
        if (fishID != null) {
            hql += "           and geno.zdbID = :fishID ";
        }
        hql += "    order by gene.abbreviationOrder, " +
                "             experiment.genotypeExperiment.genotype.nickname, " +
                "             experiment.assay.displayOrder ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        if (geneZdbID != null)
            query.setString("geneID", geneZdbID);
        if (fishID != null)
            query.setString("fishID", fishID);

        return (List<ExpressionExperiment>) query.list();
    }

    /**
     * Retrieve an experiment figure stage for given pub, gene and fish.
     *
     * @param publicationID Publication
     * @param geneZdbID     gene
     * @param fishID        fish
     * @return list of experiment figure stages.
     */
    @SuppressWarnings("unchecked")
    public List<ExperimentFigureStage> getExperimentFigureStagesByGeneAndFish(String publicationID,
                                                                              String geneZdbID,
                                                                              String fishID,
                                                                              String figureID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select experiment, result, figure from ExpressionExperiment experiment, ExpressionResult result," +
                "Figure figure ";
        hql += "       left join experiment.marker as gene ";
        if (fishID != null) {
            hql += "       join experiment.genotypeExperiment.genotype geno";
        }
        hql += "     where experiment.publication.zdbID = :pubID ";
        if (geneZdbID != null)
            hql += "           and experiment.marker.zdbID = :geneID ";
        if (fishID != null)
            hql += "           and geno.zdbID = :fishID ";
        if (figureID != null)
            hql += "           and figure.zdbID = :figureID ";
        hql += " AND result.expressionExperiment = experiment ";
        hql += " AND figure member of result.figures ";
        hql += "    order by figure.orderingLabel, gene.abbreviationOrder, " +
                "             experiment.genotypeExperiment.genotype.nickname, " +
                "             experiment.assay.displayOrder, result.startStage.abbreviation ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        if (geneZdbID != null)
            query.setString("geneID", geneZdbID);
        if (fishID != null)
            query.setString("fishID", fishID);
        if (figureID != null)
            query.setString("figureID", figureID);

        List<Object[]> objects = query.list();
        if (objects == null)
            return null;

        return populateExperimentFigureStage(objects);
    }

    /**
     * Create a new figure annotation, i.e. expression_result record.
     * First check if such an annotation already exists. If so just add the figures to the
     * existing expression result.
     * Second check if the first result is 'unspeficied'. If so then update the record with
     * the new info.
     * <p/>
     * Ignore 'unspecified' term additions unless this is a first-time creation.
     *
     * @param result       figure annotation.
     * @param singleFigure Figure
     */
    public void createExpressionResult(ExpressionResult result, Figure singleFigure) {

        if (result == null)
            return;

        Session session = HibernateUtil.currentSession();
        ExpressionResult unspecifiedResult = getUnspecifiedExpressResult(result);

        // ignore unspecified addition if not the first creation.
        if (result.getAnatomyTerm().getName().equals(AnatomyItem.UNSPECIFIED))
            if (result.getZdbID() != null)
                return;
            else {
                // new unspecified record
                // if there is an 'unspecified' add figure to it.
                if (unspecifiedResult != null) {
                    unspecifiedResult.addFigure(singleFigure);
                } else {
                    // otherwise create a new one.
                    session.save(result);
                }
                return;
            }


        List<ExpressionResult> existingResult = checkForExpressionResultRecord(result);

        if (existingResult != null && existingResult.size() > 0) {
            if (existingResult.size() > 1)
                throw new RuntimeException("More than one expression result found");
            existingResult.get(0).addFigure(singleFigure);
            // check if unspecified exists
            // unspecified expression result record exists
            if (unspecifiedResult != null) {
                Set<Figure> figures = unspecifiedResult.getFigures();
                if (figures == null || figures.size() < 2) {
                    session.delete(unspecifiedResult);
                } else {
                    // has more than one figure associated
                    unspecifiedResult.removeFigure(singleFigure);
                }
            }
        } else {
            // no expression result record with given structures found
            // unspecified expression result record exists
            if (unspecifiedResult != null) {
                Set<Figure> figures = unspecifiedResult.getFigures();
                // has no figures associated
                if (figures == null || figures.size() == 0) {
                    session.delete(unspecifiedResult);
                    session.save(result);
                    result.getExpressionExperiment().addExpressionResult(result);
                } else if (unspecifiedResult.getFigures().size() == 1) {
                    // has one figure associated
                    // check if it is associated to the figure in question.
                    // if yes, remove it.
                    if (figures.contains(singleFigure)) {
                        session.delete(unspecifiedResult);
                        session.flush();
                    }
                    session.save(result);
                    result.getExpressionExperiment().addExpressionResult(result);
                } else {
                    // has more than one figure associated
                    unspecifiedResult.removeFigure(singleFigure);
                    session.save(result);
                    result.getExpressionExperiment().addExpressionResult(result);
                }
            } else{
                session.save(result);
                result.getExpressionExperiment().addExpressionResult(result);
            }
            runAntibodyAnatomyFastSearchUpdate(result);
        }
    }

    @SuppressWarnings("unchecked")
    private ExpressionResult getUnspecifiedExpressResult(ExpressionResult result) {
        AnatomyRepository anatRep = RepositoryFactory.getAnatomyRepository();
        AnatomyItem unspecified = anatRep.getAnatomyItem(AnatomyItem.UNSPECIFIED);
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ExpressionResult.class);
        criteria.add(Restrictions.eq("expressionExperiment", result.getExpressionExperiment()));
        criteria.add(Restrictions.eq("startStage", result.getStartStage()));
        criteria.add(Restrictions.eq("endStage", result.getEndStage()));
        criteria.add(Restrictions.eq("anatomyTerm", unspecified));
        criteria.add(Restrictions.eq("expressionFound", true));
        return (ExpressionResult) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    private List<ExpressionResult> checkForExpressionResultRecord(ExpressionResult result) {
        // first check if an expression result record already exists
        Session session = HibernateUtil.currentSession();
        Criteria criteria;
        if (result instanceof AnatomyExpressionResult) {
            criteria = session.createCriteria(AnatomyExpressionResult.class);
            AnatomyExpressionResult aoResult = (AnatomyExpressionResult) result;
            AnatomyItem subterm = aoResult.getSubterm();
            if (subterm == null)
                criteria.add(Restrictions.isNull("subterm"));
            else
                criteria.add(Restrictions.eq("subterm", aoResult.getSubterm()));
        } else {
            criteria = session.createCriteria(GoTermExpressionResult.class);
            GoTermExpressionResult goResult = (GoTermExpressionResult) result;
            GoTerm subterm = goResult.getSubterm();
            if (subterm == null)
                criteria.add(Restrictions.isNull("subterm"));
            else
                criteria.add(Restrictions.eq("subterm", subterm));
        }
        criteria.add(Restrictions.eq("expressionExperiment", result.getExpressionExperiment()));
        criteria.add(Restrictions.eq("startStage", result.getStartStage()));
        criteria.add(Restrictions.eq("endStage", result.getEndStage()));
        criteria.add(Restrictions.eq("anatomyTerm", result.getAnatomyTerm()));
        criteria.add(Restrictions.eq("expressionFound", result.isExpressionFound()));
        return (List<ExpressionResult>) criteria.list();
    }

    // run the script to update the fast search table for antibodies-anatomy
    public void runAntibodyAnatomyFastSearchUpdate(ExpressionResult result) {
        Session session = currentSession();
        Connection connection = session.connection();
        CallableStatement statement = null;
        String sql = "execute procedure add_ab_ao_fast_search(?)";
        try {
            statement = connection.prepareCall(sql);
            String zdbID = result.getZdbID();
            statement.setString(1, zdbID);
            statement.execute();
            LOG.info("Execute stored procedure: " + sql + " with the argument " + zdbID);
        } catch (SQLException e) {
            LOG.error("Could not run: " + sql, e);
        } finally {
            if (statement != null)
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOG.error(e);
                }
        }
    }

    /**
     * Delete a figure annotation, i.e. all expression result records.
     * ToDO:
     *
     * @param figureAnnotation experiment figure stage.
     */
    public void deleteFigureAnnotation(ExperimentFigureStage figureAnnotation) {
        if (figureAnnotation == null)
            throw new NullPointerException("No Figure Annotation provided");
        ExpressionExperiment experiment = figureAnnotation.getExpressionExperiment();
        if (experiment == null || experiment.getZdbID() == null)
            throw new NullPointerException("No expression experiment provided");
        Set<ExpressionResult> expressionResults = figureAnnotation.getExpressionResults();
        if (expressionResults == null || expressionResults.size() == 0)
            return;

        // delete all expression result records.
        Session session = HibernateUtil.currentSession();
        for (ExpressionResult result : expressionResults) {
            session.delete(result);
        }
    }

    /**
     * Retrieve an efs by experiment, figure, start and end stage id.
     *
     * @param experimentZdbID experiment
     * @param figureID        figure
     * @param startStageID    start
     * @param endStageID      end
     * @return efs object
     */
    @SuppressWarnings("unchecked")
    public ExperimentFigureStage getExperimentFigureStage(String experimentZdbID, String figureID, String startStageID, String endStageID) {
        if (StringUtils.isEmpty(experimentZdbID))
            return null;
        if (StringUtils.isEmpty(figureID))
            return null;
        if (StringUtils.isEmpty(startStageID))
            return null;
        if (StringUtils.isEmpty(endStageID))
            return null;
        validateFigureAnnotationKey(experimentZdbID, figureID, startStageID, endStageID);

        Session session = HibernateUtil.currentSession();

        String hql = "select experiment, result, figure from ExpressionExperiment experiment, ExpressionResult result," +
                "Figure figure ";
        hql += "     where experiment.zdbID = :experimentID ";
        hql += " AND result.expressionExperiment = experiment ";
        hql += " AND result.startStage.zdbID = :startID ";
        hql += " AND result.endStage.zdbID = :endID ";
        hql += " AND figure.zdbID = :figureID ";
        hql += " AND figure member of result.figures ";
        Query query = session.createQuery(hql);
        query.setString("experimentID", experimentZdbID);
        query.setString("startID", startStageID);
        query.setString("endID", endStageID);
        query.setString("figureID", figureID);

        List<Object[]> objects = query.list();
        if (objects == null)
            return null;

        List<ExperimentFigureStage> efses = populateExperimentFigureStage(objects);
        if (efses == null || efses.size() == 0)
            return null;

        if (efses.size() > 1)
            throw new RuntimeException("More than one Figure annotation found.");
        return efses.get(0);
    }

    /**
     * Retrieve all expression structures for a given publication, which is the same as the
     * structure pile.
     *
     * @param publicationID publication ID
     * @return list of expression structures.
     */
    @SuppressWarnings("unchecked")
    public List<ExpressionStructure> retrieveExpressionStructures(String publicationID) {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(ExpressionStructure.class);
        crit.add(Restrictions.eq("publication.zdbID", publicationID));
        Criteria superterm = crit.createCriteria("superterm");
        superterm.addOrder(Order.asc("nameOrder"));
        return (List<ExpressionStructure>) crit.list();
    }

    /**
     * Retrieve a single expression structure by ID.
     *
     * @param zdbID structure ID
     * @return expression structure
     */
    public ExpressionStructure getExpressionStructure(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(ExpressionStructure.class);
        crit.add(Restrictions.eq("zdbID", zdbID));
        return (ExpressionStructure) crit.uniqueResult();
    }

    /**
     * Delete a structure from the pile.
     *
     * @param structure expression structure
     */
    public void deleteExpressionStructure(ExpressionStructure structure) {
        Session session = HibernateUtil.currentSession();
        session.delete(structure);
    }

    /**
     * Delete an expression result record for a given figure.
     * If the result has more than one figure it only removes the figure-result association.
     * It removes the result object in case there is only one matching figure or if no figure is associated
     * and adds an 'unspecified' term to the efs.
     *
     * @param result expression result.
     * @param figure Figure
     */
    public void deleteExpressionResultPerFigure(ExpressionResult result, Figure figure) {
        if (result == null)
            return;

        Session session = HibernateUtil.currentSession();
        Set<Figure> figures = result.getFigures();
        if (figures == null)
            return;

        boolean lastResultOnExpression = true;
        for (ExpressionResult expResult : result.getExpressionExperiment().getExpressionResults()) {
            // filter out the ones that have the same stage info, i.e. the same efs
            if (result.getStartStage().equals(expResult.getStartStage()) && result.getEndStage().equals(expResult.getEndStage())) {
                if (!expResult.equals(result)) {
                    if (expResult.getFigures().contains(figure)) {
                        lastResultOnExpression = false;
                        break;
                    }
                }
            }
        }

        ExpressionResult unspecifiedResult = getUnspecifiedExpressResult(result);
        if (figures.size() > 1) {
            result.removeFigure(figure);
            if (lastResultOnExpression) {
                if (unspecifiedResult != null) {
                    unspecifiedResult.addFigure(figure);
                } else {
                    // add unspecified
                    createUnspecifiedExpressionResult(result, figure);
                }
            }
        } else if ((figures.size() == 1 && figures.iterator().next().equals(figure))) {
            if (!lastResultOnExpression) {
                session.delete(result);
                session.flush();
            } else {
                if (unspecifiedResult != null) {
                    session.delete(result);
                    session.flush();
                    unspecifiedResult.addFigure(figure);
                } else {
                    session.delete(result);
                    session.flush();
                    // add unspecified
                    createUnspecifiedExpressionResult(result, figure);
                }
            }
            session.refresh(result.getExpressionExperiment());
        }
    }

    private void createUnspecifiedExpressionResult(ExpressionResult result, Figure figure) {
        Session session = HibernateUtil.currentSession();
        AnatomyRepository anatRep = RepositoryFactory.getAnatomyRepository();
        AnatomyItem unspecifiedTerm = anatRep.getAnatomyItem(AnatomyItem.UNSPECIFIED);

        ExpressionResult unspecifiedResult = new ExpressionResult();
        unspecifiedResult.setExpressionExperiment(result.getExpressionExperiment());
        unspecifiedResult.setAnatomyTerm(unspecifiedTerm);
        unspecifiedResult.setStartStage(result.getStartStage());
        unspecifiedResult.setEndStage(result.getEndStage());
        unspecifiedResult.setExpressionFound(true);
        unspecifiedResult.addFigure(figure);
        session.save(unspecifiedResult);
    }

    public void createGOExpressionResult(GoTermExpressionResult newExpression, Figure figure) {
    }

    public void createAnatomyExpressionResult(AnatomyExpressionResult newExpression, Figure figure) {
    }

    /**
     * Retrieve a phenotype based on a given phenotype with
     * genox, start, end, pub, superterm = 'unspecified'
     *
     * @param pheno phenotype
     * @return phenotype
     */
    public Phenotype getUnspecifiedPhenotypeFromGenoxStagePub(Phenotype pheno) {
        Session session = HibernateUtil.currentSession();

        AnatomyRepository anatRep = RepositoryFactory.getAnatomyRepository();
        AnatomyItem unspecified = anatRep.getAnatomyItem(AnatomyItem.UNSPECIFIED);

        String hql = "select pheno from Phenotype pheno " +
                "     where pheno.genotypeExperiment = :genox" +
                "           and pheno.startStage = :start " +
                "           and pheno.endStage = :end " +
                "           and pheno.patoSuperTermzdbID = :anatomyTermID " +
                "           and pheno.patoSubTermzdbID is null " +
                "           and pheno.publication = :publication";

        Query query = session.createQuery(hql);
        query.setEntity("genox", pheno.getGenotypeExperiment());
        query.setEntity("start", pheno.getStartStage());
        query.setEntity("end", pheno.getEndStage());
        query.setString("anatomyTermID", unspecified.getZdbID());
        query.setEntity("publication", pheno.getPublication());

        @SuppressWarnings("unchecked")
        List<Phenotype> list = (List<Phenotype>) query.list();
        if (list == null || list.size() < 1)
            return null;
        else
            return list.get(0);
    }

    /**
     * Check if a pile structure already exists.
     * check for:
     * suberterm
     * subterm
     * publication ID
     *
     * @param expressedTerm term
     * @param publicationID publication
     * @return boolean
     */
    public boolean pileStructureExists(ExpressedTermDTO expressedTerm, String publicationID) {
        if (publicationID == null)
            throw new NullPointerException("No Publication provided.");
        String supertermName = expressedTerm.getSupertermName();
        if (supertermName == null)
            throw new NullPointerException("No superterm provided.");

        String subtermName = expressedTerm.getSubtermName();
        Session session = HibernateUtil.currentSession();
        Criteria crit;
        if (!StringUtils.isEmpty(subtermName)) {
            String subtermOntoloy = expressedTerm.getSubtermOntology();
            if (StringUtils.isEmpty(subtermOntoloy))
                throw new NullPointerException("No subterm ontology provided.");
            if (subtermOntoloy.equals(ExpressedTermDTO.Ontology.AO.toString())) {
                crit = session.createCriteria(AnatomyExpressionStructure.class);
            } else {
                crit = session.createCriteria(GOExpressionStructure.class);
            }
            Criteria subterm = crit.createCriteria("subterm");
            subterm.add(Restrictions.eq("name", subtermName));
        } else {
            crit = session.createCriteria(AnatomyExpressionStructure.class);
            crit.add(Restrictions.isNull("subterm"));
        }
        Criteria publication = crit.createCriteria("publication");
        publication.add(Restrictions.eq("zdbID", publicationID));
        Criteria superterm = crit.createCriteria("superterm");
        superterm.add(Restrictions.eq("name", supertermName));

        List list = crit.list();
        return list != null && list.size() > 0;
    }

    private void validateFigureAnnotationKey(String experimentZdbID, String figureID, String startStageID, String endStageID) {
        ActiveData data = new ActiveData();
        // these callse validate the keys according to zdb id syntax.
        data.setZdbID(experimentZdbID);
        data.setZdbID(figureID);
        data.setZdbID(startStageID);
        data.setZdbID(endStageID);
    }

    private List<ExperimentFigureStage> populateExperimentFigureStage
            (List<Object[]> objects) {
        List<ExperimentFigureStage> efses = new ArrayList<ExperimentFigureStage>();
        for (Object[] object : objects) {
            ExpressionExperiment exp = (ExpressionExperiment) object[0];
            ExpressionResult result = (ExpressionResult) object[1];
            Figure figure = (Figure) object[2];
            ExperimentFigureStage efs = new ExperimentFigureStage();
            efs.setExpressionExperiment(exp);
            efs.setFigure(figure);
            efs.addExpressionResult(result);
            if (!efses.contains(efs)) {
                efses.add(efs);
            } else {
                for (ExperimentFigureStage ef : efses) {
                    if (ef.equals(efs)) {
                        ef.addExpressionResult(result);
                    }
                }

            }

        }
        return efses;
    }
}
