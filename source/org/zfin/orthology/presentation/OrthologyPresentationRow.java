package org.zfin.orthology.presentation;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 */
public class OrthologyPresentationRow {

    private String species ;
    private String abbreviation ;
    private String chromosome ;
    private String position ;
    private Set<String> accessions;
    private Collection<OrthologEvidencePresentation> evidence;

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

    public void addAccession(String accession){
        if(accessions==null){
            this.accessions = new TreeSet<String>();
        }
        accessions.add(accession) ;
    }

    public Set<String> getAccessions() {
        return accessions;
    }

    public void setAccessions(Set<String> accessions) {
        this.accessions = accessions;
    }

    public void setEvidence(Collection<OrthologEvidencePresentation> evidence){
        this.evidence = evidence;
    }

    public Collection<OrthologEvidencePresentation> getEvidence() {
        return evidence;
    }

    /**
     * Copy from the row passed in.
     * @param row
     */
    public void copyFrom(OrthologyPresentationRow row) {
        if(row.getAccessions()!=null){
            for(String accession : row.getAccessions()){
                addAccession(accession);
            }
        }

        if(row.getEvidence()!=null){
            setEvidence(row.getEvidence());
        }
    }

    public boolean isPositionValid() {
        if (position == null || position.equals("") || position.equals("-")) {
            return false;
        }
        return true;
    }
}
