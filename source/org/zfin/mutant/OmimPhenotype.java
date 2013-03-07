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
    public int compareTo(OmimPhenotype anotherOmimPhenotype ) {
        String bracket = "[";
        String brace = "{";
        String questionMark = "?";

        String anotherName = anotherOmimPhenotype.getName();

        // those without any brackets [ ], braces { }, or question markers are listed alphabetically first
        if ( (getName().startsWith(bracket) || getName().startsWith(brace) || getName().startsWith(questionMark) ) && !(anotherName.startsWith(bracket) || anotherName.startsWith(brace) || anotherName.startsWith(questionMark) ) ) {
            return 1;
        } else if ( !(getName().startsWith(bracket) || getName().startsWith(brace) || getName().startsWith(questionMark) ) && (anotherName.startsWith(bracket) || anotherName.startsWith(brace) || anotherName.startsWith(questionMark) ) ) {
            return -1;
        } else {
            return getName().compareToIgnoreCase(anotherName);
        }
    }

}
