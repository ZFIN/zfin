
package org.zfin.marker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.util.FluorescenceUtil;

import javax.persistence.*;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "efg_fluorescence")
public class EfgFluorescence extends AbstractFluorescence {

    @Id
    @Column(name = "ef_pk_id")
    private long id;
    @ManyToOne
    @JoinColumn(name = "ef_mrkr_zdb_id")
    private Marker efg;

    @OneToMany
    @JoinTable(
            name = "fpProtein_efg",
            joinColumns = @JoinColumn(name = "fe_fl_protein_id"),
            inverseJoinColumns = @JoinColumn(name = "fe_mrkr_zdb_id")
    )
    @JsonView(View.API.class)
    private List<FluorescentProtein> proteins;

    @Column(name = "ef_excitation_length")
    private Integer excitationLength;
    @Column(name = "ef_emission_length")
    private Integer emissionLength;

}
