package org.zfin.anatomy.repository;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.zfin.anatomy.*;
import org.zfin.expression.ExpressionStructure;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.DataAliasGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation that provides access to the database storage layer.
 * Methods that allow to retrieve, save and update objects that pertain
 * to the Anatomy domain.
 */
public class HibernateAnatomyRepository implements AnatomyRepository {

    private static Logger LOG = Logger.getLogger(HibernateAnatomyRepository.class);

    // Cached variables
    // The stages do not change very often, only through an import
    private List<DevelopmentStage> allStagesWithoutUnknown;
    private DevelopmentStage unknown;
    private List<java.lang.String> itemAndSynonymNames;
    // The relationship types do not change often
    private List<String> relationshipTypes;
    private List<AnatomyRelationshipTypePersistence> relationshipTypesPersist;


    /**
     * Retrieve all developmental stages.
     * Note that this is cached the first time it is read from the database.
     * Make sure to call the invalidateChachedObjects() if you upload a new AO
     * and this list becomes invalid.
     *
     * @return a list of DevelopmentStage objects
     */
    @SuppressWarnings("unchecked")
    public List<DevelopmentStage> getAllStages() {

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DevelopmentStage.class);
        criteria.addOrder(Order.asc("hoursStart"));
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<DevelopmentStage> getAllStagesWithoutUnknown() {
        if (allStagesWithoutUnknown != null) {
            return allStagesWithoutUnknown;
        }

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DevelopmentStage.class);
        criteria.addOrder(Order.asc("hoursStart"));
        criteria.add(Restrictions.ne("name", DevelopmentStage.UNKNOWN));
        allStagesWithoutUnknown = criteria.list();

        return allStagesWithoutUnknown;
    }

    @SuppressWarnings("unchecked")
    public List<AnatomyItem> getAllAnatomyItems() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(AnatomyItem.class);
        List<AnatomyItem> items = criteria.list();
        if (items != null) {
            LOG.debug(items);
        }
        return items;
    }

    /**
     * Retrieve a list of anatomy terms that match a search string.
     * Matching is done via
     * 1) 'contains'
     * 2) searching all synonyms as 'contains'
     * 3) Case insensitive in both cases
     *
     * @param searchString     string
     * @param includeObsoletes include obsolete terms
     * @return list of anatomy terms
     */
    @SuppressWarnings("unchecked")
    public List<AnatomyItem> getAnatomyItemsByName(String searchString, boolean includeObsoletes) {
        Session session = HibernateUtil.currentSession();

        String hql = "select term from AnatomyItem term  " +
                "       left join fetch term.synonyms " +
                "where  " +
                "   (term.lowerCaseName like :name or exists (from AnatomySynonym syn where syn.item = term " +
                "                                           and syn.aliasLowerCase like :name and syn.aliasGroup.name <> :group))" +
                " AND term.obsolete = :obsolete " +
                " order by term.termName";


        Query query = session.createQuery(hql);
        query.setString("name", "%" + searchString.toLowerCase() + "%");
        query.setBoolean("obsolete", includeObsoletes);
        query.setString("group", DataAliasGroup.Group.SECONDARY_ID.toString());
        query.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
        List<AnatomyItem> items = query.list();

        if (items != null) {
            LOG.debug(items);
        }
        return items;
    }

    @SuppressWarnings("unchecked")
    public List<AnatomyStatistics> getAllAnatomyItemStatistics() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(AnatomyStatistics.class);
        criteria.add(Restrictions.eq("type", AnatomyStatistics.Type.GENE));
        //ToDO: find a way to sort by the joint table anatomyItem.name
