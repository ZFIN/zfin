package org.zfin.expression.repository;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.*;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Clone;
import org.zfin.marker.Gene;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.mutant.Phenotype;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.ontology.Term;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 * Repository that is used for curation actions, such as dealing with expression experiments.
 */
public class HibernateExpressionRepository implements ExpressionRepository {

    private Logger logger = Logger.getLogger(HibernateExpressionRepository.class);

    public ExpressionStageAnatomyContainer getExpressionStages(Gene gene) {
        Session session = HibernateUtil.currentSession();

        //query in expressions.hbm.xml
        Query query = session.getNamedQuery("stageanatomyfigure");
        query.setParameter("geneZdbID", gene.getZdbID());
        query.setParameter("unknown", Term.UNSPECIFIED);
        query.setParameter("unspecified", DevelopmentStage.UNKNOWN);

        Iterator stagesAndAnatomy = query.list().iterator();
        ExpressionStageAnatomyContainer xsac = new ExpressionStageAnatomyContainer();

        //the container object will handle duplication produced in the query.
        while (stagesAndAnatomy.hasNext()) {
            Object[] tuple = (Object[]) stagesAndAnatomy.next();

            DevelopmentStage stage = (DevelopmentStage) tuple[0];
            Term anat = (Term) tuple[1];
            Figure fig = (Figure) tuple[2];

            xsac.add(stage, anat, fig);

        }


        return xsac;

    }

    // todo: I think that these are wrong

    public int getExpressionPubCount(Marker marker) {
        Session session = currentSession();
        String hql = "" +
                "select count(ee.publication) " +
                " from ExpressionResult er join er.expressionExperiment as ee " +
                "where ee.probe=:markerZdbID " +
                " ";
        Query query = session.createQuery(hql);
        query.setString("markerZdbID", marker.getZdbID());
        Number number = (Number) query.uniqueResult();
        logger.fatal("do not use this method getExpressionPubCount until fixed");
        return number.intValue();
    }

    // todo: I think that these are wrong

    public int getExpressionFigureCount(Marker marker) {
        Session session = currentSession();
        String hql = "" +
                "select count(f) " +
                " from Figure f join f.expressionResults er " +
                "where er.expressionExperiment.probe=:markerZdbID " +
                "";
        Query query = session.createQuery(hql);
        query.setString("markerZdbID", marker.getZdbID());
        Number number = (Number) query.uniqueResult();
        logger.fatal("do not use this method getExpressionFigureCount until fixed");
        return number.intValue();
    }

    public int getImagesFromPubAndClone(Publication publication, Clone clone) {
        String hql = "" +
                " select count( distinct i) " +
                " from Image i join i.figure f" +
                " join f.expressionResults er join er.expressionExperiment ee join ee.probe c join ee.publication p where " +
                " c.zdbID = :markerZdbID and " +
                " p.zdbID  = :publicationZdbID ";
        Session session = currentSession();
        Query query1 = session.createQuery(hql);
        query1.setString("markerZdbID", clone.getZdbID());
        query1.setString("publicationZdbID", publication.getZdbID());
        return ((Number) query1.uniqueResult()).intValue();
    }

    public List getDirectlySubmittedExpressionSummaries(Marker marker) {

        String hql1 = "" +
                "	 select count(distinct figure )," +
                "		publication.zdbID , clone.zdbID " +
                "           from Figure figure join figure.expressionResults  er " +
                "                join er.expressionExperiment ee " +
                "                join ee.publication publication " +
                "                join ee.probe clone " +
                "           where clone.zdbID = :markerZdbID " +
                "           and publication.type  = :unpublished " +
                "            group by clone.zdbID , publication.zdbID  ";

        Session session = currentSession();
        Query query1 = session.createQuery(hql1);
        query1.setString("markerZdbID", marker.getZdbID());
        query1.setString("unpublished", "Unpublished");
        List pubList = query1.list();
        return pubList;
    }


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

