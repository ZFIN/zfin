package org.zfin.publication.repository;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.zfin.antibody.Antibody;
import org.zfin.curation.presentation.CorrespondenceDTO;
import org.zfin.curation.presentation.PersonDTO;
import org.zfin.curation.service.CurationDTOConversionService;
import org.zfin.database.SearchUtil;
import org.zfin.expression.*;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.SourceAlias;
import org.zfin.marker.*;
import org.zfin.marker.presentation.GeneBean;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.Fish;
import org.zfin.mutant.FishExperiment;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.orthology.Ortholog;
import org.zfin.profile.Person;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.*;
import org.zfin.publication.presentation.*;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.MarkerDBLink;

import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * ToDO: include documentation
 */
@Repository
public class HibernatePublicationRepository extends PaginationUtil implements PublicationRepository {

    @Autowired
    private CurationDTOConversionService converter;

    @Autowired
    private PublicationService publicationService;

    Logger logger = LogManager.getLogger(HibernatePublicationRepository.class);

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    private ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();

    public List<Publication> getExpressedGenePublications(String geneID, String anatomyItemID) {
        Session session = HibernateUtil.currentSession();
        String hql = """
                SELECT distinct publication FROM Publication publication, ExpressionExperiment exp, ExpressionResult res  \s
                WHERE (res.entity.superterm.zdbID = :aoZdbID OR
                       res.entity.subterm.zdbID = :aoZdbID)
                AND publication = exp.publication
                AND res.expressionExperiment = exp
                AND exp.gene.zdbID = :zdbID
                AND res.expressionFound is true""";
        String sql = addOrderByParameters(hql);
        Query<Publication> query = session.createQuery(sql, Publication.class);
        addPaginationParameters(query);
        query.setParameter("zdbID", geneID);
        query.setParameter("aoZdbID", anatomyItemID);
        return query.list();
    }

    public List<String> getSNPPublicationIDs(Marker marker) {
        Session session = HibernateUtil.currentSession();
        String sql = """
                select distinct snpdattr_pub_zdb_id
                 from snp_download_attribution, snp_download
                 where snpdattr_snpd_pk_id = snpd_pk_id and snpd_mrkr_zdb_id = :zdbID""";
        Query<Tuple> query = session.createNativeQuery(sql, Tuple.class);
        query.setParameter("zdbID", marker.getZdbID());
        List<Tuple> pubIDs = query.list();
        return pubIDs.stream().map(tuple -> tuple.get(0, String.class)).toList();
    }

    //TODO: refactor to JPA Criteria?
    //TODO: ScrollableResults makes it difficult to refactor to Tuple-based hql
    public PaginationResult<HighQualityProbe> getHighQualityProbeNames(Term term, int maxRow) {

        String hql = """
                select distinct exp.probe, marker
                FROM ExpressionExperiment exp, ExpressionResult res, Marker marker
                WHERE  res.entity.superterm = :term
                AND res.expressionExperiment = exp
                AND exp.probe.rating = 4
                AND exp.gene = marker
                ORDER by marker.abbreviationOrder""";
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

    //TODO: refactor to JPA Criteria?
    //TODO: ScrollableResults makes it difficult to refactor to Tuple-based hql
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

        // todo: ...
        // todo: Rewrite as HQL and include the whole marker object as it is needed.
        // todo: note that when in SQL, start at 1 (current) , but when in HQL, start at 0
        String sql = """
                SELECT exp.xpatex_gene_zdb_id as geneID, gene.mrkr_abbrev as geneSymbol,
                count(distinct fig.fig_zdb_id) as numOfFig 
                FROM  Expression_Experiment exp
                      join FISH_EXPERIMENT as genox on genox.genox_zdb_id=exp.xpatex_genox_zdb_id
                      join EXPRESSION_RESULT as result on result.xpatres_xpatex_zdb_id = exp.xpatex_zdb_id
                      join EXPRESSION_PATTERN_FIGURE as results on results.xpatfig_xpatres_zdb_id=result.xpatres_zdb_id
                      join Figure as fig on fig.fig_zdb_id=results.xpatfig_fig_zdb_id
                      join MARKER as gene on exp.xpatex_gene_zdb_id = gene.mrkr_zdb_id
                      join FISH as fish on fish.fish_zdb_Id = genox.genox_fish_zdb_id
                      join TERM as item_ on (result.xpatres_superterm_zdb_id = item_.term_zdb_id OR result.xpatres_subterm_zdb_id = item_.term_zdb_id)
                WHERE  item_.term_zdb_id = :termID AND
                       result.xpatres_expression_found is true AND
                       fish.fish_is_wildtype is true AND
                       genox.genox_is_std_or_generic_control is true AND
                       SUBSTRING (gene.mrkr_abbrev from  1 for 9) <> :withdrawn  AND  
                       not exists(
                           select 'x' from clone
                           where clone.clone_mrkr_zdb_id = exp.xpatex_probe_feature_zdb_id
                           and clone.clone_problem_type = :chimeric
                       ) AND
                       not exists(
                           select 'x' from marker m2
                           where m2.mrkr_zdb_id = exp.xpatex_probe_feature_zdb_id
                           and SUBSTRING (m2.mrkr_abbrev from 1 for 9) = :withdrawn 
                       )
                GROUP BY exp.xpatex_gene_zdb_id, gene.mrkr_abbrev
                ORDER BY numOfFig DESC, geneSymbol
                """;
        SQLQuery query = session.createSQLQuery(sql);
        query.addScalar("geneID", StringType.INSTANCE);
        query.addScalar("geneSymbol", StringType.INSTANCE);
        query.addScalar("numOfFig", IntegerType.INSTANCE);
        query.setParameter("termID", anatomyTerm.getZdbID());
        query.setParameter("withdrawn", Marker.WITHDRAWN);
        query.setParameter("chimeric", Clone.ProblemType.CHIMERIC.toString()); // todo: use enum here
        ScrollableResults results = query.scroll();

        List<Object[]> list = new ArrayList<>();
        results.beforeFirst();
        if (firstRow > 0) {
            results.setRowNumber(firstRow - 1);
        }
        while (results.next() && results.getRowNumber() < firstRow + numberOfRecords) {
            if (results.getRowNumber() >= firstRow) {
                list.add(results.get());
            }
        }

        results.last();
        int totalResults = results.getRowNumber() + 1;

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

        String sql = """
                select count(distinct fig_zdb_id)
                from figure
                     join expression_pattern_figure on xpatfig_fig_zdb_id = fig_zdb_id
                     join expression_result on xpatres_zdb_id = xpatfig_xpatres_zdb_id
                     join expression_experiment on xpatex_zdb_id = xpatres_xpatex_zdb_id
                     join fish_experiment on genox_zdb_id = xpatex_genox_zdb_id
                     join marker on mrkr_zdb_id = xpatex_gene_zdb_id
                where (xpatres_superterm_zdb_id = :termZdbId or xpatres_subterm_zdb_id = :termZdbId)
                   and xpatres_expression_found is true
                   and genox_is_std_or_generic_control is true
                   and mrkr_abbrev not like :withdrawn
                   and not exists (select 'x' from clone
                                   where clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
                                     and clone_problem_type <> :chimeric);
                """;

        Query<Number> query = session.createNativeQuery(sql);

        query.setParameter("termZdbId", anatomyTerm.getZdbID());
        query.setParameter("withdrawn", Marker.WITHDRAWN + "%");
        query.setParameter("chimeric", Clone.ProblemType.CHIMERIC.toString());

        return ((Number) query.getSingleResult()).intValue();
    }

    public Publication getPublication(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return session.get(Publication.class, zdbID);
    }

    //TODO: merge this with the alternate below and always order by date desc?
    //TODO: Seems fairly safe to have a default ordering
    public List<Publication> getPublications(List<String> zdbIDs) {
        return getPublications(zdbIDs, false);
    }

