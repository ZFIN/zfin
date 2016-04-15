package org.zfin.feature;

import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "feature_transcript_mutation_detail")
public class FeatureTranscriptMutationDetail implements Comparable<FeatureTranscriptMutationDetail> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "zfinGenerator")
    @GenericGenerator(name = "zfinGenerator",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "FRMD"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "ftmd_zdb_id")
    private String zdbID;
    @ManyToOne
    @JoinColumn(name = "ftmd_feature_zdb_id")
    private Feature feature;
    @ManyToOne
    @JoinColumn(name = "ftmd_transcript_consequence_term_zdb_id")
    private TranscriptConsequence transcriptConsequence;
    @Column(name = "ftmd_introl_number")
    private Integer intronNumber;
    @Column(name = "ftmd_exon_number")
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

    public TranscriptConsequence getTranscriptConsequence() {
        return transcriptConsequence;
    }

    public void setTranscriptConsequence(TranscriptConsequence transcriptConsequence) {
        this.transcriptConsequence = transcriptConsequence;
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

    @Override
    public int compareTo(FeatureTranscriptMutationDetail o) {
        return ObjectUtils.compare(transcriptConsequence, o.transcriptConsequence);
    }
}
