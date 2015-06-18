package org.zfin.publication.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.springframework.stereotype.Repository;
import org.zfin.antibody.Antibody;
import org.zfin.curation.Curation;
import org.zfin.database.SearchUtil;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.marker.*;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.orthology.OrthoEvidenceDisplay;
import org.zfin.orthology.Orthology;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.DOIAttempt;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;

import java.util.*;

/**
 * ToDO: include documentation
 */
@Repository
public class HibernatePublicationRepository extends PaginationUtil implements PublicationRepository {

    Logger logger = Logger.getLogger(HibernatePublicationRepository.class);

    public int getNumberOfPublications(String abstractText) {

        Criteria query = HibernateUtil.currentSession().createCriteria(Publication.class);
        query.add(Restrictions.ilike("abstractText", abstractText, MatchMode.ANYWHERE));
        query.setProjection(Projections.count("abstractText"));
        return ((Number) query.uniqueResult()).intValue();
    }

    /**
     * Retrieve all distinct publications that contain a high quality probe
     * with a rating of 4.
     *
     * @param anatomyTerm Anatomy Term
     * @return list of publications
     */
    public List<Publication> getHighQualityProbePublications(GenericTerm anatomyTerm) {
        Session session = HibernateUtil.currentSession();
        String hql = "SELECT distinct exp.publication FROM ExpressionExperiment exp, ExpressionResult res, Clone clone   " +
                "WHERE res.entity.superterm = :aoTerm " +
                "AND res.expressionExperiment = exp " +
                "AND exp.probe = clone " +
                "AND clone.rating = 4 " +
                "AND clone.problem is null";
        Query query = session.createQuery(hql);
        query.setParameter("aoTerm", anatomyTerm);
        List<Publication> list = query.list();
        return list;
    }

    public List<Publication> getExpressedGenePublications(String geneID, String anatomyItemID) {
        Session session = HibernateUtil.currentSession();
        String hql = "SELECT distinct publication FROM Publication publication, ExpressionExperiment exp, ExpressionResult res   " +
                "WHERE (res.entity.superterm.zdbID = :aoZdbID OR" +
                "       res.entity.subterm.zdbID = :aoZdbID) " +
                "AND publication = exp.publication " +
                "AND res.expressionExperiment = exp " +
                "AND exp.gene.zdbID = :zdbID " +
                "AND res.expressionFound = :expressionFound ";
        String sql = addOrderByParameters(hql);
        Query query = session.createQuery(sql);
        addPaginationParameters(query);
        query.setString("zdbID", geneID);
        query.setString("aoZdbID", anatomyItemID);
        query.setBoolean("expressionFound", true);
        List<Publication> list = query.list();
        return list;
    }


    public List<String> getSNPPublicationIDs(Marker marker) {
        Session session = HibernateUtil.currentSession();
        String sql = "select distinct snpdattr_pub_zdb_id " +
                " from snp_download_attribution, snp_download " +
                " where snpdattr_snpd_pk_id = snpd_pk_id and snpd_mrkr_zdb_id = :zdbID";
        SQLQuery query = session.createSQLQuery(sql);
        query.setString("zdbID", marker.getZdbID());
        List<String> pubIDs = query.list();
        return pubIDs;
    }

    /*
    public List<Publication> getSNPPublications(Marker marker) {
       Session session = HibernateUtil.currentSession();
        String hql = "SELECT distinct publication FROM Publication publication, ExpressionExperiment exp, ExpressionResult res, Marker marker   " +
                "WHERE res.anatomyTerm.zdbID = :aoZdbID " +
                "AND publication.zdbID = exp.publicationID " +
                "AND res.expressionExperiment = exp " +
                "AND marker.zdbID = exp.geneID " +
                "AND marker.zdbID = :zdbID ";
        String sql = addOrderByParameters(hql);
        Query query = session.createQuery(sql);
        query.setString("zdbID", marker.getZdbID());
        List<Publication> list = query.list();
        return list;
    }     */


    private String addOrderByParameters(String hql) {
        if (!isUsePagination()) {
            return hql;
        }
        StringBuilder sb = new StringBuilder(hql);
        sb.append(getOrderByClause());
        return sb.toString();
    }

    public int getNumberOfExpressedGenePublicationsWithFigures(String geneID, String anatomyItemID) {
        Session session = HibernateUtil.currentSession();
        String hql = "SELECT count(distinct exp.publication) " +
                "FROM ExpressionExperiment exp, ExpressionResult res " +
                "WHERE res.entity.superterm.zdbID = :aoZdbID " +
                "AND res.expressionExperiment = exp " +
                "AND res.figures is not empty " +
                "AND exp.gene.zdbID = :zdbID ";
        Query query = session.createQuery(hql);
        query.setString("zdbID", geneID);
        query.setString("aoZdbID", anatomyItemID);
        return ((Number) query.uniqueResult()).intValue();
    }

    private void addPaginationParameters(Query query) {
        if (isUsePagination()) {
            query.setFirstResult(getFirstRow() - 1);
            query.setMaxResults(getMaxDisplayRows());
        }
    }

    /**
     * Retrieve all publication that are annoted to genes expressed in a given
     * anatomical structure.
     *
     * @param anatomyItemID anatomical structure
     */
    public List<Publication> getExpressedGenePublications(String anatomyItemID) {
        Session session = HibernateUtil.currentSession();
        Criteria query = session.createCriteria(Marker.class);
        query.add(Restrictions.eq("symb", ""));
        List<Publication> list = query.list();
        return list;
    }

    /**
     * Retrieve the genes and CDNA/EST for the high-quality probes with
     * rating of 4.
     *
     * @param term AnatomyTerm
     * @return list of High quality probes.
     */
    public PaginationResult<HighQualityProbe> getHighQualityProbeNames(GenericTerm term) {
        return getHighQualityProbeNames(term, Integer.MAX_VALUE);
    }


    public PaginationResult<HighQualityProbe> getHighQualityProbeNames(Term term, int maxRow) {

        String hql = "select distinct exp.probe, marker " +
                "FROM ExpressionExperiment exp, ExpressionResult res, Marker marker " +
                "WHERE  res.entity.superterm = :term " +
                "AND res.expressionExperiment = exp " +
                "AND exp.probe.rating = 4 " +
                "AND exp.gene = marker " +
                "ORDER by marker.abbreviationOrder  ";
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery(hql);
        query.setParameter("term", term);
        ScrollableResults results = query.scroll();


        List<Object[]> list = new ArrayList<Object[]>();
        while (results.next() && results.getRowNumber() < maxRow) {
            list.add(results.get());
        }

        int totalCount = 0;
        if (results.last()) {
            totalCount = results.getRowNumber() + 1;
        }

        results.close();

        List<HighQualityProbe> probes = createHighQualityProbeObjects(list, term);
        return new PaginationResult<HighQualityProbe>(totalCount, probes);

    }

    @SuppressWarnings({"unchecked"})
    public List<Marker> getAllExpressedMarkers(String zdbID, int maxRow) {
        Session session = HibernateUtil.currentSession();

//        String hql = "SELECT distinct marker FROM Marker marker, ExpressionExperiment exp, ExpressionResult res  " +
//                "WHERE res.anatomyTerm.zdbID = :zdbID " +
//                "AND res.expressionExperiment = exp " +
//                "AND marker.zdbID = exp.geneID " +
//                "ORDER BY marker.abbreviationOrder";

        String hql = "SELECT distinct marker FROM Marker marker, ExpressionResult res  " +
                "WHERE (res.entity.superterm.zdbID = :zdbID OR res.entity.subterm.zdbID = :zdbID )" +
                "AND res.expressionExperiment.gene.zdbID = marker.zdbID " +
                "ORDER BY marker.abbreviationOrder";

        Query query = session.createQuery(hql);
        if (maxRow != SearchUtil.ALL) {
            query.setMaxResults(maxRow);
        }
        query.setString("zdbID", zdbID);
        return (List<Marker>) query.list();
    }


    public PaginationResult<MarkerStatistic> getAllExpressedMarkers(GenericTerm anatomyTerm) {
        return getAllExpressedMarkers(anatomyTerm, 0, Integer.MAX_VALUE);
    }

