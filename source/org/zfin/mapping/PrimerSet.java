package org.zfin.mapping;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;

import java.io.Serializable;

@Entity
@Table(name = "PRIMER_SET")
@Getter
@Setter
public class PrimerSet implements Serializable {

    @Id
    @GeneratedValue(generator = "PrimerSet")
    @GenericGenerator(name = "PrimerSet",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "zdb_id")
    private String zdbID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marker_id", insertable = false, updatable = false)
    private Marker marker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strain_id", insertable = false, updatable = false)
    private Genotype genotype;

    @Column(name = "fwd_primer")
    private String forwardPrimer;

    @Column(name = "rev_primer")
    private String reversePrimer;

    @Column(name = "band_size")
    private String bandSize;

    @Column(name = "restr_enzyme")
    private String restrictionEnzyme;

    @Column(name = "anneal_temp")
    private String annealingTemperature;
}
