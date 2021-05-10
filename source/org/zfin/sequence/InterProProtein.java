
package org.zfin.sequence;


import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
@Setter
@Getter

@Entity
@Table(name = "interpro_protein")
public class InterProProtein {
    @Id
    @Column(name = "ip_interpro_id")
    private String ipID;

    @Column(name = "ip_name")
    private String ipName;

    @Column(name = "ip_type")
    private String ipType;

}