    /**
     * Note: firstRow must be 1 or greater, i.e. the way a user would describes
     * the record number. Hibernate starts with the first row numbered '0'.
     * Do not include records where the gene or probe is WITHDRAWN.
     * Do not include records where the probe is Chimeric.
     * Written in native SQL because need to order by number of figures.
     *
     * @param anatomyTerm     anatomy term
     * @param firstRow        first row
     * @param numberOfRecords number
     */
    public PaginationResult<MarkerStatistic> getAllExpressedMarkers(GenericTerm anatomyTerm, int firstRow, int numberOfRecords) {
        if (firstRow < 0) {
            throw new RuntimeException("First Row number <" + firstRow + "> is invalid");
        }
        // Hibernate starts at 0 while the argument expects to start at 1

        Session session = HibernateUtil.currentSession();

        // todo: Rewrite as HQL and include the whole marker object as it is needed.
        // todo: note that when in SQL, start at 1 (current) , but when in HQL, start at 0
        String hql = "SELECT exp.xpatex_gene_zdb_id as geneID, gene.mrkr_abbrev as geneSymbol, " +
                "count(distinct fig.fig_zdb_id) as numOfFig  " +
                "FROM  Expression_Experiment exp, outer marker probe, Term item_, Marker gene, Figure fig," +
                "      Genotype geno, fish_Experiment genox, expression_pattern_figure results, expression_result result, fish fish " +
                "WHERE  exp.xpatex_probe_feature_zdb_id = probe.mrkr_zdb_id AND" +
                "       exp.xpatex_gene_zdb_id = gene.mrkr_zdb_id AND         " +
                "       item_.term_zdb_id = :termID AND " +
                "       result.xpatres_xpatex_zdb_id = exp.xpatex_zdb_id AND " +
                "       (result.xpatres_superterm_zdb_id = item_.term_zdb_id OR result.xpatres_subterm_zdb_id = item_.term_zdb_id) AND " +
                "       result.xpatres_expression_found = :expressionFound AND " +
                "       fig.fig_zdb_id=results.xpatfig_fig_zdb_id AND " +
                "       results.xpatfig_xpatres_zdb_id=result.xpatres_zdb_id AND " +
                "       genox.genox_zdb_id=exp.xpatex_genox_zdb_id AND " +
                "       genox.genox_fish_zdb_id=fish.fish_zdb_id AND " +
                "       fish.fish_genotype_zdb_id=geno.geno_zdb_id AND " +
                "       geno.geno_is_wildtype = :isWildtype AND " +
                "       exp.xpatex_gene_zdb_id = gene.mrkr_zdb_id AND " +
                "       genox.genox_is_std_or_generic_control = :condition AND " +
                "       gene.mrkr_abbrev[1,10] <> :withdrawn  AND   " +
                "       not exists( " +
                "           select 'x' from clone " +
                "           where clone.clone_mrkr_zdb_id = exp.xpatex_probe_feature_zdb_id " +
                "           and clone.clone_problem_type = :chimeric " +
                "       ) AND " +
                // todo: fix this query
                "       not exists( " +
                "           select 'x' from marker m2" +
                "           where m2.mrkr_zdb_id = exp.xpatex_probe_feature_zdb_id " +
                "           and m2.mrkr_abbrev[1,10] = :withdrawn  " +
                "       ) " +
                "GROUP BY exp.xpatex_gene_zdb_id, gene.mrkr_abbrev " +
                "ORDER BY numOfFig DESC, geneSymbol ";
        SQLQuery query = session.createSQLQuery(hql);
        query.addScalar("geneID", Hibernate.STRING);
        query.addScalar("geneSymbol", Hibernate.STRING);
        query.addScalar("numOfFig", Hibernate.INTEGER);
        query.setParameter("termID", anatomyTerm.getZdbID());
        query.setBoolean("expressionFound", true);
        query.setBoolean("isWildtype", true);
        query.setBoolean("condition", true);
        query.setString("withdrawn", Marker.WITHDRAWN);
        query.setString("chimeric", Clone.ProblemType.CHIMERIC.toString()); // todo: use enum here
        ScrollableResults results = query.scroll();
        results.last();
        int totalResults = results.getRowNumber() + 1;

        List<Object[]> list = new ArrayList<Object[]>();

        results.beforeFirst();
        while (results.next() && results.getRowNumber() < numberOfRecords) {
            if (results.getRowNumber() >= firstRow) {
                list.add(results.get());
            }
        }

        results.close();
        List<MarkerStatistic> markerStatistics = createMarkerStatistics(list, anatomyTerm);

        return new PaginationResult<MarkerStatistic>(totalResults, markerStatistics);
    }

    /**
     * Count the number of figures from all publications that have a gene
     * expression in a given anatomy structure.
     *
     * @param anatomyTerm Anatomy Term
     * @return number
     */
    public int getTotalNumberOfFiguresPerAnatomyItem(GenericTerm anatomyTerm) {

        Session session = HibernateUtil.currentSession();

        String sql = "select count(distinct fig_zdb_id) " +
                "from figure" +
                "     join expression_pattern_figure on xpatfig_fig_zdb_id = fig_zdb_id " +
                "     join expression_result on xpatres_zdb_id = xpatfig_xpatres_zdb_id " +
                "     join expression_experiment on xpatex_zdb_id = xpatres_xpatex_zdb_id " +
                "     join fish_experiment on genox_zdb_id = xpatex_genox_zdb_id " +
                "     join marker on mrkr_zdb_id = xpatex_gene_zdb_id " +
                "where (xpatres_superterm_zdb_id = :termZdbId or xpatres_subterm_zdb_id = :termZdbId) " +
                "   and xpatres_expression_found = :expressionFound " +
                "   and genox_is_std_or_generic_control = :condition " +
                "   and mrkr_abbrev not like :withdrawn " +
                "   and not exists (select 'x' from clone " +
                "                   where clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id " +
                "                     and clone_problem_type <> :chimeric); ";





/*
        String hql = "select count(distinct fig) from Figure fig, ExpressionResult res " +
                "WHERE (res.entity.superterm = :aoTerm OR res.entity.subterm = :aoTerm) " +
                "AND fig member of res.figures " +
                "AND res.expressionFound = :expressionFound " +
                "AND res.expressionExperiment.genotypeExperiment.standardOrGenericControl = :condition " +
                "AND res.expressionExperiment.gene.abbreviation not like :withdrawn " +
                "AND not exists ( " +
                "select 1 from Clone clone where " +
                "           clone.zdbID = res.expressionExperiment.probe.zdbID AND " +
                "           clone.problem  <> :chimeric " +
                ")";
*/
//        Query query = session.createQuery(hql);

        Query query = session.createSQLQuery(sql);

        query.setBoolean("expressionFound", true);
        query.setParameter("termZdbId", anatomyTerm.getZdbID());
        query.setBoolean("condition", true);
        query.setString("withdrawn", Marker.WITHDRAWN + "%");
        query.setString("chimeric", Clone.ProblemType.CHIMERIC.toString());
        return ((Number) query.uniqueResult()).intValue();
    }

    private List<MarkerStatistic> createMarkerStatistics(List<Object[]> list, GenericTerm anatomyTerm) {
        if (list == null) {
            return null;
        }

        List<MarkerStatistic> markers = new ArrayList<MarkerStatistic>();
        for (Object[] stats : list) {
            String markerZdbID = (String) stats[0];
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            Marker marker = mr.getMarkerByID(markerZdbID);
            MarkerStatistic statistic = new MarkerStatistic(anatomyTerm, marker);
            statistic.setNumberOfFigures((Integer) stats[2]);
            //statistic.setNumberOfPublications(getNumberOfExpressedGenePublicationsWithFigures(marker.getZdbID(), anatomyTerm.getZdbID()));
            markers.add(statistic);
        }
        return markers;
    }


    public Publication getPublication(String zdbID) {
        Session session = HibernateUtil.currentSession();
        // this will automatically close the session
//        Publication pub = (Publication) session.load(Publication.class, zdbID);
        Criteria query = session.createCriteria(Publication.class);
        query.add(Restrictions.eq("zdbID", zdbID.toUpperCase()));
        Publication pub = (Publication) query.uniqueResult();
        return pub;
    }

    public Marker getMarker(String symbol) {
        Session session = HibernateUtil.currentSession();
        Criteria query = session.createCriteria(Marker.class);
        query.add(Restrictions.eq("abbreviation", symbol));
        Marker pub = (Marker) query.uniqueResult();
        return pub;
    }

