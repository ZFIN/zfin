package org.zfin.mapping;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.zfin.marker.Marker;

/**
 * Marker Location .
 */
@Setter
@Getter
@Entity
@DiscriminatorValue("Mark")
public class MarkerLocation extends Location {

    @ManyToOne
    @JoinColumn(name = "sfcl_feature_zdb_id")
    protected Marker marker;

}
