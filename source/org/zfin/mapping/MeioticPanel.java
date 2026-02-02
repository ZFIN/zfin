package org.zfin.mapping;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("Meiotic  ")
@Getter
@Setter
public class MeioticPanel extends Panel {

    @Column(name = "mappanel_meiotic_num_meioses")
    private int numberOfMeioses;

    @Column(name = "mappanel_meiotic_cross_type")
    private String crossType;
}
