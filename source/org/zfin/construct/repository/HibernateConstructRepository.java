package org.zfin.construct.repository;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.BasicTransformerAdapter;
import org.springframework.stereotype.Repository;
import org.zfin.construct.ConstructComponent;
import org.zfin.construct.ConstructRelationship;
import org.zfin.construct.presentation.ConstructComponentPresentation;
import org.zfin.database.BtsContainsService;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.fish.WarehouseSummary;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.gwt.root.dto.FeatureDTO;
import org.zfin.gwt.root.dto.MarkerDTO;
import org.zfin.gwt.root.server.DTOMarkerService;
import org.zfin.infrastructure.*;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.*;
import org.zfin.mutant.presentation.Construct;
import org.zfin.construct.ConstructCuration;
import org.zfin.construct.repository.ConstructRepository;
//import org.zfin.mutant.repository.ConstructService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.profile.service.ProfileService;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.repository.RepositoryFactory;

import java.math.BigInteger;
import java.util.*;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

/**
 * Basic repository class to handle fish searches against a database.
 */
@Repository
public class HibernateConstructRepository implements ConstructRepository {

    private static Logger logger = Logger.getLogger(org.zfin.construct.repository.HibernateConstructRepository.class);
    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();

    public ConstructSearchResult getConstructs(ConstructSearchCriteria criteria) {

        ConstructSearchResult results = new ConstructSearchResult();
        Query query = generateConstructSearchQuery(criteria);


        int start = criteria.getStart() - 1;
        logger.debug("Setting start for jdbc to: " + start);
        logger.debug("Setting rowcount for jdbc to: " + start + criteria.getRows());

        PaginationResult constructObjects = PaginationResultFactory.createResultFromScrollableResultAndClose(start, start + criteria.getRows(), query.scroll());

        if (constructObjects.getTotalCount() == 0) {
            results.setResultsFound(0);
            return results;
        }

        logger.debug("Results found: " + constructObjects.getTotalCount());
        logger.debug("constructObjects.getPopulatedResults().size(): " + constructObjects.getPopulatedResults().size());
        logger.debug("constructObjects: " + constructObjects.toString());

        List<ConstructSearch> constructSearch = new ArrayList<ConstructSearch>(constructObjects.getTotalCount());
        for (Object obj : constructObjects.getPopulatedResults()) {
            ConstructSearch annotation = new ConstructSearch();
            Object[] annotationObj = (Object[]) obj;
            BigInteger bigInteger = (BigInteger) annotationObj[0];
            annotation.setID(bigInteger.longValue());
            annotation.setConstructID((String) annotationObj[1]);
            annotation.setConstructName((String) annotationObj[2]);


            constructSearch.add(annotation);

        }

        logger.debug("constructSearch size: " + constructSearch.size());

        List<Construct> construct = new ArrayList<Construct>(constructObjects.getTotalCount());
        for (ConstructSearch annotation : constructSearch) {
            construct.add(getConstructFromConstructSearch(annotation, criteria));
        }

        logger.debug("Construct size: " + construct.size());
        logger.debug("constructObjects total count: " + constructObjects.getTotalCount());

        results.setResults(construct);
        results.setResultsFound(constructObjects.getTotalCount());
        results.setStart(criteria.getStart());

        return results;
    }

    private Construct getConstructFromConstructSearch(ConstructSearch annotation, ConstructSearchCriteria criteria) {
        Construct singleConstruct = new Construct();
        singleConstruct.setName(annotation.getConstructName());
        singleConstruct.setID(annotation.getConstructID());
        singleConstruct.setConstructpkid(annotation.getID());
        addFigures(singleConstruct, criteria);
        return singleConstruct;
    }


    private void addFigures(Construct construct, ConstructSearchCriteria criteria) {
        if (criteria == null)
            addAllFigures(construct);
        else {
            List<String> values = criteria.getPhenotypeAnatomyCriteria().getValues();
            addFiguresByTermValues(construct, values);
        }
    }

//     figure stuff here


    private void addAllFigures(Construct construct) {
        Set<ZfinFigureEntity> figures = getAllFigures(construct.getID());
        setImageAttributeOnConstruct(construct, figures);
    }

    private void addFiguresByTermValues(Construct construct, List<String> values) {
        Set<ZfinFigureEntity> figures = getFiguresByConstructAndTerms(construct.getID(), values);
        setImageAttributeOnConstruct(construct, figures);
    }

