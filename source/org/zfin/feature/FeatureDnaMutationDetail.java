package org.zfin.feature;

import org.hibernate.annotations.GenericGenerator;
import org.zfin.sequence.ReferenceDatabase;

import javax.persistence.*;

@Entity
@Table(name = "feature_dna_mutation_detail")
public class FeatureDnaMutationDetail implements Cloneable {

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
    private Integer numberAddedBasePair;
    @Column(name = "fdmd_number_removed_dna_base_pairs")
    private Integer numberRemovedBasePair;
    @Column(name = "fdmd_exon_number")
    private Integer exonNumber;
    @Column(name = "fdmd_intron_number")
    private Integer intronNumber;
    @ManyToOne
    @JoinColumn(name = "fdmd_gene_localization_term_zdb_id")
    private GeneLocalizationTerm geneLocalizationTerm;
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

    public Integer getDnaPositionEnd() {
        return dnaPositionEnd;
    }

    public void setDnaPositionEnd(Integer dnaPositionEnd) {
        this.dnaPositionEnd = dnaPositionEnd;
    }

    public Integer getDnaPositionStart() {
        return dnaPositionStart;
    }

    public void setDnaPositionStart(Integer dnaPositionStart) {
        this.dnaPositionStart = dnaPositionStart;
    }

    public String getDnaSequenceReferenceAccessionNumber() {
        return dnaSequenceReferenceAccessionNumber;
    }

    public void setDnaSequenceReferenceAccessionNumber(String dnaSequenceReferenceAccessionNumber) {
        this.dnaSequenceReferenceAccessionNumber = dnaSequenceReferenceAccessionNumber;
    }

    public Integer getNumberAddedBasePair() {
        return numberAddedBasePair;
    }

    public void setNumberAddedBasePair(Integer numberAdditionalBasePair) {
        this.numberAddedBasePair = numberAdditionalBasePair;
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

    public Integer getNumberRemovedBasePair() {
        return numberRemovedBasePair;
    }

    public void setNumberRemovedBasePair(Integer numberRemovedBasePair) {
        this.numberRemovedBasePair = numberRemovedBasePair;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public GeneLocalizationTerm getGeneLocalizationTerm() {
        return geneLocalizationTerm;
    }

    public void setGeneLocalizationTerm(GeneLocalizationTerm geneLocalizationTerm) {
        this.geneLocalizationTerm = geneLocalizationTerm;
    }

    public FeatureDnaMutationDetail clone() {
        try {
            FeatureDnaMutationDetail detail = (FeatureDnaMutationDetail) super.clone();
            if (dnaMutationTerm != null)
                detail.dnaMutationTerm = (DnaMutationTerm) dnaMutationTerm.clone();
            if (geneLocalizationTerm != null)
                detail.geneLocalizationTerm = (GeneLocalizationTerm) geneLocalizationTerm.clone();
            return detail;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeatureDnaMutationDetail that = (FeatureDnaMutationDetail) o;

        if (dnaPositionStart != null ? !dnaPositionStart.equals(that.dnaPositionStart) : that.dnaPositionStart != null)
            return false;
        if (dnaPositionEnd != null ? !dnaPositionEnd.equals(that.dnaPositionEnd) : that.dnaPositionEnd != null)
            return false;
        if (dnaSequenceReferenceAccessionNumber != null ? !dnaSequenceReferenceAccessionNumber.equals(that.dnaSequenceReferenceAccessionNumber) : that.dnaSequenceReferenceAccessionNumber != null)
            return false;
        if (referenceDatabase != null ? !referenceDatabase.equals(that.referenceDatabase) : that.referenceDatabase != null)
            return false;
        if (numberAddedBasePair != null ? !numberAddedBasePair.equals(that.numberAddedBasePair) : that.numberAddedBasePair != null)
            return false;
        if (numberRemovedBasePair != null ? !numberRemovedBasePair.equals(that.numberRemovedBasePair) : that.numberRemovedBasePair != null)
            return false;
        if (exonNumber != null ? !exonNumber.equals(that.exonNumber) : that.exonNumber != null) return false;
        if (intronNumber != null ? !intronNumber.equals(that.intronNumber) : that.intronNumber != null) return false;
        if (geneLocalizationTerm != null ? !geneLocalizationTerm.equals(that.geneLocalizationTerm) : that.geneLocalizationTerm != null)
            return false;
        return !(dnaMutationTerm != null ? !dnaMutationTerm.equals(that.dnaMutationTerm) : that.dnaMutationTerm != null);

    }

    @Override
    public int hashCode() {
        int result = dnaPositionStart != null ? dnaPositionStart.hashCode() : 0;
        result = 31 * result + (dnaPositionEnd != null ? dnaPositionEnd.hashCode() : 0);
        result = 31 * result + (dnaSequenceReferenceAccessionNumber != null ? dnaSequenceReferenceAccessionNumber.hashCode() : 0);
        result = 31 * result + (referenceDatabase != null ? referenceDatabase.hashCode() : 0);
        result = 31 * result + (numberAddedBasePair != null ? numberAddedBasePair.hashCode() : 0);
        result = 31 * result + (numberRemovedBasePair != null ? numberRemovedBasePair.hashCode() : 0);
        result = 31 * result + (exonNumber != null ? exonNumber.hashCode() : 0);
        result = 31 * result + (intronNumber != null ? intronNumber.hashCode() : 0);
        result = 31 * result + (geneLocalizationTerm != null ? geneLocalizationTerm.hashCode() : 0);
        result = 31 * result + (dnaMutationTerm != null ? dnaMutationTerm.hashCode() : 0);
        return result;
    }

}
