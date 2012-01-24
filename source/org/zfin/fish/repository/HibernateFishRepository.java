package org.zfin.fish.repository;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.zfin.database.BtsContainsService;
import org.zfin.feature.FeatureMarkerRelationship;
import org.zfin.fish.FeatureGene;
import org.zfin.fish.FishSearchCriteria;
import org.zfin.fish.FishSearchResult;
import org.zfin.fish.FishAnnotation;
import org.zfin.fish.presentation.Fish;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.framework.search.SortType;
import org.zfin.gwt.curation.dto.FeatureMarkerRelationshipTypeEnum;
import org.zfin.infrastructure.ZfinEntity;
import org.zfin.infrastructure.ZfinFigureEntity;
import org.zfin.marker.MarkerRelationship;
import org.zfin.repository.PaginationResultFactory;

import java.math.BigInteger;
import java.util.*;

/**
 * Basic repository class to handle fish searches against a database.
 */
public class HibernateFishRepository implements FishRepository {

    private static Logger logger = Logger.getLogger(HibernateFishRepository.class);

    public FishSearchResult getFish(FishSearchCriteria criteria) {

        FishSearchResult results = new FishSearchResult();
        Query query = generateFishSearchQuery(criteria);


        int start = criteria.getStart() - 1;
        logger.debug("Setting start for jdbc to: " + start);
        logger.debug("Setting rowcount for jdbc to: " + start + criteria.getRows());

        PaginationResult fishObjects = PaginationResultFactory.createResultFromScrollableResultAndClose(start, start + criteria.getRows(), query.scroll());

        if (fishObjects.getTotalCount() == 0) {
            results.setResultsFound(0);
            return results;
        }

        logger.debug("Results found: " + fishObjects.getTotalCount());
        logger.debug("fishObjects.getPopulatedResults().size(): " + fishObjects.getPopulatedResults().size());
        logger.debug("fishObjects: " + fishObjects.toString());

        List<FishAnnotation> functionalAnnotations = new ArrayList<FishAnnotation>(fishObjects.getTotalCount());
        for (Object obj : fishObjects.getPopulatedResults()) {
            FishAnnotation annotation = new FishAnnotation();
            Object[] annotationObj = (Object[]) obj;
            BigInteger bigInteger = (BigInteger) annotationObj[0];
            annotation.setID(bigInteger.longValue());
//            annotation.setGenotypeID((String) annotationObj[1]);
            annotation.setUniqueName((String) annotationObj[1]);
            annotation.setGenotypeExperimentIds((String) annotationObj[8]);
            annotation.setMorpholinoGroupName((String) annotationObj[2]);
            annotation.setFeatureGroupName((String) annotationObj[3]);
            annotation.setPhenotypeFigureCount((Integer) annotationObj[4]);
            annotation.setPhenotypeFigureGroupName((String) annotationObj[5]);
            annotation.setGeneOrFeatureText((String) annotationObj[6]);
            annotation.setName((String) annotationObj[7]);
            annotation.setGenotypeID((String) annotationObj[9]);

            String featureComplexity = ((Integer) annotationObj[10]).toString();
            String faAllScore = annotationObj[11].toString();
            String simpleScore = ((Integer) annotationObj[12]).toString();
            String faGeneOrder = "";
            String faFeatureOrder = "";
            if (annotationObj[13] != null)
                faGeneOrder = annotationObj[13].toString();
            if (annotationObj[14] != null)
                faFeatureOrder = annotationObj[14].toString();
            String featureCount = ((Integer) annotationObj[15]).toString();

            annotation.setScoringText(generateScoringDebugOutput(featureComplexity, featureCount, faAllScore, simpleScore, faGeneOrder, faFeatureOrder));
            functionalAnnotations.add(annotation);
        }

        logger.debug("functionalAnnotations size: " + functionalAnnotations.size());

        List<Fish> fish = new ArrayList<Fish>(fishObjects.getTotalCount());
        for (FishAnnotation annotation : functionalAnnotations) {
            fish.add(getFishFromFunctionalAnnotation(annotation, criteria));
        }

        logger.debug("Fish size: " + fish.size());
        logger.debug("FishObjects total count: " + fishObjects.getTotalCount());

        results.setResults(fish);
        results.setResultsFound(fishObjects.getTotalCount());
        results.setStart(criteria.getStart());

        return results;
    }

