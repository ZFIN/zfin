package org.zfin.mutant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.ontology.HumanGeneDetail;
import org.zfin.ontology.TermExternalReference;
import org.zfin.orthology.Ortholog;

import java.io.Serializable;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "omim_phenotype")
public class OmimPhenotype implements Comparable<OmimPhenotype>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "omimp_pk_id")
    private long id;
    @Column(name = "omimp_name")
    private String name;
    @Column(name = "omimp_omim_id")
    private String omimNum;
    @ManyToOne
    @JoinColumn(name = "omimp_ortho_zdb_id")
    private Ortholog ortholog;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "omimp_termxref_mapping",
            joinColumns = @JoinColumn(name = "otm_omimp_id"),
            inverseJoinColumns = @JoinColumn(name = "otm_tx_id"))
    private Set<TermExternalReference> externalReferences;

    @Column(name = "omimp_human_gene_id")
    private String humanGeneMimNumber;

    @Transient
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

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof OmimPhenotype anotherOmimPhenotype) {

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
