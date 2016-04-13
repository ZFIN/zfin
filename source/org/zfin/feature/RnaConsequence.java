package org.zfin.feature;

import org.zfin.ontology.GenericTerm;

import javax.persistence.*;

@Entity
@Table(name = "rna_consequence_term")
public class RnaConsequence {

    @Id
    @Column(name = "rct_term_zdb_id")
    private String zdbID;
    @OneToOne
    @PrimaryKeyJoinColumn
    private GenericTerm term;
    @Column(name = "rct_term_display_name")
    private String displayName;
    @Column(name = "rct_term_order")
    private Integer order;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