        String hql = "select result, figure from ExpressionResult result, Figure figure";
        hql += "       left join fetch result.expressionExperiment ";
        hql += "       left join fetch result.startStage ";
        hql += "       left join fetch result.endStage ";
        hql += "       left join fetch result.expressionExperiment.antibody ";
        hql += "       left join fetch result.expressionExperiment.markerDBLink ";
        hql += "       left join fetch result.figures ";
        hql += "       left join fetch result.expressionExperiment.genotypeExperiment ";
        hql += "       left join fetch result.expressionExperiment.genotypeExperiment.genotype ";
        hql += "       left join result.expressionExperiment.gene as gene ";
        if (fishID != null) {
            hql += "       join result.expressionExperiment.genotypeExperiment.genotype geno";
        }
        hql += "     where result.expressionExperiment.publication.zdbID = :pubID ";
        if (geneZdbID != null)
            hql += "           and result.expressionExperiment.gene.zdbID = :geneID ";
        if (fishID != null)
            hql += "           and geno.zdbID = :fishID ";
        if (figureID != null)
            hql += "           and figure.zdbID = :figureID ";
        hql += " AND figure member of result.figures ";
        hql += "    order by figure.orderingLabel, gene.abbreviationOrder, " +
                "             result.expressionExperiment.genotypeExperiment.genotype.nickname, " +
                "             result.expressionExperiment.assay.displayOrder, result.startStage.abbreviation ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        if (geneZdbID != null)
            query.setString("geneID", geneZdbID);
        if (fishID != null)
            query.setString("fishID", fishID);
        if (figureID != null)
            query.setString("figureID", figureID);

        //query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
        List<Object[]> objects = query.list();
        if (objects == null)
            return null;

