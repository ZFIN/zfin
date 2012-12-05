package org.zfin.mutant.repository;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.zfin.database.BtsContainsService;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionResult;
import org.zfin.expression.Figure;
import org.zfin.feature.Feature;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.fish.FeatureGene;
import org.zfin.fish.FishAnnotation;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.WarehouseSummary;
import org.zfin.fish.presentation.Fish;
import org.zfin.fish.repository.FishService;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.HibernateMarkerRepository;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.*;
import org.zfin.mutant.ConstructGeneFeature;
import org.zfin.mutant.presentation.Construct;
import org.zfin.mutant.repository.ConstructRepository;
//import org.zfin.mutant.repository.ConstructService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.framework.search.SortType;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.infrastructure.ZdbFlag;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.marker.MarkerRelationship;
import org.zfin.profile.Organization;
import org.zfin.repository.PaginationResultFactory;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.FeatureDBLink;

import java.math.BigInteger;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;

/**
 * Basic repository class to handle fish searches against a database.
 */
@Repository
public class HibernateConstructRepository implements ConstructRepository {

    private static Logger logger = Logger.getLogger(HibernateConstructRepository.class);


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
            annotation.setConstructID((String)annotationObj[1]);
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



    public List<Genotype> getFigureGenotype(Figure figure,String constructID){



            String sqlResults = "select distinct geno_zdb_id " +
                    "from figure_term_construct_search ftcs, construct_search cs, genotype,genotype_experiment " +
                    "where ftcs.ftcs_cs_id =cs.cons_pk_id and " +
                    "genox_geno_zdb_id=geno_zdb_id and " +
                    "geno_is_wildtype='f' and " +
                    "genox_zdb_id=ftcs_genox_zdb_id and " +
                    "cons_construct_zdb_id=:constructId and "  +
                    "ftcs_fig_zdb_id=:figId  " ;
            //sqlResults += btsService.getFullOrClause();
            Session session = HibernateUtil.currentSession();
            Query sqlQuery = session.createSQLQuery(sqlResults);
            sqlQuery.setParameter("constructId", constructID);
            sqlQuery.setParameter("figId", figure.getZdbID());

            List<String> genotypes= sqlQuery.list();
            List<Genotype> genotype=new ArrayList<Genotype>(genotypes.size());
            for (String genos : genotypes){
                Genotype geno = getMutantRepository().getGenotypeByID(genos);
                genotype.add(geno);
            }
            return  genotype;

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
            fromClauseList.add( "figure_term_construct_search ");
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
            String btsContainsClause1=btsService1.getFullClauseConstructs();
            whereClauseList.add(" exists" + "(" + "select 'c' from construct_component_search where " +
                    "ccs_cons_id = cons_pk_id and ccs_relationship_type = 'promoter of'  ");
            whereClauseList.add(btsContainsClause1 +")");
        }

        if (criteria.getEngineeredRegionCriteria().hasValues()) {
            BtsContainsService btsService2 = new BtsContainsService("ccs_engineered_region_all_names");
            btsService2.addBtsExpandedValueList("ccs_engineered_region_all_names", criteria.getEngineeredRegionCriteria().getValues());
            whereClauseList.add(" exists" + "(" + "select 'c' from construct_component_search where " +
                    "ccs_cons_id = cons_pk_id and ccs_relationship_type = 'contains engineered region'  ");
            String btsContainsClause1=btsService2.getFullClauseConstructs();
            whereClauseList.add(btsContainsClause1 +")");

        }
        if (criteria.getExpressedGeneCriteria().hasValues()) {
            BtsContainsService btsService3 = new BtsContainsService("ccs_coding_all_names");
            btsService3.addBtsExpandedValueList("ccs_coding_all_names", criteria.getExpressedGeneCriteria().getValues());
            whereClauseList.add(" exists" + "(" + "select 'c' from construct_component_search where " +
                    "ccs_cons_id = cons_pk_id and ccs_relationship_type = 'coding sequence of'  ");
            String btsContainsClause1=btsService3.getFullClauseConstructs();
            whereClauseList.add(btsContainsClause1 +")");

        }
        if (criteria.getPhenotypeAnatomyCriteria().hasValues()) {
            BtsContainsService btsService5 = new BtsContainsService("ftcs_term_group");
            btsService5.addBtsValueList("ftcs_term_group", criteria.getPhenotypeAnatomyCriteria().getValues());
            whereClauseList.add(" ftcs_cs_id = cons_pk_id   ");
            String btsContainsClause1=btsService5.getFullClauseConstructs();
            whereClauseList.add(btsContainsClause1);

        }


