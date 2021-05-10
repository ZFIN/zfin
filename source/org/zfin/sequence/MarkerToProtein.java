
package org.zfin.sequence;

import org.zfin.marker.Marker;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@Entity
@Table(name = "marker_to_protein")
public class MarkerToProtein {

    @Id
    @Column(name = "mtp_pk_id")
    private long ID;
    @ManyToOne()
    @JoinColumn(name = "mtp_mrkr_zdb_id", referencedColumnName = "mrkr_zdb_id")
    private Marker marker;
    @Column(name = "mtp_uniprot_id")
    private String mtpUniProtID;

}