        return populateExperimentFigureStage(objects);
    }

    /**
     * Create a new figure annotation, i.e. expression_result record.
     * First check if such an annotation already exists. If so just add the figures to the
     * existing expression result.
     * Second check if the first result is 'unspecified'. If so then update the record with
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
        if (result.getSuperterm().getTermName().equals(Term.UNSPECIFIED))
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
                if (figures == null || figures.isEmpty()) {
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
            } else {
                session.save(result);
                result.getExpressionExperiment().addExpressionResult(result);
            }
            runAntibodyAnatomyFastSearchUpdate(result);
        }
    }

    @SuppressWarnings("unchecked")
    private ExpressionResult getUnspecifiedExpressResult(ExpressionResult result) {
        Term unspecified = OntologyManager.getInstance().getTermByName(Ontology.ANATOMY, Term.UNSPECIFIED);
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ExpressionResult.class);
        criteria.add(Restrictions.eq("expressionExperiment", result.getExpressionExperiment()));
        criteria.add(Restrictions.eq("startStage", result.getStartStage()));
        criteria.add(Restrictions.eq("endStage", result.getEndStage()));
        criteria.add(Restrictions.eq("superterm", unspecified));
        criteria.add(Restrictions.eq("expressionFound", true));
        return (ExpressionResult) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    private List<ExpressionResult> checkForExpressionResultRecord(ExpressionResult result) {
        // first check if an expression result record already exists
        Session session = HibernateUtil.currentSession();
        Criteria criteria;
        criteria = session.createCriteria(ExpressionResult.class);
        Term subterm = result.getSubterm();
        if (subterm == null)
            criteria.add(Restrictions.isNull("subterm"));
        else
            criteria.add(Restrictions.eq("subterm", result.getSubterm()));
        criteria.add(Restrictions.eq("expressionExperiment", result.getExpressionExperiment()));
        criteria.add(Restrictions.eq("startStage", result.getStartStage()));
        criteria.add(Restrictions.eq("endStage", result.getEndStage()));
        criteria.add(Restrictions.eq("superterm", result.getSuperterm()));
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

        String hql = "select result, figure from ExpressionResult result, Figure figure ";
        hql += "       left join fetch result.expressionExperiment as experiment ";
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
        superterm.addOrder(Order.asc("termName"));
        crit.setFetchMode("superterm", FetchMode.JOIN);
        crit.setFetchMode("superterm.start", FetchMode.JOIN);
        crit.setFetchMode("superterm.end", FetchMode.JOIN);
        crit.setFetchMode("person", FetchMode.JOIN);
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
        Term unspecifiedTerm = OntologyManager.getInstance().getTermByName(Ontology.ANATOMY, Term.UNSPECIFIED);

        ExpressionResult unspecifiedResult = new ExpressionResult();
        unspecifiedResult.setExpressionExperiment(result.getExpressionExperiment());
        unspecifiedResult.setSuperterm(unspecifiedTerm);
        unspecifiedResult.setStartStage(result.getStartStage());
        unspecifiedResult.setEndStage(result.getEndStage());
        unspecifiedResult.setExpressionFound(true);
        unspecifiedResult.addFigure(figure);
        session.save(unspecifiedResult);
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

        Term unspecified = OntologyManager.getInstance().getTermByName(Ontology.ANATOMY, Term.UNSPECIFIED);

        String hql = "select pheno from Phenotype pheno " +
                "     where pheno.genotypeExperiment = :genox" +
                "           and pheno.startStage = :start " +
                "           and pheno.endStage = :end " +
                "           and pheno.superterm = :anatomyTermID " +
                "           and pheno.subterm is null " +
                "           and pheno.publication = :publication";

        Query query = session.createQuery(hql);
        query.setEntity("genox", pheno.getGenotypeExperiment());
        query.setEntity("start", pheno.getStartStage());
        query.setEntity("end", pheno.getEndStage());
        query.setParameter("anatomyTermID", unspecified);
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
        String supertermName = expressedTerm.getSuperterm().getTermName();
        if (supertermName == null)
            throw new NullPointerException("No superterm provided.");

        Session session = HibernateUtil.currentSession();
        Criteria crit;

        if (expressedTerm.getSubterm() != null) {
            OntologyDTO subtermOntology = expressedTerm.getSubterm().getOntology();
            if (subtermOntology == null)
                throw new NullPointerException("No subterm ontology provided.");
            crit = session.createCriteria(ExpressionStructure.class);
            Criteria subterm = crit.createCriteria("subterm");
            subterm.add(Restrictions.eq("termName", expressedTerm.getSubterm().getTermName()));
        } else {
            crit = session.createCriteria(ExpressionStructure.class);
            crit.add(Restrictions.isNull("subterm"));
        }
        Criteria publication = crit.createCriteria("publication");
        publication.add(Restrictions.eq("zdbID", publicationID));
        Criteria superterm = crit.createCriteria("superterm");
        superterm.add(Restrictions.eq("termName", supertermName));

        List list = crit.list();
        return list != null && !list.isEmpty();
    }

    /**
     * Retrieve a genotype experiment for a given genotype ID.
     *
     * @param genotypeID genotype id
     * @return GenotypeExperiment
     */
    @SuppressWarnings("unchecked")
    public GenotypeExperiment getGenotypeExperimentByGenotypeID(String genotypeID) {
        Session session = HibernateUtil.currentSession();
        String hql = "from GenotypeExperiment as genox where " +
                " genox.genotype.zdbID = :genotypeID";
        Query query = session.createQuery(hql);
        query.setParameter("genotypeID", genotypeID);
        return (GenotypeExperiment) query.uniqueResult();
    }

    /**
     * Create all expression structures being used in a given publication.
     *
     * @param publicationID publication id
     */
    @Override
    public void createExpressionPile(String publicationID) {
        List<ExpressionResult> expressionResults = getAllExpressionResults(publicationID);
        if (expressionResults == null || expressionResults.isEmpty())
            return;
        Publication publication = getPublicationRepository().getPublication(publicationID);
        Set<ExpressionStructure> distinctStructures = new HashSet<ExpressionStructure>();
        for (ExpressionResult expressionResult : expressionResults) {
            ExpressionStructure expressionStructure = instantiateExpressionStructure(expressionResult, publication);
            // only create structures that are not already on the pile.
            if (distinctStructures.add(expressionStructure))
                createPileStructure(expressionStructure);
        }

    }

    private ExpressionStructure instantiateExpressionStructure(ExpressionResult expressionResult, Publication publication) {
        ExpressionStructure structure = new ExpressionStructure();
        structure.setDate(new Date());
        structure.setPerson(Person.getCurrentSecurityUser());
        structure.setPublication(publication);
        structure.setSuperterm(expressionResult.getSuperterm());
        Term subTerm = expressionResult.getSubterm();
        if (subTerm != null) {
            structure.setSubterm(subTerm);
        }
        return structure;
    }

    private List<ExpressionResult> getAllExpressionResults(String publicationID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct expression from ExpressionResult expression where" +
                "          expression.expressionExperiment.publication.id= :publicationID";
        Query query = session.createQuery(hql);
        query.setParameter("publicationID", publicationID);
        return (List<ExpressionResult>) query.list();

    }

    /**
     * Create a new structure - post-composed - for the structure pile.
     *
     * @param structure structure
     */
    public void createPileStructure(ExpressionStructure structure) {
        Session session = HibernateUtil.currentSession();
        session.save(structure);
    }

    /**
     * Retrieve Expressions for a given term.
     * @param term term
     * @return list of expressions
     */
    @Override
    public List<ExpressionResult> getExpressionsWithEntity(Term term) {
        String hql = "select distinct expression from ExpressionResult expression where " +
                "(superterm = :term OR subterm = :term) " +
                " AND expressionFound = :expressionFound ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("term", term);
        query.setBoolean("expressionFound", true);
        return (List<ExpressionResult>) query.list();
    }

    /**
     * Retrieve Expressions for a given list of terms.
     * @param terms term
     * @return list of expressions
     */
    @Override
    public List<ExpressionResult> getExpressionsWithEntity(List<Term> terms) {
        List<ExpressionResult> allExpressions = new ArrayList<ExpressionResult>(50);
        for (Term term : terms) {
            List<ExpressionResult> phenotypes = getExpressionsWithEntity(term);
            allExpressions.addAll(phenotypes);
        }
        List<ExpressionResult> nonDuplicateExpressions = removeDuplicateExpressions(allExpressions);
        Collections.sort(nonDuplicateExpressions);
        return nonDuplicateExpressions;
    }

    private List<ExpressionResult> removeDuplicateExpressions(List<ExpressionResult> allExpressions) {
        Set<ExpressionResult> results = new HashSet<ExpressionResult>();
        for (ExpressionResult result : allExpressions) {
            results.add(result);
        }
        List<ExpressionResult> expressionResults = new ArrayList<ExpressionResult>(results.size());
        expressionResults.addAll(results);
        return expressionResults;
    }


    private void validateFigureAnnotationKey(String experimentZdbID, String figureID, String startStageID, String endStageID) {
        ActiveData data = new ActiveData();
        // these calls validate the keys according to zdb id syntax.
        // ToDo: Change the validate method static to be able to call it directly.
        data.setZdbID(experimentZdbID);
        data.setZdbID(figureID);
        data.setZdbID(startStageID);
        data.setZdbID(endStageID);
    }

    private List<ExperimentFigureStage> populateExperimentFigureStage
            (List<Object[]> objects) {
        List<ExperimentFigureStage> efses = new ArrayList<ExperimentFigureStage>();
        for (Object[] object : objects) {
            //ExpressionExperiment exp = (ExpressionExperiment) object[0];
            ExpressionResult result = (ExpressionResult) object[0];
            Figure figure = (Figure) object[1];
            ExperimentFigureStage efs = new ExperimentFigureStage();
            ExpressionExperiment expressionExperiment = result.getExpressionExperiment();
            expressionExperiment.addExpressionResult(result);
            efs.setExpressionExperiment(expressionExperiment);
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

    /**
     * Retrieve all expression results for a given genotype
     *
     * @param genotype genotype
     * @return list of expression results
     */
    public List<ExpressionResult> getExpressionResultsByGenotype(Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql = "select xpRslt from ExpressionResult xpRslt, ExpressionExperiment xpExp, GenotypeExperiment genox " +
                "      where genox.genotype.zdbID = :genotypeID " +
                "        and genox = xpExp.genotypeExperiment " +
                "        and xpRslt.expressionExperiment = xpExp ";
        Query query = session.createQuery(hql);
        query.setString("genotypeID", genotype.getZdbID());

        return (List<ExpressionResult>) query.list();
    }

    @SuppressWarnings("unchecked")
    public List<AnatomyItem> getWildTypeAnatomyExpressionForMarker(String zdbID){
//       String sql = "SELECT distinct term_zdb_id" +
//               "FROM " +
//               "expression_result , expression_experiment, term , genotype_experiment, experiment , genotype, anatomy_item" +
//               "WHERE" +
//               "xpatex_gene_zdb_id = :zdbID " +
//               "AND  xpatres_xpatex_zdb_id = xpatex_zdb_id " +
//               "AND xpatres_expression_found='t'" +
//               "AND xpatres_superterm_zdb_id = term_zdb_id" +
//               "AND term_ont_id = anatitem_obo_id" +
//               "AND xpatex_genox_zdb_id = genox_zdb_id " +
//               "AND exp_zdb_id = genox_exp_zdb_id and exp_name = '_Standard'  " +
//               "AND geno_zdb_id  = genox_geno_zdb_id " +
//               "AND geno_is_wildtype = 't' " +
//               "ORDER BY anatitem_name_order asc;"  ;
        String hql = "SELECT distinct ai " +
                "FROM " +
                "ExpressionResult er, ExpressionExperiment ee, Term t, GenotypeExperiment ge, Experiment e, Genotype g, AnatomyItem ai " +
                " WHERE " +
                "ee.gene.zdbID = :zdbID " +
                "AND  er.expressionExperiment.zdbID = ee.zdbID " +
                "AND er.expressionFound = :expressionFound " +
                "AND er.superterm.id = t.id " +
                "AND t.oboID = ai.oboID " +
                "AND ee.genotypeExperiment.zdbID = ge.zdbID " +
                "AND e.zdbID = ge.experiment.zdbID and e.name = :experiment  " +
                "AND g.zdbID = ge.genotype.zdbID " +
                "AND g.wildtype = :wildType  " +
                "ORDER BY ai.nameOrder asc"  ;
        return (List<AnatomyItem>) HibernateUtil.currentSession().createQuery(hql)
                .setParameter("zdbID",zdbID)
                .setParameter("experiment","_Standard")
                .setBoolean("expressionFound",true)
                .setBoolean("wildType",true)
                .list();
    }
}
