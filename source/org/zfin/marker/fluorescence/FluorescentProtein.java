
package org.zfin.marker.fluorescence;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;

import jakarta.persistence.*;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "fluorescent_protein")
public class FluorescentProtein extends AbstractFluorescence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fp_pk_id")
    @JsonView(View.API.class)
    private long id;
    @Column(name = "fp_name")
    @JsonView(View.API.class)
    private String name;
    @Column(name = "fp_emission_length")
    @JsonView(View.API.class)
    private Integer emissionLength;
    @Column(name = "fp_excitation_length")
    @JsonView(View.API.class)
    private Integer excitationLength;
    @Column(name = "fp_emission_color")
    @JsonView(View.API.class)
    private String emissionColor;
    @Column(name = "fp_excitation_color")
    @JsonView(View.API.class)
    private String excitationColor;
    @Column(name = "fp_aliases")
    private String aliases;
    @Column(name = "fp_uuid")
    private String uuid;

    @ManyToMany
    @JoinTable(name = "fpProtein_efg", joinColumns = {
            @JoinColumn(name = "fe_fl_protein_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "fe_mrkr_zdb_id",
                    nullable = false, updatable = false)})
    @JsonView(View.API.class)
    private List<Marker> efgs;

    @ManyToMany
    @JoinTable(name = "fpProtein_construct", joinColumns = {
            @JoinColumn(name = "fc_fl_protein_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "fc_mrkr_zdb_id",
                    nullable = false, updatable = false)})
    @JsonView(View.API.class)
    private List<Marker> constructs;

    @JsonView(View.API.class)
    @JsonProperty("fpId")
    public String getID() {
        int indexOfParen = name.indexOf("(");
        String id = name.toLowerCase();
        if (indexOfParen > 0)
            id = id.substring(0, indexOfParen);
        int indexOfDot = id.indexOf(".");
        if (indexOfDot > 0)
            id = id.replace(".", "");
        return id;
    }

    public long getIdentifier(){
        return id;
    }

    public String toStringSingleLine() {
        return "ID:" + id + " UUID:" + uuid + " Name:" + name + " Emission:" + emissionColor + " Excitation:" + excitationColor + " Emission Length:" + emissionLength + " Excitation Length:" + excitationLength;
    }
}
