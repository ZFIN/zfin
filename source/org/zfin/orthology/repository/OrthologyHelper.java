package org.zfin.orthology.repository;

import org.zfin.orthology.EvidenceCode;

import java.util.Set;

/**
 * User: giles
 * Date: Aug 23, 2006
 * Time: 1:35:11 PM
 */

/**
 * Hibernate repository business object used to map most of the orthology information
*/
public class OrthologyHelper {
    private String ZdbID;
    private String geneID;
    private String species;
    private String symbol;
    private String chromosome;
    private String position;
    private Set<EvidenceCode> evidence;
    private Set<AccessionHelperDBLink> accessionHelpers;

    public String getSpecies() {
        return species;
    }

    public String getZdbID() {
        return ZdbID;
    }

    public String getGeneID() {
        return geneID;
    }

    public void setGeneID(String geneID) {
        this.geneID = geneID;
    }

    public void setZdbID(String ZdbID) {
        this.ZdbID = ZdbID;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
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

    public Set<EvidenceCode> getEvidence() {
        return evidence;
    }

    public void setEvidence(Set<EvidenceCode> evidence) {
        this.evidence = evidence;
    }

    public Set<AccessionHelperDBLink> getAccessionHelpers() {
        return accessionHelpers;
    }

    public void setAccessionHelpers(Set<AccessionHelperDBLink> accessionHelpers) {
        this.accessionHelpers = accessionHelpers;
    }
}
