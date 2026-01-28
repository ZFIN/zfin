package org.zfin.expression;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;

import java.io.Serializable;

/**
 * The stage information for the image table is located in another
 * table.  This class is necessary for hibernate to make the join.
 */
@Embeddable
@Getter
@Setter
public class ImageStage implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imgstg_start_stg_zdb_id", table = "image_stage")
    private DevelopmentStage start;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imgstg_end_stg_zdb_id", table = "image_stage")
    private DevelopmentStage end;
}
