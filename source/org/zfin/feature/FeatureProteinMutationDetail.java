package org.zfin.feature;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.sequence.ReferenceDatabase;

import javax.persistence.*;

@Entity
@Table(name = "feature_protein_mutation_detail")
public class FeatureProteinMutationDetail implements Cloneable {

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
    @OneToOne
    @JoinColumn(name = "fpmd_feature_zdb_id")
    private Feature feature;
    @ManyToOne
    @JoinColumn(name = "fpmd_protein_consequence_term_zdb_id")
    private ProteinConsequence proteinConsequence;
    @Column(name = "fpmd_protein_position_start")
    private Integer proteinPositionStart;
    @Column(name = "fpmd_protein_position_end")
    private Integer proteinPositionEnd;
    @Column(name = "fpmd_sequence_of_reference_accession_number")
    private String proteinSequenceReferenceAccessionNumber;
    @ManyToOne
    @JoinColumn(name = "fpmd_fdbcont_zdb_id")
    private ReferenceDatabase referenceDatabase;
    @Column(name = "fpmd_number_amino_acids_removed")
    private Integer numberAminoAcidsRemoved;
    @Column(name = "fpmd_number_amino_acids_added")
    private Integer numberAminoAcidsAdded;
    @ManyToOne
    @JoinColumn(name = "fpmd_wt_protein_term_zdb_id")
    private AminoAcidTerm wildtypeAminoAcid;
    @ManyToOne
    @JoinColumn(name = "fpmd_mutant_or_stop_protein_term_zdb_id")
    private AminoAcidTerm mutantAminoAcid;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public ProteinConsequence getProteinConsequence() {
        return proteinConsequence;
    }

    public void setProteinConsequences(ProteinConsequence proteinConsequence) {
        this.proteinConsequence = proteinConsequence;
    }

    public Integer getNumberAminoAcidsAdded() {
        return numberAminoAcidsAdded;
    }

    public void setNumberAminoAcidsAdded(Integer numberAminoAcidsAdded) {
        this.numberAminoAcidsAdded = numberAminoAcidsAdded;
    }

    public Integer getNumberAminoAcidsRemoved() {
        return numberAminoAcidsRemoved;
    }

    public void setNumberAminoAcidsRemoved(Integer numberAminoAcidsRemoved) {
        this.numberAminoAcidsRemoved = numberAminoAcidsRemoved;
    }

    public Integer getProteinPositionEnd() {
        return proteinPositionEnd;
    }

    public void setProteinPositionEnd(Integer proteinPositionEnd) {
        this.proteinPositionEnd = proteinPositionEnd;
    }

    public Integer getProteinPositionStart() {
        return proteinPositionStart;
    }

    public void setProteinPositionStart(Integer proteinPositionStart) {
        this.proteinPositionStart = proteinPositionStart;
    }

    public String getProteinSequenceReferenceAccessionNumber() {
        return proteinSequenceReferenceAccessionNumber;
    }

    public void setProteinSequenceReferenceAccessionNumber(String proteinSequenceReferenceAccessionNumber) {
        this.proteinSequenceReferenceAccessionNumber = proteinSequenceReferenceAccessionNumber;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    public void setReferenceDatabase(ReferenceDatabase referenceDatabase) {
        this.referenceDatabase = referenceDatabase;
    }

    public AminoAcidTerm getMutantAminoAcid() {
        return mutantAminoAcid;
    }

    public void setMutantAminoAcid(AminoAcidTerm mutantProtein) {
        this.mutantAminoAcid = mutantProtein;
    }

    public AminoAcidTerm getWildtypeAminoAcid() {
        return wildtypeAminoAcid;
    }

    public void setWildtypeAminoAcid(AminoAcidTerm wildtypeProtein) {
        this.wildtypeAminoAcid = wildtypeProtein;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public FeatureProteinMutationDetail clone() {
        try {
            FeatureProteinMutationDetail detail = (FeatureProteinMutationDetail) super.clone();
            detail.proteinConsequence = (ProteinConsequence) proteinConsequence.clone();
            detail.wildtypeAminoAcid = (AminoAcidTerm) wildtypeAminoAcid.clone();
            detail.mutantAminoAcid = (AminoAcidTerm) mutantAminoAcid.clone();
            return detail;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeatureProteinMutationDetail that = (FeatureProteinMutationDetail) o;

        if (zdbID != null ? !zdbID.equals(that.zdbID) : that.zdbID != null) return false;
        if (proteinConsequence != null ? !proteinConsequence.equals(that.proteinConsequence) : that.proteinConsequence != null)
            return false;
        if (proteinPositionStart != null ? !proteinPositionStart.equals(that.proteinPositionStart) : that.proteinPositionStart != null)
            return false;
        if (proteinPositionEnd != null ? !proteinPositionEnd.equals(that.proteinPositionEnd) : that.proteinPositionEnd != null)
            return false;
        if (proteinSequenceReferenceAccessionNumber != null ? !proteinSequenceReferenceAccessionNumber.equals(that.proteinSequenceReferenceAccessionNumber) : that.proteinSequenceReferenceAccessionNumber != null)
            return false;
        if (referenceDatabase != null ? !referenceDatabase.equals(that.referenceDatabase) : that.referenceDatabase != null)
            return false;
        if (numberAminoAcidsRemoved != null ? !numberAminoAcidsRemoved.equals(that.numberAminoAcidsRemoved) : that.numberAminoAcidsRemoved != null)
            return false;
        if (numberAminoAcidsAdded != null ? !numberAminoAcidsAdded.equals(that.numberAminoAcidsAdded) : that.numberAminoAcidsAdded != null)
            return false;
        if (wildtypeAminoAcid != null ? !wildtypeAminoAcid.equals(that.wildtypeAminoAcid) : that.wildtypeAminoAcid != null)
            return false;
        return !(mutantAminoAcid != null ? !mutantAminoAcid.equals(that.mutantAminoAcid) : that.mutantAminoAcid != null);

    }

    @Override
    public int hashCode() {
        int result = zdbID != null ? zdbID.hashCode() : 0;
        result = 31 * result + (proteinConsequence != null ? proteinConsequence.hashCode() : 0);
        result = 31 * result + (proteinPositionStart != null ? proteinPositionStart.hashCode() : 0);
        result = 31 * result + (proteinPositionEnd != null ? proteinPositionEnd.hashCode() : 0);
        result = 31 * result + (proteinSequenceReferenceAccessionNumber != null ? proteinSequenceReferenceAccessionNumber.hashCode() : 0);
        result = 31 * result + (referenceDatabase != null ? referenceDatabase.hashCode() : 0);
        result = 31 * result + (numberAminoAcidsRemoved != null ? numberAminoAcidsRemoved.hashCode() : 0);
        result = 31 * result + (numberAminoAcidsAdded != null ? numberAminoAcidsAdded.hashCode() : 0);
        result = 31 * result + (wildtypeAminoAcid != null ? wildtypeAminoAcid.hashCode() : 0);
        result = 31 * result + (mutantAminoAcid != null ? mutantAminoAcid.hashCode() : 0);
        return result;
    }
}
