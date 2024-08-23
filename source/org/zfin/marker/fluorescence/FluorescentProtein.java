
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

    @ManyToMany
    @JoinTable(name = "fpProtein_efg",
            // TODO: hibernate migration change, confirm logic still valid
            // Fixes this error: org.hibernate.AnnotationException: Join column '...' on collection property 'org.zfin...' must be defined with the same insertable and updatable attributes
            joinColumns = {@JoinColumn(name = "fe_fl_protein_id", nullable = false, updatable = false, insertable = false)},
            inverseJoinColumns = {@JoinColumn(name = "fe_mrkr_zdb_id", nullable = false, updatable = false, insertable = false)})
    @JsonView(View.API.class)
    private List<Marker> efgs;

    @ManyToMany
    @JoinTable(name = "fpProtein_construct",
            // TODO: hibernate migration change, confirm logic still valid
            // Fixes this error: org.hibernate.AnnotationException: Join column '...' on collection property 'org.zfin...' must be defined with the same insertable and updatable attributes
            joinColumns = {@JoinColumn(name = "fc_fl_protein_id", nullable = false, updatable = false, insertable = false)},
            inverseJoinColumns = {@JoinColumn(name = "fc_mrkr_zdb_id", nullable = false, updatable = false, insertable = false)})
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

}
