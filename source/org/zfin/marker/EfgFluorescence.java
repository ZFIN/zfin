
package org.zfin.marker;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Getter
@Entity
@Table(name = "efg_fluorescence")
public class EfgFluorescence {

    @Id
    @Column(name = "ef_pk_id")
    private long id;
/*
    @Column(name = "ef_mrkr_zdb_id")
    private Marker efg;
*/
    @Column(name = "ef_excitation_length")
    private String excitationLength;
    @Column(name = "ef_emission_length")
    private String emissionLength;

}
