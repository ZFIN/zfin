/**
 * Class RunCandidate.
 */
package org.zfin.sequence.reno;

import org.apache.log4j.Logger;
import org.zfin.marker.Marker;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.blast.Query;

import java.util.*;

public class RunCandidate {
    private static final Logger LOG = Logger.getLogger(RunCandidate.class);
    private String zdbID;
    private Run run;
    private Candidate candidate;
    private Set<Query> candidateQueries = new HashSet<Query>();
    private Person lockPerson;
    private boolean done;
    private Hit bestHit;


    private int occurrenceOrder;

    /**
     * Is this record locked?
     *
     * @return true if locked, false if not locked
     */
    public boolean isLocked() {
        return lockPerson != null;
    }

    /**
     * Fins and returns the best blast hit based first on expect value, then score
     *
     * @return best blast hit.
     */
    public Hit getBestHit() {
        if (bestHit != null)
            return bestHit;

        if (candidateQueries.isEmpty())
            return null;

        Hit bestHit = new Hit();
        bestHit.setExpectValue(Hit.noHitExpectValue);
        bestHit.setScore(Hit.noHitScore);
        for (Query q : candidateQueries) {
            if (!q.getBlastHits().isEmpty()) {
                Hit cHit = q.getBlastHits().iterator().next();
                if (cHit.getExpectValue() < bestHit.getExpectValue() ||
                        (cHit.getExpectValue() == bestHit.getExpectValue() && cHit.getScore() > bestHit.getScore())) {

                    bestHit = cHit;
                }
            }
        }
        return bestHit;
    }


    public void setBestHit(Hit bestHit) {
        this.bestHit = bestHit;
    }


    /**
     * Get a single marker associated with these queries, based on
     * an assumption that there will be either a single marker or no markers.
     * <p/>
     * will throw an exception if this there are more than one.
     *
     * @return marker or null if no marker is associated
     */
    public Marker getIdentifiedMarker() {
        List<Marker> markers = getIdentifiedMarkers();

        if (markers.size() > 1) {
            throw new RuntimeException("more than one marker identified with run candidate");
        }

        if (markers.size() == 1)
            return markers.get(0);
        else
            return null;

    }


    /**
     * Get all markers associated with all blast query accessions on
     * this RunCandidate
     *
     * @return List of Markers
     */
    public List<Marker> getIdentifiedMarkers() {
        List<Marker> markers = new ArrayList<Marker>();
        for (Query q : getCandidateQueries()) {
            LOG.debug("I've got a queryIM: " + q.getAccession().getNumber());
            for (Marker m : q.getAccession().getBlastableMarkers()) {
                LOG.debug("I've got a markerIM: " + m.getAbbreviation());
                if (!markers.contains(m)) {
                    markers.add(m);
                }
            }
        }

        return markers;
    }

    /**
     * Return an ordered list of blast query objects
     *
     * @return list of blast query objects
     */
    public List<Query> getCandidateQueryList() {
        List<Query> queries = new ArrayList<Query>();

        for (Query q : getCandidateQueries()) {
            queries.add(q);
        }
        Collections.sort(queries);
        return queries;
    }


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Run getRun() {
        return run;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Person getLockPerson() {
        return lockPerson;
    }

    public void setLockPerson(Person lockPerson) {
        this.lockPerson = lockPerson;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getOccurrenceOrder() {
        return occurrenceOrder;
    }

    public void setOccurrenceOrder(int occurrenceOrder) {
        this.occurrenceOrder = occurrenceOrder;
    }

    public Set<Query> getCandidateQueries() {
        return candidateQueries;
    }

    public void setCandidateQueries(Set<Query> candidateQueries) {
        this.candidateQueries = candidateQueries;
    }

    public boolean isOwner() {
        Person user = ProfileService.getCurrentSecurityUser();
        if (user == null)
            return false;

        return lockPerson != null && user.equals(lockPerson);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("RunCandidate");
        sb.append("{zdbID='").append(zdbID).append('\'');
        sb.append(", run=").append(run);
        sb.append(", candidate=").append(candidate);
        sb.append(", lockPerson=").append(lockPerson);
        sb.append(", done=").append(done);
        sb.append(", bestHit=").append(bestHit);
        sb.append(", occurrenceOrder=").append(occurrenceOrder);
        sb.append(", candidateQueries=").append(candidateQueries);
        sb.append('}');
        return sb.toString();
    }
}


