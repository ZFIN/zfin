/**
 *  Class RunCandidate.
 */
package org.zfin.sequence.reno;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerRelationship;
import org.zfin.orthology.Species;
import org.zfin.people.Person;
import org.zfin.sequence.Accession;
import org.zfin.sequence.EntrezProtRelation;
import org.zfin.sequence.blast.Hit;
import org.zfin.sequence.blast.Query;

import java.util.*;

public class RunCandidate {
    private static Logger LOG = Logger.getLogger(RunCandidate.class);
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

        //todo: handle this exception properly
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
     * Return all genes found in list of blasthits
     *
     * @return Marker objects for genes found in list of blast hits
     */
    public List<Marker> getAllSingleAssociatedGenesFromQueries() {
        List<Marker> genes = new ArrayList<Marker>();

        LOG.debug("enter getAllAssociatedGenesFromQueries: " + getCandidateQueries().size());

        for (Query q : getCandidateQueries()) {
            LOG.debug("I've got a query: " + q.getAccession().getNumber() + " num hits: " + q.getBlastHits().size());
            for (Hit h : q.getBlastHits()) {
                LOG.debug("I've got a hit: " + h.getTargetAccession().getNumber());
                Accession a = h.getTargetAccession();
                List<Marker> genesToAdd= new ArrayList<Marker>() ;
                LOG.debug("number of genes for hit: " + genes.size());
                for (Marker m : a.getMarkers()) {
                    LOG.debug("I've got a Marker: " + m.getAbbreviation()+ " of type: "+ m.getType());
                    LOG.debug("genes.contains(m): " + genes.contains(m));
                    LOG.debug("is in type group genedom: " + m.isInTypeGroup(Marker.TypeGroup.GENEDOM));
                    // if the hit is a gene, then add directly
                    if ((m.isInTypeGroup(Marker.TypeGroup.GENEDOM))
                            && (!genes.contains(m))) {
                        LOG.debug("ADDING genedom gene: " + m.getAbbreviation());
                        genesToAdd.add(m) ;
//                        genes.add(m);
                    }
                    // if the hit is not a gene, then add any genes that encode it
                    else {
                        Set<MarkerRelationship> secondMarkerRelationships = m.getSecondMarkerRelationships() ;
                        LOG.debug(m.getAbbreviation()+ (secondMarkerRelationships!=null ? " number of second marker relationships: "+ secondMarkerRelationships.size() : "null" ));
                        for (MarkerRelationship rel : m.getSecondMarkerRelationships()) {
                            Marker gene = rel.getFirstMarker();
                            LOG.debug("gene: "+ (gene==null ? "null" : gene.getAbbreviation())  ) ; 
                            LOG.debug("encoding gene is in type group genedom: " + gene.isInTypeGroup(Marker.TypeGroup.GENEDOM));
                            LOG.debug("genes to add size: " + genesToAdd.size());
                            LOG.debug("genes to add contains: " + genesToAdd.contains(gene));

                            if (gene.isInTypeGroup(Marker.TypeGroup.GENEDOM) && !genesToAdd.contains(gene) && rel.getType().equals(
                                    MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT)){
                                LOG.debug("ADDING encoding gene: "+ gene.getAbbreviation());
                                genesToAdd.add(gene) ;
                            }
                            else{
                                LOG.debug("NOT adding encoding gene: "+ gene.getAbbreviation());
                            }
                        }
                        // only add if a single encoded relationship
                    }
                }
                LOG.debug("genes to add "+genesToAdd.size() + " for hit accession " + a.getNumber());
                if(genesToAdd.size()==1){
                    LOG.debug("adding one gene: " + genesToAdd.get(0).getAbbreviation()); ;
                    Marker geneToAdd = genesToAdd.get(0) ;
                    if(!genes.contains(geneToAdd)){
                        genes.add(genesToAdd.get(0));
                    }
                }
            }
        }
        return genes;
    }


    public Set<EntrezProtRelation> getOrthologuesFromQueries(Species organism) {
        LOG.debug("enter getOrthologuesFromQueries");
        Set<EntrezProtRelation> accessionOrthologues = new TreeSet<EntrezProtRelation>();
        for (Query q : getCandidateQueries()) {
            LOG.debug(q.getAccession().getNumber());
            for (Hit h : q.getBlastHits()) {
                Accession a = h.getTargetAccession();
                LOG.debug("accessionFound: " + a.getNumber() + " " + a.getID());

                /*if (a != null) {
                if ((a.getReferenceDatabase().getOrganism().equals(organism))
                        && (!accessionOrthologues.contains(a))) {
                    accessionOrthologues.add(a);

                }*/

                for (EntrezProtRelation b : a.getRelatedEntrezAccessions()) {
                    if (b != null) {
                        if ((b.getOrganism().equals(organism))
                                && (!accessionOrthologues.contains(b)
                                && (!StringUtils.isEmpty(b.getEntrezAccession().getAbbreviation()))))

                        {
                            accessionOrthologues.add(b);
                        }
                    }
                }


            }
        }

        //  Collections.sort(accessionOrthologues);
        return accessionOrthologues;
    }

    public Set<EntrezProtRelation> getMouseOrthologuesFromQueries() {
        return getOrthologuesFromQueries(Species.MOUSE);
    }

    public Set<EntrezProtRelation> getHumanOrthologuesFromQueries() {
        return getOrthologuesFromQueries(Species.HUMAN);
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
        Person user = Person.getCurrentSecurityUser();
        if (user == null)
            return false;

        if (lockPerson != null && user.equals(lockPerson)) {
            return true;
        } else {
            return false;
        }
    }
}


