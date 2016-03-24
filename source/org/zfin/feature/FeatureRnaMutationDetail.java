package org.zfin.feature;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.ontology.GenericTerm;

import javax.persistence.*;

@Entity
@Table(name = "feature_rna_mutation_detail")
public class FeatureRnaMutationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "FRMD"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "frmd_zdb_id")
    private String zdbID;
    @ManyToOne
    @JoinColumn(name = "frmd_rna_consequence_term_zdb_id")
    private GenericTerm rnaConsequence;
    @ManyToOne
    @JoinColumn(name = "frmd_feature_zdb_id")
    private Feature feature;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public GenericTerm getRnaConsequence() {
        return rnaConsequence;
    }

    public void setRnaConsequence(GenericTerm rnaConsequence) {
        this.rnaConsequence = rnaConsequence;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
