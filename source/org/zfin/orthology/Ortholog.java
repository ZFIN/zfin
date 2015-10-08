package org.zfin.orthology;

import org.zfin.marker.Marker;

import java.io.Serializable;
import java.util.Set;

public class Ortholog implements Comparable, Serializable {

    private String zdbID;
    private Marker zebrafishGene;
    private Set<OrthologEvidence> evidenceSet;
    private NcbiOtherSpeciesGene ncbiOtherSpeciesGene;

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

