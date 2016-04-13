package org.zfin.feature;

import org.zfin.ontology.GenericTerm;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "dna_mutation_term")
public class DnaMutationTerm implements Serializable {

    @Id
    @Column(name = "dmt_term_zdb_id")
    private String termZdbID;
    @OneToOne
    @JoinColumn(name = "dmt_term_zdb_id")
    private GenericTerm mutationTerm;
    @Column(name = "dmt_term_display_name")
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public GenericTerm getMutationTerm() {
        return mutationTerm;
    }

    public void setMutationTerm(GenericTerm mutationTerm) {
        this.mutationTerm = mutationTerm;
    }

    public String getTermZdbID() {
        return termZdbID;
    }

    public void setTermZdbID(String termZdbID) {
        this.termZdbID = termZdbID;
    }
}