    private void setImageAttributeOnConstruct(Construct construct, Set<ZfinFigureEntity> figures) {
        if (figures == null || figures.size() == 0)
            return;
        construct.setExpressionFigures(figures);
        for (ZfinFigureEntity figure : figures)
            if (figure.isHasImage())
                construct.setImageAvailable(true);
    }

    /**
     * Retrieve figures for a given fish.
     *
     * @param constructID fish ID
     * @return set of figures.
     */


    public Set<ZfinFigureEntity> getAllFigures(String constructID) {
        String sql = "select  distinct ftcs_fig_zdb_id, " +
                "CASE " +
                " WHEN img_fig_zdb_id is not null then 'true'" +
                " ELSE 'false'" +
                "END as hasImage" +
                " from figure_term_construct_search, construct_search , OUTER image " +
                "where img_fig_zdb_id = ftcs_fig_zdb_id " +
                "and cons_construct_zdb_id = :constructID " +
                "and ftcs_cs_id = cons_pk_id ";
        Session session = HibernateUtil.currentSession();
        Query query = session.createSQLQuery(sql);

        query.setParameter("constructID", constructID);
        List<Object[]> constructObjects = query.list();
        if (constructObjects == null)
            return null;

        Set<ZfinFigureEntity> zfinFigureEntities = new HashSet<ZfinFigureEntity>(constructObjects.size());
        for (Object[] annotationObj : constructObjects) {
            ZfinFigureEntity zfinFigureEntity = new ZfinFigureEntity();
            zfinFigureEntity.setID((String) annotationObj[0]);
            zfinFigureEntity.setHasImage(Boolean.parseBoolean(((String) annotationObj[1]).trim()));
            zfinFigureEntities.add(zfinFigureEntity);
        }
        return zfinFigureEntities;
    }


    public Set<ZfinFigureEntity> getFiguresByConstructAndTerms(String constructID, List<String> termIDs) {
        if (termIDs == null)
            return getAllFigures(constructID);

        Session session = HibernateUtil.currentSession();
        BtsContainsService btsService = new BtsContainsService("ftcs_term_group");
        btsService.addBtsValueList("ftcs_term_group", termIDs);
        String sqlFeatures = "select  distinct ftcs_fig_zdb_id, " +
                "CASE " +
                " WHEN img_fig_zdb_id is not null then 'true'" +
                " else 'false'" +
                "END as hasImage  " +
                "from figure_term_construct_search, construct_search, OUTER image " +
                "where img_fig_zdb_id = ftcs_fig_zdb_id " +
                "and cons_construct_zdb_id = :constructID " +
                "and ftcs_cs_id = cons_pk_id AND ";

        sqlFeatures += btsService.getFullClauseConstructs();
        Query sqlQuery = session.createSQLQuery(sqlFeatures);
        sqlQuery.setParameter("constructID", constructID);
        List<Object[]> objs = sqlQuery.list();
        if (objs == null)
            return null;

        Set<ZfinFigureEntity> figures = new HashSet<ZfinFigureEntity>(objs.size());
        if (objs.size() > 0) {
            for (Object[] groupMember : objs) {
                ZfinFigureEntity figure = new ZfinFigureEntity();
                figure.setID((String) groupMember[0]);
                String hasImages = (String) groupMember[1];
                figure.setHasImage(Boolean.parseBoolean(hasImages.trim()));
                figures.add(figure);
            }
        }
        return figures;
    }


