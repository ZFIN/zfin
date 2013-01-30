package org.zfin.mutant;

import org.zfin.marker.Marker;

/**
 * OMIM Phenotype
 */
public class OmimPhenotype implements Comparable<OmimPhenotype> {
    private long id;
    private String name;
    private String omimNum;
    private Marker zfGene;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOmimNum() {
        return omimNum;
    }

    public void setOmimNum(String omimNum) {
        this.omimNum = omimNum;
    }

    public Marker getZfGene() {
        return zfGene;
    }

    public void setZfGene(Marker zfGene) {
        this.zfGene = zfGene;
    }


    @Override
    public int compareTo(OmimPhenotype OmimPhenotype) {
            return getName().compareToIgnoreCase(OmimPhenotype.getName());
    }

}
