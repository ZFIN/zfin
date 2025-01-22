package org.zfin.publication.repository;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import org.zfin.antibody.Antibody;
import org.zfin.curation.presentation.CorrespondenceDTO;
import org.zfin.curation.presentation.PersonDTO;
import org.zfin.curation.service.CurationDTOConversionService;
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
import org.zfin.marker.presentation.STRTargetRow;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.*;
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

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.zfin.database.HibernateUpgradeHelper.setTupleResultTransformer;
import static org.zfin.framework.HibernateUtil.currentSession;

/**
 * ToDO: include documentation
 */
@Repository
public class HibernatePublicationRepository extends PaginationUtil implements PublicationRepository {

    @Autowired
    private CurationDTOConversionService converter = new CurationDTOConversionService();

    @Autowired
    private PublicationService publicationService;

    Logger logger = LogManager.getLogger(HibernatePublicationRepository.class);

    private MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    private ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();

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

    //TODO: ScrollableResults makes it difficult to refactor to Tuple-based hql


    /**
     * Note: firstRow must be 1 or greater, i.e. the way a user would describes
     * the record number. Hibernate starts with the first row numbered '0'.
     * Do not include records where the gene or probe is WITHDRAWN.
     * Do not include records where the probe is Chimeric.
     * Written in native SQL because need to order by number of figures.
     *
     * @param anatomyTerm anatomy term
     * @param pagination  Pagination bean
     */
    public PaginationResult<MarkerStatistic> getAllExpressedMarkers(GenericTerm anatomyTerm, Pagination pagination) {
        int firstRow = pagination.getStart();
        int numberOfRecords = pagination.getLimit();
        if (firstRow < 0) {
            throw new RuntimeException("First Row number <" + firstRow + "> is invalid");
        }
        // Hibernate starts at 0 while the argument expects to start at 1

        Session session = HibernateUtil.currentSession();

        // todo: Rewrite as HQL and include the whole marker object as it is needed.
        // todo: note that when in SQL, start at 1 (current) , but when in HQL, start at 0
        String sql = """
            SELECT exp.xpatex_gene_zdb_id as geneID, gene.mrkr_abbrev as geneSymbol,
            count(distinct fig.fig_zdb_id) as numOfFig,
            count(distinct img.img_zdb_id) as numOfImg
            FROM  Expression_Experiment2 exp
                  join FISH_EXPERIMENT as genox on genox.genox_zdb_id=exp.xpatex_genox_zdb_id
                  join EXPRESSION_FIGURE_STAGE as efs on efs.efs_xpatex_zdb_id = exp.xpatex_zdb_id
                  join EXPRESSION_RESULT2 as result on result.xpatres_efs_id = efs.efs_pk_id
                  join Figure as fig on fig.fig_zdb_id=efs.efs_fig_zdb_id
                  left outer join Image as img on img.img_fig_zdb_id=fig.fig_zdb_id
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
                   """;
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                if (entry.getKey().startsWith("gene")) {
                    sql += " AND lower(gene.mrkr_abbrev) like :" + entry.getKey() + " ";
                }
            }
        }
        sql += """
            GROUP BY exp.xpatex_gene_zdb_id, gene.mrkr_abbrev
            ORDER BY numOfFig DESC, geneSymbol
                """;

        NativeQuery query = session.createNativeQuery(sql, Tuple.class);
        query.addScalar("geneID", StandardBasicTypes.STRING);
        query.addScalar("geneSymbol", StandardBasicTypes.STRING);
        query.addScalar("numOfFig", StandardBasicTypes.INTEGER);
        query.addScalar("numOfImg", StandardBasicTypes.INTEGER);
        query.setParameter("termID", anatomyTerm.getZdbID());
        query.setParameter("withdrawn", Marker.WITHDRAWN);
        query.setParameter("chimeric", Clone.ProblemType.CHIMERIC.toString()); // todo: use enum here // NativeQuery so chimeric is a String
        if (MapUtils.isNotEmpty(pagination.getFilterMap())) {
            for (Map.Entry<String, String> entry : pagination.getFilterMap().entrySet()) {
                query.setParameter(entry.getKey(), "%" + entry.getValue().toLowerCase() + "%");
            }
        }

        ScrollableResults results = query.scroll();

        List<Tuple> list = new ArrayList<>();
        results.beforeFirst();
        if (firstRow > 0) {
            results.setRowNumber(firstRow - 1);
        }
        while (results.next() && results.getRowNumber() < firstRow + numberOfRecords) {
            if (results.getRowNumber() >= firstRow) {
                list.add((Tuple)results.get());
            }
        }

        results.last();
        int totalResults = results.getRowNumber() + 1;

        results.close();
        List<MarkerStatistic> markerStatistics = createMarkerStatistics(list, anatomyTerm);

        return new PaginationResult<>(totalResults, markerStatistics);
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
                 join expression_figure_stage on efs_fig_zdb_id = fig_zdb_id
                 join expression_result2 on xpatres_efs_id = efs_pk_id
                 join expression_experiment2 on xpatex_zdb_id = efs_xpatex_zdb_id
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
        query.setParameter("chimeric", Clone.ProblemType.CHIMERIC.toString()); //NativeQuery so chimeric is a String

        return query.getSingleResult().intValue();
    }

    public Publication getPublication(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return session.get(Publication.class, zdbID);
    }

    //TODO: merge this with the private version alternate below and always order by date desc?
    //TODO: Seems fairly safe to have a default ordering
    public List<Publication> getPublications(List<String> zdbIDs) {
        return getPublications(zdbIDs, false);
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

    /**
     * Retrieve list of figures for a given genotype and anatomy term
     * for mutant genotypes excluding sequenceTargetingReagent.
     *
     * @param fish genotype
     * @param term anatomy term
     * @return list of figures.
     */
    public PaginationResult<Figure> getFiguresByFishAndAnatomy(Fish fish, GenericTerm term, boolean includeSubstructures) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select distinct figure from Figure figure, PhenotypeStatementWarehouse phenos,
            FishExperiment fishox, TransitiveClosure transitiveClosure
            where fishox.fish = :fish AND
                  phenos.phenotypeWarehouse.fishExperiment = fishox  AND
                  phenos.phenotypeWarehouse.figure = figure AND
                  transitiveClosure.root = :aoTerm AND
                  ( phenos.e1a = transitiveClosure.child OR phenos.e1b = transitiveClosure.child OR
                    phenos.e2a = transitiveClosure.child OR phenos.e2b = transitiveClosure.child)
             AND exists (select 'x' from GeneGenotypeExperiment where fishExperiment = fishox)
            order by figure.orderingLabel
            """;
        Query<Figure> query = session.createQuery(hql, Figure.class);
        query.setParameter("fish", fish);
        query.setParameter("aoTerm", term);
        PaginationResult<Figure> paginationResult = new PaginationResult<>(query.list());
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

    /**
     * Retrieve figures for a given gene and anatomy term.
     *
     * @param marker      marker
     * @param anatomyTerm anatomy
     * @return a list of figures
     */
    public List<Figure> getFiguresByGeneAndAnatomy(Marker marker, GenericTerm anatomyTerm) {
        Session session = HibernateUtil.currentSession();
        String hql = """
            select distinct fig from Figure fig, ExpressionResult res, Marker marker, ExpressionExperiment exp,
            FishExperiment fishox, ExpressionResultFigure xpatfig
            where
               marker = :marker AND
               exp.gene = marker AND
               res.expressionExperiment = exp AND
               (res.entity.superterm = :aoTerm OR res.entity.subterm = :aoTerm) AND
               xpatfig.expressionResult = res AND
               xpatfig.figure = fig AND
               res.expressionFound = :expressionFound AND
               exp.fishExperiment = fishox AND
               fishox.standardOrGenericControl = :condition AND
               fishox.fish.wildtype = :isWildtype
               """;
        Query<Figure> query = session.createQuery(hql, Figure.class);
        query.setParameter("expressionFound", true);
        query.setParameter("isWildtype", true);
        query.setParameter("aoTerm", anatomyTerm);
        query.setParameter("marker", marker);
        query.setParameter("condition", true);
        return query.list();
    }

    @Override
    public List<Publication> getPubsForDisplay(String zdbID) {

        List<String> publicationIDs = HibernateUtil.currentSession()
            .createNativeQuery(getCommonPublicationSQL(zdbID))
            .setParameter("markerZdbID", zdbID)
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

        return query.getSingleResult().intValue();
    }

    public Image getImageById(String zdbID) {
        Session session = HibernateUtil.currentSession();
        return session.get(Image.class, zdbID);
    }

    @Override
    public PaginationResult<Publication> getPublicationsWithFigures(Marker marker, GenericTerm anatomyTerm) {
        Session session = HibernateUtil.currentSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();

        CriteriaQuery<Publication> query = cb.createQuery(Publication.class);

        // Define the root of the query
        Root<Publication> pubs = query.from(Publication.class);

        // Join the expressionExperiments property
        Join<Publication, ExpressionExperiment2> expressionExperiments = pubs.join("expressionExperiments", JoinType.INNER);

        // Define the predicates for the query
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(expressionExperiments.get("gene"), marker));

        Join<ExpressionExperiment2, ExpressionFigureStage> figureStageSet = expressionExperiments.join("figureStageSet", JoinType.INNER);
        Join<ExpressionFigureStage, ExpressionResult2> expressionResults = figureStageSet.join("expressionResultSet", JoinType.INNER);

        predicates.add(cb.isNotNull(figureStageSet.get("figure")));
        predicates.add(cb.or(cb.equal(expressionResults.get("superTerm"), anatomyTerm), cb.equal(expressionResults.get("subTerm"), anatomyTerm)));
        predicates.add(cb.equal(expressionResults.get("expressionFound"), true));

        // Join the fishExperiment property
        Join<ExpressionExperiment2, FishExperiment> genox = expressionExperiments.join("fishExperiment", JoinType.INNER);
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
        return new PaginationResult<>(results);
    }

    @Override
    public List<String> getDistinctFigureLabels(String publicationID) {
        Session session = HibernateUtil.currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaQuery<String> query = criteriaBuilder.createQuery(String.class);
        Root<Figure> figure = query.from(Figure.class);

        query.select(figure.get("label"))
            .where(criteriaBuilder.equal(figure.get("publication").get("zdbID"), publicationID))
            .orderBy(criteriaBuilder.asc(figure.get("orderingLabel")));

        return session.createQuery(query).getResultList();
    }

    /**
     * Retrieve distinct list of genes (GENEDOM_AND_EFG) that are attributed to a given publication.
     *
     * @param pubID publication id
     * @return list of markers
     */
    public List<Marker> getGenesByPublication(String pubID) {
        return getGenesByPublication(pubID, true);
    }

    /**
     * Retrieve distinct list of genes (GENEDOM_AND_EFG if includeEgfs is true; just GENEDOM otherwise) that are
     * attributed to a given publication.
     *
     * @param pubID       publication id
     * @param includeEFGs boolean
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

        markers.addAll(getMarkersPulledThroughFeatures(pubID));
        markers.addAll(getMarkersPulledThroughSTRs(pubID));
        return new ArrayList<>(markers.stream().sorted(Comparator.comparing(Marker::getAbbreviation)).toList());
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

    public List<Feature> getFeaturesByPublication(String pubID) {
        Session session = HibernateUtil.currentSession();


        String hql = """
            select distinct feature from Feature feature, PublicationAttribution pub
                 where pub.dataZdbID = feature.zdbID
                       and pub.publication.zdbID = :pubID
            order by feature.abbreviationOrder
            """;
        Query<Feature> query = session.createQuery(hql, Feature.class);
        query.setParameter("pubID", pubID);
        return query.list();
    }

    public List<Fish> getFishByPublication(String pubID) {
        Session session = HibernateUtil.currentSession();


        String hql = """
            select distinct fish from Fish fish, PublicationAttribution pub
                 where pub.dataZdbID = fish.zdbID
                       and pub.publication.zdbID = :pubID
            order by fish.nameOrder
            """;
        Query<Fish> query = session.createQuery(hql, Fish.class);
        query.setParameter("pubID", pubID);
        return query.list();
    }

    /**
     * Retrieve list of Genotypes being used in a publication
     *
     * @param publicationID publication ID
     * @return list of genotype
     */
    public List<Genotype> getGenotypesInPublication(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                     select distinct fish from Genotype fish, PublicationAttribution record
                          where record.publication.zdbID = :pubID
                                and record.dataZdbID = fish.zdbID
                                and record.sourceType = :sourceType
                         order by fish.handle
                     """;
        Query<Genotype> query = session.createQuery(hql, Genotype.class);
        query.setParameter("pubID", publicationID);
        query.setParameter("sourceType", RecordAttribution.SourceType.STANDARD);

        return query.list();
    }

    public List<Experiment> getExperimentsByPublication(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            from Experiment
            where name in (:names)
            """;
        Query<Experiment> query = session.createQuery(hql, Experiment.class);
        query.setParameterList("names", List.of(Experiment.STANDARD, Experiment.GENERIC_CONTROL));
        List<Experiment> experimentList = query.list();

        hql = """
            select distinct experiment from Experiment experiment
            where experiment.publication.zdbID = :pubID
            and experiment.experimentConditions is not empty
            order by experiment.name
            """;
        query = session.createQuery(hql, Experiment.class);
        query.setParameter("pubID", publicationID);

        experimentList.addAll(query.list());
        return experimentList;

    }

    public Genotype getGenotypeByHandle(String handle) {
        Session session = HibernateUtil.currentSession();
        String hql = "from Genotype where handle = :handle";
        Query<Genotype> crit = session.createQuery(hql, Genotype.class);
        crit.setParameter("handle", handle);
        return crit.uniqueResult();
    }

    public List<Genotype> getNonWTGenotypesByPublication(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select distinct geno from Genotype geno, PublicationAttribution record
            where record.publication.zdbID = :pubID
               and record.dataZdbID = geno.zdbID
               and geno.wildtype = false
            order by geno.handle
            """;
        Query<Genotype> query = session.createQuery(hql, Genotype.class);
        query.setParameter("pubID", publicationID);

        return query.list();
    }

    public List<Antibody> getAntibodiesByPublication(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                     select distinct antibody from Antibody antibody, PublicationAttribution record
                          where record.publication.zdbID = :pubID 
                                and record.dataZdbID = antibody.zdbID
                         order by antibody.abbreviationOrder 
                     """;
        Query<Antibody> query = session.createQuery(hql, Antibody.class);
        query.setParameter("pubID", publicationID);

        return query.list();
    }

    public List<Antibody> getAntibodiesByPublicationAndGene(String publicationID, String geneID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select distinct antibody from Antibody antibody, PublicationAttribution record, MarkerRelationship rel
                 where record.publication.zdbID = :pubID
                       and record.dataZdbID = antibody.zdbID
                       and rel.firstMarker.zdbID = :geneID
                       and rel.secondMarker = antibody
                order by antibody.abbreviationOrder
                """;
        Query<Antibody> query = session.createQuery(hql, Antibody.class);
        query.setParameter("pubID", publicationID);
        query.setParameter("geneID", geneID);

        return query.list();
    }

    public List<Marker> getGenesByAntibody(String publicationID, String antibodyID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select distinct gene from Marker gene, PublicationAttribution record, MarkerRelationship rel
                 where record.publication.zdbID = :pubID
                       and record.dataZdbID = gene.zdbID
                       and rel.firstMarker = gene
                       and rel.secondMarker.zdbID = :antibodyID
                order by gene.abbreviationOrder
                """;
        Query<Marker> query = session.createQuery(hql, Marker.class);
        query.setParameter("pubID", publicationID);
        query.setParameter("antibodyID", antibodyID);

        return query.list();
    }

    /**
     * Retrieve access numbers for given pub and gene.
     *
     * @param publicationID string
     * @param geneID        string
     * @return list of db links
     */
    public List<MarkerDBLink> getDBLinksByGene(String publicationID, String geneID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select distinct link from MarkerDBLink link, PublicationAttribution record
                 where record.publication.zdbID = :pubID
                       and record.dataZdbID = :geneID
                       and link.marker.zdbID = :geneID
                       and link.referenceDatabase.foreignDB.dbName = :foreignDB
                order by link.accessionNumber
                """;
        Query<MarkerDBLink> query = session.createQuery(hql, MarkerDBLink.class);
        query.setParameter("pubID", publicationID);
        query.setParameter("geneID", geneID);
        query.setParameter("foreignDB", ForeignDB.AvailableName.GENBANK); //MarkerDBLink.referenceDatabase.foreignDB.dbName is an AvailableName

        return query.list();
    }

    /**
     * Retrieve db link object of a clone for a gene and pub.
     *
     * @param pubID  pub is
     * @param geneID gene ID
     * @return list of MarkerDBLinks
     */
    public List<MarkerDBLink> getDBLinksForCloneByGene(String pubID, String geneID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select distinct link
            from MarkerDBLink link, PublicationAttribution record,
                 Marker marker, Clone clone, MarkerRelationship mrel
            where record.publication.zdbID = :pubID
               and record.dataZdbID = :geneID
                                        and link.marker.zdbID = clone.zdbID
                                        and marker.zdbID = :geneID
                                        and mrel.firstMarker.zdbID = :geneID
                                        and mrel.secondMarker.zdbID = clone.id
                                        and mrel.type = :type
                                        and link.referenceDatabase.foreignDB.dbName = :foreignDB
            order by link.accessionNumber
                      """;
        Query<MarkerDBLink> query = session.createQuery(hql, MarkerDBLink.class);
        query.setParameter("pubID", pubID);
        query.setParameter("geneID", geneID);
        query.setParameter("foreignDB", ForeignDB.AvailableName.GENBANK);
        query.setParameter("type", MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);

        return query.list();
    }

    /**
     * Retrieve all figures that are associated to a given publication.
     *
     * @param pubID publication ID
     * @return list of figures
     */
    public List<Figure> getFiguresByPublication(String pubID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select figure from Figure figure
                 where figure.publication.zdbID = :pubID
                 order by figure.orderingLabel
                 """;
        Query<Figure> query = session.createQuery(hql, Figure.class);
        query.setParameter("pubID", pubID);
        return query.list();
    }

    @Override
    /**
     * Get N publication if not attempts have been registered, or the number of attempts is less than allowed amount.
     */
    public List<Publication> getPublicationsWithAccessionButNoDOIAndLessAttempts(int maxAttempts, int maxResults) {
        Session session = HibernateUtil.currentSession();
        String hql = """
                      select p from Publication p
                      where p.doi is null  
                      and p.accessionNumber is not null  
                      and ( not exists ( 
                        select 'x' from DOIAttempt da where da.publication.zdbID = p.zdbID 
                      ) OR exists ( 
                        select 'x' from DOIAttempt da where da.publication.zdbID = p.zdbID 
                        and da.numAttempts < :attempts 
                      )  )
                      order by p.publicationDate desc 
                     """;
        Query<Publication> query = session.createQuery(hql, Publication.class);
        query.setParameter("attempts", maxAttempts);
        if (maxResults >= 0) {
            query.setMaxResults(maxResults);
        }
        return query.list();
    }

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

        PaginationResult<Publication> paginationResult = new PaginationResult<>();
        Query<Publication> query;
        String hql;
        List<Publication> resultList;
        Session session = HibernateUtil.currentSession();

        hql = """
              select p.publication 
               from PublicationAttribution p 
               where p.dataZdbID = :genotypeZdbID 
              """;
        query = session.createQuery(hql, Publication.class);
        query.setParameter("genotypeZdbID", genotype.getZdbID());
        resultList = query.list();
        Set<Publication> pubList = new HashSet<>(resultList);


        hql = """
              select p.publication 
               from PublicationAttribution p, DataAlias da 
                where p.dataZdbID = da.zdbID 
               and da.dataZdbID = :genotypeZdbID 
              """;
        query = session.createQuery(hql, Publication.class);
        query.setParameter("genotypeZdbID", genotype.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);


        if (maxPubs >= 0) {
            paginationResult.setPopulatedResults((new ArrayList<>(pubList)).subList(0, maxPubs));
        } else {
            paginationResult.setPopulatedResults(new ArrayList<>(pubList));
        }
        paginationResult.setTotalCount(pubList.size());
        return paginationResult;
    }

    public List<Publication> getPublicationByPmid(Integer pubMedID) {
        String hql = "from Publication where accessionNumber = :id";
        Query<Publication> query = HibernateUtil.currentSession().createQuery(hql, Publication.class);
        query.setParameter("id", pubMedID);
        return query.list();
    }

    @Override
    public int getNumberDirectPublications(String zdbID) {
        return Integer.parseInt(HibernateUtil.currentSession().createNativeQuery("""
                                                                              select count(*) 
                                                                              from record_attribution ra 
                                                                              where ra.recattrib_data_zdb_id=:zdbID 
                                                                              """)
            .setParameter("zdbID", zdbID)
            .uniqueResult()
            .toString()
        );
    }

    @Override
    public List<Ortholog> getOrthologListByPub(String pubID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                     select distinct ortho, ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder 
                     from Ortholog as ortho 
                     join ortho.evidenceSet as evidence 
                     where evidence.publication.zdbID = :pubID 
                     order by ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder
                     """;
        Query query = session.createQuery(hql);
        query.setParameter("pubID", pubID);

        setTupleResultTransformer(query, (Object[] tuple, String[] aliases) -> tuple[0]);

        List<Ortholog> orthologList = (List<Ortholog>) query.list();
        return orthologList;
    }

    @Override
    public List<Ortholog> getOrthologListByMrkr(String mrkrID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                     select distinct ortho, ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder 
                     from Ortholog as ortho 
                     join ortho.evidenceSet as evidence 
                     where ortho.zebrafishGene.zdbID = :mrkrID 
                     order by ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder
                     """;
        Query query = session.createQuery(hql);
        query.setParameter("mrkrID", mrkrID);
        setTupleResultTransformer(query, (Object[] tuple, String[] aliases) -> tuple[0]);

        List<Ortholog> orthologList = (List<Ortholog>) query.list();
        return orthologList;
    }

    @Override
    public List<Ortholog> getOrthologPaginationByPub(String pubID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                     select distinct ortho, ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder 
                     from Ortholog as ortho 
                     join ortho.evidenceSet as evidence 
                     where evidence.publication.zdbID = :pubID 
                     order by ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder
                     """;
        Query query = session.createQuery(hql);
        query.setParameter("pubID", pubID);
        setTupleResultTransformer(query, (Object[] tuple, String[] aliases) -> tuple[0]);

        return (List<Ortholog>) query.list();
    }

    @Override
    public PaginationResult<Ortholog> getOrthologPaginationByPub(String pubID, GeneBean searchBean) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                     select distinct ortho, ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder 
                     from Ortholog as ortho 
                     join ortho.evidenceSet as evidence 
                     where evidence.publication.zdbID = :pubID 
                     order by ortho.zebrafishGene.abbreviationOrder, ortho.ncbiOtherSpeciesGene.organism.displayOrder
                     """;
        Query query = session.createQuery(hql);
        query.setParameter("pubID", pubID);
        setTupleResultTransformer(query, (Object[] tuple, String[] aliases) -> tuple[0]);

        PaginationResult<Ortholog> paginationResult = PaginationResultFactory.createResultFromScrollableResultAndClose(
            searchBean.getFirstRecordOnPage() - 1, searchBean.getLastRecordOnPage(), query.scroll());
        paginationResult.setStart(searchBean.getFirstRecord());

        return paginationResult;
    }

    @Override
    public List<Publication> getPublicationWithPubMedId(Integer maxResult) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                     from Publication as publication
                           where publication.accessionNumber is not null 
                           AND publication.type in (:type) 
                           AND publication.status = :status
                     """;


        Query<Publication> query = session.createQuery(hql, Publication.class);
        query.setParameterList("type", List.of(PublicationType.JOURNAL, PublicationType.REVIEW));
        query.setParameter("status", Publication.Status.ACTIVE);
        if (maxResult != null) {
            query.setMaxResults(maxResult);
        }

        return query.list();
    }

    public SortedSet<Publication> getAllPublicationsForFeature(Feature feature) {
        Query<Publication> query;
        String hql;
        List<Publication> resultList;
        Session session = HibernateUtil.currentSession();

        hql = """
              select p.publication 
               from PublicationAttribution p 
               where p.dataZdbID = :featureZdbID 
              """;
        query = session.createQuery(hql, Publication.class);
        query.setParameter("featureZdbID", feature.getZdbID());
        resultList = query.list();
        SortedSet<Publication> pubList = new TreeSet<>(resultList);


        hql = """
              select p.publication 
               from PublicationAttribution p , DataAlias  da 
                where p.dataZdbID = da.zdbID 
               and da.dataZdbID = :featureZdbID 
              """;
        query = session.createQuery(hql, Publication.class);
        query.setParameter("featureZdbID", feature.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);


        hql = """
              select p.publication 
               from PublicationAttribution p , FeatureMarkerRelationship fmr 
               where fmr.feature.zdbID  = :featureZdbID 
               and fmr.feature.zdbID  = p.dataZdbID 
              """;

        query = session.createQuery(hql, Publication.class);
        query.setParameter("featureZdbID", feature.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);


        hql = """
              select p.publication 
               from PublicationAttribution p , GenotypeFeature gtf 
               where gtf.genotype.zdbID  = p.dataZdbID 
                and gtf.feature.zdbID = :featureZdbID 
              """;
        query = session.createQuery(hql, Publication.class);
        query.setParameter("featureZdbID", feature.getZdbID());
        resultList = query.list();
        pubList.addAll(resultList);

        return pubList;
    }

    public SortedSet<Publication> getPublicationForJournal(Journal journal) {
        Query<Publication> query;
        String hql;
        List<Publication> resultList;
        Session session = HibernateUtil.currentSession();

        hql = """
              select publication 
               from Publication publication 
               where publication.journal = :journalWithPub 
              """;
        query = session.createQuery(hql, Publication.class);
        query.setParameter("journalWithPub", journal);
        resultList = query.list();
        SortedSet<Publication> pubList = new TreeSet<>(resultList);

        return pubList;
    }

    public Journal getJournalByID(String zdbID) {
        return HibernateUtil.currentSession().get(Journal.class, zdbID);
    }

    public SortedSet<Publication> getAllPublicationsForGenotypes(List<Genotype> genotypes) {
        List<String> genotypeZdbIDs = genotypes.stream().map(Genotype::getZdbID).toList();

        Session session = HibernateUtil.currentSession();

        String hql = "SELECT p.publication FROM PublicationAttribution p WHERE p.dataZdbID IN :genotypeZdbIDs";
        Query<Publication> query = session.createQuery(hql, Publication.class);
        query.setParameter("genotypeZdbIDs", genotypeZdbIDs);
        SortedSet<Publication> pubList = new TreeSet<>(query.list());

        hql = "SELECT p.publication FROM PublicationAttribution p, DataAlias da WHERE p.dataZdbID = da.zdbID AND da.dataZdbID IN :genotypeZdbIDs";
        query = session.createQuery(hql, Publication.class);
        query.setParameter("genotypeZdbIDs", genotypeZdbIDs);
        pubList.addAll(query.list());

        return pubList;
    }

    public SortedSet<Publication> getAllPublicationsForGenotype(Genotype genotype) {
        return getAllPublicationsForGenotypes(List.of(genotype));
    }

    public List<String> getPublicationIDsForGOwithField(String zdbID) {
        zdbID = "ZFIN:" + zdbID;
        Session session = HibernateUtil.currentSession();
        String sql = """
                     select distinct mrkrgoev_source_zdb_id 
                      from marker_go_term_evidence, inference_group_member 
                      where mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id and infgrmem_inferred_from = :zdbID
                     """;
        NativeQuery query = session.createNativeQuery(sql);
        query.setParameter("zdbID", zdbID);
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

    public List<String> getFeatureNamesWithNoGenotypesForPub(String pubZdbID) {
        String sql = """
                     select distinct f.feature_name, f.feature_zdb_id, ra1.recattrib_source_zdb_id
                      from feature f
                      join record_attribution ra1 on ra1.recattrib_data_zdb_id=f.feature_zdb_id
                      where ra1.recattrib_source_zdb_id = :pubID
                      and ra1.recattrib_source_type = 'standard'
                      and not exists (
                          select 'x'
                          from genotype_feature gf, record_attribution ra2
                          where gf.genofeat_feature_zdb_id = f.feature_zdb_id
                          and ra2.recattrib_source_type = 'standard'
                          and gf.genofeat_geno_zdb_id = ra2.recattrib_data_zdb_id
                          and ra2.recattrib_source_zdb_id = :pubID
                      );
                     """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("pubID", pubZdbID);
        setTupleResultTransformer(query, (Object[] tuple, String[] aliases) -> tuple[0]);

        return query.list();
    }

    public int deleteExpressionExperimentIDswithNoExpressionResult(Publication publication) {
        String sql = """
                     delete from expression_experiment2 x 
                      where x.xpatex_source_zdb_id = :pubID 
                        and not exists ( 
                                         select 'x' 
                                           from expression_result2 ee, expression_figure_stage efs 
                                          where efs.efs_xpatex_zdb_id = x.xpatex_zdb_id AND ee.xpatres_efs_id = efs.efs_pk_id
                                        ); 
                     """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("pubID", publication.getZdbID());
        return query.executeUpdate();
    }

    public List<String> getTalenOrCrisprFeaturesWithNoRelationship(String pubZdbID) {
        String sql = """
                     select distinct feature_name
                      from record_attribution, feature
                      where recattrib_source_zdb_id = :pubID
                      and recattrib_data_zdb_id = feature_zdb_id
                      and exists (
                        select 'x' from feature_assay
                        where featassay_feature_zdb_id = recattrib_data_zdb_id
                        and (featassay_mutagen = 'CRISPR' or featassay_mutagen = 'TALEN')
                      )
                      and (
                        not exists (
                          select 'x' from feature_marker_relationship
                          where fmrel_ftr_zdb_id = recattrib_data_zdb_id
                          and fmrel_type = 'is allele of') 
                        or not exists (
                          select 'x' from feature_marker_relationship
                          where fmrel_ftr_zdb_id = recattrib_data_zdb_id
                          and fmrel_type = 'created by')
                      );
                     """;
        Query query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("pubID", pubZdbID);
        return query.list();
    }

    public long getMarkerCount(Publication publication) {
        String sql = """
                     select count(*) FROM (
                       SELECT fmrel_mrkr_zdb_id
                       FROM record_attribution, feature_marker_relationship
                       WHERE recattrib_source_zdb_id = :zdbID
                             AND recattrib_data_zdb_id = fmrel_ftr_zdb_id
                             AND fmrel_type = 'is allele of'
                     
                       UNION
                     
                       SELECT mrkr_zdb_id
                       FROM record_attribution, marker
                       WHERE recattrib_source_zdb_id = :zdbID
                             AND recattrib_data_zdb_id = mrkr_zdb_id
                             AND mrkr_type IN
                                 (
                                   SELECT mtgrpmem_mrkr_type
                                   FROM marker_type_group_member
                                   WHERE mtgrpmem_mrkr_type_group in ('GENEDOM_AND_NTR','SEARCH_MK')
                                 )
                             AND (mrkr_type <> 'MRPHLNO' AND mrkr_type <> 'EFG')  
                     
                       UNION
                     
                       SELECT mr.mrel_mrkr_2_zdb_id
                       FROM record_attribution ra, marker m, marker_relationship mr
                       WHERE recattrib_source_zdb_id = :zdbID
                             AND recattrib_data_zdb_id = mrkr_zdb_id
                             AND m.mrkr_zdb_id = mr.mrel_mrkr_1_zdb_id
                             AND mrel_type = 'knockdown reagent targets gene'  
                     ) as q3 ;
                     """;

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
        String sql = """
                     select count(recattrib_data_zdb_id)
                      from  record_attribution, marker
                      where recattrib_source_zdb_id = :zdbID
                       and  recattrib_data_zdb_id   = mrkr_zdb_id
                       and  mrkr_type in 
                         (select mtgrpmem_mrkr_type from marker_type_group_member
                                         where mtgrpmem_mrkr_type_group = 'SEARCH_SEG');
                     """;
        return getCount(sql, publication.getZdbID());
    }

    public long getExpressionCount(Publication publication) {
        String sql = """
                     select count(distinct xpatfig_fig_zdb_id)
                       from figure, expression_pattern_figure
                      where fig_source_zdb_id = :zdbID
                              and fig_zdb_id=xpatfig_fig_zdb_id
                     """;
        return getCount(sql, publication.getZdbID());
    }

    public long getPhenotypeCount(Publication publication) {
        String sql ="""
                      select count(distinct pg_fig_zdb_id)
                        from figure, phenotype_source_generated
                       where fig_source_zdb_id = :zdbID
                              and pg_fig_zdb_id = fig_zdb_id
                     """;
        return getCount(sql, publication.getZdbID());
    }

    public long getPhenotypeAlleleCount(Publication publication) {
        String sql = """
                     select count(distinct geno_zdb_id)
                     from   record_attribution, genotype
                     where  recattrib_source_zdb_id = :zdbID
                       and  recattrib_data_zdb_id = geno_zdb_id
                       and  geno_is_wildtype = 'f';
                     """;
        return getCount(sql, publication.getZdbID());
    }

    public long getFeatureCount(Publication publication) {
        String sql = """
            select count (distinct recattrib_data_zdb_id)
            from record_attribution
            where recattrib_source_zdb_id = :zdbID
            and recattrib_data_zdb_id like 'ZDB-ALT-%'
            """;
        return getCount(sql, publication.getZdbID());
    }

    public long getFishCount(Publication publication) {
        String sql = """
                     select count(distinct fish_zdb_id)
                     from   record_attribution, fish
                     where  recattrib_source_zdb_id = :zdbID
                       and  recattrib_data_zdb_id = fish_zdb_id;
                     """;
        return getCount(sql, publication.getZdbID());
    }

    public long getOrthologyCount(Publication publication) {
        String sql = """
                     select count(*) from ortholog 
                     where exists ( 
                     select 'x' from ortholog_evidence where oev_pub_zdb_id = :zdbID 
                     and oev_ortho_zdb_id = ortho_zdb_ID)
                     """;
        return getCount(sql, publication.getZdbID());
    }

    public long getMappingDetailsCount(Publication publication) {
        String sql = """
            SELECT Count (tem.member_id)
                FROM(select distinct lms_member_1_zdb_id as member_id
                    from linkage, linkage_membership_search
                    WHERE lnkg_source_zdb_id = :zdbID
            AND lms_lnkg_zdb_id = lnkg_zdb_id
            UNION
            SELECT distinct lsingle_member_zdb_id
            FROM linkage, linkage_single
            WHERE lnkg_source_zdb_id =  :zdbID
            AND lsingle_lnkg_zdb_id = lnkg_zdb_id
            AND NOT EXISTS(SELECT 'x'
            FROM linkage_membership_search
            WHERE lms_lnkg_zdb_id = lnkg_zdb_id)
                )as tem
            """;

        return getCount(sql, publication.getZdbID());
    }

    public Boolean canDeletePublication(Publication publication) {

        String sql = """
            select count (recattrib_source_zdb_id)
                from record_attribution, figure
            where recattrib_source_zdb_id = :zdbID
            and recattrib_data_zdb_id = fig_zdb_id
            and(exists(select 'x'
            from phenotype_experiment
            where phenox_fig_zdb_id = fig_zdb_id)
            or exists (select 'x'
            from construct_figure
            where consfig_fig_zdb_id = fig_zdb_id)
            or exists (select 'x'
            from expression_pattern_figure
            where xpatfig_fig_zdb_id = fig_zdb_id)
            or exists (select 'x'
            from genotype_figure_fast_search
            where gffs_fig_zdb_id = fig_zdb_id)
                         );
            """;

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
        Query<Fish> query = session.createQuery("from Fish where handle = :handle", Fish.class);
        query.setParameter("handle", handle);
        return query.getSingleResult();
    }

    @Override
    public List<Fish> getNonWTFishByPublication(String publicationID) {
        Session session = HibernateUtil.currentSession();

        String hql = """
                     select distinct fish from Fish fish, PublicationAttribution record
                          where record.publication.zdbID = :pubID 
                                and record.dataZdbID = fish.zdbID
                         order by fish.name
                     """;
        Query<Fish> query = session.createQuery(hql, Fish.class);
        query.setParameter("pubID", publicationID);
        return query.list();
    }

    @Override
    public List<Fish> getWildtypeFish() {
        String hql = """
            from Fish as fish where
              fish.wildtype = true
            order by fish.name
            """;
        Query<Fish> query = HibernateUtil.currentSession().createQuery(hql, Fish.class);
        return query.list();
    }

    public PublicationTrackingHistory currentTrackingStatus(Publication publication) {
        Query<PublicationTrackingHistory> query = HibernateUtil.currentSession().createQuery("""
            from PublicationTrackingHistory
            where publication = :publication
            and  isCurrent = true
            order by date desc
                        """, PublicationTrackingHistory.class);
        query.setParameter("publication", publication);
        return query.uniqueResult();
    }

    public List<PublicationTrackingHistory> fullTrackingHistory(Publication publication) {
        Query<PublicationTrackingHistory> query = HibernateUtil.currentSession().createQuery("""
            from PublicationTrackingHistory
            where publication = :publication
            order by date desc
                        """, PublicationTrackingHistory.class);
        query.setParameter("publication", publication);
        return query.list();
    }

    public List<PublicationTrackingStatus> getAllPublicationStatuses() {
        return HibernateUtil.currentSession().createQuery("from PublicationTrackingStatus", PublicationTrackingStatus.class).list();
    }

    public PublicationTrackingStatus getPublicationTrackingStatus(long id) {
        return HibernateUtil.currentSession().get(PublicationTrackingStatus.class, id);
    }

    @Override
    public Long getPublicationTrackingStatus(Person person, int days, PublicationTrackingStatus... status) {

        Calendar dateInPast = Calendar.getInstance();
        dateInPast.add(Calendar.DATE, -days);

        String hql = "select count(m) from PublicationTrackingHistory as m where " +
                     "m.updater = :person AND m.status in (:status) and m.date > :dateInPast";
        return HibernateUtil.currentSession().createQuery(hql, Long.class)
            .setParameter("person", person)
            .setParameterList("status", status)
            .setParameter("dateInPast", dateInPast)
            .uniqueResult();
    }

    public PublicationTrackingStatus getPublicationStatusByName(PublicationTrackingStatus.Name name) {
        Query<PublicationTrackingStatus> query = HibernateUtil.currentSession().createQuery("""
            from PublicationTrackingStatus
            where name = :name
            """, PublicationTrackingStatus.class);
        query.setParameter("name", name);
        return query.getSingleResult();
    }

    public List<PublicationTrackingLocation> getAllPublicationLocations() {
        Query<PublicationTrackingLocation> query = HibernateUtil.currentSession().createQuery("""
            from PublicationTrackingLocation
            order by displayOrder
            """, PublicationTrackingLocation.class);
        return query.getResultList();
    }

    public List<Publication> getAllPublications() {
        return HibernateUtil.currentSession()
            .createQuery("from Publication", Publication.class)
            .list();
    }

    @Override
    public List<Publication> getAllOpenPublications() {
        List<PublicationTrackingStatus> statuses = getAllPublicationStatuses()
            .stream()
            .filter(status -> status.getType() != PublicationTrackingStatus.Type.CLOSED)
            .collect(Collectors.toList());

        List<PublicationTrackingHistory> trackingHistoryList = currentSession()
            .createQuery("from PublicationTrackingHistory where isCurrent = true AND status in (:status) order by publication desc ", PublicationTrackingHistory.class)
            .setParameterList("status", statuses)
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

    public PublicationTrackingLocation getPublicationTrackingLocation(long id) {
        return HibernateUtil.currentSession().get(PublicationTrackingLocation.class, id);
    }

    public DashboardPublicationList getPublicationsByStatus(Long status, Long location, String owner, int count,
                                                            int offset, String sort) {

        Query<PublicationTrackingHistory> listCriteria = createPubsByStatusCriteria(status, location, owner, sort, false);
        PaginationResult<PublicationTrackingHistory> histories = PaginationResultFactory
            .createResultFromScrollableResultAndClose(offset, offset + count, listCriteria.scroll());

        Query countsCriteria = createPubsByStatusCriteria(status, location, owner, null, true);
        List countList = countsCriteria.list();
        Map<String, Long> counts = new HashMap<>();
        for (Object item : countList) {
            Object[] tuple = (Object[]) item;
            PublicationTrackingStatus.Name pubStatus = (PublicationTrackingStatus.Name) tuple[0];
            counts.put(pubStatus.toString(), (long) tuple[1]);
        }

        DashboardPublicationList result = new DashboardPublicationList();
        result.setTotalCount(histories.getTotalCount());
        result.setPublications(histories.getPopulatedResults().stream()
            .map(converter::toDashboardPublicationBean)
            .collect(Collectors.toList()));
        converter.setRelatedLinks(result);
        result.setStatusCounts(counts);

        return result;
    }

    public List<PublicationFileType> getAllPublicationFileTypes() {
        return HibernateUtil.currentSession().createQuery("from PublicationFileType", PublicationFileType.class).list();
    }

    public PublicationFileType getPublicationFileType(long id) {
        return HibernateUtil.currentSession().get(PublicationFileType.class, id);
    }

    public PublicationFile getPublicationFile(long id) {
        return HibernateUtil.currentSession().get(PublicationFile.class, id);
    }

    @Override
    public List<PublicationFile> getAllPublicationFiles() {
        return HibernateUtil.currentSession().createQuery("from PublicationFile", PublicationFile.class).list();
    }

    public PublicationFileType getPublicationFileTypeByName(PublicationFileType.Name name) {
        return HibernateUtil.currentSession()
            .createQuery(" from PublicationFileType where name = :name", PublicationFileType.class)
            .setParameter("name", name)
            .uniqueResult();
    }

    public PublicationFile getOriginalArticle(Publication publication) {
        PublicationFileType originalArticle = getPublicationFileTypeByName(PublicationFileType.Name.ORIGINAL_ARTICLE);
        return (PublicationFile) HibernateUtil.currentSession()
            .createQuery("from PublicationFile where publication = :publication and type = :type", PublicationFile.class)
            .setParameter("publication", publication)
            .setParameter("type", originalArticle)
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
        CorrespondenceSentMessage originalCorrespondence = session
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
            sql = """
                   select ra.recattrib_source_zdb_id  
                   from record_attribution ra, marker_go_term_evidence ev 
                   where  ev.mrkrgoev_zdb_id  = ra.recattrib_data_zdb_id 
                   and  :markerZdbID = ev.mrkrgoev_mrkr_zdb_id 
                   and :markerGoEvdTermZdbID = ev.mrkrgoev_term_zdb_id
                   and :evidenceCode = ev.mrkrgoev_evidence_code
                  """;

            Query query = session.createNativeQuery(sql);
            query.setParameter("markerZdbID", markerZdbID);
            query.setParameter("markerGoEvdTermZdbID", markerGoEvdTermZdbID);
            query.setParameter("evidenceCode", evidenceCode);
            pubIDs = query.list();
        } else {
            sql ="""
                   select ra.recattrib_source_zdb_id  
                   from record_attribution ra ,  marker_go_term_evidence ev, inference_group_member inf 
                   where  ev.mrkrgoev_zdb_id  = ra.recattrib_data_zdb_id 
                   and inf.infgrmem_mrkrgoev_zdb_id = ev.mrkrgoev_zdb_id 
                   and  :markerZdbID = ev.mrkrgoev_mrkr_zdb_id 
                   and :markerGoEvdTermZdbID = ev.mrkrgoev_term_zdb_id
                   and :evidenceCode = ev.mrkrgoev_evidence_code
                   and :inference = inf.infgrmem_inferred_from
                  """;
            Query query = session.createNativeQuery(sql);
            query.setParameter("markerZdbID", markerZdbID);
            query.setParameter("markerGoEvdTermZdbID", markerGoEvdTermZdbID);
            query.setParameter("evidenceCode", evidenceCode);
            query.setParameter("inference", inference);
            pubIDs = query.list();
        }
        return pubIDs;
    }

    @Override
    public List<String> getPublicationIdsForFeatureType(String featureZdbID) {
        Session session = HibernateUtil.currentSession();
        String sql = """
                      select ra.recattrib_source_zdb_id  
                      from record_attribution ra
                      where :featureType = ra.recattrib_source_type 
                      and :featureID = ra.recattrib_data_zdb_id
                     """;
        NativeQuery query = session.createNativeQuery(sql);
        query.setParameter("featureType", "feature type");
        query.setParameter("featureID", featureZdbID);
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
        NativeQuery query = session.createNativeQuery(sql);
        query.setParameter("publicationZdbID", publicationId);
        List<String> dataIds = query.list();
        return dataIds;
    }

    @Override
    public Long getDirectlyAttributed(Publication publication) {
        String sql = """
                     select count(*) 
                      from record_attribution 
                      where recattrib_source_zdb_id = :zdbID 
                     """;
        return getCount(sql, publication.getZdbID());
    }

    @Override
    public List<MetricsByDateBean> getMetricsByDate(Calendar start,
                                                    Calendar end,
                                                    PublicationMetricsFormBean.QueryType queryType,
                                                    PublicationMetricsFormBean.Interval groupInterval,
                                                    PublicationMetricsFormBean.GroupType groupType) {
        String sql = getMetricsByDateSQL(queryType, groupInterval, groupType);
        return HibernateUtil.currentSession().createNativeQuery(sql)
            .setParameter("start", start)
            .setParameter("end", end)
            .setResultTransformer(Transformers.aliasToBean(MetricsByDateBean.class))
            .list();
    }

    private String getMetricsByDateSQL(PublicationMetricsFormBean.QueryType queryType,
                                      PublicationMetricsFormBean.Interval groupInterval,
                                      PublicationMetricsFormBean.GroupType groupType) {
        String groupExpression = "";
        String dateExpression = "";
        boolean currentStatusOnly = false;
        switch (groupType) {
            case ACTIVE:
                groupExpression = "status";
                currentStatusOnly = true;
                break;
            case INDEXED:
                groupExpression = "pub_indexed_status";
                dateExpression = "pub_indexed_date";
                currentStatusOnly = true;
                break;
            case STATUS:
                groupExpression = "pts_status_display";
                dateExpression = "pth_status_insert_date";
                break;
            case LOCATION:
                groupExpression = "pub_location_or_prioritization_status";
                dateExpression = "pth_status_insert_date";
                break;
        }
        if (queryType == PublicationMetricsFormBean.QueryType.PET_DATE) {
            dateExpression = "pub_arrival_date";
            currentStatusOnly = true;
        }

        //use aggregate functions to summarize the data from the inner query (parameterized_pub_location_metrics)
        //which is built using various parameters in the switch statement above
        String sqlTemplate =
            """
            WITH step1 AS (SELECT DISTINCT zdb_id, %1$s AS category, %2$s AS date FROM pub_location_metrics %4$s),
                 step2 AS (SELECT zdb_id, category, date_trunc('%3$s', date) AS date FROM step1 WHERE date >= :start AND date < :end)
            SELECT
                category,
                date,
                count(*) AS count
            FROM
                step2
            GROUP BY
                date,
                category
            ORDER BY
                date,
                category
            """;

        String currentStatusOnlyClause = currentStatusOnly ? " WHERE pth_status_is_current = true " : "";
        return String.format(sqlTemplate, groupExpression, dateExpression, groupInterval, currentStatusOnlyClause);
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
        String sql = String.format("""
            select
              %1$s as category,
              avg(history.pth_days_in_status) as average,
              stddev(history.pth_days_in_status) as "standardDeviation",
              min(history.pth_days_in_status) as minimum,
              max(history.pth_days_in_status) as maximum
            from pub_tracking_history history
            inner join publication pub on pub.zdb_id = history.pth_pub_zdb_id
            left outer join pub_tracking_status status on history.pth_status_id = status.pts_pk_id
            left outer join pub_tracking_location location on history.pth_location_id = location.ptl_pk_id
            where history.pth_status_is_current = 'f'
            and history.pth_days_in_status is not null
            and history.pth_status_insert_date < :end
            and pub.jtype = :type
            group by %1$s
            """, groupExpression);
        return HibernateUtil.currentSession().createNativeQuery(sql)
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
                """
                select
                  grouper as category,
                  avg(age) as average,
                  stddev(age) as "standardDeviation",
                  min(age) as minimum,
                  max(age) as maximum,
                  avg_of_largest(cast(age as numeric)) as "oldestAverage"
                from (
                  select
                    %1$s as grouper,
                    extract(day from (current_date - history.pth_status_insert_date)) as age
                  from
                    pub_tracking_history history
                        inner join publication pub on pub.zdb_id = history.pth_pub_zdb_id
                        left outer join pub_tracking_status status on history.pth_status_id = status.pts_pk_id
                        left outer join pub_tracking_location location on history.pth_location_id = location.ptl_pk_id
                        where history.pth_status_is_current = true
                        and pub.jtype = :type
                ) as subq
                group by grouper
                """, groupExpression);
        return HibernateUtil.currentSession().createNativeQuery(sql)
            .setParameter("type", PublicationType.JOURNAL.getDisplay())
            .setResultTransformer(Transformers.aliasToBean(MetricsOnDateBean.class))
            .list();
    }

    @Override
    public ProcessingChecklistTask getProcessingChecklistTask(ProcessingChecklistTask.Task task) {
        return HibernateUtil.currentSession()
            .createQuery("from ProcessingChecklistTask where task = :task", ProcessingChecklistTask.class)
            .setParameter("task", task)
            .uniqueResult();
    }

    @Override
    public PublicationProcessingChecklistEntry getProcessingChecklistEntry(long id) {
        return HibernateUtil.currentSession()
            .get(PublicationProcessingChecklistEntry.class, id);
    }

    @Override
    public List<PubmedPublicationAuthor> getPubmedPublicationAuthorsByPublication(Publication publication) {
        return HibernateUtil.currentSession()
            .createQuery("from PubmedPublicationAuthor where publication = :publication", PubmedPublicationAuthor.class)
            .setParameter("publication", publication)
            .list();
    }

    @Override
    public boolean isNewFeaturePubAttribution(Feature marker, String publicationId) {
        String hql = """
                     select pa from PublicationAttribution as pa where 
                      pa.dataZdbID = :featureID AND pa.sourceType = :source 
                     """;

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

    @Override
    public Map<Marker, Boolean> areNewGenePubAttribution(List<Marker> attributedMarker, String publicationId) {
        if (CollectionUtils.isEmpty(attributedMarker))
            return null;
            String hql = """
                     select pa.dataZdbID, count(pa) as ct from PublicationAttribution as pa where 
                      pa.dataZdbID in (:markerIDs) AND pa.sourceType = :source 
                      group by pa.dataZdbID 
                     """;
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
        String hql = """
            select distinct marker from SequenceTargetingReagent marker, RecordAttribution attr
            where attr.dataZdbID = marker.zdbID
            and marker.markerType.name in (:markerTypes)
            """;
        if (publicationID != null) {
            hql += "and attr.sourceZdbID = :pubID ";
        }
        if (pagination.getFieldFilter(FieldFilter.STR_NAME) != null) {
            hql += " AND marker.abbreviation like '%" + pagination.getFieldFilter(FieldFilter.STR_NAME) + "%'";
        }
        Query<SequenceTargetingReagent> query = session.createQuery(hql, SequenceTargetingReagent.class);
        if (publicationID != null) {
            query.setParameter("pubID", publicationID);
        }
        query.setParameterList("markerTypes", List.of(Marker.Type.MRPHLNO.name(), Marker.Type.CRISPR.name(), Marker.Type.TALEN.name()));
        return query.list();
    }

    @Override
    public List<Image> getImages(Publication publication) {
        String hql = """
                     from Image where 
                      figure.publication = :publication 
                      order by figure.orderingLabel 
                     """;

        Query<Image> query = HibernateUtil.currentSession().createQuery(hql, Image.class);
        query.setParameter("publication", publication);
        return query.list();
    }

    @Override
    public Map<Fish, Map<GenericTerm, List<PhenotypeStatementWarehouse>>> getAllFiguresForPhenotype() {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select phenos from PhenotypeStatementWarehouse phenos,
                FishExperiment fishox
            where phenos.phenotypeWarehouse.fishExperiment = fishox AND
            exists(select 'x' from GeneGenotypeExperiment where fishExperiment = fishox)
            """;
        Query<PhenotypeStatementWarehouse> query = session.createQuery(hql, PhenotypeStatementWarehouse.class);

        Map<Fish, List<PhenotypeStatementWarehouse>> phenoMap = query.list().stream()
            .collect(groupingBy((pheno) -> pheno.getPhenotypeExperiment().getFishExperiment().getFish()));
        Map<Fish, Map<GenericTerm, List<PhenotypeStatementWarehouse>>> phenMap = new HashMap<>();
        phenoMap.forEach((fish, phenotypeStatementWarehouses) -> {
            // groupBy Generic terms
            Map<GenericTerm, List<PhenotypeStatementWarehouse>> map = new HashMap<>();
            phenotypeStatementWarehouses.forEach(warehouse -> getTermIDs(warehouse).forEach(term -> {
                List<PhenotypeStatementWarehouse> list = map.computeIfAbsent(term, k -> new ArrayList<>());
                list.add(warehouse);
            }));
            phenMap.put(fish, map);
        });
        return phenMap;
    }

    @Override
    public Map<Fish, Map<Experiment, Map<GenericTerm, Set<PhenotypeStatementWarehouse>>>> getAllChebiPhenotype() {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select phenos from PhenotypeStatementWarehouse phenos,
                FishExperiment fishox, ExperimentCondition condition
            where phenos.phenotypeWarehouse.fishExperiment = fishox AND
            fishox.experiment = condition.experiment
            AND condition.chebiTerm is not null
            """;
        org.hibernate.query.Query<PhenotypeStatementWarehouse> query = session.createQuery(hql, PhenotypeStatementWarehouse.class);

        Map<Fish, Map<Experiment, List<PhenotypeStatementWarehouse>>> phenoMap = query.list().stream()
            .filter(Objects::nonNull)
            .collect(groupingBy((pheno) -> pheno.getPhenotypeExperiment().getFishExperiment().getFish(),
                groupingBy(pheno -> pheno.getPhenotypeWarehouse().getFishExperiment().getExperiment())));
        Map<Fish, Map<Experiment, Map<GenericTerm, Set<PhenotypeStatementWarehouse>>>> phenMap = new HashMap<>();
        phenoMap.forEach((fish, phenotypeStatementWarehouses) -> {
            Map<Experiment, Map<GenericTerm, Set<PhenotypeStatementWarehouse>>> phMap = new HashMap<>();
            phenotypeStatementWarehouses.forEach((experiment, phenotypeStatementWarehouses1) -> {
                // groupBy Generic terms
                Map<GenericTerm, Set<PhenotypeStatementWarehouse>> map = new HashMap<>();
                phenotypeStatementWarehouses1.forEach(warehouse -> getChebiTermIDs(warehouse).forEach(term -> {
                    Set<PhenotypeStatementWarehouse> list = map.computeIfAbsent(term, k -> new HashSet<>());
                    list.add(warehouse);
                }));
                phMap.put(experiment, map);
            });
            phenMap.put(fish, phMap);
        });
        return phenMap;
    }

    public List<PhenotypeStatementWarehouse> getAllChebiPhenotypeExperiment(Experiment experiment) {
        Session session = HibernateUtil.currentSession();

        String hql = """
            select phenos from PhenotypeStatementWarehouse phenos,
                FishExperiment fishox, ExperimentCondition condition
            where phenos.phenotypeWarehouse.fishExperiment = fishox AND
            fishox.experiment = condition.experiment
            AND condition.experiment = :experiment
            AND condition.chebiTerm is not null
            """;
        org.hibernate.query.Query<PhenotypeStatementWarehouse> query = session.createQuery(hql, PhenotypeStatementWarehouse.class);
        query.setParameter("experiment", experiment);
        return query.list();
    }

    private static List<GenericTerm> getTermIDs(PhenotypeStatementWarehouse phenotype) {
        List<GenericTerm> termIDs = new ArrayList<>();
        if (phenotype.getEntity().getSubterm() != null) {
            termIDs.add(phenotype.getEntity().getSubterm());
        }
        if (phenotype.getEntity().getSuperterm() != null) {
            termIDs.add(phenotype.getEntity().getSuperterm());
        }
        if (phenotype.getRelatedEntity() != null && phenotype.getRelatedEntity().getSubterm() != null) {
            termIDs.add(phenotype.getRelatedEntity().getSubterm());
        }
        if (phenotype.getRelatedEntity() != null && phenotype.getRelatedEntity().getSuperterm() != null) {
            termIDs.add(phenotype.getRelatedEntity().getSuperterm());
        }
        if (phenotype.getQuality() != null) {
            termIDs.add(phenotype.getQuality());
        }
        return termIDs;
    }

    private static List<GenericTerm> getChebiTermIDs(PhenotypeStatementWarehouse phenotype) {
        return phenotype.getPhenotypeWarehouse().getFishExperiment().getExperiment().getExperimentConditions().stream()
            .filter(Objects::nonNull)
            .map(ExperimentCondition::getChebiTerm).toList();
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

    private List<MarkerStatistic> createMarkerStatistics(List<Tuple> list, GenericTerm anatomyTerm) {
        if (list == null) {
            return null;
        }

        List<MarkerStatistic> markers = new ArrayList<>();
        for (Tuple stats : list) {
            String markerZdbID = (String) stats.get(0);
            Marker marker = markerRepository.getMarkerByID(markerZdbID);
            MarkerStatistic statistic = new MarkerStatistic(anatomyTerm, marker);
            statistic.setNumberOfFigures((Integer) stats.get(2));
            statistic.setHasImages(((Integer) stats.get(3)) > 0);
            markers.add(statistic);
        }
        return markers;
    }

    private List<HighQualityProbe> createHighQualityProbeObjects(List<Object[]> list, Term aoTerm) {
        List<HighQualityProbe> probes = new ArrayList<>();
        if (list != null) {
            for (Object[] row : list) {
                Marker subGene = (Marker) row[0];
                Marker gene = (Marker) row[1];
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

    private List<Publication> getPublications(List<String> zdbIDs, boolean orderByDateDesc) {
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

    //TODO: refactor this one? Seems like we could at least combine all if statements into a single one
    private String getCommonPublicationSQL(String zdbID) {
        // Changes to this query need to be kept in sync with the analogous query
        // in db-data-config.sql!

        ActiveData.Type dataType = ActiveData.getType(zdbID);
        String commonPubSQL =
            """
             select * from (select ra.recattrib_source_zdb_id   
             from record_attribution ra   
             where :markerZdbID = ra.recattrib_data_zdb_id 
            """;
        // marker relationship 2_1
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += """
                             union 
                             select ra.recattrib_source_zdb_id  
                             from record_attribution ra , marker_relationship mr 
                             where :markerZdbID = mr.mrel_mrkr_2_zdb_id 
                             and  ra.recattrib_data_zdb_id = mr.mrel_zdb_id 
                            """;
        }
        // marker relationship 1_2
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += """
                             union 
                             select ra.recattrib_source_zdb_id  
                             from record_attribution ra , marker_relationship mr 
                             where :markerZdbID = mr.mrel_mrkr_1_zdb_id 
                             and  ra.recattrib_data_zdb_id = mr.mrel_zdb_id 
                            """;
        }
        // str marker type necessary ?
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += """
                             union 
                             select ra.recattrib_source_zdb_id  
                             from record_attribution ra , marker_relationship mr , marker m 
                             where :markerZdbID = mr.mrel_mrkr_2_zdb_id 
                             and  ra.recattrib_data_zdb_id = mr.mrel_mrkr_1_zdb_id 
                             and  mr.mrel_mrkr_1_zdb_id = m.mrkr_zdb_id 
                             and  m.mrkr_type in ('MRPHLNO', 'TALEN', 'CRISPR') 
                            """;
        }
        // data alias
        commonPubSQL += """
                         union 
                         select ra.recattrib_source_zdb_id  
                         from record_attribution ra , data_alias da  
                         where da.dalias_zdb_id = ra.recattrib_data_zdb_id 
                         and :markerZdbID = da.dalias_data_zdb_id 
                        """;
        // db link
        commonPubSQL += """
                         union 
                         select ra.recattrib_source_zdb_id  
                         from record_attribution ra , db_link dbl  
                         where  dbl.dblink_zdb_id  = ra.recattrib_data_zdb_id 
                         and  :markerZdbID = dbl.dblink_linked_recid 
                        """;
        // db link, marker_relationship
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += """
                             union 
                             select ra.recattrib_source_zdb_id  
                             from record_attribution ra , db_link dbl , marker_relationship mr 
                             where  dbl.dblink_zdb_id  = ra.recattrib_data_zdb_id 
                             and dbl.dblink_linked_recid = mr.mrel_mrkr_2_zdb_id 
                             and  :markerZdbID = mr.mrel_mrkr_1_zdb_id 
                             and  mr.mrel_type = 'gene encodes small segment' 
                            """;
        }
        // ortho
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += """
                             union 
                             select ra.recattrib_source_zdb_id  
                             from record_attribution ra ,  ortholog_evidence oe, ortholog o 
                             where  ra.recattrib_data_zdb_id = oe.oev_ortho_zdb_id 
                             and    oe.oev_ortho_zdb_id = o.ortho_zdb_id 
                             and    :markerZdbID = o.ortho_zebrafish_gene_zdb_id 
                            """;
        }
        // marker_go_term_Evidence
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += """
                             union 
                             select ra.recattrib_source_zdb_id  
                             from record_attribution ra ,  marker_go_term_evidence ev 
                             where  ev.mrkrgoev_zdb_id  = ra.recattrib_data_zdb_id 
                             and  :markerZdbID = ev.mrkrgoev_mrkr_zdb_id 
                            """;
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
            commonPubSQL += """
                             union 
                             select ra.recattrib_source_zdb_id  
                             from record_attribution ra ,  feature_marker_relationship fmr, genotype_feature gf 
                             where  gf.genofeat_geno_zdb_id  = ra.recattrib_data_zdb_id 
                             and  :markerZdbID = fmr.fmrel_mrkr_zdb_id 
                             and fmr.fmrel_ftr_zdb_id  = gf.genofeat_feature_zdb_id 
                            """;
        }
        // expression_experiment2
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += """
                             union 
                             select xpatex_source_zdb_id  
                             from expression_experiment2 
                             where :markerZdbID = xpatex_gene_zdb_id 
                            """;
        }
        // nomenclature
        if (ActiveData.isMarker(dataType)) {
            commonPubSQL += """
                             union 
                             select ra.recattrib_source_zdb_id  
                             from record_attribution ra, marker_history mh 
                             where mh.mhist_zdb_id  = ra.recattrib_data_zdb_id 
                             and  :markerZdbID = mh.mhist_mrkr_zdb_id 
                            """;
        }
        commonPubSQL += " ) as qt where recattrib_source_zdb_id like 'ZDB-PUB%'  ";
        return commonPubSQL;
    }

    private List<Marker> getMarkersPulledThroughFeatures(String pubID) {
        Session session = HibernateUtil.currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Marker> query = criteriaBuilder.createQuery(Marker.class);
        Root<Marker> marker = query.from(Marker.class);
        Join<Marker, FeatureMarkerRelationship> fmrel = marker.join("featureMarkerRelationships");
        Join<FeatureMarkerRelationship, Feature> feature = fmrel.join("feature");
        Join<Feature, RecordAttribution> attr = feature.join("publications");

        query.select(marker)
            .distinct(true)
            .where(
                criteriaBuilder.and(
                    criteriaBuilder.equal(marker, fmrel.get("marker")),
                    criteriaBuilder.equal(fmrel.get("type"), FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF),
                    criteriaBuilder.equal(fmrel.get("feature"), feature),
                    criteriaBuilder.equal(feature.get("zdbID"), attr.get("dataZdbID")),
                    criteriaBuilder.equal(attr.get("sourceZdbID"), pubID)
                )
            );


        return session.createQuery(query).getResultList();
    }

    private List<Marker> getMarkersPulledThroughSTRs(String pubID) {
        Session session = HibernateUtil.currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Marker> query = criteriaBuilder.createQuery(Marker.class);
        Root<Marker> marker = query.from(Marker.class);
        Join<Marker, MarkerRelationship> mrel = marker.join("secondMarkerRelationships");
        Join<MarkerRelationship, Marker> firstMarker = mrel.join("firstMarker");
        Join<Marker, RecordAttribution> attr = firstMarker.join("publications");
        query.select(marker)
            .distinct(true)
            .where(
                criteriaBuilder.and(
                    criteriaBuilder.equal(mrel.get("type"), MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE),
                    criteriaBuilder.equal(attr.get("sourceZdbID"), pubID)
                )
            );

        return session.createQuery(query).getResultList();
    }

    private List getMarkersByPublication(String pubID, List<MarkerType> markerTypes) {
        return getMarkersByPublicationQuery(pubID, markerTypes).list();
    }

    private Query<Marker> getMarkersByPublicationQuery(String pubID, List<MarkerType> markerTypes) {
        Session session = HibernateUtil.currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

        CriteriaQuery<Marker> criteriaQuery = criteriaBuilder.createQuery(Marker.class);
        Root<Marker> marker = criteriaQuery.from(Marker.class);
        Join<Marker, RecordAttribution> publications = marker.join("publications", JoinType.INNER);

        criteriaQuery.select(marker)
            .distinct(true)
            .where(
                criteriaBuilder.and(
                    criteriaBuilder.equal(publications.get("sourceZdbID"), pubID),
                    marker.get("markerType").in(markerTypes)
                )
            )
            .orderBy(criteriaBuilder.asc(marker.get("abbreviationOrder")));

        return session.createQuery(criteriaQuery);
    }

    private List<DOIAttempt> getDOIAttemptsFromPubs(List<Publication> publicationList) {
        if (CollectionUtils.isEmpty(publicationList)) {
            return new ArrayList<>();
        }
        Session session = HibernateUtil.currentSession();
        String hql = """
                       select da from DOIAttempt da   
                      where da.publication in (:publicationList) 
                     """;
        Query<DOIAttempt> query = session.createQuery(hql, DOIAttempt.class);
        query.setParameterList("publicationList", publicationList);
        return query.list();
    }

    @SuppressWarnings("unchecked")
    /**
     * This class creates DOI attempts for pubs without previous DOI attempt entries.
     */
    private List<DOIAttempt> createDOIAttemptsForPubs(List<Publication> publicationList) {
        if (CollectionUtils.isEmpty(publicationList)) {
            return new ArrayList<DOIAttempt>();
        }
        Session session = HibernateUtil.currentSession();
        String hql = """
                      select p from Publication p 
                      where not exists ( select 'x' from DOIAttempt da where da.publication = p ) 
                      and p in (:publicationList) 
                     """;
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

    private long getMarkerCountByMarkerType(String zdbID, String type) {
        String sql = """
            select count(recattrib_data_zdb_id)
            from  record_attribution, marker
            where recattrib_source_zdb_id = :zdbID
              and recattrib_data_zdb_id = mrkr_zdb_id
              and mrkr_type = :mrkrType
              """;

        Query<Number> query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("zdbID", zdbID);
        query.setParameter("mrkrType", type);
        return query.getSingleResult().longValue();
    }

    private long getCount(String sql, String zdbID) {
        NativeQuery query = HibernateUtil.currentSession().createNativeQuery(sql);
        query.setParameter("zdbID", zdbID);
        return ((Number) query.uniqueResult()).longValue();
    }

    private PublicationTrackingLocation getPublicationTrackingLocationByName(PublicationTrackingLocation.Name name) {
        return HibernateUtil.currentSession()
            .createQuery("from PublicationTrackingLocation where name = :name", PublicationTrackingLocation.class)
            .setParameter("name", name)
            .uniqueResult();
    }

    private Query createPubsByStatusCriteria(Long status, Long location, String owner, String sort, boolean groupBy) {

        String hql = "";
        if (groupBy) {
            hql = """
                select pubTrack.status.name, count(*)
                """;
        }
        hql += """
            from PublicationTrackingHistory as pubTrack
            """;
        List<String> hqlClauses = new ArrayList<>();
        HashMap<String, Object> parameterMap = new HashMap<>();

        hqlClauses.add("pubTrack.isCurrent = true");

        if (status != null) {
            hqlClauses.add("pubTrack.status = :status");
            parameterMap.put("status", getPublicationTrackingStatus(status));
        }

        if (location != null) {
            if (location == 0) {
                hqlClauses.add("pubTrack.location is null");
            } else {
                hqlClauses.add("pubTrack.location = :location");
                parameterMap.put("location", getPublicationTrackingLocation(location));
            }
        }

        if (StringUtils.isNotEmpty(owner)) {
            if (owner.equals("*")) {
                hqlClauses.add("pubTrack.owner is not null");
            } else {
                hqlClauses.add("pubTrack.owner = :owner");
                parameterMap.put("owner", profileRepository.getPerson(owner));
            }
        }
        hql += " where " + String.join(" and ", hqlClauses);
        if (groupBy) {
            hql += " group by pubTrack.status.name, pubTrack.status.dashboardOrder ";
        }
        hql += " order by pubTrack.status.dashboardOrder ";
        if (StringUtils.isNotEmpty(sort)) {
            boolean isAscending = true;
            if (sort.startsWith("-")) {
                sort = sort.substring(1);
                isAscending = false;
            }
            sort = sort.replaceAll("pub.", "pubTrack.publication.");
            hql += ", " + sort;
            if (isAscending) {
                hql += " asc";
            } else {
                hql += " desc";
            }
        }

        Query query = HibernateUtil.currentSession().createQuery(hql);
        parameterMap.forEach(query::setParameter);
        return query;
    }

    @Override
    public List<CorrespondenceNeed> getCorrespondenceNeedByPublicationID(String zdbID) {
        CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
        CriteriaQuery<CorrespondenceNeed> cr = criteriaBuilder.createQuery(CorrespondenceNeed.class);
        Root<CorrespondenceNeed> root = cr.from(CorrespondenceNeed.class);
        cr.select(root);
        cr.where(criteriaBuilder.equal(root.get("publication").get("zdbID"), zdbID));
        return currentSession().createQuery(cr).list();
    }

    @Override
    public List<CorrespondenceNeedReason> getAllCorrespondenceNeedReasons() {
        CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
        CriteriaQuery<CorrespondenceNeedReason> cr = criteriaBuilder.createQuery(CorrespondenceNeedReason.class);
        Root<CorrespondenceNeedReason> root = cr.from(CorrespondenceNeedReason.class);
        cr.select(root);
        cr.orderBy(criteriaBuilder.asc(root.get("order")));
        return currentSession().createQuery(cr).list();
    }

    @Override
    public void deleteCorrespondenceNeedByPublicationID(String pubID) {
        CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
        CriteriaDelete<CorrespondenceNeed> cr = criteriaBuilder.createCriteriaDelete(CorrespondenceNeed.class);
        Root<CorrespondenceNeed> root = cr.from(CorrespondenceNeed.class);
        cr.where(criteriaBuilder.equal(root.get("publication").get("zdbID"), pubID));
        currentSession().createQuery(cr).executeUpdate();
    }

    @Override
    public CorrespondenceNeedReason getCorrespondenceNeedReasonByID(long id) {
        return currentSession().get(CorrespondenceNeedReason.class, id);
    }

    @Override
    public void insertCorrespondenceNeed(CorrespondenceNeed correspondenceNeed) {
        currentSession().save(correspondenceNeed);
    }

    @Override
    public List<CorrespondenceResolution> getCorrespondenceResolutionByPublicationID(String zdbID) {
        CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
        CriteriaQuery<CorrespondenceResolution> cr = criteriaBuilder.createQuery(CorrespondenceResolution.class);
        Root<CorrespondenceResolution> root = cr.from(CorrespondenceResolution.class);
        cr.select(root);
        cr.where(criteriaBuilder.equal(root.get("publication").get("zdbID"), zdbID));
        return currentSession().createQuery(cr).list();
    }

    @Override
    public List<CorrespondenceResolutionType> getAllCorrespondenceResolutionTypes() {
        CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
        CriteriaQuery<CorrespondenceResolutionType> cr = criteriaBuilder.createQuery(CorrespondenceResolutionType.class);
        Root<CorrespondenceResolutionType> root = cr.from(CorrespondenceResolutionType.class);
        cr.select(root);
        cr.orderBy(criteriaBuilder.asc(root.get("order")));
        return currentSession().createQuery(cr).list();
    }

    @Override
    public void deleteCorrespondenceResolutionByPublicationID(String pubID) {
        CriteriaBuilder criteriaBuilder = currentSession().getCriteriaBuilder();
        CriteriaDelete<CorrespondenceResolution> cr = criteriaBuilder.createCriteriaDelete(CorrespondenceResolution.class);
        Root<CorrespondenceResolution> root = cr.from(CorrespondenceResolution.class);
        cr.where(criteriaBuilder.equal(root.get("publication").get("zdbID"), pubID));
        currentSession().createQuery(cr).executeUpdate();
    }

    @Override
    public CorrespondenceResolutionType getCorrespondenceResolutionTypeByID(long id) {
        return currentSession().get(CorrespondenceResolutionType.class, id);
    }

    @Override
    public void insertCorrespondenceResolution(CorrespondenceResolution correspondenceResolution) {
        currentSession().save(correspondenceResolution);
    }

    @Override
    public Map<Publication, List<PublicationDbXref>> getAllDataSetsPublication() {
        String hql = """
                      select p from Publication p 
                      where p.dbXrefs is not empty
                     """;
        Query<Publication> query = HibernateUtil.currentSession().createQuery(hql, Publication.class);
        Map<Publication, List<PublicationDbXref>> returnMap = new HashMap<>();
        query.list().forEach(publication -> {
            List<PublicationDbXref> xrefs = returnMap.computeIfAbsent(publication, k -> new ArrayList<>());
            xrefs.addAll(publication.getDbXrefs());
        });
        return returnMap;
    }

    private Map<Publication, List<STRTargetRow>> strMaps;

    @Override
    public Map<Publication, List<STRTargetRow>> getAllAttributedSTRs(Pagination pagination) {
        if (strMaps != null)
            return strMaps;
        Session session = HibernateUtil.currentSession();
        String hql = """
            select marker, attr.publication from SequenceTargetingReagent marker, PublicationAttribution attr
            where attr.dataZdbID = marker.zdbID
            and marker.markerType.name in (:markerTypes)
            """;
        if (pagination.getFieldFilter(FieldFilter.STR_NAME) != null) {
            hql += " AND marker.abbreviation like '%" + pagination.getFieldFilter(FieldFilter.STR_NAME) + "%'";
        }
        if (pagination.getFieldFilter(FieldFilter.STR_TYPE) != null) {
            hql += " AND marker.zdbID like '%" + pagination.getFieldFilter(FieldFilter.STR_TYPE) + "%'";
        }
        Query<Tuple> query = session.createQuery(hql, Tuple.class);
        query.setParameterList("markerTypes", List.of(Marker.Type.MRPHLNO.name(), Marker.Type.CRISPR.name(), Marker.Type.TALEN.name()));
        Map<Publication, List<STRTargetRow>> map = new HashMap<>();
        query.list().forEach(tuple -> {
            SequenceTargetingReagent str = (SequenceTargetingReagent) tuple.get(0);
            for (Marker target : str.getTargetGenes()) {
                STRTargetRow row = new STRTargetRow(str, target);
                List<STRTargetRow> rows = map.computeIfAbsent((Publication) tuple.get(1), k -> new ArrayList<>());
                rows.add(row);
            }
        });
        strMaps = map;
        return strMaps;
    }

}
