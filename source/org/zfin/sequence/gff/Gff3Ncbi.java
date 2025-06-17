package org.zfin.sequence.gff;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.framework.entity.BaseEntity;

import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "gff3_ncbi")
public class Gff3Ncbi extends BaseEntity {

    @Id
    @GeneratedValue(generator = "sequence-generator")
    @GenericGenerator(
        name = "sequence-generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "gff3_ncbi_seq"),
            @org.hibernate.annotations.Parameter(name = "initial_value", value = "100"),
            @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
        }
    )
    @Column(name = "gff_pk_id", nullable = false)
    private long id;

    @Column(name = "gff_seqname")
    private String chromosome;

    @Column(name = "gff_source")
    private String source;

    @Column(name = "gff_feature")
    private String feature;

    @Column(name = "gff_start")
    private int start;

    @Column(name = "gff_end")
    private int end;

    @Column(name = "gff_score")
    private String score;

    @Column(name = "gff_strand")
    private String strand;

    @Column(name = "gff_frame")
    private String frame;

    @Column(name = "gff_attributes")
    private String attributes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "gff3Ncbi", fetch = FetchType.LAZY)
    private Set<Gff3NcbiAttributePair> attributePairs;

}


