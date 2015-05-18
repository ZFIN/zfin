package org.zfin.ontology;

import org.zfin.marker.Marker;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.orthology.Orthologue;
import org.zfin.orthology.Orthology;
import org.zfin.sequence.DBLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * OMIM Phenotype
 */
public class OmimPhenotypeDisplay {

    private String name;
    private String omimNum;
    private ArrayList<String> humanGene;

    public DBLink getHumanAccession() {
        return humanAccession;
    }

    public void setHumanAccession(DBLink humanAccession) {
        this.humanAccession = humanAccession;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getHumanGene() {
        return humanGene;
    }

    public void setHumanGene(ArrayList<String> humanGene) {
        this.humanGene = humanGene;
    }

    public String getOmimNum() {
        return omimNum;
    }

    public void setOmimNum(String omimNum) {
        this.omimNum = omimNum;
    }

    public Orthologue getOrthology() {
        return orthology;
    }

    public void setOrthology(Orthologue orthology) {
        this.orthology = orthology;
    }

    private Orthologue orthology;
    private DBLink humanAccession;
    private List<Marker> zfinGene;

    public List<Marker> getZfinGene() {
        return zfinGene;
    }

    public void setZfinGene(List<Marker> zfinGene) {
        this.zfinGene = zfinGene;
    }
}
