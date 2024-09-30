/**
 * Class HibernateRenoRepository.
 */
package org.zfin.sequence.reno.repository;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.zfin.profile.Person;
import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.reno.NomenclatureRun;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.presentation.RunBean;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import static org.zfin.framework.HibernateUtil.currentSession;

@Repository
public class HibernateRenoRepository implements RenoRepository {

    private static final Logger LOG = LogManager.getLogger(HibernateRenoRepository.class);

    public List<RedundancyRun> getRedundancyRuns() {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<RedundancyRun> query = criteriaBuilder.createQuery(RedundancyRun.class);
        Root<RedundancyRun> root = query.from(RedundancyRun.class);
        CriteriaQuery<RedundancyRun> all = query.select(root);

        return session.createQuery(all).getResultList();
    }

    public List<NomenclatureRun> getNomenclatureRuns() {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<NomenclatureRun> query = criteriaBuilder.createQuery(NomenclatureRun.class);
        Root<NomenclatureRun> root = query.from(NomenclatureRun.class);
        CriteriaQuery<NomenclatureRun> all = query.select(root);

        return session.createQuery(all).getResultList();
    }

    public int getQueueCandidateCount(Run oneRun) {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<RunCandidate> root = criteriaQuery.from(RunCandidate.class);

        criteriaQuery.select(criteriaBuilder.count(root)).where(
                criteriaBuilder.and(
                        oneRun == null ? criteriaBuilder.isNull(root.get("run")) : criteriaBuilder.equal(root.get("run"), oneRun),
                        criteriaBuilder.isFalse(root.get("done")),
                        criteriaBuilder.isNull(root.get("lockPerson"))
                )
        );
        Query<Long> query = session.createQuery(criteriaQuery);
        return (query.getSingleResult()).intValue();
    }

    public List<RunCandidate> getPendingCandidates(Run run) {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<RunCandidate> criteriaQuery = criteriaBuilder.createQuery(RunCandidate.class);
        Root<RunCandidate> root = criteriaQuery.from(RunCandidate.class);

        criteriaQuery.select(root).where(
                criteriaBuilder.and(
                        run == null ? criteriaBuilder.isNull(root.get("run")) : criteriaBuilder.equal(root.get("run"), run),
                        criteriaBuilder.isFalse(root.get("done")),
                        criteriaBuilder.isNotNull(root.get("lockPerson"))
                )
        );
        Query<RunCandidate> query = session.createQuery(criteriaQuery);
        return query.getResultList();
    }

    public int getPendingCandidateCount(Run oneRun) {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<RunCandidate> root = criteriaQuery.from(RunCandidate.class);

        criteriaQuery.select(criteriaBuilder.count(root)).where(
                criteriaBuilder.and(
                        oneRun == null ? criteriaBuilder.isNull(root.get("run")) : criteriaBuilder.equal(root.get("run"), oneRun),
                        criteriaBuilder.isFalse(root.get("done")),
                        criteriaBuilder.isNotNull(root.get("lockPerson"))
                )
        );
        Query<Long> query = session.createQuery(criteriaQuery);
        return (query.getSingleResult()).intValue();
    }

    public int getFinishedCandidateCount(Run oneRun) {
        Session session = currentSession();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<RunCandidate> root = criteriaQuery.from(RunCandidate.class);

        criteriaQuery.select(criteriaBuilder.count(root)).where(
                criteriaBuilder.and(
                        oneRun == null ? criteriaBuilder.isNull(root.get("run")) : criteriaBuilder.equal(root.get("run"), oneRun),
                        criteriaBuilder.isTrue(root.get("done")),
                        criteriaBuilder.isNull(root.get("lockPerson"))
                )
        );
        Query<Long> query = session.createQuery(criteriaQuery);
        return (query.getSingleResult()).intValue();
    }

    /**
     * This is a hack to overcome deficiencies in cglib.
     *
     * @return Impl of Run class.
     */
    public Run castRun(Run run) {
        Session session = currentSession();
        if (run.isRedundancy() && !(run instanceof RedundancyRun)) {
            return session.load(RedundancyRun.class, run.getZdbID());
        } else if (run.isNomenclature() && !(run instanceof NomenclatureRun)) {
            return session.load(NomenclatureRun.class, run.getZdbID());
        } else {
            LOG.warn("run type was not recast: \n" + this);
            return run;
        }
    }

    public Run getRunByID(String zdbID) {
        return currentSession().get(Run.class, zdbID);
    }