    public Marker getMarkerByZdbID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return (Marker) session.load(Marker.class, zdbID);
    }

    public boolean publicationExists(String canonicalPublicationZdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria query = session.createCriteria(Publication.class);
        query.setProjection(Projections.count("zdbID"));
        query.add(Restrictions.eq("zdbID", canonicalPublicationZdbID));
        return (1 == ((Number) query.uniqueResult()).intValue());
    }


    @SuppressWarnings("unchecked")
    public Figure getFigureById(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return (Figure) session.get(Figure.class, zdbID);
    }

    public Image getImageById(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return (Image) session.get(Image.class, zdbID);

    }

    public List<Figure> getFiguresByGeneID(String geneID, String publicationID) {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(Figure.class);
        Criteria critRes = crit.createCriteria("expressionResults");
        Criteria critExp = critRes.createCriteria("expressionExperiment");
        critExp.add(Restrictions.eq("publicationID", publicationID));
        return (List<Figure>) crit.list();
    }

    @SuppressWarnings("unchecked")
    public List<Figure> getFiguresByProbeAndPublication(String probeID, String publicationID) {
        Session session = HibernateUtil.currentSession();


        String hql = "select figure from Figure figure, ExpressionExperiment exp, ExpressionResult res " +
                "where exp.publication.zdbID = :pubID AND " +
                "      exp.probe.zdbID = :cloneID AND " +
                "      res member of exp.expressionResults AND " +
                "      figure member of res.figures " +
                "order by figure.orderingLabel   ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        query.setString("cloneID", probeID);
        // Only pick out the distinct records.
        query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
        List<Figure> figures = query.list();

        return figures;
    }

    public Figure getFigureByID(String figureZdbID) {
        Session session = HibernateUtil.currentSession();
        return (Figure) session.get(Figure.class, figureZdbID);
    }


    @SuppressWarnings("unchecked")
    public List<Figure> getFiguresByGeneAndPublication(String geneID, String publicationID) {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(Figure.class);
        Criteria critRes = crit.createCriteria("results");
        Criteria critExp = critRes.createCriteria("experiment");
        critExp.add(Restrictions.eq("publicationID", publicationID));
        Criteria critMarker = critExp.createCriteria("marker");
        critMarker.add(Restrictions.eq("zdbID", geneID));
        // Only pick out the distinct records.
        crit.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
        List<Figure> figures = crit.list();
        return figures;
    }

    public List<FeatureMarkerRelationship> getFeatureMarkerRelationshipsByPubID(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fmRel, fmRel.feature.name from FeatureMarkerRelationship fmRel, PublicationAttribution attr " +
                "      where attr.publication.zdbID = :pubID " +
                "      and attr.dataZdbID=fmRel.feature.zdbID " +
                // "      and ftr.type=:tg " +
                "      order by fmRel.feature.name";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        // query.setString("tg", "TRANSGENIC_INSERTION");

        List<Object[]> array = (List<Object[]>) query.list();
        List<FeatureMarkerRelationship> fmrelList = new ArrayList<>(array.size());
        for (Object[] arrayObject : array) {
            fmrelList.add((FeatureMarkerRelationship) arrayObject[0]);
        }
        return fmrelList;


    }

    private List<HighQualityProbe> createHighQualityProbeObjects(List<Object[]> list, Term aoTerm) {
        List<HighQualityProbe> probes = new ArrayList<HighQualityProbe>();
        if (list != null) {
            for (Object[] array : list) {
                Marker subGene = (Marker) array[0];
                Marker gene = (Marker) array[1];
                HighQualityProbe probe = new HighQualityProbe(subGene, aoTerm);
                probe.addGene(gene);
                probes.add(probe);
                //probe.add(getFiguresPerProbeAndAnatomy(gene, subGene, aoTerm));
            }
        }
        return probes;
    }

    /**
     * Return all figures for a specified gene, probe and anatommical structure.
     * Clone information is not required.
     *
     * @param gene   Gene
     * @param clone  Probe
     * @param aoTerm anatomical structure
     * @return list of figures
     */
    @SuppressWarnings("unchecked")
    public List<Figure> getFiguresPerProbeAndAnatomy(Marker gene, Marker clone, GenericTerm aoTerm) {
        Session session = HibernateUtil.currentSession();

        StringBuilder hql = new StringBuilder("select figure from Figure figure, ExpressionExperiment exp, ");
        hql.append("ExpressionResult res ");
        hql.append("where exp.gene = :gene AND ");
        if (clone != null) {
            hql.append("      exp.probe = :clone AND ");
        }
        hql.append("      res member of exp.expressionResults AND ");
        hql.append("      res.expressionFound = :expressionFound  AND ");
        hql.append("      res.entity.superterm = :term AND ");
        hql.append("      figure member of res.figures ");
        hql.append("order by figure.orderingLabel    ");
        Query query = session.createQuery(hql.toString());
        query.setParameter("gene", gene);
        if (clone != null) {
            query.setParameter("clone", clone);
        }
        query.setParameter("term", aoTerm);
        query.setBoolean("expressionFound", true);
        List<Figure> figures = query.list();
        return figures;
    }

    @SuppressWarnings("unchecked")
    public List<Publication> getPublicationsWithFiguresPerProbeAndAnatomy(Marker gene, Marker subGene, GenericTerm aoTerm) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct figure.publication from Figure figure, ExpressionExperiment exp, ExpressionResult res " +
                "where exp.gene.zdbID = :geneID AND " +
                "      exp.probe.zdbID = :cloneID AND " +
                "      res member of exp.expressionResults AND " +
                "      res.expressionFound = :expressionFound  AND " +
                "      res.entity.superterm = :term AND " +
                "      figure member of res.figures ";
        Query query = session.createQuery(hql);
        query.setString("geneID", gene.getZdbID());
        query.setString("cloneID", subGene.getZdbID());
        query.setParameter("term", aoTerm);
        query.setBoolean("expressionFound", true);
        return (List<Publication>) query.list();
    }


    public boolean updatePublications(List<Publication> publicationList) {

        Session session = HibernateUtil.currentSession();

        for (Publication publication : publicationList) {
            if (publication.getDoi() != null) {
                session.saveOrUpdate(publication);
                DOIAttempt doiAttempt = getDoiAttempt(publication);
                if (doiAttempt != null) {
                    session.delete(getDoiAttempt(publication));
                }
            }
        }

        session.flush();
        return true;
    }

    private DOIAttempt getDoiAttempt(Publication publication) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("from DOIAttempt " +
                "where publication = :pub");
        query.setParameter("pub", publication);
        return (DOIAttempt) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<Figure> getFiguresBySequenceTargetingReagentAndAnatomy(SequenceTargetingReagent sequenceTargetingReagent, GenericTerm term) {
        Session session = HibernateUtil.currentSession();

        StringBuilder hql = new StringBuilder("select figure ");
        getBaseQueryForSequenceTargetingReagentFigureData(hql);
        hql.append("order by figure.orderingLabel    ");
        Query query = session.createQuery(hql.toString());
        query.setString("markerID", sequenceTargetingReagent.getZdbID());
        query.setParameter("term", term);
        return (List<Figure>) query.list();
    }

    private void getBaseQueryForSequenceTargetingReagentFigureData(StringBuilder hql) {
        hql.append("from Figure figure, PhenotypeStatement phenotype, ");
        hql.append("FishExperiment fishox, Marker marker, Experiment exp, ExperimentCondition con ");
        hql.append("where marker.zdbID = :markerID AND ");
        hql.append("      fishox.experiment = exp AND ");
        hql.append("      con.experiment = exp AND  ");
        hql.append("      marker = con.sequenceTargetingReagent AND  ");
        hql.append("      phenotype.phenotypeExperiment.fishExperiment = geno AND  ");
        hql.append("      phenotype.phenotypeExperiment.figure = figure AND ");
        hql.append("      ( phenotype.entity.superterm = :term OR phenotype.entity.subterm = :term  OR" +
                "           phenotype.relatedEntity.superterm = :term OR phenotype.relatedEntity.subterm = :term ) ");
    }

    /**
     * Retrieve list of figures for a given genotype and anatomy term
     * for mutant genotypes excluding sequenceTargetingReagent.
     *
     * @param fish genotype
     * @param term anatomy term
     * @return list of figures.
     */
    @SuppressWarnings("unchecked")
    public PaginationResult<Figure> getFiguresByFishAndAnatomy(Fish fish, GenericTerm term, boolean includeSubstructures) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct figure from Figure figure, PhenotypeStatement phenos, " +
                "FishExperiment fishox, TransitiveClosure transitiveClosure " +
                "where fishox.fish = :fish AND " +
                "      phenos.phenotypeExperiment.fishExperiment = fishox  AND " +
                "      phenos.phenotypeExperiment.figure = figure AND " +
                "      transitiveClosure.root = :aoTerm AND " +
                "      ( phenos.entity.superterm = transitiveClosure.child OR phenos.entity.subterm = transitiveClosure.child OR " +
                "        phenos.relatedEntity.superterm = transitiveClosure.child OR phenos.relatedEntity.subterm = transitiveClosure.child) " +
                "      and 0 = all elements(fishox.fish.strList) " +
                "order by figure.orderingLabel    ";
        Query query = session.createQuery(hql);
        query.setParameter("fish", fish);
        query.setParameter("aoTerm", term);
        PaginationResult<Figure> paginationResult = new PaginationResult<Figure>(query.list());
        return paginationResult;
    }

    /**
     * Retrieve list of figures for a given genotype and anatomy term
     * for mutant genotypes excluding sequenceTargetingReagent.
     *
     * @param fish genotype
     * @param term anatomy term
     * @return list of figures.
     */
    @Override
    public PaginationResult<Figure> getFiguresByFishAndAnatomy(Fish fish, GenericTerm term) {
        return getFiguresByFishAndAnatomy(fish, term, false);
    }

    public PaginationResult<Figure> getFiguresByGeno(Genotype geno) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct figure from Figure figure, GenotypeFigure genofig " +
                "where genofig.genotype.zdbID = :genoID AND " +
                "      genofig.figure.zdbID=figure.id " +
                "order by figure.orderingLabel    ";

        Query query = session.createQuery(hql);
        query.setString("genoID", geno.getZdbID());

        PaginationResult<Figure> paginationResult = new PaginationResult<Figure>(query.list());
        return paginationResult;
    }

    public PaginationResult<Figure> getFiguresByGenoExp(Genotype geno) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct figure from Figure figure, ExpressionResult res, ExpressionExperiment exp," +
                "FishExperiment fishox, Genotype geno, ExpressionResultFigure xpatfig " +
                "where geno.zdbID = :genoID AND " +
                "      fishox.fish.genotype = geno AND " +
                "   res.expressionExperiment = exp AND " +
                "   xpatfig.expressionResult = res AND " +
                "   xpatfig.figure = figure AND " +
                "   exp.antibody is null AND " +
                "   exp.genotypeExperiment = genox AND " +
                "order by figure.orderingLabel    ";
        Query query = session.createQuery(hql);
        query.setString("genoID", geno.getZdbID());
        PaginationResult<Figure> paginationResult = new PaginationResult<Figure>(query.list());
        return paginationResult;
    }

    /**
     * Retrieve publications that have phenotype data for a given term and genotype
     *
     * @param genotype Genotype
     * @param fish
     * @param aoTerm   ao term  @return Number of publications with figures per genotype and anatomy
     */
    public PaginationResult<Publication> getPublicationsWithFigures(Fish fish, GenericTerm aoTerm, boolean includeSubstructures) {
        Session session = HibernateUtil.currentSession();
        String hql = "select publication from Publication as publication, PhenotypeStatement as phenotype, " +
                "     TransitiveClosure transitiveClosure " +
                "where " +
                " phenotype.phenotypeExperiment.figure.publication = publication and " +
                " transitiveClosure.root = :aoTerm and " +
                "(phenotype.entity.superterm = transitiveClosure.child OR phenotype.entity.subterm = transitiveClosure.child OR " +
                " phenotype.relatedEntity.superterm = transitiveClosure.child OR phenotype.relatedEntity.subterm = transitiveClosure.child) " +
                " AND phenotype.phenotypeExperiment.fishExperiment.fish = :fish" +
                " AND phenotype.phenotypeExperiment.fishExperiment.fish.genotype.wildtype = :wildtype" +
                " AND 0 = all elements(phenotype.phenotypeExperiment.fishExperiment.fish.strList) ";

        Query query = session.createQuery(hql);
        query.setParameter("aoTerm", aoTerm);
        query.setParameter("fish", fish);
        query.setBoolean("wildtype", false);

/*
        Criteria experiment = genox.createCriteria("experiment");
        experiment.add(Restrictions.in("name", new String[]{Experiment.STANDARD, Experiment.GENERIC_CONTROL}));
*/
        query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return new PaginationResult<Publication>((List<Publication>) query.list());
    }

    public PaginationResult<Publication> getPublicationsWithFiguresbyGeno(Genotype genotype) {
        Session session = HibernateUtil.currentSession();
        /*Criteria pubs = session.createCriteria(Publication.class);
        Criteria phenotype = pubs.createCriteria("phenotypes");
        phenotype.add(Restrictions.isNotEmpty("figures"));
        Criteria genox = phenotype.createCriteria("genotypeExperiment");
        genox.add(Restrictions.eq("genotype", genotype));
        Criteria geno = genox.createCriteria("genotype");
        geno.add(Restrictions.eq("wildtype", false));

        Criteria experiment = genox.createCriteria("experiment");
        experiment.add(Restrictions.in("name", new String[]{Experiment.STANDARD, Experiment.GENERIC_CONTROL}));

        pubs.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);*/
        String hql = "select distinct figure.publication  from Figure figure, GenotypeFigure genofig " +
                "where genofig.genotype.zdbID = :genoID AND " +
                "      genofig.figure.zdbID=figure.id ";

        Query query = session.createQuery(hql);
        query.setString("genoID", genotype.getZdbID());

        /*PaginationResult<Publication> paginationResult = new PaginationResult<Publication>(query.list());
       return paginationResult;*/
        return new PaginationResult<Publication>((List<Publication>) query.list());
    }

    public PaginationResult<Publication> getPublicationsWithFiguresbyGenoExp(Genotype genotype) {
        Session session = HibernateUtil.currentSession();
        Criteria pubs = session.createCriteria(Publication.class);
        Criteria expression = pubs.createCriteria("expressionExperiments");
        Criteria genox = expression.createCriteria("genotypeExperiment");
        genox.add(Restrictions.eq("genotype", genotype));
        Criteria result = expression.createCriteria("expressionResults");
        result.add(Restrictions.isNotEmpty("figures"));
        expression.add(Restrictions.isNull("antibody"));
        pubs.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return new PaginationResult<Publication>((List<Publication>) pubs.list());
    }


    public int getNumPublicationsWithFiguresPerGenotypeAndAnatomy(Genotype genotype, GenericTerm aoTerm) {
        Session session = HibernateUtil.currentSession();

        String hql = " select count(distinct figure.publication.zdbID ) from " +
                " Figure figure, PhenotypeStatement phenotype " +
                "where " +
                "      ( phenotype.entity.superterm = :aoTerm OR phenotype.entity.subterm = :aoTerm OR " +
                "        phenotype.relatedEntity.superterm = :aoTerm OR phenotype.relatedEntity.subterm = :aoTerm ) AND " +
                "      phenotype.phenotypeExperiment.genotypeExperiment.genotype.zdbID = :genoID AND " +
                "      phenotype.phenotypeExperiment.figure =figure " +
                "";
        Query query = session.createQuery(hql);
        query.setString("genoID", genotype.getZdbID());
        query.setParameter("aoTerm", aoTerm);

        return ((Number) (query.uniqueResult())).intValue();
    }

    /**
     * Retrieve figures for a given gene and anatomy term.
     *
     * @param marker      marker
     * @param anatomyTerm anatomy
     * @return a set of figures
     */
    @SuppressWarnings("unchecked")
    public List<Figure> getFiguresByGeneAndAnatomy(Marker marker, GenericTerm anatomyTerm) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct fig from Figure fig, ExpressionResult res, Marker marker, ExpressionExperiment exp, " +
                "     FishExperiment fishox, ExpressionResultFigure xpatfig " +
                "where " +
                "   marker = :marker AND " +
                "   exp.gene = marker AND " +
                "   res.expressionExperiment = exp AND " +
                "   (res.entity.superterm = :aoTerm OR res.entity.subterm = :aoTerm) AND " +
                "   xpatfig.expressionResult = res AND " +
                "   xpatfig.figure = fig AND " +
                "   res.expressionFound = :expressionFound AND " +
                "   exp.fishExperiment = fishox AND " +
                "   fishox.standardOrGenericControl = :condition AND " +
                "   fishox.fish.genotype = geno AND " +
                "   fishox.fish.geno.wildtype = :isWildtype ";
        Query query = session.createQuery(hql);
        query.setBoolean("expressionFound", true);
        query.setBoolean("isWildtype", true);
        query.setParameter("aoTerm", anatomyTerm);
        query.setParameter("marker", marker);
        query.setBoolean("condition", true);
        return (List<Figure>) query.list();
    }


    public Journal getJournalByTitle(String journalTitle) {
        try {
            Session session = HibernateUtil.currentSession();
            Criteria criteria = session.createCriteria(Journal.class);
            criteria.add(Restrictions.eq("name", journalTitle));
            return (Journal) criteria.uniqueResult();
        } catch (Exception e) {
            logger.error("failed to get journal title[" + journalTitle + "] returning null", e);
            return null;
        }
    }

    @Override
    public Journal findJournalByAbbreviation(String abbrevation) {
        Criteria criteria = HibernateUtil.currentSession().createCriteria(Journal.class);
        criteria.add(Restrictions.eq("abbreviation", abbrevation));
        return (Journal) criteria.uniqueResult();
    }


    /**
     * Utility method for filling list to a max amount.  This is a destructive method on fillList.
     *
     * @param fillList   This list will get overwritten.
     * @param sourceList
     * @param maxFill
     * @return fillList
     */
    private Collection fillList(Collection fillList, Collection sourceList, int maxFill) {
        Iterator iter = sourceList.iterator();
        while (fillList.size() < maxFill && iter.hasNext()) {
            fillList.add(iter.next());
        }
        return fillList;
    }

    final String commonPubSQL =
            " select * from (select ra.recattrib_source_zdb_id   " +
                    " from record_attribution ra   " +
                    " where :markerZdbID = ra.recattrib_data_zdb_id " +
                    // marker relationship 2_1
                    " union " +
                    " select ra.recattrib_source_zdb_id  " +
                    " from record_attribution ra , marker_relationship mr " +
                    " where :markerZdbID = mr.mrel_mrkr_2_zdb_id " +
                    " and  ra.recattrib_data_zdb_id = mr.mrel_zdb_id " +
                    // marker relationship 1_2
                    " union " +
                    " select ra.recattrib_source_zdb_id  " +
                    " from record_attribution ra , marker_relationship mr " +
                    " where :markerZdbID = mr.mrel_mrkr_1_zdb_id " +
                    " and  ra.recattrib_data_zdb_id = mr.mrel_zdb_id " +
                    // str marker type necessary ?
                    " union " +
                    " select ra.recattrib_source_zdb_id  " +
                    " from record_attribution ra , marker_relationship mr , marker m " +
                    " where :markerZdbID = mr.mrel_mrkr_2_zdb_id " +
                    " and  ra.recattrib_data_zdb_id = mr.mrel_mrkr_1_zdb_id " +
                    " and  mr.mrel_mrkr_1_zdb_id = m.mrkr_zdb_id " +
                    " and  m.mrkr_type in ('MRPHLNO', 'TALEN', 'CRISPR') " +
                    // data alias
                    " union " +
                    " select ra.recattrib_source_zdb_id  " +
                    " from record_attribution ra , data_alias da  " +
                    " where da.dalias_zdb_id = ra.recattrib_data_zdb_id " +
                    " and :markerZdbID = da.dalias_data_zdb_id " +
                    // db link
                    " union " +
                    " select ra.recattrib_source_zdb_id  " +
                    " from record_attribution ra , db_link dbl  " +
                    " where  dbl.dblink_zdb_id  = ra.recattrib_data_zdb_id " +
                    " and  :markerZdbID = dbl.dblink_linked_recid " +
                    // db link, marker_relationship
                    " union " +
                    " select ra.recattrib_source_zdb_id  " +
                    " from record_attribution ra , db_link dbl , marker_relationship mr " +
                    " where  dbl.dblink_zdb_id  = ra.recattrib_data_zdb_id " +
                    " and dbl.dblink_linked_recid = mr.mrel_mrkr_2_zdb_id " +
                    " and  :markerZdbID = mr.mrel_mrkr_1_zdb_id " +
                    " and  mr.mrel_type = 'gene encodes small segment' " +
                    // ortho
                    " union " +
                    " select ra.recattrib_source_zdb_id  " +
                    " from record_attribution ra ,  orthologue_evidence_display ev " +
                    " where  ev.oevdisp_zdb_id = ra.recattrib_data_zdb_id " +
                    " and  :markerZdbID = ev.oevdisp_gene_zdb_id " +
                    // marker_go_term_Evidence
                    " union " +
                    " select ra.recattrib_source_zdb_id  " +
                    " from record_attribution ra ,  marker_go_term_evidence ev " +
                    " where  ev.mrkrgoev_zdb_id  = ra.recattrib_data_zdb_id " +
                    " and  :markerZdbID = ev.mrkrgoev_mrkr_zdb_id " +
                    // feature_marker_realationship
                    " union " +
                    " select ra.recattrib_source_zdb_id  " +
                    " from record_attribution ra ,  feature_marker_relationship fmr " +
                    " where  fmr.fmrel_ftr_zdb_id  = ra.recattrib_data_zdb_id " +
                    " and  :markerZdbID = fmr.fmrel_mrkr_zdb_id " +
                    // feature_marker_realationship, genotype_feature
                    " union " +
                    " select ra.recattrib_source_zdb_id  " +
                    " from record_attribution ra ,  feature_marker_relationship fmr, genotype_feature gf " +
                    " where  gf.genofeat_geno_zdb_id  = ra.recattrib_data_zdb_id " +
                    " and  :markerZdbID = fmr.fmrel_mrkr_zdb_id " +
                    " and fmr.fmrel_ftr_zdb_id  = gf.genofeat_feature_zdb_id " +
                    // genotype_feature
                    " union " +
                    " select ra.recattrib_source_zdb_id  " +
                    " from record_attribution ra ,  genotype_feature gf " +
                    " where  gf.genofeat_geno_zdb_id  = ra.recattrib_data_zdb_id " +
                    " and  :markerZdbID = gf.genofeat_feature_zdb_id " +
                    // expression_experiment
                    " union " +
                    " select xpatex_source_zdb_id  " +
                    " from expression_experiment " +
                    " where :markerZdbID = xpatex_gene_zdb_id " +
                    " ) where recattrib_source_zdb_id like 'ZDB-PUB%'  ";


    @Override
    public List<Publication> getPubsForDisplay(String zdbID) {

        List<String> publicationIDs = HibernateUtil.currentSession()
                .createSQLQuery(commonPubSQL)
                .setString("markerZdbID", zdbID)
                .list();

        if (CollectionUtils.isEmpty(publicationIDs)) {
            return new ArrayList<Publication>();
        }

        String hql = " select p from Publication p  " +
                " where p.zdbID in (:zdbIDs) " +
                " order by p.publicationDate desc ";
        List<Publication> publicationLinks = HibernateUtil.currentSession()
                .createQuery(hql)
                .setParameterList("zdbIDs", publicationIDs)
                .list();


        // remove if not pubs
        return publicationLinks;
    }


    @Override
    public int getNumberAssociatedPublicationsForZdbID(String zdbID) {
        String sql = " select count(*) from ( " + commonPubSQL + " )";

        int count = Integer.valueOf(HibernateUtil.currentSession()
                .createSQLQuery(sql)
                .setString("markerZdbID", zdbID)
                .uniqueResult().toString());

        // remove if not pubs
        return count;
    }


    /**
     * @param feature
     * @param maxPubs
     * @return
     */


    public PaginationResult<Publication> getAllAssociatedPublicationsForFeature(Feature feature, int maxPubs) {

        PaginationResult<Publication> paginationResult = new PaginationResult<Publication>();
        Set<Publication> pubList = new HashSet<Publication>();
        Query query;
        String hql;
        List<Publication> resultList;
        Session session = HibernateUtil.currentSession();

        // short list:
        hql = "select p.publication " +
                " from PublicationAttribution p " +
                " where p.dataZdbID = :featureZdbID ";
        query = session.createQuery(hql);
        query.setString("featureZdbID", feature.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);


        hql = "select p.publication " +
                " from PublicationAttribution p , DataAlias  da " +
                "  where p.dataZdbID = da.zdbID " +
                " and da.dataZdbID = :featureZdbID ";
        query = session.createQuery(hql);
        query.setString("featureZdbID", feature.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);


        hql = "select p.publication " +
                " from PublicationAttribution p , FeatureMarkerRelationship fmr " +
                " where fmr.feature.zdbID  = :featureZdbID " +
                " and fmr.feature.zdbID  = p.dataZdbID ";

        query = session.createQuery(hql);
        query.setString("featureZdbID", feature.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);


        hql = "select p.publication " +
                " from PublicationAttribution p , GenotypeFeature gtf " +
                " where gtf.genotype.zdbID  = p.dataZdbID " +
                "  and gtf.feature.zdbID = :featureZdbID ";
        query = session.createQuery(hql);
        query.setString("featureZdbID", feature.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);

        String zdbIDs = "";
        for (Publication pub : pubList) {
            zdbIDs += pub.getZdbID() + "\n";
        }

        if (maxPubs >= 0) {
            paginationResult.setPopulatedResults((new ArrayList(pubList)).subList(0, maxPubs));
        } else {
            paginationResult.setPopulatedResults(new ArrayList(pubList));
        }
        paginationResult.setTotalCount(pubList.size());
        return paginationResult;
    }


    /**
     * Retrieve Figue by ID
     *
     * @param zdbID ID
     * @return Figure
     */
    public Figure getFigure(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return (Figure) session.get(Figure.class, zdbID);
    }

    @SuppressWarnings("unchecked")
    public PaginationResult<Publication> getPublicationsWithFigures(Marker marker, GenericTerm anatomyTerm) {
        Session session = HibernateUtil.currentSession();

        Criteria pubs = session.createCriteria(Publication.class);
        Criteria expresssion = pubs.createCriteria("expressionExperiments");
        expresssion.add(Restrictions.eq("gene", marker));
        Criteria result = expresssion.createCriteria("expressionResults");
        result.add(Restrictions.isNotEmpty("figures"));
        result.add(Restrictions.or(Restrictions.eq("entity.superterm", anatomyTerm), Restrictions.eq("entity.subterm", anatomyTerm)));
        result.add(Restrictions.eq("expressionFound", true));
        Criteria genox = expresssion.createCriteria("fishExperiment");
        genox.add(Restrictions.eq("standardOrGenericControl", true));
        Criteria fish = genox.createCriteria("fish");
        Criteria genotype = fish.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        pubs.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        return new PaginationResult<Publication>((List<Publication>) pubs.list());
    }

    public List<Publication> getPublicationsWithFiguresbygenotype(Genotype genotype) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @SuppressWarnings("unchecked")
    public List<String> getDistinctFigureLabels(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select figure.label from Figure figure" +
                "     where figure.publication.zdbID = :pubID " +
                "    order by figure.orderingLabel ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);

        return (List<String>) query.list();

    }

    /**
     * Retrieve distinct list of genes (GENEDOM_EFG) that are attributed to a given
     * publication.
     *
     * @param pubID publication id
     * @return list of markers
     */
    // ToDo: There must be a better way to retrieve GENEs versus all markers.
    // GENE should be a subclass of Marker
    @SuppressWarnings("unchecked")
    public List<Marker> getGenesByPublication(String pubID) {
        Session session = HibernateUtil.currentSession();

        MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
        List<MarkerType> markerTypes = markerRepository.getMarkerTypesByGroup(Marker.TypeGroup.GENEDOM_AND_EFG);

        String hql = "select distinct marker from Marker marker, PublicationAttribution pub" +
                "     where pub.dataZdbID = marker.zdbID" +
                "           and pub.publication.zdbID = :pubID " +
                "           and marker.markerType in (:markerType)  " +
                "    order by marker.abbreviationOrder ";
        Query query = session.createQuery(hql);
        query.setString("pubID", pubID);
        query.setParameterList("markerType", markerTypes);

        return (List<Marker>) query.list();
    }

    public List<Marker> getGenesByExperiment(String pubID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct marker from Marker marker, ExpressionExperiment experiment" +
                "     where experiment.publication.zdbID = :pubID" +
                "            and experiment.gene = marker " +
                "           and marker.markerType.name = :type  " +
                "    order by marker.abbreviationOrder ";
        Query query = session.createQuery(hql);
        query.setString("pubID", pubID);
        query.setString("type", Marker.Type.GENE.toString());

        return (List<Marker>) query.list();
    }

    @SuppressWarnings("unchecked")
    public List<ExpressionExperiment> getExperimentsByGeneAndFish(String publicationID, String geneZdbID, String fishID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select experiment from ExpressionExperiment experiment";
        hql += "       left join fetch experiment.gene as gene ";
        hql += "       left join fetch experiment.antibody ";
        hql += "       left join fetch experiment.genotypeExperiment ";
        hql += "       left join fetch experiment.genotypeExperiment.experiment ";
        hql += "       left join fetch experiment.markerDBLink ";
        hql += "       left join fetch experiment.expressionResults ";
        hql += "       left join fetch experiment.publication ";
        hql += "       join fetch experiment.genotypeExperiment.genotype geno";
        hql += "     where experiment.publication.zdbID = :pubID ";
        if (geneZdbID != null) {
            hql += "           and experiment.gene.zdbID = :geneID ";
        }
        if (fishID != null) {
            hql += "           and geno.zdbID = :fishID ";
        }
        hql += "    order by gene.abbreviationOrder, " +
                "             experiment.genotypeExperiment.genotype.nameOrder, " +
                "             experiment.genotypeExperiment.experiment.name, " +
                "             experiment.assay.displayOrder ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        if (geneZdbID != null) {
            query.setString("geneID", geneZdbID);
        }
        if (fishID != null) {
            query.setString("fishID", fishID);
        }
        query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
        List<ExpressionExperiment> expressionExperiments = (List<ExpressionExperiment>) query.list();
        return new ArrayList<ExpressionExperiment>(expressionExperiments);
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
     * Retrieve list of Genotypes being used in a publication
     *
     * @param publicationID publication ID
     * @return list of genotype
     */
    @SuppressWarnings("unchecked")
    public List<Genotype> getGenotypesInPublication(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fish from Genotype fish, PublicationAttribution record" +
                "     where record.publication.zdbID = :pubID " +
                "           and record.dataZdbID = fish.zdbID" +
                "           and record.sourceType = :sourceType" +
                "    order by fish.handle ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        query.setParameter("sourceType", RecordAttribution.SourceType.STANDARD);

        return (List<Genotype>) query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Experiment> getExperimentsByPublication(String publicationID) {
        Session session = HibernateUtil.currentSession();

        Criteria crit = session.createCriteria(Experiment.class);
        crit.add(Restrictions.in("name", new String[]{Experiment.STANDARD, Experiment.GENERIC_CONTROL}));
        List<Experiment> experimentList = (List<Experiment>) crit.list();


        String hql = "select distinct experiment from Experiment experiment" +
                "     where experiment.publication.zdbID = :pubID " +
                "     and experiment.experimentConditions is not empty" +
                "    order by experiment.name ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);

        experimentList.addAll(query.list());
        return experimentList;

    }

    @SuppressWarnings("unchecked")
    public Genotype getGenotypeByHandle(String handle) {
        Session session = HibernateUtil.currentSession();

        Criteria crit = session.createCriteria(Genotype.class);
        crit.add(Restrictions.eq("handle", handle));
        return (Genotype) crit.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<Genotype> getNonWTGenotypesByPublication(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct geno from Genotype geno, PublicationAttribution record" +
                "     where record.publication.zdbID = :pubID " +
                "           and record.dataZdbID = geno.zdbID" +
                "    order by geno.handle ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);

        return (List<Genotype>) query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Antibody> getAntibodiesByPublication(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct antibody from Antibody antibody, PublicationAttribution record" +
                "     where record.publication.zdbID = :pubID " +
                "           and record.dataZdbID = antibody.zdbID" +
                "    order by antibody.abbreviationOrder ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);

        return (List<Antibody>) query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Antibody> getAntibodiesByPublicationAndGene(String publicationID, String geneID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct antibody from Antibody antibody, PublicationAttribution record, MarkerRelationship rel" +
                "     where record.publication.zdbID = :pubID " +
                "           and record.dataZdbID = antibody.zdbID" +
                "           and rel.firstMarker.zdbID = :geneID " +
                "           and rel.secondMarker = antibody " +
                "    order by antibody.abbreviationOrder ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        query.setString("geneID", geneID);

        return (List<Antibody>) query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Marker> getGenesByAntibody(String publicationID, String antibodyID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct gene from Marker gene, PublicationAttribution record, MarkerRelationship rel" +
                "     where record.publication.zdbID = :pubID " +
                "           and record.dataZdbID = gene.zdbID" +
                "           and rel.firstMarker = gene " +
                "           and rel.secondMarker.zdbID = :antibodyID " +
                "    order by gene.abbreviationOrder ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        query.setString("antibodyID", antibodyID);

        return (List<Marker>) query.list();
    }

    /**
     * Retrieve access numbers for given pub and gene.
     *
     * @param publicationID string
     * @param geneID        string
     * @return list of db links
     */
    @SuppressWarnings("unchecked")
    public List<MarkerDBLink> getDBLinksByGene(String publicationID, String geneID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct link from MarkerDBLink link, PublicationAttribution record" +
                "     where record.publication.zdbID = :pubID " +
                "           and record.dataZdbID = :geneID" +
                "           and link.marker = :geneID " +
                "           and link.referenceDatabase.foreignDB.dbName = :foreignDB " +
                "    order by link.accessionNumber ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        query.setString("geneID", geneID);
        query.setString("foreignDB", ForeignDB.AvailableName.GENBANK.toString());

        return (List<MarkerDBLink>) query.list();
    }

    /**
     * Retrieve db link object of a clone for a gene and pub.
     *
     * @param pubID  pub is
     * @param geneID gene ID
     * @return list of MarkerDBLinks
     */
    @SuppressWarnings("unchecked")
    public List<MarkerDBLink> getDBLinksForCloneByGene(String pubID, String geneID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct link from MarkerDBLink link, PublicationAttribution record, " +
                "                               Marker marker, Clone clone, MarkerRelationship mrel " +
                "     where record.publication.zdbID = :pubID " +
                "           and record.dataZdbID = :geneID" +
                "           and link.marker.zdbID = clone.zdbID " +
                "           and marker.zdbID = :geneID " +
                "           and mrel.firstMarker.zdbID = :geneID " +
                "           and mrel.secondMarker.zdbID = clone.id " +
                "           and mrel.type = :type " +
                "           and link.referenceDatabase.foreignDB.dbName = :foreignDB " +
                "    order by link.accessionNumber ";
        Query query = session.createQuery(hql);
        query.setString("pubID", pubID);
        query.setString("geneID", geneID);
        query.setString("foreignDB", ForeignDB.AvailableName.GENBANK.toString());
        query.setParameter("type", MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

        return (List<MarkerDBLink>) query.list();
    }

    /**
     * Retrieve all figures that are associated to a given publication.
     *
     * @param pubID publication ID
     * @return list of figures
     */
    @SuppressWarnings("unchecked")
    public List<Figure> getFiguresByPublication(String pubID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select figure from Figure figure ";
        hql += "      where figure.publication.zdbID = :pubID ";
        hql += "      order by figure.orderingLabel ";
        Query query = session.createQuery(hql);
        query.setString("pubID", pubID);
        return (List<Figure>) query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Publication> getPublicationsWithAccessionButNoDOI(int maxResults) {
        Session session = HibernateUtil.currentSession();
        Criteria query = session.createCriteria(Publication.class);
        query.add(Restrictions.isNull("doi"));
        query.add(Restrictions.isNotNull("accessionNumber"));
        query.addOrder(Order.desc("publicationDate"));
        if (maxResults >= 0) {
            query.setMaxResults(maxResults);
        }
        return query.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    /**
     * Get N publication if not attempts have been registered, or the number of attempts is less than allowed amount.
     */
    public List<Publication> getPublicationsWithAccessionButNoDOIAndLessAttempts(int maxAttempts, int maxResults) {
        Session session = HibernateUtil.currentSession();
        String hql = " select p from Publication p" +
                " where p.doi is null  " +
                " and p.accessionNumber is not null  " +
                " and ( not exists ( " +
                "   select 'x' from DOIAttempt da where da.publication.zdbID = p.zdbID " +
                " ) OR exists ( " +
                "   select 'x' from DOIAttempt da where da.publication.zdbID = p.zdbID " +
                "   and da.numAttempts < :attempts " +
                " )  )" +
                " order by p.publicationDate desc " +
                "";
        Query query = session.createQuery(hql);
        query.setInteger("attempts", maxAttempts);
        if (maxResults >= 0) {
            query.setMaxResults(maxResults);
        }
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<DOIAttempt> getDOIAttemptsFromPubs(List<Publication> publicationList) {
        if (CollectionUtils.isEmpty(publicationList)) {
            return new ArrayList<DOIAttempt>();
        }
        Session session = HibernateUtil.currentSession();
        String hql = " select da from DOIAttempt da   " +
                " where da.publication in (:publicationList) " +
                "";
        Query query = session.createQuery(hql);
        query.setParameterList("publicationList", publicationList);
        return query.list();
    }

    @SuppressWarnings("unchecked")
    /**
     * This class creates DOI attempts for pubs without previous DOI attempt entries.
     */
    public List<DOIAttempt> createDOIAttemptsForPubs(List<Publication> publicationList) {
        if (CollectionUtils.isEmpty(publicationList)) {
            return new ArrayList<DOIAttempt>();
        }
        Session session = HibernateUtil.currentSession();
        String hql = " select p from Publication p " +
                " where not exists ( select 'x' from DOIAttempt da where da.publication = p ) " +
                " and p in (:publicationList) " +
                "";
        Query query = session.createQuery(hql);
        query.setParameterList("publicationList", publicationList);
        List<Publication> publications = query.list();
        List<DOIAttempt> doiAttempts = new ArrayList<DOIAttempt>();
        for (Publication publication : publications) {
            DOIAttempt doiAttempt = new DOIAttempt();
            doiAttempt.setNumAttempts(1); // probably will be null, though
            doiAttempt.setPublication(publication); // probably will be null, though
            HibernateUtil.currentSession().save(doiAttempt);
            doiAttempts.add(doiAttempt);
        }
        return doiAttempts;
    }

    @SuppressWarnings("unchecked")
    @Override
    /**
     * If DOIAttempt not there, then it will add one.
     * Do in two sets, one with and one without.
     * We only want 2 SQL update passes (one for insert and one for update) at most.
     */
    public List<Publication> addDOIAttempts(List<Publication> publicationList) {
        List<DOIAttempt> doiAttempts = getDOIAttemptsFromPubs(publicationList);
        for (DOIAttempt doiAttempt : doiAttempts) {
            doiAttempt.addAttempt();
        }

        createDOIAttemptsForPubs(publicationList);

        return publicationList;
    }

    public PaginationResult<Publication> getAllAssociatedPublicationsForGenotype(Genotype genotype, int maxPubs) {

        PaginationResult<Publication> paginationResult = new PaginationResult<Publication>();
        Set<Publication> pubList = new HashSet<Publication>();
        Query query;
        String hql;
        List<Publication> resultList;
        Session session = HibernateUtil.currentSession();

        hql = "select p.publication " +
                " from PublicationAttribution p " +
                " where p.dataZdbID = :genotypeZdbID ";
        query = session.createQuery(hql);
        query.setString("genotypeZdbID", genotype.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);


        hql = "select p.publication " +
                " from PublicationAttribution p, DataAlias da " +
                "  where p.dataZdbID = da.zdbID " +
                " and da.dataZdbID = :genotypeZdbID ";
        query = session.createQuery(hql);
        query.setString("genotypeZdbID", genotype.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);


        if (maxPubs >= 0) {
            paginationResult.setPopulatedResults((new ArrayList<Publication>(pubList)).subList(0, maxPubs));
        } else {
            paginationResult.setPopulatedResults(new ArrayList<Publication>(pubList));
        }
        paginationResult.setTotalCount(pubList.size());
        return paginationResult;
    }

    public List<Publication> getPublicationByPmid(String pubMedID) {
        return (List<Publication>) HibernateUtil.currentSession().createCriteria(Publication.class)
                .add(Restrictions.eq("accessionNumber", pubMedID))
                .list();
    }

    @Override
    public int getNumberDirectPublications(String zdbID) {
        return Integer.parseInt(HibernateUtil.currentSession().createSQLQuery("select count(*) " +
                        "from record_attribution ra " +
                        "where ra.recattrib_data_zdb_id=:zdbID ")
                        .setString("zdbID", zdbID)
                        .uniqueResult()
                        .toString()
        );
    }

    /**
     * Retrieve list of mutants and transgenics being used in a publication
     *
     * @param publicationID publication ID
     * @return list of genotype (non-wt)
     */
    public List<Genotype> getMutantsAndTgsByPublication(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct mutant from Genotype mutant, PublicationAttribution attr " +
                "      where attr.publication.zdbID = :pubID " +
                "        and attr.dataZdbID = mutant.zdbID" +
                "        and mutant.wildtype = 'f'" +
                "   order by mutant.nameOrder ";

        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);

        return (List<Genotype>) query.list();
    }

    @Override
    public List<Marker> getOrthologyGeneList(String pubID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct evidence.gene, evidence.gene.abbreviationOrder" +
                " from OrthoEvidenceDisplay as evidence, PublicationAttribution attr " +
                "      where attr.publication.zdbID = :pubID " +
                "        and attr.dataZdbID = evidence.zdbID " +
                "order by evidence.gene.abbreviationOrder";

        Query query = session.createQuery(hql);
        query.setString("pubID", pubID);
        List<Object[]> array = (List<Object[]>) query.list();
        List<Marker> markerList = new ArrayList<>(array.size());
        for (Object[] arrayObject : array) {
            markerList.add((Marker) arrayObject[0]);
        }
        return markerList;
    }

    @Override
    public List<Orthology> getOrthologyPublications(Marker marker) {
        Session session = HibernateUtil.currentSession();

        String hql = "select attr.publication, evidence, attr.publication.shortAuthorList from OrthoEvidenceDisplay as evidence, PublicationAttribution attr " +
                "      where evidence.gene= :gene " +
                "        and attr.dataZdbID = evidence.zdbID " +
                "order by attr.publication.shortAuthorList";

        Query query = session.createQuery(hql);
        query.setParameter("gene", marker);
        List<Object[]> array = (List<Object[]>) query.list();
        List<Orthology> orthologyList = new ArrayList<>(array.size());
        for (Object[] arrayObject : array) {
            Orthology orthology = new Orthology();
            orthology.setPublication((Publication) arrayObject[0]);
            OrthoEvidenceDisplay orthoEvidenceDisplay = (OrthoEvidenceDisplay) arrayObject[1];
            orthology.setEvidenceCode(orthoEvidenceDisplay.getEvidenceCode());
            orthology.setGene(marker);
            String[] speciesArray = orthoEvidenceDisplay.getOrganismList().split(":");
            for (String species : speciesArray) {
                orthology.addOrthologousSpecies(species);
            }
            orthologyList.add(orthology);
        }
        return orthologyList;
    }

    @Override
    public List<Publication> getPublicationWithPubMedId(Integer maxResult) {
        Session session = HibernateUtil.currentSession();

        String hql = "from Publication as publication" +
                "      where publication.accessionNumber is not null " +
                "      AND publication.type in (:type) " +
                "      AND publication.status = :status";

        Query query = session.createQuery(hql);
        query.setParameterList("type", new String[]{"Journal", "Review"});
        query.setString("status", "active");
        if (maxResult != null) {
            query.setMaxResults(maxResult);
        }

        return (List<Publication>) query.list();
    }

    public SortedSet<Publication> getAllPublicationsForFeature(Feature feature) {
        SortedSet<Publication> pubList = new TreeSet<Publication>();
        Query query;
        String hql;
        List<Publication> resultList;
        Session session = HibernateUtil.currentSession();

        hql = "select p.publication " +
                " from PublicationAttribution p " +
                " where p.dataZdbID = :featureZdbID ";
        query = session.createQuery(hql);
        query.setString("featureZdbID", feature.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);


        hql = "select p.publication " +
                " from PublicationAttribution p , DataAlias  da " +
                "  where p.dataZdbID = da.zdbID " +
                " and da.dataZdbID = :featureZdbID ";
        query = session.createQuery(hql);
        query.setString("featureZdbID", feature.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);


        hql = "select p.publication " +
                " from PublicationAttribution p , FeatureMarkerRelationship fmr " +
                " where fmr.feature.zdbID  = :featureZdbID " +
                " and fmr.feature.zdbID  = p.dataZdbID ";

        query = session.createQuery(hql);
        query.setString("featureZdbID", feature.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);


        hql = "select p.publication " +
                " from PublicationAttribution p , GenotypeFeature gtf " +
                " where gtf.genotype.zdbID  = p.dataZdbID " +
                "  and gtf.feature.zdbID = :featureZdbID ";
        query = session.createQuery(hql);
        query.setString("featureZdbID", feature.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);

        return pubList;
    }

    public SortedSet<Publication> getPublicationForJournal(Journal journal) {
        SortedSet<Publication> pubList = new TreeSet<Publication>();
        Query query;
        String hql;
        List<Publication> resultList;
        Session session = HibernateUtil.currentSession();

        hql = "select publication " +
                " from Publication publication " +
                " where publication.journal = :journalWithPub ";
        query = session.createQuery(hql);
        query.setParameter("journalWithPub", journal);
        resultList = query.list();
        pubList.addAll(resultList);

        return pubList;
    }

    public Journal getJournalByID(String zdbID) {
        return (Journal) HibernateUtil.currentSession().get(Journal.class, zdbID);
    }

    public SortedSet<Publication> getAllPublicationsForGenotype(Genotype genotype) {
        SortedSet<Publication> pubList = new TreeSet<Publication>();
        Query query;
        String hql;
        List<Publication> resultList;
        Session session = HibernateUtil.currentSession();

        hql = "select p.publication " +
                " from PublicationAttribution p " +
                " where p.dataZdbID = :genotypeZdbID ";
        query = session.createQuery(hql);
        query.setString("genotypeZdbID", genotype.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);

        hql = "select p.publication " +
                " from PublicationAttribution p , DataAlias  da " +
                "  where p.dataZdbID = da.zdbID " +
                " and da.dataZdbID = :genotypeZdbID ";
        query = session.createQuery(hql);
        query.setString("genotypeZdbID", genotype.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);

        return pubList;
    }

    public List<String> getPublicationIDsForGOwithField(String zdbID) {
        zdbID = "ZFIN:" + zdbID;
        Session session = HibernateUtil.currentSession();
        String sql = "select distinct mrkrgoev_source_zdb_id " +
                " from marker_go_term_evidence, inference_group_member " +
                " where mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id and infgrmem_inferred_from = :zdbID";
        SQLQuery query = session.createSQLQuery(sql);
        query.setString("zdbID", zdbID);
        List<String> pubIDs = query.list();
        return pubIDs;
    }

    public void addPublication(Publication publication) {
        HibernateUtil.createTransaction();
        HibernateUtil.currentSession().save(publication);
        HibernateUtil.flushAndCommitCurrentSession();
    }

    public Long getMarkerCount(Publication publication) {
        String sql = "select count(*) FROM (\n" +
                "  SELECT fmrel_mrkr_zdb_id\n" +
                "  FROM record_attribution, feature_marker_relationship\n" +
                "  WHERE recattrib_source_zdb_id = :zdbID\n" +
                "        AND recattrib_data_zdb_id = fmrel_ftr_zdb_id\n" +
                "        AND fmrel_type = 'is allele of'\n" +
                "\n" +
                "  UNION\n" +
                "\n" +
                "  SELECT mrkr_zdb_id\n" +
                "  FROM record_attribution, marker\n" +
                "  WHERE recattrib_source_zdb_id = :zdbID\n" +
                "        AND recattrib_data_zdb_id = mrkr_zdb_id\n" +
                "        AND mrkr_type IN\n" +
                "            (\n" +
                "              SELECT mtgrpmem_mrkr_type\n" +
                "              FROM marker_type_group_member\n" +
                "              WHERE mtgrpmem_mrkr_type_group = 'SEARCH_MK'\n" +
                "            )\n" +
                "        AND (mrkr_type <> 'MRPHLNO' AND mrkr_type <> 'EFG')\n" +
                "\n" +
                "  UNION\n" +
                "\n" +
                "  SELECT mr.mrel_mrkr_2_zdb_id\n" +
                "  FROM record_attribution ra, marker m, marker_relationship mr\n" +
                "  WHERE recattrib_source_zdb_id = :zdbID\n" +
                "        AND recattrib_data_zdb_id = mrkr_zdb_id\n" +
                "        AND m.mrkr_zdb_id = mr.mrel_mrkr_1_zdb_id\n" +
                "        AND mrkr_type = 'MRPHLNO'\n" +
                "\n" +
                ");";

        return getCount(sql, publication.getZdbID());
    }


    public Long getMorpholinoCount(Publication publication) {
        return getMarkerCountByMarkerType(publication.getZdbID(), Marker.Type.MRPHLNO.toString());
    }

    public Long getTalenCount(Publication publication) {
        return getMarkerCountByMarkerType(publication.getZdbID(), Marker.Type.TALEN.toString());
    }

    public Long getCrisprCount(Publication publication) {
        return getMarkerCountByMarkerType(publication.getZdbID(), Marker.Type.CRISPR.toString());
    }

    public Long getAntibodyCount(Publication publication) {
        return getMarkerCountByMarkerType(publication.getZdbID(), Marker.Type.ATB.toString());
    }

    public Long getEfgCount(Publication publication) {
        return getMarkerCountByMarkerType(publication.getZdbID(), Marker.Type.EFG.toString());
    }

    public Long getCloneProbeCount(Publication publication) {
        String sql = "\tselect count(recattrib_data_zdb_id)\n" +
                "\t from  record_attribution, marker\n" +
                "\t where recattrib_source_zdb_id = :zdbID\n" +
                "\t  and  recattrib_data_zdb_id   = mrkr_zdb_id\n" +
                "\t  and  mrkr_type in \n" +
                "\t\t  (select mtgrpmem_mrkr_type from marker_type_group_member\n" +
                "                    where mtgrpmem_mrkr_type_group = 'SEARCH_SEG');";
        return getCount(sql, publication.getZdbID());
    }

    public Long getExpressionCount(Publication publication) {
        String sql = "\tselect count(distinct xpatfig_fig_zdb_id)\n" +
                " \t  from figure, expression_pattern_figure\n" +
                " \t where fig_source_zdb_id = :zdbID\n" +
                "         and fig_zdb_id=xpatfig_fig_zdb_id";
        return getCount(sql, publication.getZdbID());
    }

    public Long getPhenotypeCount(Publication publication) {
        String sql = "\tselect count(distinct phenox_fig_zdb_id)\n" +
                " \t  from figure, phenotype_experiment\n" +
                " \t where fig_source_zdb_id = :zdbID\n" +
                "         and phenox_fig_zdb_id = fig_zdb_id";
        return getCount(sql, publication.getZdbID());
    }

    public Long getPhenotypeAlleleCount(Publication publication) {
        String sql = "\tselect count(distinct geno_zdb_id)\n" +
                "\tfrom   record_attribution, genotype\n" +
                "\twhere  recattrib_source_zdb_id = :zdbID\n" +
                "\t  and  recattrib_data_zdb_id = geno_zdb_id\n" +
                "\t  and  geno_is_wildtype = 'f';";
        return getCount(sql, publication.getZdbID());
    }

    public Long getOrthologyCount(Publication publication) {
        String sql = "\tselect count(recattrib_data_zdb_id)\n" +
                "\t from  record_attribution\n" +
                "\t where recattrib_source_zdb_id = :zdbID\n" +
                "\t  and  recattrib_data_zdb_id like 'ZDB-OEVDISP-%';";
        return getCount(sql, publication.getZdbID());
    }

    public Long getMappingDetailsCount(Publication publication) {
        String sql = "select (\n" +
                "select count(distinct lms_member_1_zdb_id)\n" +
                "    \t  from linkage, linkage_membership_search\n" +
                "    \t where lnkg_source_zdb_id = :zdbID\n" +
                "    \t and lms_lnkg_zdb_id = lnkg_zdb_id    \n" +
                ") +\n" +
                "(select  count(distinct lsingle_member_zdb_id)\n" +
                "    \t  from linkage, linkage_single\n" +
                "    \t where lnkg_source_zdb_id = :zdbID\n" +
                "    \t and lsingle_lnkg_zdb_id = lnkg_zdb_id\n" +
                "    \t and not exists (select 'x' from linkage_membership_search \n" +
                "    \t where lms_lnkg_zdb_id = lnkg_zdb_id)\n" +
                ") from systables where tabid = 1\n" +
                "\t  ;";
        return getCount(sql, publication.getZdbID());
    }

    public Boolean canDeletePublication(Publication publication) {

        String sql = "select count(recattrib_source_zdb_id) \n" +
                "               from record_attribution, figure \n" +
                "              where recattrib_source_zdb_id = :zdbID\n" +
                "                and recattrib_data_zdb_id = fig_zdb_id\n" +
                "                and (exists (select 'x' \n" +
                "                               from phenotype_experiment\n" +
                "                              where phenox_fig_zdb_id = fig_zdb_id)\n" +
                "                  or exists (select 'x' \n" +
                "\t\t               from construct_figure\n" +
                "                              where consfig_fig_zdb_id = fig_zdb_id)\n" +
                "                  or exists (select 'x' \n" +
                "\t\t               from expression_pattern_figure\n" +
                "                              where xpatfig_fig_zdb_id = fig_zdb_id)\n" +
                "                  or exists (select 'x' \n" +
                "\t\t               from genotype_figure_fast_search\n" +
                "                              where gffs_fig_zdb_id = fig_zdb_id)\n" +
                "                    );";

        Long figureDataCount = getCount(sql, publication.getZdbID());

        sql = "select count(recattrib_source_zdb_id) from record_attribution where recattrib_source_zdb_id = :zdbID";
        Long directDataCount = getCount(sql, publication.getZdbID());


        if (figureDataCount == 0 && directDataCount == 0) {
            return true;
        }

        return false;
    }

    @Override
    public Fish getFishByHandle(String handle) {
        Session session = HibernateUtil.currentSession();
        Criteria crit = session.createCriteria(Fish.class);
        crit.add(Restrictions.eq("handle", handle));
        return (Fish) crit.uniqueResult();
    }

    @Override
    public List<Fish> getNonWTFishByPublication(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fish from Fish fish, PublicationAttribution record" +
                "     where record.publication.zdbID = :pubID " +
                "           and record.dataZdbID = fish.zdbID" +
                "    order by fish.name";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);

        return (List<Fish>) query.list();
    }

    public List<Journal> findJournalByAbbreviationAndName(String query) {
        String likeQuery = "%" + query + "%";
        Criteria criteria = HibernateUtil.currentSession().createCriteria(Journal.class);
        criteria.add(Restrictions.or(
                Restrictions.ilike("name", likeQuery),
                Restrictions.ilike("abbreviation", likeQuery)
        ));
        return criteria.list();
    }

    public List<String> getFeatureNamesWithNoGenotypesForPub(String pubZdbID) {
        String sql = "select distinct f.feature_name, f.feature_zdb_id, ra1.recattrib_source_zdb_id" +
                " from feature f" +
                " join record_attribution ra1 on ra1.recattrib_data_zdb_id=f.feature_zdb_id" +
                " where ra1.recattrib_source_zdb_id = :pubID" +
                " and ra1.recattrib_source_type = 'standard'" +
                " and not exists (" +
                "     select 'x'" +
                "     from genotype_feature gf, record_attribution ra2" +
                "     where gf.genofeat_feature_zdb_id = f.feature_zdb_id" +
                "     and ra2.recattrib_source_type = 'standard'" +
                "     and gf.genofeat_geno_zdb_id = ra2.recattrib_data_zdb_id" +
                "     and ra2.recattrib_source_zdb_id = :pubID" +
                " );";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("pubID", pubZdbID);
        query.setResultTransformer(new BasicTransformerAdapter() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                return tuple[0];
            }
        });
        return query.list();
    }

    public List<String> getTalenOrCrisprFeaturesWithNoRelationship(String pubZdbID) {
        String sql = "select distinct feature_name" +
                " from record_attribution, feature" +
                " where recattrib_source_zdb_id = :pubID" +
                " and recattrib_data_zdb_id = feature_zdb_id" +
                " and exists (" +
                "   select 'x' from feature_assay" +
                "   where featassay_feature_zdb_id = recattrib_data_zdb_id" +
                "   and (featassay_mutagen = 'CRISPR' or featassay_mutagen = 'TALEN')" +
                " )" +
                " and (" +
                "   not exists (" +
                "     select 'x' from feature_marker_relationship" +
                "     where fmrel_ftr_zdb_id = recattrib_data_zdb_id" +
                "     and fmrel_type = 'is allele of') " +
                "   or not exists (" +
                "     select 'x' from feature_marker_relationship" +
                "     where fmrel_ftr_zdb_id = recattrib_data_zdb_id" +
                "     and fmrel_type = 'created by')" +
                " );";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("pubID", pubZdbID);
        return query.list();
    }

    private Long getMarkerCountByMarkerType(String zdbID, String type) {
        String sql = "select count(recattrib_data_zdb_id)\n" +
                "\tfrom  record_attribution, marker\n" +
                "\twhere recattrib_source_zdb_id = :zdbID\n" +
                "\t  and recattrib_data_zdb_id = mrkr_zdb_id\n" +
                "\t  and mrkr_type = :mrkrType\n" +
                "      ; ";

        SQLQuery query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("zdbID", zdbID);
        query.setString("mrkrType", type);
        return ((Number) query.uniqueResult()).longValue();
    }


    private Long getCount(String sql, String zdbID) {
        SQLQuery query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("zdbID", zdbID);
        return ((Number) query.uniqueResult()).longValue();
    }

}
