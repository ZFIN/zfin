package org.zfin.anatomy.repository;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;
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

import jakarta.persistence.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is an implementation that provides access to the database storage layer.
 * Methods that allow to retrieve, save and update objects that pertain
 * to the Anatomy domain.
 */
@Repository
public class HibernateAnatomyRepository implements AnatomyRepository {

	// Cached variables
	// The stages do not change very often, only through an import
	private List<DevelopmentStage> allStagesWithoutUnknown;


	/**
	 * Retrieve all developmental stages.
	 * Note that this is cached the first time it is read from the database.
	 * Make sure to call the invalidateCachedObjects() if you upload a new AO
	 * and this list becomes invalid.
	 *
	 * @return a list of DevelopmentStage objects
	 */
	public List<DevelopmentStage> getAllStages() {

		Session session = HibernateUtil.currentSession();
		Query<DevelopmentStage> query = session.createQuery("from DevelopmentStage order by hoursStart", DevelopmentStage.class);
		return query.list();
	}

	public List<DevelopmentStage> getAllStagesWithoutUnknown() {
		if (allStagesWithoutUnknown != null) {
			return allStagesWithoutUnknown;
		}

		Session session = HibernateUtil.currentSession();
		Query<DevelopmentStage> query = session.createQuery("from DevelopmentStage where name != :name order by hoursStart", DevelopmentStage.class);
		query.setParameter("name", DevelopmentStage.UNKNOWN);
		allStagesWithoutUnknown = query.list();
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
			return session.load(DevelopmentStage.class, stageID);
		}
		String zdbID = stage.getZdbID();
		if (!StringUtils.isEmpty(zdbID)) {
			return session.load(DevelopmentStage.class, zdbID);
		}
		String oboID = stage.getOboID();
		if (!StringUtils.isEmpty(oboID)) {
			return getStageByOboID(oboID);
		}
		String name = stage.getName();
		if (StringUtils.isEmpty(name))
			throw new RuntimeException("No Valid identifier found (stageID, zdbID or name)");
		return getStageByName(name);
	}

	public DevelopmentStage getStageByID(String stageID) {
		if (stageID == null)
			return null;
		Session session = HibernateUtil.currentSession();
		return session.get(DevelopmentStage.class, stageID);
	}

	public DevelopmentStage getStageByName(String stageName) {
		Session session = HibernateUtil.currentSession();
		Query<DevelopmentStage> query = session.createQuery("from DevelopmentStage where name = :name", DevelopmentStage.class);
		query.setParameter("name", stageName);
		return query.uniqueResult();
	}

	public List<AnatomyStatistics> getAnatomyItemStatisticsByStage(DevelopmentStage stage) {
		java.lang.String zdbID = stage.getZdbID();

		Session session = HibernateUtil.currentSession();
		String hql = """
			SELECT stats, info FROM AnatomyStatistics stats, AnatomyTreeInfo info, GenericTerm aoTerm
			WHERE stats.term.oboID = aoTerm.oboID
				AND  aoTerm.zdbID = info.item.zdbID
				AND info.zdbID = :zdbID
				AND stats.term.obsolete != :obsolete
				AND stats.term.termName != :aoName
				AND stats.type = :type
			ORDER BY info.sequenceNumber
			""";
		Query<Tuple> query = session.createQuery(hql, Tuple.class);
		query.setParameter("zdbID", zdbID);
		query.setParameter("aoName", "unspecified");
		query.setParameter("obsolete", true);
		query.setParameter("type", AnatomyStatistics.Type.GENE);
		List<Tuple> objects = query.list();
		return createStatisticsWithTreeInfo(objects);
	}

	public AnatomyStatistics getAnatomyStatistics(String anatomyZdbID) {
		Session session = HibernateUtil.currentSession();
		String hql = "FROM AnatomyStatistics stat " +
			"WHERE stat.zdbID = :zdbID " +
			"and stat.type = :type ";
		Query<AnatomyStatistics> query = session.createQuery(hql, AnatomyStatistics.class);
		query.setParameter("zdbID", anatomyZdbID);
		query.setParameter("type", AnatomyStatistics.Type.GENE);
		return query.uniqueResult();
	}

	public AnatomyStatistics getAnatomyStatisticsForMutants(String anatomyZdbID) {
		Session session = HibernateUtil.currentSession();
		String hql = "from AnatomyStatistics stat " +
			"where stat.zdbID = :zdbID " +
			"      AND stat.type = :type ";
		Query<AnatomyStatistics> query = session.createQuery(hql, AnatomyStatistics.class);
		query.setParameter("zdbID", anatomyZdbID);
		query.setParameter("type", AnatomyStatistics.Type.GENO);
		return query.uniqueResult();
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
	public List<GenericTerm> getTermsDevelopingFromWithOverlap(String termID, double startHours, double endHours) {
		Session session = HibernateUtil.currentSession();

		String hql = """
			select termRelationship from GenericTermRelationship termRelationship
			     JOIN termRelationship.termTwo.termStage termStage
			     where
			          termRelationship.termOne.zdbID = :termID AND
			          termRelationship.type = :type AND
			          termRelationship.type = :type AND
			          ((termStage.start.hoursStart > :start AND termStage.start.hoursStart < :end)
			        OR (termStage.end.hoursEnd > :start AND termStage.end.hoursEnd < :end)
			        OR (termStage.start.hoursStart < :start AND termStage.end.hoursEnd > :end))
			        order by termRelationship.termTwo.termNameOrder
			        """;
		Query<GenericTermRelationship> query = session.createQuery(hql, GenericTermRelationship.class);
		query.setParameter("termID", termID);
		query.setParameter("start", (float) startHours);
		query.setParameter("end", (float) endHours);
		query.setParameter("type", RelationshipSorting.DEVELOPS_FROM);
		List<GenericTermRelationship> ones = query.list();
		if (ones == null)
			return null;
		List<GenericTerm> terms;
		terms = new ArrayList<>();
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
		Query<DevelopmentStage> query = session.createQuery("from DevelopmentStage where oboID = :oboID", DevelopmentStage.class);
		query.setParameter("oboID", oboID);
		return query.uniqueResult();
	}

	/*
	 * ToDO: Convenience method as long as anatomy_display contains multiple records for a single stage.
	 */

	private List<AnatomyStatistics> createStatisticsWithTreeInfo(List<Tuple> objects) {
		if (objects == null) {
			return null;
		}
		return objects.stream().map(tuple -> {
			AnatomyStatistics stat = (AnatomyStatistics) tuple.get(0);
			AnatomyTreeInfo treeInfo = (AnatomyTreeInfo) tuple.get(1);
			stat.setTreeInfo(treeInfo);
			GenericTerm item = RepositoryFactory.getOntologyRepository().getTermByOboID(stat.getTerm().getOboID());
			stat.setTerm(item);
			return stat;
		}).collect(Collectors.toList());
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
		Session session = HibernateUtil.currentSession();
		String hql = "from DevelopmentStage where hoursStart = :hoursStart AND name != :name";
		Query<DevelopmentStage> query = session.createQuery(hql, DevelopmentStage.class);
		query.setParameter("hoursStart", start);
		query.setParameter("name", DevelopmentStage.UNKNOWN);
		return query.uniqueResult();
	}

	@Override
	public DevelopmentStage getStageByEndHours(float end) {
		Session session = HibernateUtil.currentSession();
		String hql = "from DevelopmentStage where hoursEnd = :hoursEnd AND name != :name";
		Query<DevelopmentStage> query = session.createQuery(hql, DevelopmentStage.class);
		query.setParameter("hoursEnd", end);
		query.setParameter("name", DevelopmentStage.UNKNOWN);
		return query.uniqueResult();
	}

	@Override
	public List<GenericTerm> getMultipleTerms(Set<String> aoTermIDs) {
		String hql = "from GenericTerm where oboID in (:list)";
		Query<GenericTerm> query = HibernateUtil.currentSession().createQuery(hql, GenericTerm.class);
		query.setParameterList("list", aoTermIDs);
		return query.list();
	}

}
