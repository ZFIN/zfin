
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
@Table(name = "fluorescent_protein")
public class FluorescentProtein {

    @Id
    @Column(name = "fp_pk_id")
    private long id;
    @Column(name = "fp_name")
    private String name;
    @Column(name = "fp_emission_length")
    private String emissionLength;
    @Column(name = "fp_excitation_length")
    private String excitationLength;
    @Column(name = "fp_aliases")
    private String aliases;

}
