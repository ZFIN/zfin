package org.zfin.feature;

import org.hibernate.annotations.GenericGenerator;

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
    @JoinColumn(name = "frmd_feature_zdb_id")
    private Feature feature;
    @ManyToOne
    @JoinColumn(name = "frmd_rna_consequence_term_zdb_id")
    private RnaConsequence rnaConsequence;
    @Transient
    private Integer intronNumber;
    @Transient
    private Integer exonNumber   ;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public RnaConsequence getRnaConsequence() {
        return rnaConsequence;
    }

    public void setRnaConsequence(RnaConsequence rnaConsequence) {
        this.rnaConsequence = rnaConsequence;
    }

    public Integer getExonNumber() {
        return exonNumber;
    }

    public void setExonNumber(Integer exonNumber) {
        this.exonNumber = exonNumber;
    }

    public Integer getIntronNumber() {
        return intronNumber;
    }

    public void setIntronNumber(Integer intronNumber) {
        this.intronNumber = intronNumber;
    }
}