    private Query generateConstructSearchQuery(ConstructSearchCriteria criteria) {
        Session session = HibernateUtil.currentSession();

        List<String> whereClauseList = new ArrayList<String>();
        List<String> typeClauseList = new ArrayList<String>();
        List<String> fromClauseList = new ArrayList<String>();


        String baseSelectColumns = " cons_pk_id, cons_construct_zdb_id, cons_name, cons_abbrev, cons_abbrev_order ";


        fromClauseList.add(" construct_search ");
        if (criteria.getPhenotypeAnatomyCriteria().hasValues()) {
            fromClauseList.add("figure_term_construct_search ");
        }

        StringBuilder fromClause = new StringBuilder();
        for (String clause : fromClauseList) {
            if (!(fromClause.length() == 0))
                fromClause.append(" , \n");
            fromClause.append(clause);
        }

        BtsContainsService btsService = new BtsContainsService("cons_all_names");
        btsService.addBtsExpandedValueList("cons_all_names", criteria.getConstructNameCriteria().getValues());
        String btsContainsClause = btsService.getFullClauseConstructs();
        if (btsContainsClause != null)
            whereClauseList.add(btsContainsClause);

        if (criteria.getGenePromoterCriteria().hasValues()) {

            BtsContainsService btsService1 = new BtsContainsService("ccs_promoter_all_names");
            btsService1.addBtsExpandedValueList("ccs_promoter_all_names", criteria.getGenePromoterCriteria().getValues());
            String btsContainsClause1 = btsService1.getFullClauseConstructs();
            whereClauseList.add(" exists" + "(" + "select 'c' from construct_component_search where " +
                    "ccs_cons_id = cons_pk_id and ccs_relationship_type = 'promoter of'  ");
            whereClauseList.add(btsContainsClause1 + ")");
        }

        if (criteria.getEngineeredRegionCriteria().hasValues()) {
            BtsContainsService btsService2 = new BtsContainsService("ccs_engineered_region_all_names");
            btsService2.addBtsExpandedValueList("ccs_engineered_region_all_names", criteria.getEngineeredRegionCriteria().getValues());
            whereClauseList.add(" exists" + "(" + "select 'c' from construct_component_search where " +
                    "ccs_cons_id = cons_pk_id and ccs_relationship_type = 'contains engineered region'  ");
            String btsContainsClause1 = btsService2.getFullClauseConstructs();
            whereClauseList.add(btsContainsClause1 + ")");

        }
        if (criteria.getExpressedGeneCriteria().hasValues()) {
            BtsContainsService btsService3 = new BtsContainsService("ccs_coding_all_names");
            btsService3.addBtsExpandedValueList("ccs_coding_all_names", criteria.getExpressedGeneCriteria().getValues());
            whereClauseList.add(" exists" + "(" + "select 'c' from construct_component_search where " +
                    "ccs_cons_id = cons_pk_id and ccs_relationship_type = 'coding sequence of'  ");
            String btsContainsClause1 = btsService3.getFullClauseConstructs();
            whereClauseList.add(btsContainsClause1 + ")");

        }
        if (criteria.getPhenotypeAnatomyCriteria().hasValues()) {
            BtsContainsService btsService5 = new BtsContainsService("ftcs_term_group");
            btsService5.addBtsValueList("ftcs_term_group", criteria.getPhenotypeAnatomyCriteria().getValues());
            whereClauseList.add(" ftcs_cs_id = cons_pk_id   ");
            String btsContainsClause1 = btsService5.getFullClauseConstructs();
            whereClauseList.add(btsContainsClause1);

        }


        if (criteria.getPtypeCriteria().isTrue()) {
            typeClauseList.add("'Promoter Trap Construct'");
        }
        if (criteria.getGtypeCriteria().isTrue()) {
            typeClauseList.add("'Gene Trap Construct'");
        }

        if (criteria.getEtypeCriteria().isTrue()) {

            typeClauseList.add("'Enhancer Trap Construct'");
        }

        if (criteria.getTgtypeCriteria().isTrue()) {
            typeClauseList.add("'Transgenic Construct'");
        }


        if (criteria.getAffectedGeneCriteria().hasValues()) {
            BtsContainsService btsService4 = new BtsContainsService("cgfrv_allele_gene_all_names");
            btsService4.addBtsExpandedValueList("cgfrv_allele_gene_all_names", criteria.getAffectedGeneCriteria().getValues());
            whereClauseList.add(" exists" + "(" + "select 'c' from construct_gene_feature_result_view where " +
                    "cgfrv_cs_id = cons_pk_id  ");
            String btsContainsClause1 = btsService4.getFullClauseConstructs();
            if (criteria.getAvailabilityCriteria().isTrue()) {
                whereClauseList.add(btsContainsClause1);
            } else {
                whereClauseList.add(btsContainsClause1 + ")");
            }

        }
        if (criteria.getAvailabilityCriteria().isTrue()) {
            if (criteria.getAffectedGeneCriteria().hasValues())
                whereClauseList.add(" cgfrv_available is not null" + ")");
            else {
                whereClauseList.add(" exists" + "(" + "select 'c' from construct_gene_feature_result_view where " +
                        "cgfrv_cs_id = cons_pk_id and cgfrv_available is not null" + ")");
            }

        }

        StringBuilder whereClause = new StringBuilder();
        StringBuilder typeClause = new StringBuilder();
        for (String clause : whereClauseList) {
            if (!(whereClause.length() == 0))
                whereClause.append(" and  ");
            whereClause.append(clause);
        }
        for (String orClause : typeClauseList) {
            if (!(typeClause.length() == 0))
                typeClause.append(" ,  ");
            typeClause.append(orClause);
        }
        if (!(typeClause.length() == 0)) {
            if (!(whereClause.length() == 0)) {
                whereClause.append(" and ");
                whereClause.append(" cons_type in ( ");
                whereClause.append(typeClause.toString());
                whereClause.append(")");
            } else {
                whereClause.append(" cons_type in ( ");
                whereClause.append(typeClause.toString());
                whereClause.append(")");
            }
        }


        String orderBy = "cons_abbrev_order";


        String sql = " select  distinct " + baseSelectColumns +
                "  from " + fromClause.toString();
        if (StringUtils.isNotEmpty(whereClause.toString()))
            sql += "  where " + whereClause.toString();
        sql += "  order by " + orderBy;
        logger.debug(criteria.toString());
        logger.debug(sql);
        Query query = session.createSQLQuery(sql);


        return query;
    }

