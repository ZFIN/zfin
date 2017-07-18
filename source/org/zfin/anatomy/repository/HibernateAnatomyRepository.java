package org.zfin.anatomy.repository;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.zfin.anatomy.AnatomyStatistics;
import org.zfin.anatomy.AnatomyTreeInfo;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.presentation.RelationshipSorting;
import org.zfin.expression.ExpressionStructure;
import org.zfin.framework.HibernateUtil;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.GenericTermRelationship;
import org.zfin.profile.service.ProfileService;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation that provides access to the database storage layer.
 * Methods that allow to retrieve, save and update objects that pertain
 * to the Anatomy domain.
 */
@Repository
public class HibernateAnatomyRepository implements AnatomyRepository {

    private static Logger LOG = Logger.getLogger(HibernateAnatomyRepository.class);

    // Cached variables
    // The stages do not change very often, only through an import
    private List<DevelopmentStage> allStagesWithoutUnknown;


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

    /**
     * Load the stage identified by its identifier.
     * First, it tries to find by stageID if available (fastest way since it
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
            return (DevelopmentStage) session.load(DevelopmentStage.class, stageID);
        }
        java.lang.String zdbID = stage.getZdbID();
        if (!StringUtils.isEmpty(zdbID)) {
            Criteria criteria = session.createCriteria(DevelopmentStage.class);
            criteria.add(Restrictions.eq("zdbID", zdbID));
            return (DevelopmentStage) criteria.uniqueResult();
        }
        java.lang.String oboID = stage.getOboID();
        if (!StringUtils.isEmpty(oboID)) {
            Criteria criteria = session.createCriteria(DevelopmentStage.class);
            criteria.add(Restrictions.eq("oboID", oboID));
            return (DevelopmentStage) criteria.uniqueResult();
        }
        java.lang.String name = stage.getName();
        if (StringUtils.isEmpty(name))
            throw new RuntimeException("No Valid identifier found (stageID, zdbID or name)");
        Criteria criteria = session.createCriteria(DevelopmentStage.class);
        criteria.add(Restrictions.eq("name", name));
        return (DevelopmentStage) criteria.uniqueResult();
    }

    public DevelopmentStage getStageByID(String stageID) {
        if (stageID == null)
            return null;
        Session session = HibernateUtil.currentSession();
        return (DevelopmentStage) session.get(DevelopmentStage.class, stageID);
    }

    public DevelopmentStage getStageByName(java.lang.String stageName) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DevelopmentStage.class);
        criteria.add(Restrictions.eq("name", stageName));
        return (DevelopmentStage) criteria.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<AnatomyStatistics> getAnatomyItemStatisticsByStage(DevelopmentStage stage) {
        java.lang.String zdbID = stage.getZdbID();

        Session session = HibernateUtil.currentSession();
        String hql = "SELECT stats, info FROM AnatomyStatistics stats, AnatomyTreeInfo info, GenericTerm aoTerm   " +
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

    public AnatomyStatistics getAnatomyStatistics(String anatomyZdbID) {
        Session session = HibernateUtil.currentSession();
        String hql = "FROM AnatomyStatistics stat " +
                "WHERE stat.zdbID = :zdbID " +
                "and stat.type = :type ";
        Query query = session.createQuery(hql);
        query.setParameter("zdbID", anatomyZdbID);
        query.setParameter("type", AnatomyStatistics.Type.GENE);
        return (AnatomyStatistics) query.uniqueResult();
    }

    public AnatomyStatistics getAnatomyStatisticsForMutants(String anatomyZdbID) {
        Session session = HibernateUtil.currentSession();
        String hql = "from AnatomyStatistics stat " +
                "where stat.zdbID = :zdbID " +
                "      AND stat.type = :type ";
        Query query = session.createQuery(hql);
        query.setParameter("zdbID", anatomyZdbID);
        query.setParameter("type", AnatomyStatistics.Type.GENO);
        return (AnatomyStatistics) query.uniqueResult();
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
    public List<GenericTerm> getTermsDevelopingFromWithOverlap(String termID, double startHours, double endHours) {
        Session session = HibernateUtil.currentSession();

        String hql = "from GenericTermRelationship termRelationship where " +
                "          termRelationship.termOne.zdbID = :termID AND" +
                "          termRelationship.type = :type AND " +
                "          ((termRelationship.termTwo.termStage.start.hoursStart > :start AND termRelationship.termTwo.termStage.start.hoursStart < :end)" +
                "        OR (termRelationship.termTwo.termStage.end.hoursEnd > :start AND termRelationship.termTwo.termStage.end.hoursEnd < :end) " +
                "        OR (termRelationship.termTwo.termStage.start.hoursStart < :start AND termRelationship.termTwo.termStage.end.hoursEnd > :end))" +
                "        order by termRelationship.termTwo.termNameOrder ";
        Query query = session.createQuery(hql);
        query.setParameter("termID", termID);
        query.setDouble("start", startHours);
        query.setDouble("end", endHours);
        query.setParameter("type", RelationshipSorting.DEVELOPS_FROM);
        List<GenericTermRelationship> ones = (List<GenericTermRelationship>) query.list();
        if (ones == null)
            return null;
        List<GenericTerm> terms = new ArrayList<GenericTerm>();
        for (GenericTermRelationship one : ones) {
            terms.add(one.getTermTwo());
        }
        return terms;
    }

    /**
     * Create a new structure - post-composed - for the structure pile.
     *
     * @param structure structure
     */
    public void createPileStructure(ExpressionStructure structure) {
        structure.setPerson(ProfileService.getCurrentSecurityUser());
        Session session = HibernateUtil.currentSession();
        session.save(structure);
    }

    @Override
    public DevelopmentStage getStageByOboID(String oboID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(DevelopmentStage.class);
        criteria.add(Restrictions.eq("oboID", oboID));
        return (DevelopmentStage) criteria.uniqueResult();
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
            GenericTerm item = RepositoryFactory.getOntologyRepository().getTermByOboID(stat.getTerm().getOboID());
            stat.setTerm(item);
        }
        return stats;
    }

    /**
     * Dereference chached variables to force a database re-read. This is need in case an import of AO terms
     * invalidates the data.
     */
    public void invalidateCachedObjects() {
        allStagesWithoutUnknown = null;
    }

    @Override
    public DevelopmentStage getStageByStartHours(float start) {
        return (DevelopmentStage) HibernateUtil.currentSession()
                .createCriteria(DevelopmentStage.class)
                .add(Restrictions.eq("hoursStart", start))
                .add(Restrictions.ne("name", "Unknown"))
                .uniqueResult();
    }

    @Override
    public DevelopmentStage getStageByEndHours(float end) {
            return (DevelopmentStage) HibernateUtil.currentSession()
                    .createCriteria(DevelopmentStage.class)
                    .add(Restrictions.eq("hoursEnd", end))
                    .add(Restrictions.ne("name", "Unknown"))
                    .uniqueResult();
    }

}
