package org.zfin.mutant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.zfin.feature.Feature;

@Getter
@Setter
@Entity
@Table(name = "genotype_feature")
public class GenotypeFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "GenotypeFeature")
    @GenericGenerator(name = "GenotypeFeature",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @Parameter(name = "type", value = "GENOFEAT"),
                    @Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "genofeat_zdb_id")
    private String zdbID;

    @ManyToOne
    @JoinColumn(name = "genofeat_zygocity")
    private Zygosity zygosity;
    @ManyToOne
    @JoinColumn(name = "genofeat_dad_zygocity")
    private Zygosity dadZygosity;
    @ManyToOne
    @JoinColumn(name = "genofeat_mom_zygocity")
    private Zygosity momZygosity;
    @ManyToOne
    @JoinColumn(name = "genofeat_geno_zdb_id")
    private Genotype genotype;
    @ManyToOne
    @JoinColumn(name = "genofeat_feature_zdb_id")
    private Feature feature;

    public String getParentalZygosityDisplay() {
        return GenotypeService.getParentalZygosityDisplay(momZygosity, dadZygosity);
    }
}
