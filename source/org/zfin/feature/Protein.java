package org.zfin.feature;

import org.zfin.ontology.GenericTerm;

import javax.persistence.*;

@Entity
@Table(name = "protein_controlled_vocabulary")
public class Protein {

    @Id
    @Column(name = "pcv_term_zdb_id")
    private String proteinID;
    @OneToOne
    @JoinColumn(name = "pcv_term_zdb_id")
    private GenericTerm proteinTerm;
    @Column(name = "pcv_term_display_name")
    private String displayName;
    @Column(name = "pcv_term_abbreviation")
    private String abbreviation;

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProteinID() {
        return proteinID;
    }

    public void setProteinID(String proteinID) {
        this.proteinID = proteinID;
    }

    public GenericTerm getProteinTerm() {
        return proteinTerm;
    }

    public void setProteinTerm(GenericTerm proteinTerm) {
        this.proteinTerm = proteinTerm;
    }
}