    public List<RunCandidate> getSangerRunCandidatesInQueue(Run run) {
        String hql = """
                from RunCandidate rc 
                where rc.run.zdbID = :runid and rc.done is false and rc.lockPerson is null 
                and rc.candidate.suggestedName like 'si:%'
                """;

        Query<RunCandidate> query = currentSession().createQuery(hql, RunCandidate.class);
        query.setParameter("runid", run.getZdbID());
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
     * @param run        Run
     * @param comparator Order by statement
     * @return list of RunCandidates
     */
    public List<RunCandidate> getSortedRunCandidates(Run run, String comparator, int maxNumRecords) {
        Session session = currentSession();
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

        String hql = """
                SELECT runCandidate.zdbID, hit.expectValue, max(hit.score), runCandidate.candidate.lastFinishedDate, 
                            runCandidate.occurrenceOrder, runCandidate.candidate.problem 
                FROM RunCandidate runCandidate, Hit hit, Query query 
                WHERE runCandidate.run = :run AND 
                      hit.query = query AND 
                      query.runCandidate = runCandidate AND 
                      runCandidate.done is false AND 
                      runCandidate.lockPerson is null  AND 
                      hit.expectValue = (select min(hits.expectValue) from Hit hits 
                                           where hits.query.runCandidate = runCandidate)
                GROUP BY runCandidate.zdbID, runCandidate.candidate.problem, hit.expectValue, runCandidate.candidate.lastFinishedDate, 
                         runCandidate.occurrenceOrder 
                ORDER BY \s""" + orderBy;
        Query<Tuple> query = session.createQuery(hql, Tuple.class);
        query.setParameter("run", run);
        query.setMaxResults(maxNumRecords);
        List<Tuple> runs = query.list();
        for (Tuple tuple : runs) {
            Hit bestHit1 = new Hit();
            bestHit1.setExpectValue((Double) tuple.get(1));
            bestHit1.setScore((Integer) tuple.get(2));
            list.add(getRunCandidateByID((String) tuple.get(0), bestHit1));
        }

        if (runs.size() < maxNumRecords) {
            String hql1 = """
                    select runCandidate from RunCandidate runCandidate, Query query 
                    WHERE runCandidate.run = :run AND 
                          query.runCandidate = runCandidate AND 
                          runCandidate.done = false AND 
                          runCandidate.lockPerson is null  AND 
                          not exists (select 1 from Hit hit where hit.query = query) """;
            Query<RunCandidate> nonHitQuery = session.createQuery(hql1, RunCandidate.class);
            nonHitQuery.setParameter("run", run);
            nonHitQuery.setMaxResults(maxNumRecords - runs.size());
            List<RunCandidate> nonHitRuns = nonHitQuery.list();
            list.addAll(nonHitRuns);
        }
        session.setCacheMode(oldCacheMode);
        return list;
    }

    public List<RunCandidate> getSortedNonZFRunCandidates(Run run, String comparator, int maxNumRecords) {
        Session session = currentSession();
        CacheMode oldCacheMode = session.getCacheMode();
        LOG.info("old cache mode: " + oldCacheMode);

        session.setCacheMode(CacheMode.REFRESH);

        String orderBy;

        List<RunCandidate> list = new ArrayList<RunCandidate>();


        String hql = """
                SELECT runCandidate.zdbID , hit.expectValue, max(hit.score ), runCandidate.candidate.lastFinishedDate , runCandidate.occurrenceOrder, runCandidate.candidate.problem 
                FROM RunCandidate runCandidate, Query query , Hit hit, Accession accession  
                WHERE runCandidate.run = :run
                 and runCandidate.done is false
                 and runCandidate= query.runCandidate 
                 and runCandidate.lockPerson is null  
                 and hit.query = query 
                 and hit.targetAccession  = accession 
                 and hit.expectValue = (select min(bh.expectValue)
                                            from Hit bh,Accession ab
                                          where bh.query = query 
                                            and bh.targetAccession.ID  =ab.ID 
                                        ) 
                GROUP BY runCandidate.zdbID , runCandidate.candidate.problem,hit.expectValue , runCandidate.candidate.lastFinishedDate , runCandidate.occurrenceOrder 
                ORDER BY \s""";

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
        hql += orderBy;

        Query<Tuple> query = session.createQuery(hql, Tuple.class);
        query.setParameter("run", run);
        query.setMaxResults(maxNumRecords);
        List<Tuple> runs = query.list();
        for (Tuple tuple : runs) {
            Hit bestHit1 = new Hit();
            bestHit1.setExpectValue((Double) tuple.get(1));
            bestHit1.setScore((Integer) tuple.get(2));
            list.add(getRunCandidateByID((String) tuple.get(0), bestHit1));
        }

        if (runs.size() < maxNumRecords) {
            String hql1 = """
                    select runCandidate from RunCandidate runCandidate, Query query 
                    WHERE runCandidate.run = :run AND 
                          query.runCandidate = runCandidate AND 
                          runCandidate.done is false AND 
                          runCandidate.lockPerson is null  AND 
                          not exists (select 1 from Hit hit where hit.query = query)
                    """ ;
            Query<RunCandidate> nonHitQuery = session.createQuery(hql1, RunCandidate.class);
            nonHitQuery.setParameter("run", run);
            nonHitQuery.setMaxResults(maxNumRecords - runs.size());
            List<RunCandidate> nonHitRuns = nonHitQuery.list();
            list.addAll(nonHitRuns);
        }
        session.setCacheMode(oldCacheMode);

        return list;
    }


    public RunCandidate getRunCandidateByID(String runCandidateID) {
        return currentSession().get(RunCandidate.class, runCandidateID);
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
        RunCandidate runCandidate = getRunCandidateByID(zdbID);
        runCandidate.setBestHit(bestHit);
        return runCandidate;
    }

    public Double getRunCandidateExpectValueByScore(String rcZdbId, Integer score) {
        String hql = """
                select min(h.expectValue) 
                 from org.zfin.sequence.blast.Query q, 
                      org.zfin.sequence.blast.Hit h 
                where q.runCandidate.zdbID = :rcid 
                and h.query.zdbID = q.zdbID 
                and h.score = :score """;

        Query<Number> query = currentSession().createQuery(hql, Number.class);
        query.setParameter("rcid", rcZdbId);
        query.setParameter("score", score);
        return query.uniqueResult().doubleValue();
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