    //todo: improve this, I'm sure.

    public List<ExpressionResult> getExpressionForConstructs(String constructID, List<String> termIDs) {

        Session session = HibernateUtil.currentSession();
        BtsContainsService btsService = new BtsContainsService("ftcs_term_group");
        btsService.addBtsValueList("ftcs_term_group", termIDs);
        String sqlFeatures = "select   xpatfig_xpatres_zdb_id " +

                "from figure_term_construct_search, construct_search, expression_pattern_figure , OUTER image " +
                "where img_fig_zdb_id = ftcs_fig_zdb_id " +
                "and cons_construct_zdb_id = :constructID " +
                "and ftcs_fig_zdb_id=xpatfig_fig_zdb_id " +
                "and ftcs_cs_id = cons_pk_id and   ";

        sqlFeatures += btsService.getFullOrClauseConstructs();
        Query sqlQuery = session.createSQLQuery(sqlFeatures);
        sqlQuery.setParameter("constructID", constructID);

        List<String> expRsltlist = sqlQuery.list();
        List<ExpressionResult> expressionResult = new ArrayList<ExpressionResult>();
        for (String zdbId : expRsltlist) {
            ExpressionResult expRslt = (ExpressionResult) HibernateUtil.currentSession().get(ExpressionResult.class, zdbId);
            expressionResult.add(expRslt);
        }
        return expressionResult;


    }


    private ZfinEntity getZfinEntity(String zdbID, String name) {
        ZfinEntity entity = new ZfinEntity();
        entity.setName(name);
        entity.setID(zdbID);
        return entity;
    }


    public Construct getConstruct(String constructID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select  constructSearch from ConstructSearch constructSearch  where " +
                " constructSearch.constructID = :constructID ";
        Query query = HibernateUtil.currentSession().createQuery(hql);
        query.setParameter("constructID", constructID);
        ConstructSearch annotation = (ConstructSearch) query.uniqueResult();
        if (annotation == null)
            return null;
        Construct construct = getConstructFromConstructSearch(annotation, null);
        return construct;
    }