    public List<Publication> getPublications(List<String> zdbIDs, boolean orderByDateDesc) {
        if (CollectionUtils.isEmpty(zdbIDs)) {
            return Collections.emptyList();
        }
        Session session = HibernateUtil.currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Publication> cr = cb.createQuery(Publication.class);

        Root<Publication> root = cr.from(Publication.class);
        cr.select(root).where(root.get("zdbID").in(zdbIDs));
        if (orderByDateDesc) {
            cr.orderBy(cb.desc(root.get("publicationDate")));
        }

        return session.createQuery(cr).list();
    }

    public boolean publicationExists(String canonicalPublicationZdbID) {
        return getPublication(canonicalPublicationZdbID) != null;
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

    @Override
    public List<Publication> getPubsForDisplay(String zdbID) {

        List<String> publicationIDs = HibernateUtil.currentSession()
                .createSQLQuery(getCommonPublicationSQL(zdbID))
                .setString("markerZdbID", zdbID)
                .list();

        if (CollectionUtils.isEmpty(publicationIDs)) {
            return new ArrayList<>();
        }

        return getPublications(publicationIDs, true);
    }

    @Override
    public List<Journal> getAllJournals() {
        Session session = HibernateUtil.currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Journal> cr = cb.createQuery(Journal.class);

        Root<Journal> root = cr.from(Journal.class);
        cr.select(root).orderBy(cb.asc(root.get("name")));

        return session.createQuery(cr).list();
    }

    @Override
    public Journal getJournalByAbbreviation(String abbreviation) {
        return getJournalByProperty("abbreviation", abbreviation);
    }

    public Journal getJournalByPrintIssn(String pIssn) {
        return getJournalByProperty("printIssn", pIssn);
    }

    public Journal getJournalByEIssn(String eIssn) {
        return getJournalByProperty("onlineIssn", eIssn);
    }

    //TODO: refactor this one? Seems like we could at least combine all if statements into a single one
    public String getCommonPublicationSQL(String zdbID) {
        // Changes to this query need to be kept in sync with the analogous query
        // in db-data-config.sql!

        ActiveData.Type dataType = ActiveData.getType(zdbID);
        String commonPubSQL =
            " select * from (select ra.recattrib_source_zdb_id   " +
                " from record_attribution ra   " +
                " where :markerZdbID = ra.recattrib_data_zdb_id ";

        // marker relationship 2_1
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += " union " +
                " select ra.recattrib_source_zdb_id  " +
                " from record_attribution ra , marker_relationship mr " +
                " where :markerZdbID = mr.mrel_mrkr_2_zdb_id " +
                " and  ra.recattrib_data_zdb_id = mr.mrel_zdb_id ";
        }
        // marker relationship 1_2
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += " union " +
                " select ra.recattrib_source_zdb_id  " +
                " from record_attribution ra , marker_relationship mr " +
                " where :markerZdbID = mr.mrel_mrkr_1_zdb_id " +
                " and  ra.recattrib_data_zdb_id = mr.mrel_zdb_id ";
        }
        // str marker type necessary ?
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += " union " +
                " select ra.recattrib_source_zdb_id  " +
                " from record_attribution ra , marker_relationship mr , marker m " +
                " where :markerZdbID = mr.mrel_mrkr_2_zdb_id " +
                " and  ra.recattrib_data_zdb_id = mr.mrel_mrkr_1_zdb_id " +
                " and  mr.mrel_mrkr_1_zdb_id = m.mrkr_zdb_id " +
                " and  m.mrkr_type in ('MRPHLNO', 'TALEN', 'CRISPR') ";
        }
        // data alias
        commonPubSQL += " union " +
            " select ra.recattrib_source_zdb_id  " +
            " from record_attribution ra , data_alias da  " +
            " where da.dalias_zdb_id = ra.recattrib_data_zdb_id " +
            " and :markerZdbID = da.dalias_data_zdb_id ";
        // db link
        commonPubSQL += " union " +
            " select ra.recattrib_source_zdb_id  " +
            " from record_attribution ra , db_link dbl  " +
            " where  dbl.dblink_zdb_id  = ra.recattrib_data_zdb_id " +
            " and  :markerZdbID = dbl.dblink_linked_recid ";
        // db link, marker_relationship
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += " union " +
                " select ra.recattrib_source_zdb_id  " +
                " from record_attribution ra , db_link dbl , marker_relationship mr " +
                " where  dbl.dblink_zdb_id  = ra.recattrib_data_zdb_id " +
                " and dbl.dblink_linked_recid = mr.mrel_mrkr_2_zdb_id " +
                " and  :markerZdbID = mr.mrel_mrkr_1_zdb_id " +
                " and  mr.mrel_type = 'gene encodes small segment' ";
        }
        // ortho
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += " union " +
                " select ra.recattrib_source_zdb_id  " +
                " from record_attribution ra ,  ortholog_evidence oe, ortholog o " +
                " where  ra.recattrib_data_zdb_id = oe.oev_ortho_zdb_id " +
                " and    oe.oev_ortho_zdb_id = o.ortho_zdb_id " +
                " and    :markerZdbID = o.ortho_zebrafish_gene_zdb_id ";
        }
        // marker_go_term_Evidence
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += " union " +
                " select ra.recattrib_source_zdb_id  " +
                " from record_attribution ra ,  marker_go_term_evidence ev " +
                " where  ev.mrkrgoev_zdb_id  = ra.recattrib_data_zdb_id " +
                " and  :markerZdbID = ev.mrkrgoev_mrkr_zdb_id ";
        }
        // feature_marker_relationship
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += " union " +
                " select ra.recattrib_source_zdb_id  " +
                " from record_attribution ra ,  feature_marker_relationship fmr " +
                " where  fmr.fmrel_ftr_zdb_id  = ra.recattrib_data_zdb_id " +
                " and  :markerZdbID = fmr.fmrel_mrkr_zdb_id ";
        }
        // feature_marker_relationship, genotype_feature
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += " union " +
                " select ra.recattrib_source_zdb_id  " +
                " from record_attribution ra ,  feature_marker_relationship fmr, genotype_feature gf " +
                " where  gf.genofeat_geno_zdb_id  = ra.recattrib_data_zdb_id " +
                " and  :markerZdbID = fmr.fmrel_mrkr_zdb_id " +
                " and fmr.fmrel_ftr_zdb_id  = gf.genofeat_feature_zdb_id ";
        }
        // expression_experiment
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += " union " +
                " select xpatex_source_zdb_id  " +
                " from expression_experiment " +
                " where :markerZdbID = xpatex_gene_zdb_id ";
        }
        // nomenclature
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += " union " +
                " select ra.recattrib_source_zdb_id  " +
                " from record_attribution ra, marker_history mh " +
                " where mh.mhist_zdb_id  = ra.recattrib_data_zdb_id " +
                " and  :markerZdbID = mh.mhist_mrkr_zdb_id ";
        }
        commonPubSQL += " ) as qt where recattrib_source_zdb_id like 'ZDB-PUB%'  ";
        return commonPubSQL;
    }

    public SourceAlias addJournalAlias(Journal journal, String alias) {
        //first handle the alias..

        SourceAlias journalAlias = new SourceAlias();
        journalAlias.setDataZdbID(journal.getZdbID());
        journalAlias.setAlias(alias);

        if (journal.getAliases() == null) {
            Set<SourceAlias> sourceAliases = new HashSet<>();
            sourceAliases.add(journalAlias);
            journal.setAliases(sourceAliases);
        } else {
            // if alias exists do not add continue...
            if (!journal.getAliases().add(journalAlias)) {
                return null;
            }
        }

        currentSession().save(journalAlias);

        //now handle the attribution
     /*   String updateComment;

        updateComment = "Added alias: '" + journalAlias.getAlias() + " with no attribution";


        InfrastructureService.insertUpdate(journal, updateComment);
*/
        return journalAlias;
    }

    @Override
    public int getNumberAssociatedPublicationsForZdbID(String zdbID) {
        String sql = " select count(*) from ( " + getCommonPublicationSQL(zdbID) + " ) as query ";

        Session session = currentSession();
        Query<Number> query = session.createNativeQuery(sql);

        query.setParameter("markerZdbID", zdbID);

        return ((Number) query.getSingleResult()).intValue();
    }

    /**
     * Retrieve Figure by ID
     *
     * @param zdbID ID
     * @return Figure
     */
    public Figure getFigure(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return session.get(Figure.class, zdbID);
    }

    public Figure getFigureByID(String zdbID) {
        return getFigure(zdbID);
    }

    public Image getImageById(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return session.get(Image.class, zdbID);
    }

    /** PLACEHOLDER **/
    @SuppressWarnings("unchecked")
    public PaginationResult<Publication> getPublicationsWithFigures(Marker marker, GenericTerm anatomyTerm) {
        Session session = HibernateUtil.currentSession();


        Criteria pubs = session.createCriteria(Publication.class);





        Criteria expression = pubs.createCriteria("expressionExperiments");



        expression.add(Restrictions.eq("gene", marker));

        Criteria result = expression.createCriteria("expressionResults");

        result.add(Restrictions.isNotEmpty("figures"));
        result.add(Restrictions.or(Restrictions.eq("entity.superterm", anatomyTerm), Restrictions.eq("entity.subterm", anatomyTerm)));
        result.add(Restrictions.eq("expressionFound", true));


        Criteria genox = expression.createCriteria("fishExperiment");
        genox.add(Restrictions.eq("standardOrGenericControl", true));


        Criteria fish = genox.createCriteria("fish");


        Criteria genotype = fish.createCriteria("genotype");
        genotype.add(Restrictions.eq("wildtype", true));


        pubs.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);




        return new PaginationResult<Publication>((List<Publication>) pubs.list());
    }

    @Override
    public PaginationResult<Publication> getPublicationsWithFigures_New(Marker marker, GenericTerm anatomyTerm) {
        Session session = HibernateUtil.currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Publication> query = cb.createQuery(Publication.class);

        // Define the root of the query
        Root<Publication> pubs = query.from(Publication.class);

        // Join the expressionExperiments property
        Join<Publication, ExpressionExperiment> expressionExperiments = pubs.join("expressionExperiments", JoinType.INNER);

        // Define the predicates for the query
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(expressionExperiments.get("gene"), marker));

        Join<ExpressionExperiment, ExpressionResult> expressionResults = expressionExperiments.join("expressionResults", JoinType.INNER);

        predicates.add(cb.isNotEmpty(expressionResults.get("figures"))); //compare to previous version, is expressionExperiments the right object for the .get(...) call?
        predicates.add(cb.or(cb.equal(expressionResults.get("entity.superterm"), anatomyTerm), cb.equal(expressionExperiments.get("entity.subterm"), anatomyTerm)));
        predicates.add(cb.equal(expressionResults.get("expressionFound"), true));

        // Join the fishExperiment property
        Join<ExpressionExperiment, FishExperiment> genox = expressionExperiments.join("fishExperiment", JoinType.INNER);
        predicates.add(cb.equal(genox.get("standardOrGenericControl"), true));

        // Join the fish property
        Join<FishExperiment, Fish> fish = genox.join("fish", JoinType.INNER);

        // Join the genotype property
        Join<Fish, Genotype> genotype = fish.join("genotype", JoinType.INNER);
        predicates.add(cb.equal(genotype.get("wildtype"), true));

        // Add the predicates to the query and apply DISTINCT_ROOT_ENTITY result transformer
        query.where(predicates.toArray(new Predicate[0])).distinct(true);

        List<Publication> results = session.createQuery(query).getResultList();

        // Execute the query and return the result
        return new PaginationResult<Publication>(results);
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
     * Retrieve distinct list of genes (GENEDOM_AND_EFG) that are attributed to a given publication.
     *
     * @param pubID publication id
     * @return list of markers
     */
    @SuppressWarnings("unchecked")
    public List<Marker> getGenesByPublication(String pubID) {
        return getGenesByPublication(pubID, true);
        //return getGenesByPublication(pubID, false);
    }

    /**
     * Retrieve distinct list of genes (GENEDOM_AND_EFG if includeEgfs is true; just GENEDOM otherwise) that are
     * attributed to a given publication.
     *
     * @param pubID       publication id
     * @param includeEfgs boolean
     * @return list of markers
     */
    public List<Marker> getGenesByPublication(String pubID, boolean includeEFGs) {
        Marker.TypeGroup typeGroup = Marker.TypeGroup.GENEDOM;
        List<MarkerType> markerTypes = markerRepository.getMarkerTypesByGroup(typeGroup);
        if (includeEFGs) {
            markerTypes.add(markerRepository.getMarkerTypeByName(Marker.Type.EFG.toString()));
        }
        return (List<Marker>) getMarkersByPublication(pubID, markerTypes);
    }

    public List<Marker> getSTRByPublication(String pubID) {
        Marker.TypeGroup typeGroup = Marker.TypeGroup.KNOCKDOWN_REAGENT;
        List<MarkerType> markerTypes = markerRepository.getMarkerTypesByGroup(typeGroup);

        return (List<Marker>) getMarkersByPublication(pubID, markerTypes);
    }

    public List<Marker> getGenesAndMarkersByPublication(String pubID) {
        // directly annotated markers
        List<MarkerType> markerTypes = markerRepository.getMarkerTypesByGroup(Marker.TypeGroup.GENEDOM_AND_NTR);
        markerTypes.addAll(markerRepository.getMarkerTypesByGroup(Marker.TypeGroup.SEARCH_MK));
        Set<Marker> markers = new TreeSet<>(getMarkersByPublication(pubID, markerTypes));

        // markers pulled through features
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct marker from Marker marker, RecordAttribution attr, Feature feature, FeatureMarkerRelationship fmrel " +
            "where attr.dataZdbID = feature.zdbID " +
            "and attr.sourceZdbID = :pubID " +
            "and fmrel.type = :isAllele " +
            "and fmrel.feature = feature " +
            "and fmrel.marker = marker ";
        Query query = session.createQuery(hql);
        query.setString("pubID", pubID);
        query.setParameter("isAllele", FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF);
        markers.addAll(query.list());

        // markers pulled through STRs
        hql = "select distinct marker from Marker marker, RecordAttribution attr, MarkerRelationship mrel " +
            "where attr.sourceZdbID = :pubID " +
            "and attr.dataZdbID = mrel.firstMarker " +
            "and mrel.secondMarker = marker " +
            "and mrel.type = :type ";
        query = session.createQuery(hql);
        query.setString("pubID", pubID);
        query.setParameter("type", MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
        markers.addAll(query.list());

        return new ArrayList<>(markers);
    }

    public List<Marker> getMarkersByTypeForPublication(String pubID, MarkerType markerType) {
        return (List<Marker>) getMarkersByPublication(pubID, Collections.singletonList(markerType));
    }

    public List<SequenceTargetingReagent> getSTRsByPublication(String pubID, MarkerType markerType) {
        return (List<SequenceTargetingReagent>) getMarkersByPublication(pubID, Collections.singletonList(markerType));
    }

    public PaginationResult<Clone> getClonesByPublication(String pubID, PaginationBean paginationBean) {
        List<MarkerType> markerTypes = markerRepository.getMarkerTypesByGroup(Marker.TypeGroup.SEARCH_SEG);
        ScrollableResults results = getMarkersByPublicationQuery(pubID, markerTypes).scroll();
        return PaginationResultFactory.createResultFromScrollableResultAndClose(paginationBean, results);
    }

    private List getMarkersByPublication(String pubID, List<MarkerType> markerTypes) {
        return getMarkersByPublicationQuery(pubID, markerTypes).list();
    }

    private Query getMarkersByPublicationQuery(String pubID, List<MarkerType> markerTypes) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct marker from Marker marker, RecordAttribution attr" +
            "     where attr.dataZdbID = marker.zdbID" +
            "           and attr.sourceZdbID = :pubID " +
            "           and marker.markerType in (:markerType)  " +
            "    order by marker.abbreviationOrder ";
        Query query = session.createQuery(hql);
        query.setString("pubID", pubID);
        query.setParameterList("markerType", markerTypes);
        return query;
    }

    public List<Feature> getFeaturesByPublication(String pubID) {
        Session session = HibernateUtil.currentSession();


        String hql = "select distinct feature from Feature feature, PublicationAttribution pub" +
            "     where pub.dataZdbID = feature.zdbID" +
            "           and pub.publication.zdbID = :pubID " +
            "    order by feature.abbreviationOrder ";
        Query query = session.createQuery(hql);
        query.setString("pubID", pubID);


        return (List<Feature>) query.list();
    }

    public List<Fish> getFishByPublication(String pubID) {
        Session session = HibernateUtil.currentSession();


        String hql = "select distinct fish from Fish fish, PublicationAttribution pub" +
            "     where pub.dataZdbID = fish.zdbID" +
            "           and pub.publication.zdbID = :pubID " +
            "    order by fish.nameOrder ";
        Query query = session.createQuery(hql);
        query.setString("pubID", pubID);


        return (List<Fish>) query.list();
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
        crit.add(Restrictions.in("name", List.of(Experiment.STANDARD, Experiment.GENERIC_CONTROL)));
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
            "           and geno.wildtype = 'f'" +
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

    public List<Publication> getPublicationByPmid(Integer pubMedID) {
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

    @Override
    public List<Ortholog> getOrthologListByPub(String pubID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct ortho, ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder " +
            "from Ortholog as ortho " +
            "join ortho.evidenceSet as evidence " +
            "where evidence.publication.zdbID = :pubID " +
            "order by ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder";
        Query query = session.createQuery(hql);
        query.setString("pubID", pubID);
        query.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] objects, String[] strings) {
                return objects[0];
            }

            @Override
            public List transformList(List collection) {
                return collection;
            }
        });
        List<Ortholog> orthologList = (List<Ortholog>) query.list();
        return orthologList;
    }


    @Override
    public List<Ortholog> getOrthologListByMrkr(String mrkrID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct ortho, ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder " +
            "from Ortholog as ortho " +
            "join ortho.evidenceSet as evidence " +
            "where ortho.zebrafishGene.zdbID = :mrkrID " +
            "order by ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder";
        Query query = session.createQuery(hql);
        query.setString("mrkrID", mrkrID);
        query.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] objects, String[] strings) {
                return objects[0];
            }

            @Override
            public List transformList(List collection) {
                return collection;
            }
        });
        List<Ortholog> orthologList = (List<Ortholog>) query.list();
        return orthologList;
    }

    @Override
    public List<Ortholog> getOrthologPaginationByPub(String pubID) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct ortho, ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder " +
            "from Ortholog as ortho " +
            "join ortho.evidenceSet as evidence " +
            "where evidence.publication.zdbID = :pubID " +
            "order by ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder";
        Query query = session.createQuery(hql);
        query.setString("pubID", pubID);
        query.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] objects, String[] strings) {
                return objects[0];
            }

            @Override
            public List transformList(List collection) {
                return collection;
            }
        });
        return (List<Ortholog>) query.list();
    }

    @Override
    public PaginationResult<Ortholog> getOrthologPaginationByPub(String pubID, GeneBean searchBean) {
        Session session = HibernateUtil.currentSession();

        String hql = "select distinct ortho, ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder " +
            "from Ortholog as ortho " +
            "join ortho.evidenceSet as evidence " +
            "where evidence.publication.zdbID = :pubID " +
            "order by ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder";
        Query query = session.createQuery(hql);
        query.setString("pubID", pubID);
        query.setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] objects, String[] strings) {
                return objects[0];
            }

            @Override
            public List transformList(List collection) {
                return collection;
            }
        });
        List<Ortholog> orthologList = (List<Ortholog>) query.list();
        PaginationResult<Ortholog> paginationResult = PaginationResultFactory.createResultFromScrollableResultAndClose(
            searchBean.getFirstRecordOnPage() - 1, searchBean.getLastRecordOnPage(), query.scroll());
        paginationResult.setStart(searchBean.getFirstRecord());

        return paginationResult;
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
        SortedSet<Publication> pubList = new TreeSet<>();
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
        addPublication(publication, PublicationTrackingStatus.Name.NEW, null, null);
    }

    public void addPublication(Publication publication,
                               PublicationTrackingStatus.Name status,
                               PublicationTrackingLocation.Name location,
                               Person owner) {
        Session session = HibernateUtil.currentSession();
        HibernateUtil.createTransaction();
        session.save(publication);
        PublicationTrackingHistory trackingEntry = new PublicationTrackingHistory();
        PublicationTrackingStatus newStatus = getPublicationStatusByName(status);
        if (location != null) {
            PublicationTrackingLocation newLocation = getPublicationTrackingLocationByName(location);
            trackingEntry.setLocation(newLocation);
        }
        if (owner != null) {
            trackingEntry.setOwner(owner);
        }
        trackingEntry.setPublication(publication);
        trackingEntry.setStatus(newStatus);
        trackingEntry.setUpdater(ProfileService.getCurrentSecurityUser());
        trackingEntry.setDate(new GregorianCalendar());
        session.save(trackingEntry);
        HibernateUtil.flushAndCommitCurrentSession();
    }



    public long getMarkerCount(Publication publication) {
        String sql = "select count(*) FROM (" +
            "  SELECT fmrel_mrkr_zdb_id" +
            "  FROM record_attribution, feature_marker_relationship" +
            "  WHERE recattrib_source_zdb_id = :zdbID" +
            "        AND recattrib_data_zdb_id = fmrel_ftr_zdb_id" +
            "        AND fmrel_type = 'is allele of'" +
            "" +
            "  UNION" +
            "" +
            "  SELECT mrkr_zdb_id" +
            "  FROM record_attribution, marker" +
            "  WHERE recattrib_source_zdb_id = :zdbID" +
            "        AND recattrib_data_zdb_id = mrkr_zdb_id" +
            "        AND mrkr_type IN" +
            "            (" +
            "              SELECT mtgrpmem_mrkr_type" +
            "              FROM marker_type_group_member" +
            "              WHERE mtgrpmem_mrkr_type_group in ('GENEDOM_AND_NTR','SEARCH_MK')" +
            "            )" +
            "        AND (mrkr_type <> 'MRPHLNO' AND mrkr_type <> 'EFG')  " +
            "" +
            "  UNION" +
            "" +
            "  SELECT mr.mrel_mrkr_2_zdb_id" +
            "  FROM record_attribution ra, marker m, marker_relationship mr" +
            "  WHERE recattrib_source_zdb_id = :zdbID" +
            "        AND recattrib_data_zdb_id = mrkr_zdb_id" +
            "        AND m.mrkr_zdb_id = mr.mrel_mrkr_1_zdb_id" +
            "        AND mrel_type = 'knockdown reagent targets gene'  " +
            ") as q3 ;";

        return getCount(sql, publication.getZdbID());
    }

    public long getMorpholinoCount(Publication publication) {
        return getMarkerCountByMarkerType(publication.getZdbID(), Marker.Type.MRPHLNO.toString());
    }

    public long getTalenCount(Publication publication) {
        return getMarkerCountByMarkerType(publication.getZdbID(), Marker.Type.TALEN.toString());
    }

    public long getCrisprCount(Publication publication) {
        return getMarkerCountByMarkerType(publication.getZdbID(), Marker.Type.CRISPR.toString());
    }

    public long getAntibodyCount(Publication publication) {
        return getMarkerCountByMarkerType(publication.getZdbID(), Marker.Type.ATB.toString());
    }

    public long getEfgCount(Publication publication) {
        return getMarkerCountByMarkerType(publication.getZdbID(), Marker.Type.EFG.toString());
    }

    public long getCloneProbeCount(Publication publication) {
        String sql = "\tselect count(recattrib_data_zdb_id)" +
            "\t from  record_attribution, marker" +
            "\t where recattrib_source_zdb_id = :zdbID" +
            "\t  and  recattrib_data_zdb_id   = mrkr_zdb_id" +
            "\t  and  mrkr_type in " +
            "\t\t  (select mtgrpmem_mrkr_type from marker_type_group_member" +
            "                    where mtgrpmem_mrkr_type_group = 'SEARCH_SEG');";
        return getCount(sql, publication.getZdbID());
    }

    public long getExpressionCount(Publication publication) {
        String sql = "\tselect count(distinct xpatfig_fig_zdb_id)" +
            " \t  from figure, expression_pattern_figure" +
            " \t where fig_source_zdb_id = :zdbID" +
            "         and fig_zdb_id=xpatfig_fig_zdb_id";
        return getCount(sql, publication.getZdbID());
    }

    public long getPhenotypeCount(Publication publication) {
        String sql = "\tselect count(distinct pg_fig_zdb_id)" +
            " \t  from figure, phenotype_source_generated" +
            " \t where fig_source_zdb_id = :zdbID" +
            "         and pg_fig_zdb_id = fig_zdb_id";
        return getCount(sql, publication.getZdbID());
    }

    public long getFeatureCount(Publication publication) {
        String sql = "\tselect count(distinct recattrib_data_zdb_id)" +
            "\t from  record_attribution" +
            "\t where recattrib_source_zdb_id = :zdbID" +
            "\t  and  recattrib_data_zdb_id like 'ZDB-ALT-%';";
        return getCount(sql, publication.getZdbID());
    }

    public long getPhenotypeAlleleCount(Publication publication) {
        String sql = "\tselect count(distinct geno_zdb_id)" +
            "\tfrom   record_attribution, genotype" +
            "\twhere  recattrib_source_zdb_id = :zdbID" +
            "\t  and  recattrib_data_zdb_id = geno_zdb_id" +
            "\t  and  geno_is_wildtype = 'f';";
        return getCount(sql, publication.getZdbID());
    }

    public long getFishCount(Publication publication) {
        String sql = "\tselect count(distinct fish_zdb_id)" +
            "\tfrom   record_attribution, fish" +
            "\twhere  recattrib_source_zdb_id = :zdbID" +
            "\t  and  recattrib_data_zdb_id = fish_zdb_id;";
        return getCount(sql, publication.getZdbID());
    }

    public long getOrthologyCount(Publication publication) {
        String sql = "select count(*) from ortholog " +
            "where exists ( " +
            "select 'x' from ortholog_evidence where oev_pub_zdb_id = :zdbID " +
            "and oev_ortho_zdb_id = ortho_zdb_ID)";
        return getCount(sql, publication.getZdbID());
    }

    public long getMappingDetailsCount(Publication publication) {
        String sql = "SELECT Count(tem.member_id)" +
            "FROM   (select distinct lms_member_1_zdb_id as member_id " +
            "from linkage, linkage_membership_search " +
            "WHERE  lnkg_source_zdb_id = :zdbID " +
            "AND lms_lnkg_zdb_id = lnkg_zdb_id " +
            "UNION " +
            "SELECT  distinct lsingle_member_zdb_id " +
            "FROM   linkage, linkage_single " +
            "WHERE  lnkg_source_zdb_id =  :zdbID " +
            "AND lsingle_lnkg_zdb_id = lnkg_zdb_id " +
            "AND NOT EXISTS (SELECT 'x' " +
            "FROM   linkage_membership_search " +
            "WHERE  lms_lnkg_zdb_id = lnkg_zdb_id) " +
            ") as tem";

        return getCount(sql, publication.getZdbID());
    }

    public Boolean canDeletePublication(Publication publication) {

        String sql = "select count(recattrib_source_zdb_id) " +
            "               from record_attribution, figure " +
            "              where recattrib_source_zdb_id = :zdbID" +
            "                and recattrib_data_zdb_id = fig_zdb_id" +
            "                and (exists (select 'x' " +
            "                               from phenotype_experiment" +
            "                              where phenox_fig_zdb_id = fig_zdb_id)" +
            "                  or exists (select 'x' " +
            "               from construct_figure" +
            "                              where consfig_fig_zdb_id = fig_zdb_id)" +
            "                  or exists (select 'x' " +
            "               from expression_pattern_figure" +
            "                              where xpatfig_fig_zdb_id = fig_zdb_id)" +
            "                  or exists (select 'x' " +
            "               from genotype_figure_fast_search" +
            "                              where gffs_fig_zdb_id = fig_zdb_id)" +
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

    @Override
    public List<Fish> getWildtypeFish() {
        String hql = "from Fish as fish where " +
            "   fish.wildtype = 't' " +
            "   order by fish.name";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        return query.list();
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

    public int deleteExpressionExperimentIDswithNoExpressionResult(Publication publication) {
        String sql = "delete from expression_experiment x " +
            " where x.xpatex_source_zdb_id = :pubID " +
            "   and not exists ( " +
            "                    select 'x' " +
            "                      from expression_result ee " +
            "                     where ee.xpatres_xpatex_zdb_id = x.xpatex_zdb_id " +
            "                   ); ";
        Query query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("pubID", publication.getZdbID());
        return query.executeUpdate();
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

    private long getMarkerCountByMarkerType(String zdbID, String type) {
        String sql = "select count(recattrib_data_zdb_id) " +
            "from  record_attribution, marker " +
            "where recattrib_source_zdb_id = :zdbID" +
            "  and recattrib_data_zdb_id = mrkr_zdb_id" +
            "  and mrkr_type = :mrkrType" +
            "      ; ";

        SQLQuery query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("zdbID", zdbID);
        query.setString("mrkrType", type);
        return ((Number) query.uniqueResult()).longValue();
    }


    private long getCount(String sql, String zdbID) {
        SQLQuery query = HibernateUtil.currentSession().createSQLQuery(sql);
        query.setString("zdbID", zdbID);
        return ((Number) query.uniqueResult()).longValue();
    }

    public PublicationTrackingHistory currentTrackingStatus(Publication publication) {
        return (PublicationTrackingHistory) HibernateUtil.currentSession()
            .createCriteria(PublicationTrackingHistory.class)
            .add(Restrictions.eq("publication", publication))
            .add(Restrictions.eq("isCurrent", true))
            .uniqueResult();
    }

    public List<PublicationTrackingHistory> fullTrackingHistory(Publication publication) {
        return HibernateUtil.currentSession()
            .createCriteria(PublicationTrackingHistory.class)
            .add(Restrictions.eq("publication", publication))
            .addOrder(Order.desc("date"))
            .list();
    }

    public List<PublicationTrackingStatus> getAllPublicationStatuses() {
        return HibernateUtil.currentSession().createCriteria(PublicationTrackingStatus.class).list();
    }

    public PublicationTrackingStatus getPublicationTrackingStatus(long id) {
        return HibernateUtil.currentSession().get(PublicationTrackingStatus.class, id);
    }

    @Override
    public Long getPublicationTrackingStatus(Person person, int days, PublicationTrackingStatus... status) {
        String hql = "select count(m) from PublicationTrackingHistory as m where " +
            "m.updater = :person AND m.status in (:status) and m.date > current_date - :days";
        return HibernateUtil.currentSession().createQuery(hql, Long.class)
            .setParameter("person", person)
            .setParameterList("status", status)
            .setParameter("days", days)
            .uniqueResult();
    }

    public PublicationTrackingStatus getPublicationStatusByName(PublicationTrackingStatus.Name name) {
        return (PublicationTrackingStatus) HibernateUtil.currentSession()
            .createCriteria(PublicationTrackingStatus.class)
            .add(Restrictions.eq("name", name))
            .uniqueResult();
    }

    public List<PublicationTrackingLocation> getAllPublicationLocations() {
        return HibernateUtil.currentSession()
            .createCriteria(PublicationTrackingLocation.class)
            .addOrder(Order.asc("displayOrder"))
            .list();
    }

    public List<Publication> getAllPublications() {
        return HibernateUtil.currentSession()
            .createCriteria(Publication.class)
            .list();
    }

    ;

    public PublicationTrackingLocation getPublicationTrackingLocation(long id) {
        return (PublicationTrackingLocation) HibernateUtil.currentSession().get(PublicationTrackingLocation.class, id);
    }

    public PublicationTrackingLocation getPublicationTrackingLocationByName(PublicationTrackingLocation.Name name) {
        return (PublicationTrackingLocation) HibernateUtil.currentSession()
            .createCriteria(PublicationTrackingLocation.class)
            .add(Restrictions.eq("name", name))
            .uniqueResult();
    }

    @Override
    public List<Publication> getAllOpenPublications() {
        List<PublicationTrackingStatus> statuses = getAllPublicationStatuses()
            .stream()
            .filter(status -> status.getType() != PublicationTrackingStatus.Type.CLOSED)
            .collect(Collectors.toList());

        List<PublicationTrackingHistory> trackingHistoryList = currentSession()
            .createCriteria(PublicationTrackingHistory.class)
            .add(Restrictions.eq("isCurrent", true))
            .add(Restrictions.in("status", statuses))
            .addOrder(Order.desc("publication"))
            .list();

        List<Publication> publications = trackingHistoryList
            .stream()
            .map(elem -> elem.getPublication())
            .collect(Collectors.toList());

        return publications;
    }

    @Override
    public List<Publication> getAllOpenPublicationsOfJournalType(PublicationType type) {
        List<Publication> openPublications = getAllOpenPublications();
        List<Publication> filteredPublications = openPublications
            .stream()
            .filter(elem -> type.equals(elem.getType()))
            .collect(Collectors.toList());
        return filteredPublications;
    }

    public DashboardPublicationList getPublicationsByStatus(Long status, Long location, String owner, int count,
                                                            int offset, String sort) {

        Criteria listCriteria = createPubsByStatusCriteria(status, location, owner);
        listCriteria.addOrder(Order.asc("status"));
        if (StringUtils.isNotEmpty(sort)) {
            boolean isAscending = true;
            if (sort.startsWith("-")) {
                isAscending = false;
                sort = sort.substring(1);
            }
            Order order;
            if (isAscending) {
                order = Order.asc(sort);
            } else {
                order = Order.desc(sort);
            }
            listCriteria.addOrder(order);
        }
        PaginationResult<PublicationTrackingHistory> histories = PaginationResultFactory
            .createResultFromScrollableResultAndClose(offset, offset + count, listCriteria.scroll());

        Criteria countsCriteria = createPubsByStatusCriteria(status, location, owner);
        List countList = countsCriteria.setProjection(Projections.projectionList()
                .add(Projections.groupProperty("status"))
                .add(Projections.rowCount()))
            .list();
        Map<String, Long> counts = new HashMap<>();
        for (Object item : countList) {
            Object[] tuple = (Object[]) item;
            PublicationTrackingStatus pubStatus = (PublicationTrackingStatus) tuple[0];
            counts.put(pubStatus.getName().toString(), (long) tuple[1]);
        }

        DashboardPublicationList result = new DashboardPublicationList();
        result.setTotalCount(histories.getTotalCount());
        result.setPublications(histories.getPopulatedResults().stream()
            .map(converter::toDashboardPublicationBean)
            .collect(Collectors.toList()));
        result.setStatusCounts(counts);

        return result;
    }

    private Criteria createPubsByStatusCriteria(Long status, Long location, String owner) {
        Criteria criteria = HibernateUtil.currentSession().createCriteria(PublicationTrackingHistory.class)
            .add(Restrictions.eq("isCurrent", true))
            .createAlias("publication", "pub");

        if (status != null) {
            criteria.add(Restrictions.eq("status", getPublicationTrackingStatus(status)));
        }

        if (location != null) {
            if (location == 0) {
                criteria.add(Restrictions.isNull("location"));
            } else {
                criteria.add(Restrictions.eq("location", getPublicationTrackingLocation(location)));
            }
        }

        if (StringUtils.isNotEmpty(owner)) {
            if (owner.equals("*")) {
                criteria.add(Restrictions.isNotNull("owner"));
            } else {
                criteria.add(Restrictions.eq("owner", profileRepository.getPerson(owner)));
            }
        }
        return criteria;
    }

    public List<PublicationFileType> getAllPublicationFileTypes() {
        return HibernateUtil.currentSession().createCriteria(PublicationFileType.class).list();
    }

    public PublicationFileType getPublicationFileType(long id) {
        return (PublicationFileType) HibernateUtil.currentSession().get(PublicationFileType.class, id);
    }

    public PublicationFile getPublicationFile(long id) {
        return (PublicationFile) HibernateUtil.currentSession().get(PublicationFile.class, id);
    }

    public PublicationFileType getPublicationFileTypeByName(PublicationFileType.Name name) {
        return (PublicationFileType) HibernateUtil.currentSession()
            .createCriteria(PublicationFileType.class)
            .add(Restrictions.eq("name", name))
            .uniqueResult();
    }

    public PublicationFile getOriginalArticle(Publication publication) {
        PublicationFileType originalArticle = getPublicationFileTypeByName(PublicationFileType.Name.ORIGINAL_ARTICLE);
        return (PublicationFile) HibernateUtil.currentSession()
            .createCriteria(PublicationFile.class)
            .add(Restrictions.eq("publication", publication))
            .add(Restrictions.eq("type", originalArticle))
            .uniqueResult();
    }

    public PublicationFile addPublicationFile(Publication publication, PublicationFileType type, MultipartFile file) throws IOException {
        Transaction tx = HibernateUtil.createTransaction();
        if (type.getName() == PublicationFileType.Name.ORIGINAL_ARTICLE) {
            // if there already is an original article for this pub, we need to delete that
            // record because there can be only one of those.
            PublicationFile existingArticle = getOriginalArticle(publication);
            if (existingArticle != null) {
                HibernateUtil.currentSession().delete(existingArticle);
                HibernateUtil.currentSession().flush();
            }
        }
        PublicationFile pubFile = publicationService.processPublicationFile(publication, file.getOriginalFilename(), type, file.getInputStream());
        HibernateUtil.currentSession().save(pubFile);
        tx.commit();
        return pubFile;
    }

    public CorrespondenceSentMessage addSentCorrespondence(Publication publication, CorrespondenceDTO dto) {
        CorrespondenceSentMessage correspondence = new CorrespondenceSentMessage();
        correspondence.setPublication(publication);
        correspondence.setFrom(profileRepository.getPerson(dto.getFrom().getZdbID()));
        correspondence.setResend(false);
        correspondence.setSentDate(new Date());

        CorrespondenceComposedMessage message = new CorrespondenceComposedMessage();
        message.setFrom(profileRepository.getPerson(dto.getFrom().getZdbID()));
        message.setPublication(publication);
        message.setComposedDate(new Date());
        message.setSubject(dto.getSubject());
        message.setText(dto.getMessage());
        message.setRecipientEmailList(dto.getTo().stream()
            .map(PersonDTO::getEmail)
            .collect(Collectors.joining(", ")));

        Set<CorrespondenceRecipient> recipients = new HashSet<>();
        for (PersonDTO to : dto.getTo()) {
            CorrespondenceRecipient recipient = new CorrespondenceRecipient();
            recipient.setFirstName(to.getFirstName());
            recipient.setLastName(to.getLastName());
            recipient.setEmail(to.getEmail());
            recipient.setPerson(profileRepository.getPerson(to.getZdbID()));
            recipient.setMessage(message);
            recipients.add(recipient);
        }

        message.setRecipients(recipients);
        correspondence.setMessage(message);

        Session session = HibernateUtil.currentSession();
        session.save(message);
        session.save(correspondence);

        return correspondence;
    }

    public CorrespondenceSentMessage addResentCorrespondence(Publication publication, CorrespondenceDTO dto) {
        Session session = HibernateUtil.currentSession();
        CorrespondenceSentMessage originalCorrespondence = (CorrespondenceSentMessage) session
            .get(CorrespondenceSentMessage.class, dto.getId());
        if (originalCorrespondence == null) {
            return null;
        }
        CorrespondenceSentMessage resentCorrespondence = new CorrespondenceSentMessage();
        resentCorrespondence.setFrom(profileRepository.getPerson(dto.getFrom().getZdbID()));
        resentCorrespondence.setPublication(publication);
        resentCorrespondence.setSentDate(new Date());
        resentCorrespondence.setResend(true);
        resentCorrespondence.setMessage(originalCorrespondence.getMessage());
        session.save(resentCorrespondence);
        return resentCorrespondence;
    }

    public CorrespondenceReceivedMessage addReceivedCorrespondence(Publication publication, CorrespondenceDTO dto) {
        CorrespondenceReceivedMessage correspondence = new CorrespondenceReceivedMessage();
        correspondence.setPublication(publication);
        correspondence.setFromEmail(dto.getFrom().getEmail());
        correspondence.setFrom(profileRepository.getPerson(dto.getFrom().getZdbID()));
        correspondence.setDate(new Date());
        correspondence.setTo(profileRepository.getPerson(dto.getTo().get(0).getZdbID()));
        correspondence.setSubject(dto.getSubject());
        correspondence.setText(dto.getMessage());
        HibernateUtil.currentSession().save(correspondence);
        return correspondence;
    }

    @Override
    public List<String> getPublicationIdsForMarkerGo(String markerZdbID, String markerGoEvdTermZdbID, String evidenceCode, String inference) {
        Session session = HibernateUtil.currentSession();
        String sql;
        List<String> pubIDs = null;
        if (StringUtils.isEmpty(inference) || inference.isEmpty() || inference.equals("null")) {
            sql = " select ra.recattrib_source_zdb_id  " +
                " from record_attribution ra, marker_go_term_evidence ev " +
                " where  ev.mrkrgoev_zdb_id  = ra.recattrib_data_zdb_id " +
                " and  :markerZdbID = ev.mrkrgoev_mrkr_zdb_id " +
                " and :markerGoEvdTermZdbID = ev.mrkrgoev_term_zdb_id" +
                " and :evidenceCode = ev.mrkrgoev_evidence_code";

            SQLQuery query = session.createSQLQuery(sql);
            query.setString("markerZdbID", markerZdbID);
            query.setString("markerGoEvdTermZdbID", markerGoEvdTermZdbID);
            query.setString("evidenceCode", evidenceCode);
            pubIDs = query.list();
        } else {
            sql = " select ra.recattrib_source_zdb_id  " +
                " from record_attribution ra ,  marker_go_term_evidence ev, inference_group_member inf " +
                " where  ev.mrkrgoev_zdb_id  = ra.recattrib_data_zdb_id " +
                " and inf.infgrmem_mrkrgoev_zdb_id = ev.mrkrgoev_zdb_id " +
                " and  :markerZdbID = ev.mrkrgoev_mrkr_zdb_id " +
                " and :markerGoEvdTermZdbID = ev.mrkrgoev_term_zdb_id" +
                " and :evidenceCode = ev.mrkrgoev_evidence_code" +
                " and :inference = inf.infgrmem_inferred_from";
            SQLQuery query = session.createSQLQuery(sql);
            query.setString("markerZdbID", markerZdbID);
            query.setString("markerGoEvdTermZdbID", markerGoEvdTermZdbID);
            query.setString("evidenceCode", evidenceCode);
            query.setString("inference", inference);
            pubIDs = query.list();
        }
        return pubIDs;
    }

    @Override
    public List<String> getPublicationIdsForFeatureType(String featureZdbID) {
        Session session = HibernateUtil.currentSession();
        String sql = " select ra.recattrib_source_zdb_id  " +
            " from record_attribution ra" +
            " where :featureType = ra.recattrib_source_type " +
            " and :featureID = ra.recattrib_data_zdb_id";
        SQLQuery query = session.createSQLQuery(sql);
        query.setString("featureType", "feature type");
        query.setString("featureID", featureZdbID);
        List<String> pubIDs = query.list();
        return pubIDs;
    }

    public GregorianCalendar getNewestPubEntryDate() {
        return (GregorianCalendar) HibernateUtil
            .currentSession()
            .createQuery("select max(pub.entryDate) from Publication pub")
            .uniqueResult();
    }

    public GregorianCalendar getOldestPubEntryDate() {
        return (GregorianCalendar) HibernateUtil
            .currentSession()
            .createQuery("select min(pub.entryDate) from Publication pub")
            .uniqueResult();
    }

    @Override
    public List<String> getDirectlyAttributedZdbids(String publicationId, Pagination pagination) {
        Session session = HibernateUtil.currentSession();
        String sql = """        
                 select DISTINCT ra.recattrib_data_zdb_id  
                 from record_attribution ra 
                 where :publicationZdbID = ra.recattrib_source_zdb_id
                 """;

        if (pagination.getFieldFilter(FieldFilter.ENTITY_ID) != null) {
            sql += " AND lower(ra.recattrib_data_zdb_id) like '%" + pagination.getFieldFilter(FieldFilter.ENTITY_ID).toLowerCase() + "%'";
        }
        sql += " order by ra.recattrib_data_zdb_id ";
        SQLQuery query = session.createSQLQuery(sql);
        query.setString("publicationZdbID", publicationId);
        List<String> dataIds = query.list();
        return dataIds;
    }

    @Override
    public Long getDirectlyAttributed(Publication publication) {
        String sql = "select count(*) " +
            " from record_attribution " +
            " where recattrib_source_zdb_id = :zdbID ";
        return getCount(sql, publication.getZdbID());
    }

    @Override
    public List<MetricsByDateBean> getMetricsByDate(Calendar start,
                                                    Calendar end,
                                                    PublicationMetricsFormBean.QueryType query,
                                                    PublicationMetricsFormBean.Interval groupInterval,
                                                    PublicationMetricsFormBean.GroupType groupType) {
        String groupExpression = "";
        String dateExpression = "";
        boolean currentStatusOnly = false;
        switch (groupType) {
            case ACTIVE:
                groupExpression = "pub.status";
                currentStatusOnly = true;
                break;
            case INDEXED:
                groupExpression = "case when pub.pub_is_indexed = 't' then 'Indexed' else 'Unindexed' end";
                dateExpression = "pub.pub_indexed_date";
                currentStatusOnly = true;
                break;
            case STATUS:
                groupExpression = "status.pts_status_display";
                dateExpression = "history.pth_status_insert_date";
                break;
            case LOCATION:
                groupExpression = "location.ptl_location_display";
                dateExpression = "history.pth_status_insert_date";
                break;
        }
        if (query == PublicationMetricsFormBean.QueryType.PET_DATE) {
            dateExpression = "pub.pub_arrival_date";
            currentStatusOnly = true;
        }
        String sql = String.format(
            "select u.category as category, u.date as date, count(*) as count " +
                "from ( " +
                "  select distinct pub.zdb_id, %1$s as category, date_trunc('%2$s', %3$s) as date " +
                "  from publication pub " +
                "  left outer join pub_tracking_history history on pub.zdb_id = history.pth_pub_zdb_id " +
                "  left outer join pub_tracking_status status on history.pth_status_id = status.pts_pk_id " +
                "  left outer join pub_tracking_location location on history.pth_location_id = location.ptl_pk_id " +
                "  where %3$s >= :start " +
                "  and %3$s < :end " +
                "  and pub.jtype = :type " +
                (currentStatusOnly ? "and history.pth_status_is_current = 't' " : "") +
                ") as u " +
                "group by u.category, u.date", groupExpression, groupInterval.toString(), dateExpression);
        return HibernateUtil.currentSession().createSQLQuery(sql)
            .setParameter("start", start)
            .setParameter("end", end)
            .setParameter("type", PublicationType.JOURNAL.getDisplay())
            .setResultTransformer(Transformers.aliasToBean(MetricsByDateBean.class))
            .list();
    }

    @Override
    public List<MetricsOnDateBean> getCumulativeMetrics(Calendar end, PublicationMetricsFormBean.GroupType groupType) {
        String groupExpression = "";
        switch (groupType) {
            case STATUS:
                groupExpression = "status.pts_status_display";
                break;
            case LOCATION:
                groupExpression = "location.ptl_location_display";
                break;
        }
        String sql = String.format(
            "select " +
                "  %1$s as category, " +
                "  avg(history.pth_days_in_status) as average, " +
                "  stddev(history.pth_days_in_status) as \"standardDeviation\", " +
                "  min(history.pth_days_in_status) as minimum, " +
                "  max(history.pth_days_in_status) as maximum " +
                "from pub_tracking_history history " +
                "inner join publication pub on pub.zdb_id = history.pth_pub_zdb_id " +
                "left outer join pub_tracking_status status on history.pth_status_id = status.pts_pk_id " +
                "left outer join pub_tracking_location location on history.pth_location_id = location.ptl_pk_id " +
                "where history.pth_status_is_current = 'f' " +
                "and history.pth_days_in_status is not null " +
                "and history.pth_status_insert_date < :end " +
                "and pub.jtype = :type " +
                "group by %1$s", groupExpression);
        return HibernateUtil.currentSession().createSQLQuery(sql)
            .setParameter("end", end)
            .setParameter("type", PublicationType.JOURNAL.getDisplay())
            .setResultTransformer(Transformers.aliasToBean(MetricsOnDateBean.class))
            .list();
    }

    @Override
    public List<MetricsOnDateBean> getSnapshotMetrics(PublicationMetricsFormBean.GroupType groupType) {
        String groupExpression = "";
        switch (groupType) {
            case STATUS:
                groupExpression = "status.pts_status_display";
                break;
            case LOCATION:
                groupExpression = "location.ptl_location_display";
                break;
        }
        String sql = String.format(
            "select " +
                "  grouper as category, " +
                "  avg(age) as average, " +
                "  stddev(age) as \"standardDeviation\", " +
                "  min(age) as minimum, " +
                "  max(age) as maximum, " +
                "  avg_of_largest(cast(age as numeric)) as \"oldestAverage\" " +
                "from ( " +
                "  select " +
                "    %1$s as grouper, " +
                "    extract(day from (current_date - history.pth_status_insert_date)) as age " +
                "  from " +
                "    pub_tracking_history history " +
                "        inner join publication pub on pub.zdb_id = history.pth_pub_zdb_id " +
                "        left outer join pub_tracking_status status on history.pth_status_id = status.pts_pk_id " +
                "        left outer join pub_tracking_location location on history.pth_location_id = location.ptl_pk_id " +
                "        where history.pth_status_is_current = 't' " +
                "        and pub.jtype = :type " +
                ") as subq " +
                "group by grouper;", groupExpression);
        return HibernateUtil.currentSession().createSQLQuery(sql)
            .setParameter("type", PublicationType.JOURNAL.getDisplay())
            .setResultTransformer(Transformers.aliasToBean(MetricsOnDateBean.class))
            .list();
    }

    @Override
    public ProcessingChecklistTask getProcessingChecklistTask(ProcessingChecklistTask.Task task) {
        return (ProcessingChecklistTask) HibernateUtil.currentSession()
            .createCriteria(ProcessingChecklistTask.class)
            .add(Restrictions.eq("task", task))
            .uniqueResult();
    }

    @Override
    public PublicationProcessingChecklistEntry getProcessingChecklistEntry(long id) {
        return (PublicationProcessingChecklistEntry) HibernateUtil.currentSession()
            .get(PublicationProcessingChecklistEntry.class, id);
    }

    @Override
    public List<PubmedPublicationAuthor> getPubmedPublicationAuthorsByPublication(Publication publication) {
        return HibernateUtil.currentSession()
            .createCriteria(PubmedPublicationAuthor.class)
            .add(Restrictions.eq("publication", publication))
            .list();
    }

    @Override
    public Map<Marker, Boolean> areNewGenePubAttribution(List<Marker> attributedMarker, String publicationId) {
        if (CollectionUtils.isEmpty(attributedMarker))
            return null;
        String hql = "select pa.dataZdbID, count(pa) as ct from PublicationAttribution as pa where " +
            " pa.dataZdbID in (:markerIDs) AND pa.sourceType = :source " +
            " group by pa.dataZdbID ";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameterList("markerIDs", attributedMarker.stream().map(Marker::getZdbID).collect(Collectors.toList()));
        query.setParameter("source", RecordAttribution.SourceType.STANDARD);
        List<Object[]> pubAttrList = query.list();
        Map<Marker, Boolean> map = new HashMap<>();
        attributedMarker.forEach(marker -> {
            map.put(marker, pubAttrList.stream()
                .anyMatch(pubAttribution -> pubAttribution[0].equals(marker.getZdbID())
                    && ((Long) pubAttribution[1]) == 1));
        });
        return map;
    }

    @Override
    public List<SequenceTargetingReagent> getSTRsByPublication(String publicationID, Pagination pagination) {
        Session session = HibernateUtil.currentSession();
        String hql = "select distinct marker from SequenceTargetingReagent marker, RecordAttribution attr" +
            "     where attr.dataZdbID = marker.zdbID" +
            "           and attr.sourceZdbID = :pubID " +
            "           and marker.markerType.name in (:markerTypes)";
        if (pagination.getFieldFilter(FieldFilter.STR_NAME) != null) {
            hql += " AND marker.abbreviation like '%" + pagination.getFieldFilter(FieldFilter.STR_NAME) + "%'";
        }
        Query query = session.createQuery(hql);
        query.setString("pubID", publicationID);
        query.setParameterList("markerTypes", List.of(Marker.Type.MRPHLNO.name(), Marker.Type.CRISPR.name(), Marker.Type.TALEN.name()));
        return query.list();
    }

    @Override
    public List<Image> getImages(Publication publication) {
        String hql = "from Image where " +
            " figure.publication = :publication " +
            " order by figure.orderingLabel ";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("publication", publication);
        return query.list();
    }

    @Override
    public boolean isNewFeaturePubAttribution(Feature marker, String publicationId) {
        String hql = "select pa from PublicationAttribution as pa where " +
            " pa.dataZdbID = :featureID AND pa.sourceType = :source ";

        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("featureID", marker.getZdbID());
        query.setParameter("source", RecordAttribution.SourceType.STANDARD);
        List<PublicationAttribution> pubAttrList = query.list();
        Set<String> pubList = pubAttrList.stream().map(attribution -> attribution.getPublication().getZdbID()).collect(Collectors.toSet());
        return CollectionUtils.isNotEmpty(pubList) && pubList.size() == 1 && pubList.contains(publicationId);
    }

    @Override
    public boolean hasCuratedOrthology(Marker marker) {

        return CollectionUtils.isNotEmpty(getOrthologListByMrkr(marker.zdbID));
    }

    private String addOrderByParameters(String hql) {
        if (!isUsePagination()) {
            return hql;
        }
        StringBuilder sb = new StringBuilder(hql);
        sb.append(getOrderByClause());
        return sb.toString();
    }

    private void addPaginationParameters(Query query) {
        if (isUsePagination()) {
            query.setFirstResult(getFirstRow() - 1);
            query.setMaxResults(getMaxDisplayRows());
        }
    }

    private List<MarkerStatistic> createMarkerStatistics(List<Object[]> list, GenericTerm anatomyTerm) {
        if (list == null) {
            return null;
        }

        List<MarkerStatistic> markers = new ArrayList<MarkerStatistic>();
        for (Object[] stats : list) {
            String markerZdbID = (String) stats[0];
            Marker marker = markerRepository.getMarkerByID(markerZdbID);
            MarkerStatistic statistic = new MarkerStatistic(anatomyTerm, marker);
            statistic.setNumberOfFigures((Integer) stats[2]);
            //statistic.setNumberOfPublications(getNumberOfExpressedGenePublicationsWithFigures(marker.getZdbID(), anatomyTerm.getZdbID()));
            markers.add(statistic);
        }
        return markers;
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

    private DOIAttempt getDoiAttempt(Publication publication) {
        Session session = HibernateUtil.currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<DOIAttempt> cr = cb.createQuery(DOIAttempt.class);

        Root<DOIAttempt> root = cr.from(DOIAttempt.class);
        cr.select(root).where(cb.equal(root.get("publication"), publication));

        return session.createQuery(cr).uniqueResult();
    }

    private Journal getJournalByProperty(String propertyName, String propertyValue) {
        Session session = HibernateUtil.currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Journal> cr = cb.createQuery(Journal.class);
        Root<Journal> root = cr.from(Journal.class);
        cr.select(root).where(cb.equal(root.get(propertyName), propertyValue));
        return session.createQuery(cr).uniqueResult();
    }
}
