package org.zfin.fish.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.zfin.database.BtsContainsService;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.fish.*;
import org.zfin.fish.presentation.FishResult;
import org.zfin.fish.presentation.MartFish;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.search.SortType;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.infrastructure.ZdbFlag;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.mutant.Fish;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.Term;
import org.zfin.repository.RepositoryFactory;
import org.zfin.search.FieldName;
import org.zfin.search.service.SolrService;

import java.util.*;

/**
 * Basic repository class to handle fish searches against a database.
 */
@Repository
public class HibernateFishRepository implements FishRepository {

    private static Logger logger = Logger.getLogger(HibernateFishRepository.class);



    private Query generateFishSearchQuery(FishSearchCriteria criteria) {
        Session session = HibernateUtil.currentSession();

        List<String> whereClauseList = new ArrayList<String>();
        List<String> fromClauseList = new ArrayList<String>();

        String baseSelectColumns = " fas_pk_id, fas_geno_name, \n" +
                " fas_str_group, fas_feature_group, fas_pheno_figure_count, fas_pheno_figure_group, \n" +
                " fas_all, fas_geno_long_name, fas_genox_group, fas_genotype_group, fas_xpat_figure_count, fas_xfigg_has_images ";

        String sortColumns = " fas_fish_significance  as complexity, \n";

        if (!StringUtils.isEmpty(criteria.getGeneOrFeatureNameCriteria().getValue())) {
            sortColumns = sortColumns +
                    " cast(fas_all_score as lvarchar), \n" +
                    " case when fas_all_score > 1 \n" +
                    "      then 100 " +
                    "      else 1  " +
                    " end as simple_score,  \n";
        } else {
            sortColumns = sortColumns +
                    " 'no score' as fas_all_score, \n" +
                    " case when 2 > 1 " +
                    "      then 100 " +
                    "      else 1  " +
                    " end as simple_score, \n";

        }

        sortColumns = sortColumns + " fas_gene_order, fas_feature_order, \n" +
                "fas_fish_parts_count \n";

        fromClauseList.add(" fish_annotation_search ");


        if (criteria.getExcludeSequenceTargetingReagentCriteria().isTrue()) {
            whereClauseList.add(" fas_str_group is null ");
        }

        if (criteria.getRequireSequenceTargetingReagentCriteria().isTrue()) {
            whereClauseList.add(" fas_str_group is not null ");
        }

        if (criteria.getExcludeTransgenicsCriteria().isTrue()) {
            whereClauseList.add(" fas_construct_group is null ");
        }

        if (criteria.getRequireTransgenicsCriteria().isTrue()) {
            whereClauseList.add(" fas_construct_group is not null ");
        }

        if (criteria.getMutationTypeCriteria().hasValues()) {
            whereClauseList.add(" exists (select 'c' from gene_feature_result_view where " +
                    "gfrv_fas_id = fas_pk_id and gfrv_affector_type_display = :mutationType) ");
        }


        StringBuilder fromClause = new StringBuilder();
        for (String clause : fromClauseList) {
            if (!(fromClause.length() == 0))
                fromClause.append(" , \n");
            fromClause.append(clause);
        }

        BtsContainsService btsService = new BtsContainsService("fas_all");
        btsService.addBtsExpandedValueList("fas_all", criteria.getGeneOrFeatureNameCriteria().getValues());
        btsService.addBtsValueList("fas_pheno_term_group", criteria.getPhenotypeAnatomyCriteria().getValues());
        String btsContainsClause = btsService.getFullClause();
        if (btsContainsClause != null)
            whereClauseList.add(btsContainsClause);

        StringBuilder whereClause = new StringBuilder();
        for (String clause : whereClauseList) {
            if (!(whereClause.length() == 0))
                whereClause.append("\n and ");
            whereClause.append(clause);
        }

        StringBuilder orderClause = new StringBuilder();
        for (SortType sortType : criteria.getSort()) {
            if (!StringUtils.isEmpty(orderClause.toString()))
                orderClause.append(" , \n");
            if (sortType.equals(SortType.LUCENE_SIMPLE))
                orderClause.append(" simple_score desc ");
            else if (sortType.equals(SortType.LUCENE_RAW))
                orderClause.append(" fas_all_score desc ");
            else if (sortType.equals(SortType.FEATURE_TYPE))
                orderClause.append(" fas_fish_significance asc ");
            else if (sortType.equals(SortType.GENE_COUNT_ASC))
                orderClause.append(" fas_gene_count asc ");
            else if (sortType.equals(SortType.GENE_COUNT_DESC))
                orderClause.append(" fas_gene_count desc ");
            else if (sortType.equals(SortType.FISH_PARTS_COUNT_ASC))
                orderClause.append(" fas_fish_parts_count asc ");
            else if (sortType.equals(SortType.FISH_PARTS_COUNT_DESC))
                orderClause.append(" fas_fish_parts_count desc ");
            else if (sortType.equals(SortType.COMPLEXITY))
                orderClause.append(" complexity asc ");
            else if (sortType.equals(SortType.GENE_A_TO_Z))
                orderClause.append(" fas_gene_order asc");
            else if (sortType.equals(SortType.GENE_Z_TO_A))
                orderClause.append(" fas_gene_order desc ");
            else if (sortType.equals(SortType.FEATURE_A_TO_Z))
                orderClause.append(" fas_affector_order asc ");
            else if (sortType.equals(SortType.FEATURE_Z_TO_A))
                orderClause.append(" fas_affector_order desc ");
            else //the default, just to make sure rows can't flop back and forth
                orderClause.append("  fas_geno_name asc ");
        }


        String sql = "\n select " + baseSelectColumns + ", \n" + sortColumns +
                "\n  from " + fromClause.toString();
        if (StringUtils.isNotEmpty(whereClause.toString()))
            sql += "\n  where " + whereClause.toString();
        sql += "\n  order by " + orderClause.toString();

        logger.debug(criteria.toString());
        logger.debug(sql);
        Query query = session.createSQLQuery(sql);
        if (criteria.getMutationTypeCriteria().hasValues())
            query.setParameter("mutationType", criteria.getMutationTypeCriteria().getValue());

        return query;
    }

