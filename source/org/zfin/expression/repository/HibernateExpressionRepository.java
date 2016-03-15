package org.zfin.expression.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.hibernate.transform.ResultTransformer;
import org.springframework.stereotype.Repository;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.expression.*;
import org.zfin.expression.presentation.ExpressedStructurePresentation;
import org.zfin.expression.presentation.PublicationExpressionBean;
import org.zfin.expression.presentation.StageExpressionPresentation;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.ExpressedTermDTO;
import org.zfin.gwt.root.dto.OntologyDTO;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Clone;
import org.zfin.marker.Gene;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.FigureLink;
import org.zfin.publication.presentation.FigurePresentation;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.util.TermFigureStageRange;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.*;

/**
 * Repository that is used for curation actions, such as dealing with expression experiments.
 */
@Repository
public class HibernateExpressionRepository implements ExpressionRepository {

    private Logger logger = Logger.getLogger(HibernateExpressionRepository.class);

    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

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
            GenericTerm anat = (GenericTerm) tuple[1];
            Figure fig = (Figure) tuple[2];

            xsac.add(stage, anat, fig);

        }


        return xsac;

    }

    public int getExpressionFigureCountForGenotype(Genotype genotype) {
        String sql = "   select count(distinct xpatfig_fig_zdb_id) " +
                "           from expression_pattern_figure " +
                "                join expression_result " +
                "			on xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "                join expression_experiment " +
                "			on xpatex_zdb_id = xpatres_xpatex_zdb_id " +
                "                join genotype_experiment " +
                "           on xpatex_genox_zdb_id = genox_zdb_id " +
                "          where genox_geno_zdb_id = :genotypeZdbID " +
                "         and xpatex_atb_zdb_id is null ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("genotypeZdbID", genotype.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }

    @Override
    public Publication getExpressionSinglePub(Marker marker) {
        String sql = "  select p from Publication p join p.expressionExperiments ee where ee.gene = :gene ";
        Query query = HibernateUtil.currentSession().createQuery(sql);
        query.setString("gene", marker.getZdbID());
        List<Publication> pubs = query.list();

        if (CollectionUtils.isEmpty(pubs)) {
            logger.debug("a single pub not returned for marker expression: " + marker);
            return null;
        } else if (pubs.size() > 1) {
            logger.debug("multiple pubs [" + pubs.size() + "] returned for marker expression: " + marker);
        }

        return pubs.get(0);
    }

    @Override
    public List<Publication> getExpressionPub(Marker marker) {
        String sql = "  select distinct p from Publication p " +
                "join p.expressionExperiments ee " +
                "where ee.gene = :gene " +
                "and not exists (from Clone as clone " +
                "where ee.probe = clone and clone.problem = :chimeric)";
        Query query = HibernateUtil.currentSession().createQuery(sql);
        query.setString("gene", marker.getZdbID());
        query.setParameter("chimeric", Clone.ProblemType.CHIMERIC);
        return query.list();
    }

    public int getExpressionPubCountForGene(Marker marker) {
        String sql = "  select count(distinct xpatex_source_zdb_id) " +
                "      from expression_experiment join " +
                "           expression_result on xpatex_zdb_id = xpatres_xpatex_zdb_id " +
                "      join expression_pattern_figure on xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "     where xpatex_gene_zdb_id = :markerZdbID " +
                "    and not exists( " +
                "         select 'x' from clone " +
                "         where clone_mrkr_zdb_id=xpatex_probe_feature_zdb_id " +
                "         and NVL(clone_problem_type,'') = 'Chimeric' " +
                "     ) " +
                "     and not exists ( " +
                "         select 'x' from marker " +
                "         where mrkr_zdb_id = xpatex_probe_feature_zdb_id " +
                "         and mrkr_abbrev[1,10] = 'WITHDRAWN:' " +
                "     ) ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("markerZdbID", marker.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }


    public int getExpressionPubCountForEfg(Marker marker) {
        String sql = "  select count(distinct xpatex_source_zdb_id) " +
                "      from expression_experiment join " +
                "           expression_result on xpatex_zdb_id = xpatres_xpatex_zdb_id " +
                "      join expression_pattern_figure on xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "     where xpatex_gene_zdb_id = :markerZdbID ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("markerZdbID", marker.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }

    public int getExpressionPubCountForClone(Clone clone) {
        String sql = "  select count(distinct xpatex_source_zdb_id) " +
                "      from expression_experiment join " +
                "           expression_result on xpatex_zdb_id = xpatres_xpatex_zdb_id " +
                "      join expression_pattern_figure on xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "     where xpatex_probe_feature_zdb_id = :markerZdbID ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("markerZdbID", clone.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }


    public int getExpressionFigureCountForEfg(Marker marker) {
        String sql = "   select count(distinct xpatfig_fig_zdb_id) " +
                "           from expression_pattern_figure " +
                "                join expression_result " +
                "			on xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "                join expression_experiment " +
                "			on xpatex_zdb_id = xpatres_xpatex_zdb_id " +
                "          where xpatex_gene_zdb_id = :markerZdbID ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("markerZdbID", marker.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }

    public int getExpressionFigureCountForClone(Clone clone) {
        String sql = "   select count(distinct xpatfig_fig_zdb_id) " +
                "           from expression_pattern_figure " +
                "                join expression_result " +
                "			on xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "                join expression_experiment " +
                "			on xpatex_zdb_id = xpatres_xpatex_zdb_id " +
                "          where xpatex_probe_feature_zdb_id = :markerZdbID ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("markerZdbID", clone.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }

    @Override
    public FigureLink getExpressionSingleFigure(Marker marker) {
        String sql = "select distinct xpatfig_fig_zdb_id , f.fig_label " +
                "     from expression_pattern_figure " +
                "     join expression_result on xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "     join expression_experiment on xpatex_zdb_id = xpatres_xpatex_zdb_id " +
                "     join figure f on xpatfig_fig_zdb_id=f.fig_zdb_id " +
                "     where xpatex_gene_zdb_id = :markerZdbID " +
                "     and not exists " +
                "     ( " +
                "     select 'x' from marker " +
                "     where mrkr_zdb_id = xpatex_probe_feature_zdb_id " +
                "     and mrkr_abbrev[1,10] = 'WITHDRAWN:' " +
                "     ) " +
                "     and not exists " +
                "     ( " +
                "     select 'x' from clone " +
                "     where clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id " +
                "     and clone_problem_type = 'Chimeric' " +
                "     ) ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("markerZdbID", marker.getZdbID());
        query.setMaxResults(1);
        query.setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                FigureLink figureLink = new FigureLink();
                figureLink.setFigureZdbId(tuple[0].toString());
                figureLink.setLinkContent(tuple[1].toString());
                figureLink.setLinkValue(
                        FigurePresentation.getLink(figureLink.getFigureZdbId(), figureLink.getLinkContent())
                );
                return figureLink;
            }
        });
        return (FigureLink) query.uniqueResult();
    }

    public int getExpressionFigureCountForGene(Marker marker) {
        String sql = "   select count(distinct xpatfig_fig_zdb_id) " +
                "           from expression_pattern_figure " +
                "                join expression_result " +
                "			on xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "                join expression_experiment " +
                "			on xpatex_zdb_id = xpatres_xpatex_zdb_id " +
                "          where xpatex_gene_zdb_id = :markerZdbID " +
                "         and not exists " +
                "         ( " +
                "           select 'x' from marker " +
                "             where mrkr_zdb_id = xpatex_probe_feature_zdb_id " +
                "             and mrkr_abbrev[1,10] = 'WITHDRAWN:' " +
                "         ) " +
                "         and not exists " +
                "         ( " +
                "          select 'x' from clone " +
                "             where clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id " +
                "             and clone_problem_type = 'Chimeric' " +
                "         ) ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("markerZdbID", marker.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
    }

    public int getExpressionFigureCountForFish(Fish fish) {
        String sql = "   select count(distinct xpatfig_fig_zdb_id) " +
                "           from expression_pattern_figure " +
                "                join expression_result " +
                "			on xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "                join expression_experiment " +
                "			on xpatex_zdb_id = xpatres_xpatex_zdb_id " +
                "                join fish_experiment " +
                "           on xpatex_genox_zdb_id = genox_zdb_id " +
                "          where genox_fish_zdb_id = :fishID " +
                "         and xpatex_atb_zdb_id is null ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("fishID", fish.getZdbID());
        Object result = query.uniqueResult();
        return Integer.parseInt(result.toString());
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

    public List<PublicationExpressionBean> getDirectlySubmittedExpressionForClone(Clone clone) {

        String sql = "  select count(distinct xpatfig_fig_zdb_id), " +
                "       pub.zdb_id, pub.pub_mini_ref,m.mrkr_abbrev, m.mrkr_Zdb_id" +
                "           from expression_pattern_figure " +
                "                join expression_result on xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "                join expression_experiment on xpatex_zdb_id = xpatres_xpatex_zdb_id " +
                "                join publication pub on pub.zdb_id = xpatex_source_zdb_id " +
                "                join marker m on m.mrkr_zdb_id=xpatex_probe_feature_zdb_id " +
                "           where xpatex_probe_feature_zdb_id = :markerZdbID " +
                "           and pub.jtype = 'Unpublished' " +
                "           group by m.mrkr_zdb_id,m.mrkr_abbrev, pub.zdb_id, pub.pub_mini_ref; ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("markerZdbID", clone.getZdbID());

        query.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                PublicationExpressionBean publicationExpressionBean = new PublicationExpressionBean();
                publicationExpressionBean.setNumFigures(Integer.parseInt(tuple[0].toString()));
                publicationExpressionBean.setPublicationZdbID(tuple[1].toString());
                publicationExpressionBean.setMiniAuth(tuple[2].toString());
                if (tuple[3] != null) {
                    publicationExpressionBean.setProbeFeatureAbbrev(tuple[3].toString());
                }
                if (tuple[4] != null) {
                    publicationExpressionBean.setProbeFeatureZdbId(tuple[4].toString());
                }
                return publicationExpressionBean;
            }

            @Override
            public List transformList(List collection) {
                List<PublicationExpressionBean> list = new ArrayList<PublicationExpressionBean>();
                for (Object o : collection) {
                    list.add((PublicationExpressionBean) o);
                }
                return list;
            }
        });
        List<PublicationExpressionBean> pubList = query.list();
        return pubList;
    }

    public List<PublicationExpressionBean> getDirectlySubmittedExpressionForGene(Marker marker) {

        String sql = "  select count(distinct xpatfig_fig_zdb_id), " +
                "		pub.zdb_id, pub.pub_mini_ref,m.mrkr_abbrev, m.mrkr_zdb_id " +
                "           from expression_pattern_figure " +
                "                join expression_result on xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "                join expression_experiment on xpatex_zdb_id = xpatres_xpatex_zdb_id " +
                "                join publication pub on pub.zdb_id = xpatex_source_zdb_id " +
                "                join marker m on m.mrkr_zdb_id=xpatex_probe_feature_zdb_id " +
                "          where xpatex_gene_zdb_id = :markerZdbID " +
                "          and pub.jtype = 'Unpublished' " +
                "          and not exists( " +
                "             select 'x' from clone " +
                "             where clone_mrkr_zdb_id=xpatex_probe_feature_zdb_id " +
                "             and clone_problem_type = 'Chimeric' " +
                "          ) " +
                "          and not exists ( " +
                "             select 'x' from marker " +
                "             where mrkr_zdb_id = xpatex_probe_feature_zdb_id " +
                "             and mrkr_abbrev[1,10] = 'WITHDRAWN:' " +
                "         ) " +
                "         group by m.mrkr_zdb_id, m.mrkr_abbrev, pub.zdb_id, pub.pub_mini_ref ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("markerZdbID", marker.getZdbID());

        query.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                PublicationExpressionBean publicationExpressionBean = new PublicationExpressionBean();
                publicationExpressionBean.setNumFigures(Integer.parseInt(tuple[0].toString()));
                publicationExpressionBean.setPublicationZdbID(tuple[1].toString());
                publicationExpressionBean.setMiniAuth(tuple[2].toString());
                if (tuple[3] != null) {
                    publicationExpressionBean.setProbeFeatureAbbrev(tuple[3].toString());
                }
                if (tuple[4] != null) {
                    publicationExpressionBean.setProbeFeatureZdbId(tuple[4].toString());
                }
                return publicationExpressionBean;
            }

            @Override
            public List transformList(List collection) {
                List<PublicationExpressionBean> list = new ArrayList<PublicationExpressionBean>();
                for (Object o : collection) {
                    list.add((PublicationExpressionBean) o);
                }
                return list;
            }
        });
        List<PublicationExpressionBean> pubList = query.list();
        return pubList;
    }

    @Override
    public List<PublicationExpressionBean> getDirectlySubmittedExpressionForEfg(Marker marker) {

        String sql = "  select count(distinct xpatfig_fig_zdb_id), " +
                "           pub.zdb_id, pub.pub_mini_ref ,m.mrkr_abbrev, m.mrkr_zdb_id  " +
                "           from expression_pattern_figure " +
                "                join expression_result on xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "                join expression_experiment on xpatex_zdb_id = xpatres_xpatex_zdb_id " +
                "                join publication pub on pub.zdb_id = xpatex_source_zdb_id " +
                "                join marker m on m.mrkr_zdb_id=xpatex_gene_zdb_id" +
                "           where xpatex_gene_zdb_id = :markerZdbID " +
                "           and pub.jtype = 'Unpublished' " +
                "         group by m.mrkr_zdb_id, m.mrkr_abbrev, pub.zdb_id, pub.pub_mini_ref ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("markerZdbID", marker.getZdbID());

        query.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                PublicationExpressionBean publicationExpressionBean = new PublicationExpressionBean();
                publicationExpressionBean.setNumFigures(Integer.parseInt(tuple[0].toString()));
                publicationExpressionBean.setPublicationZdbID(tuple[1].toString());
                publicationExpressionBean.setMiniAuth(tuple[2].toString());
                if (tuple[3] != null) {
                    publicationExpressionBean.setProbeFeatureAbbrev(tuple[3].toString());
                }
                if (tuple[4] != null) {
                    publicationExpressionBean.setProbeFeatureZdbId(tuple[4].toString());
                }
                return publicationExpressionBean;
            }

            @Override
            public List transformList(List collection) {
                List<PublicationExpressionBean> list = new ArrayList<PublicationExpressionBean>();
                for (Object o : collection) {
                    list.add((PublicationExpressionBean) o);
                }
                return list;
            }
        });
        List<PublicationExpressionBean> pubList = query.list();
        return pubList;
    }


    @SuppressWarnings("unchecked")
    public ExpressionExperiment getExpressionExperiment(String experimentID) {
        Session session = HibernateUtil.currentSession();
        return (ExpressionExperiment) session.get(ExpressionExperiment.class, experimentID);
    }

    @SuppressWarnings("unchecked")
    public ExpressionExperiment2 getExpressionExperiment2(String experimentID) {
        Session session = HibernateUtil.currentSession();
        return (ExpressionExperiment2) session.get(ExpressionExperiment2.class, experimentID);
    }

    public ExpressionDetailsGenerated getExpressionExperiment2(long id) {
        return (ExpressionDetailsGenerated) currentSession().get(ExpressionDetailsGenerated.class, id);
    }

    @Override
    public ExpressionDetailsGenerated getExpressionDetailsGenerated(String xpatZdbID, String figZdbID) {
        if (StringUtils.isEmpty(xpatZdbID) || StringUtils.isEmpty(figZdbID)) {
            return null;
        }

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ExpressionDetailsGenerated.class);
        criteria.add(Restrictions.eq("expressionExperiment.zdbID", xpatZdbID));
        criteria.add(Restrictions.eq("figure.zdbID", figZdbID));
        return (ExpressionDetailsGenerated) criteria.uniqueResult();
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

    public FishExperiment getFishExperimentByID(String fishExpID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(FishExperiment.class);
        criteria.add(Restrictions.eq("zdbID", fishExpID));
        return (FishExperiment) criteria.uniqueResult();
    }

    /**
     * Retrieve FishExperiment by Experiment ID
     *
     * @param experimentID id
     * @return FishExperiment
     */
    public FishExperiment getFishExperimentByExperimentIDAndFishID(String experimentID, String fishID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(FishExperiment.class);
        criteria.add(Restrictions.eq("experiment.zdbID", experimentID));
        criteria.add(Restrictions.eq("fish.zdbID", fishID));
        return (FishExperiment) criteria.uniqueResult();
    }

    /**
     * Create a new genotype experiment for given experiment and genotype.
     *
     * @param experiment genotype experiment
     */
    public void createFishExperiment(FishExperiment experiment) {
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
     * @param fishID       id
     */
    public FishExperiment createFishExperiment(String experimentID, String fishID) {
        Experiment experiment = getExperimentByID(experimentID);
        Fish fish = getMutantRepository().getFish(fishID);
        FishExperiment fishox = new FishExperiment();
        fishox.setExperiment(experiment);
        fishox.setFish(fish);
        createFishExperiment(fishox);
        return fishox;
    }

    /**
     * Create a new expression Experiment.
     *
     * @param expressionExperiment expression experiment
     */
    public void createExpressionExperiment(ExpressionExperiment2 expressionExperiment) {
        Session session = HibernateUtil.currentSession();
        session.save(expressionExperiment);
    }

    /**
     * Remove an existing expression experiment and all objects that it is composed of.
     * Note: It delegates the call to removing the ActiveData record (OCD).
     *
     * @param experiment expression experiment
     */
    public void deleteExpressionExperiment(ExpressionExperiment2 experiment) {
        InfrastructureRepository infraRep = RepositoryFactory.getInfrastructureRepository();
        infraRep.deleteActiveDataByZdbID(experiment.getZdbID());
    }

    @SuppressWarnings("unchecked")
    public List<ExpressionExperiment2> getExperimentsByGeneAndFish(String publicationID, String geneZdbID, String fishID) {
        String hql = "select experiment from ExpressionExperiment2 experiment "
                + "       left join experiment.gene as gene "
                + "       left join experiment.fishExperiment as fishox"
                + "     where experiment.publication.zdbID = :pubID ";
        if (geneZdbID != null) {
            hql += "           and gene.zdbID = :geneID ";
        }
        if (fishID != null) {
            hql += "           and fishox.fish.zdbID = :fishID ";
        }
        hql += "    order by gene.abbreviationOrder, " +
                "             fishox.fish.name, " +
                "             fishox.experiment.name, " +
                "             experiment.assay.displayOrder ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("pubID", publicationID);
        if (geneZdbID != null) {
            query.setString("geneID", geneZdbID);
        }
        if (fishID != null) {
            query.setString("fishID", fishID);
        }
        query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

        return (List<ExpressionExperiment2>) query.list();
    }


    @SuppressWarnings("unchecked")
    public List<ExpressionExperiment> getExperiments(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select experiment from ExpressionExperiment experiment" +
                "       left join experiment.gene as gene " +
                "     where experiment.publication.zdbID = :pubID " +
                "    order by gene.abbreviationOrder, " +
                "             experiment.fishExperiment.fish.name, " +
                "             experiment.assay.displayOrder ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);

        return (List<ExpressionExperiment>) query.list();

    }


    /**
     * Retrieve an experiment figure stage for given pub, gene and fish.
     *
     * @param publicationID Publication
     * @param geneZdbID     gene
     * @param fishZdbID     fish
     * @return list of experiment figure stages.
     */
    @SuppressWarnings("unchecked")
    public List<ExpressionFigureStage> getExperimentFigureStagesByGeneAndFish(String publicationID,
                                                                              String geneZdbID,
                                                                              String fishZdbID,
                                                                              String figureZdbID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select efs from ExpressionFigureStage as efs "
                + "       left join efs.expressionExperiment.gene as gene "
                + "       join efs.expressionExperiment.fishExperiment.fish as fish "
                + "       where efs.expressionExperiment.publication.zdbID = :pubID ";
        if (geneZdbID != null) {
            hql += " and gene.zdbID = :geneZdbID ";
        }
        if (StringUtils.isNotEmpty(figureZdbID)) {
            hql += " and efs.figure.zdbID = :figureZdbID ";
        }
        if (fishZdbID != null) {
            hql += " and fish.zdbID = :fishZdbID ";
        }

        hql += "       order by efs.figure.orderingLabel, gene.abbreviationOrder "
                + "             , fish.name "
                + "             , efs.expressionExperiment.assay.displayOrder "
                + "             , efs.startStage.abbreviation "
                + " ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);

        if (geneZdbID != null) {
            query.setString("geneZdbID", geneZdbID);
        }
        if (StringUtils.isNotEmpty(figureZdbID)) {
            query.setString("figureZdbID", figureZdbID);
        }
        if (fishZdbID != null) {
            query.setString("fishZdbID", fishZdbID);
        }

        return (List<ExpressionFigureStage>) query.list();
    }

    @SuppressWarnings("unchecked")
    public List<ExpressionExperiment> getExperimentsByGeneAndFish2(String publicationID, String geneZdbID, String fishID) {

        String hql = "select experiment from ExpressionExperiment experiment "
                + "       left join experiment.gene as gene "
                + "       left join experiment.fishExperiment as fishox"
                + "     where experiment.publication.zdbID = :pubID ";
        if (geneZdbID != null) {
            hql += "           and gene.zdbID = :geneID ";
        }
        if (fishID != null) {
            hql += "           and fishox.fish.zdbID = :fishID ";
        }
        hql += "    order by gene.abbreviationOrder, " +
                "             fishox.fish.name, " +
                "             fishox.experiment.name, " +
                "             experiment.assay.displayOrder ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("pubID", publicationID);
        if (geneZdbID != null) {
            query.setString("geneID", geneZdbID);
        }
        if (fishID != null) {
            query.setString("fishID", fishID);
        }
        query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

        return (List<ExpressionExperiment>) query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Genotype> getFishUsedInExperiment(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fish from Fish fish, ExpressionExperiment ee," +
                "                               FishExperiment fishox " +
                "     where ee.publication.zdbID = :pubID " +
                "           and ee.fishExperiment = fishox " +
                "           and fishox.fish = fish" +
                "    order by fish.handle ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);

        return (List<Genotype>) query.list();
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

        if (result == null) {
            return;
        }

        Session session = HibernateUtil.currentSession();
        ExpressionResult unspecifiedResult = getUnspecifiedExpressResult(result);

        // ignore unspecified addition if not the first creation.
        if (result.getSuperTerm().getTermName().equals(Term.UNSPECIFIED)) {
            if (result.isExpressionFound()) {
                return;
            } else {
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
        }


        List<ExpressionResult> existingResult = checkForExpressionResultRecord(result);

        if (existingResult != null && existingResult.size() > 0) {
            if (existingResult.size() > 1) {
                throw new RuntimeException("More than one expression result found");
            }
            existingResult.get(0).addFigure(singleFigure);
            // check if unspecified exists
            // unspecified expression result record exists
            if (unspecifiedResult != null) {
                Set<Figure> figures = unspecifiedResult.getFigures();
                if (figures == null || figures.size() < 2) {
                    ////getInfrastructureRepository().deleteActiveDataByZdbID(unspecifiedResult.getZdbID());
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
                    ////getInfrastructureRepository().deleteActiveDataByZdbID(unspecifiedResult.getZdbID());
                    session.save(result);
                    result.getExpressionExperiment().addExpressionResult(result);
                } else if (unspecifiedResult.getFigures().size() == 1) {
                    // has one figure associated
                    // check if it is associated to the figure in question.
                    // if yes, remove it.
                    if (figures.contains(singleFigure)) {
///                        getInfrastructureRepository().deleteActiveDataByZdbID(unspecifiedResult.getZdbID());
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
        GenericTerm unspecified = ontologyRepository.getTermByNameActive(Term.UNSPECIFIED, Ontology.ANATOMY);
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ExpressionResult.class);
        criteria.add(Restrictions.eq("expressionExperiment", result.getExpressionExperiment()));
        criteria.add(Restrictions.eq("startStage", result.getStartStage()));
        criteria.add(Restrictions.eq("endStage", result.getEndStage()));
        criteria.add(Restrictions.eq("entity.superterm", unspecified));
        criteria.add(Restrictions.eq("expressionFound", true));
        return (ExpressionResult) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<ExpressionResult> checkForExpressionResultRecord(ExpressionResult result) {
        // first check if an expression result record already exists
        Session session = HibernateUtil.currentSession();
        Criteria criteria;
        criteria = session.createCriteria(ExpressionResult.class);
        Term subterm = result.getSubTerm();
        if (subterm == null) {
            criteria.add(Restrictions.isNull("entity.subterm"));
        } else {
            criteria.add(Restrictions.eq("entity.subterm", result.getSubTerm()));
        }
        criteria.add(Restrictions.eq("expressionExperiment", result.getExpressionExperiment()));
        criteria.add(Restrictions.eq("startStage", result.getStartStage()));
        criteria.add(Restrictions.eq("endStage", result.getEndStage()));
        criteria.add(Restrictions.eq("entity.superterm", result.getSuperTerm()));
        criteria.add(Restrictions.eq("expressionFound", result.isExpressionFound()));
        return (List<ExpressionResult>) criteria.list();
    }
    public List<ExpressionResult2> checkForExpressionResultRecord2(ExpressionResult2 result) {
        // first check if an expression result record already exists
        Session session = HibernateUtil.currentSession();
        Criteria criteria;
        criteria = session.createCriteria(ExpressionResult2.class);
        Term subTerm = result.getSubTerm();
        if (subTerm == null) {
            criteria.add(Restrictions.isNull("subTerm"));
        } else {
            criteria.add(Restrictions.eq("subTerm", result.getSubTerm()));
        }
        criteria.add(Restrictions.eq("expressionFigureStage", result.getExpressionFigureStage()));

        criteria.add(Restrictions.eq("superTerm", result.getSuperTerm()));
        criteria.add(Restrictions.eq("expressionFound", result.isExpressionFound()));
        return (List<ExpressionResult2>) criteria.list();
    }

    // run the script to update the fast search table for antibodies-anatomy

    public void runAntibodyAnatomyFastSearchUpdate(ExpressionResult result) {
        Session session = currentSession();
        Connection connection = session.connection();
        CallableStatement statement = null;
        String sql = "execute procedure add_ab_ao_fast_search(?)";
        try {
            statement = connection.prepareCall(sql);
            String zdbID = "";
            ////String zdbID = result.getZdbID();
            statement.setString(1, zdbID);
            statement.execute();
            logger.info("Execute stored procedure: " + sql + " with the argument " + zdbID);
        } catch (SQLException e) {
            logger.error("Could not run: " + sql, e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
        }
    }

    /**
     * Delete a expressionfigure stage record including all expression result on it
     *
     * @param figureAnnotation experiment figure stage.
     */
    public void deleteFigureAnnotation(ExpressionFigureStage figureAnnotation) {
        if (figureAnnotation == null) {
            throw new NullPointerException("No Figure Annotation provided");
        }
        HibernateUtil.currentSession().delete(figureAnnotation);
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
    public ExpressionFigureStage getExperimentFigureStage(String experimentZdbID, String figureID, String startStageID, String endStageID) {
        if (StringUtils.isEmpty(experimentZdbID)) {
            return null;
        }
        if (StringUtils.isEmpty(figureID)) {
            return null;
        }
        if (StringUtils.isEmpty(startStageID)) {
            return null;
        }
        if (StringUtils.isEmpty(endStageID)) {
            return null;
        }
        validateFigureAnnotationKey(experimentZdbID, figureID, startStageID, endStageID);

        Session session = HibernateUtil.currentSession();

        String hql = "select efs from ExpressionFigureStage as efs ";
        hql += "     where efs.expressionExperiment.zdbID = :experimentID ";
        hql += " AND efs.startStage.zdbID = :startID ";
        hql += " AND efs.endStage.zdbID = :endID ";
        hql += " AND efs.figure.zdbID = :figureID ";
        Query query = session.createQuery(hql);
        query.setString("experimentID", experimentZdbID);
        query.setString("startID", startStageID);
        query.setString("endID", endStageID);
        query.setString("figureID", figureID);

        return (ExpressionFigureStage) query.uniqueResult();
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

    public void deleteExpressionStructuresForPub(Publication publication) {
        List<ExpressionStructure> structures = HibernateUtil.currentSession()
                .createCriteria(ExpressionStructure.class)
                .add(Restrictions.eq("publication", publication))
                .list();
        for (ExpressionStructure structure : structures) {
            getInfrastructureRepository().deleteActiveDataByZdbID(structure.getZdbID());
        }
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
    public void deleteExpressionResultPerFigure(ExpressionResult2 result, Figure figure) {
/*
        if (result == null) {
            return;
        }

        Session session = HibernateUtil.currentSession();
        Set<Figure> figures = result.getFigures();
        if (figures == null) {
            return;
        }

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
                getInfrastructureRepository().deleteActiveDataByZdbID(result.getZdbID());
                session.flush();
            } else {
                getInfrastructureRepository().deleteActiveDataByZdbID(result.getZdbID());
                session.flush();
                if (unspecifiedResult != null) {
                    unspecifiedResult.addFigure(figure);
                } else {
                    // add unspecified
                    createUnspecifiedExpressionResult(result, figure);
                }
            }
            session.refresh(result.getExpressionExperiment());
        }
*/
    }

    private void createUnspecifiedExpressionResult(ExpressionResult result, Figure figure) {
        Session session = HibernateUtil.currentSession();
        GenericTerm unspecifiedTerm = ontologyRepository.getTermByNameActive(Term.UNSPECIFIED, Ontology.ANATOMY);

        ExpressionResult unspecifiedResult = new ExpressionResult();
        unspecifiedResult.setExpressionExperiment(result.getExpressionExperiment());
        unspecifiedResult.setSuperTerm(unspecifiedTerm);
        unspecifiedResult.setStartStage(result.getStartStage());
        unspecifiedResult.setEndStage(result.getEndStage());
        unspecifiedResult.setExpressionFound(true);
        unspecifiedResult.addFigure(figure);
        session.save(unspecifiedResult);
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
        if (publicationID == null) {
            throw new NullPointerException("No Publication provided.");
        }
        String supertermName = expressedTerm.getEntity().getSuperTerm().getTermName();
        if (supertermName == null) {
            throw new NullPointerException("No superterm provided.");
        }

        Session session = HibernateUtil.currentSession();

        Criteria crit = session.createCriteria(ExpressionStructure.class);
        if (expressedTerm.getEntity().getSubTerm() != null) {
            OntologyDTO subtermOntology = expressedTerm.getEntity().getSubTerm().getOntology();
            if (subtermOntology == null) {
                throw new NullPointerException("No subterm ontology provided.");
            }
            Criteria subterm = crit.createCriteria("subterm");
            subterm.add(Restrictions.eq("termName", expressedTerm.getEntity().getSubTerm().getTermName()));
        } else {
            crit.add(Restrictions.isNull("subterm"));
        }
        if (expressedTerm.getQualityTerm() != null) {
            if (expressedTerm.getQualityTerm().getTerm() != null) {
                crit.add(Restrictions.eq("eapQualityTerm.zdbID", expressedTerm.getQualityTerm().getTerm().getZdbID()));
            }
            crit.add(Restrictions.eq("tag", expressedTerm.getQualityTerm().getTag()));
        } else {
            crit.add(Restrictions.isNull(("eapQualityTerm")));
        }
        crit.add(Restrictions.eq("expressionFound", expressedTerm.isExpressionFound()));
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
     * @return FishExperiment
     */
    @SuppressWarnings("unchecked")
    public FishExperiment getGenotypeExperimentByGenotypeID(String genotypeID) {
        Session session = HibernateUtil.currentSession();
        String hql = "from FishExperiment as fishox where " +
                " fishox.fish.genotype.zdbID = :genotypeID";
        Query query = session.createQuery(hql);
        query.setParameter("genotypeID", genotypeID);
        return (FishExperiment) query.uniqueResult();
    }

    /**
     * Create all expression structures being used in a given publication.
     *
     * @param publicationID publication id
     */
    @Override
    public void createExpressionPile(String publicationID) {
        List<ExpressionResult2> expressionResults = getAllExpressionResults(publicationID);
        if (expressionResults == null || expressionResults.isEmpty()) {
            return;
        }
        Publication publication = getPublicationRepository().getPublication(publicationID);
        Set<ExpressionStructure> distinctStructures = new HashSet<>();
        for (ExpressionResult2 expressionResult : expressionResults) {
            Set<ExpressionStructure> expressionStructureSet = instantiateExpressionStructure(expressionResult, publication);
            for (ExpressionStructure structure : expressionStructureSet) {
                // only create structures that are not already on the pile.
                if (distinctStructures.add(structure))
                    createPileStructure(structure);
            }
        }

    }

    private Set<ExpressionStructure> instantiateExpressionStructure(ExpressionResult2 expressionResult, Publication publication) {
        Set<ExpressionStructure> structureList = new HashSet<>();
        if (expressionResult.isEap()) {
            for (ExpressionPhenotypeTerm quality : expressionResult.getPhenotypeTermSet()) {
                ExpressionStructure structure = getExpressionStructure(expressionResult, publication);
                structure.setEapQualityTerm(quality.getQualityTerm());
                structure.setTag(quality.getTag());
                structureList.add(structure);
            }
        } else {
            structureList.add(getExpressionStructure(expressionResult, publication));
        }
        return structureList;
    }

    private ExpressionStructure getExpressionStructure(ExpressionResult2 expressionResult, Publication publication) {
        ExpressionStructure structure = new ExpressionStructure();
        structure.setDate(new Date());
        structure.setPerson(ProfileService.getCurrentSecurityUser());
        structure.setPublication(publication);
        structure.setSuperterm(expressionResult.getSuperTerm());
        structure.setExpressionFound(expressionResult.isExpressionFound());
        GenericTerm subTerm = expressionResult.getSubTerm();
        if (subTerm != null) {
            structure.setSubterm(subTerm);
        }
        return structure;
    }

    private List<ExpressionResult2> getAllExpressionResults(String publicationID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct expression from ExpressionResult2 expression where" +
                "          expression.expressionFigureStage.expressionExperiment.publication.id= :publicationID";
        Query query = session.createQuery(hql);
        query.setParameter("publicationID", publicationID);
        return (List<ExpressionResult2>) query.list();

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
     *
     * @param term term
     * @return list of expressions
     */
    @Override
    public List<ExpressionResult> getExpressionsWithEntity(GenericTerm term) {
        String hql = "select distinct expression from ExpressionResult expression where " +
                "(entity.superterm = :term OR entity.subterm = :term) " +
                " AND expressionFound = :expressionFound ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("term", term);
        query.setBoolean("expressionFound", true);
        return (List<ExpressionResult>) query.list();
    }

    /**
     * Retrieve Expressions for a given list of terms.
     *
     * @param terms term
     * @return list of expressions
     */
    @Override
    public List<ExpressionResult> getExpressionsWithEntity(List<GenericTerm> terms) {
        List<ExpressionResult> allExpressions = new ArrayList<>(50);
        for (GenericTerm term : terms) {
            List<ExpressionResult> phenotypes = getExpressionsWithEntity(term);
            allExpressions.addAll(phenotypes);
        }
        List<ExpressionResult> nonDuplicateExpressions = removeDuplicateExpressions(allExpressions);
        Collections.sort(nonDuplicateExpressions);
        return nonDuplicateExpressions;
    }

    private List<ExpressionResult> removeDuplicateExpressions(List<ExpressionResult> allExpressions) {
        Set<ExpressionResult> results = new HashSet<>();
        for (ExpressionResult result : allExpressions) {
            results.add(result);
        }
        List<ExpressionResult> expressionResults = new ArrayList<>(results.size());
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
        List<ExperimentFigureStage> efses = new ArrayList<>();
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
    public List<ExpressionResult> getExpressionResultsByFish(Genotype genotype) {
        Session session = HibernateUtil.currentSession();

        String hql = "select xpRslt from ExpressionResult xpRslt, ExpressionExperiment xpExp, FishExperiment fishox " +
                "      where fishox.fish.genotype.zdbID = :genotypeID " +
                "        and fishox = xpExp.fishExperiment " +
                "        and xpRslt.expressionExperiment = xpExp " +
                "        and xpExp.gene != null";
        Query query = session.createQuery(hql);
        query.setString("genotypeID", genotype.getZdbID());

        return (List<ExpressionResult>) query.list();
    }

    public List<ExpressionResult> getExpressionResultsByFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String hql = "select xpRslt from ExpressionResult xpRslt, ExpressionExperiment xpExp, FishExperiment fishox " +
                "      where fishox.fish = :fish " +
                "        and fishox = xpExp.fishExperiment " +
                "        and xpRslt.expressionExperiment = xpExp " +
                "        and xpExp.gene != null";
        Query query = session.createQuery(hql);
        query.setParameter("fish", fish);

        return (List<ExpressionResult>) query.list();
    }

    public long getExpressionResultsByFishAndPublication(Fish fish, String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select count(result) from ExpressionResult result " +
                "      where  result.expressionExperiment.publication.zdbID = :publicationID" +
                "        and result.expressionExperiment.fishExperiment.fish = :fish ";

        Query query = session.createQuery(hql);
        query.setParameter("fish", fish);
        query.setParameter("publicationID", publicationID);

        return (long) query.uniqueResult();
    }

    public List<String> getExpressionFigureIDsByFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String sql = "select distinct xpatfig_fig_zdb_id " +
                "  from expression_result, expression_pattern_figure, expression_experiment, fish_experiment " +
                " where xpatres_xpatex_zdb_id = xpatex_zdb_id " +
                "   and xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "   and xpatex_genox_zdb_id = genox_zdb_id " +
                "   and genox_fish_zdb_id = :fishID ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("fishID", fish.getZdbID());

        return (List<String>) query.list();
    }

    public List<String> getExpressionPublicationIDsByFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String sql = "select distinct fig_source_zdb_id  " +
                "  from expression_result, expression_pattern_figure, figure, expression_experiment, fish_experiment " +
                " where xpatres_xpatex_zdb_id = xpatex_zdb_id " +
                "   and xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "   and fig_zdb_id = xpatfig_fig_zdb_id " +
                "   and xpatex_genox_zdb_id = genox_zdb_id " +
                "   and genox_fish_zdb_id = :fishID ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("fishID", fish.getZdbID());

        return (List<String>) query.list();
    }

    public List<ExpressedStructurePresentation> getWildTypeExpressionExperiments(String zdbID) {
        String hql = "select " +
                "distinct super_term.term_name as super_name " +
                ", super_term.term_ont_id as super_id " +
                ", sub_term.term_name as sub_name " +
                ", sub_term.term_ont_id as sub_id " +
                "from expression_result er " +
                "join expression_experiment ee on ee.xpatex_zdb_id=er.xpatres_xpatex_zdb_id " +
                "join fish_experiment fe on fe.genox_zdb_id = ee.xpatex_genox_zdb_id " +
                "join fish fish on fish.fish_zdb_id=fe.genox_fish_zdb_id " +
                "join genotype g on g.geno_zdb_id=fish.fish_genotype_zdb_id " +
                "join term super_term on er.xpatres_superterm_zdb_id=super_term.term_zdb_id " +
                "left outer join term sub_term on er.xpatres_subterm_zdb_id = sub_term.term_zdb_id " +
                "where ee.xpatex_gene_zdb_id= :markerZdbID " +
                "and fe.genox_is_std_or_generic_control='t' " +
                "and er.xpatres_expression_found='t' " +
                "and g.geno_is_wildtype='t' " +
                "and (exists (select 'x' from clone where ee.xpatex_probe_feature_zdb_id = clone_mrkr_zdb_id  " +
                "and (clone_problem_type !='Chimeric' or clone_problem_type is null)) " +
                "or ee.xpatex_probe_feature_Zdb_id is null)";

        return (List<ExpressedStructurePresentation>) HibernateUtil.currentSession().createSQLQuery(hql)
                .setResultTransformer(new BasicTransformerAdapter() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        ExpressedStructurePresentation eePresentation = new ExpressedStructurePresentation();
                        eePresentation.setSuperTermName(tuple[0].toString());
                        eePresentation.setSuperTermOntId(tuple[1].toString());

                        if (tuple[2] != null) {
                            eePresentation.setSubTermName(tuple[2].toString());
                            eePresentation.setSubTermOntId(tuple[3].toString());
                        }
                        return eePresentation;
                    }
                })
                .setParameter("markerZdbID", zdbID)
                .list();
    }

    /**
     * Retrieve all expression results for a given Sequence Targeting Reagent
     *
     * @param sequenceTargetingReagent sequenceTargetingReagent
     * @return list of expression results
     */
    public List<ExpressionResult> getExpressionResultsBySequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent) {
        Session session = HibernateUtil.currentSession();


        String hql = "select xpRslt from ExpressionResult xpRslt, ExpressionExperiment xpExp, FishExperiment fishox, CleanExpFastSrch cefs " +
                "        where fishox = xpExp.fishExperiment " +
                "        and xpRslt.expressionExperiment = xpExp " +
                " and xpExp.gene.zdbID like 'ZDB-GENE%'" +
                " and cefs.fishExperiment=fishox" +
                " and cefs.gene=:str";

        Query query = session.createQuery(hql);
        query.setParameter("str", sequenceTargetingReagent);


        List<ExpressionResult> expressionResults = (List<ExpressionResult>) query.list();


        return expressionResults;
    }


    public List<String> getExpressionFigureIDsBySequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent) {
        String sql = " select distinct xpatfig_fig_zdb_id  " +
                "   from expression_result, expression_pattern_figure, expression_experiment, fish_experiment, clean_expression_fast_search  " +
                "  where xpatres_xpatex_zdb_id = xpatex_zdb_id " +
                "    and xpatex_gene_zdb_id like 'ZDB-GENE%' " +
                "    and xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "    and xpatex_genox_zdb_id = genox_zdb_id " +
                "    and cefs_genox_zdb_id = genox_zdb_id " +
                "    and cefs_mrkr_zdb_id = :strID ";

        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("strID", sequenceTargetingReagent.getZdbID());

        return (List<String>) query.list();
    }

    public List<String> getExpressionFigureIDsBySequenceTargetingReagentAndExpressedGene(SequenceTargetingReagent sequenceTargetingReagent, Marker expressedGene) {
        String sql = "select distinct xpatfig_fig_zdb_id  " +
                "  from expression_result, expression_pattern_figure, expression_experiment, fish_experiment, clean_expression_fast_search " +
                " where xpatres_xpatex_zdb_id = xpatex_zdb_id " +
                "   and xpatex_gene_zdb_id like 'ZDB-GENE%' " +
                "   and xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "   and xpatex_gene_zdb_id = :expressedGeneID " +
                "   and xpatex_genox_zdb_id = genox_zdb_id " +
                "   and cefs_genox_zdb_id = genox_zdb_id " +
                "   and cefs_mrkr_zdb_id = :strID ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("strID", sequenceTargetingReagent.getZdbID());
        query.setString("expressedGeneID", expressedGene.getZdbID());
        return (List<String>) query.list();
    }

    public List<String> getExpressionPublicationIDsBySequenceTargetingReagent(SequenceTargetingReagent sequenceTargetingReagent) {
        String sql = "select distinct fig_source_zdb_id  " +
                "  from expression_result, expression_pattern_figure, figure, expression_experiment, fish_experiment, clean_expression_fast_search " +
                " where xpatres_xpatex_zdb_id = xpatex_zdb_id " +
                "   and xpatex_gene_zdb_id like 'ZDB-GENE%' " +
                "   and xpatfig_xpatres_zdb_id = xpatres_zdb_id " +
                "   and fig_zdb_id = xpatfig_fig_zdb_id " +
                "   and xpatex_genox_zdb_id = genox_zdb_id " +
                "   and cefs_genox_zdb_id = genox_zdb_id " +
                "   and cefs_mrkr_zdb_id = :strID ";

        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("strID", sequenceTargetingReagent.getZdbID());
        return (List<String>) query.list();
    }

    /**
     * Retrieve all terms that are used in an expression statement.
     *
     * @return set of expressed Terms.
     */
    @Override
    public Set<String> getAllDistinctExpressionTermIDs() {
        String hql = "select distinct result.entity.superterm.id from ExpressionResult as result";
        List<String> results = HibernateUtil.currentSession().createQuery(hql).list();
        Set<String> expressedTerms = new HashSet<>(2000);
        expressedTerms.addAll(results);
        // sub terms
        hql = "select distinct result.entity.subterm.id from ExpressionResult as result";
        results = HibernateUtil.currentSession().createQuery(hql).list();
        expressedTerms.addAll(results);
        return expressedTerms;
    }

    /**
     * Retrieve all terms that are used in a phenotype statement except pato terms.
     *
     * @return set of expressed Terms.
     */
    @Override
    public Set<String> getAllDistinctPhenotypeTermIDs() {
        String hql = "select distinct pheno.entity.superterm.id from PhenotypeStatement as pheno";
        List<String> results = HibernateUtil.currentSession().createQuery(hql).list();
        Set<String> expressedTerms = new HashSet<>(2000);
        expressedTerms.addAll(results);
        // sub terms
        hql = "select distinct pheno.entity.subterm.id from PhenotypeStatement as pheno";
        results = HibernateUtil.currentSession().createQuery(hql).list();
        expressedTerms.addAll(results);
        // super terms related
        hql = "select distinct pheno.relatedEntity.superterm.id from PhenotypeStatement as pheno";
        results = HibernateUtil.currentSession().createQuery(hql).list();
        expressedTerms.addAll(results);
        hql = "select distinct pheno.relatedEntity.subterm.id from PhenotypeStatement as pheno";
        results = HibernateUtil.currentSession().createQuery(hql).list();
        expressedTerms.addAll(results);
        return expressedTerms;
    }

    /**
     * Retrieve expression results for given super term and stage range.
     *
     * @param range
     * @return
     */
    @Override
    public List<ExpressionResult> getExpressionResultsByTermAndStage(TermFigureStageRange range) {
        String hql = "SELECT distinct result " +
                "FROM " +
                "ExpressionResult result " +
                " WHERE " +
                "result.entity.superterm = :superTerm " +
                "AND result.startStage = :start " +
                "AND result.endStage = :end ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("superTerm", range.getSuperTerm());
        query.setParameter("start", range.getStart());
        query.setParameter("end", range.getEnd());
        return (List<ExpressionResult>) query
                .list();
    }

    @Override
    public ExpressionResult getExpressionResult(Long expressionResultID) {
        return (ExpressionResult) HibernateUtil.currentSession().get(ExpressionResult.class, expressionResultID);
    }

    @Override
    public void deleteExpressionResult(ExpressionResult2 expressionResult) {
        HibernateUtil.currentSession().delete(expressionResult);
    }

    @SuppressWarnings("unchecked")
    public List<GenericTerm> getWildTypeAnatomyExpressionForMarker(String zdbID) {
        String hql = "SELECT distinct ai " +
                "FROM " +
                "ExpressionResult er, GenericTerm ai " +
                " join er.expressionExperiment ee " +
                " join ee.fishExperiment ge " +
                " join ge.fish fish " +
                " join fish.genotype g " +
                " WHERE " +
                "ee.gene.zdbID = :zdbID " +
                "AND er.entity.superterm.oboID = ai.oboID " +
                "AND er.expressionFound = 't' " +
                "AND ge.standard = 't' " +
                "AND g.wildtype= 't' " +
                "ORDER BY ai.termName asc";
        return (List<GenericTerm>) HibernateUtil.currentSession().createQuery(hql)
                .setParameter("zdbID", zdbID)
                .list();
    }

    public List<Figure> getFigures(ExpressionSummaryCriteria expressionCriteria) {
        List<Figure> figures = new ArrayList<Figure>();
        //store strings for createAlias here, then only create what's necessary at the end.
        Map<String, String> aliasMap = new HashMap<String, String>();
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Figure.class);


        //duplicate createAlias statements are ok, so we'll put the necessary
        //aliases in for any set of restrictions.

        if (expressionCriteria.getGene() != null) {
            aliasMap.put("expressionResults", "xpatres");
            aliasMap.put("xpatres.expressionExperiment", "xpatex");
            criteria.add(Restrictions.eq("xpatex.gene", expressionCriteria.getGene()));
            logger.debug("gene: " + expressionCriteria.getGene().getZdbID());
        }

        if (expressionCriteria.getFishExperiment() != null) {
            aliasMap.put("expressionResults", "xpatres");
            aliasMap.put("xpatres.expressionExperiment", "xpatex");
            criteria.add(Restrictions.eq("xpatex.fishExperiment", expressionCriteria.getFishExperiment()));
            logger.debug("genox: " + expressionCriteria.getFishExperiment().getZdbID());
        }

        if (expressionCriteria.getFish() != null) {
            aliasMap.put("expressionResults", "xpatres");
            aliasMap.put("xpatres.expressionExperiment", "xpatex");
            aliasMap.put("xpatex.fishExperiment", "genox");
            criteria.add(Restrictions.eq("genox.fish", expressionCriteria.getFish()));
            logger.debug("fish: " + expressionCriteria.getFish().getZdbID());
        }

        if (expressionCriteria.getAntibody() != null) {
            aliasMap.put("expressionResults", "xpatres");
            aliasMap.put("xpatres.expressionExperiment", "xpatex");
            criteria.add(Restrictions.eq("xpatex.antibody", expressionCriteria.getAntibody()));
            logger.debug("antibody: " + expressionCriteria.getAntibody().getZdbID());
        }

        if (expressionCriteria.getEntity() != null) {

            if (expressionCriteria.getEntity().getSuperterm() != null
                    && expressionCriteria.getEntity().getSubterm() != null) {
                aliasMap.put("expressionResults", "xpatres");
                criteria.add(Restrictions.eq("xpatres.entity.superterm", expressionCriteria.getEntity().getSuperterm()));
                criteria.add(Restrictions.eq("xpatres.entity.subterm", expressionCriteria.getEntity().getSubterm()));
                logger.debug("superterm: " + expressionCriteria.getEntity().getSuperterm().getZdbID());
                logger.debug("subterm:" + expressionCriteria.getEntity().getSuperterm().getZdbID());
            }

            if (expressionCriteria.getEntity().getSuperterm() != null
                    && expressionCriteria.getEntity().getSubterm() == null) {
                aliasMap.put("expressionResults", "xpatres");
                criteria.add(Restrictions.eq("xpatres.entity.superterm", expressionCriteria.getEntity().getSuperterm()));
                criteria.add(Restrictions.isNull("xpatres.entity.subterm"));
                logger.debug("subterm:" + expressionCriteria.getEntity().getSuperterm().getZdbID());
            }

        }


        if (expressionCriteria.getSingleTermEitherPosition() != null) {
            aliasMap.put("expressionResults", "xpatres");
            criteria.add(Restrictions.or(Restrictions.eq("xpatres.entity.superterm", expressionCriteria.getSingleTermEitherPosition()),
                    Restrictions.eq("xpatres.entity.subterm", expressionCriteria.getSingleTermEitherPosition())));
            logger.debug("single term, either position: " + expressionCriteria.getSingleTermEitherPosition().getZdbID());
        }

        if (expressionCriteria.getStart() != null) {
            aliasMap.put("expressionResults", "xpatres");
            criteria.add(Restrictions.eq("xpatres.startStage", expressionCriteria.getStart()));
            logger.debug("start stage: " + expressionCriteria.getStart().getZdbID());
        }

        if (expressionCriteria.getEnd() != null) {
            aliasMap.put("expressionResults", "xpatres");
            criteria.add(Restrictions.eq("xpatres.endStage", expressionCriteria.getEnd()));
            logger.debug("end stage: " + expressionCriteria.getEnd().getZdbID());
        }

        if (expressionCriteria.isWithImagesOnly()) {
            criteria.add(Restrictions.isNotEmpty("images"));
            logger.debug("with images only");
        }

        if (expressionCriteria.isWildtypeOnly()) {
            aliasMap.put("expressionResults", "xpatres");
            aliasMap.put("xpatres.expressionExperiment", "xpatex");
            //aliasMap.put("xpatex.fishExperiment", "genox");
            aliasMap.put("genox.fish", "fish");
            aliasMap.put("fish.genotype", "genotype");
            criteria.add(Restrictions.eq("fish.wildtype", true));
            criteria.add(Restrictions.eq("genotype.wildtype", true));
            logger.debug("wildtype only");
        }

        if (expressionCriteria.isStandardEnvironment()) {
            aliasMap.put("expressionResults", "xpatres");
            aliasMap.put("xpatres.expressionExperiment", "xpatex");
            aliasMap.put("xpatex.fishExperiment", "genox");
            criteria.add(Restrictions.eq("genox.standardOrGenericControl", true));
            logger.debug("standard or generic-control only");
        }

        if (expressionCriteria.isChemicalEnvironment()) {
            aliasMap.put("expressionResults", "xpatres");
            aliasMap.put("xpatres.expressionExperiment", "xpatex");
            aliasMap.put("xpatex.fishExperiment", "genox");
            aliasMap.put("genox.experiment", "exp");
            aliasMap.put("exp.experimentConditions", "cond");
            aliasMap.put("cond.conditionDataType", "cdt");
            criteria.add(Restrictions.eq("cdt.group", "chemical"));
            logger.debug("chemical environments only");
        }

        if (expressionCriteria.isHeatShockEnvironment()) {
            aliasMap.put("expressionResults", "xpatres");
            aliasMap.put("xpatres.expressionExperiment", "xpatex");
            aliasMap.put("xpatex.fishExperiment", "genox");
            aliasMap.put("genox.experiment", "exp");
            aliasMap.put("exp.experimentConditions", "cond");
            aliasMap.put("cond.conditionDataType", "cdt");
            criteria.add(Restrictions.eq("cdt.name", "heat shock"));
            logger.debug("chemical environments only");
        }

        //now add all of the aliases that were marked as necessary above
        for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
            criteria.createAlias(entry.getKey(), entry.getValue());
        }

        logger.debug("getting figures for an ExpressionSummaryCriteria object");
        figures.addAll(criteria.list());
        return figures;
    }


    @Override
    public Set<ExpressionStatement> getExpressionStatements(ExpressionSummaryCriteria expressionCriteria) {
        Set<ExpressionResult> results = new HashSet<ExpressionResult>();
        Set<ExpressionStatement> expressionStatements = new TreeSet<ExpressionStatement>();
        Session session = HibernateUtil.currentSession();

        //store strings for createAlias here, then only create what's necessary at the end.
        Map<String, String> aliasMap = new HashMap<String, String>();

        Criteria criteria = session.createCriteria(ExpressionResult.class);
        Criteria figureCriteria = null;
        //duplicate createAlias statements are ok, so we'll put the necessary
        //aliases in for any set of restrictions.

        if (expressionCriteria.getFigure() != null) {
            //if there were hql, I would do member of..
            if (figureCriteria == null) {
                figureCriteria = criteria.createCriteria("figures", "figure");
            }
            figureCriteria.add(Restrictions.eq("zdbID", expressionCriteria.getFigure().getZdbID()));
            logger.debug("figure: " + expressionCriteria.getFigure().getZdbID());
        }

        if (expressionCriteria.getGene() != null) {
            aliasMap.put("expressionExperiment", "xpatex");
            criteria.add(Restrictions.eq("xpatex.gene", expressionCriteria.getGene()));
            logger.debug("gene: " + expressionCriteria.getGene().getZdbID());
        }

        if (expressionCriteria.getFishExperiment() != null) {
            aliasMap.put("expressionExperiment", "xpatex");
            criteria.add(Restrictions.eq("xpatex.fishExperiment", expressionCriteria.getFishExperiment()));
            logger.debug("genox: " + expressionCriteria.getFishExperiment().getZdbID());
        }

        if (expressionCriteria.getFish() != null) {
            aliasMap.put("xpatex.fishExperiment", "genox");
            criteria.add(Restrictions.eq("genox.fish", expressionCriteria.getFish()));
            logger.debug("fish: " + expressionCriteria.getFish().getZdbID());
        }


        if (expressionCriteria.getAntibody() != null) {
            aliasMap.put("expressionExperiment", "xpatex");
            criteria.add(Restrictions.eq("xpatex.antibody", expressionCriteria.getAntibody()));
            logger.debug("antibody: " + expressionCriteria.getAntibody().getZdbID());
        }


        if (expressionCriteria.getStart() != null) {
            criteria.add(Restrictions.eq("startStage", expressionCriteria.getStart()));
            logger.debug("start stage: " + expressionCriteria.getStart().getZdbID());
        }

        if (expressionCriteria.getEnd() != null) {
            criteria.add(Restrictions.eq("endStage", expressionCriteria.getEnd()));
            logger.debug("end stage: " + expressionCriteria.getEnd().getZdbID());
        }

        if (expressionCriteria.isWithImagesOnly()) {
            if (figureCriteria == null) {
                figureCriteria = criteria.createCriteria("figures", "figure");
            }
            figureCriteria.add(Restrictions.isNotEmpty("images"));
            logger.debug("with images only");
        }

        if (expressionCriteria.isWildtypeOnly()) {
            aliasMap.put("expressionExperiment", "xpatex");
            aliasMap.put("xpatex.fishExperiment", "genox");
            aliasMap.put("genox.fish", "fish");
            aliasMap.put("fish.genotype", "genotype");
            criteria.add(Restrictions.eq("fish.wildtype", true));
            criteria.add(Restrictions.eq("genotype.wildtype", true));
            logger.debug("wildtype only");
        }

        if (expressionCriteria.isStandardEnvironment()) {
            aliasMap.put("expressionExperiment", "xpatex");
            aliasMap.put("xpatex.fishExperiment", "genox");
            criteria.add(Restrictions.eq("genox.standardOrGenericControl", true));
            logger.debug("standard or generic-control only");
        }

        if (expressionCriteria.isChemicalEnvironment()) {
            aliasMap.put("expressionExperiment", "xpatex");
            aliasMap.put("xpatex.fishExperiment", "genox");
            aliasMap.put("genox.experiment", "exp");
            aliasMap.put("exp.experimentConditions", "cond");
            aliasMap.put("cond.conditionDataType", "cdt");
            criteria.add(Restrictions.eq("cdt.group", "chemical"));
            logger.debug("chemical environments only");
        }

        for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
            criteria.createAlias(entry.getKey(), entry.getValue());
        }

        logger.debug("getting terms for an ExpressionSummaryCriteria object");
        results.addAll(criteria.list());
        for (ExpressionResult result : results) {
            ExpressionStatement statement = new ExpressionStatement();
            statement.setEntity(result.getEntity());
            statement.setExpressionFound(result.isExpressionFound());
            expressionStatements.add(statement);
        }

        return expressionStatements;
    }

    public List<ExpressionResult> getExpressionOnSecondaryTerms() {
        Session session = HibernateUtil.currentSession();
        List<ExpressionResult> allExpressions = new ArrayList<>();

        String hql = "select result from ExpressionResult result " +
                "     where result.entity is not null AND result.entity.superterm is not null AND result.entity.superterm.secondary = :secondary";
        Query query = session.createQuery(hql);
        query.setBoolean("secondary", true);

        allExpressions.addAll((List<ExpressionResult>) query.list());

        hql = "select result from ExpressionResult result " +
                "     where result.entity is not null AND result.entity.subterm is not null AND result.entity.subterm.secondary = :secondary";
        Query queryEntitySub = session.createQuery(hql);
        queryEntitySub.setBoolean("secondary", true);
        allExpressions.addAll((List<ExpressionResult>) queryEntitySub.list());

        return allExpressions;
    }

    /**
     * Retrieve list of expression result records that use obsoleted terms in the annotation.
     *
     * @return
     */
    @Override
    public List<ExpressionResult> getExpressionOnObsoletedTerms() {
        Session session = HibernateUtil.currentSession();
        List<ExpressionResult> allExpressions = new ArrayList<>();

        String hql = "select result from ExpressionResult result " +
                "     where result.entity is not null AND result.entity.superterm is not null AND result.entity.superterm.obsolete = :obsolete";
        Query query = session.createQuery(hql);
        query.setBoolean("obsolete", true);

        allExpressions.addAll((List<ExpressionResult>) query.list());

        hql = "select result from ExpressionResult result " +
                "     where result.entity is not null AND result.entity.subterm is not null AND result.entity.subterm.obsolete = :obsolete";
        Query queryEntitySub = session.createQuery(hql);
        queryEntitySub.setBoolean("obsolete", true);
        allExpressions.addAll((List<ExpressionResult>) queryEntitySub.list());

        return allExpressions;
    }

    @Override
    public int getImagesFromPubAndClone(PublicationExpressionBean publicationExpressionBean) {
        String sql = "select count(distinct img_zdb_id) " +
                "             from image, expression_pattern_figure, " +
                "                expression_result, expression_experiment , clone " +
                "	     where " +
                "		img_fig_zdb_id=xpatfig_fig_zdb_id " +
                "             and  xpatfig_xpatres_zdb_id=xpatres_zdb_id " +
                "             and  xpatex_zdb_id=xpatres_xpatex_zdb_id " +
                "             and  clone_mrkr_zdb_id=xpatex_probe_feature_zdb_id " +
                "             and  xpatex_source_zdb_id= :pubZdbId " +
                "             and  xpatex_probe_feature_zdb_id=:probeZdbId";
        return Integer.parseInt(
                HibernateUtil.currentSession().createSQLQuery(sql)
                        .setParameter("pubZdbId", publicationExpressionBean.getPublicationZdbID())
                        .setParameter("probeZdbId", publicationExpressionBean.getProbeFeatureZdbId())
                        .uniqueResult().toString()
        );
    }

    @Override
    public int getImagesForEfg(PublicationExpressionBean publicationExpressionBean) {
        String sql = "select count(distinct img_zdb_id) " +
                "             from image, expression_pattern_figure, " +
                "                expression_result, expression_experiment  " +
                "	     where img_fig_zdb_id=xpatfig_fig_zdb_id " +
                "             and  xpatfig_xpatres_zdb_id=xpatres_zdb_id " +
                "             and  xpatex_zdb_id=xpatres_xpatex_zdb_id " +
                "             and  xpatex_source_zdb_id= :pubZdbId ";
        return Integer.parseInt(
                HibernateUtil.currentSession().createSQLQuery(sql)
                        .setParameter("pubZdbId", publicationExpressionBean.getPublicationZdbID())
                        .uniqueResult().toString()
        );
    }


    @Override
    public StageExpressionPresentation getStageExpressionForMarker(String zdbID) {

        StageExpressionPresentation stageExpressionPresentation = new StageExpressionPresentation();
        String hql = " select er.startStage " +
                "FROM " +
                "ExpressionResult er " +
                " join er.expressionExperiment ee " +
                " join ee.fishExperiment fe " +
                " join fe.fish fish " +
                " join fish.genotype g " +
                " WHERE " +
                "ee.gene.zdbID = :zdbID " +
                "AND er.expressionFound = 't' " +
                "AND fe.standardOrGenericControl = 't' " +
                "AND g.wildtype= 't' " +
                "AND not exists (from Clone as clone " +
                "where ee.probe = clone and clone.problem = :chimeric ) " +
                "order by er.startStage.hoursStart asc ";
        DevelopmentStage startStage = (DevelopmentStage) HibernateUtil.currentSession()
                .createQuery(hql)
                .setString("zdbID", zdbID)
                .setParameter("chimeric", Clone.ProblemType.CHIMERIC)
                .setMaxResults(1)
                .uniqueResult();

        String hql2 = " select er.endStage  " +
                "FROM " +
                "ExpressionResult er " +
                " join er.expressionExperiment ee " +
                " join ee.fishExperiment fe " +
                " join fe.fish fish " +
                " join fish.genotype g " +
                " WHERE " +
                "ee.gene.zdbID = :zdbID " +
                "AND er.expressionFound = 't' " +
                "AND fe.standardOrGenericControl = 't' " +
                "AND g.wildtype= 't' " +
                "AND not exists (from Clone as clone " +
                "where ee.probe = clone and clone.problem = :chimeric ) " +
                "order by er.endStage.hoursEnd desc  ";
        DevelopmentStage endStage = (DevelopmentStage) HibernateUtil.currentSession()
                .createQuery(hql2)
                .setString("zdbID", zdbID)
                .setParameter("chimeric", Clone.ProblemType.CHIMERIC)
                .setMaxResults(1)
                .uniqueResult();

        stageExpressionPresentation.setStartStage(startStage);
        stageExpressionPresentation.setEndStage(endStage);

        return stageExpressionPresentation;
    }

    @Override
    public List<ExpressionExperiment> getExpressionExperimentByGene(Marker gene) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ExpressionExperiment.class);
        criteria.add(Restrictions.eq("gene", gene));
        return (List<ExpressionExperiment>) criteria.list();
    }

    @Override
    public long getExpressionExperimentByFishAndPublication(Fish fish, String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select count(*) from ExpressionExperiment " +
                "      where  publication.zdbID = :publicationID" +
                "        and fishExperiment.fish = :fish ";

        Query query = session.createQuery(hql);
        query.setParameter("fish", fish);
        query.setParameter("publicationID", publicationID);

        return (long) query.uniqueResult();
    }

    @Override
    public List<ExpressionExperiment2> getExperiments2(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select experiment from ExpressionExperiment2 experiment" +
                "       left join experiment.gene as gene " +
                "     where experiment.publication.zdbID = :pubID " +
                "    order by gene.abbreviationOrder, " +
                "             experiment.fishExperiment.fish.name, " +
                "             experiment.assay.displayOrder ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);

        return (List<ExpressionExperiment2>) query.list();
    }

    @Override
    public void createExpressionFigureStage(ExpressionFigureStage expressionFigureStage) {
        Session session = HibernateUtil.currentSession();
        session.save(expressionFigureStage);
    }

    @Override
    public List<ExpressionResult2> getPhenotypeFromExpressionsByFigureFish(String publicationID, String figureID, String fishID, String featureID) {
        String hql = "select result from ExpressionResult2 result " +
                "     where result.expressionFigureStage.expressionExperiment.publication.zdbID = :pubID " +
                "     and result.phenotypeTermSet IS NOT EMPTY ";
        if (StringUtils.isNotEmpty(figureID))
            hql += "     and result.expressionFigureStage.figure.zdbID = :figID ";
        if (StringUtils.isNotEmpty(fishID))
            hql += "     and result.expressionFigureStage.expressionExperiment.fishExperiment.fish.zdbID = :fishID ";
        hql += "    order by result.expressionFigureStage.figure.orderingLabel, " +
                "             result.expressionFigureStage.expressionExperiment.fishExperiment.fish.nameOrder, " +
                "             result.expressionFigureStage.startStage.abbreviation ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setString("pubID", publicationID);
        if (StringUtils.isNotEmpty(figureID))
            query.setString("figID", figureID);
        if (StringUtils.isNotEmpty(fishID))
            query.setString("fishID", fishID);

        return (List<ExpressionResult2>) query.list();
    }

    @Override
    public List<ExpressionExperiment2> getExperiment2sByAntibody(Antibody antibody) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ExpressionExperiment2.class);
        criteria.add(Restrictions.eq("antibody", antibody));
        return (List<ExpressionExperiment2>) criteria.list();
    }

    @Override
    public List<ExpressionExperiment2> getExpressionExperiment2sByFish(Fish fish) {
        Session session = HibernateUtil.currentSession();

        String hql = "select xpExp from ExpressionExperiment2 xpExp, FishExperiment fishox " +
                "      where fishox.fish = :fish " +
                "        and fishox = xpExp.fishExperiment ";
        Query query = session.createQuery(hql);
        query.setParameter("fish", fish);

        return (List<ExpressionExperiment2>) query.list();
    }

    @Override
    public List<ExpressionResult2> getExpressionResultList(Marker gene) {
        Session session = HibernateUtil.currentSession();

        String hql = "select result from ExpressionResult2 as result " +
                "      where expressionFigureStage.expressionExperiment.gene = :gene " +
                "AND result.phenotypeTermSet is not empty ";
        Query query = session.createQuery(hql);
        query.setParameter("gene", gene);

        return (List<ExpressionResult2>) query.list();
    }
}
