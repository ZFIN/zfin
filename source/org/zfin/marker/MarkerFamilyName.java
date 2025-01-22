package org.zfin.marker;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "genedom_family_name")
@Setter
@Getter
public class MarkerFamilyName implements Serializable {

    @Id
    @Column(name = "gfam_name")
    private String markerFamilyName ;
                           
}
