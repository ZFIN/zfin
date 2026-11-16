package org.zfin.mapping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.feature.Variant;

@Entity
@Table(name = "variant_flanking_sequence")
@Setter
@Getter
public class VariantSequence {

    @Id
    @Column(name = "vfseq_zdb_id")
    @GeneratedValue(generator = "zdbIdGeneratorForVariantSequence")
    @org.hibernate.annotations.GenericGenerator(
            name = "zdbIdGeneratorForVariantSequence",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "type", value = "VFSEQ"),
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            }
    )
    private String zdbID;

    /** The variant whose mutation this flanking sequence surrounds (ZFIN-10622). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vfseq_variant_zdb_id", nullable = false)
    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private Variant variant;

    @Column(name = "vfseq_sequence")
    private String vfsTargetSequence;

    @Column(name = "vfseq_offset_start")
    private int vfsOffsetStart;

    @Column(name = "vfseq_offset_stop")
    private int vfsOffsetStop;

    @Column(name = "vfseq_variation")
    private String vfsVariation;

    @Column(name = "vfseq_five_prime_flanking_sequence")
    private String vfsLeftEnd;

    @Column(name = "vfseq_three_prime_flanking_sequence")
    private String vfsRightEnd;

    @Column(name = "vfseq_type")
    private String vfsType;

    @Column(name = "vfseq_flanking_sequence_type")
    private String vfsFlankType;

    @Column(name = "vfseq_flanking_sequence_origin")
    private String vfsFlankOrigin;
}