    public WarehouseSummary getWarehouseSummary(WarehouseSummary.Mart mart) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(WarehouseSummary.class);
        criteria.add(Restrictions.eq("martName", mart.getName()));
        return (WarehouseSummary) criteria.uniqueResult();
    }

    /**
     * Retrieve the status of the construct mart:
     * true: construct mart ready for usage
     * false: construct mart is being rebuilt.
     *
     * @return status
     */

    public ZdbFlag getConstructMartStatus() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ZdbFlag.class);
        criteria.add(Restrictions.eq("type", ZdbFlag.Type.REGEN_CONSTRUCTMART));
        return (ZdbFlag) criteria.uniqueResult();
    }


    public ConstructRelationship getConstructRelationship(ConstructCuration marker1, Marker marker2, ConstructRelationship.Type type) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(ConstructRelationship.class);
        criteria.add(Restrictions.eq("construct", marker1));
        criteria.add(Restrictions.eq("marker", marker2));
        criteria.add(Restrictions.eq("type", type));
        return (ConstructRelationship) criteria.uniqueResult();

    }

    public ConstructRelationship getConstructRelationshipByID(String zdbID) {
        Session session = currentSession();
        Criteria criteria = session.createCriteria(ConstructRelationship.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        return (ConstructRelationship) criteria.uniqueResult();
    }


    public List<ConstructRelationship> getConstructRelationshipsByPublication(String publicationZdbID) {
        List<ConstructRelationship.Type> constructRelationshipList = new ArrayList<ConstructRelationship.Type>();
        constructRelationshipList.add(ConstructRelationship.Type.PROMOTER_OF);
        constructRelationshipList.add(ConstructRelationship.Type.CODING_SEQUENCE_OF);
        constructRelationshipList.add(ConstructRelationship.Type.CONTAINS_REGION);

        Session session = currentSession();
        String hql = "select distinct cmr from ConstructRelationship as cmr, " +
                "PublicationAttribution as attribution " +
                "where  attribution.dataZdbID = cmr.zdbID AND " +
                "cmr.type in (:constructRelationshipType)AND " +
                "attribution.publication.zdbID = :pubID ";

        Query query = session.createQuery(hql);
        query.setParameter("pubID", publicationZdbID);
        query.setParameterList("constructRelationshipType", constructRelationshipList);
        List<ConstructRelationship> constructRelationships = (List<ConstructRelationship>) query.list();
        Collections.sort(constructRelationships, new Comparator<ConstructRelationship>() {
            @Override
            public int compare(ConstructRelationship o1, ConstructRelationship o2) {
                return o1.getConstruct().getName().compareTo(o2.getConstruct().getName());
            }
        });
        // order
        /*Collections.sort(markerRelationships, new Comparator<ConstructRelationship>(){
            @Override
            public int compare(ConstructRelationship o1, ConstructRelationship o2) {
                return o1.getFirstMarker().getAbbreviationOrder().compareTo(o2.getFirstMarker().getAbbreviationOrder()) ;
            }
        });*/
        return constructRelationships;
    }


    public void addConstructRelationships(Set<Marker> promMarker, Set<Marker> codingMarker, ConstructCuration construct, String pubID) {
        //      HibernateUtil.createTransaction();

        if (!promMarker.isEmpty()) {
            for (Marker promMarkers : promMarker) {
                ConstructRelationship cmRel = getConstructRelationship(construct, promMarkers, ConstructRelationship.Type.PROMOTER_OF);
                if (cmRel == null) {
                    ConstructRelationship promMRel = new ConstructRelationship();
                    promMRel.setConstruct(construct);
                    promMRel.setMarker(promMarkers);
                    promMRel.setType(ConstructRelationship.Type.PROMOTER_OF);
                    currentSession().save(promMRel);
                    addConstructRelationshipAttribution(promMRel, pr.getPublication(pubID), construct);
                }
                // ir.insertRecordAttribution(promMRel.getZdbID(),pubID);

            }
        }
        if (!codingMarker.isEmpty()) {
            for (Marker codingMarkers : codingMarker) {
                ConstructRelationship cmRel = getConstructRelationship(construct, codingMarkers, ConstructRelationship.Type.CODING_SEQUENCE_OF);
                if (cmRel == null) {
                    ConstructRelationship codingRel = new ConstructRelationship();
                    codingRel.setConstruct(construct);
                    ;
                    codingRel.setMarker(codingMarkers);
                    codingRel.setType(ConstructRelationship.Type.CODING_SEQUENCE_OF);
                    currentSession().save(codingRel);
                    addConstructRelationshipAttribution(codingRel, pr.getPublication(pubID), construct);
                }
                //    ir.insertRecordAttribution(codingRel.getZdbID(),pubID);

                //

            }
        }
        currentSession().flush();
        //       flushAndCommitCurrentSession();

    }

    public void addConstructPub(ConstructCuration construct, Publication publication) {
        if (publication == null)
            throw new RuntimeException("Cannot attribute this marker with a blank pub.");

        String markerZdbID = construct.getZdbID();
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        RecordAttribution recordAttribution = ir.getRecordAttribution(markerZdbID, publication.getZdbID(), RecordAttribution.SourceType.STANDARD);

        // only add the publication when it is not there
        if (recordAttribution == null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setDataZdbID(markerZdbID);
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            pa.setPublication(publication);
            Set<PublicationAttribution> pubAttrbs = new HashSet<PublicationAttribution>();
            pubAttrbs.add(pa);
            Marker mrkr = new Marker();
            mrkr.setPublications(pubAttrbs);
            currentSession().save(pa);
        }
    }

    public ConstructCuration getConstructByID(String zdbID) {
        Session session = currentSession();
        return (ConstructCuration) session.get(ConstructCuration.class, zdbID);
    }

    public ConstructCuration getConstructByName(String conName) {
        Session session = currentSession();
        return (ConstructCuration) session.get(ConstructCuration.class, conName);
    }

    public void createConstruct(ConstructCuration construct, Publication pub) {
        if (construct.getName() == null)
            throw new RuntimeException("Cannot create a new construct without a name.");
        if (construct == null)
            throw new RuntimeException("No construct object provided.");
        if (construct.getConstructType() == null)
            throw new RuntimeException("Cannot create a new construct without a type.");
        if (pub == null)
            throw new RuntimeException("Cannot create a new construct without a publication.");

        construct.setOwner(ProfileService.getCurrentSecurityUser());
        if (!construct.getOwner().getAccountInfo().getRoot())
            throw new RuntimeException("Non-root user cannot create a construct");
        currentSession().save(construct);
        // Need to flush here to make the trigger fire as that will
        // create a MarkerHistory record needed.
        //   currentSession().flush();

        //add publication to attribution list.
        RepositoryFactory.getInfrastructureRepository().insertRecordAttribution(construct.getZdbID(), pub.getZdbID());

        // run procedure for fast search table

    }

    @Override
    public List<Fish> getFishByFigureConstruct(Figure figure, String constructID) {
        String sqlResults = "select distinct fish_zdb_id " +
                "from figure_term_construct_search ftcs, construct_search cs, fish_experiment,fish, genotype " +
                "where ftcs.ftcs_cs_id =cs.cons_pk_id and " +
                "genox_fish_zdb_id=fish_zdb_id and " +
                "fish_genotype_zdb_id=geno_zdb_id and " +
                "geno_is_wildtype='f' and " +
                "genox_zdb_id=ftcs_genox_zdb_id and " +
                "cons_construct_zdb_id=:constructId and " +
                "ftcs_fig_zdb_id=:figId  ";
        Session session = HibernateUtil.currentSession();
        Query sqlQuery = session.createSQLQuery(sqlResults);
        sqlQuery.setParameter("constructId", constructID);
        sqlQuery.setParameter("figId", figure.getZdbID());

        List<String> fishIDList = sqlQuery.list();
        List<Fish> fishList = new ArrayList<>(fishIDList.size());
        for (String fishID : fishIDList) {
            Fish geno = getMutantRepository().getFish(fishID);
            fishList.add(geno);
        }
        return fishList;
    }

    @Override
    public List<ConstructComponent> getConstructComponentsByComponentID(String componentZdbID) {
        Session session = HibernateUtil.currentSession();

        Criteria criteria = session.createCriteria(ConstructComponent.class);
        criteria.add(Restrictions.eq("componentZdbID", componentZdbID));
        return (List<ConstructComponent>) criteria.list();
    }

    public void addConstructRelationshipAttribution(ConstructRelationship cmrel, Publication attribution, ConstructCuration construct) {

        String attributionZdbID = attribution.getZdbID();
        String relZdbID = cmrel.getZdbID();

        if (attributionZdbID.equals(""))
            throw new RuntimeException("Cannot attribute this alias with a blank pub.");

        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        RecordAttribution recordAttribution = ir.getRecordAttribution(relZdbID, attributionZdbID, RecordAttribution.SourceType.STANDARD);

        // only add the publication when it is not there
        if (recordAttribution == null) {
            PublicationAttribution pa = new PublicationAttribution();
            pa.setSourceZdbID(attributionZdbID);
            pa.setDataZdbID(relZdbID);
            pa.setSourceType(RecordAttribution.SourceType.STANDARD);
            Publication publication = RepositoryFactory.getPublicationRepository().getPublication(attributionZdbID);
            pa.setPublication(publication);
            currentSession().save(pa);
            currentSession().refresh(cmrel);
            addConstructPub(construct, publication);
        }
        /*/change to construct signature
        Marker marker= getMarkerRepository().getMarkerByID(construct.getZdbID());
        ir.insertUpdatesTable(marker, "", "new attribution, construct relationship: " + cmrel.getZdbID() + " with pub: " + attributionZdbID, attributionZdbID, "");*/
    }
}
