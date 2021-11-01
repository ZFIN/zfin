
package org.zfin.marker.fluorescence;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "fluorescent_marker")
public class FluorescentMarker extends AbstractFluorescence {

    @Id
    @Column(name = "fm_pk_id")
    private long id;
    @ManyToOne
    @JoinColumn(name = "fm_mrkr_zdb_id")
    @JsonView(View.API.class)
    private Marker efg;

    @JsonView(View.API.class)
    public List<FluorescentProtein> getProteins() {
        return new ArrayList<>(efg.getFluorescentProteins());
    }

    @Column(name = "fm_excitation_length")
    @JsonView(View.API.class)
    private Integer excitationLength;

    @Column(name = "fm_emission_length")
    @JsonView(View.API.class)
    private Integer emissionLength;

    @Column(name = "fm_emission_color")
    @JsonView(View.API.class)
    private String emissionColor;

    @Column(name = "fm_excitation_color")
    @JsonView(View.API.class)
    private String excitationColor;

}
