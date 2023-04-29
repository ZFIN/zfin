package org.zfin.mutant;

import lombok.Getter;
import lombok.Setter;
import org.zfin.ontology.HumanGeneDetail;
import org.zfin.ontology.TermExternalReference;
import org.zfin.orthology.Ortholog;

import java.io.Serializable;
import java.util.Set;

@Setter
@Getter
public class OmimPhenotype implements Comparable<OmimPhenotype>, Serializable {
    private long id;
    private String name;
    private String omimNum;
    private Ortholog ortholog;
    private Set<TermExternalReference> externalReferences;
    private String humanGeneMimNumber;

    private HumanGeneDetail humanGeneDetail;

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

    public String getHumanGeneMimNumber() {
        return humanGeneMimNumber;
    }

    public void setHumanGeneMimNumber(String humanGeneMimNumber) {
        this.humanGeneMimNumber = humanGeneMimNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof OmimPhenotype anotherOmimPhenotype) {
            if (anotherOmimPhenotype == null) {
                return false;
            }

            if(Long.valueOf(anotherOmimPhenotype.getId()) != null && Long.valueOf(id) != null && anotherOmimPhenotype.getId() == id) {
                return true;
            }

            if (anotherOmimPhenotype.getName() != null && anotherOmimPhenotype.getOmimNum() != null) {
                if (anotherOmimPhenotype.getName().equals(this.name) && anotherOmimPhenotype.getOmimNum().equals(this.omimNum)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (Long.valueOf(id) != null) {
            result = (int) id;
        }
        result = 51 * result + name.hashCode();
        if (omimNum != null) {
            result = 31 * result + omimNum.hashCode();
        }
        if (ortholog != null)
            result = 31 * result + ortholog.getZdbID().hashCode();
        return result;
    }
}
