
package org.zfin.sequence;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Getter

@Entity
@Table(name = "protein_to_pdb")
public class ProteinToPDB {

    @Id
    @Column(name = "ptp_pk_id")
    private long ID;
    @Column(name = "ptp_uniprot_id")
    private String uniProtID;
    @Column(name = "ptp_pdb_id")
    private String pdbID;

}