            if (criteria.getPtypeCriteria().isTrue()) {
                typeClauseList.add("'Promoter Trap Construct'");
            }
             if (criteria.getGtypeCriteria().isTrue()) {
                 typeClauseList.add("'Gene Trap Construct'" );
                               }

                if (criteria.getEtypeCriteria().isTrue()){

                    typeClauseList.add( "'Enhancer Trap Construct'");
                }

                if (criteria.getTgtypeCriteria().isTrue()){
                    typeClauseList.add("'Transgenic Construct'" );
                }


        if (criteria.getAffectedGeneCriteria().hasValues()) {
            BtsContainsService btsService4 = new BtsContainsService("cgfrv_allele_gene_all_names");
            btsService4.addBtsExpandedValueList("cgfrv_allele_gene_all_names", criteria.getAffectedGeneCriteria().getValues());
            whereClauseList.add(" exists" + "(" + "select 'c' from construct_gene_feature_result_view where " +
                    "cgfrv_cs_id = cons_pk_id  ");
            String btsContainsClause1=btsService4.getFullClauseConstructs();
            if (criteria.getAvailabilityCriteria().isTrue()) {
                whereClauseList.add(btsContainsClause1);
            }
            else{
            whereClauseList.add(btsContainsClause1 +")");
            }

        }
        if (criteria.getAvailabilityCriteria().isTrue()) {
            if (criteria.getAffectedGeneCriteria().hasValues())
                whereClauseList.add(" cgfrv_available is not null" +")" );
            else
            {
                whereClauseList.add(" exists" + "(" + "select 'c' from construct_gene_feature_result_view where " +
                    "cgfrv_cs_id = cons_pk_id and cgfrv_available is not null" +")");
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
       if (!(typeClause.length() == 0)){
           if (!(whereClause.length() == 0)) {
              whereClause.append(" and ");
               whereClause.append(" cons_type in ( " );
              whereClause.append(typeClause.toString());
               whereClause.append(")");
           }
           else{
               whereClause.append(" cons_type in ( " );
               whereClause.append(typeClause.toString());
               whereClause.append(")");
           }
           }


          String orderBy="cons_abbrev_order";


        String sql = " select  distinct " + baseSelectColumns +
                "  from " + fromClause.toString();
        if (StringUtils.isNotEmpty(whereClause.toString()))
            sql += "  where " +  whereClause.toString();
        sql += "  order by " + orderBy;
        logger.debug(criteria.toString());
        logger.debug(sql);
        Query query = session.createSQLQuery(sql);


        return query;
    }

    //todo: improve this, I'm sure.

    public List<ExpressionResult> getExpressionForConstructs(String constructID,  List<String> termIDs) {

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

            List<String> expRsltlist=sqlQuery.list();
            List<ExpressionResult> expressionResult = new ArrayList<ExpressionResult>();
            for(String zdbId: expRsltlist){
                ExpressionResult expRslt = (ExpressionResult) HibernateUtil.currentSession().get(ExpressionResult.class,zdbId) ;
                expressionResult.add(expRslt) ;
            }
            return expressionResult ;


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
        ConstructSearch annotation=(ConstructSearch)query.uniqueResult();
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

}
