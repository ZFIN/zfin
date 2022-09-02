package org.zfin.marker;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
