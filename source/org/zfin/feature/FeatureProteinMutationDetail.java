package org.zfin.feature;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.ontology.GenericTerm;

import javax.persistence.*;

@Entity
@Table(name = "feature_protein_mutation_detail")
public class FeatureProteinMutationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "FPMD"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "fpmd_zdb_id")
    private String zdbID;
    @ManyToOne
    @JoinColumn(name = "fpmd_protein_consequence_term_zdb_id")
    private GenericTerm proteinConsequence;
    @ManyToOne
    @JoinColumn(name = "fpmd_feature_zdb_id")
    private Feature feature;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public GenericTerm getProteinConsequence() {
        return proteinConsequence;
    }

    public void setProteinConsequence(GenericTerm proteinConsequence) {
        this.proteinConsequence = proteinConsequence;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