/*
        criteria.addOrder(Order.asc("orderNames"));
*/
        List<AnatomyStatistics> items = criteria.list();

        if (items != null) {
            LOG.debug(items);
        }
        return items;
    }

    /**
     * Retrieve statistics for all anatomy terms that match the
     * search term name. The search is case-insensitive.
     *
     * @param searchTerm search term string
     * @return list of AnatomyStatistics object. Null if no term was found or the search
     *         term is null.
     */
    @SuppressWarnings("unchecked")
    public List<AnatomyStatistics> getAnatomyItemStatistics(String searchTerm) {
        if (searchTerm == null)
            return null;

        Session session = HibernateUtil.currentSession();
        String hql = "select stats from AnatomyStatistics stats join stats.term as term, AnatomyItem anatItem " +
                "where  " +
                "   (anatItem.lowerCaseName like :name or exists (from AnatomySynonym syn where anatItem = syn.item " +
                "                                           and syn.aliasLowerCase like :name )) " +
                "   AND stats.type = :type " +
                "   AND term.oboID = anatItem.oboID " +
                "order by anatItem.termName";
        Query query = session.createQuery(hql);
        query.setString("name", "%" + searchTerm.toLowerCase() + "%");
        query.setParameter("type", AnatomyStatistics.Type.GENE);
//        query.setParameter("group", DataAlias.Group.ALIAS);

        // TODO: Temporarily until we obsolete the anatomy_item table
        List<AnatomyStatistics> items = query.list();
        for (AnatomyStatistics stat : items) {
            AnatomyItem item = getAnatomyTermByOboID(stat.getTerm().getOboID());
            stat.setAnatomyItem(item);
        }

        if (items != null) {
            LOG.debug(items);
        }
        return items;
    }

    public AnatomyItem loadAnatomyItem(AnatomyItem anatomyItem) {
        Session session = HibernateUtil.currentSession();
        return (AnatomyItem) session.load(AnatomyItem.class, anatomyItem.getZdbID());
    }

    public AnatomyItem getAnatomyTermByID(String aoZdbID) {
        Session session = HibernateUtil.currentSession();
        return (AnatomyItem) session.get(AnatomyItem.class, aoZdbID);
    }

    /**
     * To retrieve the relationship of an anatomical item we need to search two
     * columns in the table anatomy_relationship: anatrel_anatitem_1_zdb_id and anatrel_anatitem_2_zdb_id.
     * This then joins with the relationship types.
     *
     * @param anatomyItem Anatomy Term
     * @return list of AnatomyRelationship objects
     */
    @SuppressWarnings("unchecked")
    public List<AnatomyRelationship> getAnatomyRelationships(AnatomyItem anatomyItem) {
        List<AnatomyRelationship> allRelationships = new ArrayList<AnatomyRelationship>();
        Session session = HibernateUtil.currentSession();
        String hqlOne = "select rel from AnatomyRelationshipOne rel, AnatomyItem term " +
                " where rel.anatomyItemOne = term AND" +
                "       term = :aoTerm  " +
                "       order by rel.anatomyItemTwo.nameOrder asc ";
        Query queryOne = session.createQuery(hqlOne);
        queryOne.setParameter("aoTerm", anatomyItem);
        List<AnatomyRelationship> list = queryOne.list();
        allRelationships.addAll(list);

        String hqlTwo = "select rel from AnatomyRelationshipTwo rel, AnatomyItem term " +
                " where rel.anatomyItemTwo = term AND" +
                "       term = :aoTerm  " +
                "       order by rel.anatomyItemOne.nameOrder asc ";
        Query queryTwo = session.createQuery(hqlTwo);
        queryTwo.setParameter("aoTerm", anatomyItem);
        List<AnatomyRelationship> list2 = queryTwo.list();
        allRelationships.addAll(list2);

        return allRelationships;
    }

    @SuppressWarnings("unchecked")
    private List<AnatomyRelationshipTypePersistence> getAllAnatomyRelationshipTypesPersist() {
        if (relationshipTypesPersist != null) {
            return relationshipTypesPersist;
        }

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(AnatomyRelationshipTypePersistence.class);
        relationshipTypesPersist = criteria.list();
        return relationshipTypesPersist;
    }

    /**
     * Retrieve all relationship types (small number) for anatomical items.
     * This list is cached as it is just a small list and does not change frequently.
     * Make sure to call the invalidateChachedObjects() if you upload a new AO.
     *
     * @return AnatomyRelationshipType
     */

    public List<String> getAllAnatomyRelationshipTypes() {
        if (relationshipTypes != null) {
            return relationshipTypes;
        }
        relationshipTypes = new ArrayList<String>();

        List<AnatomyRelationshipTypePersistence> types = getAllAnatomyRelationshipTypesPersist();

        if (types != null) {
            for (AnatomyRelationshipTypePersistence typePersist : types) {
                relationshipTypes.add(typePersist.getRelationOneToTwo());
                relationshipTypes.add(typePersist.getRelationTwoToOne());
            }
            LOG.debug(relationshipTypes);
        }
        return relationshipTypes;
    }

    /**
     * Load the stage identified by its identifier.
     * First, it tries to find by stageID if available (fastest wasy since it
     * is the primary key). Second, it tries to find the stage by zdbID if
     * available. Lastly, it tries to search by name if available. Each of them needs to
     * find a unique record otherwise a Runtime exception is being thrown.
     *
     * @param stage Stage
     */
    public DevelopmentStage getStage(DevelopmentStage stage) {
        if (stage == null || stage.getZdbID() == null)
            return null;

        Session session = HibernateUtil.currentSession();
        long stageID = stage.getStageID();
        if (stageID != 0) {
            DevelopmentStage stageRet = (DevelopmentStage) session.load(DevelopmentStage.class, stageID);
            return stageRet;
        }
        java.lang.String zdbID = stage.getZdbID();
        if (!StringUtils.isEmpty(zdbID)) {
            Criteria criteria = session.createCriteria(DevelopmentStage.class);
            criteria.add(Restrictions.eq("zdbID", zdbID));
            DevelopmentStage stageRet = (DevelopmentStage) criteria.uniqueResult();
            return stageRet;
        }
        java.lang.String oboID = stage.getOboID();
        if (!StringUtils.isEmpty(oboID)) {
            Criteria criteria = session.createCriteria(DevelopmentStage.class);
            criteria.add(Restrictions.eq("oboID", oboID));
            DevelopmentStage stageRet = (DevelopmentStage) criteria.uniqueResult();
            return stageRet;
        }
        java.lang.String name = stage.getName();
        if (StringUtils.isEmpty(name))
            throw new RuntimeException("No Valid identifier found (stageID, zdbID or name)");
        Criteria criteria = session.createCriteria(DevelopmentStage.class);
        criteria.add(Restrictions.eq("name", name));
        DevelopmentStage stageRet = (DevelopmentStage) criteria.uniqueResult();
        return stageRet;
    }

    public DevelopmentStage getStageByID(String stageID) {
        Session session = HibernateUtil.currentSession();
        return (DevelopmentStage) session.get(DevelopmentStage.class, stageID);
    }

    public DevelopmentStage getStageByName(java.lang.String stageName) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DevelopmentStage.class);
        criteria.add(Restrictions.eq("name", stageName));
        DevelopmentStage stage = (DevelopmentStage) criteria.uniqueResult();
        return stage;
    }

    /**
     * Precondition for a method that takes a development stage object.
     *
     * @param stage Stage
     */
    private void validateStage(DevelopmentStage stage) {
        if (stage == null) {
            throw new RuntimeException("No stage object found.");
        }
        String zdbID = stage.getZdbID();
        if (StringUtils.isEmpty(zdbID)) {
            throw new RuntimeException("No valid zdbID on stage object found.");
        }
    }

    @SuppressWarnings("unchecked")
    public List<AnatomyStatistics> getAnatomyItemStatisticsByStage(DevelopmentStage stage) {
        java.lang.String zdbID = stage.getZdbID();

        Session session = HibernateUtil.currentSession();
        String hql = "SELECT stats, info FROM AnatomyStatistics stats, AnatomyTreeInfo info, AnatomyItem aoTerm   " +
                "WHERE stats.term.oboID = aoTerm.oboID " +
                "AND  aoTerm.zdbID = info.item.zdbID " +
                "AND info.zdbID = :zdbID " +
                "AND stats.term.obsolete != :obsolete " +
                "AND stats.term.termName != :aoName " +
                "AND stats.type = :type " +
                "ORDER BY info.sequenceNumber ";
        Query query = session.createQuery(hql);
        query.setString("zdbID", zdbID);
        query.setString("aoName", "unspecified");
        query.setBoolean("obsolete", true);
        query.setParameter("type", AnatomyStatistics.Type.GENE);
        List<Object[]> objects = query.list();
        List<AnatomyStatistics> items = createStatisticsWithTreeInfo(objects);

        if (items != null) {
            LOG.debug(items);
        }
        return items;
    }

    public void insertDevelopmentStage(DevelopmentStage stage) {
        Session session = HibernateUtil.currentSession();
        session.save(stage);
    }

    public void insertAnatomyItem(AnatomyItem item) {
        Session session = HibernateUtil.currentSession();
        session.save(item);
    }

    public AnatomyStatistics getAnatomyStatistics(String anatomyZdbID) {
        Session session = HibernateUtil.currentSession();
        String hql = "FROM AnatomyStatistics stat " +
                "WHERE stat.zdbID = :zdbID " +
                "and stat.type = :type ";
        Query query = session.createQuery(hql);
        query.setParameter("zdbID", anatomyZdbID);
        query.setParameter("type", AnatomyStatistics.Type.GENE);
        AnatomyStatistics stat = (AnatomyStatistics) query.uniqueResult();
        return stat;
    }

    public AnatomyStatistics getAnatomyStatisticsForMutants(String anatomyZdbID) {
        Session session = HibernateUtil.currentSession();
        String hql = "from AnatomyStatistics stat " +
                "where stat.zdbID = :zdbID " +
                "      AND stat.type = :type ";
        Query query = session.createQuery(hql);
        query.setParameter("zdbID", anatomyZdbID);
        query.setParameter("type", AnatomyStatistics.Type.GENO);
        AnatomyStatistics stat = (AnatomyStatistics) query.uniqueResult();
        return stat;
    }

    /**
     * Retrieve an anatomy term for a given name.
     * The lookup is case-insensitive.
     * Returns null if no term is found or the search name is null
     *
     * @param name ao term name
     * @return AnatomyItem
     */
    public AnatomyItem getAnatomyItem(String name) {
        if (name == null)
            return null;
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(AnatomyItem.class);
        criteria.add(Restrictions.eq("lowerCaseName", name.toLowerCase()));
        return (AnatomyItem) criteria.uniqueResult();
    }

    /**
     * Retrieve an anatomy term for a given synonym name
     * The lookup is case-insensitive.
     * Returns null if no term is found or the search name is null
     *
     * @param name ao synonym name
     * @return AnatomyItem
     */
    @SuppressWarnings("unchecked")
    public List<AnatomySynonym> getAnatomyTermsBySynonymName(String name) {
        if (name == null)
            return null;
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(AnatomySynonym.class);
        criteria.add(Restrictions.eq("aliasLowerCase", name.toLowerCase()));
        return (List<AnatomySynonym>) criteria.list();
    }

    public boolean isSubstructureOf(AnatomyItem term, AnatomyItem rootTerm) {
        Session session = HibernateUtil.currentSession();
        String hql = "select 1 from AnatomyChildren root where root.child = :child AND root.root = :parent ";
        Query query = session.createQuery(hql);
        query.setParameter("child", term);
        query.setParameter("parent", rootTerm);
        Object result = query.uniqueResult();
        return result != null;
    }

    @SuppressWarnings("unchecked")
    public List<String> getAnatomyTermsForAutoComplete() {
        Session session = HibernateUtil.currentSession();

        String hql = "select term.termName FROM AnatomyItem term ";
        Query query = session.createQuery(hql);
        List<String> terms = null;
        terms = (List<String>) query.list();
        return terms;

    }

    /**
     * Retrieve a list of terms that develops_from the given term and are defined in the
     * stage range given.
     * In other words, the given structure develops_into the list of terms retrieved.
     * E.g.
     * 'adaxial cell' develops_into 'migratory slow muscle precursor cell'
     *
     * @param termID     Term id
     * @param startHours start
     * @param endHours   end
     * @return list of anatomy terms
     */
    @SuppressWarnings("unchecked")
    public List<AnatomyItem> getTermsDevelopingFromWithOverlap(String termID, double startHours, double endHours) {
        Session session = HibernateUtil.currentSession();

        String hql = "from AnatomyRelationshipOne term where " +
                "          term.anatomyItemOne.zdbID = :termID AND" +
                "          term.type.typeID = :type AND " +
                "          ((term.anatomyItemTwo.start.hoursStart > :start AND term.anatomyItemTwo.start.hoursStart < :end)" +
                "        OR (term.anatomyItemTwo.end.hoursEnd > :start AND term.anatomyItemTwo.end.hoursEnd < :end) " +
                "        OR (term.anatomyItemTwo.start.hoursStart < :start AND term.anatomyItemTwo.end.hoursEnd > :end))" +
                "        order by term.anatomyItemTwo.nameOrder ";
        Query query = session.createQuery(hql);
        query.setParameter("termID", termID);
        query.setDouble("start", startHours);
        query.setDouble("end", endHours);
        query.setParameter("type", AnatomyRelationshipTypePersistence.Type.DEVELOPS_FROM.toString());
        List<AnatomyRelationshipOne> ones = (List<AnatomyRelationshipOne>) query.list();
        if (ones == null)
            return null;
        List<AnatomyItem> terms = new ArrayList<AnatomyItem>();
        for (AnatomyRelationshipOne one : ones) {
            terms.add(one.getAnatomyItemTwo());
        }
        return terms;
    }

    /**
     * Retrieve a list of terms that develops_into the given term and are defined in the
     * stage range given.
     * In other words, the given structure develops_from the list of terms retrieved.
     * E.g.
     * 'slow muscle' develops_from 'migratory slow muscle precursor cell', 'myotome, 'slow muscle myoblast'
     *
     * @param termID     Term id
     * @param startHours start
     * @param endHours   end
     * @return list of anatomy terms
     */
    @SuppressWarnings("unchecked")
    public List<AnatomyItem> getTermsDevelopingIntoWithOverlap(String termID, double startHours, double endHours) {
        Session session = HibernateUtil.currentSession();

        String hql = "from AnatomyRelationshipOne term where " +
                "          term.anatomyItemTwo.zdbID = :termID AND" +
                "          term.type.typeID = :type AND " +
                "          ((term.anatomyItemOne.start.hoursStart >= :start AND term.anatomyItemOne.start.hoursStart < :end)" +
                "        OR (term.anatomyItemOne.end.hoursEnd >= :start AND term.anatomyItemOne.end.hoursEnd < :end) " +
                "        OR (term.anatomyItemOne.start.hoursStart <= :start AND term.anatomyItemOne.end.hoursEnd > :end))" +
                "        order by term.anatomyItemOne.nameOrder ";
        Query query = session.createQuery(hql);
        query.setParameter("termID", termID);
        query.setDouble("start", startHours);
        query.setDouble("end", endHours);
        query.setParameter("type", AnatomyRelationshipTypePersistence.Type.DEVELOPS_FROM.toString());
        List<AnatomyRelationshipOne> ones = (List<AnatomyRelationshipOne>) query.list();
        if (ones == null)
            return null;
        List<AnatomyItem> terms = new ArrayList<AnatomyItem>();
        for (AnatomyRelationshipOne one : ones) {
            terms.add(one.getAnatomyItemOne());
        }
        return terms;
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
     * Retrieve an anatomical structure by OBO id;
     *
     * @param termID obo id
     * @return anatomical structure
     */
    @SuppressWarnings("unchecked")
    public AnatomyItem getAnatomyTermByOboID(String termID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(AnatomyItem.class);
        criteria.add(Restrictions.eq("oboID", termID));
        return (AnatomyItem) criteria.uniqueResult();
    }

    /**
     * Retrieve the start stage for a given anatomy term, identified by its obo id.
     *
     * @param oboID obo id
     * @return stage
     */
    public DevelopmentStage getStartStage(String oboID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select stage from DevelopmentStage stage, AnatomyItem term " +
                "     where term.start = stage AND term.oboID = :oboID";

        Query query = session.createQuery(hql);
        query.setParameter("oboID", oboID);
        return (DevelopmentStage) query.uniqueResult();
    }

    /**
     * Retrieve the end stage for a given anatomy term, identified by its obo id.
     *
     * @param oboID obo id
     * @return stage
     */
    public DevelopmentStage getEndStage(String oboID) {
        Session session = HibernateUtil.currentSession();
        String hql = "select stage from DevelopmentStage stage, AnatomyItem term " +
                "     where term.end = stage AND term.oboID = :oboID";

        Query query = session.createQuery(hql);
        query.setParameter("oboID", oboID);
        return (DevelopmentStage) query.uniqueResult();
    }

    /*
     * ToDO: Convenience method as long as anatomy_display contains multiple records for a single stage.
     */

    private List<AnatomyStatistics> createStatisticsWithTreeInfo(List<Object[]> objects) {
        if (objects == null) {
            return null;
        }
        List<AnatomyStatistics> stats = new ArrayList();
        for (Object[] object : objects) {
            AnatomyStatistics stat = (AnatomyStatistics) object[0];
            AnatomyTreeInfo treeInfo = (AnatomyTreeInfo) object[1];
            stat.setTreeInfo(treeInfo);
            stats.add(stat);
            AnatomyItem item = getAnatomyTermByOboID(stat.getTerm().getOboID());
            stat.setAnatomyItem(item);
        }
        return stats;
    }

    /**
     * Dereference chached variables to force a database re-read. This is need in case an import of AO terms
     * invalidates the data.
     */
    public void invalidateCachedObjects() {
        relationshipTypes = null;
        allStagesWithoutUnknown = null;
        unknown = null;
    }

}
