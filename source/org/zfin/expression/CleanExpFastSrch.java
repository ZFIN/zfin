package org.zfin.expression;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.marker.Marker;
import org.zfin.mutant.FishExperiment;

/**
 * Created by prita on 12/23/2015.
 */
@Entity
@Table(name = "clean_expression_fast_search")
@Getter
@Setter
public class CleanExpFastSrch {

    @Id
    @Column(name = "cefs_pk_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cefs_mrkr_zdb_id")
    private Marker gene;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cefs_genox_zdb_id")
    private FishExperiment fishExperiment;
}
