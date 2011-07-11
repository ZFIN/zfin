package org.zfin.orthology;

import org.zfin.marker.Marker;
import org.zfin.sequence.EntrezProtRelation;

import java.util.Set;

/**
 * This orthologue class is not (currently) used by the rest of the orthology package,
 * (search page, etc).
 * Instead, it's a more direct representation of the orthologue table in the database
 * intended to be used for easy hibernate mapping.
 */
public class Orthologue implements Comparable {

    private String zdbID;
    private Marker gene;
    // ToDo: This attribute is not yet fully used as the orthologue table
    // contains the abbreviation of the target gene and not the PK of the
    // accession table (accession_bank)
    // If we decided to change this then the abbreviation attribute could go away.
    private EntrezProtRelation accession;
    private Species organism;
    private String abbreviation;
    private String name;

    private Set<OrthoEvidence> evidences;
    private String chromosome;
    private String position;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }


    public EntrezProtRelation getAccession() {
        return accession;
    }

    public void setAccession(EntrezProtRelation accession) {
        this.accession = accession;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }

    public Species getOrganism() {
        return organism;
    }

    public void setOrganism(Species organism) {
        this.organism = organism;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int compareTo(Object o) {
        //todo: ordering method doesn't use a zero padded attribute
        return getAbbreviation().compareTo(((Orthologue) o).getAbbreviation());
    }


    public Set<OrthoEvidence> getEvidences() {
        return evidences;
    }

    public void setEvidences(Set<OrthoEvidence> evidences) {
        this.evidences = evidences;
    }

    public String toString() {
        String lineFeed = System.getProperty("line.separator");
        StringBuilder sb = new StringBuilder("ORTHOLOG");
        sb.append(lineFeed);
        sb.append("zdbID: " + zdbID);
        sb.append(lineFeed);
        sb.append("abbreviation: " + abbreviation);
        sb.append(lineFeed);
        sb.append("name: " + name);
        sb.append(lineFeed);
        sb.append("gene: " + gene);
        sb.append(lineFeed);
        sb.append("organism: " + organism);
        sb.append(lineFeed);
        sb.append(evidences);
        return sb.toString();
    }
}

