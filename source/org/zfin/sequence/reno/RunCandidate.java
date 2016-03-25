package org.zfin.sequence.reno;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.Species;
import org.zfin.marker.Marker;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.sequence.Accession;
import org.zfin.sequence.EntrezProtRelation;
import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.blast.Query;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "run_candidate")
public class RunCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "RUNCAN")
            })
    @Column(name = "runcan_zdb_id")
    private String zdbID;
    @ManyToOne
    @JoinColumn(name = "runcan_run_zdb_id")
    private Run run;
    @ManyToOne
    @JoinColumn(name = "runcan_cnd_zdb_id")
    private Candidate candidate;
    @OneToMany(mappedBy = "runCandidate")
    private Set<Query> candidateQueries = new HashSet<>();
    @ManyToOne
    @JoinColumn(name = "runcan_locked_by")
    private Person lockPerson;
    @Column(name = "runcan_done")
    private boolean done;
    @Transient
    private Hit bestHit;
    @Column(name = "runcan_occurrence_order")
    private int occurrenceOrder;
    @Transient
    private static final Logger LOG = Logger.getLogger(RunCandidate.class);

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
        List<Marker> markers = new ArrayList<>();
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


    public Set<EntrezProtRelation> getOrthologsFromQueries(Species.Type organism) {
        LOG.debug("enter getOrthologuesFromQueries");
        Set<EntrezProtRelation> accessionOrthologues = new TreeSet<>();
        for (Query q : getCandidateQueries()) {
            LOG.debug(q.getAccession().getNumber());
            for (Hit h : q.getBlastHits()) {
                Accession a = h.getTargetAccession();
                LOG.debug("accessionFound: " + a.getNumber() + " " + a.getID() + a.getOrganism());
                for (EntrezProtRelation b : a.getRelatedEntrezAccessions()) {
                    if (b != null) {
                        if ((b.getOrganism().equals(organism))
                                && (!accessionOrthologues.contains(b)
                                && (!StringUtils.isEmpty(b.getEntrezAccession().getAbbreviation())))) {
                            accessionOrthologues.add(b);
                        }
                    }
                }

            }
        }
        return accessionOrthologues;
    }

    public Set<EntrezProtRelation> getMouseOrthologsFromQueries() {
        return getOrthologsFromQueries(Species.Type.MOUSE);
    }

    public Set<EntrezProtRelation> getHumanOrthologsFromQueries() {
        return getOrthologsFromQueries(Species.Type.HUMAN);
    }


    /**
     * Return an ordered list of blast query objects
     *
     * @return list of blast query objects
     */
    public List<Query> getCandidateQueryList() {
        List<Query> queries = new ArrayList<>();

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


