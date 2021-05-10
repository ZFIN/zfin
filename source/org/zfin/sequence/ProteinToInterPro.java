
package org.zfin.sequence;


import javax.persistence.*;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@Entity
@Table(name = "protein_to_interpro")
public class ProteinToInterPro {

    @Id
    @Column(name = "pti_pk_id")
    private long ID;
    @Column(name = "pti_uniprot_id")
    private String uniProtID;
    @Column(name = "pti_interpro_id")
    private String interProID;

}