    private Query generateFishSearchQuery(FishSearchCriteria criteria) {
        Session session = HibernateUtil.currentSession();

        List<String> whereClauseList = new ArrayList<String>();
        List<String> fromClauseList = new ArrayList<String>();

        String baseSelectColumns = " fas_pk_id, fas_geno_name, \n" +
                " fas_morpholino_group, fas_feature_group, fas_pheno_figure_count, fas_pheno_figure_group, \n" +
                " fas_all, fas_geno_name, fas_genox_group, fas_genotype_group ";

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


        if (criteria.getExcludeMorphantsCriteria().isTrue()) {
            whereClauseList.add(" fas_morpholino_group is null ");
        }

        if (criteria.getRequireMorphantsCriteria().isTrue()) {
            whereClauseList.add(" fas_morpholino_group is not null ");
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


        sb.append("<tr><td><strong>Feature+Mo Count:</strong></td><td>");
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

    private void addAllFigures(Fish fish) {
        Set<ZfinFigureEntity> figures = getAllFigures(fish.getFishID());
        setImageAttributeOnFish(fish, figures);
    }

    private void addFiguresByTermValues(Fish fish, List<String> values) {
        Set<ZfinFigureEntity> figures = getFiguresByFishAndTerms(fish.getFishID(), values);
        setImageAttributeOnFish(fish, figures);
    }

    private void setImageAttributeOnFish(Fish fish, Set<ZfinFigureEntity> figures) {
        if (figures == null || figures.size() == 0)
            return;
        fish.setPhenotypeFigures(figures);
        for (ZfinFigureEntity figure : figures)
            if (figure.isHasImage())
                fish.setImageAvailable(true);
    }

    /**
     * Retrieve figures for a given fish.
     *
     * @param fishID fish ID
     * @return set of figures.
     */
    public Set<ZfinFigureEntity> getAllFigures(String fishID) {
        String sql = "select distinct ftfs_fig_zdb_id, " +
                "CASE " +
                " WHEN img_fig_zdb_id is not null then 'true'" +
                " ELSE 'false'" +
                "END as hasImage" +
                " from figure_term_fish_search, fish_annotation_search , OUTER image " +
                "where img_fig_zdb_id = ftfs_fig_zdb_id " +
                "and (fas_genox_group = :genoxIds AND fas_genotype_group = :genoID)" +
                "and ftfs_fas_id = fas_pk_id ";
        Session session = HibernateUtil.currentSession();
        Query query = session.createSQLQuery(sql);
        query.setParameter("genoxIds", FishService.getGenotypeExperimentIDsString(fishID));
        query.setParameter("genoID", FishService.getGenotypeID(fishID));
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

    /**
     * Retrieve fish by primary key
     *
     * @param fishID ID
     * @return fish
     */
    public Fish getFish(Long fishID) {
        Session session = HibernateUtil.currentSession();
        FishAnnotation annotation = (FishAnnotation) session.load(FishAnnotation.class, fishID);
        Fish fish = getFishFromFunctionalAnnotation(annotation, null);
        return fish;
    }

    /**
     * Retrieve fish by genotype experiment ids
     * or by genotype IDs
     *
     * @param genoGenoxIDs IDs
     * @return fish
     */
    public Fish getFish(String genoGenoxIDs) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(FishAnnotation.class);
        Fish minimalFish = FishService.getGenoGenoxByFishID(genoGenoxIDs);
        if (StringUtils.isEmpty(minimalFish.getGenotypeExperimentIDsString())) {
            criteria.add(Restrictions.and(
                    Restrictions.isNull("genotypeExperimentIds"),
                    Restrictions.eq("genotypeID", genoGenoxIDs)
            ));
            criteria.add(Restrictions.eq("genotypeID", genoGenoxIDs));
        } else if (minimalFish.getGenotype() == null) {
            criteria.add(Restrictions.and(
                    Restrictions.isNull("genotypeID"),
                    Restrictions.eq("genotypeExperimentIds", genoGenoxIDs)
            ));
        } else
            criteria.add(Restrictions.and(
                    Restrictions.eq("genotypeExperimentIds", minimalFish.getGenotypeExperimentIDsString()),
                    Restrictions.eq("genotypeID", minimalFish.getGenotype().getID())
            ));
        FishAnnotation annotation = (FishAnnotation) criteria.uniqueResult();
        if (annotation == null)
            return null;
        Fish fish = getFishFromFunctionalAnnotation(annotation, null);
        return fish;
    }

    private Fish getFishFromFunctionalAnnotation(FishAnnotation annotation, FishSearchCriteria criteria) {
        Fish singleFish = new Fish();
        singleFish.setID(String.valueOf(annotation.getID()));
        singleFish.setGenotype(getZfinEntity(annotation.getGenotypeID(), annotation.getGenotypeID()));
        singleFish.setName(annotation.getName());
        singleFish.setGenotypeExperimentIDs(getIdList(annotation.getGenotypeExperimentIds()));
        singleFish.setGenotypeExperimentIDsString(annotation.getGenotypeExperimentIds());
        populateGeneFeatureConstruct(singleFish, annotation);
        addFigures(singleFish, criteria);
        singleFish.setGeneOrFeatureText(annotation.getGeneOrFeatureText());
        singleFish.setScoringText(annotation.getScoringText());
        //
        singleFish.setMorpholinos(getMorpholinos(annotation));
        return singleFish;
    }

    private void addFigures(Fish fish, FishSearchCriteria criteria) {
        if (criteria == null)
            addAllFigures(fish);
        else {
            List<String> values = criteria.getPhenotypeAnatomyCriteria().getValues();
            addFiguresByTermValues(fish, values);
        }
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

    private void populateGeneFeatureConstruct(Fish singleFish, FishAnnotation annotation) {
        String hql = "from FeatureGene " +
                "where fishAnnotation = :fishAnnotation " +
                "order by gene.nameOrder, feature.nameOrder";
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery(hql);
        query.setParameter("fishAnnotation", annotation);
        List<FeatureGene> genotypeFeatures = query.list();
        Collections.sort(genotypeFeatures, new Comparator<FeatureGene>() {
            public int compare(FeatureGene o1, FeatureGene o2) {
                if (o1.getGene() == null && o2.getGene() == null)
                    return o1.getFeature().getNameOrder().compareTo(o2.getFeature().getNameOrder());
                if (o1.getGene() != null && o2.getGene() == null)
                    return -1;
                if (o1.getGene() == null && o2.getGene() != null)
                    return 1;
                return o1.getGene().getNameOrder().compareTo(o2.getGene().getNameOrder());
            }
        });
        for (FeatureGene geneFeature : genotypeFeatures) {
            singleFish.addFeatureGene(geneFeature);
        }
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
        if (termIDs == null)
            return getAllFigures(fishID);

        Session session = HibernateUtil.currentSession();
        BtsContainsService btsService = new BtsContainsService("ftfs_term_group");
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
        Fish fish = FishService.getGenoGenoxByFishID(fishID);
        sqlQuery.setParameter("genoxIds", fish.getGenotypeExperimentIDsString());
        sqlQuery.setParameter("genoID", fish.getGenotype().getID());
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

    public String getGenoxMaxLength() {
        Session session = HibernateUtil.currentSession();
        String sqlFeatures = "select first 1 fas_genox_group From fish_annotation_search order by length(fas_genox_group) desc ";
        Query sqlQuery = session.createSQLQuery(sqlFeatures);
        return (String) sqlQuery.uniqueResult();
    }

    /**
     * retrieve all figures for given fish id
     *
     * @param fishAnnotation FishAnnotation
     * @return set of morpholino entities
     */
    public List<ZfinEntity> getMorpholinos(FishAnnotation fishAnnotation) {
        if (fishAnnotation.getMorpholinoGroupName() == null)
            return null;
        Session session = HibernateUtil.currentSession();
        String sqlFeatures = "select distinct morphgm_member_name, morphgm_member_id from morpholino_group_member, morpholino_group " +
                "where morphgm_group_id = morphg_group_pk_id and morphg_group_name = :morphoIds ";
        Query sqlQuery = session.createSQLQuery(sqlFeatures);
        sqlQuery.setParameter("morphoIds", fishAnnotation.getMorpholinoGroupName());
        List<Object[]> objs = sqlQuery.list();
        if (objs == null)
            return null;

        Set<ZfinEntity> morpholinos = new HashSet<ZfinEntity>(objs.size());
        if (objs.size() > 0) {
            for (Object[] groupMember : objs) {
                ZfinEntity morpholino = new ZfinEntity();
                morpholino.setName((String) groupMember[0]);
                morpholino.setID((String) groupMember[1]);
                morpholinos.add(morpholino);
            }
        }
        List<ZfinEntity> morpholinoList = new ArrayList<ZfinEntity>(morpholinos.size());
        morpholinoList.addAll(morpholinos);
        return morpholinoList;
    }

    private List<FeatureGene> getTransgenicFeature(String featureID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select featureRelation from FeatureMarkerRelationship as featureRelation where " +
                " featureRelation.feature.zdbID = :featureID and featureRelation.featureMarkerRelationshipType = :type";
        Query query = session.createQuery(hql);
        query.setParameter("featureID", featureID);
        query.setString("type", FeatureMarkerRelationshipTypeEnum.CONTAINS_INNOCUOUS_SEQUENCE_FEATURE.toString());
        List<FeatureMarkerRelationship> relationships = query.list();
        if (relationships == null)
            return null;

        List<FeatureGene> featureGenes = new ArrayList<FeatureGene>();
        for (FeatureMarkerRelationship relation : relationships) {
            FeatureGene featureGene = new FeatureGene();
            ZfinEntity feature = getZfinEntity(relation.getFeature().getZdbID(), relation.getFeature().getAbbreviation());
            featureGene.setType(relation.getFeature().getType().getDisplay());
            featureGene.setFeature(feature);
            ZfinEntity construct = getZfinEntity(relation.getMarker().getZdbID(), relation.getMarker().getAbbreviation());
            featureGene.setConstruct(construct);
            featureGenes.add(featureGene);
        }
        return featureGenes;
    }

    private List<FeatureGene> getFeatureGene(String featureID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select featureRelation from FeatureMarkerRelationship as featureRelation where " +
                " featureRelation.feature.zdbID = :featureID and featureRelation.featureMarkerRelationshipType = :type";
        Query query = session.createQuery(hql);
        query.setParameter("featureID", featureID);
        query.setString("type", FeatureMarkerRelationshipTypeEnum.IS_ALLELE_OF.toString());
        List<FeatureMarkerRelationship> relationships = query.list();
        if (relationships == null)
            return null;

        List<FeatureGene> featureGenes = new ArrayList<FeatureGene>();
        for (FeatureMarkerRelationship relation : relationships) {
            FeatureGene featureGene = new FeatureGene();
            ZfinEntity feature = getZfinEntity(relation.getFeature().getZdbID(), relation.getFeature().getAbbreviation());
            featureGene.setType(relation.getFeature().getType().getDisplay());
            ZfinEntity gene = getZfinEntity(relation.getMarker().getZdbID(), relation.getMarker().getAbbreviation());
            featureGene.setFeature(feature);
            featureGene.setGene(gene);
            featureGenes.add(featureGene);
        }
        return featureGenes;
    }

    private List<FeatureGene> getMorpholinoGenes(String morpholinoID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select relation from MarkerRelationship as relation where " +
                " relation.firstMarker.zdbID = :featureID and relation.markerRelationshipType = :type";
        Query query = session.createQuery(hql);
        query.setParameter("featureID", morpholinoID);
        query.setString("type", MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE.toString());
        List<MarkerRelationship> relationships = query.list();
        if (relationships == null)
            return null;

        List<FeatureGene> featureGenes = new ArrayList<FeatureGene>();
        for (MarkerRelationship relation : relationships) {
            FeatureGene featureGene = new FeatureGene();
            ZfinEntity morpholino = getZfinEntity(relation.getFirstMarker().getZdbID(), relation.getFirstMarker().getAbbreviation());
            featureGene.setType("Morpholino");
            ZfinEntity gene = getZfinEntity(relation.getSecondMarker().getZdbID(), relation.getSecondMarker().getAbbreviation());
            featureGene.setFeature(morpholino);
            featureGene.setGene(gene);
            featureGenes.add(featureGene);
        }
        return featureGenes;
    }

    private ZfinEntity getZfinEntity(String zdbID, String name) {
        ZfinEntity entity = new ZfinEntity();
        entity.setName(name);
        entity.setID(zdbID);
        return entity;
    }


}
