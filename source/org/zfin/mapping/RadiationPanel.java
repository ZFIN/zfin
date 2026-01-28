package org.zfin.mapping;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("Radiation")
@Getter
@Setter
public class RadiationPanel extends Panel {

    @Column(name = "mappanel_rh_rad_dose")
    private String radiationDose;

    @Column(name = "mappanel_rh_num_cell_lines")
    private String numberOfCellLines;
}