    //todo: improve this, I'm sure.
    // maybe we'll eventually want something like this saved for future debugging, but html here
    // seems a little wrong..

    private String generateScoringDebugOutput(String featureComplexity, String featureCount, String faAllScore, String simpleScore,
                                              String faGeneOrder, String faFeatureOrder) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table>");

        sb.append("<tr><td><strong>Lucene Score:</strong></td><td>");
        sb.append(faAllScore);
        sb.append("</td></tr>");

        sb.append("<tr><td><strong>Exact vs Begins Score:</strong></td><td>");
        sb.append(simpleScore);
        sb.append("</td></tr>");


        sb.append("<tr><td><strong>Feature+STR Count:</strong></td><td>");
        sb.append(featureCount);
        sb.append("</td></tr>");


        sb.append("<tr><td><strong>Feature Complexity:</strong></td><td>");
        sb.append(featureComplexity);
        sb.append("</td></tr>");

        sb.append("<tr><td><strong>Gene Ordering Column:</strong></td><td>");
        sb.append(faGeneOrder);
        sb.append("</td></tr>");

        sb.append("<tr><td><strong>Feature Ordering Column:</strong></td><td>");
        sb.append(faFeatureOrder);
        sb.append("</td></tr>");

        sb.append("</table>");

        return sb.toString();
    }


    public Set<ZfinFigureEntity> getAllFigures(String fishZdbID) {
        String sql = "select phenox_fig_zdb_id,\n" +
                "        CASE\n" +
                "         WHEN img_fig_zdb_id is not null then 'true'\n" +
                "         ELSE 'false'\n" +
                "        END as hasImage\n" +
                "from phenotype_experiment\n" +
                "     join fish_experiment on phenox_genox_zdb_id = genox_zdb_id\n" +
                "     left outer join image on img_fig_zdb_id = phenox_fig_zdb_id\n" +
                "where genox_fish_zdb_id = :fishZdbID " +
                "UNION\n" +
                "select xedg_fig_zdb_id,\n" +
                "        CASE\n" +
                "         WHEN img_fig_zdb_id is not null then 'true'\n" +
                "         ELSE 'false'\n" +
                "        END as hasImage\n" +
                "from xpat_exp_details_generated\n" +
                "     join fish_experiment on xedg_genox_zdb_id = genox_zdb_id\n" +
                "     left outer join image on img_fig_zdb_id = xedg_fig_zdb_id\n" +
                "where genox_fish_zdb_id = :fishZdbID ;";
        Session session = HibernateUtil.currentSession();
        Query query = session.createSQLQuery(sql);
        query.setParameter("fishZdbID", fishZdbID);
        List<Object[]> fishObjects = query.list();
        if (fishObjects == null)
            return null;

        Set<ZfinFigureEntity> zfinFigureEntities = new HashSet<ZfinFigureEntity>(fishObjects.size());
        for (Object[] annotationObj : fishObjects) {
            ZfinFigureEntity zfinFigureEntity = new ZfinFigureEntity();
            zfinFigureEntity.setID((String) annotationObj[0]);
            zfinFigureEntity.setHasImage(Boolean.parseBoolean(((String) annotationObj[1]).trim()));
            zfinFigureEntities.add(zfinFigureEntity);
        }
        return zfinFigureEntities;
    }


    public Fish getFishByName(String name) {
        Criteria criteria = HibernateUtil.currentSession().createCriteria(Fish.class);
        criteria.add(Restrictions.eq("name",name));
        return (Fish)criteria.uniqueResult();
    }



    /**
     * Turn comma-delimited list of IDs into a list of strings.
     *
     * @param genotypeExperimentNames names
     * @return list
     */
    private List<String> getIdList(String genotypeExperimentNames) {
        if (genotypeExperimentNames == null)
            return null;
        String[] token = genotypeExperimentNames.split(",");
        List<String> idList = new ArrayList<String>(token.length);
        Collections.addAll(idList, token);
        return idList;
    }


    /**
     * retrieve all figures for given fish id
     *
     * @param fishID
     * @return set of figures
     */
    public Set<ZfinFigureEntity> getPhenotypeFigures(String fishID) {
        Session session = HibernateUtil.currentSession();
        String sqlFeatures = "select pfiggm_member_name, pfiggm_member_id, " +
                "CASE " +
                " WHEN img_fig_zdb_id is not null then 'true' " +
                " else 'false'" +
                "END as hasImage " +
                "from phenotype_figure_group_member, phenotype_figure_group, figure, OUTER image " +
                "where pfiggm_group_id = pfigg_group_pk_id " +
                "and pfigg_genox_zdb_id = :fishID " +
                "and fig_zdb_id = pfiggm_member_id " +
                "and img_fig_zdb_id = fig_zdb_id";
        Query sqlQuery = session.createSQLQuery(sqlFeatures);
        sqlQuery.setParameter("fishID", fishID);
        List<Object[]> objs = sqlQuery.list();
        if (objs == null)
            return null;

        Set<ZfinFigureEntity> figures = new HashSet<ZfinFigureEntity>(objs.size());
        if (objs.size() > 0) {
            for (Object[] groupMember : objs) {
                ZfinFigureEntity figure = new ZfinFigureEntity();
                figure.setName((String) groupMember[0]);
                figure.setID((String) groupMember[1]);
                figure.setHasImage(Boolean.parseBoolean((String) groupMember[2]));
                figures.add(figure);
            }
        }
        return figures;
    }

    /**
     * Retrieve all figures for given fish id
     * that have phenotypes associated with the termID list
     * directly or indirectly through a substructure.
     *
     * @param fishID  fish ID
     * @param termIDs term ID list
     * @return set of figures
     */
    public Set<ZfinFigureEntity> getFiguresByFishAndTerms(String fishID, List<String> termIDs) {
        if (CollectionUtils.isEmpty(termIDs)) {
            return getAllFigures(fishID);
        }


        Session session = HibernateUtil.currentSession();


        SolrServer server = SolrService.getSolrServer("prototype");
        SolrQuery query = new SolrQuery();
        //todo: drop the figure id in directly?
        query.setFields(FieldName.ID.getName(), FieldName.FIGURE_ID.getName(), FieldName.THUMBNAIL.getName());
        query.addFilterQuery(FieldName.XREF.getName() + ":\"" + fishID + "\"");

        for (String termID : termIDs) {
            Term term = RepositoryFactory.getInfrastructureRepository().getTermByID(termID);
            query.addFilterQuery(FieldName.ANATOMY_TF.getName()            + ":\"" + term.getTermName() + "\""
                      + " OR " + FieldName.BIOLOGICAL_PROCESS_TF.getName() + ":\"" + term.getTermName() + "\""
                      + " OR " + FieldName.MOLECULAR_FUNCTION_TF.getName() + ":\"" + term.getTermName() + "\""
                      + " OR " + FieldName.CELLULAR_COMPONENT_TF.getName() + ":\"" + term.getTermName() + "\""
            );
        }


        QueryResponse response = new QueryResponse();
        try {
            response = server.query(query);
        } catch (Exception e) {
            logger.error(e);
        }

        Set<ZfinFigureEntity> figureEntitySet = new HashSet<>();

        for (SolrDocument doc : response.getResults()) {
            ZfinFigureEntity figure = new ZfinFigureEntity();
            //get the figure id from the url
            String figureZdbID = (String) doc.get(FieldName.FIGURE_ID.getName());
            figure.setID(figureZdbID);
            if (CollectionUtils.isNotEmpty((Collection)doc.get(FieldName.THUMBNAIL.getName()))) {
                figure.setHasImage(true);
            } else {
                figure.setHasImage(false);
            }
            figureEntitySet.add(figure);
        }

        return figureEntitySet;


/*        BtsContainsService btsService = new BtsContainsService("ftfs_term_group");
        btsService.addBtsValueList("ftfs_term_group", termIDs);
        String sqlFeatures = "select distinct ftfs_fig_zdb_id, " +
                "CASE " +
                " WHEN img_fig_zdb_id is not null then 'true'" +
                " else 'false'" +
                "END as hasImage  " +
                "from figure_term_fish_search, fish_annotation_search, OUTER image " +
                "where img_fig_zdb_id = ftfs_fig_zdb_id " +
                "and ftfs_fas_id = fas_pk_id " +
                "and (fas_genox_group = :genoxIds AND fas_genotype_group = :genoID) AND ";
        sqlFeatures += btsService.getFullOrClause();
        Query sqlQuery = session.createSQLQuery(sqlFeatures);
        MartFish fish = FishService.getGenoGenoxByFishID(fishID);
        sqlQuery.setParameter("genoxIds", fish.getGenotypeExperimentIDsString());
        sqlQuery.setParameter("genoID", fish.getGenotype().getZdbID());
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
        return figures;*/
        //return new HashSet<ZfinFigureEntity>();
    }

    public String getGenoxMaxLength() {
        Session session = HibernateUtil.currentSession();
        String sqlFeatures = "select first 1 fas_genox_group From fish_annotation_search order by length(fas_genox_group) desc ";
        Query sqlQuery = session.createSQLQuery(sqlFeatures);
        return (String) sqlQuery.uniqueResult();
    }

    /**
     * Retrieve the Warehouse summary info for a given mart.
     *
     * @param mart mart
     * @return warehouse summary
     */
    @Override
    public WarehouseSummary getWarehouseSummary(WarehouseSummary.Mart mart) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(WarehouseSummary.class);
        criteria.add(Restrictions.eq("martName", mart.getName()));
        return (WarehouseSummary) criteria.uniqueResult();
    }

    /**
     * Retrieve the status of the fish mart:
     * true: fish mart ready for usage
     * false: fish mart is being rebuilt.
     *
     * @return status
     */
    @Override
    public ZdbFlag getFishMartStatus() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(ZdbFlag.class);
        criteria.add(Restrictions.eq("type", ZdbFlag.Type.REGEN_FISHMART_BTS_INDEXES));
        return (ZdbFlag) criteria.uniqueResult();
    }

    /**
     * retrieve all figures for given fish id
     *
     * @param fishAnnotation FishAnnotation
     * @return set of SequenceTargetingReagent entities
     */
    private List<SequenceTargetingReagent> getSequenceTargetingReagents(FishAnnotation fishAnnotation) {
        if (fishAnnotation.getSequenceTargetingReagentGroupName() == null)
            return null;
        Session session = HibernateUtil.currentSession();
        String sqlFeatures = "select distinct strgm_member_name, strgm_member_id from str_group_member, str_group " +
                "where strgm_group_id = strg_group_pk_id and strg_group_name = :strGroupName ";
        Query sqlQuery = session.createSQLQuery(sqlFeatures);
        sqlQuery.setParameter("strGroupName", fishAnnotation.getSequenceTargetingReagentGroupName());
        List<Object[]> objs = sqlQuery.list();
        if (objs == null)
            return null;

        Set<ZfinEntity> sequenceTargetingReagents = new HashSet<ZfinEntity>(objs.size());
        if (objs.size() > 0) {
            for (Object[] groupMember : objs) {
                ZfinEntity sequenceTargetingReagent = new ZfinEntity();
                sequenceTargetingReagent.setName((String) groupMember[0]);
                sequenceTargetingReagent.setID((String) groupMember[1]);
                sequenceTargetingReagents.add(sequenceTargetingReagent);
            }
        }
        List<SequenceTargetingReagent> sequenceTargetingReagentList = new ArrayList<>(sequenceTargetingReagents.size());
        for (ZfinEntity entity : sequenceTargetingReagents) {
            SequenceTargetingReagent str = RepositoryFactory.getMarkerRepository().getSequenceTargetingReagent(entity.getID());
            sequenceTargetingReagentList.add(str);
        }

        return sequenceTargetingReagentList;
    }


    private ZfinEntity getZfinEntity(String zdbID, String name) {
        ZfinEntity entity = new ZfinEntity();
        entity.setName(name);
        entity.setID(zdbID);
        return entity;
    }


}
