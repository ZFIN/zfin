package org.zfin.feature;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.sequence.ReferenceDatabase;

import javax.persistence.*;

@Entity
@Table(name = "feature_dna_mutation_detail")
public class FeatureDnaMutationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "FDMD"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "fdmd_zdb_id")
    private String zdbID;
    @OneToOne
    @JoinColumn(name = "fdmd_feature_zdb_id")
    private Feature feature;
    @Column(name = "fdmd_dna_position_start")
    private Integer dnaPositionStart;
    @Column(name = "fdmd_dna_position_end")
    private Integer dnaPositionEnd;
    @Column(name = "fdmd_dna_sequence_of_reference_accession_number")
    private String dnaSequenceReferenceAccessionNumber;
    @ManyToOne
    @JoinColumn(name = "fdmd_fdbcont_zdb_id")
    private ReferenceDatabase referenceDatabase;
    @Column(name = "fdmd_number_additional_dna_base_pairs")
    private Integer numberAdditionalBasePair;
    @Column(name = "fdmd_number_removed_dna_base_pairs")
    private Integer numberRemovedBasePair;
    @Column(name = "fdmd_exon_number")
    private Integer exonNumber;
    @Column(name = "fdmd_intron_number")
    private Integer intronNumber;
    @ManyToOne
    @JoinColumn(name = "fdmd_dna_mutation_term_zdb_id")
    private DnaMutationTerm dnaMutationTerm;


    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

        public ReferenceDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    public void setReferenceDatabase(ReferenceDatabase referenceDatabase) {
        this.referenceDatabase = referenceDatabase;
    }

    public DnaMutationTerm getDnaMutationTerm() {
        return dnaMutationTerm;
    }

    public void setDnaMutationTerm(DnaMutationTerm dnaMutationTerm) {
        this.dnaMutationTerm = dnaMutationTerm;
    }

    public int getDnaPositionEnd() {
        return dnaPositionEnd;
    }

    public void setDnaPositionEnd(int dnaPositionEnd) {
        this.dnaPositionEnd = dnaPositionEnd;
    }

    public int getDnaPositionStart() {
        return dnaPositionStart;
    }

    public void setDnaPositionStart(int dnaPositionStart) {
        this.dnaPositionStart = dnaPositionStart;
    }

    public String getDnaSequenceReferenceAccessionNumber() {
        return dnaSequenceReferenceAccessionNumber;
    }

    public void setDnaSequenceReferenceAccessionNumber(String dnaSequenceReferenceAccessionNumber) {
        this.dnaSequenceReferenceAccessionNumber = dnaSequenceReferenceAccessionNumber;
    }

    public int getNumberAdditionalBasePair() {
        return numberAdditionalBasePair;
    }

    public void setNumberAdditionalBasePair(int numberAdditionalBasePair) {
        this.numberAdditionalBasePair = numberAdditionalBasePair;
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

    public int getNumberRemovedBasePair() {
        return numberRemovedBasePair;
    }

    public void setNumberRemovedBasePair(int numberRemovedBasePair) {
        this.numberRemovedBasePair = numberRemovedBasePair;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
