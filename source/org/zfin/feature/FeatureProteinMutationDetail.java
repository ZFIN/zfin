package org.zfin.feature;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.sequence.ReferenceDatabase;

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
    @OneToOne
    @JoinColumn(name = "fpmd_feature_zdb_id")
    private Feature feature;
    @Column(name = "fpmd_protein_position_start")
    private Integer proteinPositionStart;
    @Column(name = "fpmd_protein_position_end")
    private Integer proteinPositionEnd;
    @Column(name = "fpmd_protein_sequence_of_reference_accession_number")
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
    private AminoAcidTerm wildtypeProtein;
    @ManyToOne
    @JoinColumn(name = "fpmd_mutant_or_stop_protein_term_zdb_id")
    private AminoAcidTerm mutantProtein;

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public int getNumberAminoAcidsAdded() {
        return numberAminoAcidsAdded;
    }

    public void setNumberAminoAcidsAdded(int numberAminoAcidsAdded) {
        this.numberAminoAcidsAdded = numberAminoAcidsAdded;
    }

    public int getNumberAminoAcidsRemoved() {
        return numberAminoAcidsRemoved;
    }

    public void setNumberAminoAcidsRemoved(int numberAminoAcidsRemoved) {
        this.numberAminoAcidsRemoved = numberAminoAcidsRemoved;
    }

    public int getProteinPositionEnd() {
        return proteinPositionEnd;
    }

    public void setProteinPositionEnd(int proteinPositionEnd) {
        this.proteinPositionEnd = proteinPositionEnd;
    }

    public int getProteinPositionStart() {
        return proteinPositionStart;
    }

    public void setProteinPositionStart(int proteinPositionStart) {
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

    public AminoAcidTerm getMutantProtein() {
        return mutantProtein;
    }

    public void setMutantProtein(AminoAcidTerm mutantProtein) {
        this.mutantProtein = mutantProtein;
    }

    public AminoAcidTerm getWildtypeProtein() {
        return wildtypeProtein;
    }

    public void setWildtypeProtein(AminoAcidTerm wildtypeProtein) {
        this.wildtypeProtein = wildtypeProtein;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
