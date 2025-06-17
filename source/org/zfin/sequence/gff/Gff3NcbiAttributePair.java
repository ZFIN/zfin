package org.zfin.sequence.gff;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.framework.entity.BaseEntity;

@Setter
@Getter
@Entity
@Table(name = "gff3_ncbi_attribute")
public class Gff3NcbiAttributePair extends BaseEntity {

    @Id
    @GeneratedValue(generator = "sequence-generator")
    @GenericGenerator(
        name = "sequence-generator",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "gff3_ncbi_attribute_seq"),
            @org.hibernate.annotations.Parameter(name = "initial_value", value = "100"),
            @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
        }
    )
    @Column(name = "gna_pk_id", nullable = false)
    private long id;

    @ManyToOne
    @JoinColumn(name = "gna_gff_pk_id")
    private Gff3Ncbi gff3Ncbi;

    @Column(name = "gna_key")
    private String key;

    @Column(name = "gna_value")
    private String value;


}


