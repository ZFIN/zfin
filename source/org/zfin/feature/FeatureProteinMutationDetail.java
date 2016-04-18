package org.zfin.feature;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.sequence.ReferenceDatabase;

import javax.persistence.*;
import java.util.Set;

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
    @ManyToMany
    @JoinTable(name = "feature_protein_consequence",
            joinColumns = {@JoinColumn(name = "fpc_protein_consequence_term_zdb_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "fpc_fpmd_zdb_id", nullable = false, updatable = false)})
    private Set<ProteinConsequence> proteinConsequences;
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

    public Set<ProteinConsequence> getProteinConsequences() {
        return proteinConsequences;
    }

    public void setProteinConsequences(Set<ProteinConsequence> proteinConsequences) {
        this.proteinConsequences = proteinConsequences;
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
}
