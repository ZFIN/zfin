package org.zfin.orthology;

import org.zfin.Species;
import org.zfin.marker.Marker;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;

public class Ortholog implements Comparable, Serializable {

    private String zdbID;
    private Marker zebrafishGene;
    private Set<OrthologEvidence> evidenceSet;
    private NcbiOtherSpeciesGene ncbiOtherSpeciesGene;
    private SortedSet<OrthologExternalReference> externalReferenceList;

    private String name;
    private String symbol;
    private String chromosome;
    private org.zfin.Species organism;
    private boolean obsolete;

    public NcbiOtherSpeciesGene getNcbiOtherSpeciesGene() {
        return ncbiOtherSpeciesGene;
    }

    public void setNcbiOtherSpeciesGene(NcbiOtherSpeciesGene ncbiGene) {
        this.ncbiOtherSpeciesGene = ncbiGene;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Marker getZebrafishGene() {
        return zebrafishGene;
    }

    public void setZebrafishGene(Marker gene) {
        this.zebrafishGene = gene;
    }


    public SortedSet<OrthologExternalReference> getExternalReferenceList() {
        return externalReferenceList;
    }

    public void setExternalReferenceList(SortedSet<OrthologExternalReference> externalReferenceList) {
        this.externalReferenceList = externalReferenceList;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public org.zfin.Species getOrganism() {
        return organism;
    }

    public void setOrganism(Species organism) {
        this.organism = organism;
    }

    public int compareTo(Object o) {
        //todo: ordering method doesn't use a zero padded attribute
        return getNcbiOtherSpeciesGene().getAbbreviation().compareTo(((Ortholog) o).getNcbiOtherSpeciesGene().getAbbreviation());
    }

    public Set<OrthologEvidence> getEvidenceSet() {
        return evidenceSet;
    }

    public void setEvidenceSet(Set<OrthologEvidence> evidenceSet) {
        this.evidenceSet = evidenceSet;
    }

    public String toString() {
        String lineFeed = System.getProperty("line.separator");
        return "ORTHOLOG" + lineFeed +
                "zdbID: " + zdbID + lineFeed +
                "abbreviation: " + ncbiOtherSpeciesGene.getAbbreviation() + lineFeed +
                "name: " + ncbiOtherSpeciesGene.getName() + lineFeed +
                "gene: " + zebrafishGene + lineFeed +
                "organism: " + ncbiOtherSpeciesGene.getOrganism().toString() + lineFeed + evidenceSet;
    }

}

