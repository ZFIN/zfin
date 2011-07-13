package org.zfin.orthology.repository;

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
    private Set<String> evidenceCodes ;

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

    public void addEvidenceCode(String evidenceCode){
        if(evidenceCodes==null){
            evidenceCodes = new TreeSet<String>();
        }
        evidenceCodes.add(evidenceCode);
    }

    public Set<String> getEvidenceCodes() {
        return evidenceCodes;
    }

    public void setEvidenceCodes(Set<String> evidenceCodes) {
        this.evidenceCodes = evidenceCodes;
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

        if(row.getEvidenceCodes()!=null){
            for(String evidenceCode : row.getEvidenceCodes()){
                addEvidenceCode(evidenceCode);
            }
        }
    }

    public boolean isPositionValid() {
        if (position == null || position.equals("") || position.equals("-")) {
            return false;
        }
        return true;
    }
}
