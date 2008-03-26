/**
 *  Class HibernateRenoRepository.
 */
package org.zfin.sequence.reno.repository;

//import org.apache.log4j.Logger;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.CacheMode;
import org.hibernate.criterion.Restrictions;
import org.zfin.framework.HibernateUtil;
import org.zfin.people.Person;
import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.sequence.reno.presentation.RunBean;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class HibernateRenoRepository implements RenoRepository {

    private static Logger LOG = Logger.getLogger(HibernateRenoRepository.class);


    public List<RedundancyRun> getRedundancyRuns() {
//        Session session = HibernateUtil.currentSession();

//        Query query = session.createQuery(" from Run r where r.type = :type " +
//                " order by r.date desc");
//        query.setParameter("type", Run.Type.REDUNDANCY);
//        return query.list();

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(RedundancyRun.class);
//        criteria.add(Restrictions.eq("type", Run.Type.REDUNDANCY));
        return criteria.list();

    }


    public List<NomenclatureRun> getNomenclatureRuns() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(NomenclatureRun.class);
//        criteria.add(Restrictions.eq("type", Run.Type.NOMENCLATURE));
        return criteria.list();
    }

    public Integer getQueueCandidateCount(Run oneRun) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("select count(*) from RunCandidate rc " +
                "where rc.run = :run and rc.done = 'f' and rc.lockPerson is null");
        query.setParameter("run", oneRun);
        Integer count = (Integer) query.uniqueResult();
        return count;
    }

    public Integer getPendingCandidateCount(Run oneRun) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("select count(*) from RunCandidate rc " +
                "where rc.run = :run and rc.done = 'f' and rc.lockPerson is not null");
        query.setParameter("run", oneRun);
        Integer count = (Integer) query.uniqueResult();
        return count;
    }

    public Integer getFinishedCandidateCount(Run oneRun) {
        Session session = HibernateUtil.currentSession();
        Query query = session.createQuery("select count(*) from RunCandidate rc " +
                "where rc.run = :run and rc.done = 't' and rc.lockPerson is null");
        query.setParameter("run", oneRun);
        Integer count = (Integer) query.uniqueResult();
        return count;
    }

    /**
     * This is a hack to overcome deficiencies in cglib.
     *
     * @return Impl of Run class.
     */
    public Run castRun(Run run) {

        if (run.isRedundancy() && !(run instanceof RedundancyRun)) {
            return (RedundancyRun) HibernateUtil.currentSession().load(RedundancyRun.class, run.getZdbID());
        } else if (run.isNomenclature() && !(run instanceof NomenclatureRun)) {
            return (NomenclatureRun) HibernateUtil.currentSession().load(NomenclatureRun.class, run.getZdbID());
        } else {
            LOG.warn("run type was not recast: \n" + this);
            return run;
        }

    }


    public Run getRunByID(String zdbID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(org.zfin.sequence.reno.Run.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        return (Run) criteria.uniqueResult();
    }

    public List<RunCandidate> getRunCandidates(String runZdbId) {

        Session session = HibernateUtil.currentSession();
        String hql = " from RunCandidate rc " +
                " where rc.run.zdbID = :runid and rc.done='f' and rc.lockPerson is null";

        Query query = session.createQuery(hql);
        query.setParameter("runid", runZdbId);
        query.setMaxResults(40);

        return query.list();
    }

    /**
     * Note: You do not get a full Hit record back, just non-persistent one that
     * comes form the best blast hit results, i.e. no hit.zdbID (to save an extra
     * db call to get all the bests hits since we obtain them right here.
     * We only want to see the problem ones when there are no more records left.
     * <p/>
     * Additionally, we want to make sure that we go directly to the database and update the cache
     * as triggers may have run in the background effecting sorting.
     *
     * @param runZdbId   run zdbID
     * @param comparator Order by statement
     * @return list of RunCandidates
     */
    public List<RunCandidate> getSortedRunCandidates(String runZdbId, String comparator, int maxNumRecords) {
        Session session = HibernateUtil.currentSession();
        CacheMode oldCacheMode = session.getCacheMode();
        LOG.info("old cache mode: " + oldCacheMode);

        session.setCacheMode(CacheMode.REFRESH);

        String orderBy;

        List<RunCandidate> list = new ArrayList<RunCandidate>();

        if (comparator.equals(RunBean.SORT_BY_OCCURRENCE_DSC)) {
            orderBy = "runCandidate.candidate.problem asc, runCandidate.occurrenceOrder desc, hit.expectValue asc, max(hit.score) desc";
        } else if (comparator.equals(RunBean.SORT_BY_OCCURRENCE_ASC)) {
            orderBy = "runCandidate.candidate.problem asc, runCandidate.occurrenceOrder asc, hit.expectValue asc, max(hit.score) desc";
        } else if (comparator.equals(RunBean.SORT_BY_LASTDONE_ASC)) {
            orderBy = "runCandidate.candidate.problem asc, runCandidate.candidate.lastFinishedDate asc, hit.expectValue asc, max(hit.score) desc";
        } else if (comparator.equals(RunBean.SORT_BY_LASTDONE_DSC)) {
            orderBy = "runCandidate.candidate.problem asc, runCandidate.candidate.lastFinishedDate desc, hit.expectValue asc, max(hit.score) desc";
        } else {
            orderBy = "runCandidate.candidate.problem asc, hit.expectValue asc, max(hit.score) desc";
        }

        String hql = "SELECT runCandidate.zdbID, hit.expectValue, max(hit.score), runCandidate.candidate.lastFinishedDate, " +
                "            runCandidate.occurrenceOrder, runCandidate.candidate.problem " +
                "FROM RunCandidate runCandidate, Hit hit, Query query " +
                "WHERE runCandidate.run.zdbID = :zdbID AND " +
                "      hit.query = query AND " +
                "      query.runCandidate = runCandidate AND " +
                "      runCandidate.done = :done AND " +
                "      runCandidate.lockPerson is null  AND " +
                "      hit.expectValue = (select min(hits.expectValue) from Hit hits " +
                "                           where hits.query.runCandidate = runCandidate)" +
                "GROUP BY runCandidate.zdbID, runCandidate.candidate.problem, hit.expectValue, runCandidate.candidate.lastFinishedDate, " +
                "         runCandidate.occurrenceOrder " +
                "ORDER BY " + orderBy;
        Query query = session.createQuery(hql);
        query.setString("zdbID", runZdbId);
        query.setBoolean("done", false);
        query.setMaxResults(maxNumRecords);
        List runs = query.list();
        for (Object run : runs) {
            Object[] tuple = (Object[]) run;
            Hit bestHit1 = new Hit();
            bestHit1.setExpectValue((Double) tuple[1]);
            bestHit1.setScore((Integer) tuple[2]);
            list.add(getRunCandidateByID((String) tuple[0], bestHit1));
        }

        if (runs == null || runs.size() < maxNumRecords) {
            String hql1 = "select runCandidate from RunCandidate runCandidate, Query query " +
                    "WHERE runCandidate.run.zdbID = :zdbID AND " +
                    "      query.runCandidate = runCandidate AND " +
                    "      runCandidate.done = :done AND " +
                    "      runCandidate.lockPerson is null  AND " +
                    "      not exists (select 1 from Hit hit where hit.query = query) ";
            Query nonHitQuery = session.createQuery(hql1);
            nonHitQuery.setString("zdbID", runZdbId);
            nonHitQuery.setBoolean("done", false);
            nonHitQuery.setMaxResults(maxNumRecords - runs.size());
            List<RunCandidate> nonHitRuns = nonHitQuery.list();
            for (RunCandidate cand : nonHitRuns) {
                list.add(cand);
            }
        }
        session.setCacheMode(oldCacheMode);

        return list;
    }


    public RunCandidate getRunCandidateByID(String runCandidateID) {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(RunCandidate.class);
        criteria.add(Restrictions.eq("zdbID", runCandidateID));
        String hql = " from RunCandidate eager ";
        return (RunCandidate) criteria.uniqueResult();
    }

    /**
     * Load RunCandidate objects and then add the best Hit object.
     * This object is needed for the Candidate in queue page.
     *
     * @param zdbID   RunCandidate ZDB ID
     * @param bestHit The best Hit object
     * @return RunCandidate
     */
    private RunCandidate getRunCandidateByID(String zdbID, Hit bestHit) {

        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(RunCandidate.class);
        criteria.add(Restrictions.eq("zdbID", zdbID));
        RunCandidate runCandidate = (RunCandidate) criteria.uniqueResult();
        runCandidate.setBestHit(bestHit);
        return runCandidate;

    }

    public Double getRunCandidateExpectValueByScore(String rcZdbId, Integer score) {
        Session session = HibernateUtil.currentSession();
        String hql = " select min(h.expectValue) " +
                "  from org.zfin.sequence.blast.Query q, " +
                "       org.zfin.sequence.blast.Hit h " +
                " where q.runCandidate.zdbID = :rcid " +
                " and h.query.zdbID = q.zdbID " +
                " and h.score = :score ";

        Query query = session.createQuery(hql);
        query.setParameter("rcid", rcZdbId);
        query.setParameter("score", score);
        return (Double) query.uniqueResult();
    }

    public List<RunCandidate> getPendingCandidates(Run run) {
        Session session = HibernateUtil.currentSession();
        String hql = " from RunCandidate rc " +
                " where rc.run = :run and rc.done='f' and rc.lockPerson is not null";

        Query query = session.createQuery(hql);
        query.setParameter("run", run);
        return query.list();
    }


    public boolean lock(Person newOwner, RunCandidate rc) {
        if (rc.getLockPerson() == null) {
            rc.setLockPerson(newOwner);
            return true;
        } else if (rc.getLockPerson().getZdbID().equals(newOwner.getZdbID())) {
            return true;
        } else {
            return false;
        }
    }


    public boolean unlock(Person currentOwner, RunCandidate rc) {
        if (rc.getLockPerson() != null && rc.getLockPerson().equals(currentOwner)) {
            rc.setLockPerson(null);
            return true;
        } else {
            return false;
        }
    }

}
