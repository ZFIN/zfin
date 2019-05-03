package org.zfin.feature;

import org.hibernate.annotations.GenericGenerator;
import javax.persistence.*;

@Entity
@Table(name = "feature_genomic_mutation_detail")
public class FeatureGenomicMutationDetail implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "FGMD"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "fgmd_zdb_id")
    private String zdbID;
    @ManyToOne
    @JoinColumn(name = "fgmd_feature_zdb_id")
    private Feature feature;
    @Column(name = "fgmd_sequence_of_reference")
    private String fgmdSeqRef;
    @Column(name = "fgmd_sequence_of_variation")
    private String fgmdSeqVar;
    @Column(name = "fgmd_sequence_of_reference_accession_number")
    private String fgmdSequenceReferenceAccessionNumber;
    @Column(name = "fgmd_variation_strand")
    private String fgmdVarStrand;
    @Column(name = "fgmd_padded_base")
    private String fgmdPaddedBase;


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

    public String getFgmdSeqRef() {
        return fgmdSeqRef;
    }

    public void setFgmdSeqRef(String fgmdSeqRef) {
        this.fgmdSeqRef = fgmdSeqRef;
    }

    public String getFgmdSeqVar() {
        return fgmdSeqVar;
    }

    public void setFgmdSeqVar(String fgmdSeqVar) {
        this.fgmdSeqVar = fgmdSeqVar;
    }

    public String getFgmdSequenceReferenceAccessionNumber() {
        return fgmdSequenceReferenceAccessionNumber;
    }

    public void setFgmdSequenceReferenceAccessionNumber(String fgmdSequenceReferenceAccessionNumber) {
        this.fgmdSequenceReferenceAccessionNumber = fgmdSequenceReferenceAccessionNumber;
    }

    public String getFgmdVarStrand() {
        return fgmdVarStrand;
    }

    public void setFgmdVarStrand(String fgmdVarStrand) {
        this.fgmdVarStrand = fgmdVarStrand;
    }

    public String getFgmdPaddedBase() {
        return fgmdPaddedBase;
    }

    public void setFgmdPaddedBase(String fgmdPaddedBase) {
        this.fgmdPaddedBase = fgmdPaddedBase;
    }


    public FeatureGenomicMutationDetail clone() {
        try {
            FeatureGenomicMutationDetail detail = (FeatureGenomicMutationDetail) super.clone();

            return detail;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeatureGenomicMutationDetail that = (FeatureGenomicMutationDetail) o;

        if (fgmdSeqRef != null ? !fgmdSeqRef.equals(that.fgmdSeqRef) : that.fgmdSeqRef != null)
            return false;
        if (fgmdSeqVar != null ? !fgmdSeqVar.equals(that.fgmdSeqVar) : that.fgmdSeqVar != null)
            return false;
        if (fgmdSequenceReferenceAccessionNumber != null ? !fgmdSequenceReferenceAccessionNumber.equals(that.fgmdSequenceReferenceAccessionNumber) : that.fgmdSequenceReferenceAccessionNumber != null)
            return false;
        if (fgmdPaddedBase != null ? !fgmdPaddedBase.equals(that.fgmdPaddedBase) : that.fgmdPaddedBase != null)
            return false;
        return (fgmdVarStrand != null ? !fgmdVarStrand.equals(that.fgmdVarStrand) : that.fgmdVarStrand != null);

    }

    @Override
    public int hashCode() {
        int result = fgmdSeqRef != null ? fgmdSeqRef.hashCode() : 0;
        result = 31 * result + (fgmdSeqVar != null ? fgmdSeqVar.hashCode() : 0);
        result = 31 * result + (fgmdSequenceReferenceAccessionNumber != null ? fgmdSequenceReferenceAccessionNumber.hashCode() : 0);
        result = 31 * result + (fgmdPaddedBase != null ? fgmdPaddedBase.hashCode() : 0);
        result = 31 * result + (fgmdVarStrand != null ? fgmdVarStrand.hashCode() : 0);

        return result;
    }
    
}
