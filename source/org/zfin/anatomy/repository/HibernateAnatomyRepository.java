package org.zfin.anatomy.repository;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.zfin.anatomy.*;
import org.zfin.framework.HibernateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is an implementaion that provides access to the database storage layer.
 * Methods that allow to retrieve, save and update objects that pertain
 * to the Anatomy domain.
 */
public class HibernateAnatomyRepository implements AnatomyRepository {

    private static Logger LOG = Logger.getLogger(HibernateAnatomyRepository.class);

    // Chached variables
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
    public List<DevelopmentStage> getAllStages() {

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DevelopmentStage.class);
        criteria.addOrder(Order.asc("hoursStart"));
        return criteria.list();
    }

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
     * Since hibernate does not support UNION yet, we need to go out and grab the
     * anatomy item names and the synonym names in two queries and sort it. This list is cached.
     *
     * @return String
     */
    public List<String> getAllAnatomyNamesAndSynonyms() {
        if (itemAndSynonymNames != null)
            return itemAndSynonymNames;

        Session session = HibernateUtil.currentSession();
        String hql = "select name from AnatomyItem ";
        Query query = session.createQuery(hql);
        itemAndSynonymNames = query.list();

        String synsHql = "select name from AnatomySynonym where item.zdbID like 'ZDB-ANAT-%' ";
        Query querySyn = session.createQuery(synsHql);
        List<String> syns = querySyn.list();
        itemAndSynonymNames.addAll(syns);
        Collections.sort(itemAndSynonymNames);
        return itemAndSynonymNames;
    }

    /**
     * Retrieve a list of anatomy terms that match a search string.
     * Matching is done via
     * 1) 'contains'
     * 2) searching all synonyms as 'contains'
     * 3) Case insensitive in both cases
     *
     * @param searchString string
     * @return list of anatomy terms
     */
    public List<AnatomyItem> getAnatomyItemsByName(String searchString) {
        Session session = HibernateUtil.currentSession();
        String hql = "select term from AnatomyItem term  " +
                "where  " +
                "   (upper(term.name) like :name or exists (from AnatomySynonym syn where syn.item = term " +
                "                                           and upper(syn.name) like :name )) " +
                "order by term.name";
        Query query = session.createQuery(hql);
        query.setString("name", "%" + searchString.toUpperCase() + "%");
        List<AnatomyItem> items = query.list();

        if (items != null) {
            LOG.debug(items);
        }
        return items;
    }

    public List<AnatomyStatistics> getAllAnatomyItemStatistics() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(AnatomyStatistics.class);
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

    public List<AnatomyStatistics> getAnatomyItemStatistics(String searchTerm) {
        Session session = HibernateUtil.currentSession();
        String hql = "select stats from AnatomyStatistics stats join stats.anatomyItem as anatItem " +
                "where  " +
                "   (anatItem.name like :name or exists (from AnatomySynonym syn where anatItem = syn.item " +
                "                                           and syn.name like :name )) " +
                "   AND stats.type = :type " +
                "order by anatItem.name";
        Query query = session.createQuery(hql);
        query.setString("name", "%" + searchTerm + "%");
        query.setParameter("type", AnatomyStatistics.Type.GENE);
//        query.setParameter("group", DataAlias.Group.ALIAS);

        List<AnatomyStatistics> items = query.list();

        if (items != null) {
            LOG.debug(items);
        }
        return items;
    }

    public AnatomyItem loadAnatomyItem(AnatomyItem anatomyItem) {
        Session session = HibernateUtil.currentSession();
        anatomyItem = (AnatomyItem) session.load(AnatomyItem.class, anatomyItem.getZdbID());
        return anatomyItem;
    }

    /**
     * To retrieve the relationship of an anatomical item we need to search two
     * columns in the table anatomy_relationship: anatrel_anatitem_1_zdb_id and anatrel_anatitem_2_zdb_id.
     * This then joins with the relationship types.
     *
     * @param anatomyItem Anatomy Term
     * @return list of AnatomyRelationship objects
     */
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
     * @param stage
     */
    public DevelopmentStage getStage(DevelopmentStage stage) {
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
     * @param stage
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

    public List<AnatomyStatistics> getAnatomyItemStatisticsByStage(DevelopmentStage stage) {
        java.lang.String zdbID = stage.getZdbID();

        Session session = HibernateUtil.currentSession();
        String hql = "SELECT stats, info FROM AnatomyStatistics stats, AnatomyTreeInfo info   " +
                "WHERE stats.anatomyItem.zdbID = info.item.zdbID " +
                "AND info.zdbID = :zdbID " +
                "AND stats.anatomyItem.obsolete != :obsolete " +
                "AND stats.anatomyItem.name != :aoName " +
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

    public AnatomyItem getAnatomyItem(String name) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(AnatomyItem.class);
        criteria.add(Restrictions.eq("name", name));
        return (AnatomyItem) criteria.uniqueResult();
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
