package org.zfin.mutant;

import org.zfin.ontology.TermExternalReference;
import org.zfin.orthology.Ortholog;

import java.util.Set;

/**
 * OMIM Phenotype
 */
public class OmimPhenotype implements Comparable<OmimPhenotype> {
    private long id;
    private String name;
    private String omimNum;
    private Ortholog ortholog;
    private Set<TermExternalReference> externalReferences;

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

    public Ortholog getOrtholog() {
        return ortholog;
    }

    public void setOrtholog(Ortholog ortholog) {
        this.ortholog = ortholog;
    }

    public Set<TermExternalReference> getExternalReferences() {
        return externalReferences;
    }

    public void setExternalReferences(Set<TermExternalReference> externalReferences) {
        this.externalReferences = externalReferences;
    }

    @Override
    public int compareTo(OmimPhenotype anotherOmimPhenotype) {

        if (getOmimNum() == null && anotherOmimPhenotype.getOmimNum() != null) {
            return 1;
        } else if (getOmimNum() != null && anotherOmimPhenotype.getOmimNum() == null) {
            return -1;
        }

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
            if (getName().equals(anotherName)) {
                if (getOmimNum() == null && anotherOmimPhenotype.getOmimNum() == null) {
                    return 1;
                } else {
                    return getOmimNum().compareTo(anotherOmimPhenotype.getOmimNum());
                }
            } else {
                return getName().compareToIgnoreCase(anotherName);
            }
        }
    }

}
