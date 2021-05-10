package org.zfin.orthology.presentation;

import org.zfin.orthology.OrthologExternalReference;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 */
public class OrthologyPresentationRow {

    private String orthoID;
    private String species ;
    private String abbreviation ;
    private String chromosome ;
    private String position ;
    private Set<OrthologExternalReference> accessions;
    private Collection<OrthologEvidencePresentation> evidence;

    public String getOrthoID() {
        return orthoID;
    }

    public void setOrthoID(String orthoID) {
        this.orthoID = orthoID;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void addAccession(OrthologExternalReference accession) {
        if (accessions==null) {
            this.accessions = new TreeSet<>();
        }
        accessions.add(accession) ;
    }

    public Set<OrthologExternalReference> getAccessions() {
        return accessions;
    }

    public void setAccessions(Set<OrthologExternalReference> accessions) {
        this.accessions = accessions;
    }

    public void setEvidence(Collection<OrthologEvidencePresentation> evidence){
        this.evidence = evidence;
    }

    public Collection<OrthologEvidencePresentation> getEvidence() {
        return evidence;
    }

    public boolean isPositionValid() {
        return !(position == null || position.equals("") || position.equals("-"));
    }
}
