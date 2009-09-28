package org.zfin.publication.repository;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.CanonicalMarker;
import org.zfin.database.SearchUtil;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.*;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.Morpholino;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.antibody.Antibody;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ForeignDB;

import java.util.*;

/**
 * ToDO: include documentation
 */
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
    public List<Publication> getHighQualityProbePublications(AnatomyItem anatomyTerm) {
        Session session = HibernateUtil.currentSession();
        String hql = "SELECT distinct exp.publication FROM ExpressionExperiment exp, ExpressionResult res, Clone clone   " +
                "WHERE res.anatomyTerm = :aoTerm " +
                "AND res.expressionExperiment = exp " +
                "AND exp.clone = clone " +
                "AND clone.rating = 4 " +
                "AND clone.problem is null";
        Query query = session.createQuery(hql);
        query.setParameter("aoTerm", anatomyTerm);
        List<Publication> list = query.list();
        return list;
    }

    public List<Publication> getExpressedGenePublications(String geneID, String anatomyItemID) {
        Session session = HibernateUtil.currentSession();
        String hql = "SELECT distinct publication FROM Publication publication, ExpressionExperiment exp, ExpressionResult res, Marker marker   " +
                "WHERE res.anatomyTerm.zdbID = :aoZdbID " +
                "AND publication.zdbID = exp.publicationID " +
                "AND res.expressionExperiment = exp " +
                "AND marker.zdbID = exp.geneID " +
                "AND marker.zdbID = :zdbID ";
        String sql = addOrderByParameters(hql);
        Query query = session.createQuery(sql);
        addPaginationParameters(query);
        query.setString("zdbID", geneID);
        query.setString("aoZdbID", anatomyItemID);
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
        if (!isUsePagination())
            return hql;
        StringBuilder sb = new StringBuilder(hql);
        sb.append(getOrderByClause());
        return sb.toString();
    }

    public int getNumberOfExpressedGenePublications(String geneID, String anatomyItemID) {
        Session session = HibernateUtil.currentSession();
        String hql = "SELECT count(distinct pub) FROM Publication pub, ExpressionExperiment exp, ExpressionResult res, Marker marker   " +
                "WHERE res.anatomyTerm.zdbID = :aoZdbID " +
                "AND pub.zdbID = exp.publicationID " +
                "AND res.expressionExperiment = exp " +
                "AND marker.zdbID = exp.geneID " +
                "AND marker.zdbID = :zdbID ";
        Query query = session.createQuery(hql);
        query.setString("zdbID", geneID);
        query.setString("aoZdbID", anatomyItemID);
        return ((Number) query.uniqueResult()).intValue();
    }

    public int getNumberOfExpressedGenePublicationsWithFigures(String geneID, String anatomyItemID) {
        Session session = HibernateUtil.currentSession();
        String hql = "SELECT count(distinct exp.publication) " +
                "FROM ExpressionExperiment exp, ExpressionResult res " +
                "WHERE res.anatomyTerm.zdbID = :aoZdbID " +
                "AND res.expressionExperiment = exp " +
                "AND res.figures is not empty " +
                "AND exp.marker.zdbID = :zdbID ";
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
    public PaginationResult<HighQualityProbe> getHighQualityProbeNames(AnatomyItem term) {
        return getHighQualityProbeNames(term, Integer.MAX_VALUE);
    }


    public PaginationResult<HighQualityProbe> getHighQualityProbeNames(AnatomyItem term, int maxRow) {

        String hql = "select distinct probe, marker " +
                "FROM ExpressionExperiment exp, ExpressionResult res, Clone clone, Marker probe, Marker marker " +
                "WHERE  res.anatomyTerm.zdbID = :zdbID " +
                "AND res.expressionExperiment = exp " +
                "AND exp.clone = clone " +
                "AND exp.probe = probe " +
                "AND exp.marker = marker " +
                "AND clone.rating = 4 " +
                "ORDER by marker.abbreviationOrder  ";
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery(hql);
        query.setString("zdbID", term.getZdbID());
        ScrollableResults results = query.scroll();


        List<Object[]> list = new ArrayList<Object[]>();
        while (results.next() && results.getRowNumber() < maxRow) {
            list.add(results.get());
        }

        int totalCount = 0;
        if (results.last())
            totalCount = results.getRowNumber() + 1;

        results.close();

        List<HighQualityProbe> probes = createHighQualityProbeObjects(list, term);
        return new PaginationResult<HighQualityProbe>(totalCount, probes);

    }

    public List<Marker> getAllExpressedMarkers(String zdbID, int maxRow) {
        Session session = HibernateUtil.currentSession();

//        String hql = "SELECT distinct marker FROM Marker marker, ExpressionExperiment exp, ExpressionResult res  " +
//                "WHERE res.anatomyTerm.zdbID = :zdbID " +
//                "AND res.expressionExperiment = exp " +
//                "AND marker.zdbID = exp.geneID " +
//                "ORDER BY marker.abbreviationOrder";

        String hql = "SELECT distinct marker FROM Marker marker, ExpressionResult res  " +
                "WHERE res.anatomyTerm.zdbID = :zdbID " +
                "AND res.expressionExperiment.geneID = marker.zdbID " +
                "ORDER BY marker.abbreviationOrder";

        Query query = session.createQuery(hql);
        if (maxRow != SearchUtil.ALL) {
            query.setMaxResults(maxRow);
        }
        query.setString("zdbID", zdbID);
        List<Marker> list = query.list();
        return list;
    }


    public PaginationResult<MarkerStatistic> getAllExpressedMarkers(AnatomyItem anatomyTerm) {
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
    public PaginationResult<MarkerStatistic> getAllExpressedMarkers(AnatomyItem anatomyTerm, int firstRow, int numberOfRecords) {
        if (firstRow < 0)
            throw new RuntimeException("First Row number <" + firstRow + "> is invalid");
        // Hibernate starts at 0 while the argument expects to start at 1

        Session session = HibernateUtil.currentSession();

        // todo: Rewrite as HQL and include the whole marker object as it is needed.
        // todo: note that when in SQL, start at 1 (current) , but when in HQL, start at 0
        String hql = "SELECT exp.xpatex_gene_zdb_id as geneID, gene.mrkr_abbrev as geneSymbol, " +
                "count(distinct fig.fig_zdb_id) as numOfFig  " +
                "FROM  Expression_Experiment exp, outer marker probe, Anatomy_Item item_, Marker gene, Figure fig," +
                "      Genotype geno, Genotype_Experiment genox, expression_pattern_figure results, expression_result result," +
                "      Experiment experiment " +
                "WHERE  exp.xpatex_probe_feature_zdb_id = probe.mrkr_zdb_id AND" +
                "       exp.xpatex_gene_zdb_id = gene.mrkr_zdb_id AND         " +
                "       item_.anatitem_zdb_id = :anatomyZdbID AND " +
                "       result.xpatres_xpatex_zdb_id = exp.xpatex_zdb_id AND " +
                "       result.xpatres_anat_item_zdb_id = item_.anatitem_zdb_id AND " +
                "       result.xpatres_expression_found = :expressionFound AND " +
                "       fig.fig_zdb_id=results.xpatfig_fig_zdb_id AND " +
                "       results.xpatfig_xpatres_zdb_id=result.xpatres_zdb_id AND " +
                "       genox.genox_zdb_id=exp.xpatex_genox_zdb_id AND " +
                "       genox.genox_geno_zdb_id=geno.geno_zdb_id AND " +
                "       geno.geno_is_wildtype = :isWildtype AND " +
                "       exp.xpatex_gene_zdb_id = gene.mrkr_zdb_id AND " +
                "       experiment.exp_zdb_id = genox.genox_exp_zdb_id AND " +
                "       experiment.exp_name = :condition AND " +
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
                "ORDER BY numOfFig DESC";
        SQLQuery query = session.createSQLQuery(hql);
        query.addScalar("geneID", Hibernate.STRING);
        query.addScalar("geneSymbol", Hibernate.STRING);
        query.addScalar("numOfFig", Hibernate.INTEGER);
        query.setString("anatomyZdbID", anatomyTerm.getZdbID());
        query.setBoolean("expressionFound", true);
        query.setBoolean("isWildtype", true);
        query.setString("condition", Experiment.STANDARD);
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
    public int getTotalNumberOfFiguresPerAnatomyItem(AnatomyItem anatomyTerm) {

        Session session = HibernateUtil.currentSession();
        String hql = "select count(distinct fig) from Figure fig, ExpressionResult res " +
                "WHERE res.anatomyTerm = :aoTerm " +
                "AND fig member of res.figures " +
                "AND res.expressionFound = :expressionFound " +
                "AND res.expressionExperiment.genotypeExperiment.experiment.name = :condition " +
                "AND res.expressionExperiment.marker.abbreviation not like :withdrawn " +
                "AND not exists ( " +
                "select 1 from Clone clone where " +
                "           clone.zdbID = res.expressionExperiment.clone.zdbID AND " +
                "           clone.problem  <> :chimeric " +
                ")";
        Query query = session.createQuery(hql);
        query.setBoolean("expressionFound", true);
        query.setParameter("aoTerm", anatomyTerm);
        query.setString("condition", Experiment.STANDARD);
        query.setString("withdrawn", Marker.WITHDRAWN + "%");
        query.setString("chimeric", Clone.ProblemType.CHIMERIC.toString());
        return ((Number) query.uniqueResult()).intValue();
    }

    /**
     * Count the number of images from all publications that have a gene
     * expression in a given anatomy structure.
     *
     * @param anatomyTerm ao term
     * @return number
     */
    public int getTotalNumberOfImagesPerAnatomyItem(AnatomyItem anatomyTerm) {
        Session session = HibernateUtil.currentSession();
        String hql = "select count(image) from Image image, ExpressionResult res where " +
                "res.anatomyTerm = :aoTerm AND " +
                "image.figure member of res.figures AND " +
                "res.expressionFound = :expressionFound ";
        Query query = session.createQuery(hql);
        query.setBoolean("expressionFound", true);
        query.setParameter("aoTerm", anatomyTerm);
        return ((Number) query.uniqueResult()).intValue();
    }

    private List<MarkerStatistic> createMarkerStatistics(List<Object[]> list, AnatomyItem anatomyTerm) {
        if (list == null)
            return null;

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

    public void insertCanonicalMarker(CanonicalMarker canon) {
        Session session = HibernateUtil.currentSession();
        session.save(canon);
    }

    public boolean publicationExists(String canonicalPublicationZdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria query = session.createCriteria(Publication.class);
        query.setProjection(Projections.count("zdbID"));
        query.add(Restrictions.eq("zdbID", canonicalPublicationZdbID));
        return (1 ==((Number) query.uniqueResult()).intValue()) ;
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
                "      exp.clone.zdbID = :cloneID AND " +
                "      res member of exp.expressionResults AND " +
                "      figure member of res.figures " +
                "order by figure.orderingLabel   ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        query.setString("cloneID", probeID);
        List<Figure> figures = query.list();

        // Only pick out the distinct records.
        DistinctRootEntityResultTransformer trafo = new DistinctRootEntityResultTransformer();
        return trafo.transformList(figures);
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
        List<Figure> figures = crit.list();

        // Only pick out the distinct records.
        DistinctRootEntityResultTransformer trafo = new DistinctRootEntityResultTransformer();
        return trafo.transformList(figures);
    }

    private List<HighQualityProbe> createHighQualityProbeObjects(List<Object[]> list, AnatomyItem aoTerm) {
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
    public List<Figure> getFiguresPerProbeAndAnatomy(Marker gene, Marker clone, AnatomyItem aoTerm) {
        Session session = HibernateUtil.currentSession();

        StringBuilder hql = new StringBuilder("select figure from Figure figure, ExpressionExperiment exp, ");
        hql.append("ExpressionResult res ");
        hql.append("where exp.marker.zdbID = :geneID AND ");
        if (clone != null)
            hql.append("      exp.clone.zdbID = :cloneID AND ");
        hql.append("      res member of exp.expressionResults AND ");
        hql.append("      res.expressionFound = :expressionFound  AND ");
        hql.append("      res.anatomyTerm.zdbID = :aoZdbID AND ");
        hql.append("      figure member of res.figures ");
        hql.append("order by figure.orderingLabel    ");
        Query query = session.createQuery(hql.toString());
        query.setString("geneID", gene.getZdbID());
        if (clone != null)
            query.setString("cloneID", clone.getZdbID());
        query.setString("aoZdbID", aoTerm.getZdbID());
        query.setBoolean("expressionFound", true);
        List<Figure> figures = query.list();
        return figures;
    }

    @SuppressWarnings("unchecked")
    public List<Publication> getPublicationsWithFiguresPerProbeAndAnatomy(Marker gene, Marker subGene, AnatomyItem aoTerm) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct figure.publication from Figure figure, ExpressionExperiment exp, ExpressionResult res " +
                "where exp.marker.zdbID = :geneID AND " +
                "      exp.clone.zdbID = :cloneID AND " +
                "      res member of exp.expressionResults AND " +
                "      res.expressionFound = :expressionFound  AND " +
                "      res.anatomyTerm.zdbID = :aoZdbID AND " +
                "      figure member of res.figures ";
        Query query = session.createQuery(hql);
        query.setString("geneID", gene.getZdbID());
        query.setString("cloneID", subGene.getZdbID());
        query.setString("aoZdbID", aoTerm.getZdbID());
        query.setBoolean("expressionFound", true);
        return (List<Publication>) query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Publication> getPublicationsWithAccessionButNoDOI(int maxResults) {
        Session session = HibernateUtil.currentSession();
        Criteria query = session.createCriteria(Publication.class);
        query.add(Restrictions.isNull("doi"));
        query.add(Restrictions.isNotNull("accessionNumber"));
        query.add(Restrictions.ne("accessionNumber", "none"));
        query.addOrder(Order.desc("publicationDate"));
        if (maxResults >= 0) {
            query.setMaxResults(maxResults);
        }
        List<Publication> list = query.list();
        return list;
    }


    public boolean updatePublications(List<Publication> publicationList) {

        Session session = HibernateUtil.currentSession();

        for (Publication publication : publicationList) {
            if (publication.getDoi() != null) {
                session.update(publication);
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public List<Figure> getFiguresByMorpholinoAndAnatomy(Morpholino morpholino, AnatomyItem term) {
        Session session = HibernateUtil.currentSession();

        StringBuilder hql = new StringBuilder("select figure ");
        getBaseQueryForMorpholinoFigureData(hql);
        hql.append("order by figure.orderingLabel    ");
        Query query = session.createQuery(hql.toString());
        query.setString("markerID", morpholino.getZdbID());
        query.setString("aoZdbID", term.getZdbID());
        List<Figure> figures = query.list();
        return figures;
    }

    private void getBaseQueryForMorpholinoFigureData(StringBuilder hql) {
        hql.append("from Figure figure, Phenotype pheno, ");
        hql.append("GenotypeExperiment geno, Marker marker, Experiment exp, ExperimentCondition con ");
        hql.append("where marker.zdbID = :markerID AND ");
        hql.append("      geno.experiment = exp AND ");
        hql.append("      con.experiment = exp AND  ");
        hql.append("      marker = con.morpholino AND  ");
        hql.append("      pheno.genotypeExperiment = geno AND  ");
        hql.append("      figure member of pheno.figures AND ");
        hql.append("      ( pheno.patoSubTermzdbID = :aoZdbID OR pheno.patoSuperTermzdbID = :aoZdbID ) ");
    }


    public PaginationResult<Figure> getFiguresByGenoAndAnatomy(Genotype geno, AnatomyItem term) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct figure from Figure figure, Phenotype pheno, " +
                "GenotypeExperiment exp, Genotype geno " +
                "where geno.zdbID = :genoID AND " +
                "      exp.genotype = geno AND " +
                "      pheno.genotypeExperiment = exp  AND " +
                "      figure member of pheno.figures AND " +
                "      ( pheno.patoSubTermzdbID = :aoZdbID OR pheno.patoSuperTermzdbID = :aoZdbID ) " +
                "order by figure.orderingLabel    ";
        Query query = session.createQuery(hql);
        query.setString("genoID", geno.getZdbID());
        query.setString("aoZdbID", term.getZdbID());
        PaginationResult<Figure> paginationResult = new PaginationResult<Figure>(query.list());
        return paginationResult;
    }

    public PaginationResult<Publication> getPublicationsWithFigures(Genotype genotype, AnatomyItem aoTerm) {
        Session session = HibernateUtil.currentSession();
        Criteria pubs = session.createCriteria(Publication.class);
        Criteria phenotype = pubs.createCriteria("phenotypes");
        phenotype.add(Restrictions.or(
                Restrictions.eq("patoSubTermzdbID", aoTerm.getZdbID()),
                Restrictions.eq("patoSuperTermzdbID", aoTerm.getZdbID())));
        phenotype.add(Restrictions.isNotEmpty("figures"));
        Criteria genox = phenotype.createCriteria("genotypeExperiment");
        genox.add(Restrictions.eq("genotype", genotype));
        Criteria geno = genox.createCriteria("genotype");
        geno.add(Restrictions.eq("wildtype", false));
/*
        Criteria experiment = genox.createCriteria("experiment");
        experiment.add(Restrictions.in("name", new String[]{Experiment.STANDARD, Experiment.GENERIC_CONTROL}));
*/
        pubs.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return new PaginationResult<Publication>((List<Publication>) pubs.list());
    }


    public int getNumPublicationsWithFiguresPerGenotypeAndAnatomy(Genotype genotype, AnatomyItem aoTerm) {
        Session session = HibernateUtil.currentSession();

        String hql = " select count(distinct figure.publication.zdbID ) from " +
                " Figure figure, Phenotype phenotype " +
                "where " +
                "      ( phenotype.patoSubTermzdbID = :aoZdbID OR phenotype.patoSuperTermzdbID = :aoZdbID ) AND " +
                "      phenotype.genotypeExperiment.genotype.zdbID = :genoID AND " +
                "      figure member of phenotype.figures " +
                "";
        Query query = session.createQuery(hql);
        query.setString("genoID", genotype.getZdbID());
        query.setString("aoZdbID", aoTerm.getZdbID());

        return ((Number) (query.uniqueResult())).intValue() ;
    }

    /**
     * Retrieve the publications for the figures for a given morpholino and anatomy term
     *
     * @param morpholino Morpholino
     * @param aoTerm     anatomy Term
     * @return List of publications
     */
    public List<Publication> getPublicationsWithFiguresPerMorpholinoAndAnatomy(Morpholino morpholino, AnatomyItem aoTerm) {
        Session session = HibernateUtil.currentSession();

        StringBuilder hql = new StringBuilder("select figure ");
        getBaseQueryForMorpholinoFigureData(hql);
        hql.append("order by figure.orderingLabel    ");
        Query query = session.createQuery(hql.toString());
        query.setString("markerID", morpholino.getZdbID());
        query.setString("aoZdbID", aoTerm.getZdbID());
        List<Publication> publications = query.list();
        return publications;
    }

    /**
     * Retrieve figures for a given gene and anatomy term.
     *
     * @param marker      marker
     * @param anatomyTerm anatomy
     * @return a set of figures
     */
    @SuppressWarnings("unchecked")
    public List<Figure> getFiguresByGeneAndAnatomy(Marker marker, AnatomyItem anatomyTerm) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct fig from Figure fig, ExpressionResult res, Marker marker, ExpressionExperiment exp, " +
                "     Genotype geno, GenotypeExperiment genox, Experiment experiment " +
                "where " +
                "   marker = :marker AND " +
                "   exp.marker = marker AND " +
                "   res.expressionExperiment = exp AND " +
                "   res.anatomyTerm = :aoTerm AND " +
                "   fig member of res.figures AND " +
                "   res.expressionFound = :expressionFound AND " +
                "   exp.genotypeExperiment = genox AND " +
                "   genox.experiment = experiment AND " +
                "   experiment.name = :condition AND " +
                "   genox.genotype = geno AND " +
                "   geno.wildtype = :isWildtype ";
        Query query = session.createQuery(hql);
        query.setBoolean("expressionFound", true);
        query.setBoolean("isWildtype", true);
        query.setParameter("aoTerm", anatomyTerm);
        query.setParameter("marker", marker);
        query.setParameter("condition", Experiment.STANDARD);
        return (List<Figure>) query.list();
    }


    public Journal getJournalByTitle(String journalTitle) {
        try {
            Session session = HibernateUtil.currentSession();
            Criteria criteria = session.createCriteria(Journal.class);
            criteria.add(Restrictions.eq("name", journalTitle));
            return (Journal) criteria.uniqueResult();
        }
        catch (Exception e) {
            logger.error("failed to get journal title[" + journalTitle + "] returning null", e);
            return null;
        }
    }


    /**
     * Utility method for filling list to a max amount.  This is a destructive method on fillList.
     * @param fillList  This list will get overwritten.
     * @param sourceList
     * @param maxFill
     * @return fillList
     */
    private Collection fillList(Collection fillList,Collection sourceList,int maxFill){
        Iterator iter = sourceList.iterator() ;
        while(fillList.size() < maxFill && iter.hasNext()){
            fillList.add(iter.next()) ;
        }
        return fillList ;
    }

    /**
     * From citgeneric.apg, includes references for:
     * 1 - direct attribution t omarker
     * 2 - attributed second marker relations
     * 3 - attributed first marker relations
     * 4 - attributed second marker relations where first marker is a morpholino (redundant?)
     * 5 - direct attribution to alias that links to marker
     * 6 - direct attribution to dblink that links to marker
     * 7 - direct attribution to orthologue evidence that links to marker
     * 8 - direct attribution to GO evidence that links to marker
     * 9 - direct attribution to feature evidence that links to marker
     * 10 - direct attribution to genotype feature evidence that links to marker somehow through feature
     * 11 - direct attribution to genotype feature evidence that links to marker directly
     * @param marker
     * @param maxPubs
     * @return The number of markers and any publications.
     */
    public PaginationResult<Publication> getAllAssociatedPublicationsForMarker(Marker marker, int maxPubs){

        PaginationResult<Publication> paginationResult = new PaginationResult<Publication>() ;
        Set<Publication> pubList = new HashSet<Publication>() ;
        Query query ;
        String hql ;
        List<Publication> resultList ;
        Session session = HibernateUtil.currentSession() ;

        // short list:
        hql = "select p.publication " +
                " from PublicationAttribution p " +
                " where p.dataZdbID = :markerZdbID " ;
        query = session.createQuery(hql) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        resultList = query.list() ;
        pubList.addAll(resultList) ;

        hql = "select p.publication " +
                " from PublicationAttribution p , MarkerRelationship mr " +
                " where p.dataZdbID = mr.zdbID " +
                " and mr.secondMarker.zdbID = :markerZdbID " ;
        query = session.createQuery(hql) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        resultList = query.list() ;
        pubList.addAll(resultList) ;

        hql = "select p.publication " +
                " from PublicationAttribution p , MarkerRelationship mr " +
                " where p.dataZdbID = mr.zdbID " +
                " and mr.firstMarker.zdbID = :markerZdbID " ;
        query = session.createQuery(hql) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        resultList = query.list() ;
        pubList.addAll(resultList) ;

        hql = "select p.publication " +
                " from PublicationAttribution p , MarkerRelationship mr " +
                " where p.dataZdbID = mr.firstMarker.zdbID " +
                " and mr.secondMarker.zdbID = :markerZdbID " +
                " and mr.firstMarker.markerType = :markerType " ;
        query = session.createQuery(hql) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        query.setString("markerType",Marker.Type.MRPHLNO.name()) ;
        resultList = query.list() ;
        pubList.addAll(resultList) ;

        hql = "select p.publication " +
                " from PublicationAttribution p , DataAlias  da " +
                "  where p.dataZdbID = da.zdbID " +
                " and da.dataZdbID = :markerZdbID " ;
        query = session.createQuery(hql) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        resultList = query.list() ;
        pubList.addAll(resultList) ;


        hql = "select p.publication " +
                " from PublicationAttribution p , MarkerDBLink dbl " +
                "  where dbl.zdbID = p.dataZdbID " +
                "  and dbl.marker.zdbID = :markerZdbID " ;
        query = session.createQuery(hql) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        resultList = query.list() ;
        pubList.addAll(resultList) ;

        hql = "select p.publication " +
                " from PublicationAttribution p , OrthoEvidenceDisplay oed " +
                "  where oed.zdbID = p.dataZdbID " +
                "  and oed.gene.zdbID = :markerZdbID " ;
        query = session.createQuery(hql) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        resultList = query.list() ;
        pubList.addAll(resultList) ;

        hql = "select p.publication " +
                " from PublicationAttribution p , MarkerGoTermEvidence mgte " +
                "  where mgte.zdbID = p.dataZdbID " +
                "  and mgte.marker.zdbID  = :markerZdbID " ;
        query = session.createQuery(hql) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        resultList = query.list() ;
        pubList.addAll(resultList) ;

        hql = "select p.publication " +
                " from PublicationAttribution p , FeatureMarkerRelationship fmr " +
                " where fmr.featureZdbId  = p.dataZdbID " +
                "  and fmr.marker.zdbID = :markerZdbID " ;
        query = session.createQuery(hql) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        resultList = query.list() ;
        pubList.addAll(resultList) ;

        hql = "select p.publication " +
                " from PublicationAttribution p , GenotypeFeature gtf, FeatureMarkerRelationship fmr " +
                "  where gtf.genotype.zdbID  = p.dataZdbID " +
                "  and fmr.featureZdbId = gtf.feature.zdbID" +
                "  and fmr.marker.zdbID = :markerZdbID  " ;
        query = session.createQuery(hql) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        resultList = query.list() ;
        pubList.addAll(resultList) ;

        hql = "select p.publication " +
                " from PublicationAttribution p , GenotypeFeature gtf " +
                " where gtf.genotype.zdbID  = p.dataZdbID " +
                "  and gtf.feature.zdbID = :markerZdbID " ;
        query = session.createQuery(hql) ;
        query.setString("markerZdbID",marker.getZdbID()) ;
        resultList = query.list() ;
        pubList.addAll(resultList) ;

        String zdbIDs = "" ;
        for(Publication pub: pubList){
            zdbIDs += pub.getZdbID()+"\n" ;
        }

        if(maxPubs>=0){
            paginationResult.setPopulatedResults((new ArrayList(pubList)).subList(0,maxPubs));
        }
        else{
            paginationResult.setPopulatedResults(new ArrayList(pubList));
        }
        paginationResult.setTotalCount(pubList.size());
        return paginationResult ;
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
    public PaginationResult<Publication> getPublicationsWithFigures(Marker marker, AnatomyItem anatomyTerm) {
        Session session = HibernateUtil.currentSession();

        Criteria pubs = session.createCriteria(Publication.class);
        Criteria expresssion = pubs.createCriteria("expressionExperiments");
        expresssion.add(Restrictions.eq("marker", marker));
        Criteria result = expresssion.createCriteria("expressionResults");
        result.add(Restrictions.isNotEmpty("figures"));
        result.add(Restrictions.eq("anatomyTerm", anatomyTerm));
        result.add(Restrictions.eq("expressionFound", true));
        Criteria genox = expresssion.createCriteria("genotypeExperiment");
        Criteria genotype = genox.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));
        Criteria experiment = genox.createCriteria("experiment");
        experiment.add(Restrictions.eq("name", Experiment.STANDARD));
        pubs.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        return new PaginationResult<Publication>((List<Publication>) pubs.list());
    }

    @SuppressWarnings("unchecked")
    public List<ExpressionExperiment> getExperiments(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select experiment from ExpressionExperiment experiment" +
                "       left join experiment.marker as gene " +
                "     where experiment.publication.zdbID = :pubID " +
                "    order by gene.abbreviationOrder, " +
                "             experiment.genotypeExperiment.genotype.nickname, " +
                "             experiment.assay.displayOrder ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);

        return (List<ExpressionExperiment>) query.list();

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
                "            and experiment.marker = marker " +
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

    @SuppressWarnings("unchecked")
    public List<Genotype> getFishUsedInExperiment(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct fish from Genotype fish, ExpressionExperiment ee," +
                "                               GenotypeExperiment genox " +
                "     where ee.publication.zdbID = :pubID " +
                "           and ee.genotypeExperiment = genox " +
                "           and genox.genotype = fish" +
                "    order by fish.handle ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);

        return (List<Genotype>) query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Experiment> getExperimentsByPublication(String publicationID) {
        Session session = HibernateUtil.currentSession();

        Criteria crit = session.createCriteria(Experiment.class);
        crit.add(Restrictions.in("name", new String[]{Experiment.STANDARD, Experiment.GENERIC_CONTROL}));
        List<Experiment> experimentList = (List<Experiment>) crit.list();


        String hql = "select distinct experiment from Experiment experiment" +
                "     where experiment.publication.zdbID = :pubID" +
                "    order by experiment.name ";
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);

        experimentList.addAll(query.list());
        return experimentList;

    }

    @SuppressWarnings("unchecked")
    public Genotype getGenotypeByNickname(String nickname) {
        Session session = HibernateUtil.currentSession();

        Criteria crit = session.createCriteria(Genotype.class);
        crit.add(Restrictions.eq("nickname", nickname));
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


}
